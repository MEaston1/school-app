package com.app.parentconnect.data.repository

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.app.parentconnect.common.Constants.FAILED
import com.app.parentconnect.common.Constants.IN_PROGRESS
import com.app.parentconnect.common.Constants.MEDICAL_DB
import com.app.parentconnect.common.Constants.MEDICAL_IMAGES_DB
import com.app.parentconnect.common.Constants.SUCCEEDED
import com.app.parentconnect.common.Utils.M_MEM_CACHE
import com.app.parentconnect.data.model.entity.Medical
import com.app.parentconnect.data.model.process.MedicalRequestCall
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import java.util.*

class MedicalRepository {
    private var mutableLiveData: MutableLiveData<MedicalRequestCall> = MutableLiveData()
    fun select(): MutableLiveData<MedicalRequestCall> {
        val mLiveData = MutableLiveData<MedicalRequestCall>()
        val r = MedicalRequestCall()
        r.status = IN_PROGRESS
        r.message = "Fetching Consent Forms Please Wait.."
        mLiveData.value = r
        MEDICAL_DB.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                M_MEM_CACHE.clear()
                if (dataSnapshot.exists() && dataSnapshot.childrenCount > 0) {
                    for (ds in dataSnapshot.children) { //Now get consent form Objects and populate our arraylist.
                        val Medical = ds.getValue(Medical::class.java)
                        if (Medical != null) {
                            Medical.key = ds.key
                            M_MEM_CACHE.add(Medical)
                        }
                    }
                    r.status = SUCCEEDED
                    r.message = "DOWNLOAD COMPLETE"
                } else {
                    r.status = SUCCEEDED
                    r.message = "NO DATA FOUND"
                }
                r.medical = M_MEM_CACHE
                mLiveData.postValue(r)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("PARENT CONNECT", databaseError.message)
                r.status = FAILED
                r.message = databaseError.message
                mLiveData.postValue(r)
            }
        })
        return mLiveData
    }
    fun saveTextLocally(medical: Medical): MutableLiveData<MedicalRequestCall> {
        val r = MedicalRequestCall()
        r.status = IN_PROGRESS
        r.message = "Saving ..."
        mutableLiveData.value = r
        return run {
            r.status = IN_PROGRESS
            r.message = "Saving...Please wait.."
            mutableLiveData.postValue(r)
            //push data to FirebaseDatabase. Table or Child called Consent will be created.
            try{
                if (medical.key.isNullOrEmpty()){
                    MEDICAL_DB.push().setValue(medical);
                }else{
                    MEDICAL_DB.child(medical.key!!).setValue(medical)
                }
                r.status = SUCCEEDED
                r.message = "Congrats!. Successfully Saved."

            }catch (e: Exception){
                r.status = FAILED
                r.message= e.message!!
            }
            mutableLiveData.postValue(r)
            mutableLiveData
        }
    }
    fun updateImageText(medical: Medical, mImageUri: Uri): MutableLiveData<MedicalRequestCall> {
        mutableLiveData = MutableLiveData()
        val r = MedicalRequestCall()
        r.status = IN_PROGRESS
        r.message = "Performing Validation"
        mutableLiveData.value = r
        return (run {
            if (mUploadTask != null && mUploadTask!!.isInProgress) {
                r.status = IN_PROGRESS
                r.message = "An Upload is already in Progress.Please be patient"
                mutableLiveData.postValue(r)
                return mutableLiveData as MutableLiveData<MedicalRequestCall>
            }
            r.status = IN_PROGRESS
            r.message = "Now Uploading Image...Please wait.."
            mutableLiveData.postValue(r)
            val imageRef: StorageReference = MEDICAL_IMAGES_DB.child(mImageUri.lastPathSegment!!)
            mUploadTask = imageRef.putFile(mImageUri)
            (mUploadTask as UploadTask).continueWithTask(
                (Continuation { task: Task<UploadTask.TaskSnapshot?> ->
                    if (!task.isSuccessful) {
                        r.status = IN_PROGRESS
                        r.message = "ERROR ENCOUNTERED: " + task.exception!!.message
                    }
                    imageRef.downloadUrl
                } as Continuation<UploadTask.TaskSnapshot?, Task<Uri>>)
            )
                .addOnCompleteListener { task: Task<Uri?> ->
                    if (task.isSuccessful) {
                        val downloadUri = task.result
                        val url = downloadUri.toString()
                        medical.medicalImageURL = url
                        r.status = IN_PROGRESS
                        r.message = "Image Upload successful. We are now saving text details"
                    } else {
                        r.status = FAILED
                        r.message =
                            "Unfortunately Image Could not be uploaded: FULL DETAILS: " + task.exception!!.message
                    }
                    mutableLiveData.postValue(r)
                    mutableLiveData = updateOnlyText(medical, mutableLiveData)
                }
            mutableLiveData
        })
    }
    private fun uploadOnlyText(medical: Medical): MutableLiveData<MedicalRequestCall> {
        val r = MedicalRequestCall()
        r.status = IN_PROGRESS
        r.message = "Starting Insert.."
        mutableLiveData.value = r
        return run {
            r.status = IN_PROGRESS
            r.message = "Inserting Medical Forms...Please wait.."
            mutableLiveData.postValue(r)
            //push data to FirebaseDatabase. Table or Child called Consent will be created.
            MEDICAL_DB.push().setValue(medical)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        r.status = SUCCEEDED
                        if (medical.medicalImageURL != null && medical.medicalImageURL!!.isNotEmpty()) {
                            r.message = "Congrats! Both Text and Image Inserted Successfully"
                        } else {
                            r.message = "Text Successfully Saved."
                        }
                    } else {
                        r.status = FAILED
                        if (medical.medicalImageURL != null && medical.medicalImageURL!!.isNotEmpty()) {
                            r.message =
                                "Unfortunaletly Text Was Not Inserted. However Image was uploaded. ERROR: " + task.exception!!.message
                        } else {
                            r.message =
                                "Unfortunaletly! Both Text and Image Were Not Inserted: " + task.exception!!.message
                        }
                    }
                    mutableLiveData.postValue(r)
                }
            mutableLiveData
        }
    }
    fun uploadImageText(medical: Medical, mImageUri: Uri): MutableLiveData<MedicalRequestCall> {
        mutableLiveData = MutableLiveData()
        val r = MedicalRequestCall()
        r.status = IN_PROGRESS
        r.message = "Deleting Consent Form"
        mutableLiveData.value = r
        return (run {
            if (mUploadTask != null && mUploadTask!!.isInProgress) {
                r.status = IN_PROGRESS
                r.message = "An Upload is already in Progress.Please be patient"
                mutableLiveData.postValue(r)
                return mutableLiveData
            }
            r.status = IN_PROGRESS
            r.message = "Now Uploading Image...Please wait.."
            mutableLiveData.postValue(r)
            val imageRef: StorageReference = MEDICAL_IMAGES_DB.child(mImageUri.lastPathSegment!!)
            mUploadTask = imageRef.putFile(mImageUri)
            (mUploadTask as UploadTask).continueWithTask(
                (Continuation { task: Task<UploadTask.TaskSnapshot?> ->
                    if (!task.isSuccessful) {
                        r.status = IN_PROGRESS
                        r.message = "ERROR ENCOUNTERED: " + task.exception!!.message
                        mutableLiveData.postValue(r)
                    }
                    imageRef.downloadUrl
                } as Continuation<UploadTask.TaskSnapshot?, Task<Uri>>)
            )
                .addOnCompleteListener { task: Task<Uri?> ->
                    if (task.isSuccessful) {
                        if (task.isSuccessful) {
                            val downloadUri = task.result
                            val url =
                                Objects.requireNonNull(downloadUri)
                                    .toString()
                            medical.medicalImageURL = url
                            r.status =
                                IN_PROGRESS
                            r.message =
                                "Image Upload successful. We are now saving text details"
                        } else {
                            r.status =
                                IN_PROGRESS
                            r.message =
                                "Unfortunately Image Could not be uploaded: FULL DETAILS: " + task.exception!!.message
                        }
                        mutableLiveData.postValue(r)
                        mutableLiveData = uploadOnlyText(medical)
                    } else {
                        r.status = FAILED
                        r.message =
                            "Unfortunately Image Could not be uploaded: FULL DETAILS: " + task.exception!!.message
                        mutableLiveData.postValue(r)
                    }
                }
            mutableLiveData
        })
    }
    private fun updateOnlyText(medical: Medical, mLiveData: MutableLiveData<MedicalRequestCall>): MutableLiveData<MedicalRequestCall> {
        val r = MedicalRequestCall()
        r.status = IN_PROGRESS
        r.message = "Starting Update.."
        mLiveData.value = r
        return run {
            r.status = IN_PROGRESS
            r.message = "Updating News...Please wait.."
            mLiveData.postValue(r)
            val finalLiveData: MutableLiveData<MedicalRequestCall> = mLiveData
            MEDICAL_DB.child(medical.key!!).setValue(medical)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        r.status = SUCCEEDED
                        if (medical.medicalImageURL != null && medical.medicalImageURL!!.isNotEmpty()) {
                            r.message = "Congrats! Both Text and Image Updated Successfully"
                        } else {
                            r.message =
                                "Text Successfully Updated. However Image was not Uploaded."
                        }
                    } else {
                        r.status = FAILED
                        if (medical.medicalImageURL != null && medical.medicalImageURL!!.isNotEmpty()) {
                            r.message =
                                "Unfortunaletly Text Was Not Updated. However Image was uploaded. ERROR: " + task.exception!!.message
                        } else {
                            r.message =
                                "Unfortunaletly! Both Text and Image Were Not Updated: " + task.exception!!.message
                        }
                    }
                    finalLiveData.postValue(r)
                }
            finalLiveData
        }
    }
    fun deleteImageText(selectedMedical: Medical): MutableLiveData<MedicalRequestCall> {
        mutableLiveData = MutableLiveData()
        val r = MedicalRequestCall()
        r.status = IN_PROGRESS
        r.message = "Performing Validation"
        mutableLiveData.value = r
        return run {
            r.status = IN_PROGRESS
            r.message = "Deleting Consent Form...Please wait.."
            mutableLiveData.postValue(r)
            val selectedStarKey = selectedMedical.key
            MEDICAL_DB.child(selectedStarKey!!).removeValue()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        r.status = IN_PROGRESS
                        r.message =
                            selectedMedical.medicalChildName + " text REMOVED DELETED SUCCESSFULLY..Now deleting image"
                        mutableLiveData = deleteOnlyImage(selectedMedical.medicalImageURL!!, true)
                    } else {
                        r.status = FAILED
                        r.message = "UNSUCCESSFUL: " + task.exception!!.message
                    }
                    mutableLiveData.postValue(r)
                }
            mutableLiveData
        }
    }
    private fun deleteOnlyImage(imageURL: String,textWasDeleted: Boolean): MutableLiveData<MedicalRequestCall> {
        val r = MedicalRequestCall()
        r.status = IN_PROGRESS
        r.message = "Deleting Image..Now"
        mutableLiveData.value = r
        if (imageURL.isEmpty()) {
            r.status = FAILED
            if (textWasDeleted) {
                r.message =
                    "While Text was deleted Image could not because Null Image URL of null image url."
            } else {
                r.message = "Image cannot not be deleted because of Null Image URL."
            }
            return mutableLiveData
        }
        if (!textWasDeleted) {
            r.status = FAILED
            r.message =
                "Image Not Deleted Because it's URL is still saved in Database. This means Text deletion was not successful"
            return mutableLiveData
        }
        val imageRef: StorageReference
        try {
            imageRef = mStorage.getReferenceFromUrl(imageURL)
        } catch (e: Exception) {
            r.status = FAILED
            r.message =
                "While Text was deleted, image could not because a Wrong Image URL was specified. DETAILS: " + e.message
            return mutableLiveData
        }
        r.status = IN_PROGRESS
        r.message = "Text Already Deleted...Now Deleting Image. Please wait..."
        imageRef.delete().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                r.status = SUCCEEDED
                r.message = "Congrats! Both Image and Text Deleted Successfully"
            } else {
                r.status = FAILED
                r.message =
                    "While Text was Deleted Successfully, Image Was Not Deleted. DETAILS: " + task.exception!!.message
            }
            mutableLiveData.postValue(r)
        }
        return mutableLiveData
    }
    fun search(searchTerm: String): MutableLiveData<MedicalRequestCall> {
        var searchTerm = searchTerm
        val mLiveData = MutableLiveData<MedicalRequestCall>()
        val r = MedicalRequestCall()
        r.status = IN_PROGRESS
        r.message = "Fetching Consent Form Please Wait.."
        mLiveData.value = r
        if (searchTerm.isNotEmpty()) {
            val letters = searchTerm.toCharArray()
            val firstLetter = letters[0].toString().toUpperCase()
            val remainingLetters = searchTerm.substring(1)
            searchTerm = firstLetter + remainingLetters
        }
        val firebaseSearchQuery: Query =
            MEDICAL_DB.orderByChild("name").startAt(searchTerm)
                .endAt(searchTerm + "\uf8ff")
        val tempSearchTerm = searchTerm
        firebaseSearchQuery.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                M_MEM_CACHE.clear()
                if (dataSnapshot.exists() && dataSnapshot.childrenCount > 0) {
                    for (ds in dataSnapshot.children) { //Now get Consent Objects and populate our arraylist.
                        val medical = ds.getValue(Medical::class.java)!!
                        medical.key = ds.key
                        M_MEM_CACHE.add(medical)
                    }
                    r.status = SUCCEEDED
                    r.message = "Search Complete"
                } else {
                    r.status = SUCCEEDED
                    r.message = String.format("%s Not Found", tempSearchTerm)
                }
                r.medical = M_MEM_CACHE
                mLiveData.postValue(r)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("Parent connect failure", databaseError.message)
                r.status = FAILED
                r.message = databaseError.message
                mLiveData.postValue(r)
            }
        })
        return mLiveData
    }
    companion object {
        private val mStorage = FirebaseStorage.getInstance()
        private var mUploadTask: StorageTask<*>? = null
    }
}
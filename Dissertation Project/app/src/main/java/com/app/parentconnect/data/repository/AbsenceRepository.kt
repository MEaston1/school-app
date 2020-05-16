package com.app.parentconnect.data.repository

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
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
import com.app.parentconnect.common.Constants.ABSENCE_DB
import com.app.parentconnect.common.Constants.FAILED
import com.app.parentconnect.common.Constants.ABSENCE_IMAGES_DB
import com.app.parentconnect.common.Constants.IN_PROGRESS
import com.app.parentconnect.common.Constants.SUCCEEDED
import com.app.parentconnect.common.Utils.A_MEM_CACHE
import com.app.parentconnect.data.model.entity.Absence
import com.app.parentconnect.data.model.process.AbsenceRequestCall
import java.util.*

class AbsenceRepository {
    private var mutableLiveData: MutableLiveData<AbsenceRequestCall> = MutableLiveData()
    fun select(): MutableLiveData<AbsenceRequestCall> {
        val mLiveData = MutableLiveData<AbsenceRequestCall>()
        val r = AbsenceRequestCall()
        r.status = IN_PROGRESS
        r.message = "Fetching Absence Reports Please Wait.."
        mLiveData.value = r
        ABSENCE_DB.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                A_MEM_CACHE.clear()
                if (dataSnapshot.exists() && dataSnapshot.childrenCount > 0) {
                    for (ds in dataSnapshot.children) { //Now get News Objects and populate our arraylist.
                        val Absence = ds.getValue(Absence::class.java)
                        if (Absence != null) {
                            Absence.key = ds.key
                            A_MEM_CACHE.add(Absence)
                        }
                    }
                    r.status = SUCCEEDED
                    r.message = "DOWNLOAD COMPLETE"
                } else {
                    r.status = SUCCEEDED
                    r.message = "NO DATA FOUND"
                }
                r.absence = A_MEM_CACHE
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
    fun saveTextLocally(absence: Absence): MutableLiveData<AbsenceRequestCall> {
        val r = AbsenceRequestCall()
        r.status = IN_PROGRESS
        r.message = "Saving ..."
        mutableLiveData.value = r
        return run {
            r.status = IN_PROGRESS
            r.message = "Saving...Please wait.."
            mutableLiveData.postValue(r)
            //push data to FirebaseDatabase. Table or Child called News will be created.
            try{
                if (absence.key.isNullOrEmpty()){
                    ABSENCE_DB.push().setValue(absence);
                }else{
                    ABSENCE_DB.child(absence.key!!).setValue(absence)
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
    private fun uploadOnlyText(absence: Absence): MutableLiveData<AbsenceRequestCall> {
        val r = AbsenceRequestCall()
        r.status = IN_PROGRESS
        r.message = "Starting Insert.."
        mutableLiveData.value = r
        return run {
            r.status = IN_PROGRESS
            r.message = "Inserting Absence Reports...Please wait.."
            mutableLiveData.postValue(r)
            //push data to FirebaseDatabase. Table or Child called News will be created.
            ABSENCE_DB.push().setValue(absence)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        r.status = SUCCEEDED
                        if (absence.absenceImageURL != null && absence.absenceImageURL!!.isNotEmpty()) {
                            r.message = "Congrats! Both Text and Image Inserted Successfully"
                        } else {
                            r.message = "Text Successfully Saved."
                        }
                    } else {
                        r.status = FAILED
                        if (absence.absenceImageURL != null && absence.absenceImageURL!!.isNotEmpty()) {
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
    fun updateImageText(absence: Absence, mImageUri: Uri): MutableLiveData<AbsenceRequestCall> {
        mutableLiveData = MutableLiveData()
        val r = AbsenceRequestCall()
        r.status = IN_PROGRESS
        r.message = "Performing Validation"
        mutableLiveData.value = r
        return (run {
            if (mUploadTask != null && mUploadTask!!.isInProgress) {
                r.status = IN_PROGRESS
                r.message = "An Upload is already in Progress.Please be patient"
                mutableLiveData.postValue(r)
                return mutableLiveData as MutableLiveData<AbsenceRequestCall>
            }
            r.status = IN_PROGRESS
            r.message = "Now Uploading Image...Please wait.."
            mutableLiveData.postValue(r)
            val imageRef: StorageReference = ABSENCE_IMAGES_DB.child(mImageUri.lastPathSegment!!)
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
                        absence.absenceImageURL = url
                        r.status = IN_PROGRESS
                        r.message = "Image Upload successful. We are now saving text details"
                    } else {
                        r.status = FAILED
                        r.message =
                            "Unfortunately Image Could not be uploaded: FULL DETAILS: " + task.exception!!.message
                    }
                    mutableLiveData.postValue(r)
                    mutableLiveData = updateOnlyText(absence, mutableLiveData)
                }
            mutableLiveData
        })
    }
    fun uploadImageText(absence: Absence, mImageUri: Uri): MutableLiveData<AbsenceRequestCall> {
        mutableLiveData = MutableLiveData()
        val r = AbsenceRequestCall()
        r.status = IN_PROGRESS
        r.message = "Deleting News"
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
            val imageRef: StorageReference = ABSENCE_IMAGES_DB.child(mImageUri.lastPathSegment!!)
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
                            absence.absenceImageURL = url
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
                        mutableLiveData = uploadOnlyText(absence)
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
    private fun updateOnlyText(absence: Absence, mLiveData: MutableLiveData<AbsenceRequestCall>): MutableLiveData<AbsenceRequestCall> {
        val r = AbsenceRequestCall()
        r.status = IN_PROGRESS
        r.message = "Starting Update.."
        mLiveData.value = r
        return run {
            r.status = IN_PROGRESS
            r.message = "Updating News...Please wait.."
            mLiveData.postValue(r)
            val finalLiveData: MutableLiveData<AbsenceRequestCall> = mLiveData
            ABSENCE_DB.child(absence.key!!).setValue(absence)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        r.status = SUCCEEDED
                        if (absence.absenceImageURL != null && absence.absenceImageURL!!.isNotEmpty()) {
                            r.message = "Congrats! Both Text and Image Updated Successfully"
                        } else {
                            r.message =
                                "Text Successfully Updated. However Image was not Uploaded."
                        }
                    } else {
                        r.status = FAILED
                        if (absence.absenceImageURL != null && absence.absenceImageURL!!.isNotEmpty()) {
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
    fun deleteImageText(selectedAbsence: Absence): MutableLiveData<AbsenceRequestCall> {
        mutableLiveData = MutableLiveData()
        val r = AbsenceRequestCall()
        r.status = IN_PROGRESS
        r.message = "Performing Validation"
        mutableLiveData.value = r
        return run {
            r.status = IN_PROGRESS
            r.message = "Deleting News...Please wait.."
            mutableLiveData.postValue(r)
            val selectedStarKey = selectedAbsence.key
            ABSENCE_DB.child(selectedStarKey!!).removeValue()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        r.status = IN_PROGRESS
                        r.message =
                            selectedAbsence.childName + " text REMOVED DELETED SUCCESSFULLY..Now deleting image"
                        mutableLiveData = deleteOnlyImage(selectedAbsence.absenceImageURL!!, true)
                    } else {
                        r.status = FAILED
                        r.message = "UNSUCCESSFUL: " + task.exception!!.message
                    }
                    mutableLiveData.postValue(r)
                }
            mutableLiveData
        }
    }
    private fun deleteOnlyImage(imageURL: String,textWasDeleted: Boolean
    ): MutableLiveData<AbsenceRequestCall> {
        val r = AbsenceRequestCall()
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
    fun search(searchTerm: String): MutableLiveData<AbsenceRequestCall> {
        var searchTerm = searchTerm
        val mLiveData = MutableLiveData<AbsenceRequestCall>()
        val r = AbsenceRequestCall()
        r.status = IN_PROGRESS
        r.message = "Fetching News Please Wait.."
        mLiveData.value = r
        if (searchTerm.isNotEmpty()) {
            val letters = searchTerm.toCharArray()
            val firstLetter = letters[0].toString().toUpperCase()
            val remainingLetters = searchTerm.substring(1)
            searchTerm = firstLetter + remainingLetters
        }
        val firebaseSearchQuery: Query =
            ABSENCE_DB.orderByChild("name").startAt(searchTerm)
                .endAt(searchTerm + "\uf8ff")
        val tempSearchTerm = searchTerm
        firebaseSearchQuery.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                A_MEM_CACHE.clear()
                if (dataSnapshot.exists() && dataSnapshot.childrenCount > 0) {
                    for (ds in dataSnapshot.children) { //Now get Absence Objects and populate our arraylist.
                        val absence = ds.getValue(Absence::class.java)!!
                        absence.key = ds.key
                        A_MEM_CACHE.add(absence)
                    }
                    r.status = SUCCEEDED
                    r.message = "Search Complete"
                } else {
                    r.status = SUCCEEDED
                    r.message = String.format("%s Not Found", tempSearchTerm)
                }
                r.absence = A_MEM_CACHE
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
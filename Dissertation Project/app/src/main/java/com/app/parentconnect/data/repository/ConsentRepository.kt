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
import com.app.parentconnect.common.Constants.CONSENT_DB
import com.app.parentconnect.common.Constants.FAILED
import com.app.parentconnect.common.Constants.CONSENT_IMAGES_DB
import com.app.parentconnect.common.Constants.IN_PROGRESS
import com.app.parentconnect.common.Constants.SUCCEEDED
import com.app.parentconnect.common.Utils.C_MEM_CACHE
import com.app.parentconnect.data.model.entity.Consent
import com.app.parentconnect.data.model.process.ConsentRequestCall
import java.util.*


class ConsentRepository {
    private var mutableLiveData: MutableLiveData<ConsentRequestCall> = MutableLiveData()
    fun select(): MutableLiveData<ConsentRequestCall> {
        val mLiveData = MutableLiveData<ConsentRequestCall>()
        val r = ConsentRequestCall()
        r.status = IN_PROGRESS
        r.message = "Fetching Consent Forms Please Wait.."
        mLiveData.value = r
        CONSENT_DB.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                C_MEM_CACHE.clear()
                if (dataSnapshot.exists() && dataSnapshot.childrenCount > 0) {
                    for (ds in dataSnapshot.children) { //Now get consent form Objects and populate our arraylist.
                        val Consent = ds.getValue(Consent::class.java)
                        if (Consent != null) {
                            Consent.key = ds.key
                            C_MEM_CACHE.add(Consent)
                        }
                    }
                    r.status = SUCCEEDED
                    r.message = "DOWNLOAD COMPLETE"
                } else {
                    r.status = SUCCEEDED
                    r.message = "NO DATA FOUND"
                }
                r.consent = C_MEM_CACHE
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
    fun saveTextLocally(consent: Consent): MutableLiveData<ConsentRequestCall> {
        val r = ConsentRequestCall()
        r.status = IN_PROGRESS
        r.message = "Saving ..."
        mutableLiveData.value = r
        return run {
            r.status = IN_PROGRESS
            r.message = "Saving...Please wait.."
            mutableLiveData.postValue(r)
            //push data to FirebaseDatabase. Table or Child called Consent will be created.
            try{
                if (consent.key.isNullOrEmpty()){
                    CONSENT_DB.push().setValue(consent);
                }else{
                    CONSENT_DB.child(consent.key!!).setValue(consent)
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
    fun updateImageText(consent: Consent, mImageUri: Uri): MutableLiveData<ConsentRequestCall> {
        mutableLiveData = MutableLiveData()
        val r = ConsentRequestCall()
        r.status = IN_PROGRESS
        r.message = "Performing Validation"
        mutableLiveData.value = r
        return (run {
            if (mUploadTask != null && mUploadTask!!.isInProgress) {
                r.status = IN_PROGRESS
                r.message = "An Upload is already in Progress.Please be patient"
                mutableLiveData.postValue(r)
                return mutableLiveData as MutableLiveData<ConsentRequestCall>
            }
            r.status = IN_PROGRESS
            r.message = "Now Uploading Image...Please wait.."
            mutableLiveData.postValue(r)
            val imageRef: StorageReference = CONSENT_IMAGES_DB.child(mImageUri.lastPathSegment!!)
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
                        consent.consentImageURL = url
                        r.status = IN_PROGRESS
                        r.message = "Image Upload successful. We are now saving text details"
                    } else {
                        r.status = FAILED
                        r.message =
                            "Unfortunately Image Could not be uploaded: FULL DETAILS: " + task.exception!!.message
                    }
                    mutableLiveData.postValue(r)
                    mutableLiveData = updateOnlyText(consent, mutableLiveData)
                }
            mutableLiveData
        })
    }
    private fun uploadOnlyText(consent: Consent): MutableLiveData<ConsentRequestCall> {
        val r = ConsentRequestCall()
        r.status = IN_PROGRESS
        r.message = "Starting Insert.."
        mutableLiveData.value = r
        return run {
            r.status = IN_PROGRESS
            r.message = "Inserting Consent Forms...Please wait.."
            mutableLiveData.postValue(r)
            //push data to FirebaseDatabase. Table or Child called Consent will be created.
            CONSENT_DB.push().setValue(consent)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        r.status = SUCCEEDED
                        if (consent.consentImageURL != null && consent.consentImageURL!!.isNotEmpty()) {
                            r.message = "Congrats! Both Text and Image Inserted Successfully"
                        } else {
                            r.message = "Text Successfully Saved."
                        }
                    } else {
                        r.status = FAILED
                        if (consent.consentImageURL != null && consent.consentImageURL!!.isNotEmpty()) {
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
    fun uploadImageText(consent: Consent, mImageUri: Uri): MutableLiveData<ConsentRequestCall> {
        mutableLiveData = MutableLiveData()
        val r = ConsentRequestCall()
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
            val imageRef: StorageReference = CONSENT_IMAGES_DB.child(mImageUri.lastPathSegment!!)
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
                            consent.consentImageURL = url
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
                        mutableLiveData = uploadOnlyText(consent)
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
    private fun updateOnlyText(consent: Consent, mLiveData: MutableLiveData<ConsentRequestCall>): MutableLiveData<ConsentRequestCall> {
        val r = ConsentRequestCall()
        r.status = IN_PROGRESS
        r.message = "Starting Update.."
        mLiveData.value = r
        return run {
            r.status = IN_PROGRESS
            r.message = "Updating News...Please wait.."
            mLiveData.postValue(r)
            val finalLiveData: MutableLiveData<ConsentRequestCall> = mLiveData
            CONSENT_DB.child(consent.key!!).setValue(consent)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        r.status = SUCCEEDED
                        if (consent.consentImageURL != null && consent.consentImageURL!!.isNotEmpty()) {
                            r.message = "Congrats! Both Text and Image Updated Successfully"
                        } else {
                            r.message =
                                "Text Successfully Updated. However Image was not Uploaded."
                        }
                    } else {
                        r.status = FAILED
                        if (consent.consentImageURL != null && consent.consentImageURL!!.isNotEmpty()) {
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
    fun deleteImageText(selectedConsent: Consent): MutableLiveData<ConsentRequestCall> {
        mutableLiveData = MutableLiveData()
        val r = ConsentRequestCall()
        r.status = IN_PROGRESS
        r.message = "Performing Validation"
        mutableLiveData.value = r
        return run {
            r.status = IN_PROGRESS
            r.message = "Deleting Consent Form...Please wait.."
            mutableLiveData.postValue(r)
            val selectedStarKey = selectedConsent.key
            CONSENT_DB.child(selectedStarKey!!).removeValue()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        r.status = IN_PROGRESS
                        r.message =
                            selectedConsent.consentChildName + " text REMOVED DELETED SUCCESSFULLY..Now deleting image"
                        mutableLiveData = deleteOnlyImage(selectedConsent.consentImageURL!!, true)
                    } else {
                        r.status = FAILED
                        r.message = "UNSUCCESSFUL: " + task.exception!!.message
                    }
                    mutableLiveData.postValue(r)
                }
            mutableLiveData
        }
    }
    private fun deleteOnlyImage(imageURL: String,textWasDeleted: Boolean): MutableLiveData<ConsentRequestCall> {
        val r = ConsentRequestCall()
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
    fun search(searchTerm: String): MutableLiveData<ConsentRequestCall> {
        var searchTerm = searchTerm
        val mLiveData = MutableLiveData<ConsentRequestCall>()
        val r = ConsentRequestCall()
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
            CONSENT_DB.orderByChild("name").startAt(searchTerm)
                .endAt(searchTerm + "\uf8ff")
        val tempSearchTerm = searchTerm
        firebaseSearchQuery.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                C_MEM_CACHE.clear()
                if (dataSnapshot.exists() && dataSnapshot.childrenCount > 0) {
                    for (ds in dataSnapshot.children) { //Now get Consent Objects and populate our arraylist.
                        val consent = ds.getValue(Consent::class.java)!!
                        consent.key = ds.key
                        C_MEM_CACHE.add(consent)
                    }
                    r.status = SUCCEEDED
                    r.message = "Search Complete"
                } else {
                    r.status = SUCCEEDED
                    r.message = String.format("%s Not Found", tempSearchTerm)
                }
                r.consent = C_MEM_CACHE
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
package com.example.schoolapp.data.repository

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
import com.example.schoolapp.common.Constants.DB
import com.example.schoolapp.common.Constants.FAILED
import com.example.schoolapp.common.Constants.IMAGES_DB
import com.example.schoolapp.common.Constants.IN_PROGRESS
import com.example.schoolapp.common.Constants.SUCCEEDED
import com.example.schoolapp.common.Utils.MEM_CACHE
import com.example.schoolapp.data.model.entity.News
import com.example.schoolapp.data.model.process.RequestCall
import com.google.firebase.auth.FirebaseAuth
import java.util.*


class NewsRepository {
    private var mutableLiveData: MutableLiveData<RequestCall> = MutableLiveData()
    fun select(): MutableLiveData<RequestCall> {
        val mLiveData = MutableLiveData<RequestCall>()
        val r = RequestCall()
        r.status = IN_PROGRESS
        r.message = "Fetching News Please Wait.."
        mLiveData.value = r
        DB.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                MEM_CACHE.clear()
                if (dataSnapshot.exists() && dataSnapshot.childrenCount > 0) {
                    for (ds in dataSnapshot.children) { //Now get News Objects and populate our arraylist.
                        val news = ds.getValue(News::class.java)
                        if (news != null) {
                            news.key = ds.key
                            MEM_CACHE.add(news)
                        }
                    }
                    r.status = SUCCEEDED
                    r.message = "DOWNLOAD COMPLETE"
                } else {
                    r.status = SUCCEEDED
                    r.message = "NO DATA FOUND"
                }
                r.news = MEM_CACHE
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

    fun saveTextLocally(news: News): MutableLiveData<RequestCall> {
        val r = RequestCall()
        r.status = IN_PROGRESS
        r.message = "Saving ..."
        mutableLiveData.value = r
        return run {
            r.status = IN_PROGRESS
            r.message = "Saving...Please wait.."
            mutableLiveData.postValue(r)
            //push data to FirebaseDatabase. Table or Child called News will be created.
            try{
                if (news.key.isNullOrEmpty()){
                    DB.push().setValue(news);
                }else{
                    DB.child(news.key!!).setValue(news)
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

    private fun uploadOnlyText(news: News): MutableLiveData<RequestCall> {
        val r = RequestCall()
        r.status = IN_PROGRESS
        r.message = "Starting Insert.."
        mutableLiveData.value = r
        return run {
            r.status = IN_PROGRESS
            r.message = "Inserting News Text...Please wait.."
            mutableLiveData.postValue(r)
            //push data to FirebaseDatabase. Table or Child called News will be created.
            DB.push().setValue(news)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        r.status = SUCCEEDED
                        if (news.imageURL != null && news.imageURL!!.isNotEmpty()) {
                            r.message = "Congrats! Both Text and Image Inserted Successfully"
                        } else {
                            r.message = "Text Successfully Saved."
                        }
                    } else {
                        r.status = FAILED
                        if (news.imageURL != null && news.imageURL!!.isNotEmpty()) {
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

    fun uploadImageText(news: News, mImageUri: Uri): MutableLiveData<RequestCall> {
        mutableLiveData = MutableLiveData()
        val r = RequestCall()
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
            val imageRef: StorageReference = IMAGES_DB.child(mImageUri.lastPathSegment!!)
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
                            news.imageURL = url
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
                        mutableLiveData = uploadOnlyText(news)
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

    private fun updateOnlyText(news: News, mLiveData: MutableLiveData<RequestCall>): MutableLiveData<RequestCall> {
        val r = RequestCall()
        r.status = IN_PROGRESS
        r.message = "Starting Update.."
        mLiveData.value = r
        return run {
            r.status = IN_PROGRESS
            r.message = "Updating News...Please wait.."
            mLiveData.postValue(r)
            val finalLiveData: MutableLiveData<RequestCall> = mLiveData
            DB.child(news.key!!).setValue(news)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        r.status = SUCCEEDED
                        if (news.imageURL != null && news.imageURL!!.isNotEmpty()) {
                            r.message = "Congrats! Both Text and Image Updated Successfully"
                        } else {
                            r.message =
                                "Text Successfully Updated. However Image was not Uploaded."
                        }
                    } else {
                        r.status = FAILED
                        if (news.imageURL != null && news.imageURL!!.isNotEmpty()) {
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

    fun updateImageText(news: News, mImageUri: Uri): MutableLiveData<RequestCall> {
        mutableLiveData = MutableLiveData()
        val r = RequestCall()
        r.status = IN_PROGRESS
        r.message = "Performing Validation"
        mutableLiveData.value = r
        return (run {
            if (mUploadTask != null && mUploadTask!!.isInProgress) {
                r.status = IN_PROGRESS
                r.message = "An Upload is already in Progress.Please be patient"
                mutableLiveData.postValue(r)
                return mutableLiveData as MutableLiveData<RequestCall>
            }
            r.status = IN_PROGRESS
            r.message = "Now Uploading Image...Please wait.."
            mutableLiveData.postValue(r)
            val imageRef: StorageReference = IMAGES_DB.child(mImageUri.lastPathSegment!!)
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
                        news.imageURL = url
                        r.status = IN_PROGRESS
                        r.message = "Image Upload successful. We are now saving text details"
                    } else {
                        r.status = FAILED
                        r.message =
                            "Unfortunately Image Could not be uploaded: FULL DETAILS: " + task.exception!!.message
                    }
                    mutableLiveData.postValue(r)
                    mutableLiveData = updateOnlyText(news, mutableLiveData)
                }
            mutableLiveData
        })
    }

    fun deleteImageText(selectedNews: News): MutableLiveData<RequestCall> {
        mutableLiveData = MutableLiveData()
        val r = RequestCall()
        r.status = IN_PROGRESS
        r.message = "Performing Validation"
        mutableLiveData.value = r
        return run {
            r.status = IN_PROGRESS
            r.message = "Deleting News...Please wait.."
            mutableLiveData.postValue(r)
            val selectedStarKey = selectedNews.key
            DB.child(selectedStarKey!!).removeValue()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        r.status = IN_PROGRESS
                        r.message =
                            selectedNews.title + " text REMOVED DELETED SUCCESSFULLY..Now deleting image"
                        mutableLiveData = deleteOnlyImage(selectedNews.imageURL!!, true)
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
    ): MutableLiveData<RequestCall> {
        val r = RequestCall()
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

    fun search(searchTerm: String): MutableLiveData<RequestCall> {
        var searchTerm = searchTerm
        val mLiveData = MutableLiveData<RequestCall>()
        val r = RequestCall()
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
            DB.orderByChild("name").startAt(searchTerm)
                .endAt(searchTerm + "\uf8ff")
        val tempSearchTerm = searchTerm
        firebaseSearchQuery.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                MEM_CACHE.clear()
                if (dataSnapshot.exists() && dataSnapshot.childrenCount > 0) {
                    for (ds in dataSnapshot.children) { //Now get News Objects and populate our arraylist.
                        val news = ds.getValue(News::class.java)!!
                        news.key = ds.key
                        MEM_CACHE.add(news)
                    }
                    r.status = SUCCEEDED
                    r.message = "Search Complete"
                } else {
                    r.status = SUCCEEDED
                    r.message = String.format("%s Not Found", tempSearchTerm)
                }
                r.news = MEM_CACHE
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

    fun login(email: String, password: String): MutableLiveData<RequestCall> {
        val mLiveData = MutableLiveData<RequestCall>()
        val r = RequestCall()
        r.status = IN_PROGRESS
        r.message = "Signing You In...Please wait"
        mLiveData.value = r
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    r.status = SUCCEEDED
                    r.message = "Congrats! You have Logged in Successfully"
                } else {
                    r.status = FAILED
                    r.message = "Unsuccessful. Details: " + task.exception!!.message
                }
                mLiveData.postValue(r)
            }
        return mLiveData
    }

    companion object {
        private val mStorage = FirebaseStorage.getInstance()
        private var mUploadTask: StorageTask<*>? = null
    }
}
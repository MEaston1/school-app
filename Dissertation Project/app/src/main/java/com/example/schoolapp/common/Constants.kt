package com.example.schoolapp.common

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.example.schoolapp.R

object Constants {
    //The following line points us to the location of our database
    @JvmField
    val DB = FirebaseDatabase.getInstance()
        .getReference("teacher_announcements")
    //this next one is for absence forms
    @JvmField
    val ABSENCE_DB = FirebaseDatabase.getInstance()
        .getReference("absence_reports")
    @JvmField
    val CONSENT_DB = FirebaseDatabase.getInstance()
        .getReference("consent_forms")
    @JvmField
    val MEDICAL_DB = FirebaseDatabase.getInstance()
        .getReference("medical_forms")
    //The following points us to where our images will be stored
    @JvmField
    val IMAGES_DB = FirebaseStorage.getInstance()
        .getReference("teacher_announcements")
    @JvmField
    val ABSENCE_IMAGES_DB = FirebaseStorage.getInstance()
        .getReference("absence_reports")
    @JvmField
    val CONSENT_IMAGES_DB = FirebaseStorage.getInstance()
        .getReference("consent_forms")
    @JvmField
    val MEDICAL_IMAGES_DB = FirebaseStorage.getInstance()
        .getReference("medical_forms")

    val ADMIN_EMAIL = "clarkkent@gmail.com"
    val EDITOR_1_EMAIL = "johndoe@gmail.com"

    //Different states of our requests
    const val FAILED = -1
    const val IN_PROGRESS = 0
    const val SUCCEEDED = 1

    //An Array of local images. We will show one of them randomly as our dashboard banner
    @JvmField
    val LOCAL_IMAGES = intArrayOf(
        R.drawable.speaker, R.drawable.school
    )
}
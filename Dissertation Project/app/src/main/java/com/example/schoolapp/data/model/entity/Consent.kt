package com.example.schoolapp.data.model.entity

import com.google.firebase.database.Exclude
import java.io.Serializable

class Consent: Serializable {
    var childName: String? = ""
    var tripName: String? = ""
    var parentPhoneNumber: String? = ""
    var consentVerification: String? = ""
    var datePublished: String? = ""
    var views: String? = "0"
    var publisher: String? = ""
    var imageURL: String? = ""

    @get:Exclude
    @set:Exclude
    var key: String? = ""

    override fun toString(): String {
        return childName!!
    }

}
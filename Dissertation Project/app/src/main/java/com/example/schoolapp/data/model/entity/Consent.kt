package com.example.schoolapp.data.model.entity

import com.google.firebase.database.Exclude
import java.io.Serializable

class Consent: Serializable {
    var consentChildName: String? = ""
    var eventName: String? = ""
    var consentParentPhoneNumber: String? = ""
    var consentVerification: String? = ""
    var datePublished: String? = ""
    var dateConsentUpdated: String? = ""
    var views: String? = "0"
    var publisher: String? = ""
    var consentImageURL: String? = ""

    @get:Exclude
    @set:Exclude
    var key: String? = ""

    override fun toString(): String {
        return consentChildName!!
    }

}
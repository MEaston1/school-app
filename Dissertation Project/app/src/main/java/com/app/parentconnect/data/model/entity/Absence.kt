package com.app.parentconnect.data.model.entity

import com.google.firebase.database.Exclude
import java.io.Serializable

class Absence: Serializable {
    var childName: String? = ""
    var absenceReason: String? = ""
    var durationExpected: String? = ""
    var dateUpdated: String? = ""
    var datePublished: String? = ""
    var views: String? = "0"
    var publisher: String? = ""
    var absenceImageURL: String? = ""

    @get:Exclude
    @set:Exclude
    var key: String? = ""

    override fun toString(): String {
        return childName!!
    }

}
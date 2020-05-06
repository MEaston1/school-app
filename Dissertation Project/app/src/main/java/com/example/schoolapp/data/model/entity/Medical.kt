package com.example.schoolapp.data.model.entity

import com.google.firebase.database.Exclude
import java.io.Serializable

/*
  This is the News Class for the medical forms. It's roles are:
  1. Define the properties of our medical/allergy item.
  2. Assign Default News values using the elvis operator
 */
class Medical: Serializable {
    var childName: String? = ""
    var medicalDescription: String? = ""
    var allergy: String? = ""
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
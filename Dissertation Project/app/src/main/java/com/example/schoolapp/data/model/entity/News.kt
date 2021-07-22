package com.example.schoolapp.data.model.entity

import com.google.firebase.database.Exclude
import java.io.Serializable

/*
  This is the News Class for the teacher announcements. It's roles are:
  1. Define the properties of our News item.
  2. Assign Default News values using the elvis operator
 */
class News : Serializable {
    var title: String? = ""
    var content: String? = ""
    var country: String? = ""
    var tags: String? = ""
    var datePublished: String? = ""
    var dateUpdated: String? = ""
    var views: String? = "0"
    var publisher: String? = ""
    var imageURL: String? = ""

    @get:Exclude
    @set:Exclude
    var key: String? = ""

    override fun toString(): String {
        return title!!
    }

}
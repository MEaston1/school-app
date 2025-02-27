package com.example.schoolapp.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class User(val uid: String, val username: String, val profileImageUrl: String, val permsStatus: String): Parcelable {
  constructor() : this("", "", "", "")
}
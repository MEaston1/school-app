package com.example.schoolapp.data.model.process

import com.example.schoolapp.data.model.entity.Absence
import com.example.schoolapp.data.model.entity.Consent
import com.example.schoolapp.data.model.entity.Medical

// This file contains request calls for the smaller operations besides announcements

class ConsentRequestCall {
    //A single request will have the following attributes
    var status = 0
    var message: String = "NO MESSAGE"
    var consent: List<Consent> = ArrayList()
}
class AbsenceRequestCall {
    //A single request will have the following attributes
    var status = 0
    var message: String = "NO MESSAGE"
    var absence: List<Absence> = ArrayList()
}
class MedicalRequestCall {
    //A single request will have the following attributes
    var status = 0
    var message: String = "NO MESSAGE"
    var medical: List<Medical> = ArrayList()
}
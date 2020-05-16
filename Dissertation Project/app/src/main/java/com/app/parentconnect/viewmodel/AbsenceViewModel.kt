package com.app.parentconnect.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.app.parentconnect.data.model.entity.Absence
import com.app.parentconnect.data.model.process.AbsenceRequestCall
import com.app.parentconnect.data.repository.AbsenceRepository

class AbsenceViewModel(application: Application) :
    AndroidViewModel(application) {
    private val absenceRepository: AbsenceRepository = AbsenceRepository()
    fun saveLocally(absence: Absence): MutableLiveData<AbsenceRequestCall> {
        return absenceRepository.saveTextLocally(absence)
    }

    fun upload(absence: Absence, imageUri: Uri): MutableLiveData<AbsenceRequestCall> {
        return absenceRepository.uploadImageText(absence, imageUri)
    }

    fun updateImageText(absence: Absence, imageUri: Uri): MutableLiveData<AbsenceRequestCall> {
        return absenceRepository.updateImageText(absence, imageUri)
    }

    fun updateOnlyText(absence: Absence): MutableLiveData<AbsenceRequestCall> {
        return absenceRepository.saveTextLocally(absence)
    }

    fun delete(absence: Absence): MutableLiveData<AbsenceRequestCall> {
        return absenceRepository.deleteImageText(absence)
    }

    val allAbsence: MutableLiveData<AbsenceRequestCall>
        get() = absenceRepository.select()

    fun search(searchTerm: String): MutableLiveData<AbsenceRequestCall> {
        return absenceRepository.search(searchTerm)
    }
}
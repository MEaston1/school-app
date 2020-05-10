package com.example.schoolapp.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.schoolapp.data.model.entity.Medical
import com.example.schoolapp.data.model.process.MedicalRequestCall
import com.example.schoolapp.data.repository.MedicalRepository

class MedicalViewModel(application: Application) :
    AndroidViewModel(application) {
    private val medicalRepository: MedicalRepository = MedicalRepository()
    fun saveLocally(medical: Medical): MutableLiveData<MedicalRequestCall> {
        return medicalRepository.saveTextLocally(medical)
    }

    fun upload(medical: Medical, imageUri: Uri): MutableLiveData<MedicalRequestCall> {
        return medicalRepository.uploadImageText(medical, imageUri)
    }

    fun updateImageText(medical: Medical, imageUri: Uri): MutableLiveData<MedicalRequestCall> {
        return medicalRepository.updateImageText(medical, imageUri)
    }

    fun updateOnlyText(medical: Medical): MutableLiveData<MedicalRequestCall> {
        return medicalRepository.saveTextLocally(medical)
    }

    fun delete(medical: Medical): MutableLiveData<MedicalRequestCall> {
        return medicalRepository.deleteImageText(medical)
    }

    val allMedical: MutableLiveData<MedicalRequestCall>
        get() = medicalRepository.select()

    fun search(searchTerm: String): MutableLiveData<MedicalRequestCall> {
        return medicalRepository.search(searchTerm)
    }
}
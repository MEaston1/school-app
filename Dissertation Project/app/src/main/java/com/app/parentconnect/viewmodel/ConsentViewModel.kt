package com.app.parentconnect.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.app.parentconnect.data.model.entity.Consent
import com.app.parentconnect.data.model.process.ConsentRequestCall
import com.app.parentconnect.data.repository.ConsentRepository

class ConsentViewModel(application: Application) :
    AndroidViewModel(application) {
    private val consentRepository: ConsentRepository = ConsentRepository()
    fun saveLocally(consent: Consent): MutableLiveData<ConsentRequestCall> {
        return consentRepository.saveTextLocally(consent)
    }

    fun upload(consent: Consent, imageUri: Uri): MutableLiveData<ConsentRequestCall> {
        return consentRepository.uploadImageText(consent, imageUri)
    }

    fun updateImageText(consent: Consent, imageUri: Uri): MutableLiveData<ConsentRequestCall> {
        return consentRepository.updateImageText(consent, imageUri)
    }

    fun updateOnlyText(consent: Consent): MutableLiveData<ConsentRequestCall> {
        return consentRepository.saveTextLocally(consent)
    }

    fun delete(consent: Consent): MutableLiveData<ConsentRequestCall> {
        return consentRepository.deleteImageText(consent)
    }

    val allConsent: MutableLiveData<ConsentRequestCall>
        get() = consentRepository.select()

    fun search(searchTerm: String): MutableLiveData<ConsentRequestCall> {
        return consentRepository.search(searchTerm)
    }
}
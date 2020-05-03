package com.example.schoolapp.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.schoolapp.data.model.entity.News
import com.example.schoolapp.data.model.process.RequestCall
import com.example.schoolapp.data.repository.NewsRepository

class NewsViewModel(application: Application) :
    AndroidViewModel(application) {
    private val newsRepository: NewsRepository = NewsRepository()
    fun saveLocally(news: News): MutableLiveData<RequestCall> {
        return newsRepository.saveTextLocally(news)
    }
    fun upload(news: News, imageUri: Uri): MutableLiveData<RequestCall> {
        return newsRepository.uploadImageText(news, imageUri)
    }

    fun updateImageText(news: News, imageUri: Uri): MutableLiveData<RequestCall> {
        return newsRepository.updateImageText(news, imageUri)
    }

    fun updateOnlyText(news: News): MutableLiveData<RequestCall> {
        return newsRepository.saveTextLocally(news)
    }

    fun delete(news: News): MutableLiveData<RequestCall> {
        return newsRepository.deleteImageText(news)
    }

    val allNews: MutableLiveData<RequestCall>
        get() = newsRepository.select()

    fun search(searchTerm: String): MutableLiveData<RequestCall> {
        return newsRepository.search(searchTerm)
    }
    fun login(email: String,password: String): MutableLiveData<RequestCall> {
        return newsRepository.login(email,password)
    }
}
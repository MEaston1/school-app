package com.example.schoolapp.data.model.process

import com.example.schoolapp.data.model.entity.News

/**
 * This class will represent a single request or Firebase operation
 */
class RequestCall {
    //A single request will have the following attributes
    var status = 0
    var message: String = "NO MESSAGE"
    var news: List<News> = ArrayList()

}
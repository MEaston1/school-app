package com.app.parentconnect.data.model.process


import com.app.parentconnect.data.model.entity.News

// This class contains a request for firebase operations

class RequestCall {
    //A single request will have the following attributes
    var status = 0
    var message: String = "NO MESSAGE"
    var news: List<News> = ArrayList()

}

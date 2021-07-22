package com.example.schoolapp.view.ui.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.example.schoolapp.R
import com.example.schoolapp.common.Constants.LOCAL_IMAGES
import com.example.schoolapp.common.PermissionManager
import com.example.schoolapp.common.Utils
import com.example.schoolapp.common.Utils.loadImageFromNetwork
import com.example.schoolapp.common.Utils.receive
import com.example.schoolapp.common.Utils.sendAnnouncementToActivity
import com.example.schoolapp.data.model.entity.News
import com.example.schoolapp.view.ui.base.BaseActivity
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.activity_detail.titleTV
import java.util.*

class DetailActivity : BaseActivity() {
    //Let's define our instance fields
    private var receivedNews: News? = null

    /**
     * We will now receive and show our data to their appropriate views.
     */
    private fun receiveAndShowData() {
        receivedNews = receive(intent, this@DetailActivity)
        if (receivedNews != null) {
            titleTV.text = receivedNews!!.title
            contentTV.text = receivedNews!!.content
            countryTV.text = receivedNews!!.country
            tagsTV.text = receivedNews!!.tags
            datePublishedTV.text = receivedNews!!.datePublished
            dateUpdated.text = receivedNews!!.dateUpdated
            authorTV.text = receivedNews!!.publisher
            mCollapsingToolbarLayout.setExpandedTitleColor(resources.getColor(R.color.white))
            loadImageFromNetwork(receivedNews!!.imageURL!!,
                LOCAL_IMAGES[Random().nextInt(LOCAL_IMAGES.size)], dImageView
            )

            if(receivedNews!!.views == ""){
                receivedNews!!.views= (1).toString()
            }else{
                receivedNews!!.views= (receivedNews!!.views!!.toInt() +1).toString()
            }
            newsViewModel().saveLocally(receivedNews!!)
        }

        editFAB.setOnClickListener {
            if (PermissionManager.isLoggedIn){
                sendAnnouncementToActivity(this, receivedNews, UploadActivity::class.java)
                finish()
            }else{
                Utils.promptLogin(this, "INSUFFICIENT PRIVILEGES", "You need to Login First")
            }
        }
    }

    /**
     * Let's inflate our menu for the detail page
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.detail_page_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_edit -> {
                if (PermissionManager.isLoggedIn){
                    sendAnnouncementToActivity(this, receivedNews, UploadActivity::class.java)
                    finish()
                }else{
                    Utils.promptLogin(this, "INSUFFICIENT PRIVILEGES", "You need to Login First")
                }
                return true
            }

        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView( R.layout.activity_detail)
        receiveAndShowData()
    }
}
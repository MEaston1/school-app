package com.app.parentconnect.view.ui.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.app.parentconnect.R
import com.app.parentconnect.common.Constants.LOCAL_IMAGES
import com.app.parentconnect.common.PermissionManager
import com.app.parentconnect.common.Utils
import com.app.parentconnect.common.Utils.loadImageFromNetwork
import com.app.parentconnect.common.Utils.receive
import com.app.parentconnect.common.Utils.sendAnnouncementToActivity
import com.app.parentconnect.data.model.entity.News
import com.app.parentconnect.view.ui.base.BaseActivity
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
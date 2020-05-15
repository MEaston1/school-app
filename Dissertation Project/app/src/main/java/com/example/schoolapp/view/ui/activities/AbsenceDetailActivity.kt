package com.example.schoolapp.view.ui.activities


import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.example.schoolapp.R
import com.example.schoolapp.common.Constants.LOCAL_IMAGES
import com.example.schoolapp.common.PermissionManager
import com.example.schoolapp.common.Utils
import com.example.schoolapp.common.Utils.loadImageFromNetwork
import com.example.schoolapp.common.Utils.receiveAbsence
import com.example.schoolapp.common.Utils.sendAbsenceAnnouncementToActivity
import com.example.schoolapp.data.model.entity.Absence
import com.example.schoolapp.view.ui.base.BaseActivity
import kotlinx.android.synthetic.main.activity_absence_detail.*
import java.util.*

class AbsenceDetailActivity : BaseActivity() {

    private var receivedAbsence: Absence? = null


    private fun receiveAndShowAbsenceData() {
        receivedAbsence = receiveAbsence(intent, this@AbsenceDetailActivity)
        if (receivedAbsence != null) {
            titleAbsenceTV.text = receivedAbsence!!.childName
            contentAbsenceTV.text = receivedAbsence!!.absenceReason
            absenceDurationTV.text = receivedAbsence!!.durationExpected
            datePublishedAbsenceTV.text = receivedAbsence!!.datePublished
            dateAbsenceUpdated.text = receivedAbsence!!.dateUpdated
            authorAbsenceTV.text = receivedAbsence!!.publisher
            mCollapsingToolbarLayout.setExpandedTitleColor(resources.getColor(R.color.white))
            loadImageFromNetwork(receivedAbsence!!.absenceImageURL!!,
                LOCAL_IMAGES[Random().nextInt(LOCAL_IMAGES.size)], dAbsenceImageView
            )

            if(receivedAbsence!!.views == ""){
                receivedAbsence!!.views= (1).toString()
            }else{
                receivedAbsence!!.views= (receivedAbsence!!.views!!.toInt() +1).toString()
            }
            absenceViewModel().saveLocally(receivedAbsence!!)
        }

        editAbsenceFAB.setOnClickListener {
            if (PermissionManager.isLoggedIn){
                sendAbsenceAnnouncementToActivity(this, receivedAbsence, AbsenceUploadActivity::class.java)
                finish()
            }else{
                Utils.promptLogin(this, "INSUFFICIENT PRIVILEGES", "You need to Login First")
            }

        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.detail_page_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_edit -> {
                if (PermissionManager.isLoggedIn){
                    sendAbsenceAnnouncementToActivity(this, receivedAbsence, UploadActivity::class.java)
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
        setContentView(R.layout.activity_absence_detail)
        receiveAndShowAbsenceData()
    }
}

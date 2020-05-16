package com.app.parentconnect.view.ui.activities

import android.os.Bundle
import android.view.MenuItem
import com.app.parentconnect.R
import com.app.parentconnect.common.PermissionManager
import com.app.parentconnect.common.Utils
import com.app.parentconnect.common.Utils.receiveConsent
import com.app.parentconnect.common.Utils.sendConsentAnnouncementToActivity
import com.app.parentconnect.data.model.entity.Consent
import com.app.parentconnect.view.ui.base.BaseActivity
import kotlinx.android.synthetic.main.activity_consent_detail.*


class ConsentDetailActivity : BaseActivity() {

    private var receivedConsent: Consent? = null

    private fun receiveAndShowConsentData() {
        receivedConsent = receiveConsent(intent, this@ConsentDetailActivity)
        if (receivedConsent != null) {
            titleConsentTV.text = receivedConsent!!.consentChildName
            eventNameTV.text = receivedConsent!!.eventName
            cParentPhoneNumberTV.text = receivedConsent!!.consentParentPhoneNumber
            consentVerificationTV.text = receivedConsent!!.consentVerification
            datePublishedConsentTV.text = receivedConsent!!.datePublished
            authorConsentTV.text = receivedConsent!!.publisher

            if(receivedConsent!!.views == ""){
                receivedConsent!!.views= (1).toString()
            }else{
                receivedConsent!!.views= (receivedConsent!!.views!!.toInt() +1).toString()
            }
            consentViewModel().saveLocally(receivedConsent!!)
        }

       /* editConsentFAB.setOnClickListener {
            if (PermissionManager.isLoggedIn){
                sendConsentAnnouncementToActivity(this, receivedConsent, UploadConsentActivity::class.java)
                finish()
            }else{
                Utils.promptLogin(this, "INSUFFICIENT PRIVILEGES", "You need to Login First")
            }

        } */
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_edit -> {
                if (PermissionManager.isLoggedIn){
                    sendConsentAnnouncementToActivity(this, receivedConsent, ConsentUploadActivity::class.java)
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
        setContentView(R.layout.activity_consent_detail)
        receiveAndShowConsentData()
    }

}

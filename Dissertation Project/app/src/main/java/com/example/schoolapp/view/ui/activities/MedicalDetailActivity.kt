package com.example.schoolapp.view.ui.activities

import android.os.Bundle
import android.view.MenuItem
import com.example.schoolapp.R
import com.example.schoolapp.common.Constants.LOCAL_IMAGES
import com.example.schoolapp.common.PermissionManager
import com.example.schoolapp.common.Utils
import com.example.schoolapp.common.Utils.loadImageFromNetwork
import com.example.schoolapp.common.Utils.receiveMedical
import com.example.schoolapp.common.Utils.sendMedicalAnnouncementToActivity
import com.example.schoolapp.data.model.entity.Medical
import com.example.schoolapp.view.ui.base.BaseActivity
import kotlinx.android.synthetic.main.activity_medical_detail.*
import java.util.*

class MedicalDetailActivity : BaseActivity(){

    private var receivedMedical: Medical? = null

    private fun receiveAndShowMedicalData() {
        receivedMedical = receiveMedical(intent, this@MedicalDetailActivity)
        if (receivedMedical != null) {
            titleMedicalTV.text = receivedMedical!!.medicalChildName
            allergiesTV.text = receivedMedical!!.allergies
            medicalDescriptionTV.text = receivedMedical!!.medicalDescription
            mParentPhoneNumberTV.text = receivedMedical!!.mParentPhoneNumber
            requiredMedicineTV.text = receivedMedical!!.medicineRequired
            datePublishedMedicalTV.text = receivedMedical!!.dateMedicalPublished
            dateMedicalUpdatedTV.text = receivedMedical!!.dateMedicalUpdated
            authorMedicalTV.text = receivedMedical!!.publisher
            mCollapsingToolbarLayout.setExpandedTitleColor(resources.getColor(R.color.white))
            loadImageFromNetwork(receivedMedical!!.medicalImageURL!!,
                LOCAL_IMAGES[Random().nextInt(LOCAL_IMAGES.size)], dMedicalImageView
            )


            if(receivedMedical!!.views == ""){
                receivedMedical!!.views= (1).toString()
            }else{
                receivedMedical!!.views= (receivedMedical!!.views!!.toInt() +1).toString()
            }
            medicalViewModel().saveLocally(receivedMedical!!)
        }

        /* editMedicalFAB.setOnClickListener {
             if (PermissionManager.isLoggedIn){
                 sendMedicalAnnouncementToActivity(this, receivedMedical, UploadMedicalActivity::class.java)
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
                    sendMedicalAnnouncementToActivity(this, receivedMedical, MedicalUploadActivity::class.java)
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
        setContentView(R.layout.activity_medical_detail)
    }

}
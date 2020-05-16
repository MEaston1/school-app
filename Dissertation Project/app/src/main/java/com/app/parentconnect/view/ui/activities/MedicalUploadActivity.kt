package com.app.parentconnect.view.ui.activities

import `in`.mayanknagwanshi.imagepicker.ImageSelectActivity
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Observer
import com.app.parentconnect.R
import com.app.parentconnect.common.CacheManager
import com.app.parentconnect.common.Constants
import com.app.parentconnect.common.Utils
import com.app.parentconnect.common.Utils.loadImageFromNetwork
import com.app.parentconnect.common.Utils.receiveMedical
import com.app.parentconnect.common.Utils.showInfoDialog
import com.app.parentconnect.data.model.entity.Medical
import com.app.parentconnect.view.ui.base.BaseEditingActivity
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_medical_upload.*
import java.io.File

class MedicalUploadActivity : BaseEditingActivity() {

    private val c: Context = this
    private var receivedMedical: Medical? = null
    private var resumedAfterImagePicker = false
    private var chosenFile: File? = null

    private fun captureImage() {
        val i = Intent(this, ImageSelectActivity::class.java)
        i.putExtra(ImageSelectActivity.FLAG_COMPRESS, false) //default is true
        i.putExtra(ImageSelectActivity.FLAG_CAMERA, true) //default is true
        i.putExtra(ImageSelectActivity.FLAG_GALLERY, true) //default is true
        startActivityForResult(i, 1213)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            if (requestCode == 1213) {
                val filePath =
                    data.getStringExtra(ImageSelectActivity.RESULT_FILE_PATH)
                chosenFile = File(filePath)
                Picasso.get().load(chosenFile!!).error(R.drawable.image_not_found).into(topMedicalImageImg)
            }
        }
        resumedAfterImagePicker = true
    }

    private fun handleEvents() {
        medicalDatePublished.setOnClickListener { v: View? ->
            selectDate(medicalDatePublished)
        }
        medicalDateUpdated.setOnClickListener { v: View? ->
            selectDate(medicalDateUpdated)
        }
        medicalPickedImg.setOnClickListener { v: View? -> checkPermissionsThenPickImage() }

    }
    private fun checkPermissionsThenPickImage() {
        Dexter.withActivity(this)
            .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse) {
                    show("Good...READ EXTERNAL PERMISSION GRANTED")
                    captureImage()
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse) {
                    show("WHOOPS! PERMISSION DENIED: Please grant permission first")
                    if (response.isPermanentlyDenied) {
                        showSettingDialog()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest,
                    token: PermissionToken
                ) {
                    Log.i("DEXTER PERMISSION", "Permission Rationale Should be shown")
                    token.continuePermissionRequest()
                }
            }).check()
    }
    private fun uploadOnlyText(medical: Medical) {
        medicalViewModel().saveLocally(medical)
            .observe(this, Observer { r ->
                if (makeMedicalRequest(r, "FORM PUBLISHING") == Constants.SUCCEEDED) {
                    show("Medical Form Published Successfully")
                    clearEditTexts(medicalChildNameTxt!!, medicalDescriptionTxt!!, mParentPhoneNumberTxt!!, allergiesTxt!!, requiredMedicineTxt!!, medicalDatePublished!!)
                }
            })
    }
    private fun upload(medical: Medical) {
        medicalViewModel().upload(medical, Uri.fromFile(chosenFile))
            .observe(this, Observer { r ->
                if (makeMedicalRequest(r, "FORM PUBLISHING") == Constants.SUCCEEDED) {
                    show("Medical Form Publishing Successfully")
                    clearEditTexts(medicalChildNameTxt!!, medicalDescriptionTxt!!, mParentPhoneNumberTxt!!, allergiesTxt!!, requiredMedicineTxt!!, medicalDatePublished!!)
                }
            })
    }
    private fun update(medical: Medical) {
        medicalViewModel().updateImageText(medical, Uri.fromFile(chosenFile))
            .observe(this, Observer { r ->
                if (makeMedicalRequest(r, "FORM UPDATE") == Constants.SUCCEEDED) {
                    show("Form Updated Successfully")
                    Utils.openActivity(c, MedicalListingActivity::class.java)
                    finish()
                }
            })
    }

    private fun updateOnlyText(medical: Medical) {
        medicalViewModel().updateOnlyText(medical)
            .observe(this, Observer { r ->
                if (makeMedicalRequest(r, "MEDICAL FORM TEXT UPDATE") == Constants.SUCCEEDED) {
                    show("Form Updated Successfully")
                    Utils.openActivity(c, MedicalListingActivity::class.java)
                    finish()
                }
            })
    }

    private fun delete(medical: Medical) {
        medicalViewModel().delete(medical)
            .observe(this, Observer { r ->
                if (makeMedicalRequest(r, "FORM DELETE") == Constants.SUCCEEDED) {
                    show("Medical Form Deleted Successfully")
                    Utils.openActivity(
                        this@MedicalUploadActivity,
                        MedicalListingActivity::class.java
                    )
                    finish()
                }
            })
    }
    private fun validateThenUpload() {
        if (validate(medicalChildNameTxt, allergiesTxt, medicalDescriptionTxt, requiredMedicineTxt, mParentPhoneNumberTxt)) {
            val n = Medical()
            n.medicalChildName = valOf(medicalChildNameTxt)
            n.allergies = valOf(allergiesTxt)
            n.medicalDescription = valOf(medicalDescriptionTxt)
            n.medicineRequired = valOf(requiredMedicineTxt)
            n.mParentPhoneNumber = valOf(mParentPhoneNumberTxt)
            n.dateMedicalPublished = valOf(medicalDatePublished)
            n.dateMedicalUpdated = valOf(medicalDateUpdated)
            n.medicalImageURL = ""
            n.views="0"
            n.publisher= CacheManager.CURRENT_USER

            if (chosenFile == null) {
                uploadOnlyText(n)
            }else{
                upload(n)
            }
        } else {
            show("Please fill up all Fields First")
        }
    }
    private fun validateThenUpdate() {
        if (validate(medicalChildNameTxt, allergiesTxt, medicalDescriptionTxt, requiredMedicineTxt, mParentPhoneNumberTxt)) {
            val n = receivedMedical
            n!!.medicalChildName = valOf(medicalChildNameTxt)
            n.allergies = valOf(allergiesTxt)
            n.medicalDescription = valOf(medicalDescriptionTxt)
            n.medicineRequired = valOf(requiredMedicineTxt)
            n.mParentPhoneNumber = valOf(mParentPhoneNumberTxt)
            n.dateMedicalPublished = valOf(medicalDatePublished)
            n.dateMedicalUpdated = valOf(medicalDateUpdated)
            if (chosenFile == null) {
                updateOnlyText(n)
            } else {
                update(n)
            }
        }
    }

    override fun onBackPressed() {
        showInfoDialog(this, "Warning", "Are you sure you want to exit?"
        )
    }

    //Can't use the same menu as another activity without crashing

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (receivedMedical == null) {
            menuInflater.inflate(R.menu.new_item_menu, menu)
            headerUploadMedicalTxt!!.text = "Add Medical Form"
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.insertMenuItem -> {
                validateThenUpload()
                return true
            }
            R.id.editMenuItem -> {
                //only validate if received news is not null
                if(receivedMedical != null) validateThenUpdate()
                return true
            }
            R.id.deleteMenuItem -> {
                //delete only if received news is not null
                receivedMedical?.let { delete(it) }
                return true
            }
            R.id.viewAllMenuItem -> {
                Utils.openActivity(this, MedicalListingActivity::class.java)
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onResume() {
        super.onResume()
        val o = receiveMedical(intent, c)
        when {
            o != null -> {
                receivedMedical = o
                if (!resumedAfterImagePicker) {
                    medicalChildNameTxt.setText(receivedMedical!!.medicalChildName)
                    mParentPhoneNumberTxt.setText(receivedMedical!!.mParentPhoneNumber)
                    requiredMedicineTxt.setText(receivedMedical!!.medicineRequired)
                    medicalDatePublished.setText(receivedMedical!!.dateMedicalUpdated)
                    medicalDateUpdated.setText(receivedMedical!!.dateMedicalUpdated)
                }
                when {
                    chosenFile != null -> {
                        Picasso.get().load(chosenFile!!).error(R.drawable.image_not_found).into(topMedicalImageImg)
                    }
                    else -> {
                        loadImageFromNetwork(receivedMedical!!.medicalImageURL!!, R.drawable.image_not_found, topMedicalImageImg)
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medical_upload)
    }
}

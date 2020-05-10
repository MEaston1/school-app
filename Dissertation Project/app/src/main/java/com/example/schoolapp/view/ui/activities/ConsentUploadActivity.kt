package com.example.schoolapp.view.ui.activities

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
import androidx.lifecycle.Observer
import com.example.schoolapp.R
import com.example.schoolapp.common.CacheManager
import com.example.schoolapp.common.Constants.SUCCEEDED
import com.example.schoolapp.common.Utils
import com.example.schoolapp.common.Utils.loadImageFromNetwork
import com.example.schoolapp.common.Utils.openActivity
import com.example.schoolapp.common.Utils.receiveConsent
import com.example.schoolapp.data.model.entity.Consent
import com.example.schoolapp.view.ui.base.BaseEditingActivity
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_consent_upload.*
import java.io.File

class ConsentUploadActivity : BaseEditingActivity() {
    private val c: Context = this
    private var receivedConsent: Consent? = null
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
                Picasso.get().load(chosenFile!!).error(R.drawable.image_not_found).into(topConsentImageImg)
            }
        }
        resumedAfterImagePicker = true
    }
    private fun handleEvents() {
        consentDatePublished.setOnClickListener { v: android.view.View? ->
            selectDate(consentDatePublished)
        }
        consentPickedImg.setOnClickListener { v: android.view.View? -> checkPermissionsThenPickImage() }

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
    private fun uploadOnlyText(consent: Consent) {
        consentViewModel().saveLocally(consent)
            .observe(this, Observer { r ->
                if (makeConsentRequest(r, "FORM PUBLISHING") == SUCCEEDED) {
                    show("Consent Form Published Successfully")
                    clearEditTexts(consentChildNameTxt!!, eventNameTxt!!, cParentPhoneNumberTxt!!, consentConfirmationTxt!! , consentDatePublished!!)
                }
            })
    }
    private fun upload(consent: Consent) {
        consentViewModel().upload(consent, Uri.fromFile(chosenFile))
            .observe(this, Observer { r ->
                if (makeConsentRequest(r, "FORM PUBLISHING") == SUCCEEDED) {
                    show("Consent Form Publishing Successfully")
                    clearEditTexts(consentChildNameTxt!!, eventNameTxt!!, cParentPhoneNumberTxt!!, consentConfirmationTxt!! , consentDatePublished!!)
                }
            })
    }
    private fun update(consent: Consent) {
        consentViewModel().updateImageText(consent, Uri.fromFile(chosenFile))
            .observe(this, Observer { r ->
                if (makeConsentRequest(r, "FORM UPDATE") == SUCCEEDED) {
                    show("Form Updated Successfully")
                    openActivity(c, ConsentListingActivity::class.java)
                    finish()
                }
            })
    }
    private fun updateOnlyText(consent: Consent) {
        consentViewModel().updateOnlyText(consent)
            .observe(this, Observer { r ->
                if (makeConsentRequest(r, "CONSENT FORM TEXT UPDATE") == SUCCEEDED) {
                    show("Form Updated Successfully")
                    openActivity(c, ConsentListingActivity::class.java)
                    finish()
                }
            })
    }
    private fun delete(consent: Consent) {
        consentViewModel().delete(consent)
            .observe(this, Observer { r ->
                if (makeConsentRequest(r, "FORM DELETE") == SUCCEEDED) {
                    show("Consent Form Deleted Successfully")
                    openActivity(
                        this@ConsentUploadActivity,
                        ConsentListingActivity::class.java
                    )
                    finish()
                }
            })
    }

    private fun validateThenUpload() {
        if (validate(consentChildNameTxt, eventNameTxt, cParentPhoneNumberTxt, cParentPhoneNumberTxt, consentConfirmationTxt)) {
            val n = Consent()
            n.consentChildName = valOf(consentChildNameTxt)
            n.eventName = valOf(eventNameTxt)
            n.consentParentPhoneNumber = valOf(cParentPhoneNumberTxt)
            n.consentVerification = valOf(consentConfirmationTxt)
            n.datePublished = valOf(consentDatePublished)
            n.dateConsentUpdated = valOf(consentDateUpdated)
            n.consentImageURL = ""
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
        if (validate(consentChildNameTxt, eventNameTxt, cParentPhoneNumberTxt, cParentPhoneNumberTxt, consentConfirmationTxt)) {
            val n = receivedConsent
            n!!.consentChildName = valOf(consentChildNameTxt)
            n.eventName = valOf(eventNameTxt)
            n.consentParentPhoneNumber = valOf(cParentPhoneNumberTxt)
            n.consentVerification = valOf(consentConfirmationTxt)
            n.datePublished = valOf(consentDatePublished)
            n.dateConsentUpdated = valOf(consentDateUpdated)
            if (chosenFile == null) {
                updateOnlyText(n)
            } else {
                update(n)
            }
        }
    }
    override fun onBackPressed() {
        Utils.showInfoDialog(
            this, "Warning", "Are you sure you want to exit?"
        )
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (receivedConsent == null) {
            menuInflater.inflate(R.menu.new_item_menu, menu)
            headerConsentTxt!!.text = "Add Consent Form"
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
                //only validate if received form is not null
                if(receivedConsent != null) validateThenUpdate()
                return true
            }
            R.id.deleteMenuItem -> {
                //delete only if received form is not null
                receivedConsent?.let { delete(it) }
                return true
            }
            R.id.viewAllMenuItem -> {
                openActivity(this, ConsentListingActivity::class.java)
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onResume() {
        super.onResume()
        val o = receiveConsent(intent, c)
        when {
            o != null -> {
                receivedConsent = o
                if (!resumedAfterImagePicker) {
                    consentChildNameTxt.setText(receivedConsent!!.consentChildName)
                    eventNameTxt.setText(receivedConsent!!.eventName)
                    cParentPhoneNumberTxt.setText(receivedConsent!!.consentParentPhoneNumber)
                    consentConfirmationTxt.setText(receivedConsent!!.consentVerification)
                    consentDatePublished.setText(receivedConsent!!.datePublished)
                    consentDateUpdated.setText(receivedConsent!!.dateConsentUpdated)
                }
                when {
                    chosenFile != null -> {
                        Picasso.get().load(chosenFile!!).error(R.drawable.image_not_found).into(topConsentImageImg)
                    }
                    else -> {
                        loadImageFromNetwork(receivedConsent!!.consentImageURL!!, R.drawable.image_not_found, topConsentImageImg)
                    }
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_consent_upload)
        handleEvents()
    }

}

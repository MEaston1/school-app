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
import com.app.parentconnect.R
import com.app.parentconnect.common.Constants.SUCCEEDED
import com.app.parentconnect.common.Utils.openActivity
import com.app.parentconnect.data.model.entity.Absence
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.squareup.picasso.Picasso
import androidx.lifecycle.Observer
import com.app.parentconnect.common.CacheManager
import com.app.parentconnect.common.Utils
import com.app.parentconnect.common.Utils.loadImageFromNetwork
import com.app.parentconnect.common.Utils.receiveAbsence
import com.app.parentconnect.view.ui.base.BaseEditingActivity
import kotlinx.android.synthetic.main.activity_absence_upload.*
import kotlinx.android.synthetic.main.activity_absence_upload.absenceDatePublished
import kotlinx.android.synthetic.main.activity_absence_upload.absenceDateUpdated
import kotlinx.android.synthetic.main.activity_absence_upload.pickedAbsenceImg
import kotlinx.android.synthetic.main.activity_absence_upload.topAbsenceImageImg
import java.io.File

class AbsenceUploadActivity : BaseEditingActivity() {
    private val c: Context = this
    private var receivedAbsence: Absence? = null
    //image
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
                Picasso.get().load(chosenFile!!).error(R.drawable.image_not_found).into(topAbsenceImageImg)
            }
        }
        resumedAfterImagePicker = true
    }

    private fun handleEvents() {
        absenceDatePublished.setOnClickListener { v: View? ->
            selectDate(absenceDatePublished)
        }
        absenceDateUpdated.setOnClickListener { v: View? ->
            selectDate(absenceDateUpdated)
        }
        pickedAbsenceImg.setOnClickListener { v: View? -> checkPermissionsThenPickImage() }

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

    private fun uploadOnlyText(absence: Absence) {
        absenceViewModel().saveLocally(absence)
            .observe(this, Observer { r ->
                if (makeAbsenceRequest(r, "FORM PUBLISHING") == SUCCEEDED) {
                    show("Absence Form Published Successfully")
                    clearEditTexts(childNameTxt!!, absenceReasonTxt!!, absenceDurationTxt!!, absenceDatePublished!!)
                }
            })
    }
    private fun upload(absence: Absence) {
        absenceViewModel().upload(absence, Uri.fromFile(chosenFile))
            .observe(this, Observer { r ->
                if (makeAbsenceRequest(r, "FORM PUBLISHING") == SUCCEEDED) {
                    show("Absence Form Publishing Successfully")
                    clearEditTexts(childNameTxt!!, absenceReasonTxt!!, absenceDurationTxt!!, absenceDatePublished!!)
                }
            })
    }

    private fun update(absence: Absence) {
        absenceViewModel().updateImageText(absence, Uri.fromFile(chosenFile))
            .observe(this, Observer { r ->
                if (makeAbsenceRequest(r, "FORM UPDATE") == SUCCEEDED) {
                    show("Form Updated Successfully")
                    openActivity(c, AbsenceListingActivity::class.java)
                    finish()
                }
            })
    }

    private fun updateOnlyText(absence: Absence) {
        absenceViewModel().updateOnlyText(absence)
            .observe(this, Observer { r ->
                if (makeAbsenceRequest(r, "ABSENCE FORM TEXT UPDATE") == SUCCEEDED) {
                    show("Form Updated Successfully")
                    openActivity(c, AbsenceListingActivity::class.java)
                    finish()
                }
            })
    }

    private fun delete(absence: Absence) {
        absenceViewModel().delete(absence)
            .observe(this, Observer { r ->
                if (makeAbsenceRequest(r, "FORM DELETE") == SUCCEEDED) {
                    show("Absence Form Deleted Successfully")
                    openActivity(
                        this@AbsenceUploadActivity,
                        AbsenceListingActivity::class.java
                    )
                    finish()
                }
            })
    }

    private fun validateThenUpload() {
        if (validate(childNameTxt, absenceDurationTxt, absenceReasonTxt)) {
            val n = Absence()
            n.childName = valOf(childNameTxt)
            n.absenceReason = valOf(absenceReasonTxt)
            n.durationExpected = valOf(absenceDurationTxt)
            n.datePublished = valOf(absenceDatePublished)
            n.dateUpdated = valOf(absenceDateUpdated)
            n.absenceImageURL = ""
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
        if (validate(childNameTxt, absenceReasonTxt, absenceDurationTxt)) {
            val n = receivedAbsence
            n!!.childName = valOf(childNameTxt)
            n.absenceReason = valOf(absenceReasonTxt)
            n.durationExpected = valOf(absenceDurationTxt)
            n.datePublished = valOf(absenceDatePublished)
            n.dateUpdated = valOf(absenceDateUpdated)
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
        if (receivedAbsence == null) {
            menuInflater.inflate(R.menu.new_item_menu, menu)
            headerTxt!!.text = "Add Absence"
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
                if(receivedAbsence != null) validateThenUpdate()
                return true
            }
            R.id.deleteMenuItem -> {
                //delete only if received news is not null
                receivedAbsence?.let { delete(it) }
                return true
            }
            R.id.viewAllMenuItem -> {
                openActivity(this, AbsenceListingActivity::class.java)
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        val o = receiveAbsence(intent, c)
        when {
            o != null -> {
                receivedAbsence = o
                if (!resumedAfterImagePicker) {
                    childNameTxt.setText(receivedAbsence!!.childName)
                    absenceReasonTxt.setText(receivedAbsence!!.absenceReason)
                    absenceDurationTxt.setText(receivedAbsence!!.durationExpected)
                    absenceDatePublished.setText(receivedAbsence!!.datePublished)
                    absenceDateUpdated.setText(receivedAbsence!!.dateUpdated)
                }
                when {
                    chosenFile != null -> {
                        Picasso.get().load(chosenFile!!).error(R.drawable.image_not_found).into(topAbsenceImageImg)
                    }
                    else -> {
                        loadImageFromNetwork(receivedAbsence!!.absenceImageURL!!, R.drawable.image_not_found, topAbsenceImageImg)
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_absence_upload)
        handleEvents()
    }
}

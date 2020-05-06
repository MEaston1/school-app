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
import android.view.View
import androidx.lifecycle.Observer
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.squareup.picasso.Picasso
import com.example.schoolapp.R
import com.example.schoolapp.common.CacheManager
import com.example.schoolapp.common.Constants.SUCCEEDED
import com.example.schoolapp.common.Utils.loadImageFromNetwork
import com.example.schoolapp.common.Utils.openActivity
import com.example.schoolapp.common.Utils.receive
import com.example.schoolapp.common.Utils.showInfoDialog
import com.example.schoolapp.data.model.entity.News
import com.example.schoolapp.view.ui.base.BaseEditingActivity
import kotlinx.android.synthetic.main.activity_upload.*
import java.io.File

class UploadActivity : BaseEditingActivity() {
    //we'll have several instance fields
    private val c: Context = this
    private var receivedNews: News? = null
    //image
    private var resumedAfterImagePicker = false
    private var chosenFile: File? = null

    // Capture or select image
    private fun captureImage() {
        val i = Intent(this, ImageSelectActivity::class.java)
        i.putExtra(ImageSelectActivity.FLAG_COMPRESS, false) //default is true
        i.putExtra(ImageSelectActivity.FLAG_CAMERA, true) //default is true
        i.putExtra(ImageSelectActivity.FLAG_GALLERY, true) //default is true
        startActivityForResult(i, 1213)
    }

    /*
      After capturing or selecting image, we will get the image path
      and use it to instantiate a file object
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            if (requestCode == 1213) {
                val filePath =
                    data.getStringExtra(ImageSelectActivity.RESULT_FILE_PATH)
                chosenFile = File(filePath)
                Picasso.get().load(chosenFile!!).error(R.drawable.image_not_found).into(topImageImg)
            }
        }
        resumedAfterImagePicker = true
    }
    // Lets handles events and Shows single choice dialogs

    private fun handleEvents() {
        countryTxt.setOnClickListener {
            selectCountry(countryTxt)
        }
        datePublished.setOnClickListener { v: View? ->
            selectDate(datePublished)
        }
        dateUpdated.setOnClickListener { v: View? ->
            selectDate(dateUpdated)
        }
        pickedImg.setOnClickListener { v: View? -> checkPermissionsThenPickImage() }

    }

    /**
     * We use a library known as Dexter to check for permissions at
     * runtime.If we haven't been granted then we present the user with
     * a dialog to take him to settings page to grant us permission
     */
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

    private fun uploadOnlyText(news: News) {
        newsViewModel().saveLocally(news)
            .observe(this, Observer { r ->
                if (makeRequest(r, "NEWS PUBLISHING") == SUCCEEDED) {
                    show("News Text Published Successfully")
                    clearEditTexts(titleTxt!!, descriptionTxt!!, countryTxt!!, tagsTxt!!, datePublished!!)
                }
            })
    }
    private fun upload(news: News) {
        newsViewModel().upload(news, Uri.fromFile(chosenFile))
            .observe(this, Observer { r ->
                if (makeRequest(r, "NEWS PUBLISHING") == SUCCEEDED) {
                    show("News Publishing Successfully")
                    clearEditTexts(titleTxt!!, descriptionTxt!!, countryTxt!!, tagsTxt!!, datePublished!!)
                }
            })
    }

    private fun update(news: News) {
        newsViewModel().updateImageText(news, Uri.fromFile(chosenFile))
            .observe(this, Observer { r ->
                if (makeRequest(r, "NEWS UPDATE") == SUCCEEDED) {
                    show("News Updated Successfully")
                    openActivity(c, ListingActivity::class.java)
                    finish()
                }
            })
    }

    private fun updateOnlyText(news: News) {
        newsViewModel().updateOnlyText(news)
            .observe(this, Observer { r ->
                if (makeRequest(r, "NEWS TEXT UPDATE") == SUCCEEDED) {
                    show("News Updated Successfully")
                    openActivity(c, ListingActivity::class.java)
                    finish()
                }
            })
}

    private fun delete(news: News) {
        newsViewModel().delete(news)
            .observe(this, Observer { r ->
                if (makeRequest(r, "NEWS DELETE") == SUCCEEDED) {
                    show("News Deleted Successfully")
                    openActivity(
                        this@UploadActivity,
                        ListingActivity::class.java
                    )
                    finish()
                }
            })
    }

    private fun validateThenUpload() {
        if (validate(titleTxt, descriptionTxt, countryTxt)) {
            val n = News()
            n.title = valOf(titleTxt)
            n.content = valOf(descriptionTxt)
            n.country = valOf(countryTxt)
            n.tags = valOf(tagsTxt)
            n.datePublished = valOf(datePublished)
            n.dateUpdated = valOf(dateUpdated)
            n.imageURL = ""
            n.views="0"
            n.publisher=CacheManager.CURRENT_USER

            if (chosenFile == null) {
                uploadOnlyText(n)
            }else{
                upload(n)
            }
        } else {
            show("Please fill up all Fields First")
        }
    }

    /**
     * Validate then Update our News. If image has not been changed
     * then we update only text,otherwise both images and text.
     */
    private fun validateThenUpdate() {
        if (validate(titleTxt, descriptionTxt, countryTxt)) {
            val n = receivedNews
            n!!.title = valOf(titleTxt)
            n.content = valOf(descriptionTxt)
            n.country = valOf(countryTxt)
            n.tags = valOf(tagsTxt)
            n.datePublished = valOf(datePublished)
            n.dateUpdated = valOf(dateUpdated)
            if (chosenFile == null) {
                updateOnlyText(n)
            } else {
                update(n)
            }
        }
    }

    /**
     * When user presses back button, we will warn him
     */
    override fun onBackPressed() {
        showInfoDialog(this,"Warning","Are you sure you want to exit?"
        )
    }

    /**
     * Menus will be inflated based on the intention of opening this
     * activity. If, while we don't pass any news along then we
     * inflate the new_item_menu. If a news is passed along then we
     * inflate the edit_item_menu
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (receivedNews == null) {
            menuInflater.inflate(R.menu.new_item_menu, menu)
            headerTxt!!.text = "Publish News"
        } else {
            menuInflater.inflate(R.menu.edit_item_menu, menu)
            headerTxt!!.text = "Update News"
        }
        return true
    }

    /**
     * When user selects a menu item in toolbar
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.insertMenuItem -> {
                validateThenUpload()
                return true
            }
            R.id.editMenuItem -> {
                //only validate if received news is not null
                if(receivedNews != null) validateThenUpdate()
                return true
            }
            R.id.deleteMenuItem -> {
                //delete only if received news is not null
                receivedNews?.let { delete(it) }
                return true
            }
            R.id.viewAllMenuItem -> {
                openActivity(this, ListingActivity::class.java)
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        val o = receive(intent, c)
        when {
            o != null -> {
                receivedNews = o
                if (!resumedAfterImagePicker) {
                    titleTxt.setText(receivedNews!!.title)
                    descriptionTxt.setText(receivedNews!!.content)
                    countryTxt.setText(receivedNews!!.country)
                    tagsTxt.setText(receivedNews!!.tags)
                    datePublished.setText(receivedNews!!.datePublished)
                    dateUpdated.setText(receivedNews!!.dateUpdated)
                }
                when {
                    chosenFile != null -> {
                        Picasso.get().load(chosenFile!!).error(R.drawable.image_not_found).into(topImageImg)
                    }
                    else -> {
                        loadImageFromNetwork(receivedNews!!.imageURL!!, R.drawable.image_not_found, topImageImg)
                    }
                }
            }
        }
    }

    /**
     * Let's override our onCreate() method
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload)

        handleEvents()
    }
}
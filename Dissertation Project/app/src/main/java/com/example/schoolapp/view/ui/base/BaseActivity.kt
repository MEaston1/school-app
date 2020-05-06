package com.example.schoolapp.view.ui.base


import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.schoolapp.common.Constants.FAILED
import com.example.schoolapp.common.Constants.IN_PROGRESS
import com.example.schoolapp.common.Constants.SUCCEEDED
import com.example.schoolapp.data.model.process.AbsenceRequestCall
import com.example.schoolapp.data.model.process.RequestCall
import com.example.schoolapp.viewmodel.AbsenceViewModel
import com.example.schoolapp.viewmodel.NewsViewModel
import kotlinx.android.synthetic.main._state.*

open class BaseActivity : AppCompatActivity() {
    protected open var a: BaseActivity = this

    protected fun newsViewModel(): NewsViewModel{
        return ViewModelProvider(this).get(NewsViewModel::class.java)
    }
    protected fun absenceViewModel(): AbsenceViewModel{
        return ViewModelProvider(this).get(AbsenceViewModel::class.java)
    }
    protected fun openPage(clazz: Class<*>?) {
        val intent = Intent(this, clazz)
        startActivity(intent)
    }
    protected fun show(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun openSetting() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivityForResult(intent, 101)
    }

    protected fun showSettingDialog() {
        Log.i("PERMISSION", "Showing Permission Setting Dialog")
        val builder =
            AlertDialog.Builder(this)
        builder.setTitle("Assign Permissions")
        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.")
        builder.setPositiveButton("GO TO SETTING") { dialog: DialogInterface, _: Int ->
            dialog.cancel()
            openSetting()
        }
        builder.setNegativeButton("Cancel") { dialog: DialogInterface, which: Int -> dialog.cancel() }
        builder.show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    /**
     * Uncomment below line to enable custom fonts in all activities
     */
//    override fun attachBaseContext(newBase: Context) {
//        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
//    }

    protected fun createStateCard(title: String?, msg: String?, isShowing: Boolean, isLoading: Boolean, STATE: Int) { //state widgets
        val handler = Handler()
        val delayedHiding =
            Runnable { progressCard!!.visibility = View.GONE }
        if (isShowing) {
            progressCard!!.visibility = View.VISIBLE
            if (isLoading) {
                pb!!.visibility = View.VISIBLE
            } else {
                pb!!.visibility = View.GONE
            }
            titleTV!!.text = title
            msgTV!!.text = msg
            when (STATE) {
                FAILED -> {
                    titleTV.setTextColor(resources.getColor(android.R.color.holo_red_dark))
                    msgTV.setTextColor(resources.getColor(android.R.color.holo_red_light))
                    handler.postDelayed(delayedHiding, 10000)
                }
                IN_PROGRESS -> {
                    titleTV.setTextColor(resources.getColor(android.R.color.holo_blue_dark))
                    msgTV.setTextColor(resources.getColor(android.R.color.holo_blue_light))
                }
                SUCCEEDED -> {
                    titleTV.setTextColor(resources.getColor(android.R.color.holo_green_dark))
                    msgTV.setTextColor(resources.getColor(android.R.color.holo_green_light))
                    handler.postDelayed(delayedHiding, 10000)
                }
            }
        } else {
            progressCard!!.visibility = View.GONE
        }
        closeBtn!!.setOnClickListener { v: View? ->
            progressCard.visibility = View.GONE
        }
    }

    protected fun makeRequest(r: RequestCall?, OPERATION: String): Int {
        if (r == null) {
            createStateCard("$OPERATION FAILED", "Null RequestCall Received", isShowing = true, isLoading = false, STATE = FAILED)
        } else {
            when (r.status) {
                IN_PROGRESS -> {
                    createStateCard("$OPERATION IN PROGRESS", r.message, true, true, IN_PROGRESS)
                }
                FAILED -> {
                    createStateCard("ERROR", r.message, true, false, FAILED)
                }
                SUCCEEDED -> {
                    createStateCard("CONGRATS!", r.message, true, false, SUCCEEDED)
                }
            }
            return r.status
        }
        return -999
    }
    protected fun makeAbsenceRequest(r: AbsenceRequestCall?, OPERATION: String): Int {
        if (r == null) {
            createStateCard("$OPERATION FAILED", "Null AbsenceRequestCall Received", isShowing = true, isLoading = false, STATE = FAILED)
        } else {
            when (r.status) {
                IN_PROGRESS -> {
                    createStateCard("$OPERATION IN PROGRESS", r.message, true, true, IN_PROGRESS)
                }
                FAILED -> {
                    createStateCard("ERROR", r.message, true, false, FAILED)
                }
                SUCCEEDED -> {
                    createStateCard("CONGRATS!", r.message, true, false, SUCCEEDED)
                }
            }
            return r.status
        }
        return -999
    }
}
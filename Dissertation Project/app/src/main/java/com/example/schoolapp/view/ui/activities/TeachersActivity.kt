package com.example.schoolapp.view.ui.activities
import android.os.Bundle
import com.example.schoolapp.R
import com.example.schoolapp.view.ui.base.BaseActivity
import com.example.schoolapp.common.Utils.openActivity
import kotlinx.android.synthetic.main.activity_teachers.*

class TeachersActivity : BaseActivity() {
    private fun initializeWidgets() { //We have 4 cards in the dashboard
        viewAbsenceCard!!.setOnClickListener {
            openActivity(this, ListingActivity::class.java)
        }
        addAnnouncementsCard!!.setOnClickListener {
            openActivity(this, UploadActivity::class.java)
        }
        viewMedicalCard!!.setOnClickListener {
            openActivity(this, UploadActivity::class.java)
        }
        viewConsentCard!!.setOnClickListener {
            openActivity(this, UploadActivity::class.java)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teachers)
        initializeWidgets()
    }
}

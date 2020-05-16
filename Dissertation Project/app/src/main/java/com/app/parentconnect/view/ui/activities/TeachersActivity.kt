package com.app.parentconnect.view.ui.activities
import android.os.Bundle
import com.app.parentconnect.R
import com.app.parentconnect.view.ui.base.BaseActivity
import com.app.parentconnect.common.Utils.openActivity
import kotlinx.android.synthetic.main.activity_teachers.*

class TeachersActivity : BaseActivity() {
    private fun initializeWidgets() { //We have 4 cards in the dashboard
        viewAbsenceCard!!.setOnClickListener {
            openActivity(this, AbsenceListingActivity::class.java)
        }
        addAnnouncementsCard!!.setOnClickListener {
            openActivity(this, UploadActivity::class.java)
        }
        viewMedicalCard!!.setOnClickListener {
            openActivity(this, MedicalListingActivity::class.java)
        }
        viewConsentCard!!.setOnClickListener {
            openActivity(this, ConsentListingActivity::class.java)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teachers)
        initializeWidgets()
    }
}

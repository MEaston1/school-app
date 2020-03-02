package com.example.schoolapp.more
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.schoolapp.R
import kotlinx.android.synthetic.main.activity_more.*


class MoreActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_more)
        val features = listOf(
            MoreActivityDetails("Calendar",R.drawable.calendar),
            MoreActivityDetails("Absence Notice",R.drawable.absence),
            MoreActivityDetails("Allergy Form",R.drawable.medicine),
            MoreActivityDetails("Consent Form",R.drawable.consent),
            MoreActivityDetails("Messages",R.drawable.letter),
            MoreActivityDetails("Medical Form",R.drawable.medical_forms),
            MoreActivityDetails("News",R.drawable.newspaper),
            MoreActivityDetails("App Information",R.drawable.question_mark),
            MoreActivityDetails("Log Out",R.drawable.log_out)
        )
        FeaturesList.apply {
            layoutManager = LinearLayoutManager( this@MoreActivity)
            adapter = MoreFeaturesAdapter(features){startActivity(
                Intent(this@MoreActivity, MoreActivityDetails::class.java)
            )
            }
        }
    }
}
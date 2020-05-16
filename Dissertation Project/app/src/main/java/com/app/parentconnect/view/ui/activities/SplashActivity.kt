package com.app.parentconnect.view.ui.activities

import android.os.Bundle
import android.view.animation.AnimationUtils
import com.app.parentconnect.R
import com.app.parentconnect.common.Utils.openActivity
import com.app.parentconnect.registerlogin.LoginActivity
import com.app.parentconnect.view.ui.base.BaseActivity
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : BaseActivity() {

    private fun showSplashAnimation() {     // below drops the graduation hat logo
        val animation =  AnimationUtils.loadAnimation(this, R.anim.drop)
        mLogo.startAnimation(animation) // below loads the fade in animation for titles
        val fadeIn =  AnimationUtils.loadAnimation(this, R.anim.fade_in)
        mainTitle.startAnimation(fadeIn)
        subTitle.startAnimation(fadeIn)
    }

    private fun goToLoginPage() {         // Function delays travel to login activity
        val t: Thread = object : Thread() {
            override fun run() {
                try {
                    sleep(1500)
                    openActivity(
                        this@SplashActivity,
                        LoginActivity::class.java
                    )
                    finish()
                    super.run()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
        t.start()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView( R.layout.activity_splash)
        showSplashAnimation()
    }

    override fun onResume() {
        super.onResume()
        goToLoginPage()
    }
}
package com.example.schoolapp.view.ui.activities

import android.os.Bundle
import android.view.animation.AnimationUtils
import com.example.schoolapp.R
import com.example.schoolapp.common.Utils.openActivity
import com.example.schoolapp.registerlogin.LoginActivity
import com.example.schoolapp.view.ui.base.BaseActivity
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : BaseActivity() {
    /**
     * Let's show our Splash animation using Animation class. We fade in our widgets.
     */
    private fun showSplashAnimation() {
        val animation =  AnimationUtils.loadAnimation(this, R.anim.drop)
        mLogo.startAnimation(animation)
        val fadeIn =  AnimationUtils.loadAnimation(this, R.anim.fade_in)
        mainTitle.startAnimation(fadeIn)
        subTitle.startAnimation(fadeIn)
    }

    private fun goToLoginPage() {
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
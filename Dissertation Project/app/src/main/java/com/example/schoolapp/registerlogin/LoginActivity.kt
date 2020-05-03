package com.example.schoolapp.registerlogin
import android.os.Bundle
import androidx.lifecycle.Observer
import com.google.firebase.auth.FirebaseAuth
import com.example.schoolapp.R
import com.example.schoolapp.common.CacheManager
import com.example.schoolapp.common.Constants
import com.example.schoolapp.common.PermissionManager
import com.example.schoolapp.MainActivity
import com.example.schoolapp.view.ui.base.BaseActivity
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : BaseActivity() {

    private fun validate(): Boolean {
        if (email_edittext_login.text.isNullOrEmpty() || email_edittext_login.text.isBlank()){
            email_edittext_login.error = "Invalid Value"
            return false;
        }
        if (password_edittext_login.text.isNullOrEmpty() || password_edittext_login.text.isBlank()){
            password_edittext_login.error = "Invalid Value"
            return false;
        }
        return true
    }
    private fun login() {
        if (!validate()){
            return
        }
        newsViewModel().login(email_edittext_login.text.toString(),password_edittext_login.text.toString()).observe(this, Observer {
            if (makeRequest(it,"LOGIN")== Constants.SUCCEEDED){
                CacheManager.CURRENT_USER= FirebaseAuth.getInstance().currentUser?.email!!
                if (PermissionManager.isLoggedIn){
                    openPage(MainActivity::class.java)
                    finish()
                }else{
                    show("Something is wrong. Email is null or empty")
                }
            }
        })
    }
    override fun onResume() {
        super.onResume()


        if (FirebaseAuth.getInstance().currentUser != null) {
            CacheManager.CURRENT_USER = FirebaseAuth.getInstance().currentUser!!.email!!
            openPage(MainActivity::class.java)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        login_button_login.setOnClickListener{
            login()
        }
        back_to_registration_text_view.setOnClickListener{     // triggers activity which takes user back to registration screen
            finish()
        }
    }
}


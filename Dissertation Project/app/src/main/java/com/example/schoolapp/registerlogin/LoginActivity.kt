package com.example.schoolapp.registerlogin
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.schoolapp.MainActivity
import com.example.schoolapp.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {    //login activity extends AppCompatActivity

    override fun onCreate(savedInstanceState: Bundle?) {           //main class function which is triggered when the activity is opened
            super.onCreate(savedInstanceState)

            setContentView(R.layout.activity_login)                //sets screen layout to the activity login screen

            login_button_login.setOnClickListener {        // triggers performLogin() function when the login button is pressed
                peformLogin()
                    }
            back_to_registration_text_view.setOnClickListener{     // triggers activity which takes user back to registration screen
                finish()
            }
    }

    private fun peformLogin(){                                            // function to perform login
            val email = email_edittext_login.text.toString()              //takes up email input field in login activity
            val password = password_edittext_login.text.toString()        //takes up password input field in login activity
            if (email.isEmpty() || password.isEmpty()){                   // if the either field is empty send this on-screen message
                Toast.makeText(this, "Please fill out email/pw.", Toast.LENGTH_LONG).show()
                return
            }
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)     //firenbaseAuth ensures that the login details are legitimate
                .addOnCompleteListener {
                    if (!it.isSuccessful) return@addOnCompleteListener
                    Log.d("Login", "Successfully logged in: ${it.result?.user?.uid}")       //logcat test successful message

                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)    // clear the flags of previous ops, send the user to the main activity
                    startActivity(intent)
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to log in: ${it.message}", Toast.LENGTH_LONG).show()  //if the login fails this message will appear on-screen
                }
    }
}



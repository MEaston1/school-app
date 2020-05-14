package com.example.schoolapp.registerlogin


import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.example.schoolapp.MainActivity
import com.example.schoolapp.R
import com.example.schoolapp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*

class RegisterActivity : AppCompatActivity() {  // class extends AppCompatActivity

    companion object{
        val TAG = "RegisterActivity"            // this replaces "RegisterActivity" in the log listeners to make the code shorter
    }

    override fun onCreate(savedInstanceState: Bundle?) {   //"onCreate" is the main function in every class, triggered when the specific activity is opened
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)        // links the class to the xml layout file

        register_button_register.setOnClickListener { // when the register button is pressed the performregister() function is launched
            performRegister()

        }
                                                                      //If the user already has an account when the register button is pressed...
        already_have_an_acount_text_view.setOnClickListener {
            Log.d(TAG, "Try to show login activity")            // sends a message through logcat to prove the function works

            // launches the login activity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        select_photo_button.setOnClickListener {            // this function triggers the onActivityResult() function
            Log.d(TAG, "Try to show photo selector")

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }
    }

    var selectedPhotoUri: Uri? = null                 // crates a uri variable for the selected photo, uris allow images to be converted into data that can be added to a JSON file

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {              // This function displays the photo collection on the phone used
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
            Log.d(TAG, "Photo was selected")

            selectedPhotoUri = data.data

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            select_photo_imageview_register.setImageBitmap(bitmap)

            select_photo_button.alpha = 0f
        }
    }
    private fun performRegister(){                              // this function adds a new account to the firebase database
        val email = email_edittext_register.text.toString()           //creating the email and password account values
        val password = password_edittext_register.text.toString()

        if (email.isEmpty() || password.isEmpty()){                     // If the email and password fields aren't filled a small message is returned telling the user to fill them
            Toast.makeText(this, "Please enter text in email/pw", Toast.LENGTH_LONG).show()
            return
        }

        Log.d(TAG, "Email is: " + email)
        Log.d(TAG, "Password: $password")
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)    //this line adds an instance of the new account to the Firebase Authentication feature
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener

                //else if successful
                Log.d(TAG, "Successfully created user with uid: ${it.result?.user?.uid}")
                Toast.makeText(this, "Successfully created user", Toast.LENGTH_LONG).show()
                uploadImageToFirebaseStorage()                                                             // uploads image previously selected from the phone to the firebase remote storage

                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)          //this clears all past intents before starting the new intent
                startActivity(intent)                                                       //this takes the user to the MainActivity screen
            }
            .addOnFailureListener {                                              // If the app fails to add the user a logcat message and on screen message will be displayed
                Log.d(TAG, "Failed to create user: ${it.message}")
                Toast.makeText(this, "Failed to create user: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }
    private fun uploadImageToFirebaseStorage() {                       // this function puts the image from the new account into the correct file path inside the Firebase Storage
        if (selectedPhotoUri == null) return
        val filename = UUID.randomUUID().toString()
        val ref  = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d(TAG, "Successfully uploaded image: ${it.metadata?.path}")

                ref.downloadUrl.addOnSuccessListener {

                    saveUserToFirebaseDatabase(it.toString())
                }
            }
            .addOnFailureListener {
                Log.d(TAG, "Failed to upload image to storage: ${it.message}")    //logcat failure message
            }
    }

    private fun saveUserToFirebaseDatabase(profileImageUrl: String){                    //this function saves the user to the remote Firebase Database
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        val permsStatus = "basic"
        val user =
            User(                                                                // this makes sure the user's information is all grouped in the database
                uid,
                username_edittext_register.text.toString(),
                profileImageUrl, permsStatus
            )
        ref.setValue(user)
            .addOnSuccessListener {
                Log.d(TAG, "Finally we saved the user to Firebase Database")                //logcat testing success message

                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            .addOnFailureListener {
                Log.d(TAG, "Failed to set value to database ${it.message}")                 //logcat testing failure message
            }
    }
}



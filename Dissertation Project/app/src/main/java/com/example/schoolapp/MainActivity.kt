package com.example.schoolapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import com.example.schoolapp.messages.LatestMessagesActivity
import com.example.schoolapp.misc.MoreListActivity
import com.example.schoolapp.models.User
import com.example.schoolapp.registerlogin.RegisterActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    lateinit var ref: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        moreButton.setOnClickListener {
            startActivity(Intent(this, MoreListActivity::class.java))
        }
        messageButton.setOnClickListener{
            startActivity(Intent(this, LatestMessagesActivity::class.java))
        }
        verifyUserIsLoggedIn()
        getUser()
    }
    private fun verifyUserIsLoggedIn(){
        val uid = FirebaseAuth.getInstance().uid           // gets the user id from firebase authentication services
        if (uid == null) {                                 // if there is no user id then return to the register activity
            val intent = Intent(this, RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item?.itemId){
            R.id.menu_sign_out -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, RegisterActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }

        return super.onOptionsItemSelected(item)
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.logout_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }
    private fun getUser(){
        val welcomeName: TextView = findViewById(R.id.mainactivity_username_msg)
        val userPicture: ImageView = findViewById(R.id.mainactivity_profile_image)
        val currentUser = FirebaseAuth.getInstance().currentUser // find the logged in user id from firebase
        val userUid = currentUser?.uid
        ref = FirebaseDatabase.getInstance().getReference("users/$userUid")
        ref.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapShot: DataSnapshot) {
                val user = snapShot.getValue(User::class.java)
                Picasso.get().load(user?.profileImageUrl).into(userPicture)
                welcomeName.text = user?.username

            }
            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }
}
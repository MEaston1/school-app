package com.example.schoolapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.schoolapp.messages.LatestMessagesActivity
import com.example.schoolapp.models.User
import com.example.schoolapp.registerlogin.RegisterActivity
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var ref: DatabaseReference
    lateinit var toolbar: Toolbar
    lateinit var drawerLayout: DrawerLayout
    lateinit var navView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.content_main)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, 0, 0
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        navView.setNavigationItemSelectedListener(this)
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
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                startActivity(Intent(this, MainActivity::class.java))
            }
            R.id.nav_messages -> {
                startActivity(Intent(this, LatestMessagesActivity::class.java))
            }
            R.id.nav_announcements -> {
                Toast.makeText(this, "Friends clicked", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_about -> {
                Toast.makeText(this, "Update clicked", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_consent_forms -> {
                Toast.makeText(this, "Update clicked", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_absence -> {
                Toast.makeText(this, "Update clicked", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_calendar -> {
                Toast.makeText(this, "Update clicked", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_logout -> {
                Toast.makeText(this, "Sign out clicked", Toast.LENGTH_SHORT).show()
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, RegisterActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}
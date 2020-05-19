package com.example.schoolapp

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.schoolapp.common.CacheManager
import com.example.schoolapp.common.CacheManager.CURRENT_USER
import com.example.schoolapp.common.CacheManager.CURRENT_USER_PERMS
import com.example.schoolapp.common.Constants.ADMIN_USER
import com.example.schoolapp.common.Constants.BASIC_USER
import com.example.schoolapp.common.Constants.EDITOR_USER
import com.example.schoolapp.common.PermissionManager
import com.example.schoolapp.common.Utils.openActivity
import com.example.schoolapp.messages.LatestMessagesActivity
import com.example.schoolapp.models.User
import com.example.schoolapp.registerlogin.RegisterActivity
import com.example.schoolapp.view.ui.activities.*
import com.example.schoolapp.view.ui.base.BaseActivity
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var ref: DatabaseReference
    lateinit var toolbar: Toolbar
    lateinit var drawerLayout: DrawerLayout
    lateinit var navView: NavigationView
    lateinit var navHeaderImageView: ImageView
    lateinit var navHeaderTextView: TextView

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
        consentButton.setOnClickListener{
            startActivity(Intent(this, ConsentUploadActivity::class.java))
        }
        absenceButton.setOnClickListener{
            startActivity(Intent(this, AbsenceUploadActivity::class.java))
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
        val headerView = navView.getHeaderView(0)
        navHeaderImageView = headerView.findViewById(R.id.navHeaderImageView)
        navHeaderTextView = headerView.findViewById(R.id.navHeaderTextView)
        val welcomeName: TextView = findViewById(R.id.mainactivity_username_msg)
        val userPicture: ImageView = findViewById(R.id.mainactivity_profile_image)
        val currentUser = FirebaseAuth.getInstance().currentUser // find the logged in user id from firebase
        val userUid = currentUser?.uid
        ref = FirebaseDatabase.getInstance().getReference("users/$userUid")
        ref.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapShot: DataSnapshot) {
                val user = snapShot.getValue(User::class.java)
                Picasso.get().load(user?.profileImageUrl).into(userPicture)
                Picasso.get().load(user?.profileImageUrl).into(navHeaderImageView)
                welcomeName.text = user?.username
                navHeaderTextView.text = user?.username
                if(user?.permsStatus == "teacher"){
                    CURRENT_USER_PERMS = EDITOR_USER
                }else if(user?.permsStatus == "admin"){
                    CURRENT_USER_PERMS = ADMIN_USER
                }else CURRENT_USER_PERMS = BASIC_USER
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
                Toast.makeText(this, "announcements selected", Toast.LENGTH_SHORT).show()
                openActivity(this, ListingActivity::class.java)
            }
            R.id.nav_about -> {
                startActivity(Intent(this, AboutActivity::class.java))
            }
            R.id.nav_consent_forms -> {
                startActivity(Intent(this, ConsentUploadActivity::class.java))
            }
            R.id.nav_medical_form -> {
                startActivity(Intent(this, MedicalUploadActivity::class.java))
            }
            R.id.nav_absence -> {
                startActivity(Intent(this, AbsenceUploadActivity::class.java))
            }
            R.id.nav_calendar -> {
                Toast.makeText(this, "Feature Not Yet Implemented", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_teachers -> {

                if(PermissionManager.canPublishNews()){
                    Toast.makeText(this, "Access Granted", Toast.LENGTH_SHORT).show()
                    openActivity(this, TeachersActivity::class.java)
                }else Toast.makeText(this, "Insufficient Permission level", Toast.LENGTH_SHORT).show()

            }
            R.id.nav_logout -> {
                Toast.makeText(this, "Sign out clicked", Toast.LENGTH_SHORT).show()
                CacheManager.CURRENT_USER=""
                CacheManager.CURRENT_USER_PERMS=""
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
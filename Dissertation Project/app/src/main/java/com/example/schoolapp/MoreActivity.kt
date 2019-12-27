package com.example.schoolapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MoreActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdaptter: RecyclerView.Adapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_more)
    }

}
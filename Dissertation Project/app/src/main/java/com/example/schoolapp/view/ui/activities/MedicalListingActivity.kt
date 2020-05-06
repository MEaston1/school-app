package com.example.schoolapp.view.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.SearchView
import android.widget.Toast
import com.example.schoolapp.R
import com.example.schoolapp.data.model.entity.Medical
import com.example.schoolapp.databinding.ModelBinding
import com.example.schoolapp.view.ui.base.BaseActivity
import com.squareup.picasso.Picasso
import com.synnapps.carouselview.ImageListener
import easyadapter.dc.com.library.EasyAdapter
import kotlinx.android.synthetic.main.activity_listings.*
import kotlinx.android.synthetic.main.activity_medical_listing.*

class MedicalListingActivity : BaseActivity(),SearchView.OnQueryTextListener, MenuItem.OnActionExpandListener {
    private lateinit var adapter: EasyAdapter<Medical, ModelBinding>

    private var networkImages = arrayOf<String?>(
        "https://images.unsplash.com/photo-1580458072512-96ced1f43991?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=751&q=80"
    )

    // To set simple images
    private var imageListener = ImageListener(fun(position: Int, imageView: ImageView) {
        if (!networkImages[position].isNullOrEmpty()) {
            Picasso.get().load(networkImages[position]).placeholder(R.drawable.placeholder)
                .error(R.drawable.image_not_found).fit()
                .centerCrop().into(imageView)
        }else{
            Picasso.get().load(R.drawable.school)
                .error(R.drawable.image_not_found).fit()
                .centerCrop().into(imageView)
        }
    })
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medical_listing)
    }
}

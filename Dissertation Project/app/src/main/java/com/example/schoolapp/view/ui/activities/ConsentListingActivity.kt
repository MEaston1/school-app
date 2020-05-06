package com.example.schoolapp.view.ui.activities
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.SearchView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import com.synnapps.carouselview.ImageListener
import easyadapter.dc.com.library.EasyAdapter
import com.example.schoolapp.R
import com.example.schoolapp.common.Constants.SUCCEEDED
import com.example.schoolapp.common.PermissionManager
import com.example.schoolapp.common.Utils
import com.example.schoolapp.data.model.entity.Consent
import com.example.schoolapp.databinding.ModelBinding
import com.example.schoolapp.view.ui.base.BaseActivity
import kotlinx.android.synthetic.main.activity_consent_listing.*

class ConsentListingActivity : BaseActivity(), SearchView.OnQueryTextListener, MenuItem.OnActionExpandListener {
    private lateinit var adapter: EasyAdapter<Consent, ModelBinding>

    private var networkImages = arrayOf<String?>(
        "https://images.unsplash.com/photo-1580458072512-96ced1f43991?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=751&q=80"
    )

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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.listings_page_menu, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(this)
        searchView.isIconified = true
        searchView.queryHint = "Search"
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_consent_listing)
    }
}

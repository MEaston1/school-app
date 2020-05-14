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
import com.example.schoolapp.common.Utils.MEM_CACHE
import com.example.schoolapp.common.Utils.getImageURLs
import com.example.schoolapp.common.Utils.openActivity
import com.example.schoolapp.data.model.entity.News
import com.example.schoolapp.databinding.ModelBinding
import com.example.schoolapp.view.ui.base.BaseActivity
import kotlinx.android.synthetic.main.activity_listings.*
import java.util.*

class ListingActivity : BaseActivity(), SearchView.OnQueryTextListener, MenuItem.OnActionExpandListener {
    private lateinit var adapter: EasyAdapter<News, ModelBinding>

    //We define our instance fields
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

    private fun setupCarousel() {
        carouselView!!.setImageListener(imageListener)
        carouselView.setImageClickListener { position: Int ->
            Toast.makeText(this, "Clicked item: $position", Toast.LENGTH_SHORT)
                .show()
        }
        carouselView.pageCount = networkImages.size
    }


    private fun setupStuff() {
        adapter = object : EasyAdapter<News, ModelBinding>(R.layout.model) {
            override fun onBind(binding: ModelBinding, n: News) {
                binding.mTitleTV.text = n.title
                binding.mContentTV.text = n.content

                if (!n.imageURL.isNullOrEmpty()) {
                    Picasso.get().load(n.imageURL)
                        .placeholder(R.drawable.load_dots).error(R.drawable.image_not_found)
                        .into(binding.mImageView)
                }
                binding.contentCard.setOnClickListener {
                    Utils.sendAnnouncementToActivity(a, n, DetailActivity::class.java)
                }

                binding.mViewsTV.text = n.views

                //SEARCH NEWS

                val query = Utils.SEARCH_STRING     //sets as value from utils data class
                if (query.isNotEmpty() && n.title!!.toLowerCase(Locale.getDefault()) // turns to lowercase
                        .contains(query.toLowerCase(Locale.getDefault()))
                ) {
                    val startPos: Int = n.title!!.toLowerCase(Locale.getDefault())
                        .indexOf(query.toLowerCase(Locale.getDefault()))
                    val endPos: Int = startPos + query.length
                    val spanString =
                        Spannable.Factory.getInstance().newSpannable(binding.mTitleTV.text)
                    spanString.setSpan(
                        ForegroundColorSpan(Color.GREEN), startPos, endPos,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    binding.mTitleTV.text = spanString
                }
            }
        }
        /**
         * Today news shown horizontally
         */
        val todayAdapter = adapter
        val hlm = LinearLayoutManager(this)
        hlm.orientation = RecyclerView.HORIZONTAL
        todayRV.layoutManager = hlm
        todayAdapter.addAll(Utils.get5MostRecentNews(MEM_CACHE), true)
        todayRV.adapter = todayAdapter

        adapter.clear(true)
        rv.layoutManager = LinearLayoutManager(this)
        if (MEM_CACHE.size >= 5) {
            val recent = Utils.get5MostRecentNews(MEM_CACHE)
            adapter.addAll(Utils.subtractList(MEM_CACHE, recent), true)
        } else {
            adapter.addAll(MEM_CACHE, true)
        }
        rv.adapter = adapter
        //setup image carousel
        setupCarousel()
    }

    private fun bindData() {
        newsViewModel().allNews.observe(this, Observer { r ->
            if (makeRequest(r, "DOWNLOAD") == SUCCEEDED) {
                val mNews = r.news
                MEM_CACHE = r.news as ArrayList<News>
                if (mNews.isNotEmpty()) {
                    createStateCard(
                        "Successfully Fetched Announcements",
                        mNews.size.toString() + " News Found",
                        isShowing = true,
                        isLoading = false,
                        STATE = SUCCEEDED
                    )
                    networkImages = getImageURLs(mNews)
                    setupStuff()

                } else {
                    createStateCard(
                        "Successfully Connected",
                        "However No News was Found in Database",
                        true,
                        false,
                        SUCCEEDED
                    )
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.listings_page_menu, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(this)
        searchView.isIconified = true
        searchView.queryHint = "Search"
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_new) {
            if(PermissionManager.isLoggedIn){
                openActivity(this, UploadActivity::class.java)
                finish()
            }else{
                Utils.promptLogin(this, "NOT ALLOWED", "You need to Login First")

            }

            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        return false
    }

    override fun onQueryTextChange(query: String): Boolean {
        Utils.SEARCH_STRING = query
        adapter.clear(true)
        adapter.addAll(Utils.filter(query, MEM_CACHE), true)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter
        return false
    }

    override fun onMenuItemActionExpand(item: MenuItem): Boolean {
        return false
    }

    override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
        return false
    }

    override fun onResume() {
        super.onResume()
        Utils.SEARCH_STRING=""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listings)
        bindData()
    }
}
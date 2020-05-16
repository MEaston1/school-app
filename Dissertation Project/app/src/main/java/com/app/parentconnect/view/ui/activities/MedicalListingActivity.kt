package com.app.parentconnect.view.ui.activities

import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.parentconnect.R
import com.app.parentconnect.common.Constants.SUCCEEDED
import com.app.parentconnect.common.Utils
import com.app.parentconnect.common.Utils.M_MEM_CACHE
import com.app.parentconnect.data.model.entity.Medical
import com.app.parentconnect.databinding.ModelBinding
import com.app.parentconnect.view.ui.base.BaseActivity
import com.squareup.picasso.Picasso
import easyadapter.dc.com.library.EasyAdapter
import kotlinx.android.synthetic.main.activity_medical_listing.*
import java.util.*
import kotlin.collections.ArrayList


class MedicalListingActivity : BaseActivity(),SearchView.OnQueryTextListener, MenuItem.OnActionExpandListener {
    private lateinit var adapter: EasyAdapter<Medical, ModelBinding>

    private var networkImages = arrayOf<String?>(
        "https://images.unsplash.com/photo-1580458072512-96ced1f43991?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=751&q=80"
    )

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
        setContentView(R.layout.activity_medical_listing)
        bindData()
    }
    private fun setupStuff() {
        adapter = object : EasyAdapter<Medical, ModelBinding>(R.layout.model) {
            override fun onBind(binding: ModelBinding, n: Medical) {
                binding.mTitleTV.text = n.medicalChildName
                binding.mContentTV.text = n.medicalDescription

                if (!n.medicalImageURL.isNullOrEmpty()) {
                    Picasso.get().load(n.medicalImageURL)
                        .placeholder(R.drawable.load_dots).error(R.drawable.image_not_found)
                        .into(binding.mImageView)
                }
                binding.contentCard.setOnClickListener {
                    Utils.sendMedicalAnnouncementToActivity(a, n, MedicalDetailActivity::class.java)
                }

                binding.mViewsTV.text = n.views

                //SEARCH NEWS

                val query = Utils.SEARCH_STRING
                if (query.isNotEmpty() && n.medicalChildName!!.toLowerCase(Locale.getDefault())
                        .contains(query.toLowerCase(Locale.getDefault()))
                ) {
                    val startPos: Int = n.medicalChildName!!.toLowerCase(Locale.getDefault())
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
        adapter.clear(true)
        medicalRV.layoutManager = LinearLayoutManager(this)
        if(M_MEM_CACHE.size >= 5){
            val recent = Utils.get5MostRecentMedical(M_MEM_CACHE)
            adapter.addAll(Utils.subtractListMedical(M_MEM_CACHE, recent), true)
        } else {
            adapter.addAll(M_MEM_CACHE, true)
        }
        medicalRV.adapter = adapter

    }
    private fun bindData() {
        medicalViewModel().allMedical.observe(this, Observer { r ->
            if (makeMedicalRequest(r, "DOWNLOAD") == SUCCEEDED) {
                val mMedical = r.medical
                M_MEM_CACHE = r.medical as ArrayList<Medical>
                if (mMedical.isNotEmpty()) {
                    createStateCard(
                        "Successfully Fetched Medical Forms",
                        mMedical.size.toString() + " Medical Forms Found",
                        isShowing = true,
                        isLoading = false,
                        STATE = SUCCEEDED
                    )
                    networkImages = Utils.getMedicalImageURLs(mMedical)
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
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_new) {
            Utils.openActivity(this, MedicalUploadActivity::class.java)
            finish()
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
        adapter.addAll(Utils.filterMedical(query, M_MEM_CACHE), true)
        medicalRV.layoutManager = LinearLayoutManager(this)
        medicalRV.adapter = adapter
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
}

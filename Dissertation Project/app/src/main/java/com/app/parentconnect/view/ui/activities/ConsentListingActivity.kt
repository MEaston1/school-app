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
import com.squareup.picasso.Picasso
import easyadapter.dc.com.library.EasyAdapter
import com.app.parentconnect.R
import com.app.parentconnect.common.Constants.SUCCEEDED
import com.app.parentconnect.common.Utils
import com.app.parentconnect.common.Utils.C_MEM_CACHE
import com.app.parentconnect.common.Utils.getConsentImageURLs
import com.app.parentconnect.data.model.entity.Consent
import com.app.parentconnect.databinding.ModelBinding
import com.app.parentconnect.view.ui.base.BaseActivity
import kotlinx.android.synthetic.main.activity_consent_listing.*

import java.util.*

class ConsentListingActivity : BaseActivity(), SearchView.OnQueryTextListener, MenuItem.OnActionExpandListener {
    private lateinit var adapter: EasyAdapter<Consent, ModelBinding>

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
        setContentView(R.layout.activity_consent_listing)
        bindData()
    }

    private fun setupStuff() {
        adapter = object : EasyAdapter<Consent, ModelBinding>(R.layout.model) {
            override fun onBind(binding: ModelBinding, n: Consent) {
                binding.mTitleTV.text = n.consentChildName
                binding.mContentTV.text = n.eventName

                if (!n.consentImageURL.isNullOrEmpty()) {
                    Picasso.get().load(n.consentImageURL)
                        .placeholder(R.drawable.load_dots).error(R.drawable.image_not_found)
                        .into(binding.mImageView)
                }
                binding.contentCard.setOnClickListener {
                    Utils.sendConsentAnnouncementToActivity(a, n, ConsentDetailActivity::class.java)
                }

                binding.mViewsTV.text = n.views

                //SEARCH NEWS

                val query = Utils.SEARCH_STRING
                if (query.isNotEmpty() && n.consentChildName!!.toLowerCase(Locale.getDefault())
                        .contains(query.toLowerCase(Locale.getDefault()))
                ) {
                    val startPos: Int = n.consentChildName!!.toLowerCase(Locale.getDefault())
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
        consentRV.layoutManager = LinearLayoutManager(this)
        if (C_MEM_CACHE.size >= 5) {
            val recent = Utils.get5MostRecentConsent(C_MEM_CACHE)
            adapter.addAll(Utils.subtractListConsent(C_MEM_CACHE, recent), true)
        } else {
            adapter.addAll(C_MEM_CACHE, true)
        }
        consentRV.adapter = adapter
    }

    private fun bindData() {
        consentViewModel().allConsent.observe(this, Observer { r ->
            if (makeConsentRequest(r, "DOWNLOAD") == SUCCEEDED) {
                val mConsent = r.consent
                C_MEM_CACHE = r.consent as ArrayList<Consent>
                if (mConsent.isNotEmpty()) {
                    createStateCard(
                        "Successfully Fetched Consent Forms",
                        mConsent.size.toString() + " Consent Forms Found",
                        isShowing = true,
                        isLoading = false,
                        STATE = SUCCEEDED
                    )
                    networkImages = getConsentImageURLs(mConsent)
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
            Utils.openActivity(this, ConsentUploadActivity::class.java)
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
        adapter.addAll(Utils.filterConsent(query, C_MEM_CACHE), true)
        consentRV.layoutManager = LinearLayoutManager(this)
        consentRV.adapter = adapter
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

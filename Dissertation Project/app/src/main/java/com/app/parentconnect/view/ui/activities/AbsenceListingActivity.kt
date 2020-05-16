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
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import easyadapter.dc.com.library.EasyAdapter
import com.app.parentconnect.R
import com.app.parentconnect.common.Constants.SUCCEEDED
import com.app.parentconnect.common.Utils
import com.app.parentconnect.common.Utils.A_MEM_CACHE
import com.app.parentconnect.common.Utils.getAbsenceImageURLs
import com.app.parentconnect.common.Utils.openActivity
import com.app.parentconnect.data.model.entity.Absence
import com.app.parentconnect.databinding.ModelBinding
import com.app.parentconnect.view.ui.base.BaseActivity
import kotlinx.android.synthetic.main.activity_absence_listing.*
import java.util.*


class AbsenceListingActivity : BaseActivity(), SearchView.OnQueryTextListener, MenuItem.OnActionExpandListener {
    private lateinit var adapter: EasyAdapter<Absence, ModelBinding>

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

    private fun setupStuff() {
        adapter = object : EasyAdapter<Absence, ModelBinding>(R.layout.model) {
            override fun onBind(binding: ModelBinding, n: Absence) {
                binding.mTitleTV.text = n.childName
                binding.mContentTV.text = n.absenceReason

                if (!n.absenceImageURL.isNullOrEmpty()) {
                    Picasso.get().load(n.absenceImageURL)
                        .placeholder(R.drawable.load_dots).error(R.drawable.image_not_found)
                        .into(binding.mImageView)
                }
                binding.contentCard.setOnClickListener {
                    Utils.sendAbsenceAnnouncementToActivity(a, n, AbsenceDetailActivity::class.java)
                }

                binding.mViewsTV.text = n.views

                //SEARCH NEWS

                val query = Utils.SEARCH_STRING
                if (query.isNotEmpty() && n.childName!!.toLowerCase(Locale.getDefault())
                        .contains(query.toLowerCase(Locale.getDefault()))
                ) {
                    val startPos: Int = n.childName!!.toLowerCase(Locale.getDefault())
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
        /*
         Today absences shown horizontally
         */
        val todayAdapter = adapter
        val hlm = LinearLayoutManager(this)
        hlm.orientation = RecyclerView.HORIZONTAL
        absenceTodayRV.layoutManager = hlm
        todayAdapter.addAll(Utils.get5MostRecentAbsences(A_MEM_CACHE), true)
        absenceTodayRV.adapter = todayAdapter

        adapter.clear(true)
        absenceRV.layoutManager = LinearLayoutManager(this)
        if (A_MEM_CACHE.size >= 5) {
            val recent = Utils.get5MostRecentAbsences(A_MEM_CACHE)
            adapter.addAll(Utils.subtractListAbsence(A_MEM_CACHE, recent), true)
        } else {
            adapter.addAll(A_MEM_CACHE, true)
        }
        absenceRV.adapter = adapter
    }

    private fun bindData() {
        absenceViewModel().allAbsence.observe(this, Observer { r ->
            if (makeAbsenceRequest(r, "DOWNLOAD") == SUCCEEDED) {
                val mAbsence = r.absence
                A_MEM_CACHE = r.absence as ArrayList<Absence>
                if (mAbsence.isNotEmpty()) {
                    createStateCard(
                        "Successfully Fetched Announcements",
                        mAbsence.size.toString() + " News Found",
                        isShowing = true,
                        isLoading = false,
                        STATE = SUCCEEDED
                    )
                    networkImages = getAbsenceImageURLs(mAbsence)
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
                openActivity(this, AbsenceUploadActivity::class.java)
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
        adapter.addAll(Utils.filterAbsences(query, A_MEM_CACHE), true)
        absenceRV.layoutManager = LinearLayoutManager(this)
        absenceRV.adapter = adapter
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
        setContentView(R.layout.activity_absence_listing)
        bindData()
    }

}

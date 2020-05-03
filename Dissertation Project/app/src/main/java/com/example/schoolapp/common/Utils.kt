package com.example.schoolapp.common

import android.content.Context
import android.content.Intent
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso
import com.yarolegovich.lovelydialog.LovelyStandardDialog
import com.example.schoolapp.R
import com.example.schoolapp.data.model.entity.News
import com.example.schoolapp.registerlogin.LoginActivity
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

object Utils {
    @JvmField
    var MEM_CACHE: ArrayList<News> = ArrayList()

    @JvmField
    var SEARCH_STRING = ""

    /**
     * Show Toast message for a short time
     * @param c
     * @param message
     */
    fun show(c: Context?, message: String?) {
        Toast.makeText(c, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Open another activity
     * @param c
     * @param clazz
     */
    @JvmStatic
    fun openActivity(c: Context, clazz: Class<*>?) {
        val intent = Intent(c, clazz)
        c.startActivity(intent)
    }

    /**
     * How to create an info dialog
     * @param activity - hosting the dialog
     * @param title - title of the dialog
     * @param message - message in the dialog
     */
    @JvmStatic
    fun showInfoDialog(activity: AppCompatActivity, title: String?, message: String?) {
        LovelyStandardDialog(activity, LovelyStandardDialog.ButtonLayout.HORIZONTAL)
            .setTopColorRes(R.color.indigo)
            .setButtonsColorRes(R.color.darkDeepOrange)
            .setIcon(R.drawable.home_round)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Relax") { }
            .setNegativeButton("Go Back") { activity.finish() }
            .show()
    }
    @JvmStatic
    fun promptLogin(activity: AppCompatActivity, title: String?, message: String?) {
        LovelyStandardDialog(activity, LovelyStandardDialog.ButtonLayout.HORIZONTAL)
            .setTopColorRes(R.color.colorPrimary)
            .setButtonsColorRes(R.color.colorAccent)
            .setIcon(R.drawable.home_round)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Fine") { }
            .setNeutralButton("Login") {
                openActivity(activity, LoginActivity::class.java)
            }
            .show()
    }

    /**
     * Send a news to another activity
     * @param c - Context
     * @param news - The News to be sent
     * @param clazz - Target activity
     */
    @JvmStatic
    fun sendPlanetToActivity(c: Context, news: News?, clazz: Class<*>?) {
        val i = Intent(c, clazz)
        i.putExtra("PLANET_KEY", news)
        c.startActivity(i)
    }

    /**
     * Receive the sent planet
     * @param intent
     * @param c
     * @return
     */
    @JvmStatic
    fun receive(intent: Intent, c: Context?): News? {
        try {
            return intent.getSerializableExtra("PLANET_KEY") as News
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * Load image from online using Picasso
     * @param imageURL
     * @param fallBackImage
     * @param imageView
     */
    @JvmStatic
    fun loadImageFromNetwork(imageURL: String, fallBackImage: Int, imageView: ImageView) {
        if (imageURL.isNotEmpty()) {
            Picasso.get().load(imageURL).placeholder(R.drawable.load_dots).into(imageView)
        } else {
            Picasso.get().load(fallBackImage).into(imageView)
        }
    }

    /**
     * Extract image URL from downloaded data
     * @param news - the lists
     * @return
     */
    @JvmStatic
    fun getImageURLs(news: List<News>): Array<String?> {
        val imageURLs = arrayOfNulls<String>(news.size)
        news.withIndex().forEach({ (i, planet) ->
            imageURLs[i] = planet.imageURL
        })
        return imageURLs
    }

    @JvmStatic
    fun filter(query: String, news: List<News>): ArrayList<News> {
        val hits = ArrayList<News>()
        for (n in news) {
            if (n.title!!.toLowerCase(Locale.getDefault()).contains(query.toLowerCase(Locale.getDefault()))) {
                hits.add(n)
            }
        }
        return hits
    }

    @JvmStatic
    fun getDateToday(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        val currentDate = sdf.format(Date())
        return currentDate
    }

    @JvmStatic
    fun get5MostRecentNews(news: ArrayList<News>): ArrayList<News> {
        val recent = ArrayList<News>()
        if (news.isNullOrEmpty()) {
            return recent
        }

        if (news.size >= 5) {
            var i = 0
            for (n in news.asReversed()) {
                if(i >= 5) {
                    break
                }
                recent.add(n)
                i++
            }
        }else{
            return news
        }
        return recent
    }
    @JvmStatic
    fun subtractList(initialList: ArrayList<News>, toBeRemoved: ArrayList<News>): ArrayList<News> {
        val result = ArrayList<News>()

        if (initialList.isNullOrEmpty()){
            return result
        }
        if (toBeRemoved.isNullOrEmpty()){
            return initialList
        }
        for (n in initialList){
            var hit = false
            for (n1 in toBeRemoved){
                if (n.key.equals(n1.key,true)){
                    hit = true
                }
            }
            if (!hit){
                result.add(n)
            }
        }
        return result
    }
    @JvmStatic
    fun getLike(key: String): String {
        return key
    }


}
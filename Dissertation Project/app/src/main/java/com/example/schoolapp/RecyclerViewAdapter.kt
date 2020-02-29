package com.example.schoolapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.more_activity_layout.view.*

class RecyclerViewAdapter(val context: Context, val features: List<Feature>) : RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder() {

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var currentFeature: Feature? = null
        var currentPosition: Int = 0

        init {
            itemView.setOnClickListener{
                Toast.makeText(context, currentFeature!!.title + " selected! ", Toast.LENGTH_SHORT).show()
            }
        }

        fun setData(Feature: Feature?, pos: Int){
        itemView.textTitle.text = Feature!!.title

            this.currentFeature = Feature
            this.currentPosition = pos
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.more_activity_layout, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return features.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val Feature = features [position]
        holder.setData(Feature, position)
    }
}
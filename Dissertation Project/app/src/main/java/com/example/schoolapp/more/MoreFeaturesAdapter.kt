package com.example.schoolapp.more

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_more_layout.view.*
import com.example.schoolapp.R

class MoreFeaturesAdapter (private val features: List<MoreActivityDetails>, private val featureClicked: () -> Unit) : RecyclerView.Adapter <MoreFeaturesAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder{
        val layoutView =
            LayoutInflater.from(parent.context).inflate(R.layout.activity_more_layout, parent, false)
        return ViewHolder(layoutView)
    }
    override fun getItemCount() = features.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val feature = features[position]
        holder.view.featureName.text = "${feature.featureName}"
        holder.view.photoOfFeature.setImageDrawable(holder.view.context.getDrawable(feature.featurePhoto))
        holder.view.setOnClickListener{
            featureClicked.invoke()
        }
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)
}
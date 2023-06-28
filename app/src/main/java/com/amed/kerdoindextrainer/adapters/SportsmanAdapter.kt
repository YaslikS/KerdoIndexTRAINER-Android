package com.amed.kerdoindextrainer.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.amed.kerdoindextrainer.R
import com.amed.kerdoindextrainer.model.User

class SportsmanAdapter() : RecyclerView.Adapter<SportsmanAdapter.MyViewHolder>() {

    lateinit var sportsmanList: List<User>

    fun setContentList(list: List<User>) {
        this.sportsmanList = list
        notifyDataSetChanged()
    }

    inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var nameSportsmanItem = itemView.findViewById<TextView>(R.id.nameTVinCell)!!
        var emailSportsmanItem = itemView.findViewById<TextView>(R.id.emailTVinCell)!!
        var yearMovieItem = itemView.findViewById<ImageView>(R.id.appCompatImageView)!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.cell_sportsman, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int = sportsmanList.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.nameSportsmanItem.text = sportsmanList[position].name
        holder.emailSportsmanItem.text = sportsmanList[position].email
    }

}
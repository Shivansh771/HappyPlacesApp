package com.example.happyplaces.adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.happyplaces.databinding.ItemHappyPlaceBinding
import com.example.happyplaces.models.HappyPlaceModel

class HappyPlacesAdapter ( var list : ArrayList < HappyPlaceModel > ) :
    RecyclerView.Adapter < HappyPlacesAdapter.ViewHolder > () {

    inner class ViewHolder ( binding : ItemHappyPlaceBinding ) :
        RecyclerView.ViewHolder ( binding.root ) {
        val tvTitle = binding.tvTitle
        val tvDescription = binding.tvDescription
        val civPlaceImage = binding.ivPlaceImage
    }

    override fun onCreateViewHolder ( parent : ViewGroup, viewType : Int ) : ViewHolder {
        return ViewHolder ( ItemHappyPlaceBinding.inflate (
            LayoutInflater.from ( parent.context ), parent, false
        )
        )
    }

    override fun onBindViewHolder ( holder : ViewHolder, position : Int ) {
        val model : HappyPlaceModel = list [ position ]

        if ( holder is ViewHolder ) {
            holder.civPlaceImage.setImageURI ( Uri.parse ( model.image ) )
            holder.tvTitle.text = model.title
            holder.tvDescription.text = model.description
        }
    }

    override fun getItemCount () : Int {
        return list.size
    }

}

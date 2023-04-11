package com.example.happyplaces.adapters

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.happyplaces.activities.AddHappyPlaceActivity
import com.example.happyplaces.activities.MainActivity
import com.example.happyplaces.databinding.ItemHappyPlaceBinding
import com.example.happyplaces.models.HappyPlaceModel
import android.content.Context
import android.provider.ContactsContract.Data
import com.example.happyplaces.database.DatabaseHandler
import www.sanju.motiontoast.MotionToast

class HappyPlacesAdapter (private val context: Context,var list : ArrayList < HappyPlaceModel > ) :
    RecyclerView.Adapter < HappyPlacesAdapter.ViewHolder > () {
    private var onClickListener: OnClickListener? = null
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
    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    override fun onBindViewHolder ( holder : ViewHolder, position : Int ) {
        val model : HappyPlaceModel = list [ position ]

        if ( holder is ViewHolder ) {
            holder.civPlaceImage.setImageURI ( Uri.parse ( model.image ) )
            holder.tvTitle.text = model.title
            holder.tvDescription.text = model.description
            holder.itemView.setOnClickListener {

                if (onClickListener != null) {
                    onClickListener!!.onClick(position, model)
                }
            }
        }
    }

    fun notifyEditItem(activity: Activity, position: Int, requestCode: Int) {
        val intent = Intent(context,AddHappyPlaceActivity::class.java)
        intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS, list[position])
        activity.startActivityForResult(
            intent,
            requestCode
        ) // Activity is started with requestCode

        notifyItemChanged(position) // Notify any registered observers that the item at position has changed.
    }
    fun removeAt(position: Int){
        val dbHandler=DatabaseHandler(context)
        val isDelete=dbHandler.deleteHappyPlace(list[position])
        if(isDelete>0){
            list.removeAt(position)
            notifyItemRemoved(position)
        }
    }
    override fun getItemCount () : Int {
        return list.size
    }
    interface OnClickListener {
        fun onClick(position: Int, model: HappyPlaceModel)
    }
}



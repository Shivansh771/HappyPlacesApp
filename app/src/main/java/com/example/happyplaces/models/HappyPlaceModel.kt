package com.example.happyplaces.models

import android.icu.text.CaseMap.Title
import java.io.FileDescriptor

data class HappyPlaceModel(
    val id:Int, val title: String,val image:String, val description:String, val date:String,val location:String, val latitude:Double,val longitude:Double
)
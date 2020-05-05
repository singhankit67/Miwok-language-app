package com.example.miwokapp

import android.media.MediaPlayer
import android.widget.ImageView

class Word/*(defaultTranslation:String , miwokTranslation:String , images:Int , songs:Int)*/ {
    var mDefaultTranslation:String? = null
    var mMiwokTranslation:String? = null
    private val NO_IMAGE_PROVIDED = -1
    var mImages = NO_IMAGE_PROVIDED

    var mSongs:Int = 0


    constructor(defaultTranslation:String , miwokTranslation:String , images:Int , songs:Int){

        mDefaultTranslation = defaultTranslation
        mMiwokTranslation = miwokTranslation
        this.mImages = images
        mSongs = songs

    }
    constructor(defaultTranslation:String , miwokTranslation:String , songs:Int ){
        mDefaultTranslation = defaultTranslation
        mMiwokTranslation = miwokTranslation
        mSongs = songs

    }


    fun hasImage():Boolean{
        return mImages != NO_IMAGE_PROVIDED
    }
}
package com.example.miwokapp

import android.app.Activity
import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

class WordAdapter(Context:Activity,Words:ArrayList<Word>):ArrayAdapter<Word>(Context,0,Words)  {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        var listItemView = convertView
        if(listItemView == null){
            listItemView=LayoutInflater.from(getContext()).inflate(R.layout.singlevalueview,parent,false)
        }
        var currentWord = getItem(position)
        val miwoknameTextView = listItemView?.findViewById(R.id.miwokWord) as TextView
        miwoknameTextView.setText(currentWord.mMiwokTranslation)
        val defaultnameTextView = listItemView.findViewById(R.id.englishWord) as TextView
        defaultnameTextView.setText(currentWord.mDefaultTranslation)


        val imagesView = listItemView?.findViewById(R.id.image) as ImageView
        if(currentWord.hasImage()) {
            imagesView.setImageResource(currentWord.mImages)
            imagesView.visibility = View.VISIBLE
        }else{
            imagesView.visibility = View.GONE
        }
        return listItemView


    }
    //companion object {
        //private val LOG_TAG = WordAdapter::class.java!!.getSimpleName()
    //}
}
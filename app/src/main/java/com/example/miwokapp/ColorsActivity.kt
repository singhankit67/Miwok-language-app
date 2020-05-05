package com.example.miwokapp

import android.content.Intent
import android.media.MediaPlayer
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.NonNull
import android.support.design.widget.BottomNavigationView
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ListView

class ColorsActivity : AppCompatActivity() {
    var mMediaPlayer:MediaPlayer? = null
    private val mCompletionListener = object: MediaPlayer.OnCompletionListener {
        override fun onCompletion(mp:MediaPlayer) {
            releaseMediaPlayer()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_colors)
        val bottomNavigation : BottomNavigationView? = findViewById(R.id.btm_nav) as BottomNavigationView
        bottomNavigation?.setSelectedItemId(R.id.Colors)
        bottomNavigation?.setOnNavigationItemSelectedListener(
            object:BottomNavigationView.OnNavigationItemSelectedListener {
                override fun onNavigationItemSelected(@NonNull item:MenuItem):Boolean {
                    when (item.itemId) {
                        R.id.Numbers -> {
                            startActivity(Intent(applicationContext, NumbersActivity::class.java))
                            overridePendingTransition(0, 0)
                            return true
                        }
                        R.id.Familymembers -> {
                            startActivity(Intent(applicationContext, FamilyActivity::class.java))
                            overridePendingTransition(0, 0)
                            return true
                        }
                        R.id.Colors -> {
                            startActivity(Intent(applicationContext, ColorsActivity::class.java))
                            overridePendingTransition(0, 0)
                            return true
                        }
                        R.id.Phrases -> {
                            startActivity(Intent(applicationContext, PhrasesActivity::class.java))
                            overridePendingTransition(0, 0)
                            return true
                        }
                    }
                    return true
                }
            })
        val actionbar = supportActionBar
        actionbar!!.title = "Colors"
        //set back button
        actionbar.setDisplayHomeAsUpEnabled(true)
        actionbar.setDisplayHomeAsUpEnabled(true)
        var arrayList1 = ArrayList<Word>()
        arrayList1.add(Word("red","weṭeṭṭi",R.drawable.color_red,R.raw.color_red))
        arrayList1.add(Word("green","chokokki",R.drawable.color_green,R.raw.color_green))
        arrayList1.add(Word("brown","ṭakaakki",R.drawable.color_brown,R.raw.color_brown))
        arrayList1.add(Word("gray","ṭopoppi",R.drawable.color_gray,R.raw.color_gray))
        arrayList1.add(Word("black","kululli",R.drawable.color_black,R.raw.color_black))
        arrayList1.add(Word("white","kelelli",R.drawable.color_white,R.raw.color_white))
        arrayList1.add(Word("dusty yellow","ṭopiisә",R.drawable.color_dusty_yellow,R.raw.color_dusty_yellow))
        arrayList1.add(Word("mustard yellow","chiwiiṭә",R.drawable.color_mustard_yellow,R.raw.color_mustard_yellow))

        //var count = 0
        val Adapter1 = WordAdapter(this,arrayList1)
        val listView = findViewById(R.id.list1) as ListView
        listView.setAdapter(Adapter1)
        listView.setOnItemClickListener(object: AdapterView.OnItemClickListener {
            override fun onItemClick(adapterView:AdapterView<*>, view: View, position:Int, l:Long) {
                // Get the {@link Word} object at the given position the user clicked on
                val word = arrayList1.get(position)
                releaseMediaPlayer()
                // Create and setup the {@link MediaPlayer} for the audio resource associated
                // with the current word
                var mMediaPlayer = MediaPlayer.create(this@ColorsActivity, word.mSongs)
                // Start the audio file
                mMediaPlayer.start()
                mMediaPlayer.setOnCompletionListener(mCompletionListener)
            }
        })
    }
    private fun releaseMediaPlayer() {
        // If the media player is not null, then it may be currently playing a sound.
        if (mMediaPlayer != null)
        {
            // Regardless of the current state of the media player, release its resources
            // because we no longer need it.
            mMediaPlayer?.release()
            // Set the media player back to null. For our code, we've decided that
            // setting the media player to null is an easy way to tell that the media player
            // is not configured to play an audio file at the moment.
            mMediaPlayer = null
        }
    }

}

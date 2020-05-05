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

class PhrasesActivity : AppCompatActivity() {
    var mMediaPlayer: MediaPlayer? = null
    private val mCompletionListener = object: MediaPlayer.OnCompletionListener {
        override fun onCompletion(mp:MediaPlayer) {
            releaseMediaPlayer()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phrases)
        val bottomNavigation : BottomNavigationView? = findViewById(R.id.btm_nav) as BottomNavigationView
        bottomNavigation?.setSelectedItemId(R.id.Phrases)
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
        actionbar!!.title = "Phrases"
        //set back button
        actionbar.setDisplayHomeAsUpEnabled(true)
        actionbar.setDisplayHomeAsUpEnabled(true)
        val arrayList2:ArrayList<Word> = ArrayList()
        arrayList2.add(Word("Where are you going?","minto wuksus",R.raw.phrase_where_are_you_going))
        arrayList2.add(Word("What is your name?","tinnә oyaase'nә",R.raw.phrase_what_is_your_name))
        arrayList2.add(Word("My name is...","oyaaset...",R.raw.phrase_my_name_is))
        arrayList2.add(Word("How are you feeling?","michәksәs?",R.raw.phrase_how_are_you_feeling))
        arrayList2.add(Word("I’m feeling good","kuchi achit",R.raw.phrase_im_feeling_good))
        arrayList2.add(Word("Are you coming?","әәnәs'aa?",R.raw.phrase_are_you_coming))
        arrayList2.add(Word("Yes, I’m coming.","hәә’ әәnәm",R.raw.phrase_yes_im_coming))
        arrayList2.add(Word("I’m coming.","әәnәm",R.raw.phrase_im_coming))
        arrayList2.add(Word("Let’s go.","yoowutis",R.raw.phrase_lets_go))
        arrayList2.add(Word("Come here.","әnni'nem",R.raw.phrase_come_here))

        //var count = 0
        val Adapter2 = WordAdapter(this,arrayList2)
        val listView = findViewById(R.id.list2) as ListView
        listView.setAdapter(Adapter2)
        listView.setOnItemClickListener(object: AdapterView.OnItemClickListener {
            override fun onItemClick(adapterView:AdapterView<*>, view:View, position:Int, l:Long) {
                // Get the {@link Word} object at the given position the user clicked on
                val word = arrayList2.get(position)
                releaseMediaPlayer() //to give up on the current playing audio and start the nxt one which is being clicked
                // Create and setup the {@link MediaPlayer} for the audio resource associated
                // with the current word
                val mMediaPlayer = MediaPlayer.create(this@PhrasesActivity, word.mSongs)
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
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

}

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

class FamilyActivity : AppCompatActivity() {
    var mMediaPlayer: MediaPlayer? = null
    private val mCompletionListener = object: MediaPlayer.OnCompletionListener {
        override fun onCompletion(mp:MediaPlayer) {
            releaseMediaPlayer()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_family)
        val bottomNavigation : BottomNavigationView? = findViewById(R.id.btm_nav) as BottomNavigationView
        bottomNavigation?.setSelectedItemId(R.id.Familymembers)
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
        var actionbar1 = supportActionBar
        actionbar1!!.title = "Family"
        //set back button
        actionbar1.setDisplayHomeAsUpEnabled(true)
        actionbar1.setDisplayHomeAsUpEnabled(true)
        var arrayList3 = ArrayList<Word>()
        arrayList3.add(Word("father", "әpә", R.drawable.family_father, R.raw.family_father))
        arrayList3.add(Word("mother", "әṭa", R.drawable.family_mother, R.raw.family_mother))
        arrayList3.add(Word("son", "angsi", R.drawable.family_son, R.raw.family_son))
        arrayList3.add(Word("daughter", "tune", R.drawable.family_daughter, R.raw.family_daughter))
        arrayList3.add(
            Word(
                "older brother",
                "taachi",
                R.drawable.family_older_brother,
                R.raw.family_older_brother
            )
        )

        arrayList3.add(
            Word(
                "younger brother",
                "chalitti",
                R.drawable.family_younger_brother,
                R.raw.family_younger_brother
            )
        )
        arrayList3.add(
            Word(
                "older sister",
                "teṭe",
                R.drawable.family_older_sister,
                R.raw.family_older_sister
            )
        )
        arrayList3.add(
            Word(
                "younger sister",
                "kolliti",
                R.drawable.family_younger_sister,
                R.raw.family_younger_sister
            )
        )
        arrayList3.add(
            Word(
                "grandmother",
                "ama",
                R.drawable.family_grandmother,
                R.raw.family_grandmother
            )
        )
        arrayList3.add(
            Word(
                "grandfather",
                "paapa",
                R.drawable.family_grandfather,
                R.raw.family_grandfather
            )
        )


        //var count = 0
        val Adapter3 = WordAdapter(this, arrayList3)
        val listView = findViewById(R.id.list3) as ListView
        listView.setAdapter(Adapter3)
        listView.setOnItemClickListener(object : AdapterView.OnItemClickListener {
            override fun onItemClick(
                adapterView: AdapterView<*>,
                view: View,
                position: Int,
                l: Long
            ) {
                // Get the {@link Word} object at the given position the user clicked on
                val word = arrayList3.get(position)
                releaseMediaPlayer()
                // Create and setup the {@link MediaPlayer} for the audio resource associated
                // with the current word
                var mMediaPlayer = MediaPlayer.create(this@FamilyActivity, word.mSongs)
                // Start the audio file
                mMediaPlayer?.start()
                mMediaPlayer?.setOnCompletionListener(mCompletionListener)
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

    


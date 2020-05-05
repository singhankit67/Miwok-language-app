package com.example.miwokapp

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer

import android.os.Bundle
import android.os.Handler
import android.support.annotation.NonNull
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem

import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import com.example.miwokapp.R.id.Numbers
import java.util.concurrent.TimeUnit


class NumbersActivity : AppCompatActivity() {
    var myActivity: Activity? = null
    var mMediaPlayer: MediaPlayer? = null
    var numberAct : NumbersActivity? = null
    var coloract : ColorsActivity? = null
    var familyact : FamilyActivity? = null
    var phrasesact : PhrasesActivity? = null
    var menuItem:MenuItem? = null

   // lateinit var afChangeListener:AudioManager.OnAudioFocusChangeListener
    private val afChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                releaseMediaPlayer()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                mMediaPlayer?.pause()
                mMediaPlayer?.seekTo(0)
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                // Lower the volume, keep playing
                mMediaPlayer?.pause()
                mMediaPlayer?.seekTo(0)
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                // Your app has been granted audio focus again
                // Raise volume to normal, restart playback if necessary
                mMediaPlayer?.start()
            }
        }
    }
    var audioManager: AudioManager? = null
    private val mCompletionListener = object: MediaPlayer.OnCompletionListener {
        override fun onCompletion(mp:MediaPlayer) {
            releaseMediaPlayer()
        }
    }

    //var dispalyNumbersList: LinearLayout? = null

    /*fun onCreateView( inflater: LayoutInflater, container: ViewGroup?,
                               savedInstanceState: Bundle?
    ): View? {
        //which ever action is done should be performed on numbers page
        val view = inflater. inflate(R.layout.activity_numbers, container, false)
        myActivity?.title= "Numbers List"
        return view

    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_numbers)
        val bottomNavigation : BottomNavigationView? = findViewById(R.id.btm_nav) as BottomNavigationView
        bottomNavigation?.setSelectedItemId(R.id.Numbers)
       /* bottomNavigation?.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener())
        run({
            val onNavigationItemSelected(@NonNull menuItem: MenuItem?):Boolean
            run({ when (menuItem.getItemId()) {
                R.id.dashboard -> {
                    startActivity(Intent(getApplicationContext(), dashboard::class.java))
                    overridePendingTransition(0, 0)
                    return true
                }
                R.id.dashboard -> {
                    startActivity(Intent(getApplicationContext(), dashboard::class.java))
                    overridePendingTransition(0, 0)
                    return true
                }
                R.id.home -> {
                    startActivity(Intent(getApplicationContext(), home::class.java))
                    overridePendingTransition(0, 0)
                    return true
                }
                R.id.about -> {
                    startActivity(Intent(getApplicationContext(), about::class.java))
                    overridePendingTransition(0, 0)
                    return true
                }
            }
                return false }) })*/
        //bottomNavigation.selectedItemId(Numbers)
        /*bottomNavigation.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemReselectedListener())
        run({
            val onNavigationItemSelected:Boolean
            (MenuItem)
            menuItem
            run({ return false }) })*/
        //bottomNavigation?.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener{menuItem ->
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

        //})
            /*when(item.itemId) {
                R.id.Colors->{
                    coloract = ColorsActivity()
                    val fm =(object  as AppCompatActivity).supportFragmentManager
                    val ft = fm.beginTransaction()

                        //ft.add(R.id.frameLayout, coloract)
                    ft.add(R.id.frameLayout,coloract)
                    ft.commit()
                }

            }
            true
        }*/
        val actionbar = supportActionBar
        actionbar!!.title = "Numbers"
        //set back button
        actionbar.setDisplayHomeAsUpEnabled(true)
        actionbar.setDisplayHomeAsUpEnabled(true)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager?
        //lateinit var afChangeListener:AudioManager.OnAudioFocusChangeListener

        var arrayList = ArrayList<Word>()
        arrayList.add(Word("one", "lutti", R.drawable.number_one, R.raw.number_one))
        arrayList.add(Word("two", "otiiko", R.drawable.number_two, R.raw.number_two))
        arrayList.add(Word("three", "tolookosu", R.drawable.number_three, R.raw.number_three))
        arrayList.add(Word("four", "oyyisa", R.drawable.number_four, R.raw.number_four))
        arrayList.add(Word("five", "massokka", R.drawable.number_five, R.raw.number_five))
        arrayList.add(Word("six", "temmokka", R.drawable.number_six, R.raw.number_six))
        arrayList.add(Word("seven", "kenekaku", R.drawable.number_seven, R.raw.number_seven))
        arrayList.add(Word("eight", "kawinta", R.drawable.number_eight, R.raw.number_eight))
        arrayList.add(Word("nine", "wo'e", R.drawable.number_nine, R.raw.number_nine))
        arrayList.add(Word("ten", "na'aacha", R.drawable.number_ten, R.raw.number_ten))
        //var count = 0
        val Adapter = WordAdapter(this, arrayList)
        val listView = findViewById(R.id.list) as ListView
        listView.setAdapter(Adapter)
        listView.setOnItemClickListener(object : AdapterView.OnItemClickListener {
            override fun onItemClick(
                adapterView: AdapterView<*>,
                view: View,
                position: Int,
                l: Long
            ) {
                // Get the {@link Word} object at the given position the user clicked on
                val word = arrayList.get(position)
                releaseMediaPlayer() //to give up on the current playing audio and start the nxt one which is being clicked
                // Create and setup the {@link MediaPlayer} for the audio resource associated
                // with the current word
                //lateinit var afChangeListener AudioManager.OnAudioFocusChangeListener
// Request audio focus for playback

                val result: Int = audioManager!!.requestAudioFocus(
                    afChangeListener,
                    // Use the music stream.
                    AudioManager.STREAM_MUSIC,
                    // Request permanent focus.
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
                )

                if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    // Start playback

                    var mMediaPlayer = MediaPlayer.create(this@NumbersActivity, word.mSongs)
                    // Start the audio file
                    mMediaPlayer.start()
                    mMediaPlayer.setOnCompletionListener(mCompletionListener)
                }
            }
        })
    }

    private fun releaseMediaPlayer() {
        // If the media player is not null, then it may be currently playing a sound.
        if (mMediaPlayer != null) {
            // Regardless of the current state of the media player, release its resources
            // because we no longer need it.
            mMediaPlayer?.release()
            // Set the media player back to null. For our code, we've decided that
            // setting the media player to null is an easy way to tell that the media player
            // is not configured to play an audio file at the moment.
            mMediaPlayer = null
            audioManager?.abandonAudioFocus(afChangeListener)
        }
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}


        //val displayNumbersList = findViewById(R.id.list) as LinearLayout
            //while(count<arrayList.size) {
                //var display = TextView(this) //to store the ans in text format on this screen itself
                //display.setText(arrayList.get(count)) //a text view to display the value in txt format
                //displayNumbersList.addView(display)// to place it on the numbers screen
                //count = count + 1
            //}

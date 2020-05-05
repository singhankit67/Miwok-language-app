package com.example.miwokapp

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler

class Splash : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        Handler().postDelayed({
            val startAct = Intent(this@Splash, NumbersActivity::class.java)
            startActivity(startAct) // to initialize th start act function
            this.finish()
        }, 5000)
    }
}

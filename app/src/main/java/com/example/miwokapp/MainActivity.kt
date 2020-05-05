package com.example.miwokapp

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //numberList()
        var numbersList = findViewById(R.id.numbers) as TextView
        numbersList.setOnClickListener({
            val nolist = Intent(this@MainActivity, NumbersActivity::class.java)
            startActivity(nolist)
        })
        //phrasesList
        var phrasesList = findViewById(R.id.phrases) as TextView
        phrasesList.setOnClickListener({
            val phstart = Intent(this@MainActivity, PhrasesActivity::class.java)
            startActivity(phstart)
        })
        var colorsList = findViewById(R.id.colors) as TextView
        colorsList.setOnClickListener({
            val clstart = Intent(this@MainActivity, ColorsActivity::class.java)
            startActivity(clstart)
        })
        var familyList = findViewById(R.id.family) as TextView
        familyList.setOnClickListener({
            val fmstart = Intent(this@MainActivity, FamilyActivity::class.java)
            startActivity(fmstart)
        })
    }

}

package com.reelvideos.app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.chahinem.pageindicator.PageIndicator;
import com.google.android.material.button.MaterialButton;
import com.reelvideos.app.adapters.IntroPageSliderAdapter;
import com.reelvideos.app.base.CoreApp;
import com.reelvideos.app.config.GlobalVariables;

import static com.reelvideos.app.config.Constants.PREF_USER_SAW_INTRO_PAGE;

public class appInto extends AppCompatActivity {

    private ViewPager viewPager;
    private PageIndicator pageIndicator;
    private IntroPageSliderAdapter introPageSliderAdapter;

    MaterialButton button;

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String TEXT ="SAW_INTRO";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_app_into );


        viewPager= findViewById ( R.id.viewPager );
        pageIndicator= findViewById ( R.id.indicator );

        introPageSliderAdapter = new IntroPageSliderAdapter ( this );

        viewPager.setAdapter ( introPageSliderAdapter );

        pageIndicator.attachTo ( viewPager );
        viewPager.addOnPageChangeListener ( viewListener );

        button= findViewById ( R.id.skipBtn );
        button.setOnClickListener (view1 -> {
            Intent intent=new Intent(appInto.this, MainActivity.class);
            startActivity(intent);
            saveData();
            overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
            finish();
        });
    }
    
    private void saveData() {
        SharedPreferences sharedPreferences = CoreApp.getInstance().getSharedPreferences();
        sharedPreferences.edit().putBoolean(TEXT, true).apply();
    }

    ViewPager.OnPageChangeListener viewListener = new ViewPager.OnPageChangeListener () {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {

            if (position==2){
                button.setText ( getString(R.string.explore_now) );
            }
            else
                button.setText ( getString(R.string.skip_txt) );
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };
}
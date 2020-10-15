package com.reelvideos.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.reelvideos.app.config.GlobalVariables;

public class SplashActivity extends AppCompatActivity {

    CountDownTimer countDownTimer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);


        countDownTimer = new CountDownTimer(1500, 1000) {

            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {


//                if (GlobalVariables.hasUserSawIntroPage ())
//                {
                    Intent intent=new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
                    finish();
//                }
//
//                else{
//                    Intent intent=new Intent(SplashActivity.this, appInto.class);
//                    startActivity(intent);
//                    overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
//                    finish();
//                }

            }
        }.start();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        countDownTimer.cancel();
    }

    public boolean loadData() {
//        SharedPreferences sharedPreferences = getSharedPreferences ( SHARED_PREFS, MODE_PRIVATE);
//        return sharedPreferences.getBoolean ( TEXT,true);
        return GlobalVariables.hasUserSawIntroPage();
    }

}
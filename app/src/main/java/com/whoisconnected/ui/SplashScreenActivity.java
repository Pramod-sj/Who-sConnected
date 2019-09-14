package com.whoisconnected.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;

import com.whoisconnected.R;

public class SplashScreenActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        sharedPreferences= PreferenceManager.getDefaultSharedPreferences(this);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(sharedPreferences.getBoolean("SHOW_ON_BOARDING",true)) {
                    ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(SplashScreenActivity.this, findViewById(R.id.logoImageView), "LOGO_IMAGEVIEW");
                    startActivity(new Intent(SplashScreenActivity.this, BoardingActivity.class), activityOptions.toBundle());
                    SplashScreenActivity.this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
                else{
                    startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
                    finish();
                }
            }
        },1000);
    }
}

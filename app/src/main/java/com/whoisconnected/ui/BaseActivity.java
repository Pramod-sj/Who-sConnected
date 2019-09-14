package com.whoisconnected.ui;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.widget.ContentLoadingProgressBar;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.snackbar.Snackbar;
import com.whoisconnected.R;

public abstract class  BaseActivity extends AppCompatActivity {
    private TextView toolbarTextView;
    private ImageButton navigationButton,optionMenuButton;
    private AppBarLayout appBarLayout;
    private CoordinatorLayout coordinatorLayout;
    private ContentLoadingProgressBar contentLoadingProgressBar;
    private WifiManager wifiManager;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wifiManager= (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
    }

    @Override
    public void setContentView(int layoutResID) {
        View parentView=LayoutInflater.from(this).inflate(R.layout.activity_base,null,false);
        FrameLayout container=parentView.findViewById(R.id.frameLayout);
        coordinatorLayout=parentView.findViewById(R.id.coordinatorLayout);
        appBarLayout=parentView.findViewById(R.id.appBarLayout);
        navigationButton=parentView.findViewById(R.id.toolbarNavigationButton);
        optionMenuButton=parentView.findViewById(R.id.toolbarOptionMenuButton);
        toolbarTextView=parentView.findViewById(R.id.toolbartext);
        contentLoadingProgressBar=parentView.findViewById(R.id.progressBar);
        getLayoutInflater().inflate(layoutResID, container, true);
        super.setContentView(parentView);
    }

    @Override
    public void setTitle(int titleId) {
        super.setTitle(titleId);
        toolbarTextView.setText(getString(titleId));
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        toolbarTextView.setText(title);
    }

    public void showProgress(int progress){
        contentLoadingProgressBar.setIndeterminate(false);
        contentLoadingProgressBar.show();
        contentLoadingProgressBar.setProgress(progress);
    }

    public void updateProgress(int progress){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            contentLoadingProgressBar.setProgress(progress,true);
        }
        else{
            contentLoadingProgressBar.setProgress(progress);
        }
    }

    public void showIndeterminateProgress(){
        contentLoadingProgressBar.setIndeterminate(true);
        contentLoadingProgressBar.setVisibility(View.VISIBLE);
        contentLoadingProgressBar.show();
    }
    public void hideIndeterminateProgress(){
        contentLoadingProgressBar.setIndeterminate(false);
        contentLoadingProgressBar.hide();
    }


    public void hideProgress(){
        contentLoadingProgressBar.hide();
    }

    public void showSnackbar(String message){
        Snackbar.make(coordinatorLayout,message,Snackbar.LENGTH_SHORT).setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).show();
    }
    public void showSnackbarWithAction(String message, String buttonText, View.OnClickListener onClickListener){
        Snackbar.make(coordinatorLayout,message,Snackbar.LENGTH_INDEFINITE).setAction(buttonText,onClickListener).setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).show();
    }


    public void showSnackbarWithAction(String message, int duration, String buttonText, View.OnClickListener onClickListener){
        Snackbar.make(coordinatorLayout,message,duration).setAction(buttonText,onClickListener).setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).show();
    }

    public void showSnackbarWithView(View view,String message){
        Snackbar.make(view,message,Snackbar.LENGTH_SHORT)
                .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).show();
    }

    public void showSnackbar(String message,int duration){
        Snackbar.make(coordinatorLayout,message,duration).setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).show();
    }

    public AppBarLayout getAppBarLayout() {
        return appBarLayout;
    }

    public CoordinatorLayout getCoordinatorLayout() {
        return coordinatorLayout;
    }

    public void setHomeButton(int drawableId, View.OnClickListener onClickListener){
        navigationButton.setVisibility(View.VISIBLE);
        navigationButton.setImageResource(drawableId);
        navigationButton.setOnClickListener(onClickListener);
    }

    public void setOptionMenuButton(int drawableId, View.OnClickListener onClickListener){
        optionMenuButton.setVisibility(View.VISIBLE);
        optionMenuButton.setImageResource(drawableId);
        optionMenuButton.setOnClickListener(onClickListener);
    }




    public WifiManager getWifiManager() {
        return wifiManager;
    }

    public boolean isConnectedOverWifi(){
        ConnectivityManager connectivityManager= (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo=connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return networkInfo != null && networkInfo.isConnected();
    }

    public void shakeView(final View view, boolean withDelay){
        final Animation animation= AnimationUtils.loadAnimation(this,R.anim.shake_anim);
        animation.setStartOffset(50);
        if(withDelay) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    view.startAnimation(animation);
                }
            }, 1000);
        }
        else{
            view.startAnimation(animation);
        }
    }

    public void lessShakeView(final View view, boolean withDelay){
        final Animation animation= AnimationUtils.loadAnimation(this,R.anim.less_shake_anim);
        animation.setStartOffset(50);
        if(withDelay) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    view.startAnimation(animation);
                }
            }, 1000);
        }
        else{
            view.startAnimation(animation);
        }
    }
}

package com.whoisconnected.util;

import android.content.Context;
import android.view.Gravity;
import android.view.ViewGroup;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.facebook.ads.AbstractAdListener;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.facebook.ads.InterstitialAd;
import com.whoisconnected.R;


public class AdManager {
    private InterstitialAd interstitialAd;
    private Context context;
    private boolean isInterstitiaShown=false;
    public AdManager(Context context) {
        this.context=context;
    }

    public void loadInterstitiaAd(){
        interstitialAd=new InterstitialAd(context,context.getResources().getString(R.string.interstitial_ad_id));
        interstitialAd.loadAd();
    }

    int FALLBACK_LOAD_COUNT=0;
    public void showInterStitialAd(){
        interstitialAd.setAdListener(new AbstractAdListener() {
            @Override
            public void onError(Ad ad, AdError error) {
                super.onError(ad, error);
                if(FALLBACK_LOAD_COUNT<3){
                    interstitialAd.loadAd();
                    FALLBACK_LOAD_COUNT++;
                }
            }

            @Override
            public void onAdLoaded(Ad ad) {
                super.onAdLoaded(ad);
                if (interstitialAd.isAdLoaded() && isInterstitiaShown==false){
                    interstitialAd.show();
                    isInterstitiaShown=true;
                }

            }

            @Override
            public void onAdClicked(Ad ad) {
                super.onAdClicked(ad);
            }

            @Override
            public void onInterstitialDisplayed(Ad ad) {
                super.onInterstitialDisplayed(ad);
            }

            @Override
            public void onInterstitialDismissed(Ad ad) {
                super.onInterstitialDismissed(ad);
                isInterstitiaShown=false;
            }
        });
        if (interstitialAd.isAdLoaded() && isInterstitiaShown==false){
            interstitialAd.show();
            isInterstitiaShown=true;
        }
    }

    AdView bannerAdView;
    int BANNER_FALLBACK_COUNT=3;
    public void showBanner(CoordinatorLayout coordinatorLayout){
        bannerAdView=new AdView(context,context.getResources().getString(R.string.banner_id),AdSize.BANNER_HEIGHT_50);
        bannerAdView.loadAd();
        bannerAdView.setAdListener(new AbstractAdListener() {
            @Override
            public void onError(Ad ad, AdError error) {
                super.onError(ad, error);
                if(BANNER_FALLBACK_COUNT>0){
                    bannerAdView.loadAd();
                    BANNER_FALLBACK_COUNT--;
                }
            }
        });
        CoordinatorLayout.LayoutParams params=new CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity= Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL;
        params.insetEdge=Gravity.BOTTOM;
        bannerAdView.setLayoutParams(params);
        coordinatorLayout.addView(bannerAdView);
    }
    public void destroyBannerAds(){
        if(bannerAdView!=null){
            bannerAdView.destroy();
        }
    }

    public void destroyInterstitialAds(){
        if(interstitialAd!=null){
            interstitialAd.destroy();
        }
    }

}

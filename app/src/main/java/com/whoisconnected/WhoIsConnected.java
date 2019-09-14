package com.whoisconnected;

import android.app.Application;
import android.content.Context;

import androidx.room.Room;

import com.facebook.ads.AdSettings;
import com.facebook.ads.AudienceNetworkAds;
import com.whoisconnected.database.DB;
import com.whoisconnected.util.CustomExceptionHandler;

public class WhoIsConnected extends Application {
    private static DB DB;
    @Override
    public void onCreate() {
        super.onCreate();
        if(!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)){
            Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(this));
        }
        if(AudienceNetworkAds.isInAdsProcess(this)){
            return;
        }
        AdSettings.addTestDevice("5a37c734-2349-4af8-a1ae-151b335e3243");
        AudienceNetworkAds.initialize(this);
        DB = Room.databaseBuilder(this, DB.class,"app_db").allowMainThreadQueries().build();
    }

    public static void deleteAppData(Context context){
        try{
            String packageName=context.getPackageName();
            Runtime runtime=Runtime.getRuntime();
            runtime.exec("pm clear "+packageName);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    public static DB getDB() {
        return DB;
    }
}

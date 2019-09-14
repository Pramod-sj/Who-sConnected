package com.whoisconnected.util;

import android.text.format.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class FancyTime {
    public static final String DATE_FORMAT="yyyy-MM-dd HH:mm:ss";
    public static String getTimeLine(String date){
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Long time=null;
        try {
            time=simpleDateFormat.parse(date).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long now=Calendar.getInstance().getTimeInMillis();
        String ago="";
        float diffInHours=((now-time)/(1000*60*60))%24;
        if(diffInHours<1){
            ago= DateUtils.getRelativeTimeSpanString(time,now,DateUtils.MINUTE_IN_MILLIS).toString();
        }
        else if(diffInHours>=1 && diffInHours<24){
            ago= DateUtils.getRelativeTimeSpanString(time,now,DateUtils.HOUR_IN_MILLIS).toString();
        }
        else {
            ago= DateUtils.getRelativeTimeSpanString(time,now,DateUtils.DAY_IN_MILLIS).toString();

        }
        return ago;
    }
}

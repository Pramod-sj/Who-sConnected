package com.whoisconnected.ui;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.Keep;

import com.google.gson.Gson;
import com.whoisconnected.util.AdManager;
import com.whoisconnected.R;
import com.whoisconnected.util.Util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class YourPublicIpActivity extends BaseActivity{
    TextView privateIPAddress,publicIpTextView,countryTextView,countryCodeTextView,cityTextView,regionTextVIew,timezoneTextView,zipCodeTextVIew,ispTextView;
    AdManager adManager;
    FrameLayout ipAddressFrameLayout,macAddressFrameLayout;
    ClipboardManager clipboardManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_your_public_ip);
        setTitle("Your Public IP");
        clipboardManager= (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        adManager=new AdManager(this);
        adManager.loadInterstitiaAd();
        adManager.showBanner(getCoordinatorLayout());
        setHomeButton(R.drawable.ic_arrow_back_black_24dp, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        privateIPAddress=findViewById(R.id.localIPTextView);
        publicIpTextView=findViewById(R.id.publicIPTextView);
        publicIpTextView.setSelected(true);
        cityTextView=findViewById(R.id.cityTextView);
        regionTextVIew=findViewById(R.id.regionTextView);
        timezoneTextView=findViewById(R.id.timeZoneTextView);
        zipCodeTextVIew=findViewById(R.id.zipCodeTextView);
        countryTextView=findViewById(R.id.countryTextView);
        countryCodeTextView=findViewById(R.id.countryCodeTextView);
        ispTextView=findViewById(R.id.ispTextView);
        privateIPAddress.setText(Util.formatIPAddress(getWifiManager().getDhcpInfo().ipAddress));
        GetPublicIP getPublicIP=new GetPublicIP(this,new OnIpFetchListener() {
            @Override
            public void onFetch(final PublicIP p) {
                publicIpTextView.setText(p.getQuery());
                countryTextView.setText(p.getCountry());
                countryCodeTextView.setText(p.getCountryCode());
                zipCodeTextVIew.setText(p.getZip());
                timezoneTextView.setText(p.getTimezone());
                regionTextVIew.setText(p.getRegionName());
                cityTextView.setText(p.getCity());
                ispTextView.setText(p.getIsp());
                hideIndeterminateProgress();
                adManager.showInterStitialAd();
            }

            @Override
            public void onStart() {
                showIndeterminateProgress();
            }

            @Override
            public void onFailure(String message) {
                hideIndeterminateProgress();
            }
        });
        getPublicIP.execute();
        ipAddressFrameLayout=findViewById(R.id.ipAddressFramelayout);
        ipAddressFrameLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ClipData clipData=ClipData.newPlainText("Ip address",privateIPAddress.getText().toString());
                clipboardManager.setPrimaryClip(clipData);
                showSnackbar("Copied!");
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                return false;
            }
        });
        macAddressFrameLayout=findViewById(R.id.macAddressFramelayout);
        macAddressFrameLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ClipData clipData=ClipData.newPlainText("MAC address",publicIpTextView.getText().toString());
                clipboardManager.setPrimaryClip(clipData);
                showSnackbar("Copied!");
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                return false;
            }
        });
    }

    public static class GetPublicIP extends AsyncTask<Void,Void,PublicIP>{

        OnIpFetchListener onIpFetchListener;
        Context context;
        public GetPublicIP(Context context,OnIpFetchListener onIpFetchListener) {
            this.onIpFetchListener = onIpFetchListener;
            this.context=context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ((Activity)context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onIpFetchListener.onStart();
                }
            });
        }

        @Override
        protected PublicIP doInBackground(Void... voids) {
            try {
                URL url = new URL("https://www.trackip.net/ip");
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                if(httpURLConnection.getResponseCode()==200){
                    InputStreamReader inputStreamReader=new InputStreamReader(httpURLConnection.getInputStream());
                    BufferedReader bufferedReader=new BufferedReader(inputStreamReader);
                    String line;
                    String result="";
                    while((line=bufferedReader.readLine())!=null){
                        result+=line+"\n";
                    }
                    URL url2=new URL("http://www.ip-api.com/json/"+result);
                    HttpURLConnection httpURLConnection1= (HttpURLConnection) url2.openConnection();
                    if(httpURLConnection1.getResponseCode()==200){
                        InputStreamReader inputStreamReader1=new InputStreamReader(httpURLConnection1.getInputStream());
                        BufferedReader bufferedReader1=new BufferedReader(inputStreamReader1);
                        String line1;
                        String result1="";
                        while((line1=bufferedReader1.readLine())!=null){
                            result1+=line1+"\n";
                        }
                        return new Gson().fromJson(result1,PublicIP.class);
                    }
                }


            }
            catch (Exception e){
                Log.i("ERROR",e
                .toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(PublicIP publicIP) {
            super.onPostExecute(publicIP);
            if(publicIP==null){
                onIpFetchListener.onFailure("Something went wrong");
            }
            else{
                onIpFetchListener.onFetch(publicIP);
            }
            this.context=null;
        }


    }

    public interface OnIpFetchListener{
        void onStart();
        void onFetch(PublicIP p);
        void onFailure(String message);
    }

    @Keep
    public class PublicIP{
        private String query;
        private String city;
        private String country;
        private String countryCode;
        private String as;
        private String isp;
        private String org;
        private String regionName;
        private String timezone;
        private String zip;
        private float lat;
        private float lang;

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getCountryCode() {
            return countryCode;
        }

        public void setCountryCode(String countryCode) {
            this.countryCode = countryCode;
        }

        public String getAs() {
            return as;
        }

        public void setAs(String as) {
            this.as = as;
        }

        public String getIsp() {
            return isp;
        }

        public void setIsp(String isp) {
            this.isp = isp;
        }

        public String getOrg() {
            return org;
        }

        public void setOrg(String org) {
            this.org = org;
        }

        public String getRegionName() {
            return regionName;
        }

        public void setRegionName(String regionName) {
            this.regionName = regionName;
        }

        public String getTimezone() {
            return timezone;
        }

        public void setTimezone(String timezone) {
            this.timezone = timezone;
        }

        public String getZip() {
            return zip;
        }

        public void setZip(String zip) {
            this.zip = zip;
        }

        public float getLat() {
            return lat;
        }

        public void setLat(float lat) {
            this.lat = lat;
        }

        public float getLang() {
            return lang;
        }

        public void setLang(float lang) {
            this.lang = lang;
        }
    }

    @Override
    protected void onDestroy() {
        adManager.destroyBannerAds();
        super.onDestroy();
    }
}

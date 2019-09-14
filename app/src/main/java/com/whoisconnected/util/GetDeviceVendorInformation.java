package com.whoisconnected.util;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.whoisconnected.database.Model.Device;
import com.whoisconnected.database.Model.Macvender;
import com.whoisconnected.WhoIsConnected;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GetDeviceVendorInformation extends AsyncTask<Void,Void,Macvender> {
    String macAddress;
    OnDataFetchedListener onDataFetchedListener;

    public GetDeviceVendorInformation(String macAddress, OnDataFetchedListener onDataFetchedListener) {
        this.macAddress = macAddress;
        this.onDataFetchedListener = onDataFetchedListener;
    }

    @Override
    protected Macvender doInBackground(Void... voids) {
        Device device= WhoIsConnected.getDB().dbDAO().getDeviceByMac(macAddress);
        if(device.getMacvender()==null){
            String urlString="http://www.macvendors.co/api/"+macAddress+"/json";
            URL url;
            HttpURLConnection httpURLConnection=null;
            try {
                url = new URL(urlString);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                int code=httpURLConnection.getResponseCode();
                StringBuilder result=new StringBuilder();
                if(code==200){
                    InputStream inputStream=new BufferedInputStream(httpURLConnection.getInputStream());
                    if (inputStream!=null){
                        BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(inputStream));
                        String line="";
                        while ((line=bufferedReader.readLine())!=null){
                            result.append(line);
                        }
                        inputStream.close();
                    }
                }
                JSONObject jsonObject=new JSONObject(result.toString());
                Macvender macvender = new Gson().fromJson(jsonObject.getJSONObject("result").toString(), Macvender.class);
                WhoIsConnected.getDB().dbDAO().updateHostName(macvender,macAddress);
                return macvender;
            }
            catch (Exception e){
                Log.i("ERROR",e.toString());
            }
            finally {
                if(httpURLConnection!=null) {
                    httpURLConnection.disconnect();
                }
            }
            return null;
        }
        else{
            return device.getMacvender();
        }
    }

    public interface  OnDataFetchedListener{
        void onFetched(Macvender macvender);
        void onFailure(String errorMessage);
    }

    @Override
    protected void onPostExecute(Macvender macvender) {
        super.onPostExecute(macvender);
        if(onDataFetchedListener!=null && macvender!=null){
            onDataFetchedListener.onFetched(macvender);
        }
    }
}

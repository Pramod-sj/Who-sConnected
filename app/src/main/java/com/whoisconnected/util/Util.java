package com.whoisconnected.util;

import android.net.wifi.WifiManager;

import com.whoisconnected.database.Model.Device;

import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {

    public static boolean isValidIp(String ip) {
        Pattern pattern = Pattern.compile("^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})$");
        Matcher matcher = pattern.matcher(ip);
        return matcher.find();
    }

    public static ArrayList<String> getMacAddrFromArrayList(ArrayList<Device> devices){
        ArrayList<String> macAddr=new ArrayList<>();
        for (Device device:devices){
            macAddr.add(device.getMacAddress());
        }
        return macAddr;
    }

    public void test(){
        try{
            Hashtable env=new Hashtable();
        }
        catch (Exception e){

        }
    }

    public static String getMacAdddress(){
        try{
            List<NetworkInterface> networkInterfaces= Collections.list(NetworkInterface.getNetworkInterfaces());
            for(NetworkInterface networkInterface:networkInterfaces){
                if(!networkInterface.getName().equalsIgnoreCase("wlan0")){
                    continue;
                }
                byte[] macBytes=networkInterface.getHardwareAddress();
                if(macBytes==null){
                    return "02:00:00:00:00:00";
                }
                StringBuilder stringBuilder=new StringBuilder();
                for (byte b:macBytes){
                    stringBuilder.append(Integer.toHexString(b & 0xFF)+":");
                }
                if(stringBuilder.length()>0){
                    stringBuilder.deleteCharAt(stringBuilder.length()-1);
                }
                return stringBuilder.toString();
            }
        }
        catch (Exception e){

        }
        return "02:00:00:00:00:00";
    }

    public static String trimAcessPointName(String name){
        StringBuilder stringBuilder=new StringBuilder();
        stringBuilder.append(name);
        stringBuilder.deleteCharAt(0);
        stringBuilder.deleteCharAt(stringBuilder.length()-1);
        return stringBuilder.toString();
    }
    public static byte[] getByteIpAddress(String ipaddress){
        String[] data=ipaddress.split(Pattern.quote("."));
        byte[] byteData=new byte[data.length];
        for (int i=0;i<data.length;i++) {
            byteData[i]= (byte)Integer.parseInt(data[i]);
        }
        return byteData;
    }

    public static String formatIPAddress(int ipaddress){
        return String.format(Locale.US,"%d.%d.%d.%d",(ipaddress&0xff),(ipaddress>>8&0xff),(ipaddress>>16&0xff),(ipaddress>>24&0xff));
    }

    public static String getGatewayIp(WifiManager wifiManager){
        return Util.formatIPAddress(wifiManager.getDhcpInfo().gateway);
    }
}

package com.whoisconnected.tools;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;

import com.whoisconnected.database.Model.Device;
import com.whoisconnected.database.Model.Scan;
import com.whoisconnected.util.FancyTime;
import com.whoisconnected.util.Util;
import com.whoisconnected.WhoIsConnected;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class HostScanner extends AsyncTask<Void,Integer, Scan> {
    public String WIFI_IP_ADDRESS;
    public static int TIMEOUT=10;
    private static String subnet;
    private static final int LOWER=1;
    private static final int UPPER=255;
    private Context context;
    private ExecutorService executorService;
    private WifiManager wifiManager;
    private long startTimeInMillis;
    private boolean cancel;

    public void setCancel(boolean cancel) {
        this.cancel = cancel;
    }

    public HostScanner(Context context) {
        this.context=context;
        wifiManager= (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        this.WIFI_IP_ADDRESS = Util.formatIPAddress(wifiManager.getConnectionInfo().getIpAddress());
        subnet=getSubnet(WIFI_IP_ADDRESS);
        executorService= Executors.newFixedThreadPool(35);
    }

    public void setOnScanListener(OnScanListener onScanListener) {
        this.onScanListener = onScanListener;
    }

    private String getSubnet(String ipAddress){
        String[] d=ipAddress.split(Pattern.quote("."));
        return d[0]+"."+d[1]+"."+d[2]+".";
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        startTimeInMillis=Calendar.getInstance().getTimeInMillis();
        ((Activity)context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onScanListener.onScanStart();
            }
        });
    }

    @Override
    protected Scan doInBackground(Void... voids) {
        Scan scan=null;
        try {
            Map<Integer,String> deviceMac=new HashMap<>();
            List<Future<Ping.PingResult>> futures=new ArrayList<>();
            for (int i = LOWER; i < UPPER; i++) {
                String host = subnet + i;
                Future<Ping.PingResult> pingResultFuture=isHostReachable(executorService,host,1);
                futures.add(pingResultFuture);
            }
            executorService.awaitTermination(200L, TimeUnit.MILLISECONDS);
            for (int i=0;i<futures.size();i++){
                Future<Ping.PingResult> pingResultFuture =futures.get(i);
                if(pingResultFuture.get().isReachable()){
                    final Device device=new Device();
                    device.setIpAddress(pingResultFuture.get().getHost());
                    device.setLastIPNumber(i);
                    InetAddress inetAddress=InetAddress.getByName(device.getIpAddress());
                    device.setHostName(Util.isValidIp(inetAddress.getCanonicalHostName())?"Unknown":inetAddress.getCanonicalHostName());
                    device.setMacAddress(getMacAddress(pingResultFuture.get().getHost()));
                    if(Util.getGatewayIp(wifiManager).equals(device.getIpAddress())){
                        device.setHostName("Wi-Fi");
                    }
                    if(WIFI_IP_ADDRESS.equals(device.getIpAddress())){
                        device.setMacAddress(Util.getMacAdddress());
                    }
                    //checking whether data already exist in device table if not fetch data using api
                    Device deviceExist= WhoIsConnected.getDB().dbDAO().getDeviceByMac(device.getMacAddress());
                    if (deviceExist==null) {
                        //inserting devices into database
                        WhoIsConnected.getDB().dbDAO().insertDevice(device);
                    }
                    else{
                        WhoIsConnected.getDB().dbDAO().updateIpAddress(device.getIpAddress(),device.getMacAddress());
                        device.setMacvender(deviceExist.getMacvender());
                    }
                    if(onScanListener!=null){
                        ((Activity)context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                onScanListener.onDeviceFound(device);
                            }
                        });
                    }
                    deviceMac.put(i,device.getMacAddress());
                }
                publishProgress(i);
            }
            //setting scan data fields
            scan=new Scan();
            long timeTaken=Calendar.getInstance().getTimeInMillis()-startTimeInMillis;
            scan.setTimeTakenInMins(Float.parseFloat(String.valueOf(timeTaken/1000D)));
            scan.setFoundMacAddress(new ArrayList<String>(deviceMac.values()));
            scan.setDateTime(new SimpleDateFormat(FancyTime.DATE_FORMAT).format(Calendar.getInstance().getTime()));
            WhoIsConnected.getDB().scanDAO().insertScan(scan);
        }catch (final Exception e){
            ((Activity)context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onScanListener.onFailure(e.getMessage());
                }
            });
        }
        return scan;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        int percentageCompeleted= (values[0]*100) /UPPER;
        onScanListener.onScanProgress(percentageCompeleted);
    }

    @Override
    protected void onPostExecute(Scan scan) {
        super.onPostExecute(scan);
        if(onScanListener!=null && scan!=null){
            onScanListener.onComplete(scan);
        }
        onScanListener.onFinished();
        this.context=null;
    }

    OnScanListener onScanListener;

    public interface OnScanListener{
        void onFinished();
        void onComplete(Scan scan);
        void onDeviceFound(Device device);
        void onScanProgress(int progress);
        void onScanStart();
        void onFailure(String errorMessage);
    }

    private String getMacAddress(String ipaddress){
        BufferedReader bufferedReader=null;
        try{
            bufferedReader=new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            StringBuilder stringBuilder=new StringBuilder();
            while ((line=bufferedReader.readLine())!=null){
                String[] splitted=line.split(" +");
                if(splitted!=null && splitted.length>=4){
                    String ip=splitted[0];
                    String mac=splitted[3];
                    if(mac.matches("..:..:..:..:..:..")){
                        if(ip.equals(ipaddress)){
                            return mac;
                        }
                    }
                }
            }
        }
        catch (Exception e){

        }
        return "00:00:00:00:00:00";
    }




    public Future<Ping.PingResult> isHostReachable(ExecutorService executorService, final String host, final int timeout){
        return executorService.submit(new Callable<Ping.PingResult>() {
            @Override
            public Ping.PingResult call() throws Exception {
                if(cancel){
                    Ping.PingResult pingResult=new Ping.PingResult();
                    pingResult.setHost(host);
                    return pingResult;
                }
                Ping ping=new Ping();
                Ping.PingOptions pingOptions=new Ping.PingOptions();
                return ping.doPing(host,pingOptions);
            }
        });
    }

}

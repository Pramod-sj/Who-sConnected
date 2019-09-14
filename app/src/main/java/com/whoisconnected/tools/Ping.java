package com.whoisconnected.tools;


import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;

public class Ping {

    public synchronized PingResult doPing(String host,PingOptions pingOptions){
        PingResult pingResult=new PingResult();
        pingResult.setHost(host);
        Process process=null;
        try {
            String cmd="ping -c 1 -W "+pingOptions.getTimeoutSecs()+" -t "+Math.max(pingOptions.getTimeToLIve(),1)+" "+host;
           process = Runtime.getRuntime().exec(cmd);
            process.waitFor();
            int exit=process.exitValue();
            switch (exit){
                case 0:
                    String result="";
                    InputStreamReader inputStreamReader=new InputStreamReader(process.getInputStream());
                    BufferedReader bufferedReader=new BufferedReader(inputStreamReader);
                    String line;
                    while((line=bufferedReader.readLine())!=null){
                        result+=line+"\n";
                    }
                    inputStreamReader.close();
                    bufferedReader.close();
                    process.destroy();
                    pingResult.setReachable(true);
                    pingResult.setFullOutputString(result);
                    pingResult.setPingError(null);
                    break;
                case 1:
                    pingResult.setPingError("Failed: 1");
                    pingResult.setFullOutputString(host+" not reachable");
                    pingResult.setReachable(false);
                    process.destroy();
                    break;
                default:
                    pingResult.setPingError("Error: "+exit);
                    pingResult.setFullOutputString("No internet or something is seriously very wrong");
                    pingResult.setReachable(false);
                    process.destroy();
                    break;
            }
        }
        catch (Exception e){
            Log.i("ERROR",e.toString());
        }
        return pingResult;
    }

    public void pingAsync(final String host, final PingResultListener pingResultListener){
        new Thread(){
            @Override
            public void run() {
                super.run();
                pingResultListener.onResult(doPing(host,new PingOptions()));
            }
        }.start();
    }

    public interface PingResultListener{
        void onResult(PingResult pingResult);
    }

    public static class AsyncPing extends AsyncTask<Void,Integer,PingStats>{
        String host;
        PingOptions pingOptions;
        PingListener pingListener;
        Context context;
        boolean cancel=false;
        public AsyncPing(Context context,String host,PingOptions pingOptions,PingListener p) {
            this.host = host;
            this.pingOptions = pingOptions;
            this.pingListener=p;
            this.context=context;
        }

        public void setCancel(boolean cancel) {
            this.cancel = cancel;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ((Activity)context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pingListener.onScanStart();
                }
            });
        }

        @Override
        protected PingStats doInBackground(Void... voids) {
            Ping ping=new Ping();
            ArrayList<PingStats> pingStatses=new ArrayList<>();
            for (int i=1;i<=pingOptions.getCount();i++){
                if(cancel){
                    break;
                }
                PingResult pingResult=ping.doPing(host,pingOptions);
                if(pingResult.getPingError()==null) {
                    pingStatses.add(getPingStats(pingResult.getFullOutputString()));
                    final PingSequence pingSequence=formatPingOutput(pingResult.getFullOutputString());
                    pingSequence.setIcmp_seq(i);
                    ((Activity)context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pingListener.onResult(pingSequence);
                        }
                    });
                }
                else{
                    pingResult.setPingSequence(new PingSequence());
                }
                publishProgress(i);
            }

            return normalizeStats(pingOptions.getCount(),pingStatses);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
             final int percentage=(values[0]*100)/pingOptions.getCount();
            ((Activity)context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pingListener.onScanProgress(percentage);
                }
            });
        }

        private synchronized PingStats normalizeStats(int totalPingRequest, ArrayList<PingStats> pingStatses){
            PingStats pingStats=new PingStats();
            if(pingStatses.size()>0) {
                float avg = 0;
                ArrayList<Float> max = new ArrayList<>();
                ArrayList<Float> min = new ArrayList<>();
                long totalTimeTaken = 0;
                long mean = 0, smean = 0;
                for (PingStats pingStats1 : pingStatses) {
                    avg += pingStats1.getAvg();
                    max.add(pingStats1.getMax());
                    min.add(pingStats1.getMin());
                    totalTimeTaken += pingStats1.getTotalTime();
                    mean += pingStats1.getAvg();
                    smean += pingStats1.getAvg() * pingStats1.getAvg();
                }
                mean = mean / pingStatses.size();
                smean = smean / pingStatses.size();
                pingStats.setAvg(avg / pingStatses.size());
                pingStats.setMax(Collections.max(max));
                pingStats.setMin(Collections.min(min));
                pingStats.setPacketsTransmitted(totalPingRequest);
                pingStats.setPacketsLoss(100 - (pingStatses.size() * 100) / totalPingRequest);
                pingStats.setPacketsReceived(pingStatses.size());
                pingStats.setTotalTime(totalTimeTaken);
                pingStats.setMdev((float) Math.sqrt(smean - (mean * mean)));
            }
            return pingStats;
        }

        @Override
        protected void onPostExecute(PingStats pingStats) {
            super.onPostExecute(pingStats);
            pingListener.onComplete(pingStats);
            this.context=null;
        }
    }

    public interface PingListener{
        void onScanStart();
        void onResult(PingSequence pingSequence);
        void onComplete(PingStats pingStats);
        void onScanProgress(int i);
    }


    public static PingStats getPingStats(String processOutput){
        if(processOutput==null || processOutput.equals("")){
            return null;
        }
        PingStats pingStats=new PingStats();
        int start=processOutput.indexOf("/mdev = ");
        int end=processOutput.indexOf(" ms\n",start);
        String[] times=processOutput.substring(start+8,end).split("/");
        pingStats.setMin(Float.parseFloat(times[0]));
        pingStats.setAvg(Float.parseFloat(times[1]));
        pingStats.setMax(Float.parseFloat(times[2]));
        pingStats.setMdev(Float.parseFloat(times[3]));

        String[] lines=processOutput.split("\n");
        String stats1stLine="";
        for (String line:lines){
            if(line.contains("%")){
                stats1stLine=line;
            }
        }
        int endPacketPRansIndex=stats1stLine.indexOf(" packets transmitted");
        String transmittedPacket=stats1stLine.substring(0,endPacketPRansIndex);

        int startReceivedIndex=stats1stLine.indexOf("packets transmitted, ");
        int endReceivedIndex=stats1stLine.indexOf(" received");
        String packetReceived=stats1stLine.substring(startReceivedIndex+("packets transmitted, ".length()),endReceivedIndex);

        int startLossIndex=stats1stLine.indexOf("received, ");
        int endLossdIndex=stats1stLine.indexOf("% packet");
        String loss=stats1stLine.substring(startLossIndex+("received, ".length()),endLossdIndex);

        int startTimeIndex=stats1stLine.indexOf("time ");
        int endTimeIndex=stats1stLine.indexOf("ms");
        String time=stats1stLine.substring(startTimeIndex+("time ".length()),endTimeIndex);

        pingStats.setPacketsLoss(Long.parseLong(loss));
        pingStats.setPacketsReceived(Long.parseLong(packetReceived));
        pingStats.setPacketsTransmitted(Long.parseLong(transmittedPacket));
        pingStats.setTotalTime(Long.parseLong(time));
        return pingStats;
    }

    public static PingSequence formatPingOutput(String processOutput){
        String[] lines=processOutput.split("\n");
        String reqFromLine="";
        for (String line:lines){
            if(line.contains("time=")){
                reqFromLine+=line;
                break;
            }
        }
        PingSequence pingSequence=new PingSequence();
        int startFromHostIndex=reqFromLine.indexOf("from ");
        int endFromHostIndex=reqFromLine.indexOf(": icmp_seq");
        String fromHost=reqFromLine.substring(startFromHostIndex+("from ".length()),endFromHostIndex);
        int startIcmpSeqIndex=reqFromLine.indexOf("icmp_seq=");
        int endIcmpSeqIndex=reqFromLine.indexOf(" ttl=");
        String icmp_seq=reqFromLine.substring(startIcmpSeqIndex+("icmp_seq=".length()),endIcmpSeqIndex);
        int startTTLIndex=reqFromLine.indexOf("ttl=");
        int endTTLIndex=reqFromLine.indexOf(" time=");
        String ttl=reqFromLine.substring(startTTLIndex+("ttl=".length()),endTTLIndex);
        int timeIndex=reqFromLine.indexOf("time=");
        int endtimeIndex=reqFromLine.indexOf(" ms");
        String time=reqFromLine.substring(timeIndex+"time=".length(),endtimeIndex);
        pingSequence.setIcmp_seq(Long.parseLong(icmp_seq));
        pingSequence.setTime(Float.parseFloat(time));
        pingSequence.setTtl(Long.parseLong(ttl));
        pingSequence.setFromHost(fromHost);
        Log.i("FROM",pingSequence.getFromHost());
        return pingSequence;
    }

    public static class PingStats{
        private float min=0f;
        private float avg=0f;
        private float max=0f;
        private float mdev=0f;
        private long packetsTransmitted=0;
        private long packetsReceived=0;
        private long packetsLoss=0;
        private long totalTime=0;

        public long getPacketsTransmitted() {
            return packetsTransmitted;
        }

        public void setPacketsTransmitted(long packetsTransmitted) {
            this.packetsTransmitted = packetsTransmitted;
        }

        public long getPacketsReceived() {
            return packetsReceived;
        }

        public void setPacketsReceived(long packetsReceived) {
            this.packetsReceived = packetsReceived;
        }

        public long getPacketsLoss() {
            return packetsLoss;
        }

        public void setPacketsLoss(long packetsLoss) {
            this.packetsLoss = packetsLoss;
        }

        public long getTotalTime() {
            return totalTime;
        }

        public void setTotalTime(long totalTime) {
            this.totalTime = totalTime;
        }

        public float getMin() {
            return min;
        }

        public void setMin(float min) {
            this.min = min;
        }

        public float getAvg() {
            return avg;
        }

        public void setAvg(float avg) {
            this.avg = avg;
        }

        public float getMax() {
            return max;
        }

        public void setMax(float max) {
            this.max = max;
        }

        public float getMdev() {
            return mdev;
        }

        public void setMdev(float mdev) {
            this.mdev = mdev;
        }
    }

    public static class PingResult{
        private String host;
        private boolean isReachable=false;
        private String pingError;
        private String fullOutputString;
        private PingSequence pingSequence;

        public PingSequence getPingSequence() {
            return pingSequence;
        }

        public void setPingSequence(PingSequence pingSequence) {
            this.pingSequence = pingSequence;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public boolean isReachable() {
            return isReachable;
        }

        public void setReachable(boolean reachable) {
            isReachable = reachable;
        }

        public String getPingError() {
            return pingError;
        }

        public void setPingError(String pingError) {
            this.pingError = pingError;
        }

        public String getFullOutputString() {
            return fullOutputString;
        }

        public void setFullOutputString(String fullOutputString) {
            this.fullOutputString = fullOutputString;
        }
    }


    public static class PingOptions{
        private int timeoutSecs;
        private int timeToLIve;
        private int count;

        public PingOptions() {
            count=1;
            timeToLIve=128;
            timeoutSecs=1;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public int getTimeoutSecs() {
            return timeoutSecs;
        }

        public void setTimeoutSecs(int timeoutSecs) {
            this.timeoutSecs = timeoutSecs;
        }

        public int getTimeToLIve() {
            return timeToLIve;
        }

        public void setTimeToLIve(int timeToLIve) {
            this.timeToLIve = timeToLIve;
        }
    }

    public static class PingSequence{
        String fromHost;
        long icmp_seq;
        long ttl;
        float time;

        public String getFromHost() {
            return fromHost;
        }

        public void setFromHost(String fromHost) {
            this.fromHost = fromHost;
        }

        public long getIcmp_seq() {
            return icmp_seq;
        }

        public void setIcmp_seq(long icmp_seq) {
            this.icmp_seq = icmp_seq;
        }

        public long getTtl() {
            return ttl;
        }

        public void setTtl(long ttl) {
            this.ttl = ttl;
        }

        public float getTime() {
            return time;
        }

        public void setTime(float time) {
            this.time = time;
        }
    }
}

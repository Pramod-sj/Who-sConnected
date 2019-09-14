package com.whoisconnected.tools;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class PortScanner extends AsyncTask<Void,Integer,ArrayList<Integer>>{
    private String HOST_NAME;
    private int START_PORT;
    private int END_PORT;
    private Context context;
    private boolean cancelScanning=false;
    public PortScanner(Context context,String HOST_NAME, int START_PORT, int END_PORT, PortListener portListener) {
        this.context=context;
        this.HOST_NAME = HOST_NAME;
        this.START_PORT = START_PORT;
        this.END_PORT = END_PORT;
        this.portListener = portListener;
    }

    public void cancelScan(){
        cancelScanning=true;
    }

    private static synchronized boolean doTCPPortScan(String host, int port, int timeOutMillis){
        Socket socket=null;
        try{
           socket=new Socket();
           SocketAddress socketAddress=new InetSocketAddress(host,port);
           socket.connect(socketAddress,timeOutMillis);
           socket.close();
           return true;
       }
       catch (Exception e){ }
       finally {
           if(socket!=null){
               try {
                   socket.close();
               } catch (IOException e) {
                   e.printStackTrace();
               }
           }
       }
       return false;
    }



    PortListener portListener;

    @Override
    protected ArrayList<Integer> doInBackground(Void... voids) {
        ArrayList<Integer> foundOpenPort=null;
        try{
            ExecutorService executorService= Executors.newFixedThreadPool(30);
            foundOpenPort=new ArrayList<>();
            for (int i=START_PORT;i<=END_PORT;i++){
                Future<PortStatus> futurePortStatus=getPortStatus(executorService,HOST_NAME,i,100);
                final PortStatus portStatus=futurePortStatus.get();
                ((Activity)context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        portListener.onResult(portStatus);
                    }
                });
                if(portStatus.isOpen()){
                    foundOpenPort.add(portStatus.getPortNo());
                }
                publishProgress(i);
            }
            executorService.shutdown();
            executorService.awaitTermination(1, TimeUnit.HOURS);
        }
        catch (Exception e){
            Log.i("ERROR",e.toString());
        }
        return foundOpenPort;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        if(portListener!=null){
            portListener.onScanProgress((values[0]*100)/END_PORT);
        }
    }

    @Override
    protected void onPostExecute(ArrayList<Integer> v) {
        super.onPostExecute(v);
        if(portListener!=null && v!=null){
            portListener.onFinished(v);
        }
        context=null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        ((Activity)context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                portListener.onScanStart();
            }
        });
    }

    public interface PortListener{
        void onScanStart();
        void onResult(PortStatus portStatus);
        void onScanProgress(int i);
        void onFinished(ArrayList<Integer> openPorts);
    }



    public Future<PortStatus> getPortStatus(ExecutorService executorService, final String host, final int portNo, final int timeoutInMillis){
        return executorService.submit(new Callable<PortStatus>() {
            @Override
            public PortStatus call() throws Exception {
                PortStatus p=new PortStatus();
                p.setOpen(false);
                p.setPortNo(portNo);
                if(cancelScanning){
                    return p;
                }
                Boolean isOPenPort=PortScanner.doTCPPortScan(host,portNo,timeoutInMillis);
                p.setOpen(isOPenPort);
                return p;
            }
        });
    }
    public final static Map<Integer,String> REGISTERED_PORT=new HashMap(){{
        put(1,"TCP Port Service Multiplexer (TCPMUX)");
        put(5,"Remote Job Entry (RJE)");
        put(7,"ECHO");
        put(18,"Message Send Protocol (MSP)");
        put(20,"FTP -- Data");
        put(21,"FTP -- Control");
        put(22,"SSH Remote Login Protocol");
        put(23,"Telnet");
        put(25,"Simple Mail Transfer Protocol (SMTP)");
        put(29,"MSG ICP");
        put(37,"Time");
        put(42,"Host Name Server (Nameserv)");
        put(43,"WhoIs");
        put(49,"Login Host Protocol (Login)");
        put(53,"Domain Name System (DNS)");
        put(69,"Trivial File Transfer Protocol (TFTP)");
        put(70,"Gopher Services");
        put(79,"Finger");
        put(80,"HTTP");
        put(103,"X.400 Standard");
        put(108,"SNA Gateway Access Server");
        put(109,"POP2");
        put(110,"POP3");
        put(115,"Simple File Transfer Protocol (SFTP)");
        put(118,"SQL Services");
        put(119,"Newsgroup (NNTP)");
        put(137,"NetBIOS Name Service");
        put(139,"NetBIOS Datagram Service");
        put(143,"Interim Mail Access Protocol (IMAP)");
        put(150,"NetBIOS Session Service");
        put(156	,"SQL Server");
        put(161	,"SNMP");
        put(179	,"Border Gateway Protocol (BGP)");
        put(190	,"Gateway Access Control Protocol (GACP)");
        put(194	,"Internet Relay Chat (IRC)");
        put(197	,"Directory Location Service (DLS)");
        put(389	,"Lightweight Directory Access Protocol (LDAP)");
        put(396	,"Novell Netware over IP");
        put(443	,"HTTPS");
        put(444	,"Simple Network Paging Protocol (SNPP)");
        put(445	,"Microsoft-DS");
        put(458	,"Apple QuickTime");
        put(546	,"DHCP Client");
        put(547	,"DHCP Server");
        put(563	,"SNEWS");
        put(569	,"MSN");
        put(1080,"Socks");

    }};
}

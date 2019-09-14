package com.whoisconnected.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.text.format.Formatter;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.OnSuccessListener;
import com.google.android.play.core.tasks.Task;
import com.whoisconnected.database.Model.Device;
import com.whoisconnected.database.Model.Macvender;
import com.whoisconnected.database.Model.Scan;
import com.whoisconnected.util.AdManager;
import com.whoisconnected.util.FancyTime;
import com.whoisconnected.util.GetDeviceVendorInformation;
import com.whoisconnected.tools.HostScanner;
import com.whoisconnected.util.PlaceHolder;
import com.whoisconnected.R;
import com.whoisconnected.util.Util;
import com.whoisconnected.util.WifiStateBroadcastReceiver;
import com.whoisconnected.WhoIsConnected;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.view.View.GONE;

public class MainActivity extends BaseActivity {
    RecyclerView foundDeviceRecyclerView;
    FoundDeviceAdapter foundDeviceAdapter;
    PlaceHolder placeHolder;
    HostScanner hostScanner;
    FloatingActionButton startScan,cancelScan;
    AdManager adManager;
    Scan scan;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        adManager=new AdManager(this);
        adManager.loadInterstitiaAd();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(getString(R.string.app_name));
        isLocationPermissionGranted();
        adManager.showBanner(getCoordinatorLayout());
        startScan=createFab(R.drawable.ic_refresh_black_24dp,R.color.colorAccent);
        cancelScan=createFab(R.drawable.ic_close_black_24dp,android.R.color.holo_red_light);
        placeHolder=findViewById(R.id.placeHolder);
        foundDeviceRecyclerView=findViewById(R.id.foundDeviceRecyclerView);
        foundDeviceAdapter=new FoundDeviceAdapter();
        foundDeviceRecyclerView.setAdapter(foundDeviceAdapter);
        startScan.show();
        startScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if(!isConnectedOverWifi()){
                    showSnackbarWithAction("Click 'Setting' to enable your wifi", "Setting", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                        }
                    });
                    return;
                }
                if(foundDeviceAdapter!=null){
                    foundDeviceAdapter.clearDataSet();
                }
                hostScanner =new HostScanner(MainActivity.this);
                hostScanner.setOnScanListener(new HostScanner.OnScanListener() {
                    @Override
                    public void onDeviceFound(final Device device) {
                        if(foundDeviceAdapter.getItemCount()==0){
                            foundDeviceRecyclerView.setVisibility(View.VISIBLE);
                            placeHolder.hidePlaceHolder();
                        }
                        foundDeviceAdapter.addDevice(device);
                        GetDeviceVendorInformation getDeviceVendorInformation=new GetDeviceVendorInformation(device.getMacAddress(), new GetDeviceVendorInformation.OnDataFetchedListener() {
                            @Override
                            public void onFetched(Macvender macvender) {
                                device.setMacvender(macvender);
                                foundDeviceAdapter.updateDevice(device);
                            }

                            @Override
                            public void onFailure(String errorMessage) {

                            }
                        });
                        connectedCountTextView.setText(foundDeviceAdapter.getItemCount()+" devices up");
                        getDeviceVendorInformation.execute();
                    }

                    @Override
                    public void onScanStart() {
                        //showSnackbar("Scanning please wait");
                        showProgress(0);
                        connectedCountTextView.setText("0 devices up");
                        progressTextView.setVisibility(View.VISIBLE);
                        startScan.hide();
                        cancelScan.show();
                    }

                    @Override
                    public void onComplete(final Scan scan) {
                        MainActivity.this.scan=scan;
                        connectedCountTextView.setText(scan.getFoundMacAddress().size()+" devices up");
                        hideProgress();
                        lastScanTextView.setText(FancyTime.getTimeLine(scan.getDateTime())+" in "+String.format("%.2f",scan.getTimeTakenInMins())+" secs");
                        v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        progressTextView.setVisibility(GONE);
                        showSnackbar(errorMessage);
                        hideProgress();
                    }

                    @Override
                    public void onScanProgress(int progress) {
                        updateProgress(progress);
                        progressTextView.setText(progress+"%");
                    }

                    @Override
                    public void onFinished() {
                        progressTextView.setVisibility(GONE);
                        updateProgress(0);
                        startScan.show();
                        cancelScan.hide();
                        showSnackbar("Scan successfully finished!");
                        adManager.showInterStitialAd();
                    }
                });
                hostScanner.execute();
            }
        });
        cancelScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(hostScanner!=null){
                    hostScanner.setCancel(true);
                }
            }
        });
        embedWifiInterfaceInfoLayout();
        embedScanInfoTextView();
        new Thread(){
            @Override
            public void run() {
                super.run();
                MainActivity.this.scan= WhoIsConnected.getDB().scanDAO().getLastScan();
                if(scan!=null) {
                    final ExecutorService executorService= Executors.newFixedThreadPool(3);
                    final ArrayList<Device> devices=new ArrayList(WhoIsConnected.getDB().dbDAO().getDevicesByMacIds(scan.getFoundMacAddress()));
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            connectedCountTextView.setText(scan.getFoundMacAddress().size() + " device up");
                            lastScanTextView.setText(FancyTime.getTimeLine(scan.getDateTime()) + " in " + String.format("%.2f",scan.getTimeTakenInMins()) + " secs");
                            foundDeviceAdapter.setDevices(devices);
                            for(final Device device:devices) {
                                if(device.getMacvender()==null) {
                                    GetDeviceVendorInformation getDeviceVendorInformation = new GetDeviceVendorInformation(device.getMacAddress(), new GetDeviceVendorInformation.OnDataFetchedListener() {
                                        @Override
                                        public void onFetched(Macvender macvender) {
                                            device.setMacvender(macvender);
                                            foundDeviceAdapter.updateDevice(device);
                                        }

                                        @Override
                                        public void onFailure(String errorMessage) {

                                        }
                                    });
                                    getDeviceVendorInformation.executeOnExecutor(executorService);
                                }
                            }
                        }
                    });
                }
            }
        }.start();
        initBottomMenu();
        setOptionMenuButton(R.drawable.ic_more_vert_black_24dp, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomMenu();
            }
        });
        setIntentFilterForWifiState();
    }
    WifiStateBroadcastReceiver wifiStateBroadcastReceiver;
    private void setIntentFilterForWifiState(){
        wifiStateBroadcastReceiver=new WifiStateBroadcastReceiver(new WifiStateBroadcastReceiver.OnWifiStateChangeListener() {
            @Override
            public void onChanged(boolean isConnected) {
                showNotConnectedPlaceholder(isConnected);
                if(isConnected) {
                    adManager.loadInterstitiaAd();
                    adManager.showBanner(getCoordinatorLayout());
                    checkForUpdate();
                    shakeView(startScan,true);
                }
            }
        });
        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(wifiStateBroadcastReceiver,intentFilter);
    }



    private void showNotConnectedPlaceholder(boolean isConnected){
        if(isConnected){
            foundDeviceRecyclerView.setVisibility(View.VISIBLE);
            placeHolder.hidePlaceHolder();
            showNoScanHistory();
        }
        else{
            foundDeviceRecyclerView.setVisibility(View.GONE);
            placeHolder.showPlaceHolder(R.drawable.shocked,"Wifi is not connected","Before scanning this device must have to be connected to a wifi");
        }
    }

    private void showNoScanHistory(){
        new Thread(){
            @Override
            public void run() {
                super.run();
                final Scan s=WhoIsConnected.getDB().scanDAO().getLastScan();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(s==null){
                            foundDeviceRecyclerView.setVisibility(View.GONE);
                            placeHolder.showPlaceHolder(R.drawable.happy,"No scan history","Click the bottom right button to find who's connected to your wifi");
                        }
                        else{
                            placeHolder.hidePlaceHolder();
                            foundDeviceRecyclerView.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        }.start();
    }

    TextView lastScanTextView,connectedCountTextView,progressTextView;
    private void embedScanInfoTextView(){
        View view= LayoutInflater.from(this).inflate(R.layout.scan_info_layout,getAppBarLayout(),false);
        progressTextView=view.findViewById(R.id.progressTextView);
        lastScanTextView=view.findViewById(R.id.lastScanTextView);
        connectedCountTextView=view.findViewById(R.id.connectedCountTextView);
        view.setLayoutParams(new CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        getAppBarLayout().addView(view,2);
    }

    TextView wifiNameIPAddressTextView;
    private void embedWifiInterfaceInfoLayout(){
        View view= LayoutInflater.from(this).inflate(R.layout.wifi_interface_info_layout,getAppBarLayout(),false);
        wifiNameIPAddressTextView=view.findViewById(R.id.wifiNameIPAddressTextView);
        wifiNameIPAddressTextView.setText(Util.trimAcessPointName(getWifiManager().getConnectionInfo().getSSID())+" ("+ Formatter.formatIpAddress(getWifiManager().getDhcpInfo().gateway)+")");
        wifiNameIPAddressTextView.setSelected(true);
        getAppBarLayout().addView(view,1);
    }



    @Override
    protected void onResume() {
        super.onResume();
        checkForUpdate();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (scan != null) {
                    foundDeviceAdapter.setDevices(new ArrayList<Device>(WhoIsConnected.getDB().dbDAO().getDevicesByMacIds(scan.getFoundMacAddress())));
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    private boolean isLocationPermissionGranted() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},0);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 0:
                if(grantResults[0]!=PackageManager.PERMISSION_GRANTED){
                    if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,Manifest.permission.ACCESS_FINE_LOCATION)) {
                        showSnackbarWithAction("We need location service permission to fetch your router name, this is the required for device having version 8.0+", "Ask again", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                isLocationPermissionGranted();
                            }
                        });
                    }
                    else{
                        showSnackbarWithAction("Wifi name won't be shown until you manually provide access to location service",Snackbar.LENGTH_LONG, "Setting", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent=new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri=Uri.parse("package:"+getPackageName());
                                        intent.setData(uri);
                                        startActivity(intent);
                                    }
                                }
                        );
                    }
                }
                else if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    wifiNameIPAddressTextView.setText(Util.trimAcessPointName(getWifiManager().getConnectionInfo().getSSID())+" ("+Util.formatIPAddress(getWifiManager().getDhcpInfo().gateway)+")");
                }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public FloatingActionButton createFab(int drawableId,int colorId){
        FloatingActionButton floatingActionButton=new FloatingActionButton(this);
        floatingActionButton.setImageResource(drawableId);
        floatingActionButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(colorId)));
        CoordinatorLayout.LayoutParams layoutParams=new CoordinatorLayout.LayoutParams(CoordinatorLayout.LayoutParams.WRAP_CONTENT, CoordinatorLayout.LayoutParams.WRAP_CONTENT);
        int margin= (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,16,getResources().getDisplayMetrics());
        layoutParams.setMargins(margin,margin,margin,margin);
        layoutParams.gravity=Gravity.BOTTOM|Gravity.RIGHT;
        floatingActionButton.setLayoutParams(layoutParams);
        getCoordinatorLayout().addView(floatingActionButton,layoutParams);
        floatingActionButton.setVisibility(GONE);
        return floatingActionButton;
    }

    BottomSheetDialog bottomMenuOption;
    public void initBottomMenu(){
        NavigationView navigationView=new NavigationView(this);
        navigationView.inflateMenu(R.menu.option_menu);
        navigationView.setBackgroundResource(R.color.colorPrimaryDark);
        navigationView.setItemBackgroundResource(R.color.colorPrimaryDark);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.menu_ping:
                        startActivity(new Intent(MainActivity.this,PingActivity.class));
                        break;
                    case R.id.menu_public_ip:
                        startActivity(new Intent(MainActivity.this,YourPublicIpActivity.class));
                        break;
                    case R.id.menu_port_scan:
                        startActivity(new Intent(MainActivity.this,PortScannerActivity.class));
                        break;
                    case R.id.menu_about:
                        startActivity(new Intent(MainActivity.this,AboutActivity.class));
                        break;
                    case R.id.menu_rate:
                        Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id="+getPackageName()));
                        startActivity(intent);
                        break;
                    case R.id.menu_share:
                        shareApp();
                        break;
                }
                return false;
            }
        });
        bottomMenuOption=new BottomSheetDialog(this);
        bottomMenuOption.setContentView(navigationView);
    }
    public void showBottomMenu(){
        if(bottomMenuOption!=null){
            bottomMenuOption.show();
        }
    }

    public void shareApp(){
        Intent intent=new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT,getResources().getString(R.string.app_name));
        intent.putExtra(Intent.EXTRA_TEXT,getResources().getString(R.string.share_desc_string)+"\n"+getResources().getString(R.string.google_play_store_app_url));
        startActivity(Intent.createChooser(intent,"Choose one of the app to share"));
    }


    @Override
    protected void onDestroy() {
        adManager.destroyInterstitialAds();
        adManager.destroyBannerAds();
        if(wifiStateBroadcastReceiver!=null){
            unregisterReceiver(wifiStateBroadcastReceiver);
        }
        super.onDestroy();
    }

    public class FoundDeviceAdapter extends RecyclerView.Adapter<FoundDeviceAdapter.ViewHolder> {
        ArrayList<Device> devices=new ArrayList<>();


        public void setDevices(ArrayList<Device> devices) {
            this.devices = devices;
            notifyDataSetChanged();
        }

        public void clearDataSet(){
            if(devices!=null){
                int totalCount=devices.size();
                devices.clear();
                notifyItemRangeRemoved(0,totalCount);
            }
        }


        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.found_device_layout,parent,false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.ipAddressTextView.setText(devices.get(position).getIpAddress());
            holder.macAddress.setText(devices.get(position).getMacAddress());
            holder.vendorNameTextView.setText(devices.get(position).getMacvender()!=null && devices.get(position).getMacvender().getCompany()!=null?devices.get(position).getMacvender().getCompany():"--");
            holder.hostNameTextView.setText(devices.get(position).getHostName());
            if(Util.formatIPAddress(getWifiManager().getDhcpInfo().ipAddress).equals(devices.get(position).getIpAddress())){
                holder.yourDevice.setVisibility(View.VISIBLE);
            }
            else{
                holder.yourDevice.setVisibility(GONE);
            }
        }

        @Override
        public int getItemCount() {
            return devices.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView ipAddressTextView,macAddress,vendorNameTextView,hostNameTextView,yourDevice;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                yourDevice=itemView.findViewById(R.id.yourDevice);
                hostNameTextView=itemView.findViewById(R.id.hostNameTextView);
                vendorNameTextView=itemView.findViewById(R.id.vendorNameTextView);
                vendorNameTextView.setSelected(true);
                macAddress=itemView.findViewById(R.id.macAddressTextView);
                ipAddressTextView=itemView.findViewById(R.id.ipAddressTextView);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle bundle=new Bundle();
                        bundle.putSerializable("DEVICE",devices.get(getLayoutPosition()));
                        Intent intent=new Intent(MainActivity.this, DeviceInfoActivity.class);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }
                });
            }
        }
        private int isDeviceAlreadyExist(String macAddress){
            int i=0;
            for (Device device: devices) {
                if(device.getMacAddress().equals(macAddress)){
                    return i;
                }
                i++;
            }
            return -1;
        }

        public void addDevice(Device device){
            if(isDeviceAlreadyExist(device.getMacAddress())!=-1){
                return;
            }
            devices.add(device);
            notifyItemInserted(devices.size());
        }

        public void updateDevice(Device device){
            int position=isDeviceAlreadyExist(device.getMacAddress());
            if(position==-1){
                addDevice(device);
            }
            else {
                devices.set(position, device);
                notifyItemChanged(position);
            }
        }
    }

    public final static int UPDATE_REQUEST_CODE=321;
    AppUpdateManager appUpdateManager;
    private void checkForUpdate(){
        appUpdateManager=AppUpdateManagerFactory.create(this);
        Task<AppUpdateInfo> appUpdateInfoTask=appUpdateManager.getAppUpdateInfo();
        appUpdateInfoTask.addOnSuccessListener(new OnSuccessListener<AppUpdateInfo>() {
            @Override
            public void onSuccess(AppUpdateInfo appUpdateInfo) {
                if(appUpdateInfo.updateAvailability()== UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)){
                    try {
                        appUpdateManager.startUpdateFlowForResult(appUpdateInfo,AppUpdateType.IMMEDIATE,MainActivity.this,UPDATE_REQUEST_CODE);
                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                    }
                }
                else{}
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==UPDATE_REQUEST_CODE){
            if(resultCode!=RESULT_OK){
                showSnackbar("Update failed!");
            }
        }
    }

}

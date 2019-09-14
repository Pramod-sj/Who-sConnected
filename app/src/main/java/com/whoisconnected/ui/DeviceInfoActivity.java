package com.whoisconnected.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.whoisconnected.util.AdManager;
import com.whoisconnected.database.Model.Device;
import com.whoisconnected.R;
import com.whoisconnected.WhoIsConnected;

public class DeviceInfoActivity extends BaseActivity {

    TextView deviceHostNameTextView,deviceIpAddress,deviceMacAddress,deviceMacVendor,deviceVendorAddress,deviceMacVendorCountry;
    ImageButton editHostName;
    MaterialButton pingButton,portScanButton;
    FrameLayout ipAddressFrameLayout,macAddressFrameLayout,vendorAddressFrameLayout;
    AdManager adManager;
    ClipboardManager clipboardManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_info);
        setTitle("Device info");
        adManager=new AdManager(this);
        adManager.showBanner(getCoordinatorLayout());
        setHomeButton(R.drawable.ic_arrow_back_black_24dp, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        clipboardManager= (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        deviceHostNameTextView=findViewById(R.id.deviceHostNameTextView);
        deviceIpAddress=findViewById(R.id.deviceIpAddressTextView);
        deviceMacAddress=findViewById(R.id.deviceMacAddress);
        deviceMacVendor=findViewById(R.id.deviceMacVendor);
        deviceVendorAddress=findViewById(R.id.deviceMacVendorAddress);
        deviceMacVendorCountry=findViewById(R.id.deviceMacVendorCountry);
        editHostName=findViewById(R.id.editHOstNameButton);
        editHostName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUpdateHostNameDialog(new OnHostNameChanged() {
                    @Override
                    public void onChanged(String changedName) {
                        deviceHostNameTextView.setText(changedName);
                    }
                });
            }
        });
        ipAddressFrameLayout=findViewById(R.id.ipAddressFramelayout);
        ipAddressFrameLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ClipData clipData=ClipData.newPlainText("Ip address",bundledDevice.getIpAddress());
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
                ClipData clipData=ClipData.newPlainText("MAC address",bundledDevice.getMacAddress());
                clipboardManager.setPrimaryClip(clipData);
                showSnackbar("Copied!");
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                return false;
            }
        });
        vendorAddressFrameLayout=findViewById(R.id.vendorFramelayout);
        vendorAddressFrameLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ClipData clipData=ClipData.newPlainText("Vendor",bundledDevice.getMacvender().getCompany());
                clipboardManager.setPrimaryClip(clipData);
                showSnackbar("Copied!");
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                return false;
            }
        });
        pingButton=findViewById(R.id.pingButton);
        pingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle=new Bundle();
                bundle.putString("IP_ADDRESS",bundledDevice.getIpAddress());
                bundle.putInt("COUNT",10);
                Intent intent=new Intent(DeviceInfoActivity.this,PingActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        portScanButton=findViewById(R.id.portScanButton);
        portScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle=new Bundle();
                bundle.putString("IP_ADDRESS",bundledDevice.getIpAddress());
                bundle.putInt("START",1);
                bundle.putInt("END",1024);
                Intent intent=new Intent(DeviceInfoActivity.this, PortScannerActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        loadData();
    }
    private interface  OnHostNameChanged{
        void onChanged(String changedName);
    }
    private void showUpdateHostNameDialog(final OnHostNameChanged onHostNameChanged){
        final BottomSheetDialog bottomSheetDialog=new BottomSheetDialog(this);
        View view= LayoutInflater.from(this).inflate(R.layout.bottom_sheet_change_hostname_layout,null,false);
        final TextInputEditText hostNameEdt=view.findViewById(R.id.deviceHostNameEdt);
        MaterialButton positiveButton,negativeButton;
        positiveButton=view.findViewById(R.id.bottom_sheet_button2);
        negativeButton=view.findViewById(R.id.bottom_sheet_button1);
        positiveButton.setText("Change");
        negativeButton.setText("Cancel");
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String updatedhostName=hostNameEdt.getText().toString();
                new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        Log.i("SIZE", WhoIsConnected.getDB().dbDAO().updateHostName(updatedhostName,bundledDevice.getMacAddress())+"");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                bottomSheetDialog.dismiss();
                                onHostNameChanged.onChanged(updatedhostName);
                            }
                        });
                    }
                }.start();
            }
        });
        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
            }
        });
        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.setCancelable(true);
        bottomSheetDialog.show();
    }
    Device bundledDevice;
    private void loadData() {
        bundledDevice = (Device) getIntent().getExtras().getSerializable("DEVICE");
        deviceHostNameTextView.setText(bundledDevice.getHostName());
        deviceIpAddress.setText(bundledDevice.getIpAddress());
        deviceMacAddress.setText(bundledDevice.getMacAddress());
        if (bundledDevice.getMacvender() != null) {
            deviceMacVendor.setText(bundledDevice.getMacvender().getCompany());
            deviceVendorAddress.setText(bundledDevice.getMacvender().getAddress());
            deviceMacVendorCountry.setText(bundledDevice.getMacvender().getCountry());
        }
    }

    @Override
    protected void onDestroy() {
        adManager.destroyBannerAds();
        super.onDestroy();
    }
}

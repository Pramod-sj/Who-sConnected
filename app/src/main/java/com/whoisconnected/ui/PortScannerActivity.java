package com.whoisconnected.ui;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.whoisconnected.util.AdManager;
import com.whoisconnected.R;
import com.whoisconnected.tools.Ping;
import com.whoisconnected.tools.PortScanner;
import com.whoisconnected.tools.PortStatus;

import java.util.ArrayList;

public class PortScannerActivity extends BaseActivity {
    TextInputEditText iphostNameEdt,startPortEdt,endPortEdt;
    MaterialButton startPortScanButton,cancelPortScanButton;
    RecyclerView foundPortRecyclerView;
    FoundPortAdapter foundPortAdapter;
    PortScanner portScanner;
    AdManager adManager;
    TextInputLayout hostNameLayout,startInputLayout,endInputLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        adManager=new AdManager(this);
        adManager.loadInterstitiaAd();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_port_scanner);
        setTitle("Open Port scanner");
        setHomeButton(R.drawable.ic_arrow_back_black_24dp, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        startInputLayout=findViewById(R.id.startPortNoInputLayout);
        endInputLayout=findViewById(R.id.endPortNoInputLayout);
        hostNameLayout=findViewById(R.id.hostNameInputLayout);
        iphostNameEdt=findViewById(R.id.hostNameEdt);
        startPortEdt=findViewById(R.id.startPortEdt);
        endPortEdt=findViewById(R.id.endPortEdt);
        startPortScanButton=findViewById(R.id.startPortScanButton);
        cancelPortScanButton=findViewById(R.id.cancelPortScanButton);
        startPortScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String hostName=iphostNameEdt.getText().toString();
                int startPort=Math.max(Integer.parseInt(startPortEdt.getText().toString()),1);
                int endPort=Integer.parseInt(endPortEdt.getText().toString());
                if(TextUtils.isEmpty(hostName)){
                    hostNameLayout.setError("Hostname can't be empty");
                    lessShakeView(hostNameLayout,false);
                    return;
                }
                hostNameLayout.setError(null);
                if(TextUtils.isEmpty(startPortEdt.getText().toString())){
                    startInputLayout.setError("start port can't be empty");
                    lessShakeView(startInputLayout,false);
                    return;
                }
                startInputLayout.setError(null);
                if(TextUtils.isEmpty(endPortEdt.getText().toString())){
                    endInputLayout.setError("end port can't be empty");
                    lessShakeView(endInputLayout,false);
                    return;
                }
                endInputLayout.setError(null);
                if(startPort>=endPort){
                    startInputLayout.setError("Starting port can't be greater");
                    endInputLayout.setError("Ending port can't be lesser");
                    lessShakeView(startInputLayout,false);
                    lessShakeView(endInputLayout,false);
                    return;
                }
                startInputLayout.setError(null);
                endInputLayout.setError(null);
                if(foundPortAdapter!=null){
                    foundPortAdapter.clearDataSet();
                }
                portScanner=new PortScanner(PortScannerActivity.this,hostName,startPort,endPort,new PortScanner.PortListener() {
                    @Override
                    public void onResult(PortStatus portStatus) {
                        if (portStatus.isOpen()) {
                            foundPortAdapter.addPortStatus(portStatus);
                        }
                    }

                    @Override
                    public void onScanStart() {
                        showProgress(0);
                        startPortScanButton.setVisibility(View.GONE);
                        cancelPortScanButton.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onScanProgress(int i) {
                        updateProgress(i);
                    }

                    @Override
                    public void onFinished(ArrayList<Integer> openPorts) {
                        hideProgress();
                        startPortScanButton.setVisibility(View.VISIBLE);
                        cancelPortScanButton.setVisibility(View.GONE);
                        adManager.showInterStitialAd();
                    }
                });
                new Ping().pingAsync(hostName, new Ping.PingResultListener() {
                    @Override
                    public void onResult(Ping.PingResult pingResult) {
                        if(pingResult.isReachable()){
                            portScanner.execute();
                        }
                        else{
                            showSnackbar("Incorrect domain or ip address");
                            lessShakeView(hostNameLayout,false);
                        }
                    }
                });
            }
        });
        cancelPortScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(portScanner!=null){
                    portScanner.cancelScan();
                }
            }
        });
        foundPortRecyclerView=findViewById(R.id.foundPortRecyclerView);
        foundPortAdapter=new FoundPortAdapter();
        foundPortRecyclerView.setAdapter(foundPortAdapter);
        portSanForAIp();
    }

    public class FoundPortAdapter extends RecyclerView.Adapter<FoundPortAdapter.ViewHolder>{
        ArrayList<PortStatus> portStatuses;

        public FoundPortAdapter() {
            portStatuses=new ArrayList<>();
        }

        public void clearDataSet(){
            if(portStatuses!=null){
                int totalCount=portStatuses.size();
                portStatuses.clear();
                notifyItemRangeRemoved(0,totalCount);
            }
        }

        public void setPortStatuses(ArrayList<PortStatus> portStatuses) {
            this.portStatuses = portStatuses;
            notifyDataSetChanged();
        }

        public void addPortStatus(PortStatus portStatus){
            portStatuses.add(portStatus);
            notifyItemInserted(portStatuses.size());
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.found_port_item_layout,parent,false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.portNoTextView.setText(String.valueOf(portStatuses.get(position).getPortNo()));
            String portName=PortScanner.REGISTERED_PORT.get(portStatuses.get(position).getPortNo());
            holder.portNameTextView.setText(portName!=null?portName:"Unkown service");
        }

        @Override
        public int getItemCount() {
            return portStatuses.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView portNoTextView,portNameTextView;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                portNameTextView=itemView.findViewById(R.id.portNameTextView);
                portNameTextView.setSelected(true);
                portNoTextView=itemView.findViewById(R.id.portNoTextView);
            }
        }
    }

    public void portSanForAIp(){
        Bundle bundle=getIntent().getExtras();
         if(bundle!=null) {
             String ip=bundle.getString("IP_ADDRESS",null);
             int start=bundle.getInt("START",1);
             int end=bundle.getInt("END",1024);
             iphostNameEdt.setText(ip);
            startPortEdt.setText(String.valueOf(start));
            endPortEdt.setText(String.valueOf(end));
            startPortScanButton.performClick();
        }
    }

    @Override
    protected void onDestroy() {
        adManager.destroyInterstitialAds();
        super.onDestroy();
    }
}

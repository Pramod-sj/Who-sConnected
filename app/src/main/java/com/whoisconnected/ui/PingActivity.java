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
import com.whoisconnected.util.PlaceHolder;
import com.whoisconnected.R;
import com.whoisconnected.tools.Ping;

import java.util.ArrayList;

public class PingActivity extends BaseActivity {
    TextInputEditText pingEdt,pingCountEdt;
    MaterialButton startpingButton,cancelpingButton;
    TextView avgTimeTextView,minTimeTextView,maxTimeTextView,packetTransmittedTextview,packetReceivedTextView,packetLossTextView;
    RecyclerView pingSeqRecyclerView;
    PingSeqAdapter pingSeqAdapter;
    Ping.AsyncPing asyncPing;
    AdManager adManager;
    PlaceHolder placeHolder;
    TextInputLayout pingEdtLayout,pingCountEdtLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        adManager=new AdManager(this);
        adManager.loadInterstitiaAd();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ping);
        setTitle("Ping");
        setHomeButton(R.drawable.ic_arrow_back_black_24dp, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        pingEdtLayout=findViewById(R.id.pingEdtLayout);
        pingCountEdtLayout=findViewById(R.id.pingCountEdtLayout);
        pingSeqRecyclerView=findViewById(R.id.pingSeqRecyclerVIew);
        pingSeqAdapter=new PingSeqAdapter();
        pingSeqRecyclerView.setAdapter(pingSeqAdapter);
        pingCountEdt=findViewById(R.id.pingCountEdt);
        startpingButton=findViewById(R.id.startPingButton);
        cancelpingButton=findViewById(R.id.cancelPingButton);
        pingEdt=findViewById(R.id.pingEdt);
        packetTransmittedTextview=findViewById(R.id.packetSentTextView);
        packetLossTextView=findViewById(R.id.packetLossTextView);
        packetReceivedTextView=findViewById(R.id.packetReceivedTextView);
       avgTimeTextView=findViewById(R.id.avgTimeTextView);
        minTimeTextView=findViewById(R.id.minTimeTextView);
        maxTimeTextView=findViewById(R.id.maxTimeTextView);
        startpingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pingAddress=pingEdt.getText().toString();
                if(TextUtils.isEmpty(pingAddress)){
                    pingEdtLayout.setError("Domain can't be empty");
                    lessShakeView(pingEdtLayout,false);
                    return;
                }
                pingEdtLayout.setError(null);
                if(TextUtils.isEmpty(pingCountEdt.getText().toString())){
                    pingCountEdtLayout.setError("Count can't be empty");
                    lessShakeView(pingCountEdtLayout,false);
                    return;
                }
                pingCountEdtLayout.setError(null);
                if(pingSeqAdapter!=null){
                    pingSeqAdapter.clearDataSet();
                }
                Ping.PingOptions pingOptions = new Ping.PingOptions();
                pingOptions.setCount(Math.max(Integer.parseInt(pingCountEdt.getText().toString()),1));
                asyncPing=new Ping.AsyncPing(PingActivity.this,pingAddress, pingOptions, new Ping.PingListener() {
                    @Override
                    public void onComplete(Ping.PingStats pingStats) {
                        minTimeTextView.setText(String.format("%.2f",pingStats.getMin())+" ms");
                        maxTimeTextView.setText(String.format("%.2f",pingStats.getMax())+" ms");
                        avgTimeTextView.setText(String.format("%.2f",pingStats.getAvg())+" ms");
                        packetTransmittedTextview.setText(String.valueOf(pingStats.getPacketsTransmitted()));
                        packetReceivedTextView.setText(String.valueOf(pingStats.getPacketsReceived()));
                        packetLossTextView.setText(pingStats.getPacketsLoss() +"%");
                        hideProgress();
                        startpingButton.setVisibility(View.VISIBLE);
                        cancelpingButton.setVisibility(View.GONE);
                        adManager.showInterStitialAd();
                    }
                    @Override
                    public void onResult(Ping.PingSequence pingSequence) {
                        pingSeqAdapter.addPingSequence(pingSequence);
                    }

                    @Override
                    public void onScanStart() {
                        showProgress(0);
                        startpingButton.setVisibility(View.GONE);
                        cancelpingButton.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onScanProgress(int i) {
                        updateProgress(i);
                    }
                });
                new Ping().pingAsync(pingAddress, new Ping.PingResultListener() {
                    @Override
                    public void onResult(Ping.PingResult pingResult) {
                        if(pingResult.isReachable()){
                            asyncPing.execute();
                        }
                        else{
                            lessShakeView(pingEdtLayout,false);
                            showSnackbar("Incorrect domain or ip address");
                        }
                    }
                });
            }
        });
        cancelpingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(asyncPing!=null){
                    asyncPing.setCancel(true);
                }
            }
        });
        pingForAIp();
    }

    public class PingSeqAdapter extends RecyclerView.Adapter<PingSeqAdapter.ViewHolder>{

        ArrayList<Ping.PingSequence> pingSequences;

        public PingSeqAdapter() {
            this.pingSequences = new ArrayList<>();
        }

        public void setPingSequences(ArrayList<Ping.PingSequence> pingSequences) {
            this.pingSequences = pingSequences;
            notifyDataSetChanged();
        }


        public void clearDataSet(){
            if(pingSequences!=null){
                int totalCount=pingSequences.size();
                pingSequences.clear();
                notifyItemRangeRemoved(0,totalCount);
            }
        }

        public void addPingSequence(Ping.PingSequence pingSequence){
            pingSequences.add(pingSequence);
            notifyItemInserted(pingSequences.size());
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.ping_seq_item_layout,parent,false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.fromHostTextView.setText(pingSequences.get(position).getFromHost());
            holder.icmpTextView.setText(String.valueOf(pingSequences.get(position).getIcmp_seq()));
            holder.timeTextView.setText(pingSequences.get(position).getTime()+" ms");
            holder.ttlTextView.setText(pingSequences.get(position).getTtl()+"");
        }

        @Override
        public int getItemCount() {
            return pingSequences.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView ttlTextView,fromHostTextView,timeTextView,icmpTextView;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                ttlTextView=itemView.findViewById(R.id.ttlTextView);
                fromHostTextView=itemView.findViewById(R.id.responseFromTextView);
                fromHostTextView.setSelected(true);
                timeTextView=itemView.findViewById(R.id.timeTakenTextView);
                icmpTextView=itemView.findViewById(R.id.icmpSeqTextView);
            }
        }
    }

    public void pingForAIp(){
        Bundle bundle=getIntent().getExtras();
        if(bundle!=null) {
            String ip=bundle.getString("IP_ADDRESS",null);
            int count=bundle.getInt("COUNT",1);
            pingCountEdt.setText(String.valueOf(count));
            pingEdt.setText(ip);
            startpingButton.performClick();
        }
    }

    @Override
    protected void onDestroy() {
        adManager.destroyInterstitialAds();
        super.onDestroy();
    }

}

package com.whoisconnected.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.whoisconnected.BuildConfig;
import com.whoisconnected.R;
import com.whoisconnected.WhoIsConnected;

public class CustomCrashActivity extends BaseActivity {
    StringBuilder errorDetails;
    TextView toolbarTextView;
    ImageButton navButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_crash);
        setTitle("Got Crashed!");
        setHomeButton(R.drawable.ic_close_black_24dp, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        Bundle bundle=getIntent().getExtras();
        errorDetails=new StringBuilder(bundle.getString("errorDetails"));
        errorDetails.append("\n App version:"+ BuildConfig.VERSION_NAME);
        errorDetails.append("\n Model:"+ Build.MODEL);
        errorDetails.append("\n Android SDK:"+ Build.VERSION.SDK_INT);
        errorDetails.append("\n Brand :"+ Build.BRAND);
        errorDetails.append("\n Manufacturer:"+ Build.MANUFACTURER);
        TextView textView=findViewById(R.id.errorStack);
        textView.setText(errorDetails);
    }

    public void clearAppData(View view){
        WhoIsConnected.deleteAppData(this);
        finish();
        Intent intent=new Intent(Intent.ACTION_MAIN);
        startActivity(intent);
    }

    public void sendLog(View view){
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:pramodsinghjantwal@gmail.com"));
        intent.putExtra(Intent.EXTRA_SUBJECT, "Logcat");
        intent.putExtra(Intent.EXTRA_TEXT, errorDetails.toString());
        try {
            startActivity(Intent.createChooser(intent, "select client"));
        } catch (Exception e) {
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    public void restart(View view){
        finish();
        Intent intent=new Intent(this, SplashScreenActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}

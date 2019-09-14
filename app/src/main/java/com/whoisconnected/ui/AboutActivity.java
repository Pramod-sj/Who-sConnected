package com.whoisconnected.ui;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.whoisconnected.BuildConfig;
import com.whoisconnected.R;

public class AboutActivity extends BaseActivity{
    TextView opensourcelib,privaryPolicyTextView,tAndCTextView;
    TextView credit1TextView,credit2TextView,devEmailTextView,versionTextView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        setTitle("About us");
        setHomeButton(R.drawable.ic_arrow_back_black_24dp, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        versionTextView=findViewById(R.id.appVersionTextView);
        versionTextView.setText("Version "+ BuildConfig.VERSION_NAME);
        opensourcelib=findViewById(R.id.openSourceLib);
        opensourcelib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenSourceLibDialogFragment openSourceLibDialogFragment=new OpenSourceLibDialogFragment();
                openSourceLibDialogFragment.setContext(AboutActivity.this);
                openSourceLibDialogFragment.show(getSupportFragmentManager(),openSourceLibDialogFragment.getTag());
            }
        });
        credit1TextView=findViewById(R.id.credit1TextView);
        credit1TextView.setMovementMethod(LinkMovementMethod.getInstance());
        credit2TextView=findViewById(R.id.credit2TextView);
        credit2TextView.setMovementMethod(LinkMovementMethod.getInstance());
        devEmailTextView=findViewById(R.id.devEmailTextView);
        devEmailTextView.setMovementMethod(LinkMovementMethod.getInstance());
        privaryPolicyTextView=findViewById(R.id.privacyLib);
        tAndCTextView=findViewById(R.id.termCondition);
        privaryPolicyTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomInfoDialog customInfoDialog=new CustomInfoDialog(getSupportFragmentManager());
                customInfoDialog.showPrivacyPolicy();
            }
        });
        tAndCTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomInfoDialog customInfoDialog=new CustomInfoDialog(getSupportFragmentManager());
                customInfoDialog.showTermCondition();
            }
        });
    }
}

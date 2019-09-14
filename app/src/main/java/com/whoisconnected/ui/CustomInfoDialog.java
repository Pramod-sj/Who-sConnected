package com.whoisconnected.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.whoisconnected.R;

public class CustomInfoDialog extends DialogFragment {

    FragmentManager fragmentManager;

    public CustomInfoDialog(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
    }

    WebView webView;
    String url=null;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.privary_policy_dialog_layout,container,false);
        webView=view.findViewById(R.id.privacyTermConditionWebView);
        webView.loadUrl(url);
        return view;
    }

    public void showPrivacyPolicy(){
        url="file:///android_asset/privacy_policy.html";
        show(fragmentManager,getTag());
    }

    public void showTermCondition(){
        url="file:///android_asset/terms_and_conditions.html";
        show(fragmentManager,getTag());
    }

    @Override
    public void onStart() {
        super.onStart();
        if(getDialog()!=null){
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}

package com.whoisconnected.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.whoisconnected.R;

import java.util.Timer;
import java.util.TimerTask;

public class BoardingActivity extends AppCompatActivity {
    MaterialButton getStartedButton;
    TextView welcomeTextView;
    SharedPreferences sharedPreferences;
    ViewPager viewPager;
    ViewpageAdapter viewpageAdapter;
    LinearLayout dotslinearLayout;
    FrameLayout viewpagerParentLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boarding);
        sharedPreferences= PreferenceManager.getDefaultSharedPreferences(this);
        viewpagerParentLayout=findViewById(R.id.viewpagerParentLayout);
        getStartedButton=findViewById(R.id.getStartedButton);
        dotslinearLayout=findViewById(R.id.dotIndicatorLinearLayout);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                welcomeTextView.setVisibility(View.VISIBLE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                getStartedButton.setVisibility(View.VISIBLE);
                                getStartedButton.setEnabled(false);
                            }
                        },1000);
                        viewpagerParentLayout.setVisibility(View.VISIBLE);
                        autoScroll();
                    }
                },1000);
            }
        },1000);
        welcomeTextView=findViewById(R.id.welcomeTextView);
        getStartedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedPreferences.edit().putBoolean("SHOW_ON_BOARDING",false).commit();
                startActivity(new Intent(BoardingActivity.this, MainActivity.class));
                finishAffinity();
            }
        });
        viewPager=findViewById(R.id.viewPager);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                showActiveDotIndicator(position);
                if(position==2){
                    getStartedButton.setEnabled(true);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        viewpageAdapter=new ViewpageAdapter();
        viewPager.setAdapter(viewpageAdapter);
        showActiveDotIndicator(0);
    }

    private void autoScroll(){
        final Handler handler=new Handler();
        final Timer timer=new Timer();
        final Runnable runnable=new Runnable() {
            @Override
            public void run() {
                int nextPage=viewPager.getCurrentItem()+1;
                if(nextPage<3) {
                    viewPager.setCurrentItem(nextPage,true);
                    showActiveDotIndicator(nextPage);
                }
                else{
                    timer.cancel();
                }
            }
        };
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(runnable);
            }
        },1500,1500);
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }

    public class ViewpageAdapter extends PagerAdapter{

        int[] images;
        String[] titles;
        String[] subtitles;

        public ViewpageAdapter() {
            titles=getResources().getStringArray(R.array.boarding_title_texts);
            subtitles=getResources().getStringArray(R.array.boarding_sub_title_texts);
            images= new int[]{R.drawable.speedometer, R.drawable.identification, R.drawable.server};
        }

        @Override
        public int getCount() {
            return images.length;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            View view=LayoutInflater.from(container.getContext()).inflate(R.layout.viewpager_walk_through_item_layout,container,false);
            ImageView imageView=view.findViewById(R.id.pagerImageView);
            TextView titleTextView,subTitleTextView;
            titleTextView=view.findViewById(R.id.pagerTitleTextView);
            subTitleTextView=view.findViewById(R.id.pagerSubTitleTextView);
            imageView.setImageResource(images[position]);
            titleTextView.setText(titles[position]);
            subTitleTextView.setText(subtitles[position]);
            container.addView(view);
            return view;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view==object;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }
    }

    ImageView[] dotViews=new ImageView[3];
    public void showActiveDotIndicator(int position){
        if (dotslinearLayout!=null){
            dotslinearLayout.removeAllViews();
        }
        dotslinearLayout=findViewById(R.id.dotIndicatorLinearLayout);
        for(int i=0;i<dotViews.length;i++){
            dotViews[i]=new ImageView(this);
            if(i==position){
                dotViews[i].setImageDrawable(getResources().getDrawable(R.drawable.dot_active));
            }
            else{
                dotViews[i].setImageDrawable(getResources().getDrawable(R.drawable.dot_inactive));
            }
            LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(4,0,4,0);
            dotslinearLayout.addView(dotViews[i],params);
        }
    }

}

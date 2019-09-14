package com.whoisconnected.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.whoisconnected.R;

public class PlaceHolder extends FrameLayout {
    String placeHolderMessage,placeHolderTitle;
    Drawable placeHolderImage;
    ImageView placeHolderImageView;
    TextView placeHolderTextView,placeHolderDescTextView;
    Context context;
    public PlaceHolder(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context=context;
        TypedArray attributes=context.obtainStyledAttributes(attrs, R.styleable.PlaceHolder,0,0);
        placeHolderTitle=attributes.getString(R.styleable.PlaceHolder_setPlaceHolderTitle);
        placeHolderImage=attributes.getDrawable(R.styleable.PlaceHolder_setPlaceHolderImage);
        placeHolderMessage=attributes.getString(R.styleable.PlaceHolder_setPlaceHolderMessage);
        View view=LayoutInflater.from(context).inflate(R.layout.placeholder,this);
        placeHolderImageView=view.findViewById(R.id.placeholderimage);
        placeHolderTextView=view.findViewById(R.id.placeHolderTextView);
        placeHolderDescTextView=view.findViewById(R.id.placeHolderDescTextView);
        if(placeHolderImage!=null) {
            placeHolderImageView.setImageDrawable(placeHolderImage);
        }
        placeHolderDescTextView.setText(placeHolderMessage);
        placeHolderTextView.setText(placeHolderTitle);
        attributes.recycle();
    }

    public void setPlaceHolder(Drawable placeHolderImage,String title,String placeHolderMessage) {
        placeHolderImageView.setImageDrawable(placeHolderImage);
        placeHolderTextView.setText(title);
        placeHolderDescTextView.setText(placeHolderMessage);
    }

    public void showPlaceHolder(Drawable placeHolderImage,String title,String placeHolderMessage) {
        placeHolderImageView.setImageDrawable(placeHolderImage);
        placeHolderTextView.setText(title);
        placeHolderDescTextView.setText(placeHolderMessage);
        this.setVisibility(VISIBLE);
    }

    public void showPlaceHolder(int placeholdlerImageId,String title,String placeHolderMessage) {
        placeHolderImageView.setImageResource(placeholdlerImageId);
        placeHolderTextView.setText(title);
        placeHolderDescTextView.setText(placeHolderMessage);
        this.setVisibility(VISIBLE);
    }

    public void showPlaceHolder() {
        this.setVisibility(VISIBLE);
    }

    public void hidePlaceHolder(){
        this.setVisibility(GONE);
    }
}

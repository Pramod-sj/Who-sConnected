package com.whoisconnected.ui;

import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.whoisconnected.R;


public class OpenSourceLibDialogFragment extends DialogFragment {
    RecyclerView recyclerView;
    ArrayList<String> libNames;
    Context context;

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout linearLayout=new LinearLayout(context);
        recyclerView=new RecyclerView(context);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        int margin= (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,16,getResources().getDisplayMetrics());
        layoutParams.setMargins(margin,margin,margin,margin);
        recyclerView.setLayoutParams(layoutParams);
        libNames=new ArrayList(){{addAll(Arrays.asList(context.getResources().getStringArray(R.array.libraries)));}};
        recyclerView.setAdapter(new OpenSourceLibAdapter(getContext(),libNames));
        linearLayout.addView(recyclerView);
        return linearLayout;
    }

    @Override
    public void onStart() {
        super.onStart();
        if(getDialog()!=null){
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }


    public class OpenSourceLibAdapter extends RecyclerView.Adapter<OpenSourceLibAdapter.ViewHolder>{
        Context context;
        ArrayList<String> libNames;

        public OpenSourceLibAdapter(Context context, ArrayList<String> libNames) {
            this.context = context;
            this.libNames = libNames;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view= LayoutInflater.from(context).inflate(R.layout.recycler_lib_textview_item_layout,viewGroup,false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
            viewHolder.libName.setText(Html.fromHtml(libNames.get(i)));
            viewHolder.libName.setMovementMethod(LinkMovementMethod.getInstance());
        }

        @Override
        public int getItemCount() {
            return libNames.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView libName;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                libName=itemView.findViewById(R.id.libName);
            }

        }
    }
}

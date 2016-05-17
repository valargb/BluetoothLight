package com.example.bluetoothlight.View;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.bluetoothlight.R;

/**
 * Created by 肥肥 on 2016/5/14.
 */
public class ModelView extends FrameLayout {

    private ImageView modelView;
    private TextView modelName;

    public ModelView(Context context){
        super(context);
    }

    public ModelView(Context context,AttributeSet attrs){

        super(context, attrs);
        LayoutInflater inflater =(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.model,this);
        modelView = (ImageView)findViewById(R.id.modelView);
        modelName = (TextView)findViewById(R.id.modelName);
    }

    //设置图片资源
    public void setImageResource(int resId){
        modelView.setImageResource(resId);
    }

    //设置要显示的文字
    public void setModelName(String text){
        modelName.setText(text);
    }






}

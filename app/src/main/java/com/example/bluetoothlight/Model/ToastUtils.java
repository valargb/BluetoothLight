package com.example.bluetoothlight.Model;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

/**
 * Created by 肥肥 on 2016/5/10.
 * 自定义Toast时间（3500ms内自定义）
 *
 */
public class ToastUtils{
    public static void makeText(Context mContext,String mString,final int time){

        final Toast mToast = Toast.makeText(mContext,mString,Toast.LENGTH_LONG);
        mToast.show();
        Handler mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (time <= 3500){
                    mToast.cancel();
                }
            }
        },time);

    }
}

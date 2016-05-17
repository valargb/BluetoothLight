package com.example.bluetoothlight.View;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.example.bluetoothlight.Presenter.DeviceScanActivity;
import com.example.bluetoothlight.R;

import java.lang.reflect.Field;

/**
 * Created by 肥肥 on 2016/5/6.
 */
public class DataDialog extends DialogFragment{
    public Dialog onCreateDialog(Bundle savedInstanceState){
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View view = inflater.inflate(R.layout.dataread, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);

        builder.setTitle("读取参数");
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setPositiveButton("读取", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (DeviceScanActivity.gattCharacteristic_char6 != null) {
                    DeviceScanActivity
                            .ReadCharX(DeviceScanActivity.gattCharacteristic_char6);
                } else if (DeviceScanActivity.gattCharacteristic_char9 != null) {
                    DeviceScanActivity
                            .ReadCharX(DeviceScanActivity.gattCharacteristic_char9);
                }


                //利用java的反射机制，点击读取按钮时不关闭dialog
                try {
                    Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                    field.setAccessible(true);
                    field.set(dialog, false);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //同理，利用java的反射机制，随时关闭Dialog
                try {
                    Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                    field.setAccessible(true);
                    field.set(dialog, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        return builder.create();
    }



}

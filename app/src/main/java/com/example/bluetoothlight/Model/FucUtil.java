package com.example.bluetoothlight.Model;

import android.content.Context;

import java.io.InputStream;

/**
 * Created by 肥肥 on 2016/4/23.
 * 功能性函数扩展类
 *
 */
public class FucUtil {
    /*
    读取asset目录下文件
    @return content
    * */
    public static String readFile(Context mContext ,String file,String code)
    {
        int len = 0 ;
        byte []buf = null;
        String result = "";
        try{
            InputStream in = mContext.getAssets().open(file);
            len = in.available();
            buf = new byte[len];
            in.read(buf,0,len);

            result = new String(buf,code);
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;

    }
}

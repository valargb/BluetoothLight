package com.example.bluetoothlight.Model;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Created by 肥肥 on 2016/4/23.
 */
public class JsonParser {
    public static String TAG = "JsonParser";
    public static int index = 0;

    public static String parseGramarResult(String json){
        StringBuffer ret = new StringBuffer() ;

        try{
            JSONTokener mJSONTokener = new JSONTokener(json);
            JSONObject  mJSONObject = new JSONObject(mJSONTokener);

            JSONArray words = mJSONObject.getJSONArray("ws");
            for (int i = 0 ; i < words.length(); i++){
                JSONArray items = words.getJSONObject(i).getJSONArray("cw");
                for (int j = 0 ; j < items.length() ; j++){
                    JSONObject obj = items.getJSONObject(j);
                    if (obj.getString("w").contains("nomatch")){
                        ret.append("没有匹配结果");
                        return ret.toString();
                    }
                    //ret=命令词 置信度 命令词 置信度
                    ret.append(obj.getString("w"));
                    ret.append(obj.getInt("sc"));

                }
            }
        }catch (Exception e){
            e.printStackTrace();
            ret.append("没有匹配结果");
        }

        for (; index < ret.length();index++){
            //判断ret的内容，找到最接近的命令词，isDigit判断字符是否是数字，是数字则跳出循环
            if(Character.isDigit(ret.charAt(index)))
                break;
        }
        //retCommand = 最匹配的命令词
        String retCommand = ret.substring(0, index);

        LogUtil.e(TAG, "Json解析结果：    " + ret);
        LogUtil.e(TAG, "retComand:      " + retCommand);
        return retCommand;

    }
}

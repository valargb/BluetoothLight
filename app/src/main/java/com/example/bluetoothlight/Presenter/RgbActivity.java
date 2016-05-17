package com.example.bluetoothlight.Presenter;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.bluetoothlight.Model.FucUtil;
import com.example.bluetoothlight.Model.JsonParser;
import com.example.bluetoothlight.Model.LogUtil;
import com.example.bluetoothlight.Model.ToastUtils;
import com.example.bluetoothlight.Model.Utils;
import com.example.bluetoothlight.R;
import com.example.bluetoothlight.View.ColorPickView;
import com.example.bluetoothlight.View.ModelView;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.GrammarListener;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;

import java.math.BigDecimal;
import java.util.HashMap;

/**
 * Created by 肥肥 on 2016/4/14.
 */
public class RgbActivity extends Activity implements View.OnClickListener,View.OnTouchListener{

    private final static String TAG = "RgbActivity";
    private final String ACTION_NAME_RSSI = "BLUETOOTHLIGHT_RSSI";//其他文件的广播的定义必须一致
    private final String ACTION_CONNECT = "BLUETOOTHLIGHT_CONNECT";//其他文件广播的定义必须一致

    //语音听写UI初始化
   // private RecognizerDialog mRecognizerDialog;
    //创建SpeechRecognizer,语音识别对象
    private SpeechRecognizer mSpeechRecognizer;

    //缓存
    private SharedPreferences mSharedPreferences;
    //云端语法文件
    private String mCloudGrammar = null;

    private static final String KEY_GRAMMAR_ABNF_ID = "grammar_abnf_id";
    private static final String GRAMMAR_TYPE_ABNF = "abnf";
    private static final String GRAMMAR_TYPE_BNF = "bnf";

    //引擎类型
    private String mEngineType = null;
    //语法，词典临时变量
    String mContent;
    //函数调用返回值
    int ret = 0 ;

    public static AlertDialog dataDialog = null;
    public static AlertDialog warningDialog = null;

    //警告铃声
    private Button warningBtn;
    private SoundPool soundPool;
    HashMap<Integer,Integer>musicId = new HashMap<Integer, Integer>();


    private TextView txtColor;
    private TextView colorShow;
    private ColorPickView myView;

    private TextView textView_brightness;
    private TextView textView_red;
    private TextView textView_green;
    private TextView textView_blue;
    private TextView textView_white;

    private SeekBar seekBar_brightness;
    private SeekBar seekBar_red;
    private SeekBar seekBar_green;
    private SeekBar seekBar_blue;
    private SeekBar seekBar_white;


    //Dialog中的控件
  /* public static TextView textView_U;
    public static TextView textView_I;
    public static TextView textView_Pnow;
    public static TextView textView_Psum;*/

    //ModelDialog 中的控件
    public static ModelView modelEating;
    public static ModelView modelReading;
    public static ModelView modelSleeping;


    public static int[] dataInt = new int[6];
    public static byte[] dataByte = new byte[6];



    int mWhite = 0;			//白色 0~255  ------这个颜色在我们的amomcu的蓝牙灯板子上无效，不过考虑到有些朋友想利用，我这里是留出了这个接口
    int mRed = 255;			//红色 0~255
    int mGreen = 0;			//绿色 0~255
    int mBlue = 0;			//蓝色 0~255

    int mBrightness = 100;	    			// 这个是亮度的意思 0~100; 0最黑， 100最亮
    int mBrightness_old = mBrightness;	    // 这个是亮度的意思 0~100; 0最黑， 100最亮

    //根据rssi 值计算距离， 只是参考作用， 不准确---
    static final int rssibufferSize = 50;
    int[] rssibuffer = new int[rssibufferSize];
    int rssibufferIndex = 0;
    boolean rssiUsedFalg = false;


    //用于消息更新参数
    public static final int REFRESH = 0x000001;
    public static final int UPDATE_DIALOGTEXT = 0x000010;
    private static Handler mHandler ;


    //开关灯
    ToggleButton toggle_onoff = null;
    Boolean  light_onoff = false;

    //律动
    ToggleButton toggle_flash = null;
    Boolean light_flash = false;
    private final int light_flash_time_ms = 800;//律动间隔时间

    //语音输入按钮
    public static Button button_voice;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.rgb_activity);
        getActionBar().setTitle("Future Light");



        myView = (ColorPickView)findViewById(R.id.color_picker_view);
        txtColor = (TextView)findViewById(R.id.txt_color);
        colorShow = (TextView)findViewById(R.id.txt_colorshow);

        seekBar_brightness = (SeekBar) findViewById(R.id.seekbar_brightness);
        textView_brightness = (TextView) findViewById(R.id.txt_brightness);

        seekBar_white = (SeekBar) findViewById(R.id.seekbar_white);
        seekBar_red = (SeekBar) findViewById(R.id.seekbar_red);
        seekBar_green = (SeekBar) findViewById(R.id.seekbar_green);
        seekBar_blue = (SeekBar) findViewById(R.id.seekbar_blue);

        textView_white = (TextView) findViewById(R.id.txt_color_white);
        textView_red = (TextView) findViewById(R.id.txt_color_red);
        textView_green = (TextView) findViewById(R.id.txt_color_green);
        textView_blue = (TextView) findViewById(R.id.txt_color_blue);

        Button button_dataread = (Button)findViewById(R.id.button_dataread);
        button_dataread.setOnClickListener(this);


        button_voice = (Button)findViewById(R.id.button_voice);
        button_voice.setOnTouchListener(this);

        //语音识别引擎类型为云端
        mEngineType = SpeechConstant.TYPE_CLOUD;

        //创建SpeechRecognizedr对象,并初始化,注意不要导入错误的SpeechRecognizer
        mSpeechRecognizer = SpeechRecognizer.createRecognizer(RgbActivity.this,mInitListener);

        //初始化语法，命令词
        mCloudGrammar = FucUtil.readFile(this,"Command.abnf","utf-8");
        mContent = new String(mCloudGrammar);

        //设置指定引擎类型
        mSpeechRecognizer.setParameter(SpeechConstant.ENGINE_TYPE,"cloud");
        mSpeechRecognizer.setParameter(SpeechConstant.SUBJECT,"asr");
        ret = mSpeechRecognizer.buildGrammar(GRAMMAR_TYPE_ABNF,mContent,mCloudGrammarlistener);
        if (ret != ErrorCode.SUCCESS){
            LogUtil.e(TAG,"语音语法构建失败，错误码："+ ret);

        }


        //注册广播
        registerBoradcastReceiver();

         mHandler = new Handler() {
             public void handleMessage(Message msg){
                 switch (msg.what){
                     case REFRESH:

                     //设置灯泡颜色
                        SetColor2Device(mWhite, mRed, mGreen, mBlue, mBrightness);
                    //更新更新颜色分量进度
                        UpdateProgress();
                    //更新颜色分量文本
                        UpdateText();
                    break;

                     /*case UPDATE_DIALOGTEXT:

                         textView_U.setText(String.valueOf(dataInt[0]));
                         textView_I.setText(String.valueOf(dataInt[1]));
                         textView_Pnow.setText(String.valueOf(dataInt[2]));
                         textView_Psum.setText(String.valueOf(dataInt[3]));
                         LogUtil.e(TAG, "数据显示");
                         break;*/

                     default:
                         break;

                 }

            }
         };

        //开启线程
        new MyThread().start();

        //大圆 颜色拾取
        myView.setOnColorChangedListener(new ColorPickView.OnColorChangedListener() {
            @Override
            public void onColorChange(int color) {
                String pwm = Integer.toHexString(color).toUpperCase();

                //Utils.isHexChar(pwm)判断是否为十六进制字符
                if (Utils.isHexChar(pwm)) {
                    byte[] PwmValue = Utils.hexStringToBytes(pwm);
                    //"&"按位与
                    mWhite = PwmValue[0] & 0xff;
                    mRed = PwmValue[1] & 0xff;
                    mGreen = PwmValue[2] & 0xff;
                    mBlue = PwmValue[3] & 0xff;

                    //更新所有系数
                    UpdateAllParameter();
                }
            }
        });

        //连续百分比函数，亮度改变滑块监听
        seekBar_brightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
           //当进度发生改变时执行
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int i= seekBar.getProgress();//获取滑块进度值
                textView_brightness.setText("亮度："+Integer.valueOf(progress) + "%");
                LogUtil.i(TAG, "Brightness = " + Integer.toHexString(i).toUpperCase());
                mBrightness = (byte) i;
                UpdateAllParameter();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        //白色分量监听
        seekBar_white.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int i = seekBar.getProgress();
                textView_white.setTextColor(0xFF555555);
                textView_white.setText("白色分量: "+Integer.toHexString(i).toUpperCase()+"("+Integer.valueOf(i*100/255)+"%)");
                LogUtil.i(TAG,"color white ="+Integer.toHexString(i).toUpperCase());
                mWhite = i;
                UpdateAllParameter();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        //红色分量监听
        seekBar_red.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int i= seekBar.getProgress();
                textView_red.setTextColor(0xFFFF0000);
                textView_red.setText("红色分量: "+Integer.toHexString(i).toUpperCase()+"("+Integer.valueOf(i*100/255)+"%)");
                LogUtil.i(TAG, "color red = " + Integer.toHexString(i).toUpperCase());
                mRed = i;
                UpdateAllParameter();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        seekBar_green.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int i= seekBar.getProgress();
                textView_green.setTextColor(0xFF00FF00);
                textView_green.setText("绿色分量: "+Integer.toHexString(i).toUpperCase()+"("+Integer.valueOf(i*100/255)+"%)");
                Log.i(TAG, "color green = " + Integer.toHexString(i).toUpperCase());
                mGreen  = i;
                UpdateAllParameter();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        seekBar_blue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int i= seekBar.getProgress();
                textView_blue.setTextColor(0xFF0000FF);
                textView_blue.setText("蓝色分量: "+Integer.toHexString(i).toUpperCase()+"("+Integer.valueOf(i*100/255)+"%)");
                Log.i(TAG, "color blue = " + Integer.toHexString(i).toUpperCase());
                mBlue  = i;
                UpdateAllParameter();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        //开关按钮
        toggle_onoff = (ToggleButton)findViewById(R.id.togglebutton_onoff);
        toggle_onoff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                LogUtil.i(TAG, "onCheckedChaged isChecked" + isChecked);

                if (isChecked){
                    light_onoff = true;
                    mBrightness = mBrightness_old;
                    //点击开灯按钮变大

                    //避免灯不开
                    if (mBrightness == 0){
                        mBrightness = 5;
                    }
                }
                else
                {
                    light_onoff = false;
                    mBrightness_old = mBrightness;
                    mBrightness = 0;
                }

                seekBar_brightness.setProgress(mBrightness);
                UpdateAllParameter();
            }
        });

        toggle_flash = (ToggleButton)findViewById(R.id.togglebutton_flash);
        toggle_flash.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i(TAG, "onCheckedChanged  isChecked= " + isChecked);
                if (isChecked){
                    light_flash = true;
                }else{
                    light_flash = false;
                }
            }
        });

        ReadParameter();	//读出参数
        if(light_onoff)		toggle_onoff.setChecked(true);
        else 				toggle_onoff.setChecked(false);

        if(light_flash)		toggle_flash.setChecked(true);
        else 				toggle_flash.setChecked(false);

        // 更新 参数
        UpdateAllParameter();


    }


    @Override
    protected void onStart() {
        ReadParameter();
        LogUtil.e(TAG,"onStart..");
        super.onStart();
    }

    @Override
    protected void onResume() {
        LogUtil.e(TAG,"onResume..");
        super.onResume();
    }

    @Override
    protected void onPause() {
        WriteParameter();
        super.onPause();
    }

    //接收rssi的广播,测量距离
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        boolean isWarning = true;
        @Override
        public void onReceive(final Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(ACTION_NAME_RSSI)){
                //警告铃声
                int rssi = intent.getIntExtra("RSSI",0);

                int rssi_avg = 0;
                int distance_cm_min = 10; // 距离cm -30dbm
                int distance_cm_max_near = 1500; // 距离cm -90dbm
                int distance_cm_max_middle = 5000; // 距离cm -90dbm
                int distance_cm_max_far = 10000; // 距离cm -90dbm
                int near = -72;
                int middle = -80;
                int far = -88;
                double distance = 0.0f;



                    rssibuffer[rssibufferIndex] = rssi;
                    rssibufferIndex++;

                    if (rssibufferIndex == rssibufferSize)
                        rssiUsedFalg = true;

                    rssibufferIndex = rssibufferIndex % rssibufferSize;

                    if (rssiUsedFalg) {
                        int rssi_sum = 0;
                        for (int i = 0; i < rssibufferSize; i++) {
                            rssi_sum += rssibuffer[i];
                        }

                        //求rssi平均值
                        rssi_avg = rssi_sum / rssibufferSize;

                      /*if (-rssi_avg < 35)
                            rssi_avg = -35;

                        if (-rssi_avg < -near) {
                            distance = distance_cm_min
                                    + ((-rssi_avg - 35) / (double) (-near - 35))
                                    * distance_cm_max_near;
                        } else if (-rssi_avg < -middle) {
                            distance = distance_cm_min
                                    + ((-rssi_avg - 35) / (double) (-middle - 35))
                                    * distance_cm_max_middle;
                        } else {
                            distance = distance_cm_min
                                    + ((-rssi_avg - 35) / (double) (-far - 35))
                                    * distance_cm_max_far;
                        }*/

                        //测距  不准确
                        distance = Math.pow(10,(-rssi_avg + 70)/(10.0*5.395));
                        distance = (distance/100.0-3)*2;
                        if (distance < 0 ) {
                            distance = 0;
                        }else{
                        BigDecimal b = new BigDecimal(distance);
                        distance = b.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
                        }
                    }

                getActionBar().setTitle(
                        "RSSI: " + rssi_avg + " dbm" + ", " + "距离: "
                                + (int) distance + " m");

                if(distance >= 8 && isWarning){
                    isWarning = false;
                    LogUtil.e(TAG,"进入警告铃声判断"+distance);

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("WARNING");
                    builder.setIcon(R.mipmap.ic_launcher);
                    builder.setMessage("请与设备保持安全连接范围");

                    builder.setCancelable(false);//设为不可取消
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            soundPool.stop(1);
                            Intent intent = new Intent(context,RgbActivity.class);
                            context.startActivity(intent);//重新启动RgbActivity
                        }
                    });

                    warningDialog = builder.create();
                    warningDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);//设置权限，使得可以再广播中触发（系统级别）
                    warningDialog.show();


            //警告铃声响起：
           if (!warningDialog.equals(null)){
                LogUtil.e(TAG,"警告铃声响起来啦。。。");
                soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC,0);
                musicId.put(1, soundPool.load(getApplicationContext(), R.raw.bikabi, 1));
                soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                    @Override
                    public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                        soundPool.play(musicId.get(1), 1, 1, 1, 0, 1);
                    }
                });

            }
        }
            }
            else if(action.equals(ACTION_CONNECT)){
                int status = intent.getIntExtra("CONNECT_STATUC", 0);
                if(status == 0)	{
                    getActionBar().setTitle("已断开连接");
                    finish();
                }
                else{
                    getActionBar().setTitle("已连接灯泡");
                }
            }
        }
    };

    //注册广播
    public void registerBoradcastReceiver(){
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(ACTION_NAME_RSSI);
        mIntentFilter.addAction(ACTION_CONNECT);
        //注册广播
        registerReceiver(mBroadcastReceiver,mIntentFilter);
    }

    //设置参数到蓝牙设备
    private void SetColor2Device(int white,int red,int green,int blue,int brightness){
        byte[] PwmValue = new byte[4];
        //测试灯泡所用
        // 数组数值转化
        PwmValue[0] = (byte)((((int)(white/255.0*224) & 0xFF) * brightness/100) & 0xff);

        PwmValue[3] = (byte)((((int)(red/255.0*160) & 0xFF) * brightness/100) & 0xff);

        PwmValue[2] = (byte)((((int)(green/255.0*224) & 0xFF) * brightness/100) & 0xff);
        PwmValue[1] = (byte)((((int)(blue/255.0*224) & 0xFF) * brightness/100) & 0xff);

        LogUtil.w(TAG, "----------mWhite " + mRed);
        LogUtil.w(TAG, "----------PwmValue[0] " + PwmValue[1]);

        if (DeviceScanActivity.gattCharacteristic_keydata != null){
            LogUtil.w(TAG, "keydata");
            DeviceScanActivity.WriteCharX(DeviceScanActivity.gattCharacteristic_keydata,PwmValue);
        }
        if (DeviceScanActivity.gattCharacteristic_charA != null){
            LogUtil.w(TAG,"CHARA");
            DeviceScanActivity.WriteCharX(DeviceScanActivity.gattCharacteristic_charA,PwmValue);
        }
    }

    //获取颜色值的字节数组
    private byte[] GetColorByte() {
        byte[] PwmValue = new byte[4];

        PwmValue[0] = (byte)(mWhite & 0xff);
        PwmValue[1] = (byte)(mRed & 0xff);
        PwmValue[2] = (byte)(mGreen & 0xff);
        PwmValue[3] = (byte)(mBlue & 0xff);

        return PwmValue;
    }

    //获取整型值
    private int GetColorInt(){
        byte[] PwmValue = GetColorByte();
        int color = Utils.byteArrayToInt(PwmValue,0);
        return color;
    }

    //更新颜色滑块进度
    private void UpdateProgress(){
        seekBar_brightness.setProgress(mBrightness);
        seekBar_white.setProgress(mWhite);
        seekBar_red.setProgress(mRed);
        seekBar_green.setProgress(mGreen);
        seekBar_blue.setProgress(mBlue);
    }

    //更新颜色分量进度
    private void UpdateText() {
        byte[] PwmValue = new byte[4];

        PwmValue[0] = (byte) (((mWhite & 0xFF) * mBrightness / 100) & 0xff);
        PwmValue[1] = (byte) (((mRed & 0xFF) * mBrightness / 100) & 0xff);
        PwmValue[2] = (byte) (((mGreen & 0xFF) * mBrightness / 100) & 0xff);
        PwmValue[3] = (byte) (((mBlue & 0xFF) * mBrightness / 100) & 0xff);

        txtColor.setTextColor(0xFF555555);
        //txtColor.setText("当前颜色:0x" + Utils.bytesToHexString(PwmValue).toUpperCase() + "[亮白红绿蓝]");
        colorShow.setBackgroundColor(Color.rgb(mRed,mGreen,mBlue));
        LogUtil.e(TAG,"colorShow颜色："+mRed+mGreen+mBlue);

    }
        //发送消息，以便更新参数
        private void UpdateAllParameter(){
            Message msg = new Message();
            msg.what = REFRESH;
            mHandler.sendMessage(msg);
    }

    //线程，主要用于灯的闪动(亮度不变 颜色改变)
    public  class MyThread extends Thread{
        public void run(){
            while (!Thread.currentThread().isInterrupted()){
                boolean isRed = true;
                if (light_onoff && light_flash ){
                    if(isRed){
                    mRed = 255;
                    mWhite = 0;
                    mBlue = 0;
                    mGreen = 0;
                    mBrightness = 100;
                    isRed = false;
                    }else{
                        mRed = 0;
                        mWhite = 5;
                        mBlue = 0;
                        mGreen = 0;
                        mBrightness = 5;
                        isRed = true;
                    }

                    LogUtil.e(TAG,"mRed" + mRed);
                    Message msg = new Message();
                    msg.what = REFRESH;
                    mHandler.sendMessage(msg);
                }

                try{
                    Thread.sleep(light_flash_time_ms);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }

            }
        }
    }

    //将参数到存储器（onDestory（）调用，onStop()调用）
    private void WriteParameter(){
        SharedPreferences.Editor sharedata = getSharedPreferences("data" , 0).edit();
        sharedata.putInt("mWhite", mWhite);
        sharedata.putInt("mRed", mRed);
        sharedata.putInt("mGreen", mGreen);
        sharedata.putInt("mBlue", mBlue);
        sharedata.putInt("mBrightness", mBrightness);
        sharedata.putInt("mBrightness_old", mBrightness_old);
        sharedata.putBoolean("light_onoff", light_onoff);
        sharedata.putBoolean("light_flash", light_flash);

        sharedata.commit();
    }

    //从sharedPrefences中读取参数（onCreate中调用）
    private void ReadParameter()
    {
        SharedPreferences sharedata = getSharedPreferences("data", 0);
        mWhite = sharedata.getInt("mWhite", 0);
        mRed = sharedata.getInt("mRed", 255);
        mGreen = sharedata.getInt("mGreen", 0);
        mBlue = sharedata.getInt("mBlue", 0);
        mBrightness = sharedata.getInt("mBrightness", 0);
        mBrightness_old = sharedata.getInt("mBrightness_old", 0);

        light_onoff = sharedata.getBoolean("light_onoff", false);
        light_flash = sharedata.getBoolean("light_flash", false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        WriteParameter();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.e(TAG,"RgbActivity onDestroy........");
        WriteParameter();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_dataread:


                LayoutInflater inflater = LayoutInflater.from(this);
                View view = inflater.inflate(R.layout.modelshow, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setView(view);

                builder.setTitle("情景模式");
                builder.setIcon(R.mipmap.ic_launcher);



                modelEating = (ModelView)view.findViewById(R.id.modelEating);
                modelSleeping = (ModelView)view.findViewById(R.id.modelSleeping);
                modelReading = (ModelView)view.findViewById(R.id.modelReading);

                modelEating.setImageResource(R.drawable.eating);
                modelReading.setImageResource(R.drawable.reading);
                modelSleeping.setImageResource(R.drawable.sleeping);

                modelEating.setModelName("进餐模式");
                modelReading.setModelName("阅读模式");
                modelSleeping.setModelName("睡眠模式");

                modelEating.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        mBrightness = 50;
                        mRed = 255;
                        mBlue = 0;
                        mGreen = 202;
                        UpdateAllParameter();
                        UpdateProgress();
                        UpdateText();
                        ToastUtils.makeText(getApplicationContext(),"进餐模式",500);
                    }
                });

                modelReading.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mBrightness = 100;
                        mWhite = 255;
                        mRed = 255;
                        mBlue = 255;
                        mGreen = 255;
                        UpdateAllParameter();
                        UpdateProgress();
                        UpdateText();
                        ToastUtils.makeText(getApplicationContext(), "阅读模式", 500);
                    }
                });

                modelSleeping.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mBrightness = 5;
                        mWhite = 255;
                        mRed = 255;
                        mBlue = 1;
                        mGreen = 192;
                        UpdateAllParameter();
                        UpdateProgress();
                        UpdateText();
                        ToastUtils.makeText(getApplicationContext(), "睡眠模式", 500);
                    }
                });




                /*dialog中的控件,设置自定义AlertDialog，需要在builder.setView(view)后，再进行dialog控件中的findViewById
                * 情况类似于在onCreate方法中必须setContentView 后再进行布局中控件的findViewById,否则会显示空指针异常
                * java.lang.NullPointerException: Attempt to invoke virtual method on a null object reference [duplicate]*/
               /* textView_U = (TextView)view.findViewById(R.id.textView_U);
                textView_I = (TextView)view.findViewById(R.id.textView_I);
                textView_Pnow = (TextView)view.findViewById(R.id.textView_Pnow);
                textView_Psum = (TextView)view.findViewById(R.id.textView_Psum);

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
                });*/



                dataDialog = builder.create();
                dataDialog.show();


        }
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
    }

    //语音识别按钮的触碰监听
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()){

            case MotionEvent.ACTION_DOWN:
                //开始识别
                ret = mSpeechRecognizer.startListening(mRecognizerListener);
                ToastUtils.makeText(getApplicationContext(), "开始识别", 500);
                if(ret != ErrorCode.SUCCESS){
                    LogUtil.e(TAG,"识别失败，错误码："+ret);
                }
                button_voice.setAlpha((float)0.5);

                break;

            case MotionEvent.ACTION_UP:
                //停止识别
                mSpeechRecognizer.stopListening();
                ToastUtils.makeText(getApplicationContext(), "停止识别", 500);
                button_voice.setAlpha((float) 1);

                break;
            case MotionEvent.ACTION_CANCEL:

                ToastUtils.makeText(getApplicationContext(),"识别取消",500);
                break;
            case MotionEvent.ACTION_BUTTON_PRESS:
                break;

        }
        return false;
    }

    //初始化监听器
    private InitListener mInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            LogUtil.e(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS){
                LogUtil.e(TAG,"mInitListener 初始化失败，错误码："+code);
            }
        }
    };

    //云端构建语法监听器
    private GrammarListener mCloudGrammarlistener = new GrammarListener() {
        @Override
        public void onBuildFinish(String grammarId, SpeechError speechError) {
            if (speechError == null){
                String grammarID = new String(grammarId);
                Editor editor = mSharedPreferences.edit();
                if (!TextUtils.isEmpty(grammarId)){
                    editor.putString(KEY_GRAMMAR_ABNF_ID,grammarID);}
                editor.commit();
                LogUtil.e(TAG,"mCloudGrammarlistener 语法构建成功："+grammarId);
            }else {
                LogUtil.e(TAG,"语法构建失败错误码："+speechError.getErrorCode());
            }
        }
    };

    //识别监听器
    private RecognizerListener mRecognizerListener = new RecognizerListener() {
        @Override
        public void onVolumeChanged(int i, byte[] bytes) {
            //在此添加动画
            LogUtil.e(TAG,"返回音频数据："+ bytes.length);
        }
        @Override
        public void onBeginOfSpeech() {}
        @Override
        public void onEndOfSpeech() {}

        @Override
        public void onResult(RecognizerResult recognizerResult, boolean b) {
            LogUtil.e(TAG, "调用了onResult....................");
            if (recognizerResult != null){
                LogUtil.e(TAG ,"recognizer Result:" + recognizerResult.getResultString());
                String text;
                text = JsonParser.parseGramarResult(recognizerResult.getResultString());

                switch(text){
                    case "开灯":
                        break;
                    case "关灯":
                        break;
                     case "求救":
                        break;
                    case "安全":
                        break;
                    case "变亮":
                        break;
                    case "变暗":
                        break;
                    case "进餐":
                        break;
                    case "阅读":
                        break;
                    case "睡觉":
                        break;
                    case "复位":
                        break;
                    default:
                        Toast.makeText(getApplicationContext(),"无法匹配命令词，请重新发送命令",Toast.LENGTH_SHORT).show();
                }
            }else{
                LogUtil.e(TAG,"recognizerResult为null....................");
            }
        }
        @Override
        public void onError(SpeechError speechError) {
            LogUtil.e("mRecognizerListener","出错，错误代码："+speechError.getErrorCode());
            Toast.makeText(getApplicationContext(),"出错，错误代码："+speechError.getErrorCode(),Toast.LENGTH_SHORT).show();
        }
        @Override
        public void onEvent(int i, int i1, int i2, Bundle bundle) {}
    };



    public static synchronized void onCharacteristicRead(BluetoothGatt gatt,
                                                         BluetoothGattCharacteristic characteristic) {

        LogUtil.e(TAG, "onCharacteristicRead str = " + characteristic.getValue()+"");
        if (characteristic != null && gatt != null) {
            if (characteristic.getUuid().toString().equals(DeviceScanActivity.UUID_CHAR9)) {
                dataByte = characteristic.getValue();
                LogUtil.e(TAG, "UUID:"+DeviceScanActivity.UUID_CHAR9+"获得电压值： "+dataByte[0] );
                for (int i = 0; i < dataByte.length; i++) {
                    dataInt[i] = dataByte[i] & 0xFF;
                }
            }  else if(characteristic.getUuid().toString().equals(DeviceScanActivity.UUID_CHAR6)){
                dataByte = characteristic.getValue();

                for (int i = 0; i < dataByte.length; i++) {
                    dataInt[i] = dataByte[i] & 0xFF;
                }
                LogUtil.e(TAG,"UUID:"+DeviceScanActivity.UUID_CHAR6+"获得电压值(BYTE)："+dataByte[0]+","+dataByte[1]+","+dataByte[2]+","+dataByte[3]+","+dataByte[4]+","+dataByte[5]);

            }else{
                //Toast的书写
                LogUtil.e(TAG,"UUID无法匹配");
                return;
            }
        }else{
            LogUtil.e(TAG,"gatt或characteritic为null");
        }


        if (!dataDialog.equals(null)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Message message = new Message();
                    message.what = UPDATE_DIALOGTEXT;
                    mHandler.sendMessage(message);
                }
            }).start();
        }


    }



}





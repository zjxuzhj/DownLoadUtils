package com.hongjay.locallog.service;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Messenger;

/**
 * 日志service
 */
public class LoggerService extends Service {

    //初始化
    public static final int INIT = 0;
    private Messenger mMessenger;
    //关闭service
    public static final int CLOSE_SERVICE = 111;

    //上传日志
    public static final int UPLOAD_LOG = 112;
    //启动ping
    public static final int START_PING = 113;
    //关闭ping
    public static final int STOP_PING = 114;

    @Override
    public IBinder onBind(Intent intent) {
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {

            }
        });
        mMessenger = new Messenger(new LoggerHandler(getApplicationContext()));
        return mMessenger.getBinder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}

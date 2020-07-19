package com.hongjay.locallog.log;


import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.hongjay.locallog.util.JsonUtil;

import java.io.File;

/**
 * 网络请求日志
 */
public class HttpLogger extends Logger {
    private StringBuffer mMessage = new StringBuffer();
    //打印日志进程
    private Messenger mMessenger;
    //网络请求日志
    public static final int HTTP_LOG = 1;

    protected HttpLogger(boolean isOutPut, File logFolder, int saveDays, boolean isPrint, Messenger mMessenger) {
        super(isOutPut, logFolder, saveDays, isPrint);
        this.mMessenger = mMessenger;
    }

    public void setmMessenger(Messenger mMessenger) {
        this.mMessenger = mMessenger;
    }

    @Override
    public void log(String message) {
        synchronized (mMessage) {
            mMessage = formatHttpLog(mMessage, message);
            if (mMessenger != null) {
                Message msg = Message.obtain();
                msg.what = HTTP_LOG;
                Bundle bundle = new Bundle();
                bundle.putString("log", mMessage.toString());
                msg.setData(bundle);
                try {
                    mMessenger.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            } else {
                doLog(mMessage.toString());
            }
            mMessage = new StringBuffer();
        }
    }

    @Override
    public String getLog() {
        return null;
    }

    @Override
    public int getLogType() {
        return HTTP_LOG;
    }

    public static StringBuffer formatHttpLog(StringBuffer mMessage, String message) {
        // 请求或者响应开始
        if (message.startsWith("--> POST")) {
            mMessage.setLength(0);
        }

        // 以{}或者[]形式的说明是响应结果的json数据，需要进行格式化
        if ((message.startsWith("{") && message.endsWith("}"))
                || (message.startsWith("[") && message.endsWith("]"))) {
            message = JsonUtil.formatJson(message);
        }
        mMessage.append(message.concat("\n"));
        // 请求或者响应结束，打印整条日志
        if (message.startsWith("<-- END HTTP")) {
            return mMessage;
        }
        return mMessage;
    }


}

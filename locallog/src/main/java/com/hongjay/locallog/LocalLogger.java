package com.hongjay.locallog;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;

import com.hongjay.locallog.log.HttpLoggingInterceptorBuilder;
import com.hongjay.locallog.log.ServiceLogger;
import com.hongjay.locallog.log.ServiceLoggerBuilder;
import com.hongjay.locallog.model.LoggerConfigModel;
import com.hongjay.locallog.service.LoggerService;

import java.util.concurrent.CountDownLatch;

import okhttp3.logging.HttpLoggingInterceptor;

/**
 * 日志类
 */
public class LocalLogger {
    //service通讯messenger
    private Messenger mMessenger;
    //日志配置信息
    private LoggerConfigModel loggerConfigModel;
    //http日志
    private HttpLoggingInterceptor httpLoggingInterceptor;
    //http请求日志
    private static String httpLogPath = "/http";
    private boolean isInit = false;
    private CountDownLatch countDownLatch;

    private static class LoggerHolder {
        public static LocalLogger builder = new LocalLogger();
    }


    public LocalLogger() {
    }

    /**
     * @param outPut
     * @param print
     * @param saveDays
     * @param catchException
     * @param printStack
     * @param isLogCat
     * @param maxCache       单位M
     * @param context
     * @param rootPath       根目录路径
     */
    public static void init(boolean outPut, boolean print, int saveDays, boolean catchException
            , boolean printStack, boolean isLogCat, int maxCache, String rootPath, Context context) {
        LoggerHolder.builder.loggerConfigModel = new LoggerConfigModel(outPut, print, saveDays, catchException, printStack, isLogCat, maxCache, rootPath);
        LoggerHolder.builder.httpLoggingInterceptor = HttpLoggingInterceptorBuilder.build().print(LoggerHolder.builder.loggerConfigModel.isPrint())
                .outPut(LoggerHolder.builder.loggerConfigModel.isOutPut()).logFolder(rootPath + httpLogPath).saveDays(7)
                .buildInterceptor();
        if (context != null && LoggerHolder.builder.mMessenger == null
                && !LoggerHolder.builder.isInit) {
            LoggerHolder.builder.isInit = true;
            try {
                Intent intent = new Intent(context, LoggerService.class);
                context.bindService(intent, LoggerHolder.builder.mServiceConnection, context.BIND_AUTO_CREATE);
            } catch (Exception e) {

            }
        } else if (LoggerHolder.builder.mMessenger != null) {
            Message msg = Message.obtain();
            msg.what = LoggerService.INIT;
            Bundle bundle = new Bundle();
            bundle.putSerializable("config", LoggerHolder.builder.loggerConfigModel);
            msg.setData(bundle);
            try {
                LoggerHolder.builder.mMessenger.send(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        ServiceLoggerBuilder.build().logFolder(rootPath + "/crash").catchCrash(LoggerHolder.builder.loggerConfigModel.isCatchException())
                .print(true)
                .outPut(true)
                .printStack(true)
                .getServiceLogger();
    }

    public static LocalLogger getLogger() {
        return LoggerHolder.builder;
    }


    /**
     * 加载http拦截器
     * 添加http日志
     *
     * @return
     */
    public HttpLoggingInterceptor getHttpLoggingInterceptor() {
        return LoggerHolder.builder.httpLoggingInterceptor;
    }

    /**
     * 打印业务日志
     * 相同的业务Tag相同，方便后期查找
     * Tag可以使用类名加方法名
     * 同一业务可以加上步骤信息 比如：支付第一步 支付第二步
     *
     * @param tag
     * @param msg
     */
    public void logService(String tag, String msg) {
        if (mMessenger != null) {
            String formatMessage = formatMessage(tag, msg);
            Message message = Message.obtain();
            message.what = ServiceLogger.SERVICE_LOG;
            Bundle bundle = new Bundle();
            try {
                bundle.putString("log", formatMessage);
                message.setData(bundle);
                mMessenger.send(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 调试模式下打印业务日志
     * 非调试模式下不打印
     *
     * @param tag
     * @param msg
     * @param isDebug
     */
    public void debugLogService(String tag, String msg, boolean isDebug) {
        if (isDebug) {
            logService(tag, msg);
        }
    }

    /**
     * 打印业务日志
     * 并上传服务器
     * orderNum tradeNo 必传
     * 没有订单号 传设备类型或者模块类型
     *
     * @param orderNum
     * @param tradeNo
     * @param tag
     * @param msg
     */
    public void logServiceUpload(String orderNum, String tradeNo, String tag, String msg) {
        if (mMessenger != null) {
            String formatMessage = formatMessage(tag, msg);
            Message message = Message.obtain();
            message.what = ServiceLogger.SERVICE_LOG;
            try {
                Bundle bundle = new Bundle();
                bundle.putString("log", formatMessage);
                bundle.putString("orderNum", orderNum);
                bundle.putString("tradeNo", tradeNo);
                bundle.putString("msg", msg);
                message.setData(bundle);
                mMessenger.send(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


    /**
     * 打印异常日志
     *
     * @param e
     * @param msg
     */
    public void logCatch(Throwable e, String msg) {
        if (mMessenger != null) {
            Message message = Message.obtain();
            try {
                mMessenger.send(message);
                message.what = ServiceLoggerBuilder.CATCH_LOG;
                Bundle bundle = new Bundle();
                bundle.putSerializable("e", e);
                bundle.putString("msg", msg);
                message.setData(bundle);
            } catch (Exception re) {
                re.printStackTrace();
            }
        }
    }


    /**
     * 调试模式下打印异常日志
     * 非调试模式下不打印
     *
     * @param e
     * @param msg
     * @param isDebug
     */
    public void debugLogCatch(Throwable e, String msg, boolean isDebug) {
        if (isDebug) {
            logCatch(e, msg);
        }
    }


    /**
     * 打印异常日志并上传
     * orderNum tradeNo 必传
     * 没有订单号 传设备类型或者模块类型
     *
     * @param orderNum
     * @param tradeNo
     * @param e
     * @param msg
     */
    public void logCatchUpload(String orderNum, String tradeNo, Throwable e, String msg) {
        if (mMessenger != null) {
            Message message = Message.obtain();
            message.what = ServiceLoggerBuilder.CATCH_LOG;
            Bundle bundle = new Bundle();
            try {
                bundle.putSerializable("e", e);
                bundle.putString("msg", msg);
                bundle.putString("orderNum", orderNum);
                bundle.putString("tradeNo", tradeNo);
                message.setData(bundle);
                mMessenger.send(message);
            } catch (Exception re) {
                re.printStackTrace();
            }
        }
    }


    /**
     * 格式化日志信息
     *
     * @param tag
     * @param msg
     * @return
     */
    private String formatMessage(String tag, String msg) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("TAG:");
        stringBuffer.append(tag);
        stringBuffer.append("\n\n");
        stringBuffer.append(msg);
        return stringBuffer.toString();
    }


    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mMessenger = new Messenger(service);
            Message msg = Message.obtain();
            msg.what = LoggerService.INIT;
            Bundle bundle = new Bundle();
            bundle.putSerializable("config", loggerConfigModel);
            msg.setData(bundle);
            try {
                mMessenger.send(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
            HttpLoggingInterceptorBuilder.build().setMessenger(mMessenger);

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
//            Intent intent = new Intent(context, LoggerService.class);
//            context.bindService(intent, LoggerHolder.builder.mServiceConnection, context.BIND_AUTO_CREATE);
//            mBound = false;
            LoggerHolder.builder.isInit = false;
        }
    };


    /**
     * 销毁
     */
    public void deStroy(Context context) {
        if (mMessenger != null) {
            Message msg = Message.obtain();
            msg.what = LoggerService.CLOSE_SERVICE;
            try {
                mMessenger.send(msg);
                context.unbindService(mServiceConnection);
            } catch (Exception e) {
                e.printStackTrace();
            }
            mMessenger = null;
        }
        HttpLoggingInterceptorBuilder.build().setMessenger(null);
        httpLoggingInterceptor = null;

    }

    /**
     * 停止上传
     */
    public static void stopUpload() {
        if (LoggerHolder.builder.countDownLatch == null
                || LoggerHolder.builder.countDownLatch.getCount() == 0) {
            LoggerHolder.builder.countDownLatch = new CountDownLatch(1);
        }
    }

    /**
     * 上传日志
     */
    public void uploadLog() {
        if (mMessenger != null) {
            Message message = Message.obtain();
            message.what = LoggerService.UPLOAD_LOG;
            try {
                mMessenger.send(message);
            } catch (Exception re) {
                re.printStackTrace();
            }
        }
    }


}

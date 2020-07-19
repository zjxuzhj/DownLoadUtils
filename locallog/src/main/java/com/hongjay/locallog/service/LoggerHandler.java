package com.hongjay.locallog.service;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.hongjay.locallog.log.HttpLogger;
import com.hongjay.locallog.log.LogUtil;
import com.hongjay.locallog.log.ServiceLogger;
import com.hongjay.locallog.log.ServiceLoggerBuilder;
import com.hongjay.locallog.model.LoggerConfigModel;
import com.hongjay.locallog.util.FileSizeUtil;
import com.hongjay.locallog.util.TimeFormatUtil;

import java.io.File;
import java.io.IOException;

public class LoggerHandler extends Handler {

    //日志配置信息
    private LoggerConfigModel loggerConfigModel;
    //业务日志
    private ServiceLogger serviceLogger;
    //异常捕捉日志
    private ServiceLogger catchLogger;
    //logcat日志目录
    public final static String logCatPath = "/logcat";
    //日志根目录
    public static String rootDir = Environment.getExternalStorageDirectory()
            .getAbsolutePath() + "/";
    //http请求日志
    public final static String httpLogPath = "/http";
    //service请求日志
    public final static String serviceLogPath = "/service";
    //crash请求日志
    public final static String crashLogPath = "/crash";
    private HandlerThread handlerThread;
    //日志存放文件夹
    private String[] paths = new String[4];
    //网络请求日志
    private ServiceLogger httpLogger;
    private Handler handler;
    //网络请求日志
    private StringBuffer stringBuffer = new StringBuffer();
    private Context context;

    public LoggerHandler(Context context) {
        this.context = context;
    }

    @Override
    public void handleMessage(Message message) {
        Bundle bundle = message.getData();
        if (bundle != null) {
            String formatMessage = bundle.get("log") != null ? (String) bundle.get("log") : null;
            String msg = bundle.get("msg") != null ? (String) bundle.get("msg") : null;
            Throwable e = bundle.getSerializable("e") != null ? (Throwable) bundle.getSerializable("e") : null;
            super.handleMessage(message);
            switch (message.what) {
                case LoggerService.INIT:
                    loggerConfigModel = (LoggerConfigModel) bundle.get("config");
                    initLogger();
                    break;
                case ServiceLogger.SERVICE_LOG:
                    if (serviceLogger != null) {
                        serviceLogger.log(formatMessage);
                    }
                    break;
                case HttpLogger.HTTP_LOG:
                    clearLogcatFile();
                    stringBuffer.append(formatMessage);
                    if (stringBuffer.toString().contains("END")) {
                        httpLogger.log(stringBuffer.toString());
                        stringBuffer.setLength(0);
                    }
                    break;
                case ServiceLoggerBuilder.CATCH_LOG:
                    if (catchLogger != null) {
                        catchLogger.log(e, msg);
                    }
                    break;
                case LoggerService.CLOSE_SERVICE:
                    android.os.Process.killProcess(android.os.Process.myPid());
                    break;
            }
        }
    }


    /**
     * 日志初始化
     */
    private void initLogger() {
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });
        rootDir = Environment.getExternalStorageDirectory()
                .getAbsolutePath() + "/" + loggerConfigModel.getRootPath();
        File file = new File(rootDir);
        if (!file.exists()) {
            file.mkdirs();
        } else if (!file.isDirectory()) {
            file.delete();
            file.mkdirs();
        }
        if (loggerConfigModel != null && loggerConfigModel.isLogCat()) {
            File logFile = new File(rootDir + logCatPath);
            if (!logFile.exists()) {
                logFile.mkdirs();
            }
            logFile = new File(rootDir + logCatPath + "/" + TimeFormatUtil.getNday(0) + ".txt");
            if (!logFile.exists()) {
                try {
                    logFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                Runtime.getRuntime().exec("logcat -f " + logFile.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            LogUtil.deleteLogFile(rootDir + logCatPath);
        }
        if (serviceLogger == null) {
            serviceLogger = ServiceLoggerBuilder.build().saveDays(loggerConfigModel.getSaveDays()).outPut(loggerConfigModel.isOutPut()).
                    print(loggerConfigModel.isPrint()).
                    catchCrash(loggerConfigModel.isCatchException()).printStack(loggerConfigModel.isPrintStack())
                    .logFolder(loggerConfigModel.getRootPath() + serviceLogPath).getServiceLogger();
        } else {
            ServiceLoggerBuilder.build().logFolder(loggerConfigModel.getRootPath() + serviceLogPath);
        }
        if (catchLogger == null) {
            catchLogger = ServiceLoggerBuilder.build().logFolder(loggerConfigModel.getRootPath() + crashLogPath).getServiceLogger();
        } else {
            ServiceLoggerBuilder.build().logFolder(loggerConfigModel.getRootPath() + crashLogPath);
        }
        if (httpLogger == null) {
            httpLogger = ServiceLoggerBuilder.build().saveDays(loggerConfigModel.getSaveDays()).outPut(loggerConfigModel.isOutPut()).
                    print(loggerConfigModel.isPrint()).
                    catchCrash(false).printStack(false)
                    .logFolder(loggerConfigModel.getRootPath() + httpLogPath).getServiceLogger();
        } else {
            ServiceLoggerBuilder.build().logFolder(loggerConfigModel.getRootPath() + httpLogPath);
        }
        paths[0] = rootDir + httpLogPath;
        paths[1] = rootDir + logCatPath;
        paths[2] = rootDir + crashLogPath;
        paths[3] = rootDir + serviceLogPath;
        checkMaxCache(rootDir, paths);
        clearLogcatFile();
        handlerThread = new HandlerThread("check");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //每过一段时间检验一次内存
                checkMaxCache(rootDir, paths);
                handler.postDelayed(this, 50 * 1000 * 60);
            }
        }, 50 * 1000 * 60);
    }


    /**
     * 定期清理logcat日志
     */
    private void clearLogcatFile() {
        if (loggerConfigModel != null) {
            File oldFile = new File(Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + logCatPath + "/" + TimeFormatUtil.getNday(0 - loggerConfigModel.getSaveDays()) + ".txt");
            if (oldFile.exists()) {
                oldFile.delete();
            }
        }
    }


    /**
     * 检测日志容量是否超过最大值
     */
    private void checkMaxCache(String rootPath, String[] deletePaths) {
        if (deletePaths != null) {
            double size = FileSizeUtil.getFileOrFilesSize(rootPath, FileSizeUtil.SIZETYPE_MB);
            if (size > loggerConfigModel.getMaxCache()) {
                for (int i = 0; i < deletePaths.length; i++) {
                    LogUtil.deleteLogFile(deletePaths[i]);
                }
            }
        }

    }


}
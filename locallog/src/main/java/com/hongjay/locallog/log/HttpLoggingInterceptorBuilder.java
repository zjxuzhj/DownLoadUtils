package com.hongjay.locallog.log;

import android.os.Environment;
import android.os.Messenger;

import java.io.File;

import okhttp3.logging.HttpLoggingInterceptor;

/**
 * 网络请求日志构造器
 */
public class HttpLoggingInterceptorBuilder {
    //请求拦截器
    private HttpLoggingInterceptor httpLoggingInterceptor;
    //是否打印日志
    private boolean isPrint;
    //是否输出到本地文件
    private boolean isOutPut;
    //输出的文件日志保存多少天
    private int saveDays;
    //日志文件夹路径
    private File logFolder;
    //默认的日志路径
    private static final String DEFAULT_LOG_FOLDER_PATH = "httpLog";
    //打印日志进程
    private Messenger mMessenger;

    private HttpLogger httpLogger;

    private static class HttpLoggingInterceptorBuilderHolder {
        public static HttpLoggingInterceptorBuilder builder = new HttpLoggingInterceptorBuilder();
    }

    public static HttpLoggingInterceptorBuilder build() {
        return HttpLoggingInterceptorBuilderHolder.builder;
    }

    /**
     * 是否打印日志
     *
     * @param isPrint
     */
    public HttpLoggingInterceptorBuilder print(boolean isPrint) {
        this.isPrint = isPrint;
        return build();
    }


    /**
     * 是否输出日志到文件
     *
     * @param isOutPut
     * @return
     */
    public HttpLoggingInterceptorBuilder outPut(boolean isOutPut) {
        this.isOutPut = isOutPut;
        return build();
    }

    /**
     * 输出的文件日志保存多少天
     *
     * @param saveDays
     * @return
     */
    public HttpLoggingInterceptorBuilder saveDays(int saveDays) {
        this.saveDays = saveDays;
        return build();
    }


    /**
     * 日志文件存放的文件夹
     *
     * @param folderPath
     * @return
     */
    public HttpLoggingInterceptorBuilder logFolder(String folderPath) {
        logFolder = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath() + "/" + folderPath);
        if (!logFolder.mkdir()) {
            if (!logFolder.exists())
                logFolder = null;
        }
        return build();
    }

    /**
     * 设置多进程打印日志
     *
     * @param mMessenger
     */
    public HttpLoggingInterceptorBuilder setMessenger(Messenger mMessenger) {
        this.mMessenger = mMessenger;
        if (httpLogger != null)
            httpLogger.setmMessenger(mMessenger);
        return build();
    }

    /**
     * 创建拦截器
     *
     * @return
     */
    public HttpLoggingInterceptor buildInterceptor() {
        if (logFolder == null) {
            logFolder(DEFAULT_LOG_FOLDER_PATH);
        }
        httpLogger = new HttpLogger(isOutPut, logFolder, saveDays, isPrint, mMessenger);
        HttpLoggingInterceptor logInterceptor = new HttpLoggingInterceptor(httpLogger);
        logInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return logInterceptor;
    }


}
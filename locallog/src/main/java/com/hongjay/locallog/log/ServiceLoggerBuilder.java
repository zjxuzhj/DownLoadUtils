package com.hongjay.locallog.log;


import android.os.Environment;

import com.hongjay.locallog.util.TimeFormatUtil;

import java.io.File;

/**
 * 业务日志构造器
 */
public class ServiceLoggerBuilder {
    //是否打印日志
    private boolean isPrint;
    //是否输出到本地文件
    private boolean isOutPut;
    //输出的文件日志保存多少天
    private int saveDays;
    //日志文件夹路径
    private File logFolder;
    //默认的日志路径
    private static final String DEFAULT_LOG_FOLDER_PATH = "serviceLog";
    //是否打印堆栈信息
    private boolean isPrintStack;
    private ServiceLogger serviceLogger;
    private StringBuilder mMessage = new StringBuilder();
    //默认的崩溃日志目录
    private static final String DEFAULT_CRASH_LOG_FOLDER_PATH = "hongjay/crash";
    private ServiceLogger crashLogger;
    //异常日志类型
    public static final int CATCH_LOG = 3;

    private static class HttpLoggingInterceptorBuilderHolder {
        public static ServiceLoggerBuilder builder = new ServiceLoggerBuilder();
    }

    public static ServiceLoggerBuilder build() {
        return HttpLoggingInterceptorBuilderHolder.builder;
    }

    /**
     * 是否打印日志
     *
     * @param isPrint
     */
    public ServiceLoggerBuilder print(boolean isPrint) {
        this.isPrint = isPrint;
        return build();
    }

    /**
     * 是否打印堆栈信息
     *
     * @param isPrintStack
     * @return
     */
    public ServiceLoggerBuilder printStack(boolean isPrintStack) {
        this.isPrintStack = isPrintStack;
        return build();
    }


    /**
     * 是否输出日志到文件
     *
     * @param isOutPut
     * @return
     */
    public ServiceLoggerBuilder outPut(boolean isOutPut) {
        this.isOutPut = isOutPut;
        return build();
    }

    /**
     * 输出的文件日志保存多少天
     *
     * @param saveDays
     * @return
     */
    public ServiceLoggerBuilder saveDays(int saveDays) {
        this.saveDays = saveDays;
        return build();
    }


    /**
     * 日志文件存放的文件夹
     *
     * @param folderPath
     * @return
     */
    public ServiceLoggerBuilder logFolder(String folderPath) {
        logFolder = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath() + "/" + folderPath);
        serviceLogger = null;
        if (!logFolder.mkdir()) {
            if (!logFolder.exists())
                logFolder = null;
        }
        return build();
    }

    /**
     * 获取日志
     *
     * @return
     */
    public ServiceLogger getServiceLogger() {
        if (serviceLogger == null) {
            if (logFolder == null) {
                logFolder(DEFAULT_LOG_FOLDER_PATH);
            }
            serviceLogger = new ServiceLogger(isOutPut, logFolder, saveDays, isPrint, isPrintStack);
        }
        return serviceLogger;
    }

    /**
     * 设置崩溃异常捕捉
     *
     * @param isCatchCrash
     * @return
     */
    public ServiceLoggerBuilder catchCrash(boolean isCatchCrash) {
        if (isCatchCrash && crashLogger == null) {
            if (saveDays == 0) {
                saveDays = 45;
            }
            crashLogger = new ServiceLogger(true, logFolder, saveDays, true, false);
            Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread t, Throwable e) {
                    crashLogger.doLog(getMessage(e));
                    System.exit(0);
                }
            });
        }
        return build();
    }

    /**
     * 获取异常信息
     *
     * @param e
     * @return
     */
    private String getMessage(Throwable e) {
        mMessage.delete(0, mMessage.length());
        mMessage.setLength(0);
        StackTraceElement[] stackElements = e.getStackTrace();
        if (stackElements != null) {
            mMessage.append("\n\n\n");
            mMessage.append("========================================\n\n");
            mMessage.append("崩溃时间：" + TimeFormatUtil.timeFormat(System.currentTimeMillis()));
            mMessage.append("\n\n========================================\n\n");
            if (e.getCause() != null) {
                mMessage.append("崩溃信息：" + e.getCause().getMessage());
                mMessage.append("\n\n========================================\n\n");
            }
            mMessage.append("堆栈信息：\n");
            for (int i = 0; i < stackElements.length; i++) {
                mMessage.append("ClassName：" + stackElements[i].getClassName() + "\t\t|\t\t");
                mMessage.append("java文件名：" + stackElements[i].getFileName() + "\t\t|\t\t");
                mMessage.append("所在行数：" + stackElements[i].getLineNumber() + "\t\t|\t\t");
                mMessage.append("所在方法名：" + stackElements[i].getMethodName() + "\n\n\n");
            }
            mMessage.append("\n\n========================================");
            mMessage.append("\n\n\n");
        }
        return mMessage.toString();
    }
}
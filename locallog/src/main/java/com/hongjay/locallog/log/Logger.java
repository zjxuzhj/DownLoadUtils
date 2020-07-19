package com.hongjay.locallog.log;


import com.hongjay.locallog.util.TimeFormatUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import okhttp3.logging.HttpLoggingInterceptor;

/**
 * 日志输出类
 */
public abstract class Logger implements HttpLoggingInterceptor.Logger {
    //是否打印日志
    private boolean isOutPut;
    //日志存放文件夹
    private File logFolder;
    //日志保存天数
    private int saveDays;
    //是否打印日志
    private boolean isPrint;
    private File logFile;

    public Logger(boolean isOutPut, File logFolder, int saveDays, boolean isPrint) {
        this.isOutPut = isOutPut;
        if (logFolder != null) {
            this.logFolder = new File(logFolder.getAbsolutePath());
        }
        this.saveDays = saveDays;
        this.isPrint = isPrint;
        LogUtil.init(isPrint);
    }

    @Override
    public abstract void log(String message);

    public abstract String getLog();

    /**
     * 获取日志类型
     *
     * @return
     */
    public abstract int getLogType();

    /**
     * 打印日志
     *
     * @param message
     */
    protected synchronized void doLog(String message) {
        if (isPrint) {
            LogUtil.d(message);
        }
        if (isOutPut) {
            if (logFile == null || !logFile.getPath().contains(getLogFileName(0))) {
                logFile = new File(getLogFileName(0));
            }
            try {
                if (!logFile.exists()) {
                    logFile.createNewFile();
                }
                FileWriter fileWriter = new FileWriter(logFile, true);
                BufferedWriter bw = new BufferedWriter(fileWriter
                );
                bw.write(message);
                bw.close();
                fileWriter.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (saveDays != 0) {
                File oldFile = new File(getLogFileName(0 - saveDays));
                if (oldFile.exists()) {
                    oldFile.delete();
                }
            }
        }
    }


    /**
     * 获取日志文件名称
     *
     * @param date
     * @return
     */
    private String getLogFileName(int date) {
        if (logFolder == null) {
            return "";
        }
        return logFolder.getAbsolutePath() + "/" + TimeFormatUtil.getNday(date) + ".txt";

    }
}
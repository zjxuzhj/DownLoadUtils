package com.hongjay.locallog.log;

import android.os.Environment;
import android.util.Log;

import com.hongjay.locallog.logger.ButLogger;
import com.hongjay.locallog.logger.LogLevel;
import com.hongjay.locallog.service.LoggerHandler;
import com.hongjay.locallog.util.TimeFormatUtil;
import com.hongjay.locallog.util.ZipUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Log日志工具，封装logger
 */
public class LogUtil {
    /**
     * 初始化log工具，在app入口处调用
     *
     * @param isLogEnable 是否打印log
     */
    public static void init(boolean isLogEnable) {
        ButLogger.init("melot")
                .hideThreadInfo()
                .logLevel(isLogEnable ? LogLevel.FULL : LogLevel.NONE)
                .methodOffset(2);
    }

    public static void d(String message) {
        ButLogger.d(message);
    }

    public static void d(String tag, String message) {
        ButLogger.setTag(tag);
        ButLogger.d(message);
    }

    /**
     * 使用android原生log，不打印堆栈，但是还是会遵循是否在release下打印
     *
     * @param message
     */
    public static void dd(String tag, String message) {
        if (ButLogger.getSetting().isLogEnable()) {
            Log.d(tag, message);
        }
    }

    public static void i(String message) {
        ButLogger.i(message);
    }

    public static void i(String tag, String message) {
        ButLogger.setTag(tag);
        ButLogger.i(message);
    }

    public static void ii(String tag, String message) {
        if (ButLogger.getSetting().isLogEnable()) {
            Log.i(tag, message);
        }
    }

    public static void w(String message, Throwable e) {
        String info = e != null ? e.toString() : "null";
        ButLogger.w(message + "：" + info);
    }

    public static void w(String tag, String message, Throwable e) {
        String info = e != null ? e.toString() : "null";
        ButLogger.t(tag).w(message + "：" + info);
    }

    public static void ww(String tag, String message) {
        if (ButLogger.getSetting().isLogEnable()) {
            Log.w(tag, message);
        }
    }

    public static void e(String message, Throwable e) {
        ButLogger.e(e, message);
    }

    public static void e(String tag, String message, Throwable e) {
        ButLogger.setTag(tag);
        ButLogger.e(e, message);
    }

    public static void ee(String tag, String message, Throwable e) {
        if (ButLogger.getSetting().isLogEnable()) {
            Log.e(tag, message, e);
        }
    }

    public static void json(String json) {
        ButLogger.json(json);
    }

    public static void json(String tag, String json) {
        ButLogger.setTag(tag);
        ButLogger.json(json);
    }

    /**
     * 压缩日志文件
     *
     * @return
     */
    public static File zipLog(String rootPath, int maxUpload) {
        File file = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath() + "/" + rootPath + "/log.zip");
        if (file.exists()) {
            file.delete();
        }
        String zipPath = Environment.getExternalStorageDirectory()
                .getAbsolutePath() + "/" + rootPath + "/log_zip";
        try {
            copyLogPathToZip(zipPath, maxUpload, Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + "/" + rootPath);
            ZipUtils.ZipFolder(zipPath, file.getAbsolutePath());
            deleteLogFile(zipPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }


    /**
     * 拷贝要上传的日志文件
     */
    private static void copyLogPathToZip(String zipPath, int maxUpload, String rootDir) {
        try {
            File zipLog = new File(zipPath);
            if (!zipLog.exists()) {
                zipLog.mkdir();
            }
            int day = 7;
            long length = 0;
            boolean isFrist = true;
            while ((length > maxUpload * 1024 * 1024
                    && day > 0) || isFrist) {
                length = 0;
                if (!isFrist) {
                    day = day - 1;
                }
                isFrist = false;
                for (int i = 0; i < day; i++) {
                    File serviceLog = new File(rootDir + "/" + LoggerHandler.serviceLogPath + "/" + TimeFormatUtil.getNday(i) + ".txt");
                    if (serviceLog.exists()) {
                        length = length + serviceLog.length();
                    }

                    File logcatLog = new File(rootDir + "/" + LoggerHandler.logCatPath + "/" + TimeFormatUtil.getNday(i) + ".txt");
                    if (logcatLog.exists()) {
                        length = length + logcatLog.length();
                    }

                    File httpLog = new File(rootDir + "/" + LoggerHandler.httpLogPath + "/" + TimeFormatUtil.getNday(i) + ".txt");
                    if (httpLog.exists()) {
                        length = length + httpLog.length();
                    }

                    File crashLog = new File(rootDir + "/" + LoggerHandler.crashLogPath + "/" + TimeFormatUtil.getNday(i) + ".txt");
                    if (crashLog.exists()) {
                        length = length + crashLog.length();
                    }
                }
            }

            for (int i = 0; i < day; i++) {
                File serviceLog = new File(rootDir + "/" + LoggerHandler.serviceLogPath + "/" + TimeFormatUtil.getNday(0 - i) + ".txt");
                if (serviceLog.exists()) {
                    File newFile = new File(zipPath + "/service_" + TimeFormatUtil.getNday(0 - i) + ".txt");
                    if (!newFile.exists()) {
                        newFile.createNewFile();
                    }
                    copyFileUsingFileStreams(serviceLog, newFile);
                }

                File logcatLog = new File(rootDir + "/" + LoggerHandler.logCatPath + "/" + TimeFormatUtil.getNday(0 - i) + ".txt");
                if (logcatLog.exists()) {
                    File newFile = new File(zipPath + "/logcat_" + TimeFormatUtil.getNday(0 - i) + ".txt");
                    if (!newFile.exists()) {
                        newFile.createNewFile();
                    }
                    copyFileUsingFileStreams(logcatLog, newFile);
                }

                File httpLog = new File(rootDir + "/" + LoggerHandler.httpLogPath + "/" + TimeFormatUtil.getNday(0 - i) + ".txt");
                if (httpLog.exists()) {
                    File newFile = new File(zipPath + "/http_" + TimeFormatUtil.getNday(0 - i) + ".txt");
                    if (!newFile.exists()) {
                        newFile.createNewFile();
                    }
                    copyFileUsingFileStreams(httpLog, newFile);
                }

                File crashLog = new File(rootDir + "/" + LoggerHandler.crashLogPath + "/" + TimeFormatUtil.getNday(0 - i) + ".txt");
                if (crashLog.exists()) {
                    File newFile = new File(zipPath + "/crash_" + TimeFormatUtil.getNday(0 - i) + ".txt");
                    if (!newFile.exists()) {
                        newFile.createNewFile();
                    }
                    copyFileUsingFileStreams(crashLog, newFile);
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 拷贝文件
     *
     * @param source
     * @param dest
     */
    private static void copyFileUsingFileStreams(File source, File dest)
            throws IOException {
        InputStream input = null;
        OutputStream output = null;
        try {
            input = new FileInputStream(source);
            output = new FileOutputStream(dest);
            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = input.read(buf)) > 0) {
                output.write(buf, 0, bytesRead);
            }
        } finally {
            input.close();
            output.close();
        }
    }


    /**
     * 删除日志文件
     *
     * @param deletePath
     */
    public static void deleteLogFile(String deletePath) {
        File file = new File(deletePath);
        if (file != null) {
            if (file.listFiles() != null && file.listFiles().length > 0) {
                File[] files = file.listFiles();
                if (files.length > 0 && !files[0].equals(deletePath)) {
                    for (int i = 0; i < files.length; i++) {
                        File child = files[i];
                        child.delete();
                    }
                }
            }
        }
    }

}

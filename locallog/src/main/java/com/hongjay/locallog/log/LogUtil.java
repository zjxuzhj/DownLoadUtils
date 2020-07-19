package com.hongjay.locallog.log;

import android.os.Environment;
import com.hongjay.locallog.service.LoggerHandler;
import com.hongjay.locallog.util.TimeFormatUtil;
import com.hongjay.locallog.util.ZipUtils;
import com.orhanobut.logger.LogLevel;
import com.orhanobut.logger.Logger;

import java.io.*;

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
        Logger.init("LogHttpInfo")
                .hideThreadInfo()
                .logLevel(isLogEnable ? LogLevel.FULL : LogLevel.NONE)
                .methodOffset(2);
    }

    public static void d(String message) {
        Logger.d(message);
    }

    public static void i(String message) {
        Logger.i(message);
    }

    public static void w(String message, Throwable e) {
        String info = e != null ? e.toString() : "null";
        Logger.w(message + "：" + info);
    }

    public static void e(String message, Throwable e) {
        Logger.e(e, message);
    }

    public static void json(String json) {
        Logger.json(json);
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

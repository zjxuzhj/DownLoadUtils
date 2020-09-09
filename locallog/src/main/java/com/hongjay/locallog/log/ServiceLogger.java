package com.hongjay.locallog.log;


import com.hongjay.locallog.util.TimeFormatUtil;

import java.io.File;
import java.util.HashMap;

/**
 * 业务日志
 */
public class ServiceLogger extends Logger {
    private StringBuffer mMessage = new StringBuffer();
    private boolean isPrintStack;
    private HashMap<String, String> stackMap;
    private String log = null;
    //service日志
    public static final int SERVICE_LOG = 2;

    protected ServiceLogger(boolean isOutPut, File logFolder, int saveDays, boolean isPrint, boolean isPrintStack) {
        super(isOutPut, logFolder, saveDays, isPrint);
        this.isPrintStack = isPrintStack;
        stackMap = new HashMap<>();
    }

    @Override
    public void log(String message) {
        synchronized (mMessage) {
            log = null;
            mMessage.setLength(0);
            doLog("\n\n\n");
            mMessage.append("=========================\n\n");
            mMessage.append("时间：" + TimeFormatUtil.timeFormat(System.currentTimeMillis()) + "\n");
            if (isPrintStack) {
                StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
                StringBuilder builder = getStackMessage(stackTraceElements);
                mMessage.append("=========================\n\n");
                mMessage.append("堆栈信息：\n");
                mMessage.append(builder);
                mMessage.append("\n\n=========================\n\n");
            } else {
                mMessage.append("=========================\n\n");
            }
            mMessage.append("日志信息：\n");
            mMessage.append(message);
            doLog(mMessage.toString());
            log = mMessage.toString();
            doLog("\n\n=========================");
            doLog("\n\n\n");
            mMessage = new StringBuffer();
        }
    }


    /**
     * 打印异常
     *
     * @param e
     * @param message
     */
    public void log(Throwable e, String message) {
        synchronized (mMessage) {
            log = null;
            StackTraceElement[] stackTraceElements = e.getStackTrace();
            StringBuilder builder = getStackMessage(stackTraceElements);
            if (stackMap.get(builder.toString()) != null) {
                return;
            }
            mMessage.append("=========================\n\n");
            mMessage.append("时间：" + TimeFormatUtil.timeFormat(System.currentTimeMillis()) + "\n");
            stackMap.put(builder.toString(), message);
            mMessage.append("=========================\n\n");
            mMessage.append("堆栈信息：\n");
            mMessage.append(builder);
            mMessage.append("=========================\n\n");
            mMessage.append("日志信息：\n\t");
            mMessage.append(message.concat("\n\n\n"));
            doLog(mMessage.toString());
            log = mMessage.toString();
            mMessage.delete(0, mMessage.length());
            doLog("\n\n=========================");
            doLog("\n\n\n");
        }
    }

    @Override
    public String getLog() {
        return log;
    }

    @Override
    public int getLogType() {
        return SERVICE_LOG;
    }


    /**
     * 获取堆栈信息
     *
     * @param stackTraceElements
     * @return
     */
    private StringBuilder getStackMessage(StackTraceElement[] stackTraceElements) {
        StringBuilder stackString = new StringBuilder();
        for (int i = 0; i < stackTraceElements.length; i++) {
            stackString.append(stackTraceElements[i].getClassName());
            stackString.append("\t|\t");
            stackString.append(stackTraceElements[i].getMethodName());
            stackString.append("\t|\t");
            stackString.append(stackTraceElements[i].getLineNumber());
            stackString.append("\n\n");
        }
        return stackString;
    }
}

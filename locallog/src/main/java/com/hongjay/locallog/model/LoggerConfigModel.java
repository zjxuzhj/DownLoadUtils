package com.hongjay.locallog.model;


import java.io.Serializable;

/**
 * 日志配置信息
 */
public class LoggerConfigModel implements Serializable {
    //是否输出到控制台
    private boolean outPut;
    //是否打印到本地文件
    private boolean print;
    //保存时间
    private int saveDays;
    //是否捕捉异常
    private boolean catchException;
    //是否打印堆栈信息
    private boolean printStack;
    //是否保存logcat日志
    private boolean isLogCat;
    //日志文件最大容量
    private int maxCache;
    //根目录名称
    private String rootPath;


    public LoggerConfigModel(boolean outPut, boolean print, int saveDays, boolean catchException, boolean printStack, boolean isLogCat, int maxCache, String rootPath) {
        this.outPut = outPut;
        this.print = print;
        this.saveDays = saveDays;
        this.catchException = catchException;
        this.printStack = printStack;
        this.isLogCat = isLogCat;
        this.maxCache = maxCache;
        this.rootPath = rootPath;
    }

    public boolean isOutPut() {
        return outPut;
    }

    public void setOutPut(boolean outPut) {
        this.outPut = outPut;
    }

    public boolean isPrint() {
        return print;
    }

    public void setPrint(boolean print) {
        this.print = print;
    }

    public int getSaveDays() {
        return saveDays;
    }

    public void setSaveDays(int saveDays) {
        this.saveDays = saveDays;
    }

    public boolean isCatchException() {
        return catchException;
    }

    public void setCatchException(boolean catchException) {
        this.catchException = catchException;
    }

    public boolean isPrintStack() {
        return printStack;
    }

    public void setPrintStack(boolean printStack) {
        this.printStack = printStack;
    }

    public boolean isLogCat() {
        return isLogCat;
    }

    public void setLogCat(boolean logCat) {
        isLogCat = logCat;
    }

    public int getMaxCache() {
        return maxCache;
    }

    public void setMaxCache(int maxCache) {
        this.maxCache = maxCache;
    }

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }
}
package com.zhj.downloadutils.event;

import java.io.File;

/**
 * Created by HongJay on 2018/7/23.
 */
public class InstallEvent {
    private File file;

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public InstallEvent(File file) {
        this.file = file;
    }
}

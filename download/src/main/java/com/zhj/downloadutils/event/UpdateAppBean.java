package com.zhj.downloadutils.event;

import java.io.Serializable;

/**
 * 版本信息
 */
public class UpdateAppBean implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * update : Yes
     * new_version : xxxxx
     * apk_url : http://cdn.the.url.of.apk/or/patch
     * need_update_log : xxxx
     * delta : false
     * new_md5 : xxxxxxxxxxxxxx
     * target_size : 601132
     */
    //是否有新版本
    private String update;
    //新版本号
    private String new_version;
    //新app下载地址
    private String apk_file_url;
    //更新日志
    private String need_update_log;
    //必须更新的更新日志
    private String mustUpdateLog;
    //配置默认更新dialog 的title
    private String update_def_dialog_title;
    //新app大小
    private String target_size;
    //是否强制更新
    private boolean constraint;
    //md5
    private String new_md5;

    /**********以下是内部使用的数据**********/

    private String targetPath;
    private boolean mHideDialog;
    private boolean mShowIgnoreVersion;
    private boolean mDismissNotificationProgress;

    //是否隐藏对话框下载进度条,内部使用
    public boolean isHideDialog() {
        return mHideDialog;
    }

    public void setHideDialog(boolean hideDialog) {
        mHideDialog = hideDialog;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath;
    }

    public boolean isConstraint() {
        return constraint;
    }

    public UpdateAppBean setConstraint(boolean constraint) {
        this.constraint = constraint;
        return this;
    }

    public String getNewVersion() {
        return new_version;
    }

    public UpdateAppBean setNewVersion(String new_version) {
        this.new_version = new_version;
        return this;
    }

    public String getApkFileUrl() {
        return apk_file_url;
    }


    public UpdateAppBean setApkFileUrl(String apk_file_url) {
        this.apk_file_url = apk_file_url;
        return this;
    }

    public String getNeedUpdateLog() {
        return need_update_log;
    }

    public UpdateAppBean setNeedUpdateLog(String update_log) {
        this.need_update_log = update_log;
        return this;
    }

    public String getUpdateDefDialogTitle() {
        return update_def_dialog_title;
    }

    public UpdateAppBean setUpdateDefDialogTitle(String updateDefDialogTitle) {
        this.update_def_dialog_title = updateDefDialogTitle;
        return this;
    }

    public String getNewMd5() {
        return new_md5;
    }

    public UpdateAppBean setNewMd5(String new_md5) {
        this.new_md5 = new_md5;
        return this;
    }

    public String getTargetSize() {
        return target_size;
    }

    public UpdateAppBean setTargetSize(String target_size) {
        this.target_size = target_size;
        return this;
    }

    public boolean isShowIgnoreVersion() {
        return mShowIgnoreVersion;
    }

    public void showIgnoreVersion(boolean showIgnoreVersion) {
        mShowIgnoreVersion = showIgnoreVersion;
    }

    public void dismissNotificationProgress(boolean dismissNotificationProgress) {
        mDismissNotificationProgress = dismissNotificationProgress;
    }

    public boolean isDismissNotificationProgress() {
        return mDismissNotificationProgress;
    }

    public String getMustUpdateLog() {
        return mustUpdateLog;
    }

    public void setMustUpdateLog(String mustUpdateLog) {
        this.mustUpdateLog = mustUpdateLog;
    }
}

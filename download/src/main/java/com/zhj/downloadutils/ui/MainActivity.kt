package com.zhj.downloadutils.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import com.tbruyelle.rxpermissions2.RxPermissions
import com.zhj.downloadutils.R
import com.zhj.downloadutils.event.InstallEvent
import com.zhj.downloadutils.event.UpdateAppBean
import com.zhj.downloadutils.tools.AppUpdateUtils
import com.zhj.downloadutils.tools.DownLoadUtils
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MainActivity : AppCompatActivity() {
    private var mContext: Context? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        EventBus.getDefault().register(this)//注册 Eventbus
        mContext = this
        checkUpdate()
    }

    private fun checkUpdate() {
        //设置apk 的保存路径
        var path = ""
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED || !Environment.isExternalStorageRemovable()) {
            try {
                path = externalCacheDir!!.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (TextUtils.isEmpty(path)) {
                path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
            }
        } else {
            path = cacheDir.absolutePath
        }

        var message = "1. 适配竖屏战报带来的显示效果的影响，需要打开大图才能查看完整战报\n" +
                "2. 修复华为手机或android9.0版本可能出现异常崩溃的问题\n" +
                "3. 修复android5 版本的手机在浏览文章是可能崩溃的问题\n" +
                "4. 修复apk下载失败崩溃的问题"
        val updateAppBean = UpdateAppBean()
        updateAppBean.needUpdateLog = message
        updateAppBean.targetPath = path
        updateAppBean.newVersion = "2.7.0"
        updateAppBean.targetSize = (16000 / 1000).toString() + "m"
        updateAppBean.newMd5 = "046dbcf3f1be7816847fe4ea042b2f4a"
        updateAppBean.isConstraint = false
        updateAppBean.mustUpdateLog = "由于不可抗力，必须升级新版本，请升级您的应用程序。"
        updateAppBean.apkFileUrl = "https://bmob-cdn-14564.bmobcloud.com/2019/10/22/be03175340cabe2a80e03dbf26875036.apk"

        DownLoadUtils(this).showReminderDialog(updateAppBean)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onInstallEvent(installEvent: InstallEvent) {
        if (Build.VERSION.SDK_INT >= 26) {
            if (!packageManager.canRequestPackageInstalls()) {
                val rxPermissions = RxPermissions(mContext as Activity)
                rxPermissions.request(Manifest.permission.REQUEST_INSTALL_PACKAGES)
                        .subscribe(object : Observer<Boolean> {
                            override fun onSubscribe(d: Disposable) {}

                            override fun onNext(aBoolean: Boolean) {
                                if (aBoolean) {
                                    AppUpdateUtils.installApp(this@MainActivity, installEvent.file)
                                } else {
                                    // 用户拒绝了该权限，没有选中『不再询问』（Never ask again）,那么下次再次启动时，还会提示请求权限的对话框
                                    DownLoadUtils(this@MainActivity).showUserRejectionDialog("点将台想要安装新版本，大人请同意")
                                }
                            }

                            override fun onError(e: Throwable) {}

                            override fun onComplete() {}
                        })
            } else {
                AppUpdateUtils.installApp(this@MainActivity, installEvent.file)
            }
        } else {
            AppUpdateUtils.installApp(this.mContext, installEvent.file)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)//解除注册 Eventbus
    }
}

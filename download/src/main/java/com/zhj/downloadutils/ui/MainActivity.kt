package com.zhj.downloadutils.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.tbruyelle.rxpermissions2.RxPermissions
import com.zhj.downloadutils.R
import com.zhj.downloadutils.constants.Constants.INTENT_KEY
import com.zhj.downloadutils.event.InstallEvent
import com.zhj.downloadutils.event.UpdateAppBean
import com.zhj.downloadutils.tools.AppUpdateUtils
import com.zhj.downloadutils.tools.AppUpdateUtils.REQ_CODE_INSTALL_APP
import com.zhj.downloadutils.tools.NetworkUtils
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File
import java.lang.ref.WeakReference
import java.util.*

class MainActivity : AppCompatActivity() {

    val RESULT_NEED_UPDATE = "update"
    val RESULT_MUST_UPDATE = "must_update"
    val RESULT_No_UPDATE = "no_update"
    val RESULT_WHAT = 1024
    var mContext: Context? = null
    var strUrl = ""
    private val handler = MyHandler(this)
    private var mFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        EventBus.getDefault().register(this)//注册 Eventbus
        mContext = this
        checkUpdate()
    }

    private fun checkUpdate() {
        if (NetworkUtils.isNetworkAvailable(this)) {
            var result = RESULT_No_UPDATE
            val mNewVersion = 2
            val mMinVersion = 0
            if (mMinVersion != null && AppUpdateUtils.getVersionCode(mContext) < mMinVersion) {
                result = RESULT_MUST_UPDATE
                strUrl = "https://bmob-cdn-14564.bmobcloud.com/2019/10/22/be03175340cabe2a80e03dbf26875036.apk"
            } else if (AppUpdateUtils.getVersionCode(mContext) < Integer.valueOf(mNewVersion)) {
                result = RESULT_NEED_UPDATE
                strUrl = "https://bmob-cdn-14564.bmobcloud.com/2019/10/22/be03175340cabe2a80e03dbf26875036.apk"
            }
            val message = Message.obtain()
            message.what = RESULT_WHAT
            message.obj = result
            handler.sendMessage(message)
//            ICBCRetrofit.getBmobRetrofit()
//                    .create(BmobApi::class.java!!)
//                    .getVersion().enqueue(object : Callback<VersionResponse> {
//                        override fun onResponse(call: Call<VersionResponse>, response: Response<VersionResponse>) {
//                            if (response.message() == "OK") {
//                                var result = RESULT_No_UPDATE
//                                mResultsBean = response.body()!!.getResults().get(0)
//                                if (mResultsBean != null) {
//                                    (getMyApplication() as MyApplication).setVersionResponse(mResultsBean)
//                                    EventBus.getDefault().post(BannerEvent("111"))
//                                }
//                                val mNewVersion = mResultsBean.getNewVersion()
//                                val mMinVersion = mResultsBean.getMinVersion()
//                                if (mMinVersion != null && DeviceUtils.getVersionCode(mContext) < mMinVersion) {
//                                    result = RESULT_MUST_UPDATE
//                                    strUrl = mResultsBean.getApk().getUrl()
//                                } else if (DeviceUtils.getVersionCode(mContext) < Integer.valueOf(mNewVersion)) {
//                                    result = RESULT_NEED_UPDATE
//                                    strUrl = mResultsBean.getApk().getUrl()
//                                }
//                                val message = Message.obtain()
//                                message.what = RESULT_WHAT
//                                message.obj = result
//                                handler.sendMessage(message)
//                                val heroJsonArray = mResultsBean.getHerojson().split("0".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
//
//
//                            } else {
//                                Log.i("bmob", "失败：")
//                            }
//                        }
//
//                        override fun onFailure(call: Call<VersionResponse>, t: Throwable) {
//                            Log.i("bmob", "失败：")
//                        }
//                    })
        }
    }


    @SuppressLint("HandlerLeak")
    internal inner class MyHandler(activity: MainActivity) : Handler() {
        var mActivityReference: WeakReference<MainActivity>

        init {
            mActivityReference = WeakReference(activity)
        }

        override fun handleMessage(msg: Message) {
            val activity = mActivityReference.get()
            if (activity != null) {
                if (RESULT_MUST_UPDATE == msg.obj.toString()) {
                    initUpdateDialog(activity, true)
                }
                if (RESULT_NEED_UPDATE == msg.obj.toString()) {
                    initUpdateDialog(activity, false)
                }
            }
        }
    }

    fun initUpdateDialog(activity: MainActivity, mustUpdate: Boolean) {
        if (!activity.isFinishing) {
            showDialogFragment(mustUpdate)
        }
    }

    /**
     * 跳转到更新页面
     *
     * @param mustUpdate
     */
    fun showDialogFragment(mustUpdate: Boolean) {
        //设置apk 的保存路径
        var path = ""
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED || !Environment.isExternalStorageRemovable()) {
            try {
                path = Objects.requireNonNull(externalCacheDir).absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (TextUtils.isEmpty(path)) {
                path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
            }
        } else {
            path = cacheDir.absolutePath
        }

        var message = "牛皮更新了"
        val updateAppBean = UpdateAppBean()
        updateAppBean.targetPath = path
        updateAppBean.newVersion = "2.7.0"
        updateAppBean.targetSize = (16000 / 1000).toString() + "m"
        updateAppBean.newMd5 = "046dbcf3f1be7816847fe4ea042b2f4a"
        if (mustUpdate) {
            updateAppBean.isConstraint = true
            message = "由于不可抗力，必须升级新版本，请升级您的应用程序。"
        }
        updateAppBean.updateLog = message
        updateAppBean.apkFileUrl = "https://bmob-cdn-14564.bmobcloud.com/2019/10/22/be03175340cabe2a80e03dbf26875036.apk"

        val bundle = Bundle()
        bundle.putSerializable(INTENT_KEY, updateAppBean)
        val rxPermissions = RxPermissions(mContext as Activity)
        rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                .subscribe(object : Observer<Boolean> {
                    override fun onSubscribe(d: Disposable) {}

                    override fun onNext(aBoolean: Boolean) {
                        if (aBoolean) {
                            UpdateDialogFragment.newInstance(bundle).show(this@MainActivity.supportFragmentManager, "dialog")
                        } else {
                            // 用户拒绝了该权限，没有选中『不再询问』（Never ask again）,那么下次再次启动时，还会提示请求权限的对话框
                            val builder = AlertDialog.Builder(this@MainActivity, R.style.MyAlertDialogStyle)
                                    .setMessage("更新需要SD卡读写权限，大人请同意")
                            builder.setPositiveButton("同意") { dialog, which ->
                                ActivityCompat.requestPermissions(this@MainActivity,
                                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), 0)
                            }
                            builder.setNegativeButton("拒绝") { dialog, which -> }
                            builder.create()
                            if (!this@MainActivity.isFinishing) {
                                builder.show()
                            } else {
                                // 用户拒绝了该权限，并且选中『不再询问』，提醒用户手动打开权限
                                Toast.makeText(mContext, "权限被拒绝，请在设置里面开启相应权限，若无相应权限会影响使用", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    override fun onError(e: Throwable) {}

                    override fun onComplete() {}
                })

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onInstallEvent(installEvent: InstallEvent) {
        mFile = installEvent.getFile()
        if (Build.VERSION.SDK_INT >= 26) {
            if (!packageManager.canRequestPackageInstalls()) {
                val rxPermissions = RxPermissions(mContext as Activity)
                rxPermissions.request(Manifest.permission.REQUEST_INSTALL_PACKAGES)
                        .subscribe(object : Observer<Boolean> {
                            override fun onSubscribe(d: Disposable) {}

                            override fun onNext(aBoolean: Boolean) {

                                if (aBoolean) {
                                    AppUpdateUtils.installApp(this@MainActivity, mFile)
                                } else {
                                    // 用户拒绝了该权限，没有选中『不再询问』（Never ask again）,那么下次再次启动时，还会提示请求权限的对话框
                                    val builder = AlertDialog.Builder(this@MainActivity, R.style.MyAlertDialogStyle)
                                            .setMessage("点将台想要安装新版本，大人请同意")
                                    builder.setPositiveButton("同意") { dialog, which ->
                                        val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:$packageName"))
                                        startActivityForResult(intent, REQ_CODE_INSTALL_APP)
                                    }
                                    builder.setNegativeButton("拒绝") { dialog, which -> }
                                    builder.create()
                                    if (!this@MainActivity.isFinishing) {
                                        builder.show()
                                    } else {
                                        // 用户拒绝了该权限，并且选中『不再询问』，提醒用户手动打开权限
                                        Toast.makeText(mContext, "权限被拒绝，请在设置里面开启相应权限，若无相应权限会影响使用", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }

                            override fun onError(e: Throwable) {}

                            override fun onComplete() {}
                        })
            } else {
                AppUpdateUtils.installApp(this@MainActivity, mFile)
            }
        } else {
            AppUpdateUtils.installApp(this.mContext, mFile)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)//解除注册 Eventbus
    }
}

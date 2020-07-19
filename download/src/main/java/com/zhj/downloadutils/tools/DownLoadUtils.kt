package com.zhj.downloadutils.tools

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.tbruyelle.rxpermissions2.RxPermissions
import com.zhj.downloadutils.R
import com.zhj.downloadutils.constants.Constants
import com.zhj.downloadutils.event.UpdateAppBean
import com.zhj.downloadutils.ui.UpdateDialogFragment
import io.reactivex.Observer
import io.reactivex.disposables.Disposable

/**
 * Created by HongJay on 2019-10-26.
 */
class DownLoadUtils(private val activity: AppCompatActivity) {

    /**
     * 跳转到更新页面
     *
     * @param updateAppBean
     */
    fun showReminderDialog(updateAppBean: UpdateAppBean) {
        if (activity.isFinishing || !NetworkUtils.isNetworkAvailable(activity)) {
            return
        }
        val bundle = Bundle()
        bundle.putSerializable(Constants.INTENT_KEY, updateAppBean)
        val rxPermissions = RxPermissions(activity)
        rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                .subscribe(object : Observer<Boolean> {
                    override fun onSubscribe(d: Disposable) {}

                    override fun onNext(aBoolean: Boolean) {
                        if (aBoolean) {
                            UpdateDialogFragment.newInstance(bundle).show(activity.supportFragmentManager, "dialog")
                        } else {
                            // 用户拒绝了该权限，没有选中『不再询问』（Never ask again）,那么下次再次启动时，还会提示请求权限的对话框
                            val builder = AlertDialog.Builder(activity, R.style.MyAlertDialogStyle)
                                    .setMessage("更新需要SD卡读写权限，请同意")
                            builder.setPositiveButton("同意") { dialog, which ->
                                ActivityCompat.requestPermissions(activity,
                                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), 0)
                            }
                            builder.setNegativeButton("拒绝") { dialog, which -> }
                            builder.create()
                            if (!activity.isFinishing) {
                                builder.show()
                            } else {
                                // 用户拒绝了该权限，并且选中『不再询问』，提醒用户手动打开权限
                                Toast.makeText(activity, "权限被拒绝，请在设置里面开启相应权限，若无相应权限会影响使用", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    override fun onError(e: Throwable) {}

                    override fun onComplete() {}
                })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun showUserRejectionDialog(message: String) {
        // 用户拒绝了该权限，没有选中『不再询问』（Never ask again）,那么下次再次启动时，还会提示请求权限的对话框
        val builder = AlertDialog.Builder(activity, R.style.MyAlertDialogStyle)
                .setMessage(message)
        builder.setPositiveButton("同意") { dialog, which ->
            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${activity.packageName}"))
            activity.startActivityForResult(intent, AppUpdateUtils.REQ_CODE_INSTALL_APP)
        }
        builder.setNegativeButton("拒绝") { dialog, which -> }
        builder.create()
        if (!activity.isFinishing) {
            builder.show()
        } else {
            // 用户拒绝了该权限，并且选中『不再询问』，提醒用户手动打开权限
            Toast.makeText(activity, "权限被拒绝，请在设置里面开启相应权限，若无相应权限会影响使用", Toast.LENGTH_SHORT).show()
        }
    }
}

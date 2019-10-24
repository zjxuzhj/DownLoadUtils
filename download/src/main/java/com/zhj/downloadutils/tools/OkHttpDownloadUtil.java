package com.zhj.downloadutils.tools;

import android.annotation.SuppressLint;
import android.os.Environment;
import androidx.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by HongJay on 2017/12/21.
 */

public class OkHttpDownloadUtil {
    private static OkHttpDownloadUtil okHttpDownloadUtil;
    private final OkHttpClient okHttpClient;
    File file;
    private String error_msg;
    int progress;
    private boolean isRun = true;

    public static OkHttpDownloadUtil get() {
        if (okHttpDownloadUtil == null) {
            okHttpDownloadUtil = new OkHttpDownloadUtil();
        }
        return okHttpDownloadUtil;
    }

    private OkHttpDownloadUtil() {
        okHttpClient = new OkHttpClient();
    }

    /**
     * @param url      下载连接
     * @param listener 下载监听
     */
    @SuppressLint("CheckResult")
    public void download(final String url, final String path, final String fileName, final OnDownloadListener listener) {
        if (url.equals("")) {
            return;
        }
        Request request = new Request.Builder().url(url).build();
        Observable.create((ObservableOnSubscribe<Integer>) emitter -> okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                emitter.onNext(0);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                InputStream is = null;
                byte[] buf = new byte[2048];
                int len = 0;
                FileOutputStream fos = null;
                // 储存下载文件的目录
                try {
                    is = response.body().byteStream();
                    long total = response.body().contentLength();
                    String fileUri = path +"/"+ fileName;
                    file = new File(fileUri);
                    if (!file.exists()) {
                        if (!file.getParentFile().exists()) {
                            file.getParentFile().mkdirs();
                        }
                        file.createNewFile();
                    }
                    fos = new FileOutputStream(file);
                    long sum = 0;
                    int oldProgress = 0;
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                        sum += len;
                        progress = (int) (sum * 1.0f / total * 100);
                        // 下载中
                        if (oldProgress == progress) {
                            continue;
                        } else {
                            emitter.onNext(1);
                        }
                        oldProgress = progress;

                    }
                    fos.flush();
                    // 下载完成
                    emitter.onNext(2);
                } catch (Exception e) {
                    error_msg = e.getMessage();
                    emitter.onNext(0);
                } finally {
                    try {
                        if (is != null)
                            is.close();
                    } catch (IOException e) {
                    }
                    try {
                        if (fos != null)
                            fos.close();
                    } catch (IOException e) {
                    }
                }
            }

        })).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        if (integer == 0) {
                            isRun = false;
                            if ("".equals(error_msg)) {
                                listener.onDownloadFailed("下载失败");
                            } else {
                                listener.onDownloadFailed(error_msg);
                            }
                        } else if (integer == 1 && isRun) {
                            listener.onDownloading(progress, 100);
                        } else {
                            isRun = false;
                            listener.onDownloadSuccess(file);
                        }
                    }
                });


    }

    /**
     * @param saveDir
     * @return
     * @throws IOException 判断下载目录是否存在
     */
    private String isExistDir(String saveDir) throws IOException {
        // 下载位置
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

            File downloadFile = new File(Environment.getExternalStorageDirectory(), saveDir);
            if (!downloadFile.mkdirs()) {
                downloadFile.createNewFile();
            }
            String savePath = downloadFile.getAbsolutePath();
            Log.e("savePath", savePath);
            return savePath;
        }
        return null;
    }

    /**
     * @param url
     * @return 从下载连接中解析出文件名
     */
    private String getNameFromUrl(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }

    public interface OnDownloadListener {
        /**
         * 下载成功
         */
        void onDownloadSuccess(File str);

        /**
         * @param progress 下载进度
         */
        void onDownloading(int progress, long total);

        /**
         * 下载失败
         */
        void onDownloadFailed(String error);
    }

    /**
     * 下载
     *
     * @param url      下载地址
     * @param path     文件保存路径
     * @param fileName 文件名称
     * @param callback 回调
     */
    public static void download(@NonNull String url, @NonNull String path, @NonNull String fileName, @NonNull final FileCallback callback) {
        OkHttpDownloadUtil.get().download(url, path, fileName, new OkHttpDownloadUtil.OnDownloadListener() {
            @Override
            public void onDownloadSuccess(File str) {
                callback.onResponse(str);
            }

            @Override
            public void onDownloading(int progress, long total) {
                callback.onProgress(progress, total);
            }

            @Override
            public void onDownloadFailed(String error) {
                callback.onError(error);
            }
        });

    }

    /**
     * 下载回调
     */
    public interface FileCallback {
        /**
         * 进度
         *
         * @param progress 进度0.00 - 0.50  - 1.00
         * @param total    文件总大小 单位字节
         */
        void onProgress(float progress, long total);

        /**
         * 错误回调
         *
         * @param error 错误提示
         */
        void onError(String error);

        /**
         * 结果回调
         *
         * @param file 下载好的文件
         */
        void onResponse(File file);

        /**
         * 请求之前
         */
        void onBefore();
    }
}

package com.hongjay.api;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.ObservableSubscribeProxy;
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Rx工具类
 */
public class RxUtil {

    /**
     * 绑定生命周期
     *
     * @param observable
     * @param <T>
     * @return
     */
    public static <T> ObservableSubscribeProxy<T> bindLifecycle(Observable observable, LifecycleOwner owner) {
        return (ObservableSubscribeProxy) observable.subscribeOn(Schedulers.io())
                .as(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(owner, Lifecycle.Event.ON_DESTROY)));
    }

    /**
     * 回调在主线程中
     *
     * @param observable
     * @param owner
     * @param <T>
     * @return
     */
    public static <T> ObservableSubscribeProxy<T> runOnMain(Observable observable, LifecycleOwner owner) {
        return bindLifecycle(observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()), owner);
    }

    /**
     * 回调在io线程中
     *
     * @param observable
     * @param owner
     * @param <T>
     * @return
     */
    public static <T> ObservableSubscribeProxy<T> runOnIO(Observable observable, LifecycleOwner owner) {
        return bindLifecycle(observable.subscribeOn(Schedulers.io()).observeOn(Schedulers.io()), owner);
    }
}

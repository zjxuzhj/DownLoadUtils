package com.hongjay.api.service;


import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;

import java.lang.reflect.ParameterizedType;

/**
 * ApiService的基类，用于管理TService和接入生命周期
 * 若使用此类作为 {@link LifecycleOwner}
 * 适合最复杂的业务场景，多个service同时存在的时候，可以任意中止某个，或者在任意类中调用网络请求，这个时候就需要手动调用 {@link #cancelTask()} 进行取消
 * TService是定义的Api接口
 */
public abstract class ApiServiceBase<TService> implements LifecycleOwner {

    private Class<TService> mApiServiceClass;

    private TService mApiService;
    private LifecycleRegistry lifecycleRegistry;

    public TService getApiService() {
        return mApiService;
    }

    /**
     * 通过 ApiServiceManager 获得全局单例 ApiServiceImpl
     *
     * @param apiServiceImpl
     */
    public ApiServiceBase(ApiServiceImpl apiServiceImpl) {
        init();
        mApiService = (TService) apiServiceImpl.getService(mApiServiceClass);
    }

    private void init() {
        mApiServiceClass = (Class<TService>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        lifecycleRegistry = new LifecycleRegistry(this);
        lifecycleRegistry.markState(Lifecycle.State.CREATED);
    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return lifecycleRegistry;
    }


    public void cancelTask() {
        lifecycleRegistry.markState(Lifecycle.State.DESTROYED);
    }
}

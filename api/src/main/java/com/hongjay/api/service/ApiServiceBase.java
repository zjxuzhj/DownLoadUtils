package com.hongjay.api.service;


import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;

import java.lang.reflect.ParameterizedType;

/**
 * ApiService的基类
 * <p>
 * TService是定义的Api接口
 * <p>
 * <p>
 * (1)ApiServiceBase默认的构造函数使用的ApiServiceImpl是ApiServiceImpl.getInstance(),其配置参数是在ApplicationBase中的initApiService配置的
 * (2)如果有多个BaseUrl怎么办?使用第二个构造函数ApiServiceBase(ApiServiceImpl apiServiceImpl)
 * <p>
 * <p>
 * Created by coffeexmg on 16/8/26.
 */
public abstract class ApiServiceBase<TService> implements LifecycleOwner {

    private Class<TService> mApiServiceClass;

    private TService mApiService;
    private LifecycleRegistry lifecycleRegistry;

    public TService getApiService() {
        return mApiService;
    }


    /**
     * 默认的构造函数采用的ApiServiceImpl是ApiServiceImpl.getInstance(),其配置参数是在ApplicationBase中的initApiService配置的
     */
    public ApiServiceBase() {
        init();
        mApiService = (TService) ApiServiceImpl.getInstance().getService(mApiServiceClass);
    }

    /**
     * 该构造函数是需要提供ApiServiceImpl
     * 主要是为了需要多个BaseUrl的时候使用
     * ApiServiceImpl apiServiceImpl = ApiServiceImpl.createApiServiceImpl 提供
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

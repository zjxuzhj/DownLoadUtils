package com.hongjay.api;

import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.Interceptor;

/**
 * api配置项
 */
public class ApiConfig {

    //region 静态变量
    private static ApiConfig sInstance;


    public static void init(ApiConfig apiConfig) {

        if (sInstance == null) {
            sInstance = apiConfig;
        }
    }

    public static ApiConfig getInstance() {

        return sInstance;
    }


    private Builder mBuilder;

    //region 构造函数
    private ApiConfig(Builder builder) {

        mBuilder = builder;
    }

    public String getBaseUrl() {
        return mBuilder.mBaseUrl;
    }

    public long getConnectTimeout() {
        return mBuilder.mConnectTimeout;
    }

    public long getReadTimeout() {
        return mBuilder.mReadTimeout;
    }

    public boolean isRetryOnConnectionFailure() {
        return mBuilder.mRetryOnConnectionFailure;
    }

    public long getWriteTimeout() {
        return mBuilder.mWriteTimeout;
    }

    public HashMap<String, String> getHeader() {
        return mBuilder.mHeader;
    }

    public ArrayList<Interceptor> getInterceptors() {
        return mBuilder.mInterceptors;
    }

    public ArrayList<Interceptor> getNetworkInterceptors() {
        return mBuilder.networkInterceptors;
    }

    public final static int CONNECT_TIMEOUT = 10_000;

    public final static int READ_TIMEOUT = 10_000;

    public final static int WRITE_TIMEOUT = 10_000;

    public static class Builder {

        protected long mConnectTimeout = CONNECT_TIMEOUT;
        protected long mReadTimeout = READ_TIMEOUT;
        protected long mWriteTimeout = WRITE_TIMEOUT;
        protected boolean mRetryOnConnectionFailure = true;
        protected String mBaseUrl;
        protected HashMap<String, String> mHeader;
        protected ArrayList<Interceptor> mInterceptors;
        protected ArrayList<Interceptor> networkInterceptors;

        public Builder() {

        }

        /**
         * 连接超时
         *
         * @param connectTimeout
         * @return
         */
        public Builder connectTimeout(long connectTimeout) {

            mConnectTimeout = connectTimeout;
            return this;
        }

        /**
         * 读超时
         *
         * @param readTimeout
         * @return
         */
        public Builder readTimeout(long readTimeout) {

            mReadTimeout = readTimeout;
            return this;
        }

        /**
         * 写超时
         *
         * @param writeTimeout
         * @return
         */
        public Builder writeTimeout(long writeTimeout) {

            mWriteTimeout = writeTimeout;
            return this;
        }

        /**
         * 是否重试
         *
         * @param retryOnConnectionFailure
         * @return
         */
        public Builder retryOnConnectionFailure(boolean retryOnConnectionFailure) {

            mRetryOnConnectionFailure = retryOnConnectionFailure;
            return this;
        }

        /**
         * 添加header
         *
         * @param header
         * @return
         */
        public Builder header(HashMap<String, String> header) {
            mHeader = header;
            return this;
        }

        /**
         * 设置拦截器
         *
         * @param interceptors
         * @return
         */
        public Builder interceptor(ArrayList<Interceptor> interceptors) {
            mInterceptors = interceptors;
            return this;
        }

        /**
         * 网络拦截器
         *
         * @param interceptors
         * @return
         */
        public Builder networkInterceptor(ArrayList<Interceptor> interceptors) {
            networkInterceptors = interceptors;
            return this;
        }

        /**
         * @param baseUrl Api接口地址必须以"/"结尾
         * @return
         */
        public Builder baseUrl(String baseUrl) {

            mBaseUrl = baseUrl;
            return this;
        }

        public ApiConfig build() {

            return new ApiConfig(this);
        }


    }
    //endregion
}

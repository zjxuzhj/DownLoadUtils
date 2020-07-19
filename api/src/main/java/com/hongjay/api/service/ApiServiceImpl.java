package com.hongjay.api.service;

import android.text.TextUtils;

import com.hongjay.api.ApiConfig;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Retrofit的ApiServiceImpl实现
 */
public class ApiServiceImpl {
    private volatile static ApiServiceImpl s_Instance;

    public static ApiServiceImpl getInstance() {
        if (s_Instance == null) {
            synchronized (ApiServiceImpl.class) {
                if (s_Instance == null) {
                    s_Instance = new ApiServiceImpl(ApiConfig.getInstance());
                }
            }
        }
        return s_Instance;
    }

    public static ApiServiceImpl createApiServiceImpl(ApiConfig apiConfig) {
        ApiServiceImpl apiServiceImpl = new ApiServiceImpl(apiConfig);
        return apiServiceImpl;
    }

    private OkHttpClient mOkHttpClient;
    private Retrofit mRetrofit;
    private ApiConfig mApiConfig;

    //endregion
    private ApiServiceImpl(ApiConfig apiConfig) {
        //no instance
        mApiConfig = apiConfig;
        String baseUrl = apiConfig.getBaseUrl();
        if (TextUtils.isEmpty(baseUrl) == false) {
            if (baseUrl.charAt(baseUrl.length() - 1) != '/') {
                throw new IllegalArgumentException("base url must be '/' ");
            }
        }
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(apiConfig.getConnectTimeout(), TimeUnit.MILLISECONDS)
                .readTimeout(apiConfig.getReadTimeout(), TimeUnit.MILLISECONDS)
                .writeTimeout(apiConfig.getWriteTimeout(), TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(apiConfig.isRetryOnConnectionFailure());
        if (apiConfig.getHeader() != null) {
            builder.addInterceptor(new HeaderInterceptor());
        }
        if (apiConfig.getInterceptors() != null) {
            for (Interceptor interceptor : apiConfig.getInterceptors()) {
                if (interceptor != null)
                    builder.addInterceptor(interceptor);
            }
        }
        if (apiConfig.getNetworkInterceptors() != null) {
            for (Interceptor interceptor : apiConfig.getNetworkInterceptors()) {
                if (interceptor != null)
                    builder.addNetworkInterceptor(interceptor);
            }
        }
        mOkHttpClient = builder.build();
        mRetrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(mOkHttpClient)
                .build();
    }


    /**
     * header拦截器
     */
    public class HeaderInterceptor implements Interceptor {

        public HeaderInterceptor() {
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request newRequest;
            Request.Builder builder = chain.request()
                    .newBuilder();

            for (Map.Entry<String, String> entry : mApiConfig.getHeader().entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (key != null && value != null)
                    builder.addHeader(key, value);
            }
            RequestBody requestBody = chain.request().body();
            if (requestBody != null) {
                builder.method(chain.request().method(), requestBody);
            }
            newRequest = builder.build();
            Response response = chain.proceed(newRequest);
            if (response.body() != null) {
                String response_body = response.body().string();
                Response newResponse = response.newBuilder()
                        .body(ResponseBody.create(response.body().contentType(), response_body))
                        .build();
                return newResponse;
            }
            return chain.proceed(newRequest);
        }
    }


    public <T> T getService(Class<T> clazz) {
        if (mRetrofit != null) {
            return mRetrofit.create(clazz);
        } else
            return null;
    }


}
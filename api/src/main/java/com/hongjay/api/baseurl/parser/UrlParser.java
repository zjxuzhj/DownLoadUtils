package com.hongjay.api.baseurl.parser;

import com.hongjay.api.baseurl.RetrofitUrlManager;

import okhttp3.HttpUrl;
import okhttp3.Request;

/**
 * Url解析器
 */

public interface UrlParser {

    /**
     * 这里可以做一些初始化操作
     *
     * @param retrofitUrlManager {@link RetrofitUrlManager}
     */
    void init(RetrofitUrlManager retrofitUrlManager);

    /**
     * 将 {@link RetrofitUrlManager mDomainNameHub} 中映射的 URL 解析成完整的{@link HttpUrl}
     * 用来替换 @{@link Request url} 达到动态切换 URL
     *
     * @param domainUrl 用于替换的 URL 地址
     * @param url       旧 URL 地址
     * @return
     */
    HttpUrl parseUrl(HttpUrl domainUrl, HttpUrl url);
}

package com.hongjay.api.baseurl.parser;

import com.hongjay.api.baseurl.RetrofitUrlManager;

import okhttp3.HttpUrl;

/**
 * 默认解析器, 可根据自定义策略选择不同的解析器
 */
public class DefaultUrlParser implements UrlParser {

    private UrlParser mDomainUrlParser;

    @Override
    public void init(RetrofitUrlManager retrofitUrlManager) {
        this.mDomainUrlParser = new DomainUrlParser();
        this.mDomainUrlParser.init(retrofitUrlManager);
    }

    @Override
    public HttpUrl parseUrl(HttpUrl domainUrl, HttpUrl url) {
        if (null == domainUrl) return url;
        return mDomainUrlParser.parseUrl(domainUrl, url);
    }
}

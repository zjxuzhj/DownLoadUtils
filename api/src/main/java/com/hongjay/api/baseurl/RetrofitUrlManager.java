package com.hongjay.api.baseurl;

import android.text.TextUtils;
import android.util.Log;

import com.hongjay.api.baseurl.parser.DefaultUrlParser;
import com.hongjay.api.baseurl.parser.UrlParser;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.hongjay.api.baseurl.Utils.checkNotNull;


/**
 * 提供动态切换 baseurl 功能
 * 1. 支持给api添加header注解更换接口 baseurl
 * 2. 动态切换指定了 Domain-Name header 的接口 的 baseurl
 * 3. 动态切换没有配置 Domain-Name header 的接口 的 baseurl
 * 4. 提供停止动态切换功能。提供指定接口不被动态切换功能。添加lru缓存功能。
 * 5. 配合启动前环境选择，实现多环境、多baseurl，动态修改
 */
public class RetrofitUrlManager {
    private static final String TAG = "RetrofitUrlManager";
    private static final String DOMAIN_NAME = "Domain-Name";
    private static final String GLOBAL_DOMAIN_NAME = "globalDomainName";
    public static final String DOMAIN_NAME_HEADER = DOMAIN_NAME + ": ";
    /**
     * 如果在 Url 地址中加入此标识符, 框架将不会对此 Url 进行任何切换 BaseUrl 的操作
     */
    public static final String IDENTIFICATION_IGNORE = "#url_ignore";

    private HttpUrl baseUrl;
    private boolean isRun = true; //默认开始运行, 可以随时停止运行, 比如您在 App 启动后已经不需要再动态切换 BaseUrl 了
    private boolean debug = false;//在 Debug  模式下可以打印日志
    private final Map<String, HttpUrl> mDomainNameHub = new HashMap<>();
    private final Interceptor mInterceptor;
    private UrlParser mUrlParser;

    private RetrofitUrlManager() {
        UrlParser urlParser = new DefaultUrlParser();
        urlParser.init(this);
        setUrlParser(urlParser);
        this.mInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                if (!isRun()) // 可以在 App 运行时, 随时通过 setRun(false) 来结束本框架的运行
                    return chain.proceed(chain.request());
                return chain.proceed(processRequest(chain.request()));
            }
        };
    }

    private static class RetrofitUrlManagerHolder {
        private static final RetrofitUrlManager INSTANCE = new RetrofitUrlManager();
    }

    public static final RetrofitUrlManager getInstance() {
        return RetrofitUrlManagerHolder.INSTANCE;
    }

    /**
     * 将 {@link OkHttpClient.Builder} 传入, 配置一些本框架需要的参数
     *
     * @param builder {@link OkHttpClient.Builder}
     * @return {@link OkHttpClient.Builder}
     */
    public OkHttpClient.Builder with(OkHttpClient.Builder builder) {
        checkNotNull(builder, "builder cannot be null");
        return builder
                .addInterceptor(mInterceptor);
    }

    /**
     * 对 {@link Request} 进行一些必要的加工, 执行切换 BaseUrl 的相关逻辑
     *
     * @param request {@link Request}
     * @return {@link Request}
     */
    public Request processRequest(Request request) {
        if (request == null) return request;

        Request.Builder newBuilder = request.newBuilder();

        String url = request.url().toString();
        //如果 Url 地址中包含 IDENTIFICATION_IGNORE 标识符, 框架将不会对此 Url 进行任何切换 BaseUrl 的操作
        if (url.contains(IDENTIFICATION_IGNORE)) {
            return pruneIdentification(newBuilder, url);
        }

        String domainName = obtainDomainNameFromHeaders(request);

        HttpUrl httpUrl;

        // 如果有 header,获取 header 中 domainName 所映射的 url,若没有,则检查全局的 BaseUrl,未找到则为null
        if (!TextUtils.isEmpty(domainName)) {
            httpUrl = fetchDomain(domainName);
            newBuilder.removeHeader(DOMAIN_NAME);
        } else {
            httpUrl = getGlobalDomain();
        }

        if (null != httpUrl) {
            HttpUrl newUrl = mUrlParser.parseUrl(httpUrl, request.url());
            if (debug)
                Log.d(RetrofitUrlManager.TAG, "The new url is { " + newUrl.toString() + " }, old url is { " + request.url().toString() + " }");

            return newBuilder
                    .url(newUrl)
                    .build();
        }

        return newBuilder.build();

    }

    /**
     * 将 {@code IDENTIFICATION_IGNORE} 从 Url 地址中修剪掉
     *
     * @param newBuilder {@link Request.Builder}
     * @param url        原始 Url 地址
     * @return 被修剪过 Url 地址的 {@link Request}
     */
    private Request pruneIdentification(Request.Builder newBuilder, String url) {
        String[] split = url.split(IDENTIFICATION_IGNORE);
        StringBuffer buffer = new StringBuffer();
        for (String s : split) {
            buffer.append(s);
        }
        return newBuilder
                .url(buffer.toString())
                .build();
    }

    /**
     * 框架是否在运行
     *
     * @return {@code true} 为正在运行, {@code false} 为未运行
     */
    public boolean isRun() {
        return this.isRun;
    }

    /**
     * 控制框架是否运行, 在每个域名地址都已经确定, 不需要再动态更改时可设置为 {@code false}
     *
     * @param run {@code true} 为正在运行, {@code false} 为未运行
     */
    public void setRun(boolean run) {
        this.isRun = run;
    }

    /**
     * 开启 Debug 模式下可以打印日志
     *
     * @param debug true 开启 Debug 模式
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * 获取 BaseUrl
     *
     * @return {@link #baseUrl}
     */
    public HttpUrl getBaseUrl() {
        return baseUrl;
    }

    /**
     * 将 url 地址作为参数传入此方法, 并使用此方法返回的 Url 地址进行网络请求, 则会使此 Url 地址忽略掉本框架的所有更改效果
     * <p>
     * 使用场景:
     * 比如当您使用了 {@link #setGlobalDomain(String url)} 配置了全局 BaseUrl 后, 想请求一个与全局 BaseUrl
     * 不同的第三方服务商地址获取图片
     *
     * @param url Url 地址
     * @return 处理后的 Url 地址
     */
    public String setUrlNotChange(String url) {
        checkNotNull(url, "url cannot be null");
        return url + IDENTIFICATION_IGNORE;
    }

    /**
     * 全局动态替换 BaseUrl, 优先级: Header中配置的 BaseUrl > 全局配置的 BaseUrl
     * 除了作为备用的 BaseUrl, 当您项目中只有一个 BaseUrl, 但需要动态切换
     * 这种方式不用在每个接口方法上加入 Header, 就能实现动态切换 BaseUrl
     *
     * @param globalDomain 全局 BaseUrl
     */
    public void setGlobalDomain(String globalDomain) {
        checkNotNull(globalDomain, "globalDomain cannot be null");
        synchronized (mDomainNameHub) {
            mDomainNameHub.put(GLOBAL_DOMAIN_NAME, Utils.checkUrl(globalDomain));
        }
    }

    /**
     * 获取全局 BaseUrl
     */
    public synchronized HttpUrl getGlobalDomain() {
        return mDomainNameHub.get(GLOBAL_DOMAIN_NAME);
    }

    /**
     * 移除全局 BaseUrl
     */
    public void removeGlobalDomain() {
        synchronized (mDomainNameHub) {
            mDomainNameHub.remove(GLOBAL_DOMAIN_NAME);
        }
    }

    /**
     * 存放 Domain(BaseUrl) 的映射关系
     *
     * @param domainName
     * @param domainUrl
     */
    public void putDomain(String domainName, String domainUrl) {
        checkNotNull(domainName, "domainName cannot be null");
        checkNotNull(domainUrl, "domainUrl cannot be null");
        synchronized (mDomainNameHub) {
            mDomainNameHub.put(domainName, Utils.checkUrl(domainUrl));
        }
    }

    /**
     * 取出对应 {@code domainName} 的 Url(BaseUrl)
     *
     * @param domainName
     * @return
     */
    public synchronized HttpUrl fetchDomain(String domainName) {
        checkNotNull(domainName, "domainName cannot be null");
        return mDomainNameHub.get(domainName);
    }

    /**
     * 移除某个 {@code domainName}
     *
     * @param domainName {@code domainName}
     */
    public void removeDomain(String domainName) {
        checkNotNull(domainName, "domainName cannot be null");
        synchronized (mDomainNameHub) {
            mDomainNameHub.remove(domainName);
        }
    }

    /**
     * 清理所有 Domain(BaseUrl)
     */
    public void clearAllDomain() {
        mDomainNameHub.clear();
    }

    /**
     * 存放 Domain(BaseUrl) 的容器中是否存在这个 {@code domainName}
     *
     * @param domainName {@code domainName}
     * @return {@code true} 为存在, {@code false} 为不存在
     */
    public synchronized boolean haveDomain(String domainName) {
        return mDomainNameHub.containsKey(domainName);
    }

    /**
     * 存放 Domain(BaseUrl) 的容器, 当前的大小
     *
     * @return 容量大小
     */
    public synchronized int domainSize() {
        return mDomainNameHub.size();
    }

    /**
     * 可自行实现 {@link UrlParser} 动态切换 Url 解析策略
     *
     * @param parser {@link UrlParser}
     */
    public void setUrlParser(UrlParser parser) {
        checkNotNull(parser, "parser cannot be null");
        this.mUrlParser = parser;
    }

    /**
     * 从 {@link Request#header(String)} 中取出 DomainName
     *
     * @param request {@link Request}
     * @return DomainName
     */
    private String obtainDomainNameFromHeaders(Request request) {
        List<String> headers = request.headers(DOMAIN_NAME);
        if (headers == null || headers.size() == 0)
            return null;
        if (headers.size() > 1)
            throw new IllegalArgumentException("Only one Domain-Name in the headers");
        return request.header(DOMAIN_NAME);
    }
}

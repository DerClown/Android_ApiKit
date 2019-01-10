package com.yoyo.derclown.apikitdemo.APIKit;
//网络配置

import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.internal.operators.flowable.FlowableInterval;

public class ApiConfig {
    /**
     * 请求头参数
     */
    private Map<String, String> header;
    public Map<String, String> getHeader() {
        return header;
    }

    /**
     * 请求base url
     */
    private String baseUrl;
    public String getBaseUrl() {
        return baseUrl;
    }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    /**
     * 请求超时时间
     */
    private int requestTimeout;
    public int getRequestTimeout() {
        return requestTimeout;
    }
    public void setRequestTimeout(int requestTimeout) { this.requestTimeout = requestTimeout; }

    /**
     * 单例
     */
    private static final ApiConfig ourInstance = new ApiConfig();

    public static ApiConfig getInstance() {
        return ourInstance;
    }

    private ApiConfig() {
        //默认20s
        requestTimeout = 20_000;
        header = new HashMap<>();
    }

    public void addRequestHeader(String key, String value) {
        if (TextUtils.isEmpty(key) || TextUtils.isEmpty(value)) return;
        this.header.put(key, value);
    };
}

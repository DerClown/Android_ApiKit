package com.yoyo.derclown.apikitdemo.APIKit;

import java.io.IOException;
import java.util.Map;

import io.reactivex.Flowable;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

class ApiAgent {
    private ApiService apiService;

    /**
     * 单例
     */
    private static final ApiAgent ourInstance = new ApiAgent();

    static ApiAgent getInstance() {
        return ourInstance;
    }

    private ApiAgent() {
    }

    /**
     * 请求头设置
     */
    private Interceptor getHeaderInterceptor() {
        return new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();
                Request.Builder requestBuilder = original.newBuilder();
                // 添加header
                if (ApiConfig.getInstance().getHeader().size() != 0) {
                    for (Map.Entry<String, String> entry : ApiConfig.getInstance().getHeader().entrySet()) {
                        requestBuilder.addHeader(entry.getKey(), entry.getValue());
                    }
                }
                Request request = requestBuilder.build();
                return chain.proceed(request);
            }
        };
    }

    private Retrofit getRetrofit() {
        OkHttpClient okHttpClient = new OkHttpClient()
                .newBuilder()
                .addInterceptor(this.getHeaderInterceptor())
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(ApiConfig.getInstance().getBaseUrl())
                //设置数据解析器
                .addConverterFactory(GsonConverterFactory.create())
                //设置网络请求适配器，使其支持RxJava与RxAndroid
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        return retrofit;
    }

    /**
     * 获取请求服务
     * @return apiService
     */
    public ApiService getApiService() {
        this.apiService = this.getRetrofit().create(ApiService.class);
        return this.apiService;
    }

}

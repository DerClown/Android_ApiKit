package com.yoyo.derclown.apikitdemo.APIKit;

import java.util.Map;

import io.reactivex.Flowable;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

public interface ApiService {
    /**
     * GET请求
     * @param requestUrl 请求url
     * @param params 参数
     * @return 原始数据【需要后期的加工处理】
     */
    @GET("{request_url}")
    Flowable<String> sendGetReqeust(@Path("request_url") String requestUrl,
                                    @QueryMap Map<String, String> params);

    /**
     * POST请求
     * @param requestUrl 请求url
     * @param params 参数
     * @return 原始数据【需要后期的加工处理】
     */
    @FormUrlEncoded
    @POST("{request_url}")
    Flowable<String> sendPostRequest(@Path("request_url") String requestUrl,
                                     @FieldMap Map<String, String> params);
}

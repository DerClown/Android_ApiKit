package com.yoyo.derclown.apikitdemo.APIKit;

import android.support.annotation.IntDef;

import com.google.gson.Gson;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import retrofit2.HttpException;

public class ApiResponse {
    static final int ApiResponseTypeSuccess = 100;
    static final int ApiResponseTypeFailure = 101;
    static final int ApiResponseTypeTimeout = 102;

    @IntDef({ApiResponseTypeSuccess, ApiResponseTypeFailure,ApiResponseTypeTimeout})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ApiResponseType {
    }

    // 需要用户更具自己的网络请求结构来定义这几个值
    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 状态码
     */
    private int status;


    /**
     * 网络请求回来的原始数据
     */
    private Object data;


    /**
     * 响应类型
     */
    @ApiResponseType
    private int responseType;

    public int getResponseType() {
        return responseType;
    }

    public void setResponseType(int responseType) {
        this.responseType = responseType;
    }

    /**
     * 网络请求回来jsonstring
     * @param responseString
     */
    public ApiResponse(String responseString) {
        handleResponseString(responseString);
    }

    /**
     * 网络请求捕捉到的错误信息
     * @param throwable
     */
    public  ApiResponse(Throwable throwable) {
        handleFailureWithThrowable(throwable);
    }

    // 处理Api成功回调的响应结果
    private void handleResponseString(String responseString) {
        //处理结果
        Map result = new HashMap();
        Gson gson = new Gson();
        result = gson.fromJson(responseString, Map.class);

        // key 必须和后端保持一致,自行处理
        this.data = result.get("data");
        this.status = Integer.parseInt((String) result.get("status"));
        this.errorMessage = (String) result.get("errorMessage");
        // 根据后端定义好的状态结果做判断是否符合要求, 不符合要求的都是以失败处理
        // 这里以【200】作为栗子🌰, 不是200的是失败的请求
        this.responseType = this.status == 200 ? ApiResponseTypeSuccess : ApiResponseTypeFailure;
    }

    // 处理Api请求失败捕捉的Exception
    private void handleFailureWithThrowable(Throwable throwable) {
        boolean isTimeout = (throwable instanceof SocketTimeoutException) ||
                            (throwable instanceof ConnectException) ||
                            (throwable instanceof UnknownHostException) ||
                            (throwable instanceof HttpException) ? true : false;
        // warning 错误信息自行定义
        if (isTimeout) {
            this.responseType = ApiResponseTypeTimeout;
            this.errorMessage = "网络不稳定";
        } else {
            this.responseType = ApiResponseTypeFailure;
            this.errorMessage = "请稍后再试";
        }
    }


    /*****************************************************************/
    /*                      Getter && Setter                         */
    /*****************************************************************/

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }
}

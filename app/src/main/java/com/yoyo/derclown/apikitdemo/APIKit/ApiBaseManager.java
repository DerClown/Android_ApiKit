package com.yoyo.derclown.apikitdemo.APIKit;

import android.graphics.ColorSpace;
import android.support.annotation.IntDef;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import java.util.Map;

import io.reactivex.CompletableOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;
import io.reactivex.functions.Consumer;
import io.reactivex.internal.subscribers.LambdaSubscriber;

public abstract class ApiBaseManager {
    // 数据源【必须子类实现】
    @NonNull protected ApiProtocol.ApiBaseManagerDataSource dataSource = null;
    // 协议对象【外部类实现】
    private ApiProtocol.ApiBaseManagerCallBackDelegate delegate = null;
    // 验证器 【如果有需要实现，必须子类实现】
    protected ApiProtocol.ApiBaseManagerValiator valiator = null;
    // 防止RxJave内存泄漏【外部类实现】
    private ApiProtocol.BindAutoDispose bindAutoDispose = null;
    // 拦截器【子类，外部类都可以实现】
    private ApiProtocol.ApiBaseManagerInterceptor interceptor = null;
    // 数据转换器【子类实现】
    protected ApiProtocol.ApiManagerDataTransformerDelegate transformerDelegate = null;
    // 转成对应的数据模型【子类实现】
    protected ApiProtocol.ShouldApiManagerTransformerDataToTargetModel transformerDataToTargetModel = null;

    /**
     * 错误信息
     */
    private String errorMessage = null;
    /**
     * 原始格式化数据
     */
    private Object rowData = null;

    /**
     * 网络请求结果类型
     */
    @ApiHandleRequestType
    private int handleRequestType = ApiHandleRequestTypeDefault;

    /**
     * 网络是否执行中
     */
    private boolean isExcuting = false;

    /**
     * 网络观察者
     */
    private LambdaSubscriber lambdaSubscriber = null;


    //********************Life cycle******************************//

    public ApiBaseManager(ApiProtocol.ApiBaseManagerCallBackDelegate delegate, @NonNull ApiProtocol.BindAutoDispose bindAutoDispose) {
        this.delegate = delegate;
        this.bindAutoDispose = bindAutoDispose;
    }

    /**
     * 取消网络请求
     */
    public void cancelRequest() {
        if (!lambdaSubscriber.isDisposed()) {
            lambdaSubscriber.dispose();
        }
        lambdaSubscriber = null;
        this.setExcuting(false);
        this.setRowData(null);
        this.setErrorMessage(null);
        this.handleRequestType = ApiHandleRequestTypeDefault;
    }

    /**
     * 发起网络请求【请求统一入口】
     */
    public void startRequest() {
        if (this.isExcuting) return;
        Map<String, String> requestParams = dataSource.requestParamaters();
        // 追加参数的方法
        String requestUrl = dataSource.requestUrl();
        loadDataWithParams(requestParams, requestUrl);
    }

    public Object fetchData() {
        // rowData原始数据，从ApiResponse输出的data
        // 需要判断获取数据的优先级；transformerDataToTargetModel>transformerDelegate>rowData
        if (this.transformerDataToTargetModel != null) {
            Object transformerResult = ApiHelpers.transformerResponseDataToTargetMoelClass(this.getRowData(), this.transformerDataToTargetModel.TargetModelClass());
            return transformerResult;
        } else if (this.transformerDelegate != null) {
            return this.transformerDelegate.apiTransformerDataToTargetObject(this);
        }
        return this.getRowData();
    }

    /**
     *  网络请求
     * @param requestParams
     */
    private void loadDataWithParams(Map<String, String> requestParams, String requestUrl) {
        this.isExcuting = true;
        if (shouldCallApiWithParamaters(requestParams)) {
            if (this.dataSource.requestType() == ApiProtocol.BasePresenterManagerRequestTypeGet) {
                Object object = ApiAgent.getInstance()
                        .getApiService()
                        .sendGetReqeust(requestUrl, requestParams)
                        .compose(ApiScheduler.<String>Flo_io_main())
                        .as(bindAutoDispose.<String>bindAutoDispose())
                        .subscribe(new Consumer<String>() {
                            @Override
                            public void accept(String responseString) throws Exception {
                                ApiResponse response = new ApiResponse(responseString);
                                handleSuccessRequestResult(response);
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                ApiResponse response = new ApiResponse(throwable);
                                handleFailureRequest(response,ApiHandleRequestTypeFailure);
                            }
                        });
            } else {
                ApiAgent.getInstance()
                        .getApiService()
                        .sendGetReqeust(requestUrl, requestParams)
                        .compose(ApiScheduler.<String>Flo_io_main())
                        .as(bindAutoDispose.<String>bindAutoDispose())
                        .subscribe(new Consumer<String>() {
                            @Override
                            public void accept(String responseString) throws Exception {
                                ApiResponse response = new ApiResponse(responseString);
                                handleSuccessRequestResult(response);
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                ApiResponse response = new ApiResponse(throwable);
                                handleFailureRequest(response,ApiHandleRequestTypeFailure);
                            }
                        });
            }
        } else {
            this.handleFailureRequest(null, ApiHandleRequestTypeParamsError);
        }
    }

    private boolean shouldCallApiWithParamaters(Map<String, String> requestParams) {
        if (valiator != null) {
            return valiator.apiIsCorrectByRequestParamaters(requestParams);
        }
        return true;
    }

    private void handleSuccessRequestResult(ApiResponse response) {
        isExcuting = false;
        if (response.getResponseType() == ApiResponse.ApiResponseTypeSuccess) {
            apiCallBackSuccessAfterIntercept(response);
            apiCallBackSuccessWithApiResponse(response);
            apiCallBackSuccessAfterIntercept(response);
        } else {
            int handleRequestType = response.getResponseType() == ApiResponse.ApiResponseTypeTimeout ? ApiHandleRequestTypeTimeout : ApiHandleRequestTypeFailure;
            this.handleFailureRequest(response, handleRequestType);
        }
    }

    private void handleFailureRequest(@Nullable ApiResponse response, int handleRequestType) {
        isExcuting = false;
        if (response != null) {
            int handleType = response.getResponseType() == ApiResponse.ApiResponseTypeTimeout ? ApiHandleRequestTypeTimeout : ApiHandleRequestTypeFailure;
            this.setHandleRequestType(handleType);
            this.setErrorMessage(response.getErrorMessage());
        } else {
            this.setHandleRequestType(handleRequestType);
            this.setErrorMessage("稍后再试");
        }

        apiCallBackFailureBeforeIntercept();
        apiCallBackFailure();
        apiCallBackFailureAfterIntercept();
    }

    /**
     * 回调成功
     */
    private void apiCallBackSuccessWithApiResponse(ApiResponse response) {
        this.setHandleRequestType(ApiHandleRequestTypeSuccess);
        this.setRowData(response.getData());
        if (delegate == null) {
            delegate.apiCallBackDidSuccess(this, this.fetchData());
        }
    }

    /**
     * 成功回调前的拦截
     * @param response
     */
    private void apiCallBackSuccessBeforeIntercept(ApiResponse response) {
        if (interceptor != null) {
            interceptor.apiBaseManagerBeforeSuccessWithResult(this, response);
        }
    }

    /**
     * 成功回到后拦截
     */
    private void apiCallBackSuccessAfterIntercept(ApiResponse response) {
        if (interceptor != null) {
            interceptor.apiBaseManagerAfterSuccessWithResult(this, response);
        }
    }

    /**
     * 回调失败
     */
    private void apiCallBackFailure() {
        if (delegate != null) {
            delegate.apiCallBackDidFailure(this);
        }
    }

    /**
     * 回调失败前的拦截
     */
    private void apiCallBackFailureBeforeIntercept() {
        if (interceptor != null) {
            interceptor.apiBaseManagerBeforeFailure(this);
        }
    }

    /**
     * 回调失败后拦截
     */
    private void apiCallBackFailureAfterIntercept() {
        if (interceptor != null) {
            interceptor.apiBaseManagerAfterFailure(this);
        }
    }

    //****************************枚举***********************************

    // 网络请求处理类型
    //默认状态
    public static final int ApiHandleRequestTypeDefault = 1000;
    public static final int ApiHandleRequestTypeSuccess = 1001;
    public static final int ApiHandleRequestTypeFailure = 1002;
    public static final int ApiHandleRequestTypeTimeout = 1003;
    public static final int ApiHandleRequestTypeParamsError = 1004;
    public static final int ApiHandleRequestTypeNoContent = 1005;
    public static final int ApiHandleRequestTypeNoNetwork = 1006;
    @IntDef({ApiHandleRequestTypeDefault,
            ApiHandleRequestTypeSuccess,
            ApiHandleRequestTypeFailure,
            ApiHandleRequestTypeTimeout,
            ApiHandleRequestTypeParamsError,
            ApiHandleRequestTypeNoContent,
            ApiHandleRequestTypeNoNetwork})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ApiHandleRequestType {}

    //***********************Getter,Setter******************************

    public ApiProtocol.ApiBaseManagerCallBackDelegate getDelegate() {
        return delegate;
    }

    public void setDelegate(ApiProtocol.ApiBaseManagerCallBackDelegate delegate) {
        this.delegate = delegate;
    }

    public ApiProtocol.BindAutoDispose getBindAutoDispose() {
        return bindAutoDispose;
    }

    public void setBindAutoDispose(ApiProtocol.BindAutoDispose bindAutoDispose) {
        this.bindAutoDispose = bindAutoDispose;
    }

    public ApiProtocol.ApiBaseManagerInterceptor getInterceptor() {
        return interceptor;
    }

    public void setInterceptor(ApiProtocol.ApiBaseManagerInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    @ApiHandleRequestType
    public int getHandleRequestType() {
        return this.handleRequestType;
    }

    private void setHandleRequestType(@ApiHandleRequestType int handleRequestType) {
        this.handleRequestType = handleRequestType;
    }


    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Object getRowData() {
        return rowData;
    }

    public void setRowData(Object rowData) {
        this.rowData = rowData;
    }

    public boolean isExcuting() {
        return isExcuting;
    }

    public void setExcuting(boolean excuting) {
        isExcuting = excuting;
    }
}

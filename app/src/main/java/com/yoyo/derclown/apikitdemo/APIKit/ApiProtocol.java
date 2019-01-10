package com.yoyo.derclown.apikitdemo.APIKit;

import android.support.annotation.IntDef;

import com.uber.autodispose.AutoDisposeConverter;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Map;

import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;

public interface ApiProtocol {
    public static final int BasePresenterManagerRequestTypeGet = 0;
    public static final int BasePresenterManagerRequestTypePost = 1;
    public static final int BasePresenterManagerRequestTypeDelete = 2;
    public static final int BasePresenterManagerRequestTypePut = 3;

    @IntDef({BasePresenterManagerRequestTypeGet, BasePresenterManagerRequestTypePost,
            BasePresenterManagerRequestTypeDelete, BasePresenterManagerRequestTypePut})
    @Retention(RetentionPolicy.SOURCE)
    public @interface BasePresenterManagerRequestType {
    }

    /**
     * 网络请求回调协议
     */
    interface ApiBaseManagerCallBackDelegate<ApiManager extends ApiBaseManager> {
        /**
         * 请求回调成功
         * @param manager
         * @param result
         */
        void apiCallBackDidSuccess(ApiManager manager, Object result);

        /**
         * 回到失败
         * @param manager
         */
        void apiCallBackDidFailure(ApiManager manager);
    }

    interface ApiBaseManagerDataSource {
        /**
         * 网络请求参数
         * @return map
         */
        @Nullable Map<String, String> requestParamaters();

        /**
         * 网络请求url
         * @return url
         */
        @NonNull String requestUrl();

        /**
         * 请求类型
         * @return
         */
        @BasePresenterManagerRequestType
        int requestType();
    }

    /**
     * 验证器
     */
    interface ApiBaseManagerValiator {
        boolean apiIsCorrectByRequestParamaters(Map<String, String> requestParamaters);
        boolean apiIsCorrectWithCallBackData(Object data);
    }

    /**
     * API拦截器
     */
    interface ApiBaseManagerInterceptor<ApiManager extends ApiBaseManager> {
        /**
         * 回调成功之前
         * @param manager
         * @param response
         */
        void apiBaseManagerBeforeSuccessWithResult(ApiManager manager, ApiResponse response);

        /**
         * 回调成功之后
         * @param manager
         * @param response
         */
        void apiBaseManagerAfterSuccessWithResult(ApiManager manager, ApiResponse response);

        void apiBaseManagerBeforeFailure(ApiManager manager);
        void apiBaseManagerAfterFailure(ApiManager manager);
    }

    // 如果都是继承基类Activity,直接在基类实现即可
    interface BindAutoDispose {
        /**
         * 绑定Android生命周期 防止RxJava内存泄漏
         *
         * @param <T>
         * @return
         */
        <T> AutoDisposeConverter<T> bindAutoDispose();
    }

    /**
     * 数据转换
     * @param <ApiManager>
     */
    interface ApiManagerDataTransformerDelegate<ApiManager extends ApiBaseManager> {
        Object apiTransformerDataToTargetObject(ApiManager manager);
    }

    /**
     * 返回需要转换的对应数据模型对象
     */
    interface ShouldApiManagerTransformerDataToTargetModel {
        Class TargetModelClass();
    }
}

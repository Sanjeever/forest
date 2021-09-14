package com.dtflys.forest.interceptor;

import com.dtflys.forest.callback.OnError;
import com.dtflys.forest.callback.OnLoadCookie;
import com.dtflys.forest.callback.OnProgress;
import com.dtflys.forest.callback.OnRetry;
import com.dtflys.forest.callback.OnSaveCookie;
import com.dtflys.forest.callback.OnSuccess;
import com.dtflys.forest.callback.RetryWhen;
import com.dtflys.forest.exceptions.ForestRuntimeException;
import com.dtflys.forest.http.ForestCookies;
import com.dtflys.forest.http.ForestRequest;
import com.dtflys.forest.http.ForestResponse;
import com.dtflys.forest.reflection.ForestMethod;
import com.dtflys.forest.utils.ForestProgress;

/**
 * Forest拦截器接口
 * <p>拦截器在请求的初始化、发送请求前、发送成功、发送失败等生命周期中都会被调用
 * <p>总的生命周期回调函数调用顺序如下:
 * <pre>
 * Forest接口方法调用 ->
 *  | onInvokeMethod ->
 *  | beforeExecute ->
 *     | 如果返回 false -> 中断请求，直接返回
 *     | 如果返回 true ->
 *        | 发送请求 ->
 *          | 发送请求失败 ->
 *              | retryWhen ->
 *                 | 返回 true 则触发请求重试
 *                 | 返回 false 则跳转到 [onError]
 *              | onError -> 跳转到 [afterExecute]
 *          | 发送请求成功 ->
 *             | 等待响应 ->
 *             | 接受到响应 ->
 *             | retryWhen ->
 *                 | 返回 true 则触发请求重试
 *                 | 返回 false 判断响应状态 ->
 *                     | 响应失败 -> onError -> 跳转到 [afterExecute]
 *                     | 响应成功 -> onSuccess -> 跳转到 [afterExecute]
 *  | afterExecute -> 退出 Forest 接口方法，并返回数据
 * </pre>
 * @author gongjun[dt_flys@hotmail.com]
 * @since 2016-06-26
 */
public interface Interceptor<T> extends OnSuccess<T>, OnError, OnProgress, OnLoadCookie, OnSaveCookie, OnRetry {


    /**
     * 默认回调函数: 接口方法执行时调用该方法
     * <p>默认为什么都不做
     *
     * @param request
     * @param method
     * @param args
     */
    default void onInvokeMethod(ForestRequest request, ForestMethod method, Object[] args) {
    }

    /**
     * 默认回调函数: 请求执行前调用该方法
     * <p>其返回值为布尔类型，可以控制请求是否继续执行
     * <p>默认为什么都不做
     *
     * @param request Forest请求对象
     * @return {@code true}: 继续执行该请求, 否则中断请求
     */
    default boolean beforeExecute(ForestRequest request) {
        return true;
    }

    /**
     * 默认回调函数: 请求完成后(成功/失败后) 调用该方法
     * <p>默认为什么都不做
     *
     * @param request Forest请求对象
     * @param response Forest响应对象
     */
    default void afterExecute(ForestRequest request, ForestResponse response) {
    }

    /**
     * 默认回调函数: 请求成功后调用该方法
     * <p>默认为什么都不做
     *
     * @param data 请求响应返回后经过序列化后的数据
     * @param request Forest请求对象
     * @param response Forest响应对象
     */
    @Override
    default void onSuccess(T data, ForestRequest request, ForestResponse response) {
    }

    /**
     * 默认回调函数: 请求失败后调用该方法
     * <p>默认为什么都不做
     *
     * @param ex 请求失败的异常对象
     * @param request Forest请求对象
     * @param response Forest响应对象
     */
    @Override
    default void onError(ForestRuntimeException ex, ForestRequest request, ForestResponse response) {
    }


    /**
     * 默认回调函数: 在触发请求重试时执行
     * <p>默认为什么都不做
     *
     * @param request Forest请求对象
     * @param response Forest响应对象
     */
    @Override
    default void onRetry(ForestRequest request, ForestResponse response) {
    }

    /**
     * 默认文件上传或下载监听传输进度时调用该方法
     * <p>默认为什么都不做
     *
     * @param progress Forest进度对象
     */
    @Override
    default void onProgress(ForestProgress progress) {
    }

    /**
     * 默认回调函数: 在发送请求加载Cookie时调用该方法
     * <p>默认为什么都不做
     *
     * @param request Forest请求对象
     * @param cookies Cookie集合, 需要通过请求发送的Cookie都添加到该集合
     */
    @Override
    default void onLoadCookie(ForestRequest request, ForestCookies cookies) {
    }

    /**
     * 默认回调函数: 在请求响应成功后，需要保存Cookie时调用该方法
     * <p>默认为什么都不做
     *
     * @param request Forest请求对象
     * @param cookies Cookie集合，通过响应返回的Cookie都从该集合获取
     */
    @Override
    default void onSaveCookie(ForestRequest request, ForestCookies cookies) {
    }

    /**
     * 获取请求在本拦截器中的 Attribute 属性
     *
     * @param request Forest请求对象
     * @return {@link InterceptorAttributes} 对象实例
     */
    default InterceptorAttributes getAttributes(ForestRequest request) {
        return request.getInterceptorAttributes(this.getClass());
    }

    /**
     * 添加请求在本拦截器中的 Attribute 属性
     *
     * @param request Forest请求对象
     * @param name 属性名称
     * @param value 属性值
     */
    default void addAttribute(ForestRequest request, String name, Object value) {
        request.addInterceptorAttribute(this.getClass(), name, value);
    }

    default Object getAttribute(ForestRequest request, String name) {
        return request.getInterceptorAttribute(this.getClass(), name);
    }

    default <T> T getAttribute(ForestRequest request, String name, Class<T> clazz) {
        Object obj = request.getInterceptorAttribute(this.getClass(), name);
        if (obj == null) {
            return null;
        }
        return (T) obj;
    }


    default String getAttributeAsString(ForestRequest request, String name) {
        Object attr = getAttribute(request, name);
        if (attr == null) {
            return null;
        }
        return String.valueOf(attr);
    }

    default Integer getAttributeAsInteger(ForestRequest request, String name) {
        Object attr = getAttribute(request, name);
        if (attr == null) {
            return null;
        }
        return (Integer) attr;
    }

    default Float getAttributeAsFloat(ForestRequest request, String name) {
        Object attr = getAttribute(request, name);
        if (attr == null) {
            return null;
        }
        return (Float) attr;
    }

    default Double getAttributeAsDouble(ForestRequest request, String name) {
        Object attr = getAttribute(request, name);
        if (attr == null) {
            return null;
        }
        return (Double) attr;
    }

}

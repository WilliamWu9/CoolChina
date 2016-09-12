package com.jiahao.coolweather.util;

/**
 * Created by jhWu on 2016/9/12.
 */
public interface HttpCallbackListener {

    void onSuccess(String response); //成功回调

    void onFailure(Exception e); //失败回调

}

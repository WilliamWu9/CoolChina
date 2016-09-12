package com.jiahao.coolweather.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by jhWu on 2016/9/12.
 *
 * Http访问封装类，主要用于发起get数据请求，获取返回数据
 *
 * //@param address -发起访问的目的地址
 * //@param listener - 访问结果的回调
 */
public class HttpUtil {

    public static void sendHttpRequest(final String address,
                                       final HttpCallbackListener listener){
        //开启子线程执行网络访问
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;

                try {
                    URL url = new URL(address);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    InputStream in = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String line ="";
                    while((line=reader.readLine()) !=null){
                        response.append(line);
                    }
                    if (listener != null){
                        //回调onSuccess方法
                        listener.onSuccess(response.toString());
                    }
                } catch (Exception e) {
                    if (listener!=null){
                        listener.onFailure(e);
                    }
                }finally {
                    if (connection!=null){
                        connection.disconnect();
                    }
                }
            }
        }).start();

    }

}

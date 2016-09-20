package com.jiahao.coolweather.activity;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jiahao.coolweather.R;
import com.jiahao.coolweather.util.HttpCallbackListener;
import com.jiahao.coolweather.util.HttpUtil;
import com.jiahao.coolweather.util.Utility;

import org.w3c.dom.Text;

public class WeatherActivity extends AppCompatActivity implements View.OnClickListener {

    private LinearLayout weatherLayout;
    private TextView cityNameText;
    private TextView publishText;
    private TextView weatherDespText;
    private TextView temp1Text;
    private TextView temp2Text;
    private TextView currentDateText;
    private Button switchCity;
    private Button refreshWeather;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_layout);
        getSupportActionBar().hide();
        initView();
        initData();
    }


    private void initView() {
        weatherLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
        cityNameText = (TextView) findViewById(R.id.city_name);
        publishText = (TextView) findViewById(R.id.publish_text);
        weatherDespText = (TextView) findViewById(R.id.weather_desp);
        temp1Text = (TextView) findViewById(R.id.temp1);
        temp2Text = (TextView) findViewById(R.id.temp2);
        currentDateText = (TextView) findViewById(R.id.current_date);
        switchCity = (Button) findViewById(R.id.switch_city);
        refreshWeather = (Button) findViewById(R.id.refresh_weather);
        switchCity.setOnClickListener(this);
        refreshWeather.setOnClickListener(this);
    }

    private void initData() {
        String countyCode = getIntent().getStringExtra("county_code");
        if (!TextUtils.isEmpty(countyCode)) {
            //有县级代号时就去查询天气
            publishText.setText("同步中...");
            weatherLayout.setVisibility(View.INVISIBLE);
            cityNameText.setVisibility(View.INVISIBLE);
            queryWeatherCode(countyCode);
        } else {
            //没有县级代号时就直接显示本地存储的天气信息
            showWeather();
        }

    }

    /**
     * 查询县级代号所对应的天气代号
     *
     * @param countyCode
     */

    private void queryWeatherCode(String countyCode) {
        String address = "http://www.weather.com.cn/data/list3/city" + countyCode + ".xml";
        queryFromServer(address, "countyCode");
    }

    /***
     * 查询天气代号所对应的天气
     */
    private void queryWeatherInfo(String weatherCode) {
        String address = "http://www.weather.com.cn/data/cityInfo/" +weatherCode +".html";
        queryFromServer(address,"weatherCode");
    }


    /**
     * 根据传入的地址和类型去向服务器查询天气代号或者天气信息
     * @param address
     * @param weatherCode
     */
    private void queryFromServer(String address, final String type) {

        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onSuccess(String response) {
                if("countyCode".equals(type)){
                    if(!TextUtils.isEmpty(response)){
                        //从服务器返回的数据中解析出天气代号
                        String[] array = response.split("\\|");
                        if(array != null && array.length == 2){
                            String weatherCode = array[1];
                            queryWeatherInfo(weatherCode);
                        }

                    }
                }else if("weatherCode".equals(type)){
                    //处理服务器返回的天气信息
                    Utility.handleWeatherResponse(WeatherActivity.this,response);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showWeather();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        publishText.setText("同步失败...");
                    }
                });

            }
        });
    }

    /***
     * 从SharedPreferences文件中读取存储的天气信息，并显示到界面上
     */
    private void showWeather() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        cityNameText.setText(pref.getString("city_name",""));
        temp1Text.setText(pref.getString("temp1",""));
        temp2Text.setText(pref.getString("temp2",""));
        weatherDespText.setText(pref.getString("weather_desp",""));
        publishText.setText("今天"+pref.getString("publish_time","")+"发布");
        currentDateText.setText(pref.getString("current_date",""));
        weatherLayout.setVisibility(View.VISIBLE);
        cityNameText.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View view) {

    }
}

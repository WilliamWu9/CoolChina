package com.jiahao.coolweather.util;

import android.text.TextUtils;

import com.jiahao.coolweather.db.CoolWeatherDB;
import com.jiahao.coolweather.model.City;
import com.jiahao.coolweather.model.County;
import com.jiahao.coolweather.model.Province;

/**
 * Created by jhWu on 2016/9/12.
 * <p>
 * 用于解析服务器返回的信息
 */
public class Utility {
    /**
     * 解析和处理服务器返回的省级数据
     */
    public synchronized static boolean handleProvinceResponse(
            CoolWeatherDB coolWeatherDB, String response) {
        if (!TextUtils.isEmpty(response)) {
            String[] allProovinces = response.split(",");
            if (allProovinces != null && allProovinces.length > 0) {
                for (String p : allProovinces) {
                    String[] array = p.split("\\|");
                    Province province = new Province();
                    province.setProvinceCode(array[0]);
                    province.setProvinceName(array[1]);
                    //将解析出来的数据存储到Province表
                    coolWeatherDB.saveProvince(province);
                }
                return true; //这里是解析完成其已经存储进数据库了
            }
        }
        return false;
    }

    /**
     * 解析和处理服务器返回的市级数据
     */
    public static boolean handleCityResponse(CoolWeatherDB coolWeatherDB,
                                             String response, int provinceId) {
        if (!TextUtils.isEmpty(response)){
            String[] allCities = response.split(",");
            if( allCities != null  && allCities.length >0){
                for (String c: allCities){
                    String[] array = c.split("\\|");
                    City city = new City();
                    city.setCityCode(array[0]);
                    city.setCityName(array[1]);
                    city.setProvinceId(provinceId);
                    //将解析出来的数据存储到City表
                    coolWeatherDB.saveCity(city);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 解析和处理服务器返回的县级数据
     */
    public static boolean handleCountyResponse(CoolWeatherDB coolWeatherDB,
                                             String response, int cityId) {
        if (!TextUtils.isEmpty(response)){
            String[] allCities = response.split(",");
            if( allCities != null  && allCities.length >0){
                for (String c: allCities){
                    String[] array = c.split("\\|");
                    County county = new County();
                    county.setCountyCode(array[0]);
                    county.setCountyName(array[1]);
                    county.setCityId(cityId);
                    //将解析出来的数据存储到County表
                    coolWeatherDB.saveCounty(county);
                }
                return true;
            }
        }
        return false;
    }


}

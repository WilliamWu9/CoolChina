package com.jiahao.coolweather.activity;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jiahao.coolweather.R;
import com.jiahao.coolweather.db.CoolWeatherDB;
import com.jiahao.coolweather.model.City;
import com.jiahao.coolweather.model.County;
import com.jiahao.coolweather.model.Province;
import com.jiahao.coolweather.util.HttpCallbackListener;
import com.jiahao.coolweather.util.HttpUtil;
import com.jiahao.coolweather.util.Utility;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class ChooseAreaActivity extends AppCompatActivity {

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    private ProgressDialog progressDialog;
    private TextView titleText;
    private ListView listView;
    private List<String> dataList = new ArrayList<String>();
    private ArrayAdapter<String> adapter;
    private CoolWeatherDB coolWeatherDB;

    /***
     * 省、市、县列表
     */
    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;
    /**
     * 选中的省份、城市
     */
    private Province selectedProvince;
    private City selectedCity;
    /**
     * 当前选中的级别
     */
    private int currentLevel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_area);
        getSupportActionBar().hide();

        initData();
    }

    private void initData() {
        titleText = (TextView) findViewById(R.id.title_text);
        listView = (ListView) findViewById(R.id.list_view);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);
        //获取封装的数据库实例
        coolWeatherDB = CoolWeatherDB.getInstance(this);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvince = provinceList.get(position);
                    queryCities();//根据点击的位置加载该省下面的市级数据
                } else if (currentLevel == LEVEL_CITY) {
                    selectedCity = cityList.get(position);
                    queryCounties();//根据点击的位置加载该市下面的县级数据
                }
            }
        });
        //默默地加载省级数据
        queryProvinces();

    }

    /***
     * 查询全国所有的省，优先从数据库查询，如果没有的话再去服务器查询
     */
    private void queryProvinces() {
        provinceList = coolWeatherDB.loadProvinces();
        if (provinceList.size() > 0) {
            dataList.clear();
            for (Province province : provinceList) {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText("中国");
            currentLevel = LEVEL_PROVINCE;
        } else {
            queryFromServer(null, "province");
        }
    }


    /***
     * 查询选中省内的所有的市，优先从数据库查询，如果没有的话再去服务器查询
     */
    private void queryCities() {
        cityList = coolWeatherDB.loadCitys(selectedProvince.getId());
        if (cityList.size() > 0) {
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectedProvince.getProvinceName());
            currentLevel = LEVEL_CITY;
        } else {
            queryFromServer(selectedProvince.getProvinceCode(), "city");
        }

    }

    /***
     * 查询选中市内的所有县，优先从数据库查询，如果没有的话再去服务器查询
     */
    private void queryCounties() {
        countyList = coolWeatherDB.loadCountys(selectedCity.getId());
        if (countyList.size() > 0) {
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            titleText.setText(selectedCity.getCityName());
            currentLevel = LEVEL_COUNTY;
        } else {
            queryFromServer(selectedCity.getCityCode(), "county");
        }
    }

    /**
     * 根据传入的代号和类型从服务器上查询省、市、县数据
     */
    private void queryFromServer(final String code, final String type) {
        String address;
        if (TextUtils.isEmpty(code)) {
            address = "http://www.weather.com.cn/data/list3/city.xml";//直接获取省级列表
        } else {
            address = "http://www.weather.com.cn/data/list3/city" + code + ".xml";//获取相应的市、或者县列表
        }

        showProgressDialog();

        HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
            @Override
            public void onSuccess(String response) {
                boolean result = false;
                if ("province".equals(type)) {
                    result = Utility.handleProvinceResponse(coolWeatherDB, response);
                } else if ("city".equals(type)) {
                    result = Utility.handleCityResponse(coolWeatherDB, response, selectedProvince.getId());
                } else if ("county".equals(type)) {
                    result = Utility.handleCountyResponse(coolWeatherDB, response, selectedCity.getId());
                }

                if (result) {
                    //通过runOnUiThread()方法返回到主线程处理逻辑(刷新数据)
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)) {
                                queryProvinces();
                            } else if ("city".equals(type)) {
                                queryCities();
                            } else if ("county".equals(type)) {
                                queryCounties();
                            }
                        }
                    });
                }
            }


            @Override
            public void onFailure(Exception e) {
                //通过runOnUiThread()方法返回到主线程处理逻辑(刷新数据)
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }


    /**
     * 显示进度对话框
     */
    private void showProgressDialog() {
        //不去重复创建对象
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }


    /**
     * 关闭进度对话框
     */
    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    /**
     * 捕获Back键，根据当前的级别来判断，此时应该返回市列表、省列表、还是直接退出
     */
    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        if(currentLevel == LEVEL_COUNTY){
            queryCities();
        }else if(currentLevel == LEVEL_CITY){
            queryProvinces();
        }else {
            finish();
        }
    }
}

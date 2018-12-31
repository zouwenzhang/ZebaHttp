package com.zeba.http.test;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView tvTouch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvTouch=findViewById(R.id.tv_touch);
        new MyHttpRequester()
                .host(this)//在哪个页面请求的
                .touch(tvTouch)//哪个地点请求的，这两个主要用来防止重复请求
                .method("")//请求的方法名
                .post()//http请求方法
                .add("key","value")//请求的参数
                .request(null);//请求的回调，自定义
    }
}

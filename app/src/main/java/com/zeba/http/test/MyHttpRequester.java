package com.zeba.http.test;

import com.zeba.http.BaseRequester;

import java.util.Map;

public class MyHttpRequester extends BaseRequester{

    public MyHttpRequester(){
        url("http://www.baidu.com/");//请求前缀
    }

    @Override
    protected void convert(Map<String, Object> map) {
        //这里是请求之前的处理方法，可以将一些公共参数加到map中去
    }

    @Override
    protected Object handleSuccess(String response) {
        //这里是请求成功后返回的处理方法，注意这里不是主线程，
        // 所以主要做一些数据解析操作，并将解析结果返回
        return null;
    }

    @Override
    protected void onSuccess(Object response) {
        //这里是上一个方法后的主线程回调，这里主要是根据解析结果判断应该回调成功还是失败
    }

    @Override
    protected void onError(String msg) {
        //这里是请求错误的回调方法
    }

}

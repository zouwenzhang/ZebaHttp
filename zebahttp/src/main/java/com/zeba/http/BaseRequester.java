package com.zeba.http;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseRequester {
    public static final int RequestType_PostFile=3;
    public static final int RequestType_PostJson=2;
    public static final int RequestType_Post=1;
    public static final int RequestType_Get=0;
    private String url;
    private String requestMethod;
    private int requestType=RequestType_Post;
    private WeakReference<Object> requestHost;
    private WeakReference<Object> requestTouch;
    private Map<String,Object> params=new HashMap<String,Object>();
    private Map<String,File> fileParams=null;
    private Object callBack;//回调方法
    private String result;//返回的数据
    private Object response;//反序列化后存放的对象
    private Object loading;//请求过程中的数据
    private int responseCode;//返回状态码

    public BaseRequester url(String u){
        url=u;
        return this;
    }

    public BaseRequester method(String m){
        requestMethod =m;
        return this;
    }

    public BaseRequester host(Object object){
        requestHost=new WeakReference<Object>(object);
        return this;
    }

    public BaseRequester touch(Object object){
        requestTouch=new WeakReference<Object>(object);
        return this;
    }

    public BaseRequester add(String key, Object value){
        params.put(key,value);
        return this;
    }

    public BaseRequester addFile(String key, File file){
        if(fileParams==null){
            fileParams=new HashMap<>();
        }
        fileParams.put(key,file);
        return this;
    }

    public BaseRequester post(){
        requestType=RequestType_Post;
        return this;
    }

    public BaseRequester postJson(){
        requestType=RequestType_PostJson;
        return this;
    }

    public BaseRequester postFile(){
        requestType=RequestType_PostFile;
        return this;
    }

    public BaseRequester get(){
        requestType=RequestType_Get;
        return this;
    }

    public BaseRequester callBack(Object obj){
        callBack=obj;
        return this;
    }

    public boolean isRepeat(){
        return ZebaHttpClient.Instance().isRepeatRequest(this);
    }

    public void request(){
        ZebaHttpClient.Instance().request(this);
    }

    public void request(Object callBack){
        this.callBack=callBack;
        request();
    }

    protected String getFullUrl(){
        if(requestMethod==null){
            return url;
        }
        return url+ requestMethod;
    }

    protected String getRequestMethod(){
        return requestMethod;
    }

    protected String getRequestId(){
        if(getRequestHost()!=null&&getRequestTouch()==null){
            return getFullUrl()+getRequestHost().hashCode();
        }
        if(getRequestHost()!=null&&getRequestTouch()!=null){
            return getFullUrl()+getRequestHost().hashCode()+getRequestTouch().hashCode();
        }
        return getFullUrl();
    }

    protected Object getRequestHost(){
        if(requestHost!=null){
            return requestHost.get();
        }
        return requestHost;
    }

    protected Object getRequestTouch(){
        if(requestTouch!=null){
            return requestTouch.get();
        }
        return requestTouch;
    }

    protected int getRequestType(){
        return requestType;
    }

    protected Map<String,Object> getInParams(){
        return params;
    }
    protected Map<String,Object> getOutParams(){
        return params;
    }
    protected Map<String,File> getFileParams(){
        return fileParams;
    }

    protected Object getCallBack(){return callBack; }

    protected boolean isValid(){
        if(requestHost==null||requestHost.get()==null){
            return false;
        }
        return true;
    }

    protected void setResult(String r){
        result=r;
    }

    protected String getResult(){
        return result;
    }

    protected void setResponse(Object obj){
        response=obj;
    }

    protected Object getResponse(){
        return response;
    }

    protected void setLoading(Object obj){
        loading=obj;
    }

    protected Object getLoading(){
        return loading;
    }

    protected void setResponseCode(int code){
        responseCode=code;
    }

    protected int getResponseCode(){
        return responseCode;
    }

    protected void onLoading(Object obj){

    }

    /**返回成功后会调用此方法进行对象反序列化处理，注意：此方法调用的线程是异步线程*/
    protected abstract Object handleSuccess(String response);
    /**在okhttp返回成功时会调用此方法*/
    protected abstract void onSuccess(Object response);
    /**在okhttp返回失败时会调用此方法*/
    protected abstract void onError(String msg);
    /**此方法是将一些通用的参数装载到请求参数中*/
    protected abstract void convert(Map<String,Object> map);
    /**此方法是添加请求头的方法，默认为空*/
    protected Map<String,String> addHeader(){return null;}
}

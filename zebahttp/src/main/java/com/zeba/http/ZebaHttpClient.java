package com.zeba.http;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.webkit.MimeTypeMap;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ZebaHttpClient {
    private static volatile ZebaHttpClient myHttpClient;
    private static volatile Gson gson;
    public static final int Handler_Success=1;
    public static final int Handler_Error=2;
    public static final int Handler_Loading=3;
    //单例模式
    public static ZebaHttpClient Instance(){
        if (myHttpClient == null) {
            synchronized (ZebaHttpClient.class) {
                if (myHttpClient == null) {
                    myHttpClient = new ZebaHttpClient();
                }
            }
        }
        return myHttpClient;
    }

    private OkHttpClient mOkHttpClient=new OkHttpClient();
    private Map<String,BaseRequester> requesterMap=new HashMap<String,BaseRequester>();
    private final HashMap<String, List<Cookie>> cookieStore = new HashMap<>();
    private OkHttpClient.Builder builder;
    private void createBuilder(){
        if(builder!=null){
            return;
        }
        builder = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS);
    }
    public void clearCookies(){
        cookieStore.clear();
    }
    /**开启cookies*/
    public ZebaHttpClient enableCookies(){
        createBuilder();
        builder.cookieJar(new CookieJar() {
            @Override
            public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                cookieStore.put(url.host(), cookies);
            }

            @Override
            public List<Cookie> loadForRequest(HttpUrl url) {
                List<Cookie> cookies = cookieStore.get(url.host());
                return cookies != null ? cookies : new ArrayList<Cookie>();
            }
        });
        return this;
//        builder.sslSocketFactory()
//        builder.setSslSocketFactory(HttpsUtil.getSSLSocketFactory(AppRunTimeModule.getApplication()));
//			client.setSslSocketFactory(HttpsUtil.getSSLSocketFactory_Certificate(AppRunTimeModule.getApplication()));
//			client.setHostnameVerifier(new HostnameVerifier() {
//	            @Override
//	            public boolean verify(String hostname, SSLSession session) {
//	                if (hostname.equals("192.168.0.10"))
//	                    return true;
//	                else
//	                    return false;
//	            }
//	        });
    }

    public void builder(){
        if(builder!=null){
            mOkHttpClient=builder.build();
        }
    }

    public HashMap<String, List<Cookie>> getCookieStore(){
        return cookieStore;
    }

    public boolean isRepeatRequest(BaseRequester requester){
        if(requesterMap.get(requester.getRequestId())!=null){
            return true;
        }
        return false;
    }

    public Call builderRequest(BaseRequester requester){
        //防止重复请求
        if(isRepeatRequest(requester)){
            return null;
        }
        //如果不是重复请求，则将本次请求加入请求队列，以防止重复请求
        requesterMap.put(requester.getRequestId(),requester);
        //请求参数转换，为了便于有些请求参数的统一封装
        requester.convert(requester.getInParams());
        Request.Builder builder= new Request.Builder();
        //根据类型创建相应的请求参数
        switch(requester.getRequestType()){
            case BaseRequester.RequestType_Get:
                builder.url(requester.getFullUrl()+createGetBody(requester));
                builder.get();
                builder.addHeader("charset","utf-8");
                break;
            case BaseRequester.RequestType_Post:
                builder.url(requester.getFullUrl());
                builder.post(createPostParamsBody(requester));
                builder.addHeader("charset","utf-8");
                break;
            case BaseRequester.RequestType_PostJson:
                builder.url(requester.getFullUrl());
                builder.post(createPostJsonBody(requester));
                break;
            case BaseRequester.RequestType_PostFile:
                builder.url(requester.getFullUrl());
                builder.post(createPostFileBody(requester));
                break;
        }
        addHeaders(builder,requester);
        builder.tag(requester);
        Request request = builder.build();
        return mOkHttpClient.newCall(request);
    }

    /**
     * 普通的http请求都走此方法
     * */
    public void request(BaseRequester requester){
        Call call = builderRequest(requester);
        if(call==null){
            return;
        }
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                BaseRequester baseRequester= (BaseRequester) call.request().tag();
                baseRequester.setResult("网络连接失败");
                Message msg=handler.obtainMessage(Handler_Error,baseRequester);
                handler.sendMessage(msg);
            }

            @Override
            public void onResponse(Call call, Response response){
                BaseRequester baseRequester= (BaseRequester) call.request().tag();
                try{
                    String str = response.body().string();
                    baseRequester.setResponseCode(response.code());
                    baseRequester.setResult(str);
                    baseRequester.setResponse(baseRequester.handleSuccess(str));
                    Message msg=handler.obtainMessage(Handler_Success,baseRequester);
                    handler.sendMessage(msg);
                }catch (Exception e){
                    e.printStackTrace();
                    baseRequester.setResult("数据解析失败");
                    Message msg=handler.obtainMessage(Handler_Error,baseRequester);
                    handler.sendMessage(msg);
                }

            }

        });
    }

    public void addHeaders(Request.Builder builder,BaseRequester requester){
        if(requester.addHeader()==null){
            return;
        }
        Map<String,String> map=requester.addHeader();
        Iterator<String> iterator= map.keySet().iterator();
        while (iterator.hasNext()){
            String key=iterator.next();
            builder.addHeader(key,map.get(key));
        }
    }
    public static final MediaType JSON= MediaType.parse("application/json; charset=utf-8");
    public RequestBody createPostJsonBody(BaseRequester requester){
        RequestBody requestBody = RequestBody.create(JSON, getGson().toJson(requester.getOutParams()));
        return requestBody;
    }

    /**将map封装为post格式的请求参数*/
    public RequestBody createPostParamsBody(BaseRequester requester){
        FormBody.Builder builder= new FormBody.Builder();
        Iterator<String> iterator= requester.getOutParams().keySet().iterator();
        String key;
        Object value;
        while(iterator.hasNext()){
            key=iterator.next();
            value=requester.getOutParams().get(key);
            builder.add(key,String.valueOf(value));
        }
//        builder.addEncoded("charset","utf-8");
        return builder.build();
    }

    /**将map封装为get格式的请求参数*/
    public String createGetBody(BaseRequester requester){
        if(requester.getOutParams()==null||requester.getOutParams().size()==0){
            return "";
        }
        String url= requester.getFullUrl();
        StringBuffer stringBuffer=new StringBuffer();
        int p=url.lastIndexOf("?");
        if(p!=-1){
            if(p!=url.length()-1){
                stringBuffer.append("&");
            }
        }else{
            stringBuffer.append("?");
        }
        Iterator<String> iterator= requester.getOutParams().keySet().iterator();
        String key;
        Object value;
        while(iterator.hasNext()){
            key=iterator.next();
            value=requester.getOutParams().get(key);
            stringBuffer.append(key+"="+String.valueOf(value));
            if(iterator.hasNext()){
                stringBuffer.append("&");
            }
        }
        return stringBuffer.toString();
    }

    public RequestBody createPostFileBody(BaseRequester requester){
        MultipartBody.Builder requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM);
        Map<String,File> fileMap=requester.getFileParams();
        if(fileMap!=null){
            Iterator<String> iterator= fileMap.keySet().iterator();
            String key;
            File file;
            while(iterator.hasNext()){
                // MediaType.parse() 里面是上传的文件类型。
                key=iterator.next();
                file=fileMap.get(key);
                RequestBody body = RequestBody.create(MediaType.parse(getMimeTypeFromName(file.getName())), file);
                // 参数分别为， 请求key ，文件名称 ， RequestBody
                requestBody.addFormDataPart(key, file.getName(), body);
            }
        }
        Iterator<String> iterator= requester.getOutParams().keySet().iterator();
        String key;
        Object value;
        while(iterator.hasNext()){
            key=iterator.next();
            value=requester.getOutParams().get(key);
            requestBody.addFormDataPart(key,String.valueOf(value));
        }
        return requestBody.build();
    }

    public static String getMimeTypeFromName(String url){
        if(url==null||url.length()==0){
            return null;
        }
        String name=url.substring(url.lastIndexOf(".")+1);
        if(!MimeTypeMap.getSingleton().hasExtension(name)){
            return null;
        }
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(name);
    }

    private Handler handler=new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            BaseRequester baseRequester= (BaseRequester) msg.obj;
            switch(msg.what){
                case 1:
                    if(baseRequester.isValid()){
                        baseRequester.onSuccess(baseRequester.getResponse());
                    }
                    break;
                case 2:
                    if(baseRequester.isValid()){
                        baseRequester.onError(baseRequester.getResult());
                    }
                    break;
                case 3:
                    if(baseRequester.isValid()){
                        baseRequester.onLoading(baseRequester.getLoading());
                    }
                    break;
            }
            if(msg.what!=Handler_Loading){
                //将请求从请求队列中移除，以便允许下次请求
                requesterMap.remove(baseRequester.getRequestId());
            }
        }
    };

    public Handler getHandler(){
        return handler;
    }

    public static Gson getGson(){
        if (gson == null) {
            synchronized (ZebaHttpClient.class) {
                if (gson == null) {
                    gson = new Gson();
                }
            }
        }
        return gson;
    }

}

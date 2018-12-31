package com.zeba.http;

import android.os.Handler;
import android.os.Message;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class FileRequester extends BaseRequester{
    private String fileSavePath;
    private FileDownLoadCallBack fileDownLoadCallBack;

    public FileRequester savePath(String path){
        fileSavePath=path;
        return this;
    }

    public FileRequester url(Object host,Object touch,String url){
        host(host);
        touch(touch);
        url(url);
        return this;
    }

    @Override
    public BaseRequester callBack(Object obj) {
        return this;
    }

    public void download(FileDownLoadCallBack callBack){
        fileDownLoadCallBack=callBack;
        request();
    }

    @Override
    protected void convert(Map<String, Object> map) {

    }

    @Override
    protected void onLoading(Object obj) {
        if(fileDownLoadCallBack!=null){
            fileDownLoadCallBack.onLoading(getFullUrl(),(float)obj);
        }
    }

    @Override
    protected Object handleSuccess(String response) {
        return null;
    }

    @Override
    protected void onSuccess(Object response) {
        if(fileDownLoadCallBack!=null){
            fileDownLoadCallBack.onSuccess(getFullUrl(),fileSavePath);
        }
        fileDownLoadCallBack=null;
    }

    @Override
    protected void onError(String msg) {
        if(fileDownLoadCallBack!=null){
            fileDownLoadCallBack.onError(getFullUrl(),msg);
        }
        fileDownLoadCallBack=null;
    }

    @Override
    public void request() {
        get();
        method("");
        Call call= ZebaHttpClient.Instance().builderRequest(this);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Handler handler= ZebaHttpClient.Instance().getHandler();
                Message msg=handler.obtainMessage(ZebaHttpClient.Handler_Error,FileRequester.this);
                setResult("网络连接失败");
                handler.sendMessage(msg);
            }

            @Override
            public void onResponse(Call call, Response response){
                Handler handler= ZebaHttpClient.Instance().getHandler();
                long sumLength= response.body().contentLength();
                long loadLength=0;
                try{
                    File file = new File(fileSavePath);
                    if(file.exists()&&file.isFile()){
                        file.delete();
                    }
                    file.createNewFile();
                    InputStream is = null;
                    FileOutputStream fileOutputStream = null;
                    try {
                        is = response.body().byteStream();
                        fileOutputStream = new FileOutputStream(file, true);
                        byte[] buffer = new byte[2048];//缓冲数组2kB
                        int len;
                        while ((len = is.read(buffer)) != -1) {
                            fileOutputStream.write(buffer, 0, len);
                            loadLength+=len;
                            Message msg=handler.obtainMessage(ZebaHttpClient.Handler_Loading,FileRequester.this);
                            setLoading(loadLength*1f/sumLength);
                            handler.sendMessage(msg);
                        }
                        Message msg=handler.obtainMessage(ZebaHttpClient.Handler_Success,FileRequester.this);
                        setResponse(fileSavePath);
                        handler.sendMessage(msg);
                    } finally {
                        if(is!=null){
                            is.close();
                        }
                        if(fileOutputStream!=null){
                            fileOutputStream.close();
                        }
                    }
                }catch(Exception e){
                    e.printStackTrace();
                    Message msg=handler.obtainMessage(ZebaHttpClient.Handler_Error,FileRequester.this);
                    setResult("数据下载异常");
                    handler.sendMessage(msg);
                }
            }
        });
    }
}

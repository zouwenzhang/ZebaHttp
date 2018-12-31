package com.zeba.http;

public interface FileDownLoadCallBack {
    void onLoading(String url, float progress);
    void onSuccess(String url, String savePath);
    void onError(String url, String msg);
}

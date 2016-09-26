package me.wh.common.image;

public interface ImageLoadCallback {
    void onLoadSuccess(String url);

    void onLoadFail(String url);

    void onLoadClear(String url);
}

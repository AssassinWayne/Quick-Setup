package me.wh.common.image;

import android.widget.ImageView;

import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.lang.ref.WeakReference;

class ImageLoadRequest {
    private String mUrl;
    private boolean mPreload;
    private int mWidth;
    private int mHeight;
    private WeakReference<ImageView> mImageViewRef;
    private WeakReference<Object> mLoadContextRef;
    private WeakReference<ImageLoadCallback> mImageLoadCallbackRef;
    private Target<File> mTarget;

    public ImageLoadRequest(Object loadContext, ImageView imageView, String url, int width, int height) {
        if (loadContext != null) {
            mLoadContextRef = new WeakReference<Object>(loadContext);
        }
        setImageParams(imageView, url, width, height);
    }

    public ImageLoadRequest(Object loadContext, String url, boolean preload, int width, int height) {
        if (loadContext != null) {
            mLoadContextRef = new WeakReference<Object>(loadContext);
        }
        setUrlParams(url, preload, width, height, null);
    }

    public ImageLoadRequest(String url, boolean preload, int width, int height) {
        setUrlParams(url, preload, width, height, null);
    }

    public ImageLoadRequest(String url, boolean preload, int width, int height, ImageLoadCallback imageLoadCallback) {
        setUrlParams(url, preload, width, height, imageLoadCallback);
    }

    private void setImageParams(ImageView imageView, String url, int width, int height) {
        if (imageView != null) {
            mImageViewRef = new WeakReference<ImageView>(imageView);
        }
        setUrlParams(url, false, width, height, null);
    }

    private void setUrlParams(String url, boolean preload, int width, int height, ImageLoadCallback imageLoadCallback) {
        mUrl = url;
        mPreload = preload;
        mWidth = width;
        mHeight = height;
        if (imageLoadCallback != null) {
            mImageLoadCallbackRef = new WeakReference<ImageLoadCallback>(imageLoadCallback);
        }
    }

    public ImageView getImageView() {
        if (mImageViewRef == null) {
            return null;
        }
        return mImageViewRef.get();
    }

    public String getUrl() {
        return mUrl;
    }

    public boolean isPreload() {
        return mPreload;
    }

    public Object getLoadContext() {
        if (mLoadContextRef == null) {
            return null;
        }
        return mLoadContextRef.get();
    }

    public ImageLoadCallback getImageLoadCallback() {
        if (mImageLoadCallbackRef == null) {
            return null;
        }
        return mImageLoadCallbackRef.get();
    }

    public void setTarget(Target<File> target) {
        mTarget = target;
    }

    public Target<File> getTarget() {
        return mTarget;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }
}

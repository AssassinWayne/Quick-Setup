package me.wh.common.image;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import me.wh.common.CommonApplication;
import me.wh.common.thread.ThreadManager;
import me.wh.common.util.ViewUtil;

class ImagePreloadManager {
    private static final ImagePreloadManager sInstance = new ImagePreloadManager();

    public static ImagePreloadManager get() {
        return sInstance;
    }

    private ImagePreloadManager() {

    }

    private static final String HANDLER_THREAD_NAME_PRELOAD = "preload-image-handler-thread";
    private static final int TYPE_PRELOAD_ADD = 0;
    private static final int TYPE_PRELOAD_DELETE = 1;
    private static final int TYPE_PRELOAD_DO = 2;
    private static final int TYPE_PRELOAD_SUCCESS = 3;
    private static final int TYPE_PRELOAD_FAIL = 4;
    private static final int TYPE_PRELOAD_CLEAR = 5;

    private final HashMap<String, ImageLoadRequest> mImageLoadRequestMap = new HashMap<>();
    private final int mMaxPreloadCount = 2;
    private final HashSet<String> mPreloadSet = new HashSet<>();
    private final Handler mPreloadHandler = new Handler(ThreadManager.get().getHandlerThreadLooper(HANDLER_THREAD_NAME_PRELOAD)) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TYPE_PRELOAD_ADD:
                    processPreloadAdd((ImageLoadRequest) msg.obj);
                    mPreloadHandler.sendEmptyMessage(TYPE_PRELOAD_DO);
                    break;
                case TYPE_PRELOAD_DELETE:
                    processPreloadDelete((String) msg.obj);
                    mPreloadHandler.sendEmptyMessage(TYPE_PRELOAD_DO);
                    break;
                case TYPE_PRELOAD_DO:
                    processPreloadDo();
                    break;
                case TYPE_PRELOAD_SUCCESS:
                    processPreloadSuccess((String) msg.obj);
                    mPreloadHandler.sendEmptyMessage(TYPE_PRELOAD_DO);
                    break;
                case TYPE_PRELOAD_FAIL:
                    processPreloadFail((String) msg.obj);
                    mPreloadHandler.sendEmptyMessage(TYPE_PRELOAD_DO);
                    break;
                case TYPE_PRELOAD_CLEAR:
                    processPreloadClear((String) msg.obj);
                    mPreloadHandler.sendEmptyMessage(TYPE_PRELOAD_DO);
                    break;
            }
        }
    };

    void preload(String url, int width, int height) {
        if (!TextUtils.isEmpty(url)) {
            mPreloadHandler.obtainMessage(TYPE_PRELOAD_ADD, new ImageLoadRequest(url, true, width, height)).sendToTarget();
        }
    }

    void preload(Object loadContext, String url, int width, int height) {
        if (!TextUtils.isEmpty(url)) {
            mPreloadHandler.obtainMessage(TYPE_PRELOAD_ADD, new ImageLoadRequest(loadContext, url, true, width, height)).sendToTarget();
        }
    }

    void cancelPreload(String url) {
        if (!TextUtils.isEmpty(url)) {
            mPreloadHandler.obtainMessage(TYPE_PRELOAD_DELETE, url).sendToTarget();
        }
    }

    private void processPreloadAdd(ImageLoadRequest imageLoadRequest) {
        if (imageLoadRequest == null || TextUtils.isEmpty(imageLoadRequest.getUrl())) {
            return;
        }
        mImageLoadRequestMap.put(imageLoadRequest.getUrl(), imageLoadRequest);
    }

    private void processPreloadDelete(String url) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        ImageLoadRequest imageLoadRequest = mImageLoadRequestMap.remove(url);
        if (imageLoadRequest != null) {
            Target<File> target = imageLoadRequest.getTarget();
            if (target != null) {
                Glide.clear(target);
            }
        }
    }

    private void processPreloadDo() {
        if (!mImageLoadRequestMap.isEmpty()) {
            for (final Map.Entry<String, ImageLoadRequest> entry : mImageLoadRequestMap.entrySet()) {
                if (mPreloadSet.size() >= mMaxPreloadCount) {
                    break;
                }

                if (TextUtils.isEmpty(entry.getKey()) || mPreloadSet.contains(entry.getKey())) {
                    continue;
                }

                mPreloadSet.add(entry.getKey());

                ThreadManager.get().executeMain(new Runnable() {
                    @Override
                    public void run() {
                        ImageLoadRequest request = entry.getValue();
                        request.setTarget(new PreloadSimpleTarget(entry.getKey()));
                        RequestManager requestManager = null;
                        Object loadContext = request.getLoadContext();
                        if (loadContext != null) {
                            if (loadContext instanceof Fragment) {
                                requestManager = Glide.with((Fragment) loadContext);
                            } else if (loadContext instanceof android.app.Fragment) {
                                requestManager = Glide.with((android.app.Fragment) loadContext);
                            } else if (loadContext instanceof FragmentActivity) {
                                requestManager = Glide.with((FragmentActivity) loadContext);
                            } else if (loadContext instanceof Activity) {
                                requestManager = Glide.with((Activity) loadContext);
                            } else if (loadContext instanceof Context) {
                                requestManager = Glide.with((Context) loadContext);
                            }

                            if (requestManager == null) {
                                requestManager = Glide.with(CommonApplication.sApplicationContext);
                            }
                        }

                        if (requestManager == null) {
                            return;
                        }

                        DrawableRequestBuilder finalRequest =
                                requestManager.load(Uri.parse(entry.getKey()))
                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                                        .priority(Priority.LOW);
                        int width = Math.min(request.getWidth(), ViewUtil.getScreenWidth(CommonApplication.sApplicationContext));
                        int height = Math.min(request.getHeight(), ViewUtil.getScreenHeight(CommonApplication.sApplicationContext));

                        if (width > 0 && height > 0) {
                            finalRequest.preload(width, height);
                        } else {
                            finalRequest.preload();
                        }

                    }
                });

            }
        }
    }

    private void processPreloadSuccess(String url) {
        mPreloadSet.remove(url);
        ImageLoadRequest imageLoadRequest = mImageLoadRequestMap.remove(url);
        if (imageLoadRequest != null) {
            ImageLoadCallback imageLoadCallback = imageLoadRequest.getImageLoadCallback();
            if (imageLoadCallback != null) {
                imageLoadCallback.onLoadSuccess(url);
            }
        }
    }

    private void processPreloadFail(String url) {
        mPreloadSet.remove(url);
        ImageLoadRequest imageLoadRequest = mImageLoadRequestMap.remove(url);
        if (imageLoadRequest != null) {
            ImageLoadCallback imageLoadCallback = imageLoadRequest.getImageLoadCallback();
            if (imageLoadCallback != null) {
                imageLoadCallback.onLoadFail(url);
            }
        }

    }

    private void processPreloadClear(String url) {
        mPreloadSet.remove(url);
        ImageLoadRequest imageLoadRequest = mImageLoadRequestMap.remove(url);
        if (imageLoadRequest != null) {
            ImageLoadCallback imageLoadCallback = imageLoadRequest.getImageLoadCallback();
            if (imageLoadCallback != null) {
                imageLoadCallback.onLoadClear(url);
            }
        }
    }

    class PreloadSimpleTarget<File> extends SimpleTarget<File> {

        private String mUrl;

        PreloadSimpleTarget(String url) {
            mUrl = url;
        }

        @Override
        public void onResourceReady(File resource, GlideAnimation<? super File> glideAnimation) {
            mPreloadHandler.obtainMessage(TYPE_PRELOAD_SUCCESS, mUrl).sendToTarget();
        }

        @Override
        public void onLoadFailed(Exception e, Drawable errorDrawable) {
            super.onLoadFailed(e, errorDrawable);
            mPreloadHandler.obtainMessage(TYPE_PRELOAD_FAIL, mUrl).sendToTarget();
        }

        @Override
        public void onLoadCleared(Drawable placeholder) {
            super.onLoadCleared(placeholder);
            mPreloadHandler.obtainMessage(TYPE_PRELOAD_CLEAR, mUrl).sendToTarget();
        }
    }

}

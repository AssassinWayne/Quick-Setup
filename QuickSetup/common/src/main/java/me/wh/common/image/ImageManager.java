package me.wh.common.image;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.DrawableTypeRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.FutureTarget;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import me.wh.common.CommonApplication;
import me.wh.common.thread.ThreadManager;
import me.wh.common.util.ViewUtil;

public class ImageManager {
    private static final ImageManager sInstance = new ImageManager();

    public static ImageManager get() {
        return sInstance;
    }

    private ImageManager() {

    }

    public void preloadOnly(Object loadContext, String url, int width, int height) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        RequestManager requestManager = null;
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
        }

        if (requestManager == null) {
            requestManager = Glide.with(CommonApplication.sApplicationContext);
        }

        DrawableTypeRequest drawableTypeRequest = requestManager.load(url);
        drawableTypeRequest.diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.NORMAL).preload(width, height);
    }

    public interface ImageExistCallback {
        void onExist(boolean exist, File file);
    }

    public void hasLoad(Object loadContext, String url, int width, int height, final ImageExistCallback callback) {
        if (TextUtils.isEmpty(url)) {
            callback.onExist(false, null);
            return;
        }

        RequestManager requestManager = null;
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
        }

        if (requestManager == null) {
            requestManager = Glide.with(CommonApplication.sApplicationContext);
        }

        final FutureTarget<File> future = requestManager
                .load(url).downloadOnly(width, height);
        ThreadManager.get().execute(new Runnable() {
            private boolean tempExist = false;
            private FutureTarget<File> tempFuture;
            private File tempFile;
            private CountDownLatch tempCountDownLatch;

            private Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        tempFile = tempFuture.get();
                        if (tempFile != null && tempFile.exists()) {
                            tempExist = true;
                            tempCountDownLatch.countDown();
                            return;
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                    tempCountDownLatch.countDown();
                }
            };

            @Override
            public void run() {
                if (future != null) {
                    try {
                        tempFuture = future;
                        tempCountDownLatch = new CountDownLatch(1);
                        ThreadManager.get().execute(runnable);
                        tempCountDownLatch.await(300, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                callback.onExist(tempExist, tempFile == null ? null : tempFile);
            }
        });
    }

    public void loadCover(Object loadContext, ImageView imageView, String url, int placeHolderId) {
        if (imageView == null || TextUtils.isEmpty(url)) {
            if (imageView != null && placeHolderId != 0) {
                imageView.setImageResource(placeHolderId);
            }
            return;
        }

        RequestManager requestManager = null;
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
        }

        if (requestManager == null && imageView.getContext() != null) {
            requestManager = Glide.with(imageView.getContext());
        }

        if (requestManager == null) {
            requestManager = Glide.with(CommonApplication.sApplicationContext);
        }

        DrawableTypeRequest drawableTypeRequest = requestManager.load(url);
        DrawableRequestBuilder drawableRequestBuilder = drawableTypeRequest;
        if (placeHolderId != 0) {
            drawableRequestBuilder = drawableRequestBuilder.placeholder(placeHolderId);
        }
//        if (imageView instanceof CircleImageView) {
//            drawableRequestBuilder = drawableRequestBuilder.dontAnimate();
//        } else {
//            drawableRequestBuilder = drawableRequestBuilder.crossFade();
//        }
        drawableRequestBuilder = drawableRequestBuilder.crossFade();
        drawableRequestBuilder.into(imageView);
    }

    public void loadImage(Object loadContext, ImageView imageView, String url, int placeHolderId, int errorHolderId) {
        loadImage(loadContext, imageView, url, placeHolderId, errorHolderId, 0, 0);
    }

    public void loadImage(Object loadContext, ImageView imageView, String url, int placeHolderId, int errorHolderId, int width, int height) {
        if (imageView == null || TextUtils.isEmpty(url)) {
            if (imageView != null && placeHolderId != 0) {
                imageView.setImageResource(placeHolderId);
            }
            return;
        }

        RequestManager requestManager = null;
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
        }

        if (requestManager == null && imageView.getContext() != null) {
            requestManager = Glide.with(imageView.getContext());
        }

        if (requestManager == null) {
            requestManager = Glide.with(CommonApplication.sApplicationContext);
        }

        DrawableTypeRequest drawableTypeRequest = requestManager.load(url);
        DrawableRequestBuilder drawableRequestBuilder = drawableTypeRequest;
        if (placeHolderId != 0) {
            drawableRequestBuilder = drawableRequestBuilder.placeholder(placeHolderId);
        }
        if (errorHolderId != 0) {
            drawableRequestBuilder = drawableRequestBuilder.error(errorHolderId);
        }

        width = Math.min(width, ViewUtil.getScreenWidth(CommonApplication.sApplicationContext));
        height = Math.min(height, ViewUtil.getScreenHeight(CommonApplication.sApplicationContext));
        DrawableRequestBuilder builder = drawableRequestBuilder
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.NORMAL);
        if (width > 0 && height > 0) {
            builder.override(width, height);
        }
//        if (imageView instanceof CircleImageView) {
//            builder = builder.dontAnimate();
//        } else {
//            builder = builder.crossFade();
//        }
        builder = builder.crossFade();
        builder.into(imageView);
    }

    public void preloadImage(@NonNull Object loadContext, String url, int width, int height) {
        ImagePreloadManager.get().preload(loadContext, url, width, height);
    }

    public void cancelPreload(String url) {
        ImagePreloadManager.get().cancelPreload(url);
    }
}

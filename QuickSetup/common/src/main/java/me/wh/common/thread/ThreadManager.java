package me.wh.common.thread;

import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadManager {
    private static final ThreadManager sInstance = new ThreadManager();
    private static final String HANDLER_THREAD_NAME_DEFAULT = "custom-handler-thread";

    public static ThreadManager get() {
        return sInstance;
    }

    private ConcurrentHashMap<String, HandlerThread> mHandlerThreadMap = new ConcurrentHashMap<String, HandlerThread>();
    private Handler mAsyncHandler;
    private Handler mMainHandler;

    public Looper getHandlerThreadLooper() {
        return getHandlerThreadLooper(null);
    }

    public Looper getHandlerThreadLooper(String name) {
        if (name == null || name.length() == 0) {
            name = HANDLER_THREAD_NAME_DEFAULT;
        }
        HandlerThread handlerThread = mHandlerThreadMap.get(name);
        if (handlerThread == null) {
            handlerThread = new HandlerThread(name);
            handlerThread.start();
            mHandlerThreadMap.put(name, handlerThread);
        }
        return handlerThread.getLooper();
    }


    public void quitHandThreadLooper(String name) {
        if (name != null && name.length() > 0) {
            HandlerThread handlerThread = mHandlerThreadMap.remove(name);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                handlerThread.quitSafely();
            } else {
                handlerThread.quit();
            }
        }
    }

    private synchronized void initAsyncHandler() {
        if (mAsyncHandler == null) {
            mAsyncHandler = new Handler(getHandlerThreadLooper());
        }
    }

    public void executeAsync(Runnable runnable) {
        if (runnable != null) {
            if (mAsyncHandler == null) {
                initAsyncHandler();
            }
            mAsyncHandler.post(runnable);
        }
    }

    public void executeAsync(Runnable runnable, long delay) {
        if (runnable != null) {
            if (mAsyncHandler == null) {
                initAsyncHandler();
            }
            mAsyncHandler.postDelayed(runnable, delay);
        }
    }

    private synchronized void initMainHandler() {
        if (mMainHandler == null) {
            mMainHandler = new Handler(Looper.getMainLooper());
        }
    }

    public void executeMain(Runnable runnable) {
        if (runnable != null) {
            if (mMainHandler == null) {
                initMainHandler();
            }
            mMainHandler.post(runnable);
        }
    }

    public void executeMain(Runnable runnable, long delay) {
        if (runnable != null) {
            if (mMainHandler == null) {
                initMainHandler();
            }
            mMainHandler.postDelayed(runnable, delay);
        }
    }

    private PriorityThreadPoolExecutor mThreadPoolExecutor = new PriorityThreadPoolExecutor();

    public void executePriority(Runnable runnable) {
        mThreadPoolExecutor.execute(runnable);
    }

    public void removePriority(Runnable runnable) {
        mThreadPoolExecutor.remove(runnable);
    }

    private ScheduledExecutorService mScheduledExecutorService = new ScheduledThreadPoolExecutor(PriorityThreadPoolExecutor.mCorePoolSize, new RunOldRejectedExecutionHandler());


    public ScheduledFuture execute(Runnable runnable) {
        return mScheduledExecutorService.schedule(runnable, 0, TimeUnit.MILLISECONDS);
    }

    public ScheduledFuture execute(Runnable runnable, long delay) {
        return mScheduledExecutorService.schedule(runnable, delay, TimeUnit.MILLISECONDS);
    }

    public ScheduledFuture executeFixedRate(Runnable runnable, long initDelay, long period) {
        return mScheduledExecutorService.scheduleAtFixedRate(runnable, initDelay, period, TimeUnit.MILLISECONDS);
    }

    public ScheduledFuture executeFixedDelay(Runnable runnable, long initDelay, long delay) {
        return mScheduledExecutorService.scheduleWithFixedDelay(runnable, initDelay, delay, TimeUnit.MILLISECONDS);
    }

    public Executor getExecutor() {
        return mThreadPoolExecutor;
    }
}

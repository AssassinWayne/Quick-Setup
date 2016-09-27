package me.wh.common.http;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;

import com.trello.rxlifecycle.LifecycleProvider;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import me.wh.common.thread.ThreadManager;
import me.wh.common.util.LogUtil;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import okio.Buffer;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.schedulers.Schedulers;

public class HttpManager {

    private static final HttpManager sInstance = new HttpManager();
    private static final String HANDLER_THREAD_NAME_CALL = "http-call-handle-thread";

    private static final int TYPE_CALL_ADD = 0;
    private static final int TYPE_CALL_DELETE = 1;
    private static final int TYPE_CALL_CLEAR = 2;

    private final HashMap<Class, Object> mServiceMap = new HashMap<>();
    private final HashMap<Object, HashSet<Call>> mCallMap = new HashMap<>();
    private final HashMap<String, String> mMoreInfo = new HashMap<>();

    private final Object mInitServiceLock = new Object();
    private final Object mInitCallSetLock = new Object();

    private OkHttpClient mClient;
    private String mBaseApi;
    private String mAuthorization;
    private int mConnectTimeout = 30;
    private int mWriteTimeout = 30;
    private int mReadTimeout = 30;

    private OnAuthorizationListener mOnAuthorizationListener;

    private Handler mCallHandler = new Handler(ThreadManager.get().getHandlerThreadLooper(HANDLER_THREAD_NAME_CALL)) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case TYPE_CALL_ADD:
                    processCallAdd((CallEntity) msg.obj);
                    break;
                case TYPE_CALL_DELETE:
                    processCallDelete((CallEntity) msg.obj);
                    break;
                case TYPE_CALL_CLEAR:
                    processCallClear(msg.obj);
                    break;
            }
        }
    };

    private Interceptor mMoreParamsInterceptor = new Interceptor() {
        @Override
        public okhttp3.Response intercept(Chain chain) throws IOException {
            Request original = chain.request();
            String method = original.method().toLowerCase();
            Request.Builder requestBuilder = original.newBuilder();

            if ("get".equals(method)) {
                if (!mMoreInfo.isEmpty()) {
                    HttpUrl originalHttpUrl = original.url();
                    HttpUrl.Builder httpUrlBuilder = originalHttpUrl.newBuilder();
                    for (Map.Entry<String, String> entry : mMoreInfo.entrySet()) {
                        if (entry != null && !TextUtils.isEmpty(entry.getKey()) && !TextUtils.isEmpty(entry.getValue())) {
                            httpUrlBuilder.addEncodedQueryParameter(entry.getKey(), entry.getValue());
                        }
                    }
                    HttpUrl url = httpUrlBuilder.build();
                    requestBuilder = original.newBuilder()
                            .url(url);
                }
            } else if ("post".equals(method)) {
                String noMoreHead = original.header("no_more");
                if (TextUtils.isEmpty(noMoreHead) && !mMoreInfo.isEmpty()) {
                    FormBody.Builder formBodyBuilder = new FormBody.Builder();
                    for (Map.Entry<String, String> entry : mMoreInfo.entrySet()) {
                        if (entry != null && !TextUtils.isEmpty(entry.getKey()) && !TextUtils.isEmpty(entry.getValue())) {
                            formBodyBuilder.add(entry.getKey(), entry.getValue());
                        }
                    }
                    RequestBody moreRequestBody = formBodyBuilder.build();
                    Buffer buffer = new Buffer();
                    moreRequestBody.writeTo(buffer);
                    RequestBody requestBody = original.body();
                    if (requestBody != null && requestBody.contentLength() > 0) {
                        if (moreRequestBody.contentLength() > 0) {
                            buffer.writeByte('&');
                        }
                        requestBody.writeTo(buffer);
                    }
                    requestBuilder = original.newBuilder()
                            .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded;charset=UTF-8"), buffer.readByteArray()));
                }
            }

            String noTokenHead = original.header("no_token");
            if (TextUtils.isEmpty(noTokenHead) && mOnAuthorizationListener != null) {
                mAuthorization = mOnAuthorizationListener.getAuthorization();
                LogUtil.d(this.getClass(), "authorization = " + mAuthorization);
            }

            if (TextUtils.isEmpty(noTokenHead) && !TextUtils.isEmpty(mAuthorization)) {
                requestBuilder.addHeader("Authorization", mAuthorization);
            }

            return chain.proceed(requestBuilder.build());
        }
    };

    public static HttpManager getInstance() {
        return sInstance;
    }

    public void config(String baseApi, int connectTimeout, int writeTimeout, int readTimeout, HashMap<String, String> moreInfo) {
        mBaseApi = baseApi;
        mConnectTimeout = connectTimeout;
        mWriteTimeout = writeTimeout;
        mReadTimeout = readTimeout;

        mMoreInfo.clear();
        if (moreInfo != null && !moreInfo.isEmpty()) {
            mMoreInfo.putAll(moreInfo);
        }
    }

    public HttpManager authorization(OnAuthorizationListener listener) {
        mOnAuthorizationListener = listener;
        return this;
    }

    public <T> T getService(Class<T> clazz) {
        if (clazz == null) {
            return null;
        }
        Object mService = mServiceMap.get(clazz);
        if (mService == null) {
            synchronized (mInitServiceLock) {
                mService = mServiceMap.get(clazz);
                if (mService == null) {
                    Retrofit retrofit = new Retrofit.Builder()
                            .addConverterFactory(GsonConverterFactory.create())
                            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                            .client(getOkHttpClient())
                            .baseUrl(mBaseApi)
                            .build();

                    mService = retrofit.create(clazz);
                    mServiceMap.put(clazz, mService);
                }
            }
        }
        return (T) mService;
    }

    @SuppressWarnings("unchecked")
    public <T> Observable<T> observable(Object lifecycleProvider, @NonNull Observable<T> observable) {
        if (lifecycleProvider != null && lifecycleProvider instanceof LifecycleProvider) {
            return observable(((LifecycleProvider) lifecycleProvider), observable);
        } else {
            return observable(observable);
        }
    }

    private <T, R> Observable<T> observable(LifecycleProvider<R> lifecycleProvider, @NonNull Observable<T> observable) {
        return observable(observable).compose(lifecycleProvider.<T>bindToLifecycle());
    }

    private <T> Observable<T> observable(@NonNull Observable<T> observable) {
        return observable.subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io());
    }

    public void clear(Activity activity) {
        clear((Object) activity);
    }

    public void clear(Fragment fragment) {
        clear((Object) fragment);
    }

    public void clear(FragmentActivity activity) {
        clear((Object) activity);
    }

    public void clear(android.app.Fragment fragment) {
        clear((Object) fragment);
    }

    public void clear(Context context) {
        clear((Object) context);
    }

    private void clear(Object object) {
        if (object != null) {
            mCallHandler.obtainMessage(TYPE_CALL_CLEAR, object).sendToTarget();
        }
    }

    private synchronized OkHttpClient getOkHttpClient() {
        if (mClient == null) {
            OkHttpClient.Builder builder = new OkHttpClient.Builder();

            builder = builder.connectTimeout(mConnectTimeout, TimeUnit.SECONDS)
                    .addInterceptor(mMoreParamsInterceptor);
            if (LogUtil.DEBUG) {
                builder = builder.addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY));
            }
            builder.writeTimeout(mWriteTimeout, TimeUnit.SECONDS)
                    .readTimeout(mReadTimeout, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .followRedirects(true)
                    .followSslRedirects(true);

            mClient = builder.build();
        }

        return mClient;
    }

    private void processCallAdd(CallEntity entity) {
        Object object = entity.object;
        Call call = entity.call;
        if (object != null) {
            HashSet<Call> callSet = mCallMap.get(object);
            if (callSet == null) {
                synchronized (mInitCallSetLock) {
                    callSet = mCallMap.get(object);
                    if (callSet == null) {
                        callSet = new HashSet<>();
                        mCallMap.put(object, callSet);
                    }
                }
            }

            callSet.add(call);
        }
    }

    private void processCallDelete(CallEntity entity) {
        Object object = entity.object;
        Call call = entity.call;
        if (object != null) {
            HashSet<Call> callSet = mCallMap.get(object);
            if (callSet != null) {
                callSet.remove(call);
            }
            if (callSet == null || callSet.isEmpty()) {
                mCallMap.remove(object);
            }
        }
    }

    private void processCallClear(Object object) {
        if (object != null) {
            HashSet<Call> callSet = mCallMap.remove(object);
            if (callSet != null) {
                for (Call call : callSet) {
                    call.cancel();
                }
            }
        }
    }

    public interface OnAuthorizationListener {
        String getAuthorization();
    }

    private HttpManager() {
    }

    class CallEntity {
        Object object;
        Call call;

        CallEntity(Object o, Call c) {
            object = o;
            call = c;
        }
    }
}

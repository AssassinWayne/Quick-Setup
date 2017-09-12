package me.wh.common.http;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.trello.rxlifecycle2.LifecycleProvider;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
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
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class HttpManager {

    private static final HttpManager sInstance = new HttpManager();

    private final HashMap<Class, Object> mServiceMap = new HashMap<>();
    private final HashMap<String, String> mMoreInfo = new HashMap<>();

    private final Object mInitServiceLock = new Object();

    private OkHttpClient mClient;
    private String mBaseApi;
    private String mAuthorization;
    private int mConnectTimeout = 30;
    private int mWriteTimeout = 30;
    private int mReadTimeout = 30;

    private OnAuthorizationListener mOnAuthorizationListener;

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
                            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
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
        return observable(observable.compose(lifecycleProvider.<T>bindToLifecycle()));
    }

    private <T> Observable<T> observable(@NonNull Observable<T> observable) {
        return observable.subscribeOn(Schedulers.io()).unsubscribeOn(Schedulers.io());
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

    public interface OnAuthorizationListener {
        String getAuthorization();
    }

    private HttpManager() {
    }

}

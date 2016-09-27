package me.wh.quicksetup.base;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import me.wh.common.http.HttpManager;
import me.wh.common.http.data.DataResponse;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.observers.Subscribers;

/**
 * Created by WayneHu on 16/9/27.
 */
public abstract class BaseLogic {

    public ApiService getApi() {
        return getApi(null);
    }

    public ApiService getApi(Object lifecycleProvider) {
        final ApiService service = HttpManager.getInstance().getService(ApiService.class);
        final InvocationHandler invoker = new ServiceInvoker(service, lifecycleProvider);
        return (ApiService) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{ApiService.class}, invoker);
    }

    private static class ServiceInvoker implements InvocationHandler {

        private final Object mService;
        private final Object mLifecycleProvider;

        public ServiceInvoker(Object service, Object lifecycleProvider) {
            mService = service;
            mLifecycleProvider = lifecycleProvider;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
            Object result = method.invoke(mService, objects);
            if (result instanceof Observable) {
                Observable observable = (Observable) result;
                observable = HttpManager.getInstance().observable(mLifecycleProvider, observable);
                observable.lift(new Observable.Operator<Subscriber, Subscriber>() {
                    @Override
                    public Subscriber call(final Subscriber subscriber) {
                        return Subscribers.create(new Action1() {
                            @Override
                            public void call(Object o) {
                                if (o instanceof DataResponse) {
                                    if (((DataResponse) o).isSuccess()) {
                                        subscriber.onNext(o);
                                    } else {
                                        throw new ApiError((DataResponse) o);
                                    }
                                } else {
                                    subscriber.onCompleted();
                                }
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                subscriber.onError(throwable);
                            }
                        }, new Action0() {
                            @Override
                            public void call() {
                                subscriber.onCompleted();
                            }
                        });
                    }
                });
            }
            return result;
        }
    }
}

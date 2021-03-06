package me.wh.quicksetup.base;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import io.reactivex.Observable;
import me.wh.common.http.HttpManager;

/**
 * Created by WayneHu on 16/9/27.
 */
public abstract class BaseLogic {

    protected GitHubApi getApi() {
        return getApi(null);
    }

    protected GitHubApi getApi(Object lifecycleProvider) {
        final GitHubApi service = HttpManager.getInstance().getService(GitHubApi.class);
        final InvocationHandler invoker = new ServiceInvoker(service, lifecycleProvider);
        return (GitHubApi) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{GitHubApi.class}, invoker);
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
                return  HttpManager.getInstance().observable(mLifecycleProvider, observable);
//                return observable.lift(new Observable.Operator<Subscriber, Subscriber>() {
//                    @Override
//                    public Subscriber call(final Subscriber subscriber) {
//                        return Subscribers.create(new Action1() {
//                            @Override
//                            public void call(Object o) {
//                                subscriber.onNext(o);
//                            }
//                        }, new Action1<Throwable>() {
//                            @Override
//                            public void call(Throwable throwable) {
//                                subscriber.onError(throwable);
//                            }
//                        }, new Action0() {
//                            @Override
//                            public void call() {
//                                subscriber.onCompleted();
//                            }
//                        });
//                    }
//                });
            }
            return result;
        }
    }
}

package me.wh.quicksetup.logic;

import com.trello.rxlifecycle2.LifecycleProvider;

import java.util.List;

import io.reactivex.functions.Consumer;
import me.wh.quicksetup.base.BaseLogic;
import me.wh.quicksetup.pojo.GitHubUser;

/**
 * GitHub Logic Layer
 * Created by WayneHu on 16/9/27.
 */
public class GitHubLogic extends BaseLogic {

    private static GitHubLogic sInstance = new GitHubLogic();

    public static GitHubLogic get() {
        return sInstance;
    }

    public void getFollowing(LifecycleProvider lifecycleProvider, final String user) {
        getApi(lifecycleProvider)
                .getFollowing(user)
                .subscribe(new Consumer<List<GitHubUser>>() {
                    @Override
                    public void accept(List<GitHubUser> gitHubUsers) throws Exception {

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                    }
                });
//                .subscribe(new Observer<List<GitHubUser>>() {
//                    @Override
//                    public void onSubscribe(Disposable d) {
//
//                    }
//
//                    @Override
//                    public void onNext(List<GitHubUser> gitHubUsers) {
//
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//
//                    }
//
//                    @Override
//                    public void onComplete() {
//
//                    }
//                });
//                .subscribe(new Action1<List<GitHubUser>>() {
//                    @Override
//                    public void call(List<GitHubUser> userList) {
//                        // Success
//                    }
//                }, new Action1<Throwable>() {
//                    @Override
//                    public void call(Throwable throwable) {
//                        // Fail
//                    }
//                });

    }
}

package me.wh.quicksetup.logic;

import com.trello.rxlifecycle.LifecycleProvider;

import java.util.List;

import me.wh.quicksetup.base.BaseLogic;
import me.wh.quicksetup.pojo.GitHubUser;
import rx.functions.Action1;

/**
 * Created by WayneHu on 16/9/27.
 */
public class GitHubLogic extends BaseLogic {

    private static GitHubLogic sInstance = new GitHubLogic();

    public static GitHubLogic get() {
        return sInstance;
    }

    public void getFollowing(LifecycleProvider lifecycleProvider, final String user) {
        getApi(lifecycleProvider).getFollowing(user)
                .subscribe(new Action1<List<GitHubUser>>() {
                    @Override
                    public void call(List<GitHubUser> userList) {
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });
    }
}

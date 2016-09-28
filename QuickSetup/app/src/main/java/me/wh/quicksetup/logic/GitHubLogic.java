package me.wh.quicksetup.logic;

import com.trello.rxlifecycle.LifecycleProvider;

import me.wh.common.http.data.DataListResponse;
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

    public void getFollowing(LifecycleProvider lifecycleProvider, String user) {
        getApi(lifecycleProvider).getFollowing(user)
                .subscribe(new Action1<DataListResponse<GitHubUser>>() {
                    @Override
                    public void call(DataListResponse<GitHubUser> userDataSingleResponse) {

                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });
    }
}

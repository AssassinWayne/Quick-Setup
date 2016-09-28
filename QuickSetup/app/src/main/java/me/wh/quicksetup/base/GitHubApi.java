package me.wh.quicksetup.base;

import java.util.List;

import me.wh.quicksetup.pojo.GitHubUser;
import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

/**
 * Created by WayneHu on 16/9/27.
 */
public interface GitHubApi {

    @GET("users/{user}/following")
    Observable<List<GitHubUser>> getFollowing(@Path("user") String user);

}

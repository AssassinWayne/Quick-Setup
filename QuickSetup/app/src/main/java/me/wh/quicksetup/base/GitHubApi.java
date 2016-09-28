package me.wh.quicksetup.base;

import me.wh.common.http.data.DataListResponse;
import me.wh.quicksetup.pojo.GitHubUser;
import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

/**
 * Created by WayneHu on 16/9/27.
 */
public interface GitHubApi {
    @GET("users/{user}/following")
    Observable<DataListResponse<GitHubUser>> getFollowing(@Path("user") String user);

}

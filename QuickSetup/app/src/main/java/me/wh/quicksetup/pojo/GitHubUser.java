package me.wh.quicksetup.pojo;

import com.google.gson.annotations.SerializedName;

/**
 * Created by WayneHu on 16/9/27.
 */
public class GitHubUser {
    String login;
    long id;
    @SerializedName("avatar_url")
    String avatarUrl;
    @SerializedName("gravatar_id")
    String gravatarId;
    String url;
    @SerializedName("html_url")
    String htmlUrl;
    @SerializedName("followers_url")
    String followersUrl;
    @SerializedName("following_url")
    String followingUrl;
    @SerializedName("gists_url")
    String gistsUrl;
    @SerializedName("starred_url")
    String starredUrl;
    @SerializedName("subscriptions_url")
    String subscriptionsUrl;
    @SerializedName("organizations_url")
    String organizationsUrl;
    @SerializedName("repos_url")
    String reposUrl;
    @SerializedName("events_url")
    String eventsUrl;
    @SerializedName("received_events_url")
    String receivedEventsUrl;
    String type;
    @SerializedName("site_admin")
    boolean siteAdmin;
}

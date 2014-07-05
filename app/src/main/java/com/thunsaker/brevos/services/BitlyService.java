package com.thunsaker.brevos.services;

import com.thunsaker.brevos.data.api.ExpandResponse;
import com.thunsaker.brevos.data.api.LinkClicksListResponse;
import com.thunsaker.brevos.data.api.LinkClicksResponse;
import com.thunsaker.brevos.data.api.LinkInfoResponse;
import com.thunsaker.brevos.data.api.ShortenResponse;
import com.thunsaker.brevos.data.api.ShortenSaveResponse;
import com.thunsaker.brevos.data.api.TrendingLinksResponse;
import com.thunsaker.brevos.data.api.UserHistoryResponse;

import retrofit.http.GET;
import retrofit.http.Query;

public interface BitlyService {
    /*
        Shorten Link
        http://dev.bitly.com/links.html#v3_shorten
        /v3/shorten
    */
    @GET("/shorten")
    ShortenResponse createBitmarkWithoutAuth(@Query("login") String login, @Query("apiKey") String apiKey, @Query("longUrl") String longUrl, @Query("domain") String domain);

    @GET("/shorten")
    ShortenResponse createBitmark(@Query("access_token") String access_token, @Query("longUrl") String longUrl, @Query("domain") String domain);

    /*
        Shorten Link (Link Save)
        http://dev.bitly.com/links.html#v3_user_link_save
        /v3/user/link_save
     */
    @GET("/user/link_save")
    ShortenSaveResponse saveLink(@Query("access_token") String access_token, @Query("longUrl") String longUrl, @Query("private") Boolean isPrivate);

    /*
        Expand Link
        http://dev.bitly.com/links.html#v3_expand
        /v3/expand
    */
    @GET("/expand")
    ExpandResponse expandBitmarkWithoutAuth(@Query("login") String login, @Query("apiKey") String apiKey, @Query("shortUrl") String shortUrl);

    @GET("/expand")
    ExpandResponse expandBitmark(@Query("access_token") String access_token, @Query("shortUrl") String shortUrl);

    /*
        Link Info
        http://dev.bitly.com/data_apis.html#v3_link_info
        /v3/link/info?access_token=ACCESS_TOKEN&link=http%3A%2F%2Fbit.ly%2FMwSGaQ
    */
    @GET("/link/info")
    LinkInfoResponse getLinkInfo(@Query("access_token") String access_token, @Query("link") String link);

    /*
        Link Clicks
        http://dev.bitly.com/data_apis.html#v3_link_clicks
        /v3/link/clicks?access_token=ACCESS_TOKEN&link=http%3A%2F%2Fbit.ly%2FMwSGaQ
    */
    @GET("/link/clicks")
    LinkClicksResponse getLinkClicksTotal(@Query("access_token") String access_token, @Query("link") String link, @Query("unit") String unit, @Query("units") int units);

    @GET("/link/clicks")
    LinkClicksListResponse getLinkClicks(@Query("access_token") String access_token, @Query("link") String link, @Query("unit") String unit, @Query("units") int units, @Query("rollup") boolean rollup, @Query("timezone") int timezone);
//    LinkClicksListResponse getLinkClicks(@Query("access_token") String access_token, @Query("link") String link, @Query("unit") String unit, @Query("units") int units, @Query("rollup") boolean rollup);

    /*
        User History
        http://dev.bitly.com/user_info.html#v3_user_link_history
        /v3/user/link_history?access_token=ACCESS_TOKEN
     */
    @GET("/user/link_history")
    UserHistoryResponse getUserLinkHistory(@Query("access_token") String access_token, @Query("limit") int limit, @Query("offset") int offset, @Query("query") String query, @Query("archived") String archivedLinks, @Query("private") String privateLinks);

    @GET("/highvalue")
    TrendingLinksResponse getTrendingLinks(@Query("access_token") String access_token, @Query("limit") int limit);
}
package com.thunsaker.brevos.data.api;

import com.google.gson.Gson;

public class BitmarkInfo {
    public String canonical_url;
    public String category;
    public String content_length;
    public String content_type;
    public String domain;
    public String favicon_url;
    public String global_hash;
    public String html_title;
    public String http_code;
    public String indexed;
    public String[] linktags_other;
    public String[] metatags_name;
    public String original_url;
    public String robots_allowed;
    public String source_domain;
    public String aggregate_link;
    public String url_fetched;

    public BitmarkInfo(String shortUrl, String longUrl, String title) {
        this.aggregate_link = shortUrl;
        this.original_url = longUrl;
        this.html_title = title;
    }

    @Override
    public String toString() {
        return toJson(this);
    }

    public static String toJson(BitmarkInfo bitmarkInfo) {
        Gson gson = new Gson();
        return bitmarkInfo != null ? gson.toJson(bitmarkInfo) : "";
    }

    public static BitmarkInfo GetBitmarkInfoFromJson(String myBitmarkInfoJson) {
        Gson gson = new Gson();
        return gson.fromJson(myBitmarkInfoJson, BitmarkInfo.class);
    }
}
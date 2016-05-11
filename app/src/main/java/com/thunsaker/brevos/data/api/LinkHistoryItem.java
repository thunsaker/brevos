package com.thunsaker.brevos.data.api;

import com.google.gson.Gson;

import java.util.List;

public class LinkHistoryItem {
    public boolean archived;
    public String user_ts;
    public String title;
    public long created_at;
    public long modified_at;
    public boolean isPrivate;
    public String aggregate_link;
    public String long_url;
    public String client_id;
    public String link;
    public Integer clicks = -1;
    public List<BitlyShares> shares;
    public int average_color = -1;

    @Override
    public String toString() {
        return toJson(this);
    }

    public static String toJson(LinkHistoryItem myLinkHistoryItem) {
        Gson gson = new Gson();
        return myLinkHistoryItem != null ? gson.toJson(myLinkHistoryItem) : "";
    }
}

/*

{
	"status_code": 200,
	"data": {
		"link_history": [
			{
				"archived": false,
				"user_ts": 1350588710,
				"title": "Home",
				"created_at": 1350588710,
				"modified_at": 1350588723,
				"private": false,
				"aggregate_link": "http://bit.ly/R0dNko",
				"long_url": "http://alishahunsaker.com/",
				"client_id": "b50b83934bca057dd86ca1bcdd3c51749beb240f",
				"link": "http://bit.ly/R6PiUY"
			}, {
				"archived": false,
				"user_ts": 1349890983,
				"title": "Android Developers Blog: Building Quality Tablet Apps",
				"created_at": 1349890983,
				"modified_at": 1349890985,
				"private": false,
				"aggregate_link": "http://bit.ly/PPLF1n",
				"long_url": "http://android-developers.blogspot.com/2012/10/building-quality-tablet-apps.html",
				"client_id": "e66368c40b4c83046addfbd205ed8493ec4744a3",
				"link": "http://bit.ly/SQgFox"
			}
		],
		"result_count": 2382
	},
	"status_txt": "OK"
}

 */
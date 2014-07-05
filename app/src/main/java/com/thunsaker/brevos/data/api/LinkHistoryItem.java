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

//	public LinkHistoryItem() {
//		setArchived(false);
//		setUser_ts("");
//		setTitle("");
//		setCreated_at(0);
//		setModified_at(0);
//		setIsPrivate(false);
//		setAggregate_link("");
//		setLong_url("");
//		setClient_id("");
//		setLink("");
//		setLink_clicks_day(0);
//	}
//
//	public Boolean getArchived() {
//		return archived;
//	}
//	public void setArchived(Boolean archived) {
//		this.archived = archived;
//	}
//
//	public String getUser_ts() {
//		return user_ts;
//	}
//	public void setUser_ts(String user_ts) {
//		this.user_ts = user_ts;
//	}
//
//	public String getTitle() {
//		return title;
//	}
//	public void setTitle(String title) {
//		this.title = title;
//	}
//
//	public long getCreated_at() {
//		return created_at;
//	}
//	public void setCreated_at(long created_at) {
//		this.created_at = created_at;
//	}
//
//	public long getModified_at() {
//		return modified_at;
//	}
//	public void setModified_at(long modified_at) {
//		this.modified_at = modified_at;
//	}
//
//	public Boolean getIsPrivate() {
//		return isPrivate;
//	}
//	public void setIsPrivate(Boolean isPrivate) {
//		this.isPrivate = isPrivate;
//	}
//
//	public String getAggregate_link() {
//		return aggregate_link;
//	}
//	public void setAggregate_link(String aggregate_link) {
//		this.aggregate_link = aggregate_link;
//	}
//
//	public String getLong_url() {
//		return long_url;
//	}
//	public void setLong_url(String long_url) {
//		this.long_url = long_url;
//	}
//
//	public String getClient_id() {
//		return client_id;
//	}
//	public void setClient_id(String client_id) {
//		this.client_id = client_id;
//	}
//
//	public String getLink() {
//		return link;
//	}
//	public void setLink(String link) {
//		this.link = link;
//	}
//
//	public Integer getLink_clicks_day() {
//		return link_clicks_day;
//	}
//	public void setLink_clicks_day(Integer link_clicks_day) {
//		this.link_clicks_day = link_clicks_day;
//	}
//

//
//	public static LinkHistoryItem GetLinkHistoryItemFromJson(String myLinkHistoryItemJson) {
//		Gson gson = new Gson();
//		LinkHistoryItem myLinkHistoryItem = gson.fromJson(myLinkHistoryItemJson, LinkHistoryItem.class);
//		return myLinkHistoryItem;
//	}
//
//    @Deprecated
//	public static LinkHistoryItem GetLinkHistoryItemFromJson(JsonObject myjObjectLinkHistory) {
//		try {
//			LinkHistoryItem myLink = new LinkHistoryItem();
//
//			if(myjObjectLinkHistory != null) {
//				myLink.setLink(myjObjectLinkHistory.get("link") != null
//						? myjObjectLinkHistory.get("link").toString().replace("\"", "")
//								: "");
//				myLink.setLong_url(myjObjectLinkHistory.get("long_url") != null
//						? myjObjectLinkHistory.get("long_url").toString().replace("\"", "")
//								: "");
//				myLink.setTitle(myjObjectLinkHistory.get("title") != null
//						? myjObjectLinkHistory.get("title").toString().replace("\"", "")
//								: "");
//				myLink.setAggregate_link(myjObjectLinkHistory.get("aggregate_link") != null
//						? myjObjectLinkHistory.get("aggregate_link").toString().replace("\"", "")
//								: "");
//				myLink.setClient_id(myjObjectLinkHistory.get("client_id") != null
//						? myjObjectLinkHistory.get("client_id").toString().replace("\"", "")
//								: "");
//                // Switch these to longs if I ever use this method again
////				myLink.setCreated_at(myjObjectLinkHistory.get("created_at") != null
////						? myjObjectLinkHistory.get("created_at").toString().replace("\"", "")
////								: "");
////				myLink.setModified_at(myjObjectLinkHistory.get("modified_at") != null
////						? myjObjectLinkHistory.get("modified_at").toString().replace("\"", "")
////								: "");
//				myLink.setUser_ts(myjObjectLinkHistory.get("created_at") != null
//						? myjObjectLinkHistory.get("created_at").toString().replace("\"", "")
//								: "");
//				myLink.setIsPrivate(myjObjectLinkHistory.get("private") != null
//						? Boolean.parseBoolean(myjObjectLinkHistory.get("private").toString().replace("\"", ""))
//								: false);
//				myLink.setArchived(myjObjectLinkHistory.get("archive") != null
//						? Boolean.parseBoolean(myjObjectLinkHistory.get("archive").toString().replace("\"", ""))
//								: false);
//			}
//
//			return myLink;
//		} catch (Exception ex) {
//			Log.e("ShortUrl", "Error with GetShortUrlFromLinkSaveJson: " + ex.getMessage());
//			return null;
//		}
//	}
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
package com.thunsaker.brevos.data.api;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class Bitmark {
	private String long_url;
	private String url;
	private String hash;
	private String global_hash;
	private String new_hash;
    private Boolean is_private;
    private String aggregate_url;

	public Bitmark() {
        this("", "");
	}

    public Bitmark(String short_url) {
        this(short_url, "");
    }

    public Bitmark(String short_url, String long_url) {
        this(short_url, long_url, "");
    }

    public Bitmark(String short_url, String long_url, String aggregate_url) {
        this.url = short_url;
        this.long_url = long_url;
        this.aggregate_url = aggregate_url;

        this.hash = "";
        this.global_hash = "";
        this.new_hash = "";
        this.is_private = false;
    }



	public String getLong_url() {
		return long_url;
	}
	public void setLong_url(String long_url) {
		this.long_url = long_url;
	}

	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}

	public String getHash() {
		return hash;
	}
	public void setHash(String hash) {
		this.hash = hash;
	}

	public String getGlobal_hash() {
		return global_hash;
	}
	public void setGlobal_hash(String global_hash) {
		this.global_hash = global_hash;
	}

	public String getNew_hash() {
		return new_hash;
	}
	public void setNew_hash(String new_hash) {
		this.new_hash = new_hash;
	}

    public Boolean getIs_private() {
        return is_private;
    }
    public void setIs_private(Boolean is_private) {
        this.is_private = is_private;
    }

    public String getAggregate_url() {
        return aggregate_url;
    }
    public void setAggregate_url(String aggregate_url) {
        this.aggregate_url = aggregate_url;
    }

	@Override
	public String toString() {
		return toJson(this);
	}

	public static String toJson(Bitmark myBitmark) {
		Gson gson = new Gson();
		return myBitmark != null ? gson.toJson(myBitmark) : "";
	}

	public static Bitmark GetBitmarkFromJson(String myBitmarkJson) {
		Gson gson = new Gson();
        return gson.fromJson(myBitmarkJson, Bitmark.class);
	}

	public static Bitmark GetBitmarkFromLinkSaveJson(JsonObject myjObjectLinkSave) {
		try {
			Bitmark myBitmark = new Bitmark();

			if(myjObjectLinkSave != null) {
				String shortUrl = myjObjectLinkSave.get("link") != null
						? myjObjectLinkSave.get("link").toString().replace("\"", "")
								: "";
				myBitmark.setUrl(shortUrl);
				myBitmark.setLong_url(myjObjectLinkSave.get("long_url") != null
						? myjObjectLinkSave.get("long_url").toString().replace("\"", "")
								: "");
//                myBitmark.setAggregate_url(myjObjectLinkSave.get("aggregate"));
			}

			return myBitmark;
		} catch (Exception ex) {
			Log.e("Bitmark", "Error with GetBitmarkFromLinkSaveJson: " + ex.getMessage());
			return null;
		}
	}
}

/*
v3/shorten
{
	"status_code": 200,
	"status_txt": "OK",
	"data": {
		"long_url": "http://thomashunsaker.com/",
		"url": "http://bit.ly/RHFC34",
		"hash": "RHFC34",
		"global_hash": "V7s4",
		"new_hash": 0
	}
}

v3/user/link_save
{
	"status_code": 200,
	"data": {
		"link_save": {
			"link": "http://bit.ly/WAaOAB",
			"aggregate_link": "http://bit.ly/WAaOAC",
			"long_url": "http://thomashunsaker.com/apps/rapido/",
			"new_link": 1
		}
	},
	"status_txt": "OK"
}
*/
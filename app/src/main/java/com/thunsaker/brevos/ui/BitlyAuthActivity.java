package com.thunsaker.brevos.ui;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.thunsaker.android.common.annotations.ForApplication;
import com.thunsaker.android.common.util.HttpUtils;
import com.thunsaker.android.common.util.QueryStringParser;
import com.thunsaker.android.common.util.Util;
import com.thunsaker.R;
import com.thunsaker.brevos.app.BaseBrevosActivity;
import com.thunsaker.brevos.data.events.BitlyAuthEvent;
import com.thunsaker.brevos.services.BitlyPrefs;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;

public class BitlyAuthActivity extends BaseBrevosActivity {
	final String TAG = "BitlyAuthActivity";

	public static final String ACCESS_URL = "https://api-ssl.bitly.com/oauth/access_token";
	public static final String AUTHORIZE_URL = "https://bitly.com/oauth/authorize";

	public ProgressDialog loadingDialog;

    @Inject
    @ForApplication
    Context mContext;

    @Inject
    EventBus mBus;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        ActionBar ab = getSupportActionBar();
        ab.setIcon(getResources().getDrawable(R.drawable.ic_launcher_flat_white));
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeAsUpIndicator(getResources().getDrawable(R.drawable.ic_up_affordance_white));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case android.R.id.home:
	        	finish();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onResume() {
		super.onResume();

        loadingDialog = ProgressDialog.show(
                BitlyAuthActivity.this, getString(R.string.dialog_please_wait),
                getString(R.string.dialog_loading_authorization),
                true, // Undefined progress
                true, // Allow canceling of operation
                new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                        Toast.makeText(
                                getApplicationContext(),
                                getString(R.string.dialog_abort),
                                Toast.LENGTH_SHORT).show();
                    }
                });

		WebView webView = new WebView(this);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setVisibility(View.VISIBLE);
		setContentView(webView);

		String authUrl = String.format("%s?client_id=%s&redirect_uri=%s", AUTHORIZE_URL, BitlyPrefs.BITLY_CLIENT_ID, BitlyPrefs.BITLY_REDIRECT_URL);

		try {
			webView.setWebViewClient(new WebViewClient() {
				@Override
				public void onPageStarted(WebView view, String url, Bitmap favicon) { }

				@Override
				public void onPageFinished(WebView view, String url) {
					if(url.startsWith(BitlyPrefs.BITLY_REDIRECT_URL)) {
						try {
							if(url.contains("code=")) {
								String requestToken = extractParamFromUrl(url, "code");

								// Do http post here...
								String accessUrl = String.format("%s?code=%s&client_id=%s&client_secret=%s&redirect_uri=%s",
										ACCESS_URL,
										requestToken,
                                        BitlyPrefs.BITLY_CLIENT_ID,
                                        BitlyPrefs.BITLY_CLIENT_SECRET,
                                        BitlyPrefs.BITLY_REDIRECT_URL);

								new TokenFetcher(mContext, accessUrl).execute();

								view.setVisibility(View.INVISIBLE);
                                finish();
//                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
							} else if (url.contains("error=")) {
								view.setVisibility(View.INVISIBLE);
								finish();
							}
						} catch (Exception e) {
							Log.i(TAG, "IOException: " + e.getMessage());
							e.printStackTrace();
						}
					}
				}
			});

			webView.loadUrl(authUrl);
			webView.requestFocus();
			loadingDialog.dismiss();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String extractParamFromUrl(String url,String paramName) {
		String queryString = url.substring(url.indexOf("?", 0)+1,url.length());
		QueryStringParser queryStringParser = new QueryStringParser(queryString);
		return queryStringParser.getQueryParamValue(paramName);
	}

	public class TokenFetcher extends AsyncTask<Void, Integer, Boolean> {
		Context myContext;
		String myUrl;
		public TokenFetcher(Context theContext, String theUrl) {
			myContext = theContext;
			myUrl = theUrl;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			Boolean result = false;
			String response = HttpUtils.getHttpResponse(myUrl, true, Util.contentType, Util.contentType);
			if(response != null) {
				String accessToken = extractParamFromUrl(response, "access_token");
				String login = extractParamFromUrl(response, "login");
				String apikey = extractParamFromUrl(response, "apiKey");

				if(accessToken.length() > 0 && login.length() > 0 && apikey.length() > 0) {
					result = true;
					PreferencesHelper.setBitlyToken(myContext, accessToken);
					PreferencesHelper.setBitlyLogin(myContext, login);
					PreferencesHelper.setBitlyApiKey(myContext, apikey);
					PreferencesHelper.setBitlyConnected(myContext, true);
				}
			} else {
				PreferencesHelper.setBitlyConnected(myContext, result);
			}

			return result;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			Toast.makeText(myContext, "Bit.ly Account Authorized", Toast.LENGTH_SHORT).show();
            mBus.post(new BitlyAuthEvent(result, ""));
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(null);
	}
}

package com.thunsaker.brevos.ui;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.thunsaker.R;
import com.thunsaker.android.common.annotations.ForApplication;
import com.thunsaker.android.common.util.Util;
import com.thunsaker.brevos.BrevosPrefsManager;
import com.thunsaker.brevos.app.BaseBrevosActivity;
import com.thunsaker.brevos.data.events.BitlyAuthEvent;
import com.thunsaker.brevos.services.AuthHelper;
import com.thunsaker.brevos.services.BitlyService;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class BitlyAuthActivity extends BaseBrevosActivity {
	final String TAG = "BitlyAuthActivity";

	public static final int REQUEST_CODE_BITLY_SIGN_IN = 8000;


	public static final String ACCESS_URL = "https://api-ssl.bitly.com/oauth/access_token";
	public static final String AUTHORIZE_URL = "https://bitly.com/oauth/authorize";

	public ProgressDialog loadingDialog;

    @Inject
    @ForApplication
    Context mContext;

    @Inject
    EventBus mBus;

	@Inject
	BrevosPrefsManager mPreferences;

	@Inject
	BitlyService mBitlyService;

	@BindView(R.id.toolbar_bitly) Toolbar mToolbar;
	@BindView(R.id.progress_bitly) ProgressBar mProgress;
	@BindView(R.id.webview_bitly) WebView mWebView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_bitly);

		ButterKnife.bind(this);

		setSupportActionBar(mToolbar);
		setTitle(R.string.title_activity_bitly_auth);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.close, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		if (id == R.id.action_close) {
			this.setResult(RESULT_CANCELED);
			this.finish();
		}

		return super.onOptionsItemSelected(item);
	}

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onResume() {
		super.onResume();

		mProgress.setVisibility(View.VISIBLE);
		mWebView.getSettings().setJavaScriptEnabled(true);

		String authUrl = String.format("%s?client_id=%s&redirect_uri=%s",
				AUTHORIZE_URL, AuthHelper.BITLY_CLIENT_ID, AuthHelper.BITLY_REDIRECT_URL);

		Log.i(TAG, "Bitly Auth Url: " + authUrl);

		try {
			mWebView.setWebViewClient(new WebViewClient() {
				@Override
				public void onPageStarted(WebView view, String url, Bitmap favicon) { }

				@Override
				public void onPageFinished(WebView view, String url) {
					if(url.startsWith(AuthHelper.BITLY_REDIRECT_URL)) {
						try {
							if(url.contains("code=")) {
								String requestToken = Util.extractParamFromUrl(url, "code");

								mBitlyService
										.getAccessToken(
												"Non est corpus.",
												requestToken,
												AuthHelper.BITLY_CLIENT_ID,
												AuthHelper.BITLY_CLIENT_SECRET,
												AuthHelper.BITLY_REDIRECT_URL)
										.subscribeOn(Schedulers.io())
										.observeOn(AndroidSchedulers.mainThread())
										.onErrorReturn(new Func1<Throwable, Response>() {
											@Override
											public Response call(Throwable throwable) {
												mPreferences.bitlyEnabled().put(false).commit();
												mBus.post(new BitlyAuthEvent(false, ""));
												return null;
											}
										})
										.subscribe(new Action1<Response>() {
											@Override
											public void call(Response response) {
												Log.i(TAG, "Here I am!");
												boolean result = false;
												Log.i(TAG, "Response = " + response);
												if (response != null) {
													Log.i(TAG, "Here I am again!!");
													String responseBody =
															new String(
																	((TypedByteArray) response.getBody()).getBytes());
													String accessToken =
															Util.extractParamFromUrl(responseBody, "access_token");
													String login = Util.extractParamFromUrl(responseBody, "login");
													// Deprecated
													String apikey = Util.extractParamFromUrl(responseBody, "apiKey");

													if (accessToken.length() > 0 && login.length() > 0) {
														mPreferences
																.bitlyEnabled().put(true)
																.bitlyToken().put(accessToken)
																.bitlyUsername().put(login)
																.bitlyApiKey().put(apikey)
																.commit();
														result = true;
													}
												}

												mBus.post(new BitlyAuthEvent(result, ""));
												MainActivity.isBitlyConnected = true;
											}
										});

								setResult(RESULT_OK);
								finish();
							} else if (url.contains("error=")) {
								view.setVisibility(View.INVISIBLE);
								setResult(RESULT_CANCELED);
								finish();
							}
						} catch (Exception e) {
							Log.i(TAG, "IOException: " + e.getMessage());
							e.printStackTrace();
						}
					}
				}
			});

			mWebView.loadUrl(authUrl);
			mWebView.requestFocus();
			mProgress.setVisibility(View.GONE);
			mWebView.setVisibility(View.VISIBLE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(null);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mWebView = null;
	}
}

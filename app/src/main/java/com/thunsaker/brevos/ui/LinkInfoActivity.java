package com.thunsaker.brevos.ui;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.echo.holographlibrary.Bar;
import com.echo.holographlibrary.BarGraph;
import com.echo.holographlibrary.Line;
import com.echo.holographlibrary.LineGraph;
import com.echo.holographlibrary.LinePoint;
import com.squareup.picasso.Picasso;
import com.thunsaker.R;
import com.thunsaker.android.common.annotations.ForApplication;
import com.thunsaker.android.common.util.Util;
import com.thunsaker.brevos.app.BaseBrevosActivity;
import com.thunsaker.brevos.app.BrevosApp;
import com.thunsaker.brevos.app.BrevosUtil;
import com.thunsaker.brevos.data.api.Bitmark;
import com.thunsaker.brevos.data.api.BitmarkInfo;
import com.thunsaker.brevos.data.api.LinkClicks;
import com.thunsaker.brevos.data.events.GetClicksListEvent;
import com.thunsaker.brevos.data.events.GetClicksTotalEvent;
import com.thunsaker.brevos.data.events.GetInfoEvent;
import com.thunsaker.brevos.services.BitlyClient;
import com.thunsaker.brevos.services.BitlyTasks;
import com.thunsaker.brevos.services.BitlyUtil;
import com.thunsaker.brevos.ui.custom.RoundedCornerTransform;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

public class LinkInfoActivity extends BaseBrevosActivity {
    public static String EXTRA_LINK = "EXTRA_LINK";
    public static String EXTRA_LINK_INFO = "EXTRA_LINK_INFO";

    public static String EXTRA_LINK_INFO_SOURCE = "EXTRA_LINK_INFO_SOURCE";

    public static String EXTRA_SOURCE_MAIN = "EXTRA_SOURCE_MAIN";
    public static String EXTRA_SOURCE_HISTORY = "EXTRA_SOURCE_HISTORY";

    @Inject
    @ForApplication
    Context mContext;

    @Inject
    EventBus mBus;

    @Inject
    Picasso mPicasso;

    @Inject
    android.text.ClipboardManager mClipboardManagerLegacy;

    @BindView(R.id.toolbar) Toolbar mToolbar;

    @BindView(R.id.coordinatorLayoutLinkDetailContainer) CoordinatorLayout mLinkDetailMainWrapper;

    @BindView(R.id.linearLayoutLinkDetailWrapper) LinearLayout mLinkDetailWrapper;
    @BindView(R.id.textViewLinkDetailTitle) TextView mTextViewLinkTitle;
    @BindView(R.id.viewLinkDetailTitlePlaceholder) View mTextViewLinkTitlePlaceholder;
    @BindView(R.id.textViewLinkDetailLongUrl) TextView mTextViewLinkLongUrl;
    @BindView(R.id.textViewLinkDetailShortUrl) TextView mTextViewLinkShortUrl;
    @BindView(R.id.imageViewLinkDetailFavicon) ImageView mImageViewFavicon;
    @BindView(R.id.imageViewLinkDetailPrivaticon) ImageView mImageViewPrivate;

    @BindView(R.id.textViewGraphClickCount) TextView mTextViewClicksTotalGraph;
    @BindView(R.id.textViewLinkDetailClickCountTotal) TextView mTextViewClicksTotal;
    @BindView(R.id.textViewLinkDetailClickCountTotalGlobal) TextView mTextViewClicksTotalGlobal;

    @BindView(R.id.lineChartLinkDetailClickCountHourly) LineGraph mLineGraphClickHour;
    @BindView(R.id.progressBarLinkDetailClickCountHour) ProgressBar mProgressHour;
    @BindView(R.id.textViewLinkDetailClickCountHourEmpty) TextView mTextViewClickHourEmpty;
    @BindView(R.id.barChartLinkDetailClickCountDay) BarGraph mBarViewClickDay;
    @BindView(R.id.progressBarLinkDetailClickCountDay) ProgressBar mProgressDay;
    @BindView(R.id.textViewLinkDetailClickCountDayEmpty) TextView mTextViewClickDayEmpty;
    @BindView(R.id.barChartLinkDetailClickCountMonth) BarGraph mBarViewClickMonth;
    @BindView(R.id.progressBarLinkDetailClickCountMonth) ProgressBar mProgressMonth;
    @BindView(R.id.textViewLinkDetailClickCountMonthEmpty) TextView mTextViewClickMonthEmpty;

    @BindView(R.id.imageButtonLinkDetailRefresh) ImageButton mButtonRefresh;

    @BindView(R.id.fabLinkEdit) FloatingActionButton mFabEdit;

    private boolean infoEventDone = true;
    private boolean clicksHourEventDone = true;
    private boolean clicksDayEventDone = true;
    private boolean clicksMonthEventDone = true;

    private String source = EXTRA_SOURCE_MAIN;

    public Bitmark mCurrentBitmark;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(R.anim.fade_in, R.anim.slide_down);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_link);

        mBus.register(this);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        setTitle(getString(R.string.title_activity_link));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        showProgress();

        if(getIntent().hasExtra(LinkInfoActivity.EXTRA_LINK) || getIntent().hasExtra(MainActivity.BREVOS_POP_OVER_BITMARK))
            handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        BitlyTasks bitlyTasks = new BitlyTasks((BrevosApp) mContext);

        if(getIntent().hasExtra(LinkInfoActivity.EXTRA_LINK)) {
            String bitmarkString = intent.getStringExtra(LinkInfoActivity.EXTRA_LINK);
            Bitmark myLink = mCurrentBitmark = Bitmark.GetBitmarkFromJson(bitmarkString);
            if (myLink != null) {
                String longUrl = myLink.getLong_url();
                mTextViewLinkShortUrl.setText(formatShortUrl(myLink.getUrl()));

                mTextViewLinkLongUrl.setText(myLink.getLong_url());

                if(myLink.getIs_private())
                    mImageViewPrivate.setVisibility(View.VISIBLE);
                else
                    mImageViewPrivate.setVisibility(View.GONE);

                if (mImageViewFavicon != null) {
                    mPicasso.load(String.format(BrevosUtil.faviconFetcherUrl, longUrl))
                            .placeholder(Util.randInt(0,10) % 2 == 0 ? Util.randInt(0,10) % 2 == 0 ? R.drawable.blue_circle : R.drawable.orange_circle : R.drawable.yellow_circle)
                            .transform(new RoundedCornerTransform(100, 0))
                            .into(mImageViewFavicon);
                }

                getClickCountsForLink(bitlyTasks, myLink.getUrl());
                String globalLink = myLink.getAggregate_url();
                if(globalLink != null)
                    bitlyTasks.new GetClickCountTotal(globalLink, BitlyClient.CLICKS_DESTINATION_INFO, true).execute();

                if (intent.hasExtra(LinkInfoActivity.EXTRA_LINK_INFO)) {

                    String bitmarkInfoRaw = intent.getStringExtra(LinkInfoActivity.EXTRA_LINK_INFO);
                    BitmarkInfo info = BitmarkInfo.GetBitmarkInfoFromJson(bitmarkInfoRaw);
                    if (info != null) {
                        showLinkInfo(info);
                        mBus.post(new GetInfoEvent(true, "", info, new Bitmark(info.aggregate_link, info.original_url)));
                        getClickCountsForLink(bitlyTasks, info.aggregate_link);
                    } else {
                        Toast.makeText(mContext, String.format(getString(R.string.error_placeholder), getString(R.string.error_loading_info)), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    bitlyTasks.new GetLinkInfo(myLink.getUrl(), BitlyTasks.GET_INFO_ACTION_DEFAULT).execute();
                }
            } else {
                Toast.makeText(mContext, String.format(getString(R.string.error_placeholder), getString(R.string.error_loading)), Toast.LENGTH_SHORT).show();
            }
            if(getIntent().hasExtra(LinkInfoActivity.EXTRA_LINK_INFO_SOURCE)) {
                source = getIntent().getStringExtra(EXTRA_LINK_INFO_SOURCE);
            }
        } else if(getIntent().hasExtra(MainActivity.BREVOS_POP_OVER_BITMARK)) {
            String passedLinkRaw = getIntent().getStringExtra(MainActivity.BREVOS_POP_OVER_BITMARK);
            Bitmark bitmark = Bitmark.GetBitmarkFromJson(passedLinkRaw);
            bitlyTasks.new GetLinkInfo(bitmark.getUrl(), BitlyTasks.GET_INFO_ACTION_DEFAULT).execute();
//            getClickCountsForLink(bitlyTasks, passedLink);
        }
    }

    @Override
    public void onBackPressed() {
        mFabEdit.hide();
        super.onBackPressed();
    }

    private Spanned formatShortUrl(String url) {
        String shortUrl = url.replace("http://","").replace("https://","");
        return Html.fromHtml(shortUrl.replace("/", "/<b>") + "</b>");
    }

    @OnClick(R.id.imageButtonLinkDetailRefresh)
    public void refreshButton() {
        showProgress();
        String link = null;
        String globalLink = null;
        if(mCurrentBitmark != null) {
            link = mCurrentBitmark.getUrl();
            globalLink = mCurrentBitmark.getAggregate_url();
        } else {
            link = mTextViewLinkShortUrl.getText().toString().replace("<b>","").replace("</b>","");
        }

        BitlyTasks mBitlyTasks = new BitlyTasks((BrevosApp) mContext);
        getClickCountsForLink(mBitlyTasks, link);
        if(globalLink != null)
            mBitlyTasks.new GetClickCountTotal(globalLink, BitlyClient.CLICKS_DESTINATION_INFO, true).execute();
    }

    private void getClickCountsForLink(BitlyTasks bitlyTasks, String linkForClicks) {
        int offset = MainActivity.getTimeZoneOffset();

        bitlyTasks.new GetClickCount(linkForClicks, BitlyClient.UNIT_HOUR, 8, offset, false).execute();
        bitlyTasks.new GetClickCount(linkForClicks, BitlyClient.UNIT_DAY, 7, offset, false).execute();
        bitlyTasks.new GetClickCount(linkForClicks, BitlyClient.UNIT_MONTH, 3, offset, false).execute();
        clicksHourEventDone = clicksDayEventDone = clicksMonthEventDone = false;

        bitlyTasks.new GetClickCountTotal(linkForClicks, BitlyClient.CLICKS_DESTINATION_INFO, false).execute();
    }

    private void showLinkInfo(BitmarkInfo info) {
        mLinkDetailWrapper.setVisibility(View.VISIBLE);
        if(info.html_title != null && info.html_title.length() > 0) {
            mTextViewLinkTitle.setText(info.html_title);
            mTextViewLinkTitle.setVisibility(View.VISIBLE);
            mTextViewLinkTitlePlaceholder.setVisibility(View.GONE);
        } else {
            mTextViewLinkTitle.setVisibility(View.GONE);
            mTextViewLinkTitlePlaceholder.setVisibility(View.VISIBLE);
        }
        mTextViewLinkShortUrl.setText(formatShortUrl(info.aggregate_link));
//        Linkify.addLinks(mTextViewLinkShortUrl, Linkify.WEB_URLS);
        mTextViewLinkLongUrl.setText(info.original_url);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.link, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                supportFinishAfterTransition();
                break;
            case R.id.action_share:
                if(mCurrentBitmark != null)
                    openShareDialog(mCurrentBitmark.getUrl());
                break;
            case R.id.action_copy:
                if(mCurrentBitmark != null)
                    copyToClipboard(mCurrentBitmark.getUrl());
                break;
            case R.id.action_browse:
                if(mCurrentBitmark != null)
                    openBrowser(mCurrentBitmark.getUrl());
                break;
        }
        return true;
    }

    public void onEvent(GetInfoEvent event) {
        if(clicksHourEventDone && clicksDayEventDone && clicksMonthEventDone) {
            hideProgress();
        }

        if(event.result) {
            mCurrentBitmark = event.bitmark;
            showLinkInfo(event.info);
            infoEventDone = true;
        }

        if(infoEventDone && clicksHourEventDone && clicksDayEventDone && clicksMonthEventDone)
            hideProgress();
    }

    public void onEvent(GetClicksListEvent event) {
        if(infoEventDone && clicksHourEventDone && clicksDayEventDone && clicksMonthEventDone)
            hideProgress();

        boolean empty = true;

        if(event.span != null) {
            if(event.clicksList != null) {
                for (LinkClicks item : event.clicksList)
                    if(item.clicks > 0)
                        empty = false;
            }

            if (event.span.equals(BitlyClient.UNIT_HOUR)) {
                showClickCountGraphHours(!empty ? event.clicksList : null, event.timeZoneOffset);
                clicksHourEventDone = true;
            }

            if (event.span.equals(BitlyClient.UNIT_DAY)) {
                showClickCountGraphDays(!empty ? event.clicksList : null, event.timeZoneOffset);
                clicksDayEventDone = true;
            }

            if (event.span.equals(BitlyClient.UNIT_MONTH)) {
                showClickCountGraphMonth(!empty ? event.clicksList : null, event.timeZoneOffset);
                clicksMonthEventDone = true;
            }
        }

        if(infoEventDone && clicksHourEventDone && clicksDayEventDone && clicksMonthEventDone)
            hideProgress();
    }

    public void onEvent(GetClicksTotalEvent event) {
        if (event.destination == BitlyClient.CLICKS_DESTINATION_INFO) {
            showTotalClicks(event.count, event.global);
        }
    }

    private void showTotalClicks(int count, boolean global) {
        if(global) {

            mTextViewClicksTotalGlobal.setText(BitlyUtil.getLinkClicksString(count));
            mTextViewClicksTotalGlobal.setVisibility(View.VISIBLE);
//            mTextViewClicksTotalGlobal.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fade_in));
        } else {
            String clicks = BitlyUtil.getLinkClicksString(count);
            mTextViewClicksTotal.setText(clicks);
            mTextViewClicksTotal.setVisibility(View.VISIBLE);
            mTextViewClicksTotalGraph.setText(clicks);
            mTextViewClicksTotalGraph.setVisibility(View.VISIBLE);
//            mTextViewClicksTotal.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fade_in));
        }
    }


    @SuppressLint("InlinedApi")
    public void copyToClipboard(String shortUrlString) {
        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.HONEYCOMB) {
            ClipData clipData = ClipData.newPlainText("Bit.ly Short Url", shortUrlString);
            ClipboardManager mClipboardManager = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
            mClipboardManager.setPrimaryClip(clipData);
        } else {
            mClipboardManagerLegacy.setText(shortUrlString);
        }
        Toast.makeText(this, String.format(getString(R.string.link_copied), shortUrlString), Toast.LENGTH_SHORT).show();
    }

    private void openShareDialog(String shortUrlString) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, shortUrlString);

        Intent intentChooser = Intent.createChooser(intent, getText(R.string.action_share));
        intentChooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intentChooser);
    }

    private void showClickCountGraphHours(LinkClicks[] clicksList, int timeZoneOffset) {
        try {
            if(clicksList != null) {
                List<LinkClicks> linkCLicksList = new ArrayList<LinkClicks>();
                Collections.addAll(linkCLicksList, clicksList);
                // Reverse Sort List
//                Collections.sort(linkCLicksList, new Comparator<LinkClicks>() {
//                    @Override
//                    public int compare(LinkClicks lc1, LinkClicks lc2) {
//                        return ((Long) lc2.dt).compareTo(lc1.dt);
//                    }
//                });

                Line line = new Line();
                int i = 0;
                int max = 0;
                for (LinkClicks link : linkCLicksList) {
                    LinePoint point = new LinePoint();
                    point.setX(i);
                    point.setY(link.clicks);
                    DateTimeFormatter hourWithAMPM = DateTimeFormat.forPattern("ha");
                    DateTimeFormatter hour = DateTimeFormat.forPattern("h");
                    DateTimeZone tz = DateTimeZone.forOffsetHours(timeZoneOffset);
                    DateTime dt = new DateTime(link.dt * 1000, tz);
                    if (i == 0 || i == linkCLicksList.size() - 1)
                        point.setLabel_string(dt.toString(hour));
                    else
                        point.setLabel_string(dt.toString(hourWithAMPM));
                    line.addPoint(point);
                    line.setColor(Color.parseColor("#8fd400"));

                    if (link.clicks > max)
                        max = link.clicks;
                    i++;
                }

                mLineGraphClickHour.setRangeY(0, max);
                mLineGraphClickHour.addLine(line);
                mLineGraphClickHour.setLineToFill(0);

                mProgressHour.setVisibility(View.GONE);
                mTextViewClickHourEmpty.setVisibility(View.GONE);
                mLineGraphClickHour.setVisibility(View.VISIBLE);
                mLineGraphClickHour.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fade_in));
            } else {
                mProgressHour.setVisibility(View.GONE);
                mTextViewClickHourEmpty.setVisibility(View.VISIBLE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.makeText(mContext, getString(R.string.error_loading_clicks), Toast.LENGTH_SHORT).show();
        }
    }

    private void showClickCountGraphDays(LinkClicks[] clicksList, int timeZoneOffset) {
        try {
            if(clicksList != null) {
                ArrayList<Bar> points = new ArrayList<Bar>();

                int i = 0;
                for (LinkClicks link : clicksList) {
                    Bar bar = new Bar();
                    bar.setColor(i % 2 == 0 ? Color.parseColor("#8fd400") : Color.parseColor("#b4006a"));
                    // TODO: Restore my forked changes that were never committed
//                    bar.setLabelColor(Color.parseColor("#222222"));
//                    bar.setValueColor(Color.parseColor("#222222"));
                    DateTimeFormatter format = DateTimeFormat.forPattern("E");
                    DateTimeZone tz = DateTimeZone.forOffsetHours(timeZoneOffset);
                    DateTime dt = new DateTime(link.dt * 1000, tz);
                    bar.setName(dt.toString(format));
                    bar.setValue((float)link.clicks);
//                    bar.setValueString(String.valueOf(link.clicks));
                    points.add(bar);
                    i++;
                }

                mBarViewClickDay.setBars(points);

                mProgressDay.setVisibility(View.GONE);
                mTextViewClickDayEmpty.setVisibility(View.GONE);
                mBarViewClickDay.setVisibility(View.VISIBLE);
                mBarViewClickDay.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fade_in));
            } else {
                mProgressDay.setVisibility(View.GONE);
                mTextViewClickDayEmpty.setVisibility(View.VISIBLE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.makeText(mContext, getString(R.string.error_loading_clicks), Toast.LENGTH_SHORT).show();
        }
    }

    private void showClickCountGraphMonth(LinkClicks[] clicksList, int timeZoneOffset) {
        try {
            if(clicksList != null) {
                ArrayList<Bar> points = new ArrayList<Bar>();

                int i = 0;
                for (LinkClicks link : clicksList) {
                    Bar bar = new Bar();
                    bar.setColor(i % 2 == 0 ? Color.parseColor("#b4006a") : Color.parseColor("#8fd400"));
//                    bar.setLabelColor(Color.parseColor("#222222"));
//                    bar.setValueColor(Color.parseColor("#222222"));
                    DateTimeFormatter format = DateTimeFormat.forPattern("MMM");
                    DateTimeZone tz = DateTimeZone.forOffsetHours(timeZoneOffset);
                    DateTime dt = new DateTime(link.dt * 1000, tz);
                    bar.setName(dt.toString(format));
                    bar.setValue((float) link.clicks);
//                    bar.setValueString(String.valueOf(link.clicks));
                    points.add(bar);
                    i++;
                }

                mBarViewClickMonth.setBars(points);

                mProgressMonth.setVisibility(View.GONE);
                mTextViewClickMonthEmpty.setVisibility(View.GONE);
                mBarViewClickMonth.setVisibility(View.VISIBLE);
                mBarViewClickMonth.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fade_in));
            } else {
                mProgressMonth.setVisibility(View.GONE);
                mTextViewClickMonthEmpty.setVisibility(View.VISIBLE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.makeText(mContext, getString(R.string.error_loading_clicks), Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.textViewLinkDetailClickCountTotal)
    public void showMyLinkClicksToast() {
        Toast.makeText(this, getString(R.string.clicks_total_your_link), Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.textViewLinkDetailClickCountTotalGlobal)
    public void showGlobalLinkClicksToast() {
        Toast.makeText(this, getString(R.string.clicks_total_global_link), Toast.LENGTH_SHORT).show();
    }

    private void openBrowser(String short_url) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(short_url)));
    }

    @OnClick(R.id.fabLinkEdit)
    public void onFabEditClick() {
        Snackbar.make(mLinkDetailMainWrapper, "Allow Editing Some Day", Snackbar.LENGTH_SHORT).show();
    }
}
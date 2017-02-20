package com.thunsaker.brevos.services;

import android.content.Context;
import android.os.AsyncTask;

import com.thunsaker.BuildConfig;
import com.thunsaker.R;
import com.thunsaker.android.common.annotations.ForApplication;
import com.thunsaker.brevos.app.BrevosApp;
import com.thunsaker.brevos.data.api.Bitmark;
import com.thunsaker.brevos.data.api.Expand;
import com.thunsaker.brevos.data.api.ExpandResponse;
import com.thunsaker.brevos.data.api.LinkClicksListResponse;
import com.thunsaker.brevos.data.api.LinkClicksResponse;
import com.thunsaker.brevos.data.api.LinkHistoryItem;
import com.thunsaker.brevos.data.api.LinkInfoResponse;
import com.thunsaker.brevos.data.api.ShortenResponse;
import com.thunsaker.brevos.data.api.ShortenSaveResponse;
import com.thunsaker.brevos.data.api.UserHistoryResponse;
import com.thunsaker.brevos.data.events.ExpandUrlEvent;
import com.thunsaker.brevos.data.events.GetClicksEvent;
import com.thunsaker.brevos.data.events.GetClicksListEvent;
import com.thunsaker.brevos.data.events.GetClicksTotalEvent;
import com.thunsaker.brevos.data.events.GetInfoEvent;
import com.thunsaker.brevos.data.events.GetInfoTrendingEvent;
import com.thunsaker.brevos.data.events.GetUserHistoryEvent;
import com.thunsaker.brevos.data.events.ShortenedUrlEvent;
import com.thunsaker.brevos.ui.PreferencesHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;

public class BitlyTasks {
    @Inject
    EventBus mBus;

    @Inject @ForApplication
    Context mContext;

    @Inject
    BitlyService mBitlyService;

    public static int GET_INFO_ACTION_DEFAULT = 0;
    public static int GET_INFO_ACTION_MAIN = 1;

    public static int HISTORY_ARCHIVE_DEFAULT = 0; // off (default)
    public static int HISTORY_ARCHIVE_EXCLUDE = 0; // both
    public static int HISTORY_ARCHIVE_INCLUDE = 1; // off (default)
    public static int HISTORY_ARCHIVE_ONLY = 2; // on

    public static int HISTORY_PRIVATE_DEFAULT = 1; // both
    public static int HISTORY_PRIVATE_EXCLUDE = 0; // off
    public static int HISTORY_PRIVATE_INCLUDE = 1; // both
    public static int HISTORY_PRIVATE_ONLY = 2; // on

    public static int HISTORY_LIST_TYPE_DEFAULT = 0;
    public static int HISTORY_LIST_TYPE_COMPACT = 1;
    public static int HISTORY_LIST_TYPE_SEARCH = 2;

    String accessToken;

    public BitlyTasks(BrevosApp app) {
        app.inject(this);
    }

    public class CreateBitmark extends AsyncTask<Void, Integer, ShortenResponse> {
        String sourceUrl;
        String domain;
        Integer action;
        Boolean isPrivate;

        /**
         *
         * @param sourceUrl
         * @param action        Action to take after executing. Either {@link com.thunsaker.brevos.services.BitlyClient#SHORTENED_ACTION_DEFAULT} or
         *                      {@link com.thunsaker.brevos.services.BitlyClient#SHORTENED_ACTION_COPY} or
         *                      {@link com.thunsaker.brevos.services.BitlyClient#SHORTENED_ACTION_POPOVER}
         */
        public CreateBitmark(String sourceUrl, Integer action) {
            this(sourceUrl, action, BitlyClient.BITLY_DOMAIN_DEFAULT, false);
        }

        /**
         *
         * @param sourceUrl     Url to shorten
         * @param action        Action to take after executing. Either {@link com.thunsaker.brevos.services.BitlyClient#SHORTENED_ACTION_DEFAULT} or
         *                      {@link com.thunsaker.brevos.services.BitlyClient#SHORTENED_ACTION_COPY} or
         *                      {@link com.thunsaker.brevos.services.BitlyClient#SHORTENED_ACTION_POPOVER}
         * @param domain        The domain to use. Either {@link com.thunsaker.brevos.services.BitlyClient#BITLY_DOMAIN_DEFAULT} or
         *                      {@link com.thunsaker.brevos.services.BitlyClient#BITLY_DOMAIN_BITLY} or
         *                      {@link com.thunsaker.brevos.services.BitlyClient#BITLY_DOMAIN_JMP} or
         *                      {@link com.thunsaker.brevos.services.BitlyClient#BITLY_DOMAIN_BITLYCOM} or
         * @param isPrivate     Should link be created privately
         */
        public CreateBitmark(String sourceUrl, Integer action, String domain, Boolean isPrivate) {
            this.sourceUrl = sourceUrl;
            this.domain = domain;
            this.action = action;
            this.isPrivate = isPrivate;
        }

        @Override
        protected ShortenResponse doInBackground(Void... params) {
            try {
                accessToken = PreferencesHelper.getBitlyToken(mContext);

                if(!sourceUrl.startsWith("https://") && !sourceUrl.startsWith("http://"))
                    sourceUrl = "http://" + sourceUrl;

                if(accessToken != null && accessToken.length() > 0)
                    return mBitlyService.createBitmark(accessToken, sourceUrl, domain);
                else
                    return mBitlyService.createBitmarkWithoutAuth(BuildConfig.BITLY_DEFAULT_USERNAME, BuildConfig.BITLY_DEFAULT_KEY, sourceUrl, domain);
            } catch (Exception ex) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(ShortenResponse result) {
            super.onPostExecute(result);
            if(result != null) {
                int statusCode = result.status_code != null ? Integer.parseInt(result.status_code) : 500;
                if (statusCode == 200 || statusCode == 304) { // 200 Created - 304 Already exists
                    if (result.data != null) {
                        mBus.post(new ShortenedUrlEvent(true, "", result.data, action, result.data.getLong_url()));
                    } else {
                        mBus.post(new ShortenedUrlEvent(false, mContext.getString(R.string.error_parsing_response), null, action, sourceUrl));
                    }
                } else if(result.status_txt.equals("INVALID_URI")) {
                    mBus.post(new ShortenedUrlEvent(false, mContext.getString(R.string.error_invalid_url), null, action, null));
                } else {
                    mBus.post(new ShortenedUrlEvent(false, mContext.getString(R.string.error_server), null, action, sourceUrl));
                }
            } else {
                mBus.post(new ShortenedUrlEvent(false, mContext.getString(R.string.error_shortening), null, action, sourceUrl));
            }
        }
    }

    public class SaveBitmark extends AsyncTask<Void, Integer, ShortenSaveResponse> {
        String sourceUrl;
        Integer action;
        Boolean isPrivate;

        /**
         *
         * @param sourceUrl
         * @param action        Action to take after executing. Either {@link com.thunsaker.brevos.services.BitlyClient#SHORTENED_ACTION_DEFAULT} or
         *                      {@link com.thunsaker.brevos.services.BitlyClient#SHORTENED_ACTION_COPY} or
         *                      {@link com.thunsaker.brevos.services.BitlyClient#SHORTENED_ACTION_POPOVER}
         */
        public SaveBitmark(String sourceUrl, Integer action) {
            this(sourceUrl, action, false);
        }

        /**
         *
         * @param sourceUrl
         * @param action        Action to take after executing. Either {@link com.thunsaker.brevos.services.BitlyClient#SHORTENED_ACTION_DEFAULT} or
         *                      {@link com.thunsaker.brevos.services.BitlyClient#SHORTENED_ACTION_COPY} or
         *                      {@link com.thunsaker.brevos.services.BitlyClient#SHORTENED_ACTION_POPOVER}
         * @param isPrivate
         */
        public SaveBitmark(String sourceUrl, Integer action, Boolean isPrivate) {
            this.sourceUrl = sourceUrl;
            this.action = action;
            this.isPrivate = isPrivate;
        }

        @Override
        protected ShortenSaveResponse doInBackground(Void... params) {
            try {
                accessToken = PreferencesHelper.getBitlyToken(mContext);

                if(accessToken.length() > 0)
                    return mBitlyService.saveLink(accessToken, sourceUrl, isPrivate);
                // TODO: User must be authenicated to make this call...
                return null;
            } catch (Exception ex) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(ShortenSaveResponse result) {
            super.onPostExecute(result);
            if(result != null) {
                int statusCode = result.status_code != null ? Integer.parseInt(result.status_code) : 500;
                if (statusCode == 200 || statusCode == 304) { // 200 Created - 304 Already exists
                    if (result.data != null) {
                        Bitmark convertedBitmark = new Bitmark(result.data.link_save.aggregate_link, result.data.link_save.long_url);
                        convertedBitmark.setIs_private(isPrivate);
                        mBus.post(new ShortenedUrlEvent(true, "", convertedBitmark, action, sourceUrl));
                    } else {
                        mBus.post(new ShortenedUrlEvent(false, "Error handling new short url.", null, action, sourceUrl));
                    }
                } else {
                    mBus.post(new ShortenedUrlEvent(false, "Error creating short url, try again?", null, action, sourceUrl));
                }
            } else {
                mBus.post(new ShortenedUrlEvent(false, "Error creating short url, try again?", null, action, sourceUrl));
            }
        }
    }

    public class ExpandBitmark extends AsyncTask<Void, Integer, ExpandResponse> {
        String sourceUrl;
        Integer action;

        /**
         *
         * @param sourceUrl
         * @param action        Action to take after executing. Either {@link com.thunsaker.brevos.services.BitlyClient#EXPAND_ACTION_DEFAULT} or
         *                      {@link com.thunsaker.brevos.services.BitlyClient#EXPAND_ACTION_COPY}
         */
        public ExpandBitmark(String sourceUrl, Integer action) {
            this.sourceUrl = sourceUrl;
            this.action = action;
        }

        @Override
        protected ExpandResponse doInBackground(Void... params) {
            try {
                accessToken = PreferencesHelper.getBitlyToken(mContext);

                if(!sourceUrl.startsWith("https://") && !sourceUrl.startsWith("http://"))
                    sourceUrl = "http://" + sourceUrl;

                if(accessToken != null && accessToken.length() > 0)
                    return mBitlyService.expandBitmark(accessToken, sourceUrl);
                else
                    return mBitlyService.expandBitmarkWithoutAuth(BuildConfig.BITLY_DEFAULT_USERNAME, BuildConfig.BITLY_DEFAULT_KEY, sourceUrl);
            } catch (Exception ex) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(ExpandResponse result) {
            super.onPostExecute(result);
            if(result != null) {
                int statusCode = result.status_code != null ? Integer.parseInt(result.status_code) : 500;
                if (statusCode == 200) { // 200 Created
                    if(result.status_txt.equals("NOT_FOUND")) {
                        mBus.post(new ExpandUrlEvent(false, mContext.getString(R.string.error_not_found), null, action));
                    } else {
                        if (result.data != null && result.data.expand != null && result.data.expand.length > 0)  {
                            Expand expandResult = result.data.expand[0];
                            if(expandResult != null) {
                                Bitmark expandBitmark = new Bitmark(expandResult.getShort_url(), expandResult.getLong_url());
                                mBus.post(new ExpandUrlEvent(true, "", expandBitmark, action));
                            } else {
                                mBus.post(new ExpandUrlEvent(false, mContext.getString(R.string.error_parsing_response), null, action));
                            }
                        } else {
                            mBus.post(new ExpandUrlEvent(false, mContext.getString(R.string.error_parsing_response), null, action));
                        }
                    }
                } else if(result.status_txt.equals("INVALID_URI")) {
                    mBus.post(new ExpandUrlEvent(false, mContext.getString(R.string.error_invalid_url), null, action));
                } else {
                    mBus.post(new ExpandUrlEvent(false, mContext.getString(R.string.error_server), null, action));
                }
            } else {
                mBus.post(new ExpandUrlEvent(false, mContext.getString(R.string.error_expanding), null, action));
            }
        }
    }

    public class GetClickCountTotal extends AsyncTask<Void, Integer, LinkClicksResponse> {
        String link;
        String unit;
        int units = -1;
        int destination;
        boolean global;

        /**
         *
         * @param link     The link to use.
         *
         */
        public GetClickCountTotal(String link, int destination, boolean global) {
            this(link, BitlyClient.UNIT_DAY, -1, destination, global);
        }

        /**
         *
         * @param link     @link #GetClickCount
         * @param unit     The unit to fetch. Either {@link BitlyClient#UNIT_HOUR} or
         *                 {@link BitlyClient#UNIT_DAY} or {@link BitlyClient#UNIT_WEEK} or {@link BitlyClient#UNIT_MONTH}
         * @param units    How long to display the message. -1 for all.
         */
        public GetClickCountTotal(String link, String unit, int units, int destination, boolean global) {
            this.link = link;
            this.unit = unit;
            this.units = units;
            this.destination = destination;
            this.global = global;
        }

        @Override
        protected LinkClicksResponse doInBackground(Void... params) {
            try {
                accessToken = PreferencesHelper.getBitlyToken(mContext);

                if(accessToken.length() > 0)
                    return mBitlyService.getLinkClicksTotal(accessToken, link, unit, units);
                else
                    return null;
            } catch (Exception ex) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(LinkClicksResponse result) {
            super.onPostExecute(result);
            if(result != null) {
                if (result.status_code != null && Integer.parseInt(result.status_code) == 200) {
                    if (result.data != null) {
                        if(destination == BitlyClient.CLICKS_DESTINATION_INFO)
                            mBus.post(new GetClicksTotalEvent(true, "", result.data.link_clicks, result.data.unit, link, destination, global));
                        else
                            mBus.post(new GetClicksEvent(true, "", result.data.link_clicks, result.data.unit, link, destination, global));
                    } else {
                        mBus.post(new GetClicksEvent(false, mContext.getString(R.string.error_loading_clicks), 0, null, link, destination, global));
                    }
                } else {
                    mBus.post(new GetClicksEvent(false, mContext.getString(R.string.error_server), 0, null, link, destination, global));
                }
            } else {
                mBus.post(new GetClicksEvent(false, mContext.getString(R.string.error_server), 0, null, link, destination, global));
            }
        }
    }

    public class GetClickCount extends AsyncTask<Void, Integer, LinkClicksListResponse> {
        String link;
        String unit;
        int units;
        int offset;
        boolean global;

        /**
         *
         * @param link     @link #GetClickCount
         * @param unit     @link #GetClickCount
         * @param units    How long to display the message. -1 for all.
         */
        public GetClickCount(String link, String unit, int units, int offset, boolean global) {
            this.link = link;
            this.unit = unit;
            this.units = units;
            this.offset = offset;
            this.global = global;
        }

        @Override
        protected LinkClicksListResponse doInBackground(Void... params) {
            try {
                accessToken = PreferencesHelper.getBitlyToken(mContext);

                if(accessToken.length() > 0)
                    return mBitlyService.getLinkClicks(accessToken, link, unit, units, false, offset);
                else
                    return null;
            } catch (Exception ex) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(LinkClicksListResponse result) {
            super.onPostExecute(result);
            if(result != null) {
                if (result.status_code != null && Integer.parseInt(result.status_code) == 200) {
                    if (result.data != null) {
                        mBus.post(new GetClicksListEvent(true, "", result.data.link_clicks, result.data.unit, result.data.units, result.data.tz_offset, global));
                    } else {
                        mBus.post(new GetClicksListEvent(false, mContext.getString(R.string.error_loading_clicks), null, null, 0, 0, global));
                    }
                } else {
                    mBus.post(new GetClicksListEvent(false, mContext.getString(R.string.error_server), null, null, 0, 0, global));
                }
            } else {
                mBus.post(new GetClicksListEvent(false, mContext.getString(R.string.error_server), null, null, 0, 0, global));
            }
        }
    }

    public class GetLinkInfo extends AsyncTask<Void, Integer, LinkInfoResponse> {
        String link;
        int action;

        /**
         *
         * @param link
         * @param action Either {@link BitlyTasks#GET_INFO_ACTION_DEFAULT} or
         *                    {@link BitlyTasks#GET_INFO_ACTION_MAIN}
         */
        public GetLinkInfo(String link, int action) {
            this.link = link;
            this.action = action;
        }

        @Override
        protected LinkInfoResponse doInBackground(Void... params) {
            try {
                accessToken = PreferencesHelper.getBitlyToken(mContext);

                if(accessToken.length() > 0)
                    return mBitlyService.getLinkInfo(accessToken, link);
                else
                    return null;
            } catch (Exception ex) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(LinkInfoResponse result) {
            super.onPostExecute(result);

            if(result != null) {
                if (result.status_code != null && Integer.parseInt(result.status_code) == 200) {
                    if (result.data != null) {
                        if (action == GET_INFO_ACTION_DEFAULT)
                            mBus.post(new GetInfoEvent(true, "", result.data, new Bitmark(link, result.data.original_url, result.data.aggregate_link)));
                        else
                            mBus.post(new GetInfoTrendingEvent(true, "", result.data, new Bitmark(link)));
                    } else {
                        if (action == GET_INFO_ACTION_DEFAULT)
                            mBus.post(new GetInfoEvent(false, mContext.getString(R.string.error_loading_info), null, null));
                        else
                            mBus.post(new GetInfoTrendingEvent(false, mContext.getString(R.string.error_loading_info), null, null));
                    }
                } else {
                    if (action == GET_INFO_ACTION_DEFAULT)
                        mBus.post(new GetInfoEvent(false, mContext.getString(R.string.error_server), null, null));
                    else
                        mBus.post(new GetInfoTrendingEvent(false, mContext.getString(R.string.error_server), null, null));
                }
            } else {
                if (action == GET_INFO_ACTION_DEFAULT)
                    mBus.post(new GetInfoEvent(false, mContext.getString(R.string.error_server), null, null));
                else
                    mBus.post(new GetInfoTrendingEvent(false, mContext.getString(R.string.error_server), null, null));
            }
        }
    }

    public class GetUserHistory extends AsyncTask<Void, Integer, UserHistoryResponse> {
        int limit = 10;
        int offset = 0;
        String query;
        int listType;
        int archivedLinks = HISTORY_ARCHIVE_DEFAULT;
        int privateLinks = HISTORY_PRIVATE_DEFAULT;

        public GetUserHistory(int limit, int offset, String query, int listType) {
            this(limit, offset, query, listType, HISTORY_ARCHIVE_DEFAULT, HISTORY_PRIVATE_DEFAULT);
        }

        /**
         *
         * @param limit
         * @param offset
         * @param query
         * @param listType      Either {@link BitlyTasks#HISTORY_LIST_TYPE_DEFAULT} or
         *                      {@link BitlyTasks#HISTORY_LIST_TYPE_COMPACT} or
         *                      {@link BitlyTasks#HISTORY_LIST_TYPE_SEARCH}
         * @param archivedLinks Either {@link BitlyTasks#HISTORY_ARCHIVE_DEFAULT} or
         *                      {@link BitlyTasks#HISTORY_ARCHIVE_INCLUDE} or
         *                      {@link BitlyTasks#HISTORY_ARCHIVE_EXCLUDE} or
         *                      {@link BitlyTasks#HISTORY_ARCHIVE_ONLY}
         * @param privateLinks  Either {@link BitlyTasks#HISTORY_PRIVATE_DEFAULT} or
         *                      {@link BitlyTasks#HISTORY_PRIVATE_INCLUDE} or
         *                      {@link BitlyTasks#HISTORY_PRIVATE_EXCLUDE} or
         *                      {@link BitlyTasks#HISTORY_PRIVATE_ONLY}
         */
        public GetUserHistory(int limit, int offset, String query, int listType, int archivedLinks, int privateLinks) {
            this.limit = limit;
            this.offset = offset;
            this.query = query;
            this.listType = listType;
            this.archivedLinks = archivedLinks;
            this.privateLinks = privateLinks;
        }

        @Override
        protected UserHistoryResponse doInBackground(Void... params) {
            try {
                accessToken = PreferencesHelper.getBitlyToken(mContext);

                if(accessToken.length() > 0)
                    return mBitlyService.getUserLinkHistory(accessToken, limit, offset, query, BitlyUtil.getLinkHistoryUrlParam(archivedLinks), BitlyUtil.getLinkHistoryUrlParam(privateLinks));
                else
                    return null;
            } catch (Exception ex) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(UserHistoryResponse result) {
            super.onPostExecute(result);
            List<LinkHistoryItem> historyList = new ArrayList<LinkHistoryItem>();
            if(result != null) {
                if (result.status_code != null && Integer.parseInt(result.status_code) == 200) {
                    if (result.data != null && result.data.link_history != null) {
                        Collections.addAll(historyList, result.data.link_history);

                        mBus.post(new GetUserHistoryEvent(true, "", historyList, listType));
                    } else {
                        // TODO: Add "No Links Yet" message
                        mBus.post(new GetUserHistoryEvent(false, mContext.getString(R.string.error_loading_history), null, 0));
                    }
                } else {
                    mBus.post(new GetUserHistoryEvent(false, mContext.getString(R.string.error_server), null, 0));
                }
            } else {
                mBus.post(new GetUserHistoryEvent(false, mContext.getString(R.string.error_server), null, 0));
            }
        }
    }
}
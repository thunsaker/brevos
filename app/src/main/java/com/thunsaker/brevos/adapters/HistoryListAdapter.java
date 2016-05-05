package com.thunsaker.brevos.adapters;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.thunsaker.R;
import com.thunsaker.brevos.app.BrevosUtil;
import com.thunsaker.brevos.data.api.Bitmark;
import com.thunsaker.brevos.data.api.BitmarkInfo;
import com.thunsaker.brevos.data.api.LinkHistoryItem;
import com.thunsaker.brevos.services.BitlyTasks;
import com.thunsaker.brevos.services.BitlyUtil;
import com.thunsaker.brevos.ui.LinkInfoActivity;
import com.thunsaker.brevos.ui.LinkInfoActivityNfc;
import com.thunsaker.brevos.ui.MainActivity;
import com.thunsaker.brevos.ui.custom.RoundedCornerTransform;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.List;

public class HistoryListAdapter extends ArrayAdapter<LinkHistoryItem> {
    public List<LinkHistoryItem> mItems;
    private LayoutInflater mInflater;
    public Context mContext;
    public int listType;
    public int mResource;

    public HistoryListAdapter(Context context, List<LinkHistoryItem> listItems, int listType) {
        this(context, R.layout.link_history_item, listItems, listType);
    }

    public HistoryListAdapter(Context context, int resource, List<LinkHistoryItem> listItems, int listType) {
        super(context, resource, listItems);
        mInflater = LayoutInflater.from(context);
        mItems = listItems;
        mContext = context;

        this.listType = listType;

        int layoutType = R.layout.link_history_item;
        if (listType == BitlyTasks.HISTORY_LIST_TYPE_COMPACT)
            layoutType = R.layout.link_history_item_compact;
        else if (listType == BitlyTasks.HISTORY_LIST_TYPE_SEARCH)
            layoutType = R.layout.link_history_item_search;
        mResource = layoutType;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View itemView = convertView;
        if (itemView == null) {
            itemView = mInflater.inflate(mResource, null);
        }

        final LinkHistoryItem link = mItems.get(position);
        if (link != null) {
            loadHistoryView(itemView, link);
        }

        return itemView;
    }

    private void loadHistoryView(View itemView, final LinkHistoryItem link) {
        final String title = link.title != null && link.title.length() > 0 ? link.title : null;
        final String shortUrl = link.link != null && link.link.length() > 0 ? link.link.trim() : null;
        final String aggregateUrl = link.aggregate_link != null && link.aggregate_link.length() > 0 ? link.aggregate_link.trim() : null;
        final String longUrl = link.long_url != null && link.long_url.length() > 0 ? link.long_url : null;
        final long createDate = link.created_at;
        final long modifiedDate = link.modified_at;

        final TextView titleTextView = (TextView) itemView.findViewById(R.id.textViewHistoryTitle);
        if (titleTextView != null) {
            if (title != null) {
                titleTextView.setText(title);
                titleTextView.setVisibility(View.VISIBLE);
            } else {
                if (listType == BitlyTasks.HISTORY_LIST_TYPE_COMPACT) {
                    if (shortUrl != null) {
                        titleTextView.setText(shortUrl.replace("http://", "").replace("https://", ""));
                        titleTextView.setVisibility(View.VISIBLE);
                    }
                } else {
                    titleTextView.setVisibility(View.GONE);
                }
            }
        }

        TextView longUrlTextView = (TextView) itemView.findViewById(R.id.textViewHistoryUrl);
        if (longUrlTextView != null) {
            if (longUrl != null) {
                longUrlTextView.setText(longUrl.replace("http://", "").replace("https://", ""));
            }
        }

        RelativeLayout clicksWrapper = (RelativeLayout) itemView.findViewById(R.id.relativeLayoutHistoryClicksWrapper);
        ProgressBar clicksProgress = (ProgressBar) itemView.findViewById(R.id.progressHistoryClicks);
        TextView clicksText = (TextView) itemView.findViewById(R.id.textViewHistoryClicks);
        if (clicksWrapper != null) {
            if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB)
                clicksProgress.setVisibility(View.VISIBLE);

            if (link.clicks > -1) {
                clicksProgress.setVisibility(View.GONE);
                clicksText.setVisibility(View.VISIBLE);
                clicksText.setText(BitlyUtil.getLinkClicksString(link.clicks));
            } else {
                if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
                    clicksProgress.setVisibility(View.VISIBLE);
                    clicksText.setVisibility(View.GONE);
                } else {
                    clicksText.setText("-");
                }
            }

            clicksWrapper.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openLinkInfoActivity(link);
                }
            });

            clicksWrapper.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Toast.makeText(mContext, mContext.getString(R.string.open_link_details), Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
        }

//        ImageButton shareButton = (ImageButton) itemView.findViewById(R.id.imageButtonHistoryShare);
//        if(shareButton != null) {
//            shareButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    openShareDialog(shortUrl);
//                }
//            });
//            shareButton.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View v) {
//                    Toast.makeText(mContext, mContext.getString(R.string.action_share), Toast.LENGTH_SHORT).show();
//                    return true;
//                }
//            });
//        }

        FrameLayout frameOverlay = (FrameLayout) itemView.findViewById(R.id.frameLayoutHistoryWrapper);
        frameOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listType == BitlyTasks.HISTORY_LIST_TYPE_SEARCH) {
                    openLinkInfoActivity(link);
                } else {
                    openBrowser(shortUrl);
                }
            }
        });
        frameOverlay.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (listType == BitlyTasks.HISTORY_LIST_TYPE_SEARCH) {
                    Toast.makeText(mContext, mContext.getString(R.string.open_link_details), Toast.LENGTH_SHORT).show();
                } else {
//                    Toast.makeText(mContext, mContext.getString(R.string.open_in_browser), Toast.LENGTH_SHORT).show();
                    openShareDialog(shortUrl);
                }
                return true;
            }
        });

        final ImageView imageViewFavicon = (ImageView) itemView.findViewById(R.id.imageViewFavicon);
        final ImageView imageViewFaviconLarge = (ImageView) itemView.findViewById(R.id.imageViewFaviconLarge);

        if (imageViewFavicon != null || imageViewFaviconLarge != null) {
            Picasso mPicasso = Picasso.with(mContext);
//            Util.randInt(0,10) % 2 == 0 ? R.drawable.pink_circle : R.drawable.yellow_circle;
            mPicasso.load(String.format(BrevosUtil.faviconFetcherUrl, longUrl))
                    .placeholder(R.drawable.yellow_circle)
                    .transform(new RoundedCornerTransform(40, 0))
                    .into(listType == BitlyTasks.HISTORY_LIST_TYPE_COMPACT ? imageViewFaviconLarge : imageViewFavicon);
        }

        if(listType == BitlyTasks.HISTORY_LIST_TYPE_DEFAULT) {
            ImageButton copyButton = (ImageButton) itemView.findViewById(R.id.imageButtonHistoryCopy);
            if (copyButton != null) {
                copyButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        copyToClipboard(shortUrl);
                    }
                });
                copyButton.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        Toast.makeText(mContext, mContext.getString(R.string.action_copy), Toast.LENGTH_SHORT).show();
                        return true;
                    }
                });
            }

            ImageView imageViewPrivaticon = (ImageView) itemView.findViewById(R.id.imageViewPrivaticon);
            imageViewPrivaticon.setVisibility(link.isPrivate ? View.VISIBLE : View.GONE);

            TextView shortUrlTextView = (TextView) itemView.findViewById(R.id.textViewHistoryShortUrl);
            if (shortUrlTextView != null) {
                if (shortUrl != null) {
                    shortUrlTextView.setText(shortUrl.replace("http://", "").replace("https://", ""));
                }
            }

            TextView createDateTextView = (TextView) itemView.findViewById(R.id.textViewHistoryDate);
            if (createDateTextView != null) {
                if (createDate > 0) {
//                    DateTimeFormatter shortDate = DateTimeFormat.forPattern("d-MMM-YY");
                    DateTimeFormatter shortDate = DateTimeFormat.shortDate();
                    DateTimeZone tz = DateTimeZone.forOffsetHours(MainActivity.getTimeZoneOffset());
                    DateTime dt = new DateTime(createDate * 1000, tz);
                    createDateTextView.setText(dt.toString(shortDate));
                }
            }
        }
    }

    private void openLinkInfoActivity(LinkHistoryItem link) {
        Intent linkInfoIntent = new Intent(mContext, android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH ? LinkInfoActivityNfc.class : LinkInfoActivity.class);
        linkInfoIntent.putExtra(LinkInfoActivity.EXTRA_LINK, new Bitmark(link.link, link.long_url, link.aggregate_link).toString());
        linkInfoIntent.putExtra(LinkInfoActivity.EXTRA_LINK_INFO, new BitmarkInfo(link.link, link.long_url, link.title).toString());
        linkInfoIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (listType != BitlyTasks.HISTORY_LIST_TYPE_COMPACT) {
            linkInfoIntent.putExtra(LinkInfoActivity.EXTRA_LINK_INFO_SOURCE, LinkInfoActivity.EXTRA_SOURCE_HISTORY);
        }
        mContext.startActivity(linkInfoIntent);
    }

    private void openBrowser(String shortUrl) {
        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, Uri.parse(shortUrl));
        launchBrowser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(launchBrowser);
    }

    private void openShareDialog(String shortUrlString) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, shortUrlString);

        Intent intentChooser = Intent.createChooser(intent, mContext.getText(R.string.action_share));
        intentChooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intentChooser);
    }

    @SuppressLint("InlinedApi")
    private void copyToClipboard(String shortUrlString) {
        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.HONEYCOMB) {
            ClipboardManager clipboardManager = (ClipboardManager)mContext.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText("Bit.ly Short Url", shortUrlString);
            clipboardManager.setPrimaryClip(clipData);
        } else {
            android.text.ClipboardManager clipboardManager = (android.text.ClipboardManager)mContext.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboardManager.setText(shortUrlString);
        }

        Toast.makeText(mContext, String.format(mContext.getString(R.string.link_copied), shortUrlString), Toast.LENGTH_SHORT).show();
    }
}
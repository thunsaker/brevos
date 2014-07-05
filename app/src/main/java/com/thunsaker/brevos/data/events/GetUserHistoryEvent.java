package com.thunsaker.brevos.data.events;

import com.thunsaker.android.common.bus.BaseEvent;
import com.thunsaker.brevos.data.api.LinkHistoryItem;

import java.util.List;

public class GetUserHistoryEvent extends BaseEvent {
    public List<LinkHistoryItem> userHistoryList;
    public int listType;
    public GetUserHistoryEvent(Boolean result, String resultMessage, List<LinkHistoryItem> userHistoryList, int listType) {
        super(result, resultMessage);
        this.userHistoryList = userHistoryList;
        this.listType = listType;
    }
}

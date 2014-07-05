package com.thunsaker.brevos.data.events;

import com.thunsaker.android.common.bus.BaseEvent;

public class GetTrendingEvent extends BaseEvent {
    public GetTrendingEvent(Boolean result, String resultMessage) {
        super(result, resultMessage);
    }
}

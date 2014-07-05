package com.thunsaker.brevos.data.events;

import com.thunsaker.android.common.bus.BaseEvent;
import com.thunsaker.brevos.data.api.Bitmark;
import com.thunsaker.brevos.data.api.BitmarkInfo;

public class GetInfoTrendingEvent extends BaseEvent {
    public BitmarkInfo info;
    public Bitmark bitmark;

    public GetInfoTrendingEvent(Boolean result, String resultMessage, BitmarkInfo info, Bitmark bitmark) {
        super(result, resultMessage);
        this.info = info;
        this.bitmark = bitmark;
    }
}
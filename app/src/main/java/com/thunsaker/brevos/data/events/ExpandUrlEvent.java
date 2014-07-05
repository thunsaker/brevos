package com.thunsaker.brevos.data.events;

import com.thunsaker.android.common.bus.BaseEvent;
import com.thunsaker.brevos.data.api.Bitmark;

public class ExpandUrlEvent extends BaseEvent {
    public Bitmark data;
    public Integer action;

    /**
     *
     * @param data    Result of link expanding
     * @param action            Action to perform after executing.. Either {@link com.thunsaker.brevos.services.BitlyClient#EXPAND_ACTION_DEFAULT} or
        *                      {@link com.thunsaker.brevos.services.BitlyClient#EXPAND_ACTION_COPY}
     */
    public ExpandUrlEvent(Boolean result, String resultMessage, Bitmark data, Integer action) {
        super(result, resultMessage);
        this.data = data;
        this.action = action;
    }
}
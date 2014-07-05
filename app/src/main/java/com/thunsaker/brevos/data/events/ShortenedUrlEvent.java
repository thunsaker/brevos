package com.thunsaker.brevos.data.events;

import com.thunsaker.android.common.bus.BaseEvent;
import com.thunsaker.brevos.data.api.Bitmark;

public class ShortenedUrlEvent extends BaseEvent {
    public Bitmark bitmark;
    public Integer action;
    public String longUrl;

    /**
     *
     * @param bitmark       Result of link shortening
     * @param action        Action to take after executing. Either {@link com.thunsaker.brevos.services.BitlyClient#SHORTENED_ACTION_DEFAULT} or
     *                      {@link com.thunsaker.brevos.services.BitlyClient#SHORTENED_ACTION_COPY} or
     *                      {@link com.thunsaker.brevos.services.BitlyClient#SHORTENED_ACTION_POPOVER}
     */
    public ShortenedUrlEvent(Boolean result, String resultMessage, Bitmark bitmark, Integer action, String longUrl) {
        super(result, resultMessage);
        this.bitmark = bitmark;
        this.action = action;
        this.longUrl = longUrl;
    }
}

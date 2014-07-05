package com.thunsaker.brevos.data.events;

import com.thunsaker.android.common.bus.BaseEvent;
import com.thunsaker.brevos.data.api.LinkClicks;


public class GetClicksListEvent extends BaseEvent {
    public LinkClicks[] clicksList;
    public String span;
    public int units;
    public int timeZoneOffset;
    public boolean global;

    /**
     * @param result
     * @param resultMessage
     * @param clicksList
     * @param span The unit to fetch. Either {@link com.thunsaker.brevos.services.BitlyClient#UNIT_HOUR} or
*                 {@link com.thunsaker.brevos.services.BitlyClient#UNIT_DAY} or
*                 {@link com.thunsaker.brevos.services.BitlyClient#UNIT_WEEK} or {@link com.thunsaker.brevos.services.BitlyClient#UNIT_MONTH}
     * @param units
     * @param timeZoneOffset Time Zone offset...
     */
    public GetClicksListEvent(Boolean result, String resultMessage, LinkClicks[] clicksList, String span, int units, int timeZoneOffset, boolean global) {
        super(result, resultMessage);
        this.clicksList = clicksList;
        this.span = span;
        this.units = units;
        this.timeZoneOffset = timeZoneOffset;
        this.global = global;
    }
}

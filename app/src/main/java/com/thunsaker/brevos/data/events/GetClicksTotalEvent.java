package com.thunsaker.brevos.data.events;

public class GetClicksTotalEvent extends GetClicksEvent {
    /**
     * @param result
     * @param resultMessage
     * @param count
     * @param span          The unit to fetch. Either {@link com.thunsaker.brevos.services.BitlyClient#UNIT_HOUR} or
     *                      {@link com.thunsaker.brevos.services.BitlyClient#UNIT_DAY} or
     *                      {@link com.thunsaker.brevos.services.BitlyClient#UNIT_WEEK} or
     *                      {@link com.thunsaker.brevos.services.BitlyClient#UNIT_MONTH} or
     *                      {@link com.thunsaker.brevos.services.BitlyClient#UNIT_ALL}
     * @param link
     * @param destination   The destination of the click count. Either {@link com.thunsaker.brevos.services.BitlyClient#CLICKS_DESTINATION_INFO} or
     *                      {@link com.thunsaker.brevos.services.BitlyClient#CLICKS_DESTINATION_LIST}
     * @param global        Are these links from the global url?
     */
    public GetClicksTotalEvent(Boolean result, String resultMessage, int count, String span, String link, int destination, boolean global) {
        super(result, resultMessage, count, span, link, destination, global);
    }
}
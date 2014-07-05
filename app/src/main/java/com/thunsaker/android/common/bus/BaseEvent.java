package com.thunsaker.android.common.bus;

public class BaseEvent {
    public Boolean result;
    public String resultMessage;

    public BaseEvent(Boolean result, String resultMessage) {
        this.result = result;
        this.resultMessage = resultMessage;
    }
}

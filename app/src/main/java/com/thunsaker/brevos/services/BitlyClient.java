package com.thunsaker.brevos.services;

public class BitlyClient {
    public static String UNIT_HOUR = "hour";
    public static String UNIT_DAY = "day";
    public static String UNIT_WEEK = "week";
    public static String UNIT_MONTH = "month";
    public static String UNIT_YEAR = "year";
    public static String UNIT_ALL = "all";

    public static String BITLY_BASE_URL = "https://api-ssl.bitly.com/v3/";
    public static String BITLY_DOMAIN_DEFAULT = "";
    public static String BITLY_DOMAIN_BITLY = "bit.ly";
    public static String BITLY_DOMAIN_JMP = "j.mp";
    public static String BITLY_DOMAIN_BITLYCOM = "bitly.com";

    public static Integer SHORTENED_ACTION_POPOVER = 0;
    public static Integer SHORTENED_ACTION_DEFAULT = 1;
    public static Integer SHORTENED_ACTION_COPY = 2;

    public static Integer EXPAND_ACTION_DEFAULT = 0;
    public static Integer EXPAND_ACTION_COPY = 1;

    public static Integer CLICKS_DESTINATION_INFO = 0;
    public static Integer CLICKS_DESTINATION_LIST = 1;
}
package com.thunsaker.brevos.services;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BitlyUtil {
    public static boolean isValidUrl(String linkToCheck) {
        Pattern urlPattern = Pattern.compile("^((https?:\\/\\/(www)?)|(www)).+(\\..{2,4})(\\/)?.+$");
        Matcher urlMatcher = urlPattern.matcher(linkToCheck);
        return urlMatcher.matches();
    }

    public static boolean isBitlyUrl(String linkToCheck) {
        Pattern bitlyPattern = Pattern.compile("^(https?:\\/\\/)?((bit.ly)|(j.mp)|(bitly.com))\\/.+$");
        Matcher bitlyMatcher = bitlyPattern.matcher(linkToCheck);
        return bitlyMatcher.matches();
    }

    public static String getLinkHistoryUrlParam(int linkHistoryOption) {
        if(linkHistoryOption == 1)
            return "both";
        else if(linkHistoryOption == 2)
            return "on";
        else
            return "off";
    }

    public static String getLinkClicksString(int linkClicks) {
        String linkClickString = String.valueOf(linkClicks);
        if(linkClicks > 1000) {
            linkClickString = String.format("%sk", (float)(linkClicks * 0.001));
        } else if (linkClicks > 1000000) {
            linkClickString = String.format("%sM", (float)(linkClicks * 0.000001));
        } else if (linkClicks > 1000000000) {
            linkClickString = ":)";
        }
        return linkClickString;
    }
}

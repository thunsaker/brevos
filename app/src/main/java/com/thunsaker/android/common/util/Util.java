package com.thunsaker.android.common.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.util.DisplayMetrics;

import java.util.Random;

public class Util {
	private static final String LOG_TAG = "Util";

	public static final String ENCODER_CHARSET = "UTF-8";

	public static String contentType = "json/application";

	@SuppressWarnings("static-access")
	public static Boolean HasInternet(Context myContext) {
		Boolean HasConnection = false;
		ConnectivityManager connectivityManager = (ConnectivityManager) myContext
				.getSystemService(myContext.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		if (activeNetworkInfo != null) {
			State myState = activeNetworkInfo.getState();
			if (myState == State.CONNECTED || myState == State.CONNECTING) {
				HasConnection = true;
			}
		}
		return HasConnection;
	}

	/**
	 * Returns a pseudo-random number between min and max, inclusive.
	 * The difference between min and max can be at most
	 * <code>Integer.MAX_VALUE - 1</code>.
	 * From StackOverflow: http://stackoverflow.com/questions/363681/generating-random-integers-in-a-range-with-java
	 *
	 * @param min Minimum value
	 * @param max Maximum value.  Must be greater than min.
	 * @return Integer between min and max, inclusive.
	 * @see java.util.Random#nextInt(int)
	 */
	public static int randInt(int min, int max) {

		// NOTE: Usually this should be a field rather than a method
		// variable so that it is not re-seeded every call.
		Random rand = new Random();

		// nextInt is normally exclusive of the top value,
		// so add 1 to make it inclusive
		int randomNum = rand.nextInt((max - min) + 1) + min;

		return randomNum;
	}

    /**
     * This method convets dp unit to equivalent device specific value in pixels.
     *
     * @param dp A value in dp(Device independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent Pixels equivalent to dp according to device
     *
     * from: http://stackoverflow.com/a/9563438/339820
     */
    public static float convertDpToPixel(float dp,Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi/160f);
        return px;
    }

    /**
     * This method converts device specific pixels to device independent pixels.
     *
     * @param px A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent db equivalent to px value
     *
     * from: http://stackoverflow.com/a/9563438/339820
     */
    public static float convertPixelsToDp(float px,Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        return dp;
    }

	/*
        http://stackoverflow.com/questions/12408431/how-can-i-get-the-average-colour-of-an-image
     */
	private int getAverageColor(Bitmap bitmap) {
		long red = 0;
		long green = 0;
		long blue = 0;
		long pixelCount = 0;

		for (int y = 0; y < bitmap.getHeight(); y++) {
			for (int x = 0; x < bitmap.getWidth(); x++) {
				int c = bitmap.getPixel(x, y);
				pixelCount++;
				red += Color.red(c);
				green += Color.green(c);
				blue += Color.blue(c);
			}
		}

		int redAverage = (int)(red / pixelCount);
		int greenAverage = (int)(green / pixelCount);
		int blueAverage = (int)(blue / pixelCount);

		return Color.rgb(redAverage, greenAverage, blueAverage);
	}

	public static String extractParamFromUrl(String url, String paramName) {
		String queryString = url.substring(url.indexOf("?", 0)+1,url.length());
		QueryStringParser queryStringParser = new QueryStringParser(queryString);
		return queryStringParser.getQueryParamValue(paramName);
	}
}
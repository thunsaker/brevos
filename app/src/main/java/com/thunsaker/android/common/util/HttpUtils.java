package com.thunsaker.android.common.util;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class HttpUtils {
    public static String LOG_TAG = "HttpUtils";

    public static String getHttpResponse(String url, String contentType, String accepts) {
        return getHttpResponse(url, false, contentType, accepts);
    }

    public static String getHttpResponse(String url, Boolean isHttpPost, String contentType, String accepts) {
        String result = "";
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(url);
        HttpPost httpPost = new HttpPost(url);
        HttpResponse response;

        try {
            if(isHttpPost)
                response = httpclient.execute(httpPost);
            else
                response = httpclient.execute(httpGet);

            HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream instream = entity.getContent();
                result = convertStreamToString(instream);
                instream.close();
            }
        } catch (ClientProtocolException e) {
            Log.e(LOG_TAG, "There was a protocol based error", e);
        } catch (IOException e) {
            Log.e(LOG_TAG, "There was an IO Stream related error", e);
        }
        return result;
    }

    public static String getHttpResponseWithData(String url, Boolean isHttpPost, String contentType, String accepts, String data) {
        String result = "";
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(url);
        HttpPost httpPost = new HttpPost(url);
        HttpResponse response;

        try {
            httpPost.setHeader("Content-Type","application/json");
            httpPost.setEntity(new StringEntity(data, Util.ENCODER_CHARSET));

            if(isHttpPost)
                response = httpclient.execute(httpPost);
            else
                response = httpclient.execute(httpGet);

            HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream instream = entity.getContent();
                result = convertStreamToString(instream);
                instream.close();
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            Log.e(LOG_TAG, "There was a protocol based error", e);
        } catch (IOException e) {
            Log.e(LOG_TAG, "There was an IO Stream related error", e);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}

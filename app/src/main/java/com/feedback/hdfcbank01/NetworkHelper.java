package com.feedback.hdfcbank01;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetworkHelper {

    // GET Request
    public void makeGetRequest(String urlString, final GetRequestCallback callback) {
        new Thread(() -> {
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(urlString);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setConnectTimeout(10000); // 10 seconds timeout
                urlConnection.setReadTimeout(10000);

                int responseCode = urlConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder result = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    reader.close();
                    callback.onSuccess(result.toString());
                } else {
                    callback.onFailure("Request failed with code: " + responseCode);
                }
            } catch (Exception e) {
                callback.onFailure("Error: " + e.getMessage());
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }).start();
    }

    // POST Request
    public void makePostRequest(String urlString, JSONObject data, final PostRequestCallback callback) {
        new Thread(() -> {
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(urlString);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                urlConnection.setDoOutput(true);
                urlConnection.setConnectTimeout(10000);
                urlConnection.setReadTimeout(10000);

                // Write data to output stream
                OutputStream os = urlConnection.getOutputStream();
                OutputStreamWriter writer = new OutputStreamWriter(os, "UTF-8");
                writer.write(data.toString());
                writer.flush();
                writer.close();
                os.close();

                // Read response
                int responseCode = urlConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder result = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    reader.close();
                    callback.onSuccess(result.toString());
                } else {
                    callback.onFailure("Request failed with code: " + responseCode);
                }
            } catch (Exception e) {
                callback.onFailure("Error: " + e.getMessage());
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }).start();
    }

    // Callback interfaces
    public interface GetRequestCallback {
        void onSuccess(String result);
        void onFailure(String error);
    }

    public interface PostRequestCallback {
        void onSuccess(String result);
        void onFailure(String error);
    }
}

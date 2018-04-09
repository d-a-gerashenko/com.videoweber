package com.videoweber.internet.client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Dmitriy Gerashenko <d.a.gerashenko@gmail.com>
 */
public class Client {

    private final URL serverUrl;
    private final String serverKey;

    public Client(String serverUrl, String serverKey) {
        if (serverUrl == null
                || serverKey == null) {
            throw new NullPointerException();
        }
        try {
            this.serverUrl = new URL(serverUrl);
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }
        this.serverKey = serverKey;
    }

    public URL getServerUrl() {
        return serverUrl;
    }

    public String getServerKey() {
        return serverKey;
    }

    public Response send(Request request) {
        if (request == null) {
            throw new NullPointerException();
        }

        JSONObject requestDataJSONObject = new JSONObject();
        requestDataJSONObject.put("key", getServerKey());
        requestDataJSONObject.put("command", request.getCommand());
        if (request.getData() != null) {
            requestDataJSONObject.put("data", request.getData());
        }
        String requestDataString = requestDataJSONObject.toString();

        String responseDataString;
        try {
            responseDataString = sendHttp(requestDataString);
        } catch (Exception ex) {
            throw new RuntimeException(
                    String.format(
                            "Error during request execution (command: %s).",
                            request.getCommand()
                    ),
                    ex
            );
        }

        try {
            JSONObject responseJSONObject;
            try {
                responseJSONObject = new JSONObject(responseDataString);
            } catch (JSONException e) {
                throw new RuntimeException("Can't parse json response:" + responseDataString, e);
            }

            return new Response(
                    Response.Status.valueOf(responseJSONObject.getString("status").toUpperCase()),
                    responseJSONObject.get("data")
            );
        } catch (Exception ex) {
            throw new RuntimeException("Error during parsing of server answer: " + responseDataString, ex);
        }
    }

    private String sendHttp(String requestData) throws Exception {
        String urlParameters = "data=" + URLEncoder.encode(requestData, StandardCharsets.UTF_8.name());

        URL url = getServerUrl();
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        //add reuqest header
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        con.setConnectTimeout(3000);

        // Send post request
        con.setDoOutput(true);
        try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
            wr.writeBytes(urlParameters);
            wr.flush();
        }

        int responseCode = con.getResponseCode();

        StringBuilder responseData;
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream())
        )) {
            String inputLine;
            responseData = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                responseData.append(inputLine);
            }
        }

        if (responseCode != 200) {
            throw new Exception(
                    String.format(
                            "Unexpected response code %s: %s.",
                            responseCode,
                            responseData
                    )
            );
        }

        return responseData.toString();
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.calmio.calm.integration.Helpers;

import com.calmio.calm.integration.data.HTTPResponseData;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.http.auth.AuthScope;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.http.auth.Credentials;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;

/**
 *
 * @author sa
 */
public class HTTPHandler {

    private static final String USER_AGENT = "Mozilla/5.0";

    // HTTP GET request
    public static HTTPResponseData sendGet(String url, String username, String pwd) throws Exception {
        CloseableHttpClient client = HttpClientBuilder.create()
                .setDefaultCredentialsProvider(getCredentialsProvider(url, username, pwd))
                .build();

        int responseCode = 0;
        StringBuffer respo = null;

        try {
            HttpGet request = new HttpGet(url);
            request.addHeader("User-Agent", USER_AGENT);
            System.out.println("Executing request " + request.getRequestLine());
            CloseableHttpResponse response = client.execute(request);
            try {
                responseCode = response.getStatusLine().getStatusCode();
                System.out.println("\nSending 'GET' request to URL : " + url);
                System.out.println("Response Code : " + responseCode);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(response.getEntity().getContent()));
                respo = new StringBuffer();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    respo.append(inputLine);
                }
            } finally {
                response.close();
            }
        } finally {
            client.close();
        }

        HTTPResponseData result = new HTTPResponseData(responseCode, ((respo == null) ? "" : respo.toString()));
        System.out.println(result.getStatusCode() + "/n" + result.getBody());
        return result;
    }
    // HTTP POST request

    public static HTTPResponseData sendPost(String url, String body, String username, String pwd) throws Exception {

        CloseableHttpClient client = HttpClientBuilder.create()
                .setDefaultCredentialsProvider(getCredentialsProvider(url, username, pwd))
                .build();

        int responseCode = 0;
        StringBuffer respo = null;

        try {
            HttpPost request = new HttpPost(url);
            request.addHeader("User-Agent", USER_AGENT);
            request.addHeader("Accept-Language", "en-US,en;q=0.5");
            request.addHeader("Content-Type", "application/json; charset=UTF-8");
            request.setHeader("Accept", "application/json");
            System.out.println("Executing request " + request.getRequestLine());
            StringEntity se = new StringEntity(body);
            request.setEntity(se);
            CloseableHttpResponse response = client.execute(request);
            try {
                responseCode = response.getStatusLine().getStatusCode();
                System.out.println("\nSending 'POST' request to URL : " + url);
                System.out.println("Post body : " + body);
                System.out.println("Response Code : " + responseCode);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(response.getEntity().getContent()));
                respo = new StringBuffer();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    respo.append(inputLine);
                }
            } finally {
                response.close();
            }
        } finally {
            client.close();
        }

        HTTPResponseData result = new HTTPResponseData(responseCode, ((respo == null) ? "" : respo.toString()));
        System.out.println(result.getStatusCode() + "/n" + result.getBody());
        return result;
    }

    private static CredentialsProvider getCredentialsProvider(String url, String user, String pwd) throws URISyntaxException {
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        URI uri = new URI(url);
        String domain = uri.getHost();
        domain = domain.startsWith("www.") ? domain.substring(4) : domain;
        credsProvider.setCredentials(new AuthScope(domain, uri.getPort()), (Credentials) new UsernamePasswordCredentials(user, pwd));
        return credsProvider;
    }

}

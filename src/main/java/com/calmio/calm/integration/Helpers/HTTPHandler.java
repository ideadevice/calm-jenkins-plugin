/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.calmio.calm.integration.Helpers;

import com.calmio.calm.integration.data.HTTPResponseData;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 *
 * @author sa
 */
public class HTTPHandler {


	private static final String USER_AGENT = "Mozilla/5.0";

	// HTTP GET request
	public static HTTPResponseData sendGet(String url, String username, String pwd) throws Exception {
            
		URL obj = new URL(url);
                String userPassword = username + ":" + pwd;
                String encoding = new sun.misc.BASE64Encoder().encode(userPassword.getBytes());
 		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// optional default is GET
                con.setRequestProperty("Authorization", "Basic " + encoding);
		con.setRequestMethod("GET");

		//add request header
		con.setRequestProperty("User-Agent", USER_AGENT);

		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'GET' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);

                StringBuffer response;
            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()))) {
                String inputLine;
                response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
            }
            
            //print result
            HTTPResponseData result = new HTTPResponseData(responseCode, response.toString());
            System.out.println(result.getStatusCode() +"/n"+ result.getBody());
            return result;
	}
	
	// HTTP POST request
	public static HTTPResponseData sendPost(String url, String body, String username, String pwd) throws Exception {

		URL obj = new URL(url);
                String userPassword = username + ":" + pwd;
                String encoding = new sun.misc.BASE64Encoder().encode(userPassword.getBytes());
		HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

		//add reuqest header
		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", USER_AGENT);
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
                con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
		
		// Send post request
		con.setDoOutput(true);
            try (OutputStream wr = con.getOutputStream()) {
                wr.write(body.getBytes());
                wr.flush();
            }

		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'POST' request to URL : " + url);
		System.out.println("Post body : " + body);
		System.out.println("Response Code : " + responseCode);

                StringBuffer response;
            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()))) {
                String inputLine;
                response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
            }
		
            //print result
            HTTPResponseData result = new HTTPResponseData(responseCode, response.toString());
            System.out.println(result.getStatusCode() +"/n"+ result.getBody());
            return result;

	}

}

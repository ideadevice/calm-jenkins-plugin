/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.calmio.calm.integration.data;

import org.json.JSONObject;

/**
 *
 * @author sa
 */
public class HTTPResponseData {
    private int statusCode;
    private String bodyPart;
    private JSONObject jsonBody;
    
    public HTTPResponseData(int status, String body){
        statusCode = (status == 0)? 500: status;
        bodyPart = ((body == null) || (body.trim().equals("")))? "{}": body;
        jsonBody = new JSONObject(bodyPart);
    }
    
    public int getStatusCode() {
        return statusCode;
    }

    public JSONObject getJSONBody() {
        return jsonBody;
    }

    public String getBody() {
        return bodyPart;
    }
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.calmio.calm.integration.Helpers;

import com.calmio.calm.integration.data.CalmMachine;
import com.calmio.calm.integration.data.HTTPResponseData;
import com.calmio.calm.integration.exceptions.CalmIntegrationException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;
import org.json.JSONArray;

/**
 *
 * @author sa
 */
public class CalmCommunicator {

    private String url, uname, password;
    private int apiTimeOut = 1200;
    private final int stepTimer = 300;
    private Map<String, String> functionMap;

    public CalmCommunicator(String baseURL, String username, String pwd, int apiTimeOutInSecs) {
        url = baseURL;
        uname = username;
        password = pwd;
        apiTimeOut = apiTimeOutInSecs * 1000;
        functionMap = new HashMap<String, String>();
        functionMap.put("stop", "STOPPED");
        functionMap.put("start", "SUCCESS");
        functionMap.put("restart", "SUCCESS");
        functionMap.put("upgrade", "SUCCESS");
    }

    public List<CalmMachine> runBP(String jsonBody) throws CalmIntegrationException {
        JSONObject inputJson = new JSONObject(jsonBody);
        String bpName = inputJson.getString("blueprint_name");
        String appName = inputJson.getString("application_name");
        HTTPResponseData respo;
        respo = HTTPHandlerInternal(true, url + "/public/api/1/default/blueprints/run", jsonBody, "Blueprint run ");
        String appID = respo.getJSONBody().getJSONObject("data").getJSONObject("row").getString("application_uid");
        waitForAppRunStatus(appName, "SUCCESS");
        return getAllMachinesFromApp(appID, appName);
    }

    private void waitForAppRunStatus(String appName, String stateToMet) throws CalmIntegrationException {
        String state = "RANDOM";
        int appster = apiTimeOut;
        HTTPResponseData respo;
        while (!state.equalsIgnoreCase(stateToMet) && (appster > stepTimer)) {
            sleepForStepTime(stepTimer);
            appster -= stepTimer;
            respo = getAppStatus(appName);
            state = respo.getJSONBody().getJSONObject("data").getJSONArray("rows").getJSONObject(0).getString("state");
            if (state.equalsIgnoreCase(stateToMet)) {
                break;
            }
        }
    }

    private HTTPResponseData getAppStatus(String appName) throws CalmIntegrationException {
        HTTPResponseData respo;
        respo = HTTPHandlerInternal(false, url + "/api/1/default/applications?name=" + appName, null, "Application get Status ");
        return respo;
    }

    private HTTPResponseData getFlowRunStatus(String appID, String flowID) throws CalmIntegrationException {
        HTTPResponseData respo;
        respo = HTTPHandlerInternal(false, url + "/api/1/default/applications/" + appID + "/runlogs?flow_id=" + flowID, null, "Flow run get Status ");
        return respo;
    }

    private void waitForAppFlowRunStatus(String appID, String flowID, String stateToMet) throws CalmIntegrationException {
        String state = "RANDOM";
        int appster = apiTimeOut;
        HTTPResponseData respo;
        while (!state.equalsIgnoreCase(stateToMet) && (appster > stepTimer)) {
            sleepForStepTime(stepTimer);
            appster -= stepTimer;
            respo = getFlowRunStatus(appID, flowID);
            state = respo.getJSONBody().getJSONObject("data").getJSONArray("rows").getJSONObject(0).getString("status");
            if (state.equalsIgnoreCase(stateToMet)) {
                break;
            }
        }
    }

    public List<CalmMachine> runFlowInApp(String jsonBody) throws CalmIntegrationException {
        JSONObject inputJson = new JSONObject(jsonBody);
        String flowName = inputJson.getString("flow_name");
        String appName = inputJson.getString("application_name");
        HTTPResponseData respo;
        respo = HTTPHandlerInternal(true, url + "/public/api/1/default/applications/flows/run", jsonBody, "Flow run initiation ");
        String flow_id = respo.getJSONBody().getJSONObject("data").getJSONObject("row").getString("flow_run_uid");
        String appID = getAppStatus(appName).getJSONBody().getJSONObject("data").getJSONArray("rows").getJSONObject(0).getString("uid");
        waitForAppFlowRunStatus(appID, flow_id, "SUCCESS");
        return getAllMachinesFromApp(appID, appName);
    }

    public void appActions(String mode, String appName) throws CalmIntegrationException {
        String appID = getAppStatus(appName).getJSONBody().getJSONObject("data").getJSONArray("rows").getJSONObject(0).getString("uid");
        HTTPResponseData respo;
        respo = HTTPHandlerInternal(true, url + "/api/1/default/applications/" + appID + "/" + mode, "{}", "App " + mode + " initiation");
        waitForAppRunStatus(appID, functionMap.get(mode));
    }

    public void serviceActions(String actionType, String jsonBody) throws CalmIntegrationException {
        JSONObject inputJson = new JSONObject(jsonBody);
        String appName = inputJson.getString("application_name");
        String appID = getAppStatus(appName).getJSONBody().getJSONObject("data").getJSONArray("rows").getJSONObject(0).getString("uid");
        HTTPResponseData respo;
        respo = HTTPHandlerInternal(true, url + "/api/1/default/applications/" + ((actionType.equalsIgnoreCase("upgrade")) ? "strategy" : actionType) + "/run", jsonBody, "App " + actionType + " initiation");
        waitForAppRunStatus(appID, functionMap.get(actionType));
    }

    public String deleteApp(String jsonBody) throws CalmIntegrationException {
        JSONObject inputJson = new JSONObject(jsonBody);
        String appName = inputJson.getString("application_name");
        HTTPResponseData respo;
        respo = HTTPHandlerInternal(true, url + "/public/api/1/default/applications/delete", jsonBody, "Application delete initiation ");
        String appID = getAppStatus(appName).getJSONBody().getJSONObject("data").getJSONArray("rows").getJSONObject(0).getString("uid");
        waitForAppRunStatus(appName, "DELETED");
        return appName + "-" + appID;
    }

    private List<CalmMachine> getAllMachinesFromApp(String appID, String appName) throws CalmIntegrationException {
        List<CalmMachine> machineList = new ArrayList<CalmMachine>();
        HTTPResponseData respo;
        respo = HTTPHandlerInternal(false, url + "/api/1/default/resources?calm_application_name=" + appName, null, "Fetching Machine list ");
        JSONArray resourceList = respo.getJSONBody().getJSONObject("data").getJSONArray("rows");
        JSONObject m, bpArch;
        bpArch = HTTPHandlerInternal(false, url + "/api/1/default/applications/" + appID, null, "Fetching Application detail ").getJSONBody().getJSONObject("data").getJSONObject("row").getJSONObject("bp");
        String vmID, vmName, serviceName, ipAddress, credentialName, application, applicationID, cpro, proid, credid;
        for (int i=0; i < resourceList.length(); i++) {
            m = resourceList.getJSONObject(i);
            vmID = m.getString("calm_machine_id");
            vmName = m.getString("vm_name");
            serviceName = m.getString("calm_machine_name");
            serviceName = serviceName.substring(0,serviceName.lastIndexOf("[")).trim();
            ipAddress = m.getString("vm_ip");
            application = m.getString("calm_application_name");
            applicationID = m.getString("calm_application_id");            
            cpro = getValueInJsonArray(bpArch.getJSONArray("architecture"), "name", serviceName, "current_profile");
            proid = getValueInJsonArray(bpArch.getJSONArray("profiles"), "uid", cpro, "provider");
            credid = getValueInJsonArray(bpArch.getJSONArray("tasks"), "uid", proid, "credential_id");
            credentialName = getValueInJsonArray(bpArch.getJSONArray("credentials"), "uid", credid, "name");
            machineList.add(new CalmMachine(vmID, vmName, serviceName, ipAddress, credentialName, application, applicationID));       
        }
        return machineList;
    }
    
    private String getValueInJsonArray(JSONArray ja, String keyToSearch, String valueToEquate, String getKey){
        JSONObject m;
        for (int i=0; i < ja.length(); i++) {
            m = ja.getJSONObject(i);
            if(m.getString(keyToSearch).equalsIgnoreCase(valueToEquate)){
                return m.getString(getKey);
            }            
        }
        return null;
    }

    private void sleepForStepTime(int stepTime) {
        try {
            Thread.sleep(stepTime);
        } catch (InterruptedException ex) {
            Logger.getLogger(CalmCommunicator.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Thread explosion in while waiting");
        }
    }

    private HTTPResponseData HTTPHandlerInternal(boolean method, String url, String params, String ExceptionMsg) throws CalmIntegrationException {
        HTTPResponseData respo;
        try {
            if (method) {
                respo = HTTPHandler.sendPost(url, params, uname, password);
            } else {
                respo = HTTPHandler.sendGet(url, uname, password);
            }
        } catch (Exception ex) {
            Logger.getLogger(CalmCommunicator.class.getName()).log(Level.SEVERE, null, ex);
            throw new CalmIntegrationException(ExceptionMsg + " exception\n Internal:" + ex.getLocalizedMessage());
        }
        if (respo.getStatusCode() != 200) {
            throw new CalmIntegrationException(ExceptionMsg + " exception\n HTTP Response:" + respo.getStatusCode() + "\n HTTP body:" + respo.getBody());
        }
        return respo;
    }
}

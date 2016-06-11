/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.calmio.calm.integration.data;

/**
 *
 * @author sa
 */
public class CalmMachine {
    private String vmid, servName, vname, ip, cred, app, appID, credUser;
    int port;
    public CalmMachine(String vmID, String vmName, String serviceName, String ipAddress, String credentialName, String application, String applicationID, String credUsername, String conPort){
        ip = ipAddress;
        cred = credentialName;
        app = application;
        appID = applicationID;
        vmid = vmID;
        vname = vmName;
        servName = serviceName;
        credUser = credUsername;
        port = Integer.parseInt(conPort);
    }
    
    public String getIP(){
        return ip;
    }

    public int getPort(){
        return port;
    }
    
    public String getVmName(){
        return vname;
    }
    
    public String getServiceName(){
        return servName;
    }
    
    public String getVmID(){
        return vmid;
    }
    
    public String getCred(){
        return cred;
    }

    public String getCredUser(){
        return credUser;
    }
    
    public String getApplication(){
        return app;
    }
    
    public String getApplicationID(){
        return appID;
    }
    
    public String getJenkinsVMName(){
        return vname + servName.replaceAll(" ", "") + vmid;
    }
    
    public String getJenkinsVMLabel(){
        return app + "-" + appID;
    }
}

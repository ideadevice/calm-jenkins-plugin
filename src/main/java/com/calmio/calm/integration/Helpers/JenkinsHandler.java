/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.calmio.calm.integration.Helpers;

import com.calmio.calm.integration.data.CalmMachine;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.cloudbees.plugins.credentials.domains.SchemeRequirement;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.model.Descriptor;
import hudson.model.Node;
import java.io.IOException;
import hudson.plugins.sshslaves.SSHLauncher;
import hudson.slaves.DumbSlave;
import hudson.slaves.RetentionStrategy;
import java.util.List;
import jenkins.model.Jenkins;

/**
 *
 * @author shriyanshagnihotri
 */
public class JenkinsHandler {

    public static final SchemeRequirement SSH_SCHEME = new SchemeRequirement("ssh");
    
    @SuppressFBWarnings
    private static void addInstanceToJenkins(CalmMachine m) throws IOException, Descriptor.FormException {
        Jenkins.getInstance().addNode(new DumbSlave(m.getJenkinsVMName(), "Calm added slave for app:" + m.getApplication(), ".", "1", Node.Mode.NORMAL, m.getJenkinsVMLabel(), new SSHLauncher(m.getIP(), m.getPort(), getCredFromJenkins(m.getCredUser(), m.getCred()), "", "", "", ""), new RetentionStrategy.Always(), new java.util.LinkedList()));
    }

    @SuppressFBWarnings
    private static String getCredFromJenkins(String credUserName, String credEntityName) {
        List<StandardUsernameCredentials> creds = CredentialsProvider.lookupCredentials(StandardUsernameCredentials.class, Jenkins.getInstance(), null, (DomainRequirement) null);
        for (StandardUsernameCredentials c : creds) {
            if (c.getUsername().trim().equalsIgnoreCase(credUserName.trim()) && c.getDescription().contains(credEntityName.trim())) {
                return c.getId();
            }
        }
        return null;
    }

    @SuppressFBWarnings
    public static void deleteAllInstanceForLabel(String label) throws IOException {
        for (Node n : Jenkins.getInstance().getLabel(label).getNodes()) {
            Jenkins.getInstance().removeNode(n);
        }
    }
    
    @SuppressFBWarnings
    public static void addInstanceAllToJenkins(List<CalmMachine> lm) throws IOException, Descriptor.FormException {
        deleteAllInstanceForLabel(lm.get(0).getJenkinsVMLabel());
        for (CalmMachine m : lm) {
            addInstanceToJenkins(m);
        }
    }

}

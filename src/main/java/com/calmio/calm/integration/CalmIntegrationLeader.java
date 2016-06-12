/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.calmio.calm.integration;

import com.calmio.calm.integration.Helpers.CalmCommunicator;
import com.calmio.calm.integration.Helpers.JenkinsHandler;
import com.calmio.calm.integration.exceptions.CalmIntegrationException;
import hudson.Launcher;
import hudson.Extension;
import hudson.util.FormValidation;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sa
 */
public class CalmIntegrationLeader extends Builder {

    private final String event, triggerBody;
    private static final String[] triggers = {"runBP", "runFlow", "runAppActionStart", "runAppActionStop", "runAppActionRestart", "runAppDelete", "runServiceActionupgrade"};

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public CalmIntegrationLeader(String event, String triggerBody) {
        this.event = event;
        this.triggerBody = triggerBody;

    }

    /**
     * We'll use this from the <tt>config.jelly</tt>.
     */
    public String getEvent() {
        return event;
    }

    public String getTriggerBody() {
        return triggerBody;
    }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException {
        // This is where you 'build' the project.
        PrintStream log = listener.getLogger();
        log.println("##Executing Calm Integration Plugin Build Step##");
        CalmCommunicator leader = new CalmCommunicator(getDescriptor().getURL(), getDescriptor().getUser(), getDescriptor().getPwd(), getDescriptor().getTimeOut(), log);
        log.println("##Calm Communicator Initialized##");
        try {
            switch (event) {
                case "runBP":
                    JenkinsHandler.addInstanceAllToJenkins(leader.runBP(getTriggerBody()));
                    break;
                case "runFlow":
                    JenkinsHandler.addInstanceAllToJenkins(leader.runFlowInApp(getTriggerBody()));
                    break;
                case "runAppActionStart":
                    leader.appActions("start", getTriggerBody());
                    break;
                case "runAppActionStop":
                    leader.appActions("stop", getTriggerBody());
                    break;
                case "runAppActionRestart":
                    leader.appActions("restart", getTriggerBody());
                    break;
                case "runAppDelete":
                    JenkinsHandler.deleteAllInstanceForLabel(leader.deleteApp(getTriggerBody()));
                    break;
                case "runServiceActionupgrade":
                    leader.serviceActions("upgrade", getTriggerBody());
                    break;
                default:
            }
        } catch (CalmIntegrationException ex) {
            log.println("Calm Exception: " + ex.getLocalizedMessage());
            return false;
        } catch (Descriptor.FormException ex) {
            Logger.getLogger(CalmIntegrationLeader.class.getName()).log(Level.SEVERE, null, ex);
            log.println("Form Exception: " + ex.getLocalizedMessage());
            return false;
        }
        return true;
    }

    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    /**
     * Descriptor for {@link CalmIntegrationLeader}. Used as a singleton. The
     * class is marked as public so that it can be accessed from views.
     *
     * <p>
     * See
     * <tt>src/main/resources/hudson/plugins//com/calmio/calm/integration/CalmIntegrationLeader/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
     */
    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        /**
         * To persist global configuration information, simply store it in a
         * field and call save().
         *
         * <p>
         * If you don't want fields to be persisted, use <tt>transient</tt>.
         */
        private String calmURL, calmUser, calmPwd;
        private int calmTimeOut;

        /**
         * Performs on-the-fly validation of the form field 'trigger'.
         *
         * @param value This parameter receives the value that the user has
         * typed.
         * @return Indicates the outcome of the validation. This is sent to the
         * browser.
         */
        public FormValidation doCheckEvent(@QueryParameter String value)
                throws IOException, ServletException {
            boolean checker = false;
            if (value.length() == 0) {
                return FormValidation.error("Please set a Trigger");
            }
            for (String tri : triggers) {
                if (value.trim().equalsIgnoreCase(tri)) {
                    checker = true;
                }
            }
            if (!checker) {
                return FormValidation.error("Invalid Trigger");
            }
            // return FormValidation.warning("Isn't the name too short?");
            return FormValidation.ok();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types 
            return true;
        }

        /**
         * This human readable name is used in the configuration screen.
         */
        public String getDisplayName() {
            return "Calm Integration";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            // To persist global configuration information,
            // set that to properties and call save().
            calmURL = formData.getString("calmURL");
            calmUser = formData.getString("calmUser");
            calmPwd = formData.getString("calmPwd");
            calmTimeOut = formData.getInt("calmTimeOut");
            // ^Can also use req.bindJSON(this, formData);
            //  (easier when there are many fields; need set* methods for this, like setUseFrench)
            save();
            return super.configure(req, formData);
        }

        /**
         * This method returns true if the global configuration says we should
         * speak French.
         *
         * The method trigger is bit awkward because global.jelly calls this
         * method to determine the initial state of the checkbox by the naming
         * convention.
         */
        public String getURL() {
            return calmURL;
        }

        public String getUser() {
            return calmUser;
        }

        public String getPwd() {
            return calmPwd;
        }

        public int getTimeOut() {
            return calmTimeOut;
        }
    }

}

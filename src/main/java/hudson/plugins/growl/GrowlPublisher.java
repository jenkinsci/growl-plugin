package hudson.plugins.growl;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.json.JSONObject;
import net.sf.libgrowl.Application;
import net.sf.libgrowl.GrowlConnector;
import net.sf.libgrowl.IResponse;
import net.sf.libgrowl.Notification;
import net.sf.libgrowl.NotificationType;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import hudson.Extension;
import hudson.Functions;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.tasks.Mailer;

import org.jvnet.hudson.plugins.MacGrowler;

/**
 * @author srb55
 */

@SuppressWarnings("unchecked")
public class GrowlPublisher extends Notifier {

    private static final List<String> VALUES_REPLACED_WITH_NULL = Arrays.asList("", "(Default)",
            "(System Default)");

    private static final Logger LOGGER = Logger.getLogger(GrowlPublisher.class.getName());
    private final String appName = "Growler";
    
    private String IP;
    private Boolean onlyOnFailureOrRecovery;

    private GrowlPublisher(String IP, Boolean onlyOnFailureOrRecovery) {
        this.onlyOnFailureOrRecovery = onlyOnFailureOrRecovery;
        this.IP = IP;
    }

    @DataBoundConstructor
    public GrowlPublisher(String IP,String onlyOnFailureOrRecovery) {
        this(cleanToString(IP), cleanToBoolean(onlyOnFailureOrRecovery));
    }

    private static String cleanToString(String string) {
        return VALUES_REPLACED_WITH_NULL.contains(string) ? null : string;
    }

    private static Boolean cleanToBoolean(String string) {
    	Boolean result = null;
    	if ("true".equals(string) || "Yes".equals(string)) {
    		result = Boolean.TRUE;
    	} else if ("false".equals(string) || "No".equals(string)) {
    		result = Boolean.FALSE;
    	}
    	return result;
    }

   private static String createTinyUrl(String url) throws IOException {
        HttpClient client = new HttpClient();
        GetMethod gm = new GetMethod("http://tinyurl.com/api-create.php?url="
                + url.replace(" ", "%20"));

        int status = client.executeMethod(gm);
        if (status == HttpStatus.SC_OK) {
            return gm.getResponseBodyAsString();
        } else {
            throw new IOException("Non-OK response code back from tinyurl: " + status);
        }

    }
   
   private String createGrowlMessage(AbstractBuild<?, ?> build) {
		String projectName = build.getProject().getName();
		String result = build.getResult().toString();
	
		String tinyUrl = "";

		String absoluteBuildURL = ((DescriptorImpl) getDescriptor()).getUrl() + build.getUrl();
		try {
			tinyUrl = createTinyUrl(absoluteBuildURL);
		} catch (Exception e) {
			tinyUrl = "?";
		}
		
		return String.format("Project: %s\nStatus: %s\nBuild Number: %d\nURL:%s", projectName, result, build.number, tinyUrl);
	}

   	private boolean pingHost(String host) {
   		
		try
		{
		    InetAddress address = InetAddress.getByName(host);
		    return address.isReachable(10000);
		
		} catch (Exception e)
		{
		    e.printStackTrace();
		    return false;
		}
   		

   	}
    public String getIP() {
        return IP;
    }

    public Boolean getOnlyOnFailureOrRecovery() {
        return onlyOnFailureOrRecovery;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {
    	if (shouldgrowl(build)) {
    		String password = ((DescriptorImpl) getDescriptor()).password;
            try {
            	String [] clients = IP.replace(" ","").split(",");
            	for (int i=0; i< clients.length; i++) {
            		if (pingHost(clients[i])) {
            			LOGGER.log(Level.INFO, "Sending Growl to " + clients[i] + "...");
                    	String message = createGrowlMessage(build);
                    	
                        GrowlConnector growl = new GrowlConnector(clients[i]);
                        growl.setPassword(password);
                        
                        Application hudsonApp = new Application("Hudson");
                        hudsonApp.setIcon("http://hudson-ci.org/images/butler.png");
                       
                        NotificationType buildNotify = new NotificationType("BuildNotify");
                        NotificationType[] notificationTypes = new NotificationType[] { buildNotify };
                        
                        growl.register(hudsonApp, notificationTypes);

                        Notification hudsonNotify = new Notification(hudsonApp, buildNotify, "Hudson Build", message);
                        hudsonNotify.setSticky(true);
                        
                        if (growl.notify(hudsonNotify) != IResponse.OK){
                            MacGrowler notifier = MacGrowler.register( appName, password, clients[i]);
                            notifier.notify( appName, "Hudson Build", message, password);
                        }
            		} else {
            			LOGGER.log(Level.INFO, "Cannot send  Growl to " + clients[i] + ", host is down.");
            		}
                	
            	}
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Unable to send growl.", e);
                return false;
            }
    	}
    	
        return true;
    }
    
    /**
	 * Determine if this build results should be growled. Uses the local
	 * settings if they are provided, otherwise the global settings.
	 *
	 * @param build the Build object
	 * @return true if we should growl this build result
	 */
	protected boolean shouldgrowl(AbstractBuild<?, ?> build) {
		if (onlyOnFailureOrRecovery == null) {
			if (((DescriptorImpl) getDescriptor()).onlyOnFailureOrRecovery) {
				return isFailureOrRecovery(build);
			} else {
				return true;
			}
		} else if (onlyOnFailureOrRecovery.booleanValue()) {
			return isFailureOrRecovery(build);
		} else {
			return true;
		}
	}
    
    /**
     * Determine if this build represents a failure or recovery. A build failure
     * includes both failed and unstable builds. A recovery is defined as a
     * successful build that follows a build that was not successful. Always
     * returns false for aborted builds.
     *
     * @param build the Build object
     * @return true if this build represents a recovery or failure
     */
    protected boolean isFailureOrRecovery(AbstractBuild<?, ?> build) {
        if (build.getResult() == Result.FAILURE || build.getResult() == Result.UNSTABLE) {
            return true;
        } else if (build.getResult() == Result.SUCCESS) {
        	AbstractBuild<?, ?> previousBuild = build.getPreviousBuild();
            if (previousBuild != null && previousBuild.getResult() != Result.SUCCESS) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

   
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        public String password;
        public boolean onlyOnFailureOrRecovery;
        public String hudsonUrl;
        
        public DescriptorImpl() {
            super(GrowlPublisher.class);
            load();
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            // set the booleans to false as defaults
            onlyOnFailureOrRecovery = false;

            req.bindParameters(this, "growl.");
            hudsonUrl = Mailer.descriptor().getUrl();
            
            save();
            return super.configure(req, formData);
			//return includeUrl;
        }

        @Override
        public String getDisplayName() {
            return "Growl";
        }

        public String getPassword() {
            return password;
        }

        public String getUrl() {
            return hudsonUrl;
        }
        
        public boolean isOnlyOnFailureOrRecovery() {
            return onlyOnFailureOrRecovery;
        }

        @Override
        public boolean isApplicable(@SuppressWarnings("rawtypes") Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public Publisher newInstance(StaplerRequest req, JSONObject formData) throws FormException {
        	
            if (hudsonUrl == null) {
                //if Hudson URL is not configured yet, infer some default
                hudsonUrl = Functions.inferHudsonURL(req);
                save();
            }
            
            return super.newInstance(req, formData);
        }
    }
}

package hudson.plugins.growl;

import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import hudson.Extension;
import hudson.Functions;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Descriptor.FormException;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.tasks.Mailer;

import hudson.plugins.growl.growler.Growler;
import hudson.plugins.growl.growler.MacGrowler;
import hudson.plugins.growl.util.Cleaner;
import hudson.plugins.growl.util.Message;
import hudson.plugins.growl.util.Pinger;

/**
 * @author srb55
 */

@SuppressWarnings("unchecked")
public class GrowlPublisher extends Notifier {

		private static final Logger LOGGER = Logger.getLogger(GrowlPublisher.class.getName());
		
		private String IP;
		private Boolean onlyOnFailureOrRecovery;

		private GrowlPublisher(String IP, Boolean onlyOnFailureOrRecovery) {
				this.onlyOnFailureOrRecovery = onlyOnFailureOrRecovery;
				this.IP = IP;
		}

		@DataBoundConstructor
		public GrowlPublisher(String IP,String onlyOnFailureOrRecovery) {
				this(Cleaner.toString(IP), Cleaner.toBoolean(onlyOnFailureOrRecovery));
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

	private String createGrowlMessage(AbstractBuild<?, ?> build){
		return new Message(build, (DescriptorImpl)getDescriptor()).create();
	}

		@Override
		public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {
			if (!shouldGrowl(build)) {
				return true;
			}
			String password = ((DescriptorImpl) getDescriptor()).password;
			try {
				String [] clients = IP.replace(" ","").split(",");
				for(String clientIp : clients) {
					if (Pinger.host(clientIp)) {
						LOGGER.log(Level.INFO, "Sending Growl to " + clientIp + "...");
						new Growler().send(clientIp, password, createGrowlMessage(build));
					} else {
						LOGGER.log(Level.INFO, "Cannot send	 Growl to " + clientIp + ", host is down.");
					}
				}
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, "Unable to send growl.", e);
				return false;
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
	protected boolean shouldGrowl(AbstractBuild<?, ?> build) {
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
		}
		if (build.getResult() != Result.SUCCESS) {
			return false;
		}
		AbstractBuild<?, ?> previousBuild = build.getPreviousBuild();
		if (previousBuild != null && previousBuild.getResult() != Result.SUCCESS) {
			return true;
		} else {
			return false;
		}
	}
	
	
	@Extension
	public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

		public String password;
		public boolean onlyOnFailureOrRecovery;
		public String hudsonUrl;

		public DescriptorImpl() {
			super(GrowlPublisher.class);
			load();
		}

		@Override
		public boolean configure(StaplerRequest req, JSONObject formData)
				throws FormException {
			// set the booleans to false as defaults
			onlyOnFailureOrRecovery = false;

			req.bindParameters(this, "growl.");
			hudsonUrl = Mailer.descriptor().getUrl();

			save();
			return super.configure(req, formData);
			// return includeUrl;
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
		public boolean isApplicable(
				@SuppressWarnings("rawtypes") Class<? extends AbstractProject> jobType) {
			return true;
		}

		@Override
		public Publisher newInstance(StaplerRequest req, JSONObject formData)
				throws FormException {

			if (hudsonUrl == null) {
				// if Hudson URL is not configured yet, infer some default
				hudsonUrl = Functions.inferHudsonURL(req);
				save();
			}
			return super.newInstance(req, formData);
		}
	}


}
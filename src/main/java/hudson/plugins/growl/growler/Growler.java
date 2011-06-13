package hudson.plugins.growl.growler;

import hudson.plugins.growl.util.Message;
import net.sf.libgrowl.Application;
import net.sf.libgrowl.GrowlConnector;
import net.sf.libgrowl.Notification;
import net.sf.libgrowl.NotificationType;
import net.sf.libgrowl.internal.IResponse;

public class Growler  {
	/*
	 * App Name
	 */
	private final String appName = "Growler";
	
	/*
	 * App Growl Name
	 */
	private final String appNGrowlName = "Jenkins";
	private final String buildName = appNGrowlName + " Build";
	
	private NotificationType buildNotify;
	private Application jenkinsApp;

	public Growler(){
		
		jenkinsApp = new Application(appNGrowlName);
		jenkinsApp.setIcon("http://jenkins-ci.org/images/butler.png");

		buildNotify = new NotificationType("BuildNotify");
	}
	
	public void send(String clientIp, String password, Message message){
		String messageText = message.getMessageText();
		GrowlConnector growl = new GrowlConnector(clientIp);
		growl.setPassword(password);
		growl.register(jenkinsApp,  new NotificationType[] { buildNotify } );

		Notification jenkinsNotify = new Notification(jenkinsApp, buildNotify, buildName, messageText);
		jenkinsNotify.setSticky(true);
		jenkinsNotify.setCallBackURL(message.getUrl());

		// if growl can't send notification, try to send to mac.
		if (growl.notify(jenkinsNotify) != IResponse.OK){
			MacGrowler notifier = MacGrowler.register( appName, password, clientIp);
			notifier.notify( appName, buildName, messageText, password);
		}
	}
	
}
package hudson.plugins.growl.util;

import hudson.model.AbstractBuild;
import hudson.plugins.growl.GrowlPublisher.DescriptorImpl;

public class Message {
	
	private AbstractBuild<?, ?> build;
	private DescriptorImpl descriptor;
    private String url;
    private String messageText;
	
	public Message(AbstractBuild<?, ?> build, DescriptorImpl descriptor){
		this.build = build;
		this.descriptor = descriptor;
		this.url = "";
		this.messageText = "";
	}
	
	private void create(){
		String projectName = build.getProject().getName();
		String result = build.getResult().toString();
	
		String absoluteBuildURL = descriptor.getUrl() + build.getUrl();
		setUrl(absoluteBuildURL);

		setMessageText(String.format("Project: %s\nStatus: %s\nBuild Number: %d", projectName, result, build.number));
	}

	private void setUrl(String url) {
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

	private void setMessageText(String messageText) {
		this.messageText = messageText;
	}

	public String getMessageText() {
		if(messageText.isEmpty()) {
			create();
		}
		
		return messageText;
	}
		 
	
}
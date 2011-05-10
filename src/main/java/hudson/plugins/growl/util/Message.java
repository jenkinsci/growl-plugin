package hudson.plugins.growl.util;


import hudson.model.AbstractBuild;
import hudson.plugins.growl.GrowlPublisher.DescriptorImpl;

public class Message {
	
	private AbstractBuild build;
	DescriptorImpl descriptor;

	
	public Message(AbstractBuild build, DescriptorImpl descriptor){
		this.build = build;
		this.descriptor = descriptor;
	}
	
	public String create(){
		String projectName = build.getProject().getName();
		String result = build.getResult().toString();
	
		String tinyUrl = "";

		String absoluteBuildURL = descriptor.getUrl() + build.getUrl();
		tinyUrl = absoluteBuildURL;

		return String.format("Project: %s\nStatus: %s\nBuild Number: %d\nURL:%s", projectName, result, build.number, tinyUrl);
	}
		 
	
}
package hudson.plugins.growl;


import java.io.IOException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;

import hudson.model.AbstractBuild;
import hudson.plugins.growl.GrowlPublisher.DescriptorImpl;

public class Message {
	private AbstractBuild<?, ?> build;
	DescriptorImpl descriptor;
	public Message(AbstractBuild<?, ?> build, DescriptorImpl descriptor){
		this.build = build;
		this.descriptor = descriptor;
	}
	
	public String createMessage(){
		String projectName = build.getProject().getName();
		String result = build.getResult().toString();
	
		String tinyUrl = "";

		String absoluteBuildURL = descriptor.getUrl() + build.getUrl();
		try {
			tinyUrl = createTinyUrl(absoluteBuildURL);
		} catch (Exception e) {
			tinyUrl = "?";
		}
		
		return String.format("Project: %s\nStatus: %s\nBuild Number: %d\nURL:%s", projectName, result, build.number, tinyUrl);
	}
	
 private String createTinyUrl(String url) throws IOException {
		HttpClient client = new HttpClient();
		GetMethod gm = new GetMethod("http://tinyurl.com/api-create.php?url="+ url.replace(" ", "%20"));
		int status = client.executeMethod(gm);
		if (status == HttpStatus.SC_OK) {
				return gm.getResponseBodyAsString();
		} else {
				throw new IOException("Non-OK response code back from tinyurl: " + status);
		}
	}
	 
	
}
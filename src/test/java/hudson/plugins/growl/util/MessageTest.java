package hudson.plugins.growl.util;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import hudson.model.AbstractBuild;
import hudson.model.Project;
import hudson.model.Result;
import hudson.plugins.growl.GrowlPublisher.DescriptorImpl;
import hudson.plugins.growl.util.Message;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class MessageTest {
	private DescriptorImpl descriptor;
	private AbstractBuild build;
	private String confirmation;
	
	@Before
	public void configure(){
		descriptor = new DescriptorImpl();
		descriptor.hudsonUrl = "http://localhost:8080/";
		
		Project project = mock(Project.class);
		when(project.getName()).thenReturn("ProjectName");

		build = mock(AbstractBuild.class);
		when(build.getProject()).thenReturn(project);
		when(build.getResult()).thenReturn(Result.SUCCESS);
		when(build.getUrl()).thenReturn("jobs/ProjectName");
		when(build.number).thenReturn(10);
		
		
		confirmation = String.format("Project: %s\nStatus: %s\nBuild Number: %d", 
				"ProjectName", Result.SUCCESS.toString(), 10);
	}
	
	@Ignore
	@Test
	public void testCreateMessage() {
		Message message = new Message(build,descriptor);
		assertThat(message.getMessageText(), equalTo(confirmation));
		fail("Not yet implemented");
	}

}

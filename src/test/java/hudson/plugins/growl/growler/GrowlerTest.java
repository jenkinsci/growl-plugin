package hudson.plugins.growl.growler;

import org.junit.Test;

public class GrowlerTest {

	/*
	 * TODO: How to test?! GrowlConnector.notify is final.
	 */
	@Test
	public void testSend() {
		Growler growler = new Growler();
		growler.send("localhost", "", "Some text");
	}

}

package hudson.plugins.growl.growler;

import static org.junit.Assert.*;
import org.junit.Test;

public class GrowlerTest {

	/*
	 * TODO: How to test?! GrowlConnector.notify is final.
	 */
	@Test
	public void testSend() {
		Growler growler = new Growler();
		assertNotNull(growler);
	}

}

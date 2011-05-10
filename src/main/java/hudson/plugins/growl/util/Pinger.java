package hudson.plugins.growl.util;

import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Pinger {
	private static final Logger LOGGER = Logger.getLogger(Pinger.class.getName());
	
		private static boolean fallbackPingHost(String host) {
			try {
				String complement = (System.getProperty("os.name").toLowerCase().indexOf("win")>=0) ? "-n 1 -w 1000 " : "-c 1 -W 1 ";
				String command = "ping " + complement + host ;
				Process p = Runtime.getRuntime().exec(command);
				p.waitFor();
				return (p.exitValue() == 0);
			} catch (Exception e) {
				return false;
			}
		}

	public static boolean host(String host) {
		try{
			InetAddress address = InetAddress.getByName(host);
			return (address.isReachable(10000) || fallbackPingHost(host));
		} catch (Exception e){
			LOGGER.log(Level.SEVERE, "Unable to ping host:" + host + ".", e);
			return false;
		}
	}
	
}
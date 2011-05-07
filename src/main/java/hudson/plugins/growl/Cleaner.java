package hudson.plugins.growl;

import java.util.Arrays;
import java.util.List;

public class Cleaner {
	public Cleaner(){}
	private static final List<String> VALUES_REPLACED_WITH_NULL = Arrays.asList("", "(Default)","(System Default)");
	
	public static Boolean toBoolean(String value){
		Boolean result = null;
		if ("true".equals(value) || "Yes".equals(value)) {
			result = Boolean.TRUE;
		} else if ("false".equals(value) || "No".equals(value)) {
			result = Boolean.FALSE;
		}
		return result;
	}

	public static String toString(String string) {
		return VALUES_REPLACED_WITH_NULL.contains(string) ? null : string;
	}

}
package de.AS.Bau.utils;

import java.util.ArrayList;
import java.util.List;

public class HelperMethods {

	public static boolean isInt(String testString) {
		try {
			Integer.parseInt(testString);
		}catch(NumberFormatException ex) {
			return false;
		}
		return true;
	}
	
	public static boolean argsAreInt(String[] args, int beginIndex, int endIndex) {
		for(int i = beginIndex;i<=endIndex;i++) {
			try {
				Integer.parseInt(args[i]);
			}catch(NumberFormatException ex) {
				return false;
			}
		}
		return true;
	}
	
	public static boolean argsArepositiveInt(String[] args, int beginIndex, int endIndex) {
		for(int i = beginIndex;i<=endIndex;i++) {
			try {
				if(Integer.parseInt(args[i])<0) {
					return false;
				}
			}catch(NumberFormatException ex) {
				return false;
			}
		}
		return true;
	}
	
	public static List<String> checkFortiped(String tiped, List<String> outr) {
		List<String> output = new ArrayList<String>();
		for (String string : outr) {
			if (string.toLowerCase().contains(tiped.toLowerCase())) {
				output.add(string);
			}
		}
		return output;
	}

}

package net.wargearworld.Bau.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

	
	public static String getTime() {
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy' 'HH:mm:ss");
		Date date = new Date();
		String time = "[" + formatter.format(date) + "]";
		return time;
	}
}

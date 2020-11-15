package net.wargearworld.bau.utils;

import com.google.common.base.CharMatcher;

import java.text.SimpleDateFormat;
import java.util.*;

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
		SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy' 'HH:mm:ss");
		Date date = new Date();
		String time = "[" + formatter.format(date) + "]";
		return time;
	}

    public static boolean isAscii(String string) {
		return CharMatcher.ascii().matchesAllOf(string);
    }
	public static Set<Integer> getMiddlePositions(int amountOfItems){
		if(amountOfItems > 9) {
			amountOfItems = 9;
			System.err.println("Dont use more than 9 Pages to edit Kits!");
		}
		/*
		 * Calc: if its an odd number -> begin in the middle with setting items -> than
		 * let air to left and place, let air on richt and so on
		 */
		boolean odd = amountOfItems % 2 == 1;
		int remaining = amountOfItems;
		TreeSet<Integer> out = new TreeSet<Integer>();
		int begin;

		/* left side */
		if (odd) {
			begin = 4;
		} else {
			begin = 3;
		}
		int currentSet = begin;
		while (remaining > amountOfItems / 2) {
			if (currentSet < 0) {
				begin--;
				currentSet = begin;
			}
			out.add(currentSet);
			remaining--;
			currentSet -= 2;
		}

		/* right side */
		if (odd) {
			begin = 6;
		} else {
			begin = 5;
		}
		currentSet = begin;
		while (remaining > 0) {
			if (currentSet >= 9) {
				begin++;
				currentSet = begin;
			}
			out.add(currentSet);
			remaining--;
			currentSet += 2;
		}
		return out;
	}
}

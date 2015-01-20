package com.logginghub.web;

import java.util.Map;
import java.util.Set;

public class Form {

	private Map<String, String[]> paramaters;

	public Form(Map<String, String[]> paramaters) {
		this.paramaters = paramaters;
	}

	public String[] getValues(String string) {
		String[] strings = paramaters.get(string);
		return strings;
	}

	public String getFirstValue(String string) {

		String first;

		String[] strings = paramaters.get(string);
		if (strings == null || strings.length == 0) {
			first = null;
		} else {
			first = strings[0];
		}

		return first;

	}

	public Set<String> getKeys() {
		return paramaters.keySet();
	}

	public String getFirstValue(String string, String defaultValue) {
		String firstValue = getFirstValue(string);
		if (firstValue == null) {
			firstValue = defaultValue;
		}
		return firstValue;

	}

	public boolean getFirstBoolean(String string) {
		return Boolean.parseBoolean(getFirstValue(string, "false"));
	}

	public long getFirstLong(String string, long defaultValue) {

		long value;

		String result = getFirstValue(string);
		if (result == null) {
			value = defaultValue;
		} else {
			value = Long.parseLong(result);
		}

		return value;
	}

	public int getFirstInt(String string, int defaultValue) {
		int value;

		String result = getFirstValue(string);
		if (result == null) {
			value = defaultValue;
		} else {
			value = Integer.parseInt(result);
		}

		return value;
	}

}

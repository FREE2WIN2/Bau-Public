package de.AS.Bau.utils;

public enum ClickAction {
	RUN_COMMAND("run_command"),SUGGEST_COMMAND("suggest_command"), OPEN_URL("open_url");
	String action;
	ClickAction(String actionSet) {
		action = actionSet;
	}
	public String getAction(){
		return action;
	}
}

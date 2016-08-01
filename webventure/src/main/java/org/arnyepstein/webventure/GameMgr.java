package org.arnyepstein.webventure;

import adv32.Adv32;
import com.google.gson.Gson;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * This handles the startup of the context.  It will create the connection to SQS and stash it into the serviet
 * Context.
 */
public class GameMgr  {

	private final static Gson gson = new Gson();

	private static final Logger log = Logger.getLogger(GameMgr.class);

	public static class UserInfo {
		String screenName;
		Object savedGame;
		public UserInfo(String screenName) {
			this.screenName = screenName;
		}
	}

	Map<String, Adv32> activeGames = new HashMap<>();
	Map<String, UserInfo> userDb = new HashMap<>();

	synchronized public boolean addScreenName(String screenName) {
		if(userDb.containsKey(screenName)) {
			return false;
		}
		userDb.put(screenName, new UserInfo(screenName));
		return true;
	}

	synchronized public Adv32 findGame(String screenName) {
		return activeGames.get(screenName);
	}

	synchronized public void setActiveGame(String screenName, Adv32 game) {
		activeGames.put(screenName, game);
	}

	synchronized public void endActiveGame(String screenName) {
		 activeGames.remove(screenName);
	}

}

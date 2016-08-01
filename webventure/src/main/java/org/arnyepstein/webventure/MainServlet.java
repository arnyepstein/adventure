package org.arnyepstein.webventure;

import adv32.Adv32;
import adv32.EmergencyExit;
import com.google.gson.Gson;
import org.apache.log4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This handles the startup of the context.  It will create the connection to SQS and stash it into the serviet
 * Context.
 */
public class MainServlet extends HttpServlet {

	private final static Gson gson = new Gson();

	private static final Logger log = Logger.getLogger(MainServlet.class);

	private GameMgr gameMgr = new GameMgr();

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	private final static String INFO_KEY = "REST_INFO";

	private static class RestInfo {
		String uri;
		String contextPath;
		String info;
		String[] parts;
		String apiGroup;
		String apiInfo;
	}
	//	public final static String AUTHENTICATION_CONTEXT_KEY = "AUTHENTICATION_CONTEXT";
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
		throws IOException, ServletException
	{
		RestInfo restInfo = new RestInfo();

		restInfo.uri = req.getRequestURI();
		restInfo.contextPath = req.getContextPath();
		restInfo.info = restInfo.uri.substring(restInfo.contextPath.length()+1);
		String[] parts = restInfo.info.split("/", 2);
		restInfo.apiGroup = parts[0];
		if(parts.length > 1) {
			restInfo.apiInfo = parts[1];
			restInfo.parts = parts[1].split("/");
		} else {
			restInfo.apiInfo = "";
		}
		if("game".equals(restInfo.apiGroup)) {
			doGameApi(req, resp, restInfo);
		} else {
			resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	private RestInfo getRestInfo(HttpServletRequest req) {
		return (RestInfo) req.getAttribute(INFO_KEY);
	}

//	public void doNoApi(HttpServletRequest req, HttpServletResponse resp, RestInfo restInfo)
//		throws IOException {
//		if(req.getMethod().equals("GET") && "".equals(restInfo.info)) {
//			resp.sendRedirect(restInfo.contextPath + "/index.html");
//		} else {
//			resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
//		}
//	}

	private boolean isPost(HttpServletRequest req) {
		return "POST".equals(req.getMethod());
	}
	private boolean isGet(HttpServletRequest req) {
		return "GET".equals(req.getMethod());
	}

	// Supported URI's
	// game/input (POST): UI providing line of user input
	// game/user (POST): Request to create a new screen name


	public void doGameApi(HttpServletRequest req, HttpServletResponse resp, RestInfo restInfo)
		throws IOException
	{
		if(isPost(req) && "input".equals(restInfo.apiInfo)) {
			doMove(req, resp);
		} else 	if(isPost(req) && "user".equals(restInfo.apiInfo)) {
			addUser(req, resp, restInfo);
		} else {
			resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	// ---------------------------------------------------------------------
	private static class AddUserRequest {
		public String name;
	}
	// ---------------------------------------------------------------------
	private static class AddUserResponse {
		String status;
	}
	// ---------------------------------------------------------------------
	private void addUser(HttpServletRequest req, HttpServletResponse resp, RestInfo restInfo)
		throws IOException
	{
		AddUserResponse response = new AddUserResponse();
		AddUserRequest body = gson.fromJson(req.getReader(), AddUserRequest.class);
		boolean success = gameMgr.addScreenName(body.name);
		response.status = success ? "Added" : "InUse";
		gson.toJson(response, resp.getWriter());
	}


	// ---------------------------------------------------------------------
	private static class GameInputRequest {
		public String message;
		public String command;
	}
	// ---------------------------------------------------------------------
	private static class GameInputResponse {
		public List<String> data = new ArrayList<>();
	}
	// ---------------------------------------------------------------------
	private void doMove(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		GameInputRequest body = gson.fromJson(req.getReader(), GameInputRequest.class);
		HttpSession session = req.getSession();
		Adv32 game = (Adv32) session.getAttribute("adventureGame");
		Adv32.CrankOutput result = null;
		GameInputResponse answer = null;
		answer = new GameInputResponse();
		try {
			if(game == null || "new".equals(body.command)) {
				game = new Adv32();
				session.setAttribute("adventureGame", game);
				result = game.startGame();
			} else {
				result = game.nextMove(body.message);
			}
			for(String s : result.getLines()) {
				answer.data.add(s);
			}
		} catch (EmergencyExit e) {
			if(e.isError()) {
				answer.data.add(e.getMessage());
			} else {
				answer.data.add("Game Over");
			}
			session.setAttribute("adventureGame", null);
		}
		gson.toJson(answer, resp.getWriter());
	}


	public static void sendRedirect(HttpServletResponse resp, String location) {
		try {
			resp.sendRedirect(location);
		} catch (IOException e) {
			log.error("Unable to redirect to: "+location, e);
		}
	}

	public static void sendError(HttpServletResponse resp, int code) {
		try {
			resp.sendError(code);
		} catch (IOException e) {
			log.error("Unable respond with Http error code: "+code, e);
		}
	}

	public static void sendError(HttpServletResponse resp, int code, String msg) {
		try {
			resp.sendError(code, msg);
		} catch (IOException e) {
			log.error("Unable respond with Http error code: "+code+ ", and msg:"+msg, e);
		}
	}
}

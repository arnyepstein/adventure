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

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	//	public final static String AUTHENTICATION_CONTEXT_KEY = "AUTHENTICATION_CONTEXT";
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
		throws IOException, ServletException
	{
//		String uri = req.getRequestURI();
//		String contextPath = req.getContextPath();
//		String info = uri.substring(contextPath.length()+1);
//		if("".equals(info)) {
//			resp.sendRedirect(contextPath + "/index.xhtml");
//			return;
//		} else if("echo".equals(info)) {
//			doEcho(req, resp);
//		} else {
//			resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
//		}
		super.service(req, resp);

	}

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String uri = req.getRequestURI();
		String contextPath = req.getContextPath();
		String info = uri.substring(contextPath.length()+1);
		if("".equals(info)) {
			resp.sendRedirect(contextPath + "/index.xhtml");
		} else {
			resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
		}

	}

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String uri = req.getRequestURI();
		String contextPath = req.getContextPath();
		String info = uri.substring(contextPath.length()+1);
		if("api/echo".equals(info)) {
			doMove(req, resp);
		} else {
			resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	private void doMove(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		EchoRequest body = gson.fromJson(req.getReader(), EchoRequest.class);
		HttpSession session = req.getSession();
		Adv32 game = (Adv32) session.getAttribute("adventureGame");
		Adv32.CrankOutput result = null;
		EchoResponse answer = null;
		answer = new EchoResponse();
		try {
			if(game == null || "new".equals(body.command)) {
				game = new Adv32();
				session.setAttribute("adventureGame", game);
				result = game.startGame(null);
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

	private void doEcho(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		EchoRequest body = gson.fromJson(req.getReader(), EchoRequest.class);
		EchoResponse answer = new EchoResponse();
		answer.data.add("Request was:");
		answer.data.add(body.message);
		gson.toJson(answer, resp.getWriter());

	}

	private static class EchoRequest {
		public String message;
		public String command;
	}
	private static class EchoResponse {
		public List<String> data = new ArrayList<>();
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

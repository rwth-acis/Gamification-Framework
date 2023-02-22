package i5.las2peer.services.gamificationBotWrapperService;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.color.ColorSpace;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.security.NoSuchAlgorithmException;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.imageio.ImageIO;
import javax.swing.plaf.ColorUIResource;
import javax.ws.rs.Consumes;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;

import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import i5.las2peer.logging.L2pLogger;
import i5.las2peer.restMapper.RESTService;
import i5.las2peer.restMapper.annotations.ServicePath;
import i5.las2peer.security.BotAgent;
import i5.las2peer.services.gamificationBotWrapperService.database.ProfileDAO;
import i5.las2peer.tools.CryptoException;
import i5.las2peer.api.Context;
import i5.las2peer.api.logging.MonitoringEvent;
import i5.las2peer.api.ManualDeployment;
import i5.las2peer.api.execution.InternalServiceException;
import i5.las2peer.api.execution.ServiceAccessDeniedException;
import i5.las2peer.api.execution.ServiceInvocationFailedException;
import i5.las2peer.api.execution.ServiceMethodNotFoundException;
import i5.las2peer.api.execution.ServiceNotAuthorizedException;
import i5.las2peer.api.execution.ServiceNotAvailableException;
import i5.las2peer.api.execution.ServiceNotFoundException;
import i5.las2peer.api.security.Agent;
import i5.las2peer.api.security.AgentAccessDeniedException;
import i5.las2peer.api.security.AgentAlreadyExistsException;
import i5.las2peer.api.security.AgentLockedException;
import i5.las2peer.api.security.AgentOperationFailedException;
import i5.las2peer.api.security.AnonymousAgent;
import i5.las2peer.connectors.webConnector.client.ClientResponse;
import i5.las2peer.connectors.webConnector.client.MiniClient;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import io.swagger.annotations.Contact;
import io.swagger.annotations.Info;
import io.swagger.annotations.License;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.util.Json;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import i5.las2peer.services.gamification.commons.database.DatabaseManager;

/**
 * Member Service
 * 
 * This is Gamification Member service to fetch the data about members It uses
 * the LAS2peer Web-Connector for RESTful access to it.
 * 
 * Note: If you plan on using Swagger you should adapt the information below in
 * the ApiInfo annotation to suit your project. If you do not intend to provide
 * a Swagger documentation of your service API, the entire ApiInfo annotation
 * should be removed.
 * 
 */
@Api(value = "/members", authorizations = { @Authorization(value = "members_auth", scopes = {
		// @AuthorizationScope(scope = "write:members", description = "modify games in
		// your game"),
		@AuthorizationScope(scope = "read:members", description = "Get data about members") }) }, tags = "members")
@SwaggerDefinition(info = @Info(title = "Members Service", version = "0.1", description = "Member Service for Gamification Framework", termsOfService = "http://your-terms-of-service-url.com", contact = @Contact(name = "Muhammad Abduh Arifin", url = "dbis.rwth-aachen.de", email = "arifin@dbis.rwth-aachen.de"), license = @License(name = "your software license name", url = "http://your-software-license-url.com")))
@ManualDeployment
@ServicePath("/gamification/bots")
public class GamificationBotWrapperService extends RESTService {

	// instantiate the logger class
	private final L2pLogger logger = L2pLogger.getInstance(GamificationBotWrapperService.class.getName());
	/*
	 * Database configuration
	 */
	private String jdbcDriverClassName;
	private String jdbcLogin;
	private String jdbcPass;
	private String jdbcUrl;
	private String jdbcSchema;
	private DatabaseManager dbm;

	private ProfileDAO profileAccess;
	private static HashMap<String, Boolean> userContext = new HashMap<String, Boolean>();

	private static HashMap<String, LrsBotWorker> botWorkers = new HashMap<String, LrsBotWorker>();
	// this header is not known to javax.ws.rs.core.HttpHeaders
	public static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";
	public static final String HEADER_CONTENT_TYPE = "Content-Type";
	// non HTTP standard headers
	public static final String HEADER_OWNERID = "ownerid";
	public static final String HEADER_CONTENT_DESCRIPTION = "Content-Description";

	public GamificationBotWrapperService() {
		setFieldValues();
		System.out
				.println(jdbcDriverClassName + ", " + jdbcLogin + ", " + jdbcPass + ", " + jdbcUrl + ", " + jdbcSchema);

		dbm = DatabaseManager.getInstance(jdbcDriverClassName, jdbcLogin, jdbcPass, jdbcUrl, jdbcSchema);
		this.profileAccess = new ProfileDAO();
	}

	/*
	 * @Override
	 * public void run() {
	 * try {
	 * System.out.println("thread baby");
	 * Thread.sleep(60000);
	 * monitorBotWorkers();
	 * } catch (Exception e) {
	 * e.printStackTrace();
	 * }
	 * }
	 */
	/**
	 * method for workerMonitorThread, kill worker as soon they expire
	 */
	/*
	 * private void monitorBotWorkers() {
	 * List<String> workerCopy = new ArrayList<>(botWorkers);
	 * for (String lrsWorker : workerCopy ) {
	 * if (lrsWorker!= null) {
	 * //if (lrsWorker.expired()) {
	 * // workers.remove(lrsWorker);
	 * //}
	 * System.out.println("bot is here "+lrsWorker );
	 * }
	 * }
	 * }
	 */

	/**
	 * Function to return http unauthorized message
	 * 
	 * @return HTTP Response unauthorized
	 */
	private Response unauthorizedMessage() {
		JSONObject objResponse = new JSONObject();
		objResponse.put("message", "You are not authorized");
		Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
		return Response.status(HttpURLConnection.HTTP_UNAUTHORIZED).entity(objResponse.toString())
				.type(MediaType.APPLICATION_JSON).build();

	}

	/**
	 * Get a level data with specific ID from database
	 * 
	 * @return HTTP Response Returned as JSON object
	 * @param body body
	 */
	@POST
	@Path("/init")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Found a level"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal Error"),
			@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized") })
	@ApiOperation(value = "getlevelWithNum", notes = "Get level details with specific level number")
	public Response init(String body) {
		JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
		JSONObject jsonBody = new JSONObject();
		try {
			System.out.println(body);
			jsonBody = (JSONObject) parser.parse(body);
			String botName = jsonBody.get("botName").toString();
			String game = jsonBody.get("game").toString();
			String lrsToken = jsonBody.get("lrsToken").toString();
			String streakReminder = "1";
			String sbmURL = jsonBody.get("sbmURL").toString();
			String gameURL = jsonBody.get("gameURL").toString();
			String streakMessage = jsonBody.get("streakMessage").toString();
			if (jsonBody.containsKey("streakReminder")) {
				streakReminder = jsonBody.get("streakReminder").toString();
			}
			if (true || !botWorkers.containsKey(botName)) {
				BotAgent restarterBot = null;
				try {
					restarterBot = BotAgent.createBotAgent("actingAgent");
				} catch (AgentOperationFailedException | CryptoException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
					System.out.println("e2");
				}
				try {
					restarterBot.unlock("actingAgent");
					System.out.println("e4");
					restarterBot.setLoginName(botName);
					System.out.println("e3");
					Context.getCurrent().storeAgent(restarterBot);
				} catch (AgentAccessDeniedException | AgentAlreadyExistsException | AgentOperationFailedException
						| AgentLockedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					System.out.println("e1");
				}
				// HashSet<String> actionVerbs = new HashSet<>();
				HashMap<String, JSONArray> actionVerbs = new HashMap<String, JSONArray>();
				HashMap<String, JSONObject> achievements = new HashMap<String, JSONObject>();
				try {
					System.out.println("e6");
					MiniClient client = new MiniClient();

					client.setConnectorEndpoint(gameURL +
							"/gamification/actions/" + game);

					HashMap<String, String> headers = new HashMap<String, String>();
					System.out.println("user");
					client.setLogin(restarterBot.getLoginName(), restarterBot.getPassphrase());
					ClientResponse result = client.sendRequest("GET", "",
							"", MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON, headers);
					System.out.println(result.getResponse());
					JSONObject answer = (JSONObject) parser.parse(result.getResponse());
					// https://tech4comp.de/xapi/verb/compared_words
					for (Object o : (JSONArray) answer.get("rows")) {
						JSONObject jsonO = (JSONObject) o;
						if (jsonO.get("actionType") != null) {
							if (jsonO.get("actionType").toString().equals("LRS")) {
								if (actionVerbs.containsKey(jsonO.get("name").toString())) {
									JSONArray arr = actionVerbs.get(jsonO.get("name").toString());
									arr.add(jsonO);
									actionVerbs.put(jsonO.get("name").toString(), arr);
								} else {
									JSONArray arr = new JSONArray();
									arr.add(jsonO);
									actionVerbs.put(jsonO.get("name").toString(), arr);
								}

							}
						}

					}
					client.setConnectorEndpoint(gameURL +
							"/gamification/achievements/" + game);
					headers = new HashMap<String, String>();
					result = client.sendRequest("GET", "",
							"", MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON, headers);
					System.out.println(result.getResponse());
					answer = (JSONObject) parser.parse(result.getResponse());
					// https://tech4comp.de/xapi/verb/compared_words

					for (Object o : (JSONArray) answer.get("rows")) {
						JSONObject jsonO = (JSONObject) o;
						achievements.put(jsonO.get("id").toString(), jsonO);
					}
				} catch (Exception e) {

					// TODO Auto-generated catch block
					e.printStackTrace();
					System.out.println("e7");
				}
				LrsBotWorker random = new LrsBotWorker(game, botName, restarterBot, lrsToken, actionVerbs, achievements,
						streakReminder, sbmURL, gameURL, streakMessage, this.dbm);
				Thread t = new Thread(random);
				botWorkers.put(botName, random);
				t.start();

			}

		} catch (ParseException e) {
			e.printStackTrace();
		}

		// Request log
		return Response.status(HttpURLConnection.HTTP_OK).entity("Bot wrapper is online")
				.type(MediaType.APPLICATION_JSON).build();
	}

	/**
	 * Get a level data with specific ID from database
	 * 
	 * @return HTTP Response Returned as JSON object
	 * @param body body
	 */
	@POST
	@Path("/init/player")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Found a level"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal Error"),
			@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized") })
	@ApiOperation(value = "getlevelWithNum", notes = "Get level details with specific level number")
	public Response addPlayer(String body) {
		System.out.println(body);
		JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
		JSONObject jsonBody = new JSONObject();
		try {

			jsonBody = (JSONObject) parser.parse(body);
			String user = jsonBody.get("email").toString();
			String botName = jsonBody.get("botName").toString();
			for (String b : botWorkers.keySet()) {
				System.out.println(b);
			}
			if (!botWorkers.containsKey(botName)) {
				JSONObject response = new JSONObject();
				response.put("text", "Bot Not Initialised correctly, please contact support!!!");
				return Response.status(HttpURLConnection.HTTP_OK).entity(response)
						.type(MediaType.APPLICATION_JSON).build();
			} else {
				botWorkers.get(botName).addMember(encryptThisString(user));
				botWorkers.get(botName).addUsers(encryptThisString(user), jsonBody.get("channel").toString(), dbm);
			}

		} catch (ParseException e) {
			e.printStackTrace();
		}

		// Request log
		return Response.status(HttpURLConnection.HTTP_OK).entity("Bot wrapper is online")
				.type(MediaType.APPLICATION_JSON).build();
	}

	/**
	 * Get a level data with specific ID from database
	 * 
	 * @return HTTP Response Returned as JSON object
	 * @param body body
	 */
	// idea, player can ask for profile, and in path, if not yet added, ask player
	// if they want to be added to game
	@POST
	@Path("player/getProfile")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Found a level"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal Error"),
			@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized") })
	@ApiOperation(value = "getlevelWithNum", notes = "Get level details with specific level number")
	public Response getProfile(String body) {
		System.out.println(body);
		JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
		JSONObject jsonBody = new JSONObject();
		try {

			jsonBody = (JSONObject) parser.parse(body);
			String user = jsonBody.get("email").toString();
			String botName = jsonBody.get("botName").toString();

			if (!botWorkers.containsKey(botName)) {
				JSONObject response = new JSONObject();
				response.put("text", "Bot Not Initialised correctly, please contact support!!!");
				return Response.status(HttpURLConnection.HTTP_OK).entity(response)
						.type(MediaType.APPLICATION_JSON).build();
			} else {
				if (!this.botWorkers.get(botName).isRegistered(user)) {
					System.out.println("adding player");
					addPlayer(body);
				}
				BotAgent restarterBot = this.botWorkers.get(botName).getBotAgent();
				MiniClient client = new MiniClient();

				client.setConnectorEndpoint(this.botWorkers.get(botName).getGameURL() +
						"/gamification/visualization/status/"
						+ botWorkers.get(botName).getGame() + "/" + encryptThisString(user));

				HashMap<String, String> headers = new HashMap<String, String>();
				System.out.println("user");
				try {
					client.setLogin(restarterBot.getLoginName(), restarterBot.getPassphrase());
					ClientResponse result = client.sendRequest("GET", "",
							"", MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON, headers);
					System.out.println(result.getResponse());
					JSONObject answer = (JSONObject) parser.parse(result.getResponse());
					JSONObject response = new JSONObject();
					Connection conn = null;
					conn = dbm.getConnection();
					JSONObject profileInfo = this.profileAccess.getProfileInfo(conn,
							botWorkers.get(botName).getGame(),
							encryptThisString(user));
					answer.put("nickname", profileInfo.get("nickname").toString());
					answer.put("badgeId", profileInfo.get("badge_id").toString());
					answer.put("game", botWorkers.get(botName).getGame());
					answer.put("user", user);
					if (conn != null) {
						conn.close();
					}
					String base64 = pic(answer);
					response.put("text", "User Profile:");
					response.put("fileBody", base64);
					response.put("fileName", "User Profile");
					response.put("fileType", "png");

					return Response.status(HttpURLConnection.HTTP_OK).entity(response)
							.type(MediaType.APPLICATION_JSON).build();
				} catch (Exception e) {
					e.printStackTrace();
				}

			}

		} catch (ParseException e) {
			e.printStackTrace();
		}

		// Request log
		return Response.status(HttpURLConnection.HTTP_OK).entity("Bot wrapper is online")
				.type(MediaType.APPLICATION_JSON).build();
	}

	/**
	 * Get a life
	 * 
	 * @return HTTP Response Returned as JSON object
	 * @param body body
	 */
	@POST
	@Path("/player/badge")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Found a level"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal Error"),
			@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized") })
	@ApiOperation(value = "setBadgeOfProfileCard", notes = "Get level details with specific level number")
	public Response setBadgeDB(String body) {
		System.out.println(body);
		JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
		JSONObject jsonBody = new JSONObject();
		try {
			jsonBody = (JSONObject) parser.parse(body);
			String game = jsonBody.get("game").toString();
			String badgeId = jsonBody.get("badgeId").toString();
			String member_id = jsonBody.get("member_id").toString();

			Connection conn = null;

			conn = dbm.getConnection();
			boolean result = this.profileAccess.setProfileBadge(conn, game, member_id, badgeId);
			if (conn != null) {
				conn.close();
			}
			if (result) {
				return Response.status(HttpURLConnection.HTTP_OK).entity("good")
						.type(MediaType.APPLICATION_JSON).build();
			}
			return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity("bad")
					.type(MediaType.APPLICATION_JSON).build();
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity("?")
					.type(MediaType.APPLICATION_JSON).build();
		}
	}

	/**
	 * Get a level data with specific ID from database
	 * 
	 * @return HTTP Response Returned as JSON object
	 * @param body body
	 */
	@POST
	@Path("/player/achievementProgress")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Found a level"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal Error") })
	@ApiOperation(value = "getlevelWithNum", notes = "Get level details with specific level number")
	public Response achievementProgress(String body) {
		System.out.println(body);
		JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
		JSONObject jsonBody = new JSONObject();
		try {

			jsonBody = (JSONObject) parser.parse(body);
			String user = jsonBody.get("email").toString();
			String botName = jsonBody.get("botName").toString();
			for (String b : botWorkers.keySet()) {
				System.out.println(b);
			}
			if (!botWorkers.containsKey(botName)) {
				JSONObject response = new JSONObject();
				response.put("text", "Bot Not Initialised correctly, please contact support!!!");
				return Response.status(HttpURLConnection.HTTP_OK).entity(response)
						.type(MediaType.APPLICATION_JSON).build();
			} else {
				if (!botWorkers.get(botName).getUsers().keySet().contains(encryptThisString(user))) {
					addPlayer(body);
				}
				JSONArray achievements = new JSONArray();
				try {
					Serializable result = Context.get().invokeInternally(
							"i5.las2peer.services.gamificationVisualizationService.GamificationVisualizationService",
							"getQuestDetailAllRMI", botWorkers.get(botName).getGame(), encryptThisString(user));
					System.out.println(result.toString());
					achievements = (JSONArray) parser.parse(result.toString());
					String message = "";

					// now also fetch the achievements from streaks
					result = Context.get().invokeInternally(
							"i5.las2peer.services.gamificationVisualizationService.GamificationVisualizationService",
							"getStreakDetailAllRMI", botWorkers.get(botName).getGame(), encryptThisString(user));
					achievements.add(parser.parse(result.toString()));
					ArrayList<String> base64 = achievementsPng(achievements);
					JSONObject response = jsonBody;
					try {
						response.remove("closeContext");
					} catch (Exception e) {
					}

					JSONArray multiFiles = new JSONArray();
					int index = 1;
					for (String b64 : base64) {
						JSONObject jsonO = new JSONObject();
						jsonO.put("text", "");
						jsonO.put("fileBody", b64);
						jsonO.put("fileName", "Achievements " + index);
						index++;
						jsonO.put("fileType", "png");
						multiFiles.add(jsonO);
					}
					if (multiFiles.size() > 0) {
						response.put("multiFiles", multiFiles);
					} else {
						response.put("text", jsonBody.get("errorMessage").toString());
					}
					return Response.status(HttpURLConnection.HTTP_OK).entity(response)
							.type(MediaType.APPLICATION_JSON).build();
				} catch (Exception e1) {
					e1.printStackTrace();
				}

			}

		} catch (ParseException e) {
			e.printStackTrace();
		}

		// Request log
		return Response.status(HttpURLConnection.HTTP_OK).entity("Bot wrapper is online")
				.type(MediaType.APPLICATION_JSON).build();
	}

	// pls dont judge dont code here, its disgusting but works :,(
	public ArrayList<String> achievementsPng(JSONArray achievements) {
		// test if streaks always present or not
		System.out.println(achievements);
		LocalDateTime now = LocalDateTime.now();
		JSONObject streaks = (JSONObject) achievements.get(achievements.size() - 1);
		for (int i = 0; i < ((JSONArray) streaks.get("streaks")).size(); i++) {
			JSONObject streak = (JSONObject) ((JSONArray) streaks.get("streaks")).get(i);
			String times = streak.get("currentStreakLevel").toString();
			String highestStreakLevel = streak.get("highestStreakLevel").toString();
			String maxTimes = streak.get("streakLevel").toString();
			JSONObject action = (JSONObject) streak.get("action");
			JSONObject achievement = (JSONObject) streak.get("achievement");
			String desc = achievement.get("description").toString();
			String key = achievement.get("id").toString();
			String points = achievement.get("point_value").toString();
			String lockedDate = streak.get("lockedDate").toString();
			String dueDate = streak.get("dueDate").toString();
			
			String status = "REVEALED";
			String badge = "";
			if (achievement.get("badge_id") != null) {
				badge = achievement.get("badge_id").toString();
			}
			if (ChronoUnit.MINUTES.between(now,
					LocalDateTime.parse(streak.get("dueDate").toString())) <= 0) {
				times = "0";
			}

			if (streak.get("unlocked").toString().equals("t")) {
				status = "COMPLETED";
				times = maxTimes;
			}

			streak.put("times", times);
			streak.put("maxTimes", maxTimes);
			JSONArray actionArray = new JSONArray();
			action.put("description", desc);
			action.put("times", times);
			action.put("maxTimes", maxTimes);
			action.put("points", points);
			action.put("name", "streak");
			action.put("badge", badge);
			action.put("status", status);
			action.put("lockedDate", lockedDate);
			action.put("dueDate", dueDate);
			if(lockedDate.equals(dueDate)){
				action.remove("lockedDate");
				}
			action.put("name", achievement.get("name").toString());
			actionArray.add(action);
			streak.put("actionArray", actionArray);
			achievements.add(streak);
		}
		ArrayList<Integer> completed = new ArrayList<Integer>();
		ArrayList<Integer> revealed = new ArrayList<Integer>();
		try {
			String filePath = new File("").getAbsolutePath();
			int i = 1;
			for (int k = 0; k < achievements.size(); k++) {
				System.out.println(achievements.get(k));
				if (((JSONObject) achievements.get(k)).containsKey("streaks")) {
					continue;
				}
				JSONObject quest = (JSONObject) achievements.get(k);
				String description = ((JSONObject) ((JSONArray) quest.get("actionArray")).get(0)).get("description")
						.toString();
				String name = "NO NAME";// jsonO.get("name").toString();

				String number = "";// jsonO.get("number").toString();
				String maxNumber = "";// jsonO.get("maxNum").toString();
				String points = "";// jsonO.get("points").toString();
				String badge = "";// jsonO.get("badge").toString();
				String status = "";
				for (Object o : (JSONArray) (quest.get("actionArray"))) {
					JSONObject jsonO = (JSONObject) o;
					name = jsonO.get("name").toString();
					number = jsonO.get("times").toString();
					maxNumber = jsonO.get("maxTimes").toString();
					points = jsonO.get("points").toString();
					status = jsonO.get("status").toString();
					if (jsonO.get("badge") != null) {
						badge = jsonO.get("badge").toString();
					}
				}
				if (status.equals("HIDDEN")) {
					continue;
				} else if (status.equals("REVEALED")) {
					revealed.add(i);
				} else {
					completed.add(i);
				}
				System.out.println((name + description).length());
				int badgeOffset = 0;
				if (!badge.equals("")) {
					badgeOffset = 30;
				}
				// 60 text limit
				int descOffset = 0;
				String lockedDate = "";
				String dueDate = "";
				int dateOffset = 0;
				if (quest.containsKey("lockedDate")) {
					lockedDate = quest.get("lockedDate").toString();
					dueDate = quest.get("dueDate").toString();
					dateOffset = 60;
				}
				ArrayList<String> descArr = new ArrayList<String>();
				if ((name + description).length() > 53) {
					String[] split = description.split("\\s+");
					String n = name;
					String pre = "";
					int sSize = 1;
					for (String s : split) {
						if ((n + pre + s).length() < 60) {
							if (sSize == split.length) {
								pre += s;
							} else {
								pre += s + " ";
							}

						} else {
							n = "";
							descOffset += 30;
							descArr.add(pre);
							pre = s;
						}
						sSize++;
					}
					descArr.add(pre);
				}
				BufferedImage image = new BufferedImage(820, 190 + badgeOffset + descOffset + dateOffset,
						ColorSpace.TYPE_RGB);
				Font font = new Font("Comic Sans MS", Font.BOLD, 20);
				System.out.println("Working Directory = " + image.getWidth() + image.getHeight());
				// Graphics g = image.getGraphics();
				Graphics2D g = (Graphics2D) image.getGraphics();
				g.setFont(font);
				g.setColor(Color.WHITE);
				g.fillRect(0, 0, image.getWidth(), image.getHeight());
				int x = 10;
				int y = 60;
				int w = 700;
				int wOld = w;
				int h = 55;
				double progress = Double.parseDouble(number) / Double.parseDouble(maxNumber);
				if (progress > 1) {
					progress = 1.0;
				}
				double thickness = 3;
				Stroke oldStroke = g.getStroke();
				g.setStroke(new BasicStroke((float) thickness));
				g.setColor(new ColorUIResource(127, 189, 255));
				w *= progress;
				if (w != 0) {
					g.fillRoundRect(image.getWidth() / 2 - wOld / 2 + 1, y + descOffset, x + w - 1, h, 50, 50);
				}
				g.setColor(Color.BLACK);
				g.drawRect(0, 0, image.getWidth(), image.getHeight());
				g.drawRoundRect(image.getWidth() / 2 - wOld / 2, y - 1 + descOffset, x + wOld + 1, h + 1, 50, 50);
				g.setColor(Color.BLACK);
				if (descArr.size() <= 1) {
					g.drawString("Achievement " + name + " (" + description + "):", 10, 25);
				} else {
					g.drawString("Achievement " + name + " (" + descArr.get(0) + "", 10, 25);
					for (int j = 1; j < descArr.size(); j++) {
						if (j + 1 == descArr.size()) {
							g.drawString(descArr.get(j) + "):", 10, 25 + j * 30);
						} else {
							g.drawString(descArr.get(j), 10, 25 + j * 30);
						}

					}
				}
				g.drawString(number + "/" + maxNumber, image.getWidth() / 2, y + h / 2 + 5 + descOffset);
				g.drawString("REWARDS:", 10, h + y + 30 + descOffset);
				g.drawString("- " + points + " Points", 10, h + y + 60 + descOffset);
				if (badgeOffset != 0) {
					g.drawString("- BADGE: " + badge, 10, h + y + 90 + descOffset);
				}
				if (dateOffset > 0) {
					g.drawString("ADDITIONAL INFORMATION:", 10, h + y + 60 + descOffset + badgeOffset + 30);
					g.drawString(
							"- Complete between " + lockedDate.replace("T", " ") + " and " + dueDate.replace("T", " "),
							10, h + y + 90 + descOffset + badgeOffset + 30);

				}
				BufferedImage img = ImageIO.read(new File(
						filePath + "/etc/treasureChest.png"));
				g.drawImage(img.getScaledInstance(h, h, Image.SCALE_DEFAULT), image.getWidth() / 2 + wOld / 2,
						y - 10 + descOffset,
						null);
				File outputfile = new File(filePath + "/etc/img" + i + ".png");
				ImageIO.write(image, "png", outputfile);
				i++;
			}
			ArrayList<String> res = new ArrayList<String>();
			if (i == 1) {
				return res;
			}
			BufferedImage output = ImageIO.read(new File(
					filePath + "/etc/img1.png"));
			int countImgs = 0;
			revealed.addAll(completed);
			for (int index : revealed) {
				BufferedImage i2 = ImageIO.read(new File(
						filePath + "/etc/img" + index + ".png"));
				if (countImgs == 0) {
					output = i2;
				} else {
					output = joinBufferedImage(output, i2);
				}
				countImgs++;
				if (countImgs == 4 || revealed.indexOf(index) + 1 == revealed.size()) {
					countImgs = 0;
					File outputfile = new File(filePath + "/etc/imgAchs.png");
					ImageIO.write(output, "png", outputfile);
					byte[] fileContent = FileUtils.readFileToByteArray(new File(filePath + "/etc/imgAchs.png"));
					String encodedString = Base64.getEncoder().encodeToString(fileContent);
					res.add(encodedString);
				}
			}
			return res;
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<String>();
		}
	}

	public static BufferedImage joinBufferedImage(BufferedImage img1,
			BufferedImage img2) {
		int offset = 1;
		int height = img1.getHeight() + img2.getHeight() + offset;
		int width = Math.max(img1.getWidth(), img2.getWidth()) + offset;
		BufferedImage newImage = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = newImage.createGraphics();
		Color oldColor = g2.getColor();
		g2.setPaint(Color.BLACK);
		g2.fillRect(0, 0, width, height);
		g2.setColor(oldColor);
		g2.drawImage(img1, null, 0, 0);
		g2.drawImage(img2, null, 0, img1.getHeight());
		g2.dispose();
		return newImage;
	}

	/**
	 * Get a level data with specific ID from database
	 * 
	 * @return HTTP Response Returned as JSON object
	 * @param body body
	 */
	@POST
	@Path("/player/actions")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Found a level"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal Error"),
			@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized") })
	@ApiOperation(value = "getlevelWithNum", notes = "Get level details with specific level number")
	public Response actions(String body) {
		System.out.println(body);
		JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
		JSONObject jsonBody = new JSONObject();
		try {

			jsonBody = (JSONObject) parser.parse(body);
			String user = jsonBody.get("email").toString();
			String botName = jsonBody.get("botName").toString();
			for (String b : botWorkers.keySet()) {
				System.out.println(b);
			}
			if (!botWorkers.containsKey(botName)) {
				JSONObject response = new JSONObject();
				response.put("text", "Bot Not Initialised correctly, please contact support!!!");
				return Response.status(HttpURLConnection.HTTP_OK).entity(response)
						.type(MediaType.APPLICATION_JSON).build();
			} else {
				if (!botWorkers.get(botName).getUsers().keySet().contains(encryptThisString(user))) {
					addPlayer(body);
				}
				try {
					String message = "";
					int i = 1;
					Collection<JSONArray> values = botWorkers.get(botName).getActionVerbs().values();
					for (JSONArray arr : values) {
						for (Object o : arr) {
							JSONObject jsonO = (JSONObject) o;
							message += i + ". " + jsonO.get("id").toString() + " ("
									+ jsonO.get("description").toString()
									+ ") \n";
							i++;
						}
					}
					JSONObject response = jsonBody;
					response.put("text", message);
					response.put("actionCount", i);
					return Response.status(HttpURLConnection.HTTP_OK).entity(response)
							.type(MediaType.APPLICATION_JSON).build();
				} catch (Exception e1) {
					e1.printStackTrace();
				}

			}

		} catch (ParseException e) {
			e.printStackTrace();
		}

		// Request log
		return Response.status(HttpURLConnection.HTTP_OK).entity("Bot wrapper is online")
				.type(MediaType.APPLICATION_JSON).build();
	}

	/**
	 * Get a level data with specific ID from database
	 * 
	 * @return HTTP Response Returned as JSON object
	 * @param body body
	 */
	@POST
	@Path("/player/leaderboard")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Found a level"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal Error"),
			@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized") })
	@ApiOperation(value = "getlevelWithNum", notes = "Get level details with specific level number")
	public Response leaderboard(String body) {
		System.out.println(body);
		JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
		JSONObject jsonBody = new JSONObject();
		try {

			jsonBody = (JSONObject) parser.parse(body);
			String user = jsonBody.get("email").toString();
			String botName = jsonBody.get("botName").toString();
			String userMessage = jsonBody.get("msg").toString();
			String pointText = jsonBody.get("points").toString();
			if (!botWorkers.containsKey(botName)) {
				JSONObject response = new JSONObject();
				response.put("text", "Bot Not Initialised correctly, please contact support!!!");
				return Response.status(HttpURLConnection.HTTP_OK).entity(response)
						.type(MediaType.APPLICATION_JSON).build();
			}
			if (!botWorkers.get(botName).getUsers().keySet().contains(encryptThisString(user))) {
				addPlayer(body);
			}
			if (!userContext.containsKey(user) && !userMessage.contains("!")) {
				Response r = actions(body);
				JSONObject response = (JSONObject) r.getEntity();
				response.put("closeContext", false);
				String text = response.get("text").toString();
				int actionCount = Integer.valueOf(response.get("actionCount").toString());
				text += String.valueOf(actionCount + 1) + ". " + pointText +" \n";
				text += String.valueOf(actionCount + 2) + ". " + "Achievements \n";
				text += String.valueOf(actionCount + 3) + ". " + "Badges \n";
				response.put("text",text);
				userContext.put(user, true);
				return Response.status(HttpURLConnection.HTTP_OK).entity(response)
						.type(MediaType.APPLICATION_JSON).build();
			}

			try {
				System.out.println("monkey");
				int i = 1;
				Collection<JSONArray> values = botWorkers.get(botName).getActionVerbs().values();
				JSONObject chosen = null;
				String splitMessage = userMessage;
				if (userMessage.contains("!")) {
					if (userMessage.split("\\s").length > 1) {
						splitMessage = userMessage.split("\\s")[1];
					} else {

					}

					System.out.println(splitMessage);
				}
				for (JSONArray arr : values) {
					for (Object o : arr) {
						JSONObject jsonO = (JSONObject) o;

						if (userMessage.toLowerCase().contains(String.valueOf(i))
								|| jsonO.get("id").toString().toLowerCase().contains(splitMessage.toLowerCase())) {
							System.out.println("found chosen action");
							chosen = jsonO;
							break;
						}
						i++;

					}
					if (chosen != null) {
						break;
					}
				}

				if (chosen == null) {
					String other = "";
					
					if (userMessage.toLowerCase().contains(String.valueOf(i + 1))) {
						other = "point";
					} else if (userMessage.toLowerCase().contains(String.valueOf(i + 2))) {
						other = "achievement";
					} else if (userMessage.toLowerCase().contains(String.valueOf(i + 3))) {
						other = "badge";
					}
					System.out.println(other + i);
					if (!other.equals("")) {

						Serializable result = Context.get().invokeInternally(
								"i5.las2peer.services.gamificationVisualizationService.GamificationVisualizationService",
								"getLocalLeaderboardOverCollectableRMI", botWorkers.get(botName).getGame(),
								encryptThisString(user),
								other);

						System.out.println("fetching leaderboard success");
						JSONObject j = (JSONObject) parser.parse(result.toString());
						JSONArray ranks = (JSONArray) j.get("rows");
						String message = "";
						System.out.println(ranks);
						for (Object o : ranks) {
							JSONObject jsonO = (JSONObject) o;
							System.out.println(jsonO);
							String name = "";
							if (!jsonO.get("nickname").toString().equals("")) {
								name = jsonO.get("nickname").toString();
							} else {
								name = jsonO.get("memberId").toString();
								name = String.format("%." + 10 + "s", name);
							}
							if (encryptThisString(user).equals(jsonO.get("memberId").toString())) {
								message += "*Rank: " + jsonO.get("rank").toString() + " | Count: "
										+ jsonO.get("actioncount").toString() + " | Player: " + name + "* \n";

							} else {
								message += "Rank: " + jsonO.get("rank").toString() + " | Count: "
										+ jsonO.get("actioncount").toString() + " | Player: "
										+ name + " \n";

							}
						}
						JSONObject response = jsonBody;
						response.put("text", message);
						userContext.remove(user);
						return Response.status(HttpURLConnection.HTTP_OK).entity(response)
								.type(MediaType.APPLICATION_JSON).build();

					}
					String error = jsonBody.get("error").toString();
					Response r = actions(body);
					JSONObject response = (JSONObject) r.getEntity();
					response.put("closeContext", false);
					userContext.put(user, true);
					response.put("text", error + "\n" + response.get("text").toString());
					return Response.status(HttpURLConnection.HTTP_OK).entity(response)
							.type(MediaType.APPLICATION_JSON).build();

				}

				Serializable result = Context.get().invokeInternally(
						"i5.las2peer.services.gamificationVisualizationService.GamificationVisualizationService",
						"getLocalLeaderboardOverActionRMI", botWorkers.get(botName).getGame(), encryptThisString(user),
						chosen.get("id").toString());
				System.out.println("fetching leaderboard success");
				JSONObject j = (JSONObject) parser.parse(result.toString());
				JSONArray ranks = (JSONArray) j.get("rows");
				String message = "";
				System.out.println(ranks);
				for (Object o : ranks) {
					JSONObject jsonO = (JSONObject) o;
					System.out.println(jsonO);
					String name = "";
					if (!jsonO.get("nickname").toString().equals("")) {
						name = jsonO.get("nickname").toString();
					} else {
						name = jsonO.get("memberId").toString();
						name = String.format("%." + 10 + "s", name);
					}
					if (encryptThisString(user).equals(jsonO.get("memberId").toString())) {
						message += "*Rank: " + jsonO.get("rank").toString() + " | Count: "
								+ jsonO.get("actioncount").toString() + " | Player: " + name + "* \n";

					} else {
						message += "Rank: " + jsonO.get("rank").toString() + " | Count: "
								+ jsonO.get("actioncount").toString() + " | Player: "
								+ name + " \n";

					}
				}
				JSONObject response = jsonBody;
				response.put("text", message);
				userContext.remove(user);
				return Response.status(HttpURLConnection.HTTP_OK).entity(response)
						.type(MediaType.APPLICATION_JSON).build();

			} catch (Exception e1) {
				userContext.remove(user);
				e1.printStackTrace();
			}

		} catch (ParseException e) {
			e.printStackTrace();
		}

		// Request log
		return Response.status(HttpURLConnection.HTTP_OK).entity("Bot wrapper is online")
				.type(MediaType.APPLICATION_JSON).build();
	}

	/**
	 * Get a level data with specific ID from database
	 * 
	 * @return HTTP Response Returned as JSON object
	 * @param body body
	 */
	@POST
	@Path("/player/setBadge")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Found a level"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal Error"),
			@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized") })
	@ApiOperation(value = "getlevelWithNum", notes = "Get level details with specific level number")
	public Response setBadge(String body) {
		System.out.println(body);
		JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
		JSONObject jsonBody = new JSONObject();
		try {

			jsonBody = (JSONObject) parser.parse(body);
			String user = jsonBody.get("email").toString();
			String botName = jsonBody.get("botName").toString();
			String userMessage = jsonBody.get("msg").toString();
			if (!botWorkers.containsKey(botName)) {
				JSONObject response = new JSONObject();
				response.put("text", "Bot Not Initialised correctly, please contact support!!!");
				return Response.status(HttpURLConnection.HTTP_OK).entity(response)
						.type(MediaType.APPLICATION_JSON).build();
			}
			if (!botWorkers.get(botName).getUsers().keySet().contains(encryptThisString(user))) {
				addPlayer(body);
			}

			if (!userContext.containsKey(user) && (!userMessage.contains("!") || userMessage.split("\\s").length < 2)) {
				Response r = getBadges(body);
				JSONObject jsonR = (JSONObject) r.getEntity();
				System.out.println(jsonR);
				JSONArray multiFiles = (JSONArray) jsonR.get("multiFiles");
				jsonR.remove("multiFiles");
				String bulletBadges = "1. Default (No Badge)\n";
				if (multiFiles != null) {
					for (int i = 0; i < multiFiles.size(); i++) {
						bulletBadges += (i + 2) + ". " + ((JSONObject) multiFiles.get(i)).get("name").toString() + "\n";
					}
				}
				jsonR.put("closeContext", false);

				jsonR.put("text", bulletBadges);
				userContext.put(user, true);
				return Response.status(HttpURLConnection.HTTP_OK).entity(jsonR)
						.type(MediaType.APPLICATION_JSON).build();
			}

			try {
				int i = 2;
				System.out.println("monkey");
				Response r = getBadges(body);
				JSONObject jsonR = (JSONObject) r.getEntity();
				JSONArray multiFiles = (JSONArray) jsonR.get("multiFiles");
				JSONObject chosen = null;
				String splitMessage = userMessage;
				if (userMessage.contains("!")) {
					splitMessage = userMessage.split("\\s")[1];
					System.out.println(splitMessage);
				}
				if (multiFiles != null) {
					for (Object o : multiFiles) {

						JSONObject jsonO = (JSONObject) o;

						if (userMessage.toLowerCase().contains(String.valueOf(i))
								|| jsonO.get("name").toString().toLowerCase().contains(splitMessage.toLowerCase())) {
							chosen = jsonO;
							break;
						}
						i++;

					}
				}

				if (userMessage.contains("1") || userMessage.contains("1.")
						|| "default".contains(splitMessage.toLowerCase())) {
					chosen = (JSONObject) new JSONObject();
					chosen.put("id", "");
					System.out.println("default val chosen");
				}
				if (chosen == null) {
					String error = jsonBody.get("errorMessage").toString();
					Response r2 = actions(body);
					JSONObject response = (JSONObject) r2.getEntity();

					response.put("text", error + "\n" + response.get("text").toString());
					userContext.remove(user);
					jsonBody.put("msg", "");
					return setBadge(jsonBody.toJSONString());
					/*
					 * String badgeId = "";
					 * Connection conn = null;
					 * 
					 * conn = dbm.getConnection();
					 * boolean result = this.profileAccess.setProfileBadge(conn,
					 * botWorkers.get(botName).getGame(),
					 * encryptThisString(user), badgeId);
					 * if (conn != null) {
					 * conn.close();
					 * }
					 */
					// return Response.status(HttpURLConnection.HTTP_OK).entity(response)
					// .type(MediaType.APPLICATION_JSON).build();

				}
				String badgeId = chosen.get("id").toString();
				Connection conn = null;

				conn = dbm.getConnection();
				boolean result = this.profileAccess.setProfileBadge(conn, botWorkers.get(botName).getGame(),
						encryptThisString(user), badgeId);
				if (conn != null) {
					conn.close();
				}

				if (!result) {
					throw new Exception();
				}
				JSONObject response = jsonBody;
				response.put("text", jsonBody.get("successMessage").toString());
				response.put("closeContext", true);
				userContext.remove(user);
				return Response.status(HttpURLConnection.HTTP_OK).entity(response)
						.type(MediaType.APPLICATION_JSON).build();

			} catch (Exception e1) {
				userContext.remove(user);
				e1.printStackTrace();
				JSONObject response = jsonBody;
				response.put("text", jsonBody.get("errorMessage").toString());
				response.put("closeContext", true);
				userContext.remove(user);
				return Response.status(HttpURLConnection.HTTP_OK).entity(response)
						.type(MediaType.APPLICATION_JSON).build();
			}

		} catch (ParseException e) {
			e.printStackTrace();
		}

		// Request log
		return Response.status(HttpURLConnection.HTTP_OK).entity("Bot wrapper is online")
				.type(MediaType.APPLICATION_JSON).build();
	}

	/**
	 * Get a level data with specific ID from database
	 * 
	 * @return HTTP Response Returned as JSON object
	 * @param body body
	 */
	@POST
	@Path("/player/setNickname")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Found a level"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal Error"),
			@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized") })
	@ApiOperation(value = "getlevelWithNum", notes = "Get level details with specific level number")
	public Response setNickname(String body) {
		System.out.println(body);
		JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
		JSONObject jsonBody = new JSONObject();
		try {

			jsonBody = (JSONObject) parser.parse(body);
			String user = jsonBody.get("email").toString();
			String botName = jsonBody.get("botName").toString();
			String userMessage = jsonBody.get("msg").toString();

			if (!botWorkers.containsKey(botName)) {
				JSONObject response = new JSONObject();
				response.put("text", "Bot Not Initialised correctly, please contact support!!!");
				return Response.status(HttpURLConnection.HTTP_OK).entity(response)
						.type(MediaType.APPLICATION_JSON).build();
			}
			if (!botWorkers.get(botName).getUsers().keySet().contains(encryptThisString(user))) {
				addPlayer(body);
			}
			try {
				String ignoreMessage = jsonBody.get("ignoreMessage").toString();
				if (ignoreMessage.equals(userMessage) || ignoreMessage.equals(userMessage + ".")) {
					JSONObject response = jsonBody;
					response.put("closeContext", false);
					return Response.status(HttpURLConnection.HTTP_OK).entity(response)
							.type(MediaType.APPLICATION_JSON).build();
				}
				if (userMessage.length() > 11) {
					JSONObject response = jsonBody;
					response.put("text", jsonBody.get("errorMessage").toString());
					response.put("closeContext", false);
					return Response.status(HttpURLConnection.HTTP_OK).entity(response)
							.type(MediaType.APPLICATION_JSON).build();
				}
				Connection conn = null;

				conn = dbm.getConnection();
				this.profileAccess.setNickname(conn, botWorkers.get(botName).getGame(),
						encryptThisString(user), userMessage);
				if (conn != null) {
					conn.close();
				}
				JSONObject response = jsonBody;
				response.put("text", jsonBody.get("successMessage").toString());
				response.put("closeContext", true);
				return Response.status(HttpURLConnection.HTTP_OK).entity(response)
						.type(MediaType.APPLICATION_JSON).build();

			} catch (Exception e1) {
				e1.printStackTrace();
				JSONObject response = jsonBody;
				response.put("text", jsonBody.get("errorMessage").toString());
				response.put("closeContext", true);
			}

		} catch (ParseException e) {
			e.printStackTrace();
		}

		// Request log
		return Response.status(HttpURLConnection.HTTP_OK).entity("Bot wrapper is online")
				.type(MediaType.APPLICATION_JSON).build();
	}

	/**
	 * Get a level data with specific ID from database
	 * 
	 * @return HTTP Response Returned as JSON object
	 * @param body body
	 */
	@POST
	@Path("/player/badges")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Found a level"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal Error"),
			@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized") })
	@ApiOperation(value = "getlevelWithNum", notes = "Get level details with specific level number")
	public Response getBadges(String body) {
		System.out.println(body);
		JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
		JSONObject jsonBody = new JSONObject();
		try {

			jsonBody = (JSONObject) parser.parse(body);
			String user = jsonBody.get("email").toString();
			String botName = jsonBody.get("botName").toString();
			for (String b : botWorkers.keySet()) {
				System.out.println(b);
			}
			if (!botWorkers.containsKey(botName)) {
				JSONObject response = new JSONObject();
				response.put("text", "Bot Not Initialised correctly, please contact support!!!");
				return Response.status(HttpURLConnection.HTTP_OK).entity(response)
						.type(MediaType.APPLICATION_JSON).build();
			} else {
				if (!botWorkers.get(botName).getUsers().keySet().contains(encryptThisString(user))) {
					addPlayer(body);
				}
				try {
					Serializable result = Context.get().invokeInternally(
							"i5.las2peer.services.gamificationVisualizationService.GamificationVisualizationService",
							"getBadgesOfMemberRMI", botWorkers.get(botName).getGame(), encryptThisString(user));
					System.out.println(result.toString());
					JSONArray j = (JSONArray) parser.parse(result.toString());
					String message = "";
					String base64 = "";
					JSONArray multiFiles = new JSONArray();
					for (Object o : j) {
						JSONObject jsonO = (JSONObject) o;
						if (jsonO.containsKey("base64") && jsonO.get("base64") != null) {
							jsonO.put("badgeId", jsonO.get("id").toString());
							jsonO.put("text", jsonO.get("name").toString() + "\n");
							jsonO.put("fileBody", jsonO.get("base64").toString());
							jsonO.put("fileName", "badge");
							jsonO.put("fileType", "png");
							multiFiles.add(jsonO);
						}

					}
					JSONObject response = new JSONObject();
					if (multiFiles.size() > 0) {
						response.put("multiFiles", multiFiles);
					} else {
						response.put("text", jsonBody.get("errorMessage").toString());
					}
					response.put("channel", jsonBody.get("channel").toString());
					return Response.status(HttpURLConnection.HTTP_OK).entity(response)
							.type(MediaType.APPLICATION_JSON).build();
				} catch (Exception e1) {
					e1.printStackTrace();
				}

			}

		} catch (ParseException e) {
			e.printStackTrace();
		}

		// Request log
		return Response.status(HttpURLConnection.HTTP_OK).entity("Bot wrapper is online")
				.type(MediaType.APPLICATION_JSON).build();
	}

	public String pic(JSONObject json) {
		try {
			System.out.println("pcitute");
			System.out.println(json);
			String user = String.format("%." + 10 + "s", encryptThisString(json.get("user").toString()));
			String nickname = json.get("nickname").toString();
			if (!nickname.equals("")) {
				user = nickname;
			}
			String filePath = new File("").getAbsolutePath();
			System.out.println(filePath);
			BufferedImage image = null;
			for (double i = 0; i <= 101.0; i += 5.0) {
				if (Integer.valueOf(json.get("progress").toString()) <= i) {
					System.out.println("/etc/profileCard" + ((int) (i * 10.0)) + ".drawio.png");
					image = ImageIO
							.read(new File(filePath + "/etc/profileCard" + ((int) (i * 10.0)) + ".drawio.png"));
					break;
				}
			}
			Font font = new Font("Comic Sans MS", Font.BOLD, 14);
			Graphics g = image.getGraphics();
			g.setFont(font);
			g.setColor(Color.BLACK);
			// g.drawString("Player", 4, 20);
			g.drawString(user, (int) (image.getWidth() * 0.44), 44);
			g.drawString(json.get("memberLevel").toString(), (int) (image.getWidth() * 0.44), 63);
			g.drawString(json.get("memberPoint").toString(), (int) (image.getWidth() * 0.14), 240);
			if (json.get("progress").toString().equals("100")) {
				g.drawString(json.get("progress").toString() + "%", (int) (image.getWidth() * 0.47), 90);
			} else {
				int diff = Integer.valueOf(json.get("nextLevelPoint").toString())
						- Integer.valueOf(json.get("memberPoint").toString());
				g.drawString(json.get("progress").toString() + "%",
						(int) (image.getWidth() * 0.47), 90);
						if(diff== 1){
							g.drawString("(" + diff + " point to next Lvl)",
							(int) (image.getWidth() * 0.34), 115);
						} else {
							g.drawString("(" + diff + " points to next Lvl)",
							(int) (image.getWidth() * 0.34), 115);
						}
				
			}

			g.drawString(json.get("unlockedAchievements").toString() + "/" + json.get("totalAchievements").toString(),
					(int) (image.getWidth() * 0.47), 240);
			g.drawString(json.get("unlockedBadges").toString() + "/" + json.get("totalBadges").toString(),
					(int) (image.getWidth() * 0.82), 240);
			File outputfile = new File(filePath + "/etc/img.png");
			ImageIO.write(image, "png", outputfile);
			if (json.containsKey("badgeId") && !json.get("badgeId").toString().equals("")) {
				String filePathBadge = filePath.replace("GamificationGameService", "GamificationBadgeService/files/"
						+ json.get("game").toString() + "/" + json.get("badgeId").toString());

				BufferedImage img = ImageIO.read(new File(
						filePathBadge));
				// have to delete file afterwards
				g.drawImage(img.getScaledInstance(96, 96, Image.SCALE_DEFAULT), image.getWidth() - 96 - 35, 26, null);
				// g.drawImage(img.getScaledInstance(96, 96, Image.SCALE_DEFAULT), 35, 26,
				// null);
				ImageIO.write(image, "png", outputfile);
			}
			// have to delete file afterwards
			byte[] fileContent = FileUtils.readFileToByteArray(new File(filePath + "/etc/img.png"));
			String encodedString = Base64.getEncoder().encodeToString(fileContent);
			return encodedString;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("SOmething went wrong with image uwu");
		}
		return "error";

	}

	public static String encryptThisString(String input) {
		try {
			// getInstance() method is called with algorithm SHA-384
			MessageDigest md = MessageDigest.getInstance("SHA-384");

			// digest() method is called
			// to calculate message digest of the input string
			// returned as array of byte
			byte[] messageDigest = md.digest(input.getBytes());

			// Convert byte array into signum representation
			BigInteger no = new BigInteger(1, messageDigest);

			// Convert message digest into hex value
			String hashtext = no.toString(16);

			// Add preceding 0s to make it 32 bit
			try {
				System.out.println(hashtext.getBytes("UTF-16BE").length * 8);
				while (hashtext.getBytes("UTF-16BE").length * 8 < 1536) {
					hashtext = "0" + hashtext;
				}
			} catch (Exception e) {
				System.out.println(e);
			}

			// return the HashText
			return hashtext;
		}

		// For specifying wrong message digest algorithms
		catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
}

// will need to have a list of active users that has the key of the
// botchannel/name
// to each user a list of timestamps will be kept to know from which point on we
// will need to check new statements
// otherwise the search will take way too long

// for basic chat bot interaction: should i let users define points in sbf or
// pre define them?

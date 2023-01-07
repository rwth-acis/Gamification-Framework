package i5.las2peer.services.gamificationBotWrapperService;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.SQLException;
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
				} catch (Exception e) {

					// TODO Auto-generated catch block
					e.printStackTrace();
					System.out.println("e7");
				}
				LrsBotWorker random = new LrsBotWorker(game, botName, restarterBot, lrsToken, actionVerbs,
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
					String badgeId = this.profileAccess.getProfileBadge(conn, this.botWorkers.get(botName).getGame(),
							encryptThisString(user));
					answer.put("badgeId", badgeId);
					answer.put("game", this.botWorkers.get(botName).getGame());
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
					botWorkers.get(botName).addMember(encryptThisString(user));
					botWorkers.get(botName).addUsers(encryptThisString(user), jsonBody.get("channel").toString(), dbm);
				}
				try {
					Serializable result = Context.get().invokeInternally(
							"i5.las2peer.services.gamificationVisualizationService.GamificationVisualizationService",
							"getQuestDetailAllRMI", botWorkers.get(botName).getGame(), encryptThisString(user));
					System.out.println(result.toString());
					JSONObject j = (JSONObject) parser.parse(result.toString());
					String message = "";
					for (String key : j.keySet()) {
						JSONObject quest = (JSONObject) j.get(key);
						String desc = ((JSONObject) ((JSONArray) quest.get("actionArray")).get(0)).get("description")
								.toString();
						message += "*Achievement " + key + " (" + desc + ")" + " progress:* \n";
						System.out.println(message);
						for (Object o : (JSONArray) ((JSONObject) j.get(key)).get("actionArray")) {
							JSONObject jsonO = (JSONObject) o;
							message += "- Action " + jsonO.get("action").toString() + ":"
									+ jsonO.get("times").toString() + "/" + jsonO.get("maxTimes").toString() + "\n";
							message += "Rewards: \n" + "- " + jsonO.get("points").toString() + " points \n";
							if (jsonO.get("badge") != null) {
								message += "- Badge: " + jsonO.get("badge").toString() + " \n";
							}

						}
					}
					JSONObject response = jsonBody;
					try {
						response.remove("closeContext");
					} catch (Exception e) {
					}
					response.put("text", message);
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
					botWorkers.get(botName).addMember(encryptThisString(user));
					botWorkers.get(botName).addUsers(encryptThisString(user), jsonBody.get("channel").toString(), dbm);
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
			if (!userContext.containsKey(user) && !userMessage.contains("!")) {
				Response r = actions(body);
				JSONObject response = (JSONObject) r.getEntity();
				response.put("closeContext", false);
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
						System.out.println(o);
						JSONObject jsonO = (JSONObject) o;

						if (userMessage.toLowerCase().contains(String.valueOf(i))
								|| jsonO.get("id").toString().toLowerCase().contains(splitMessage.toLowerCase())) {
							chosen = jsonO;
							break;
						}
						i++;

					}
				}
				if (chosen == null) {
					String error = jsonBody.get("error").toString();
					Response r = actions(body);
					JSONObject response = (JSONObject) r.getEntity();

					response.put("text", error + "\n" + response.get("text").toString());
					return Response.status(HttpURLConnection.HTTP_OK).entity(response)
							.type(MediaType.APPLICATION_JSON).build();

				}
				Serializable result = Context.get().invokeInternally(
						"i5.las2peer.services.gamificationVisualizationService.GamificationVisualizationService",
						"getLocalLeaderboardOverActionRMI", botWorkers.get(botName).getGame(), encryptThisString(user),
						chosen.get("id").toString());
				JSONObject j = (JSONObject) parser.parse(result.toString());
				JSONArray ranks = (JSONArray) j.get("rows");
				String message = "";
				for (Object o : ranks) {
					JSONObject jsonO = (JSONObject) o;
					if (encryptThisString(user).equals(jsonO.get("memberId").toString())) {
						message += "*Rank: " + jsonO.get("rank").toString() + " | Count: "
								+ jsonO.get("actioncount").toString() + " | Player: YOU!* \n";

					} else {
						message += "Rank: " + jsonO.get("rank").toString() + " | Count: "
								+ jsonO.get("actioncount").toString() + " | Player: "
								+ jsonO.get("memberId").toString() + " \n";

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
			if (!userContext.containsKey(user) && !userMessage.contains("!")) {
				Response r = getBadges(body);
				JSONObject jsonR = (JSONObject) r.getEntity();
				System.out.println(jsonR);
				JSONArray multiFiles = (JSONArray) jsonR.get("multiFiles");
				jsonR.remove("multiFiles");
				String bulletBadges = "0. Default \n";
				for (int i = 0; i < multiFiles.size(); i++) {
					bulletBadges += (i + 1) + ". " + ((JSONObject) multiFiles.get(i)).get("name").toString() + "\n";
				}
				if (multiFiles.size() > 0) {
					jsonR.put("closeContext", false);

				}
				jsonR.put("text", bulletBadges);
				userContext.put(user, true);
				return Response.status(HttpURLConnection.HTTP_OK).entity(jsonR)
						.type(MediaType.APPLICATION_JSON).build();
			}

			try {
				int i = 1;
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
				for (Object o : multiFiles) {

					JSONObject jsonO = (JSONObject) o;

					if (userMessage.toLowerCase().contains(String.valueOf(i))
							|| jsonO.get("name").toString().toLowerCase().contains(splitMessage.toLowerCase())) {
						chosen = jsonO;
						break;
					}
					i++;

				}
				if (chosen == null) {
					String error = jsonBody.get("error").toString();
					Response r2 = actions(body);
					JSONObject response = (JSONObject) r2.getEntity();

					response.put("text", error + "\n" + response.get("text").toString());
					String badgeId = "";
					Connection conn = null;

					conn = dbm.getConnection();
					boolean result = this.profileAccess.setProfileBadge(conn, botWorkers.get(botName).getGame(),
							encryptThisString(user), badgeId);
					if (conn != null) {
						conn.close();
					}
					return Response.status(HttpURLConnection.HTTP_OK).entity(response)
							.type(MediaType.APPLICATION_JSON).build();

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
					botWorkers.get(botName).addMember(encryptThisString(user));
					botWorkers.get(botName).addUsers(encryptThisString(user), jsonBody.get("channel").toString(), dbm);
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
					response.put("multiFiles", multiFiles);
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
			String filePath = new File("").getAbsolutePath();
			System.out.println(filePath);
			BufferedImage image = null;
			for (double i = 0; i <= 101.0; i += 2.5) {
				if (Integer.valueOf(json.get("progress").toString()) <= i) {
					System.out.println("/etc/profileCard" + ((int) (i * 10.0)) + ".drawio.png");
					image = ImageIO
							.read(new File(filePath + "/etc/profileCard" + ((int) (i * 10.0)) + ".drawio.png"));
					break;
				}
			}
			Font font = new Font("Comic Sans MS", Font.BOLD, 13);
			Graphics g = image.getGraphics();
			g.setFont(font);
			g.setColor(Color.BLACK);
			// g.drawString("Player", 4, 20);
			g.drawString(json.get("memberLevel").toString(), (int) (image.getWidth() * 0.52), 44);
			g.drawString(json.get("memberPoint").toString(), (int) (image.getWidth() * 0.18), 240);
			g.drawString(json.get("progress").toString() + "%", (int) (image.getWidth() * 0.55), 90);
			g.drawString(json.get("unlockedAchievements").toString() + "/" + json.get("totalAchievements").toString(),
					(int) (image.getWidth() * 0.49), 240);
			g.drawString(json.get("unlockedBadges").toString() + "/" + json.get("totalBadges").toString(),
					(int) (image.getWidth() * 0.79), 240);
			File outputfile = new File(filePath + "/etc/img.png");
			ImageIO.write(image, "png", outputfile);
			if (json.containsKey("badgeId") && !json.get("badgeId").toString().equals("")) {
				String filePathBadge = filePath.replace("GamificationGameService", "GamificationBadgeService/files/"
						+ json.get("game").toString() + "/" + json.get("badgeId").toString());

				BufferedImage img = ImageIO.read(new File(
						filePathBadge));
				// have to delete file afterwards
				g.drawImage(img.getScaledInstance(96, 96, Image.SCALE_DEFAULT), 35, 26, null);
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
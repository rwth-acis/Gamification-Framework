package i5.las2peer.services.gamificationBotWrapperService;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
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
import javax.ws.rs.Consumes;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import i5.las2peer.logging.L2pLogger;
import i5.las2peer.restMapper.RESTService;
import i5.las2peer.restMapper.annotations.ServicePath;
import i5.las2peer.security.BotAgent;
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
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

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
	// will need to make so that every bot can choose its own token, and not an
	// environment variable
	private String LRSToken;

	private static LrsBotWorker random;

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
		
		// dbm = DatabaseManager.getInstance(jdbcDriverClassName, jdbcLogin, jdbcPass,
		// jdbcUrl, jdbcSchema);

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
			if (!botWorkers.containsKey(botName)) {
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
				HashSet<String> actionVerbs = new HashSet<>();
				try {
					System.out.println("e6");
					MiniClient client = new MiniClient();
					
					
					client.setConnectorEndpoint(
					"http://host.docker.internal:8080/gamification/actions/" + game);
					
					HashMap<String, String> headers = new HashMap<String, String>();
					System.out.println("user");
					client.setLogin(restarterBot.getLoginName(), restarterBot.getPassphrase());
					ClientResponse result = client.sendRequest("GET", "",
					"",MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON, headers);
					System.out.println(result.getResponse());
					JSONObject answer = (JSONObject) parser.parse(result.getResponse());
					// https://tech4comp.de/xapi/verb/compared_words
					for(Object o : (JSONArray) answer.get("rows")){
						JSONObject jsonO = (JSONObject) o;
						if(jsonO.get("actionType") != null){
							if(jsonO.get("actionType").toString().equals("LRS")){
								actionVerbs.add(jsonO.get("id").toString());
							}
						}
						
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.out.println("e7");
				}
				LrsBotWorker random = new LrsBotWorker(game,botName, restarterBot, lrsToken, actionVerbs);
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
			for(String b : botWorkers.keySet()){
				System.out.println(b);
			}
			if (!botWorkers.containsKey(botName)) {
				JSONObject response = new JSONObject();
				response.put("text","Bot Not Initialised correctly, please contact support!!!");
				return Response.status(HttpURLConnection.HTTP_OK).entity(response)
				.type(MediaType.APPLICATION_JSON).build();
			} else {
				botWorkers.get(botName).addMember(encryptThisString(user));
				botWorkers.get(botName).addUsers(encryptThisString(user));
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
	// idea, player can ask for profile, and in path, if not yet added, ask player if they want to be added to game
	@POST
	@Path("/init/player/getProfile")
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
				response.put("text","Bot Not Initialised correctly, please contact support!!!");
				return Response.status(HttpURLConnection.HTTP_OK).entity(response)
				.type(MediaType.APPLICATION_JSON).build();
			} else {
				if(!this.botWorkers.get(botName).isRegistered(user)){
					System.out.println("adding player");
					addPlayer(body);
				}
				BotAgent restarterBot = this.botWorkers.get(botName).getBotAgent();
				MiniClient client = new MiniClient();
					
					
				client.setConnectorEndpoint(
				"http://host.docker.internal:8080/gamification/visualization/status/" + botWorkers.get(botName).getGame() +"/"+ encryptThisString(user));
				
				HashMap<String, String> headers = new HashMap<String, String>();
				System.out.println("user");
				try{
					client.setLogin(restarterBot.getLoginName(), restarterBot.getPassphrase());
					ClientResponse result = client.sendRequest("GET", "",
					"",MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON, headers);
					System.out.println(result.getResponse());
					JSONObject answer = (JSONObject) parser.parse(result.getResponse());
					JSONObject response = new JSONObject();
					response.put("text",answer.toJSONString());
					return Response.status(HttpURLConnection.HTTP_OK).entity(response)
					.type(MediaType.APPLICATION_JSON).build();
				} catch (Exception e){
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
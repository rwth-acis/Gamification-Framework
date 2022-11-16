package i5.las2peer.services.gamificationBotWrapperService;

import java.io.IOException;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
import i5.las2peer.api.Context;
import i5.las2peer.api.logging.MonitoringEvent;
import i5.las2peer.api.ManualDeployment;
import i5.las2peer.api.security.Agent;
import i5.las2peer.api.security.AnonymousAgent;
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
public class GamificationBotWrapperService extends RESTService{

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
	// will need to make so that every bot can choose its own token, and not an environment variable
	private String LRSToken;

	private static LrsBotWorker random; 

	private static List<String> botWorkers;
	// this header is not known to javax.ws.rs.core.HttpHeaders
	public static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";
	public static final String HEADER_CONTENT_TYPE = "Content-Type";
	// non HTTP standard headers
	public static final String HEADER_OWNERID = "ownerid";
	public static final String HEADER_CONTENT_DESCRIPTION = "Content-Description";

	public GamificationBotWrapperService() {
		setFieldValues();
		System.out.println(jdbcDriverClassName + ", " + jdbcLogin + ", " + jdbcPass + ", " + jdbcUrl + ", " + jdbcSchema);
	//	dbm = DatabaseManager.getInstance(jdbcDriverClassName, jdbcLogin, jdbcPass, jdbcUrl, jdbcSchema);

	}


	/*@Override
	public void run() {
		try {
			System.out.println("thread baby");
			Thread.sleep(60000);
			monitorBotWorkers();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	*/
	/**
	 * method for workerMonitorThread, kill worker as soon they expire
	 */
	/*private void monitorBotWorkers() {
		List<String> workerCopy = new ArrayList<>(botWorkers);
		for (String lrsWorker : workerCopy ) {
			if (lrsWorker!= null) {
				//if (lrsWorker.expired()) {
				//	workers.remove(lrsWorker);
				//}
				System.out.println("bot is here "+lrsWorker );
			}
		}
	}
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
					@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized")})
			@ApiOperation(value = "getlevelWithNum", 
						  notes = "Get level details with specific level number"
						  )
			public Response init(String body)
			{
				System.out.println(LRSToken);
				JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);
				JSONObject jsonBody = new JSONObject();
				try{
					System.out.println(body);
					jsonBody = (JSONObject) parser.parse(body);
					LrsBotWorker random = new LrsBotWorker();
					Thread t = new Thread(random);
					t.start();
				} catch (ParseException e){
					e.printStackTrace();
				}

				// Request log
			return Response.status(HttpURLConnection.HTTP_OK).entity("Bot wrapper is online").type(MediaType.APPLICATION_JSON).build();
			}


}


// will need to have a list of active users that has the key of the botchannel/name
// to each user a list of timestamps will be kept to know from which point on we will need to check new statements
// otherwise the search will take way too long


// for basic chat bot interaction: should i let users define points in sbf or pre define them? 
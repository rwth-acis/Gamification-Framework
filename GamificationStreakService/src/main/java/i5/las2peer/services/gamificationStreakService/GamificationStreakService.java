package i5.las2peer.services.gamificationStreakService;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import i5.las2peer.api.Context;
import i5.las2peer.api.ManualDeployment;
import i5.las2peer.api.logging.MonitoringEvent;
import i5.las2peer.api.security.Agent;
import i5.las2peer.api.security.AnonymousAgent;
import i5.las2peer.api.security.UserAgent;
import i5.las2peer.logging.L2pLogger;
import i5.las2peer.restMapper.RESTService;
import i5.las2peer.restMapper.annotations.ServicePath;
import i5.las2peer.services.gamificationStreakService.database.DatabaseManager;
import i5.las2peer.services.gamificationStreakService.database.StreakDAO;
import i5.las2peer.services.gamificationStreakService.database.StreakModel;
import i5.las2peer.services.gamificationStreakService.database.StreakModel.StreakSatstus;
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

import org.json.JSONArray;
import org.json.JSONObject;


/**
 * Gamification Streak Service
 * 
 * This is Gamification Streak Service to manage streak element in Gamification
 * Framework It uses the LAS2peer Web-Connector for RESTful access to it.
 * 
 * Note: If you plan on using Swagger you should adapt the information below in
 * the ApiInfo annotation to suit your project. If you do not intend to provide
 * a Swagger documentation of your service API, the entire ApiInfo annotation
 * should be removed.
 * 
 */
@Api(value = "/streaks", authorizations = { @Authorization(value = "streaks_auth", scopes = {
		@AuthorizationScope(scope = "write:streaks", description = "modify streaks in your game"),
		@AuthorizationScope(scope = "read:streaks", description = "read your streaks") }) }, tags = "streaks")
@SwaggerDefinition(info = @Info(title = "Gamification Streak Service", version = "0.1", description = "Gamification Streak Service for Gamification Framework", termsOfService = "http://your-terms-of-service-url.com", contact = @Contact(name = "Marc Belsch", url = "dbis.rwth-aachen.de", email = "marc.belsch@rwth-aachen.de"), license = @License(name = "your software license name", url = "http://your-software-license-url.com")))

@ManualDeployment
@ServicePath("gamification/streaks")
public class GamificationStreakService extends RESTService {

	// instantiate the logger class
	private final L2pLogger logger = L2pLogger.getInstance(GamificationStreakService.class.getName());

	private String jdbcDriverClassName;
	private String jdbcUrl;
	private String jdbcSchema;
	private String jdbcLogin;
	private String jdbcPass;
	private DatabaseManager dbm;
	private StreakDAO streakAccess;

	public GamificationStreakService() {
		setFieldValues();
		dbm = new DatabaseManager(jdbcDriverClassName, jdbcLogin, jdbcPass, jdbcUrl, jdbcSchema);
		streakAccess = new StreakDAO();
	}

	/**
	 * Function to return http unauthorized message
	 * 
	 * @return HTTP Response returned as JSON object
	 */
	private Response unauthorizedMessage() {
		JSONObject objResponse = new JSONObject();
		logger.info("You are not authorized >> ");
		objResponse.put("message", "You are not authorized");
		return Response.status(HttpURLConnection.HTTP_UNAUTHORIZED).entity(objResponse.toString())
				.type(MediaType.APPLICATION_JSON).build();
	}

	/**
	 * Post a new streak. It consumes JSON data.
	 * 
	 * @param gameId   gameId
	 * @param contentB content JSON
	 * @return HTTP Response returned as JSON object
	 */
	@POST
	@Path("/{gameId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "{\"status\": 3, \"message\": \"Streak upload success ( (streakId) )\"}"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "{\"status\": 3, \"message\": \"Failed to upload (streakId)\"}"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "{\"status\": 1, \"message\": \"Failed to add the streak. Streak ID already exist!\"}"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "{\"status\": =, \"message\": \"Streak ID cannot be null!\"}"), })
	@ApiOperation(value = "createNewStreak", notes = "A method to store a new streak with details")
	public Response createNewStreak(
			@ApiParam(value = "Game ID to store a new streak", required = true) @PathParam("gameId") String gameId,
			@ApiParam(value = "Streak detail in JSON", required = true) byte[] content) {

		// Request log
		Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_99,
				"POST " + "gamification/streaks/" + gameId, true);
		long randomLong = new Random().nextLong();

		JSONObject objResponse = new JSONObject();

		String name = null;
		Agent agent = Context.getCurrent().getMainAgent();
		if (agent instanceof AnonymousAgent) {
			return unauthorizedMessage();
		} else if (agent instanceof UserAgent) {
			UserAgent userAgent = (UserAgent) Context.getCurrent().getMainAgent();
			name = userAgent.getLoginName();
		} else {
			name = agent.getIdentifier();
		}

		Connection conn = null;
		String streakId = "";
		try {
			conn = dbm.getConnection();

			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_14, "" + randomLong, true);

			if (content == null) {
				objResponse.put("message", "Cannot create streak. No data received");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();

			}

			try {
				if (!streakAccess.isGameIdExist(conn, gameId)) {
					objResponse.put("message", "Cannot create streak. Game not found");
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
							(String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
							.type(MediaType.APPLICATION_JSON).build();
				}
			} catch (SQLException e1) {
				e1.printStackTrace();
				objResponse.put("message",
						"Cannot create streak. Cannot check whether game ID exist or not. Database error. "
								+ e1.getMessage());
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			JSONObject obj = null;
			StreakModel streak = null;
			try {
				obj = new JSONObject(new String(content));
				streak = new StreakModel();
				streak.setStreakId(obj.getString("streakId"));
				streak.setName(obj.getString("name"));
				streak.setDescription(obj.getString("description"));
				streak.setStreakLevel(obj.getInt("streakLevel"));
				streak.setStatus(StreakSatstus.valueOf(obj.getString("status")));
				streak.setActionId(obj.getString("actionId"));
				streak.setPointThreshold(obj.getInt("pointThreshold"));
				streak.setLockedDate(LocalDateTime.parse(obj.getString("lockedDate")));
				streak.setDueDate(LocalDateTime.parse(obj.getString("dueDate")));
				streak.setPeriod(Period.parse(obj.getString("period")));
				streak.setNotificationCheck(obj.getBoolean("notificationCheck"));
				streak.setNotificationMessage(obj.getString("notificationMessage"));
				
				Map<Integer, String> badges = new HashMap<Integer, String>();
				for (Entry<String, Object>  entry: obj.getJSONObject("badges").toMap().entrySet()) {
					badges.put(Integer.valueOf(entry.getKey()), entry.getValue().toString());
				}
				streak.setBadges(badges);
				
				Map<Integer, String> achievements = new HashMap<Integer, String>();
				for (Entry<String, Object>  entry: obj.getJSONObject("achievements").toMap().entrySet()) {
					achievements.put(Integer.valueOf(entry.getKey()), entry.getValue().toString());
				}
				streak.setAchievements(achievements);
				
			} catch (Exception e) {
				e.printStackTrace();
				objResponse.put("message",
						"Cannot create streak. Cannot process input data"
								+ e.getMessage());
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			
			streakId = streak.getStreakId();
			if (streakAccess.isStreakIdExist(conn, gameId, streakId)) {
				objResponse.put("message", "Cannot create streak. Failed to add the streak. Streak ID already exist! ");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}

			streakAccess.addNewStreak(conn, gameId, streak);
			objResponse.put("message", "New streak created " + streakId);
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_15, "" + randomLong, true);
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_24, "" + name, true);
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_25, "" + gameId, true);
			return Response.status(HttpURLConnection.HTTP_CREATED).entity(objResponse.toString())
					.type(MediaType.APPLICATION_JSON).build();

		} catch (SQLException e) {
			e.printStackTrace();
			objResponse.put("message", "Cannot create streak. Database error. " + e.getMessage());
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
			return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toString())
					.type(MediaType.APPLICATION_JSON).build();

		} catch (NullPointerException e) {
			e.printStackTrace();
			objResponse.put("message",
					"Cannot create streak. NullPointerException. Failed to upload " + streakId + ". " + e.getMessage());
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
			return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toString())
					.type(MediaType.APPLICATION_JSON).build();

		}
		// always close connections
		finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				logger.printStackTrace(e);
			}
		}
	}

	/**
	 * Get a streak with specific ID from database
	 * 
	 * @param gameId  gameId
	 * @param streakId streak id
	 * @return HTTP Response returned as JSON object
	 */
	@GET
	@Path("/{gameId}/{streakId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Found a streak"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal Error"),
			@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized") })
	@ApiOperation(value = "getStreakWithId", notes = "Returns streak detail with specific ID", response = StreakModel.class)
	public Response getStreakWithId(@ApiParam(value = "Game ID") @PathParam("gameId") String gameId,
			@ApiParam(value = "Streak ID") @PathParam("streakId") String streakId) {

		// Request log
		Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_99,
				"GET " + "gamification/streaks/" + gameId + "/" + streakId, true);
		long randomLong = new Random().nextLong(); // To be able to match

		StreakModel streak = null;
		Connection conn = null;

		JSONObject objResponse = new JSONObject();
		String name = null;
		Agent agent = Context.getCurrent().getMainAgent();
		if (agent instanceof AnonymousAgent) {
			return unauthorizedMessage();
		} else if (agent instanceof UserAgent) {
			UserAgent userAgent = (UserAgent) Context.getCurrent().getMainAgent();
			name = userAgent.getLoginName();
		} else {
			name = agent.getIdentifier();
		}
		try {
			conn = dbm.getConnection();
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_16, "" + randomLong, true);

			try {
				if (!streakAccess.isGameIdExist(conn, gameId)) {
					objResponse.put("message", "Cannot get streak. Game not found");
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
							(String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
							.type(MediaType.APPLICATION_JSON).build();
				}
			} catch (SQLException e1) {
				e1.printStackTrace();
				objResponse.put("message",
						"Cannot get streak. Cannot check whether game ID exist or not. Database error. "
								+ e1.getMessage());
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			if (!streakAccess.isStreakIdExist(conn, gameId, streakId)) {
				objResponse.put("message", "Cannot get streak. Streak not found");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			streak = streakAccess.getStreakWithId(conn, gameId, streakId);

			if (streak == null) {
				objResponse.put("message", "Cannot get streak. Streak Null, Cannot find streak with " + streakId);
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			ObjectMapper objectMapper = new ObjectMapper();
			// Set pretty printing of json
			objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

			String streakString = objectMapper.writeValueAsString(streak);
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_17, "" + randomLong, true);
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_26, "" + name, true);
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_27, "" + gameId, true);
			return Response.status(HttpURLConnection.HTTP_OK).entity(streakString).type(MediaType.APPLICATION_JSON)
					.build();

		} catch (SQLException e) {
			e.printStackTrace();
			objResponse.put("message", "Cannot get streak. DB Error. " + e.getMessage());
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
			return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toString())
					.type(MediaType.APPLICATION_JSON).build();

		} catch (IOException e) {
			e.printStackTrace();
			objResponse.put("message", "Cannot get streak. Problem in the streak model. " + e.getMessage());
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
			return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toString())
					.type(MediaType.APPLICATION_JSON).build();

		}
		// always close connections
		finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				logger.printStackTrace(e);
			}
		}

	}

	/**
	 * Update a streak.
	 * 
	 * @param gameId   gameId
	 * @param streakId  streakId
	 * @param streak JSON data
	 * @return HTTP Response returned as JSON object
	 */
	@PUT
	@Path("/{gameId}/{streakId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Streak Updated"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Error occured"),
			@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad request"),
			@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized") })
	@ApiOperation(value = "updateStreak", notes = "A method to update a streak with details")
	public Response updateStreak(
			@ApiParam(value = "Game ID to store a new streak", required = true) @PathParam("gameId") String gameId,
			@ApiParam(value = "Streak ID") @PathParam("streakId") String streakId,
			@ApiParam(value = "Streak detail in JSON", required = true) byte[] content) {

		// Request log
		Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_99,
				"PUT " + "gamification/streaks/" + gameId + "/" + streakId, true);
		long randomLong = new Random().nextLong();
		JSONObject objResponse = new JSONObject();
		Connection conn = null;
		
		if (content == null) {
			objResponse.put("message", "Cannot update streak. No data received");

			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
			return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toString())
					.type(MediaType.APPLICATION_JSON).build();

		}

		String name = null;
		Agent agent = Context.getCurrent().getMainAgent();
		if (agent instanceof AnonymousAgent) {
			return unauthorizedMessage();
		} else if (agent instanceof UserAgent) {
			UserAgent userAgent = (UserAgent) Context.getCurrent().getMainAgent();
			name = userAgent.getLoginName();
		} else {
			name = agent.getIdentifier();
		}
		try {
			conn = dbm.getConnection();
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_18, "" + randomLong, true);

			try {
				if (!streakAccess.isGameIdExist(conn, gameId)) {
					logger.info("Game not found >> ");
					objResponse.put("message", "Cannot update streak. Game not found");
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
							(String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
							.type(MediaType.APPLICATION_JSON).build();
				}
			} catch (SQLException e1) {
				e1.printStackTrace();
				logger.info("Cannot check whether game ID exist or not. Database error. >> " + e1.getMessage());
				objResponse.put("message",
						"Cannot check whether game ID exist or not. Database error. " + e1.getMessage());
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			if (streakId == null) {
				logger.info("streak ID cannot be null >> ");
				objResponse.put("message", "Cannot update streak. streak ID cannot be null");

				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}

			if (!streakAccess.isStreakIdExist(conn, gameId, streakId)) {
				objResponse.put("message", "Cannot update streak. Failed to update the streak. Streak ID is not exist!");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			
			JSONObject obj = null;
			StreakModel streak = null;
			try {
				obj = new JSONObject(new String(content));
				streak = new StreakModel();
				streak.setStreakId(streakId);
				streak.setName(obj.getString("name"));
				streak.setDescription(obj.getString("description"));
				streak.setStreakLevel(obj.getInt("streakLevel"));
				streak.setStatus(StreakSatstus.valueOf(obj.getString("status")));
				streak.setActionId(obj.getString("actionId"));
				streak.setPointThreshold(obj.getInt("pointThreshold"));
				streak.setLockedDate(LocalDateTime.parse(obj.getString("lockedDate")));
				streak.setDueDate(LocalDateTime.parse(obj.getString("dueDate")));
				streak.setPeriod(Period.parse(obj.getString("period")));
				streak.setNotificationCheck(obj.getBoolean("notificationCheck"));
				streak.setNotificationMessage(obj.getString("notificationMessage"));
				
				Map<Integer, String> badges = new HashMap<Integer, String>();
				for (Entry<String, Object>  entry: obj.getJSONObject("badges").toMap().entrySet()) {
					badges.put(Integer.valueOf(entry.getKey()), entry.getValue().toString());
				}
				streak.setBadges(badges);
				
				Map<Integer, String> achievements = new HashMap<Integer, String>();
				for (Entry<String, Object>  entry: obj.getJSONObject("achievements").toMap().entrySet()) {
					achievements.put(Integer.valueOf(entry.getKey()), entry.getValue().toString());
				}
				streak.setAchievements(achievements);
				
			} catch (Exception e) {
				e.printStackTrace();
				objResponse.put("message",
						"Cannot create streak. Cannot process input data"
								+ e.getMessage());
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			
			streakAccess.updateStreak(conn, gameId, streak);
			logger.info("Streak Updated ");
			objResponse.put("message", "Streak updated " + streakId);
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_19, "" + randomLong, true);
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_28, "" + name, true);
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_29, "" + gameId, true);
			return Response.status(HttpURLConnection.HTTP_OK).entity(objResponse.toString())
					.type(MediaType.APPLICATION_JSON).build();

		} catch (SQLException e) {
			e.printStackTrace();
			objResponse.put("message", "Cannot update streak. DB Error. " + e.getMessage());
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
			return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toString())
					.type(MediaType.APPLICATION_JSON).build();

		}
		// always close connections
		finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				logger.printStackTrace(e);
			}
		}
	}

	/**
	 * Delete a streak with specified ID
	 * 
	 * @param gameId  gameId
	 * @param streakId streakId
	 * @return HTTP Response returned as JSON object
	 */
	@DELETE
	@Path("/{gameId}/{streakId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "streak Delete Success"),
			@ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "streak not found"),
			@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad Request"), })
	@ApiOperation(value = "deleteStreak", notes = "delete a streak")
	public Response deleteStreak(@PathParam("gameId") String gameId, @PathParam("streakId") String streakId) {

		// Request log
		Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_99,
				"DELETE" + "gamification/streaks/" + gameId + "/" + streakId, true);
		long randomLong = new Random().nextLong(); // To be able to match

		JSONObject objResponse = new JSONObject();
		Connection conn = null;

		String name = null;
		Agent agent = Context.getCurrent().getMainAgent();
		if (agent instanceof AnonymousAgent) {
			return unauthorizedMessage();
		} else if (agent instanceof UserAgent) {
			UserAgent userAgent = (UserAgent) Context.getCurrent().getMainAgent();
			name = userAgent.getLoginName();
		} else {
			name = agent.getIdentifier();
		}
		try {
			conn = dbm.getConnection();
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_20, "" + randomLong, true);

			try {
				if (!streakAccess.isGameIdExist(conn, gameId)) {
					objResponse.put("message", "Cannot delete streak. Game not found");
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
							(String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
							.type(MediaType.APPLICATION_JSON).build();
				}
			} catch (SQLException e1) {
				e1.printStackTrace();
				objResponse.put("message",
						"Cannot delete streak. Cannot check whether game ID exist or not. Database error. "
								+ e1.getMessage());
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			if (!streakAccess.isStreakIdExist(conn, gameId, streakId)) {
				objResponse.put("message", "Cannot delete streak. Failed to delete the streak. Streak ID is not exist!");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			streakAccess.deleteStreak(conn, gameId, streakId);

			objResponse.put("message", "streak Deleted");
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_21, "" + randomLong, true);
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_30, "" + name, true);
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_31, "" + gameId, true);
			return Response.status(HttpURLConnection.HTTP_OK).entity(objResponse.toString())
					.type(MediaType.APPLICATION_JSON).build();

		} catch (SQLException e) {

			e.printStackTrace();
			objResponse.put("message", "Cannot delete streak. Database error. " + e.getMessage());
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
			return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toString())
					.type(MediaType.APPLICATION_JSON).build();
		}
		// always close connections
		finally {
			try {
				if(conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				logger.printStackTrace(e);
			}
		}
	}

	/**
	 * Get a list of streaks from database
	 * 
	 * @param gameId       Game ID obtained from Gamification Game Service
	 * @param currentPage  current cursor page
	 * @param windowSize   size of fetched data (use -1 to fetch all data)
	 * @param searchPhrase search word
	 * @return HTTP Response returned as JSON object
	 */
	@GET
	@Path("/{gameId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Found a list of streaks"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal Error"),
			@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized") })
	@ApiOperation(value = "getStreakList", notes = "Returns a list of streaks", response = StreakModel.class, responseContainer = "List")
	public Response getStreakList(@ApiParam(value = "Game ID to return") @PathParam("gameId") String gameId,
			@ApiParam(value = "Page number for retrieving data") @QueryParam("current") int currentPage,
			@ApiParam(value = "Number of data size") @QueryParam("rowCount") int windowSize,
			@ApiParam(value = "Search phrase parameter") @QueryParam("searchPhrase") String searchPhrase) {

		// Request log
		Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_99,
				"GET " + "gamification/streaks/" + gameId, true);
		long randomLong = new Random().nextLong(); // To be able to match

		List<StreakModel> streaks = null;
		Connection conn = null;

		JSONObject objResponse = new JSONObject();
		String name = null;
		Agent agent = Context.getCurrent().getMainAgent();
		if (agent instanceof AnonymousAgent) {
			return unauthorizedMessage();
		} else if (agent instanceof UserAgent) {
			UserAgent userAgent = (UserAgent) Context.getCurrent().getMainAgent();
			name = userAgent.getLoginName();
		} else {
			name = agent.getIdentifier();
		}
		try {
			conn = dbm.getConnection();
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_46, "" + randomLong, true);

			try {
				if (!streakAccess.isGameIdExist(conn, gameId)) {
					objResponse.put("message", "Cannot get streaks. Game not found");
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
							(String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
							.type(MediaType.APPLICATION_JSON).build();
				}
			} catch (SQLException e1) {
				e1.printStackTrace();
				objResponse.put("message",
						"Cannot get streaks. Cannot check whether game ID exist or not. Database error. "
								+ e1.getMessage());
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			int offset = (currentPage - 1) * windowSize;
			int totalNum = streakAccess.getNumberOfStreaks(conn, gameId);

			if (windowSize == -1) {
				offset = 0;
				windowSize = totalNum;
			}

			streaks = streakAccess.getStreaksWithOffsetAndSearchPhrase(conn, gameId, offset, windowSize, searchPhrase);

			JSONArray streakArray = new JSONArray();
			
			for (StreakModel streak : streaks) {
				JSONObject obj = new JSONObject(streak);
				streakArray.put(obj);
			}
			
			objResponse.put("current", currentPage);
			objResponse.put("rowCount", windowSize);
			objResponse.put("rows", streakArray);
			objResponse.put("total", totalNum);

			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_47, "" + randomLong, true);
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_48, "" + name, true);
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_49, "" + gameId, true);
			return Response.status(HttpURLConnection.HTTP_OK).entity(objResponse.toString())
					.type(MediaType.APPLICATION_JSON).build();

		} catch (SQLException e) {
			e.printStackTrace();
			objResponse.put("message", "Cannot get streaks. Database error. " + e.getMessage());
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
			return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toString())
					.type(MediaType.APPLICATION_JSON).build();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			objResponse.put("message", "Cannot get streaks. JSON process error. " + e.getMessage());
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
			return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(e.getMessage())
					.type(MediaType.APPLICATION_JSON).build();

		} catch (IOException e) {
			e.printStackTrace();
			objResponse.put("message", "Cannot get streaks. IO Exception. " + e.getMessage());
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
			return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toString())
					.type(MediaType.APPLICATION_JSON).build();

		}
		// always close connections
		finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				logger.printStackTrace(e);
			}
		}
	}

	// RMI
	/**
	 * RMI function to get streak detail with specific ID
	 * 
	 * @param gameId   gameId
	 * @param streakId streakId
	 * @return Serialized JSON string of a streak
	 */
	public String getStreakWithIdRMI(String gameId, String streakId) {
		StreakModel streak;
		Connection conn = null;

		try {
			conn = dbm.getConnection();
			streak = streakAccess.getStreakWithId(conn, gameId, streakId);
			if (streak == null) {
				return null;
			}
			ObjectMapper objectMapper = new ObjectMapper();
			// Set pretty printing of json
			objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

			String streakString = objectMapper.writeValueAsString(streak);
			return streakString;
		} catch (SQLException e) {
			e.printStackTrace();
			logger.warning("Get Streak with ID RMI failed. " + e.getMessage());
			return null;
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			logger.warning("Get Streak with ID RMI failed. " + e.getMessage());
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			logger.warning("Get Streak with ID RMI failed. " + e.getMessage());
			return null;
		}
		// always close connections
		finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				logger.printStackTrace(e);
			}
		}
	}
}

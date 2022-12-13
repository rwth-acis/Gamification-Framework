package i5.las2peer.services.gamificationVisualizationService;

import java.io.IOException;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Random;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
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
import i5.las2peer.services.gamification.commons.database.DatabaseManager;
import i5.las2peer.services.gamificationVisualizationService.database.AchievementModel;
import i5.las2peer.services.gamificationVisualizationService.database.BadgeModel;
import i5.las2peer.services.gamificationVisualizationService.database.QuestModel;
import i5.las2peer.services.gamificationVisualizationService.database.QuestModel.QuestStatus;
import i5.las2peer.services.gamificationVisualizationService.database.StreakModel;
import i5.las2peer.services.gamificationVisualizationService.database.VisualizationDAO;
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
@ServicePath("/gamification/visualization")
public class GamificationVisualizationService extends RESTService {

	// instantiate the logger class
	private final L2pLogger logger = L2pLogger.getInstance(GamificationVisualizationService.class.getName());
	/*
	 * Database configuration
	 */
	private String jdbcDriverClassName;
	private String jdbcLogin;
	private String jdbcPass;
	private String jdbcUrl;
	private String jdbcSchema;
	private DatabaseManager dbm;
	private VisualizationDAO visualizationAccess;

	// this header is not known to javax.ws.rs.core.HttpHeaders
	public static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";
	public static final String HEADER_CONTENT_TYPE = "Content-Type";
	// non HTTP standard headers
	public static final String HEADER_OWNERID = "ownerid";
	public static final String HEADER_CONTENT_DESCRIPTION = "Content-Description";

	public GamificationVisualizationService() {
		setFieldValues();
		System.out.println(jdbcDriverClassName + ", " + jdbcLogin + ", " + jdbcPass + ", " + jdbcUrl + ", " + jdbcSchema);
		dbm = DatabaseManager.getInstance(jdbcDriverClassName, jdbcLogin, jdbcPass, jdbcUrl, jdbcSchema);
		this.visualizationAccess = new VisualizationDAO();
	}

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

	// //////////////////////////////////////////////////////////////////////////////////////
	// Service methods.
	// //////////////////////////////////////////////////////////////////////////////////////

	// //////////////////////////////////////////////////////////////////////////////////////
	// Game PART --------------------------------------
	// //////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Get member point
	 * 
	 * @param gameId   gameId
	 * @param memberId member id
	 * @return HTTP Response with the returnString
	 */
	@GET
	@Path("/points/{gameId}/{memberId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Game Selected"),
			@ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Game not found"),
			@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad Request"), })
	@ApiOperation(value = "", notes = "Select an Game")
	public Response getPointOfMember(@ApiParam(value = "Game ID", required = true) @PathParam("gameId") String gameId,
			@ApiParam(value = "Member ID", required = true) @PathParam("memberId") String memberId) {
		JSONObject objResponse = new JSONObject();
		Connection conn = null;

		Agent agent = Context.getCurrent().getMainAgent();
		if (agent instanceof AnonymousAgent) {
			return unauthorizedMessage();
		}

		try {
			conn = dbm.getConnection();
			if (!visualizationAccess.isGameIdExist(conn, gameId)) {
				logger.info("Game not found >> ");
				objResponse.put("message", "Game not found");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			if (!visualizationAccess.isMemberRegistered(conn, memberId)) {
				logger.info("Member ID not found >> ");
				objResponse.put("message", "Member ID not found");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			if (!visualizationAccess.isMemberRegisteredInGame(conn, memberId, gameId)) {
				logger.info("Member is not registered in Game >> ");
				objResponse.put("message", "Member is not registered in Game");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			// Add Member to Game
			Integer memberPoint = visualizationAccess.getMemberPoint(conn, gameId, memberId);
			objResponse.put("message", memberPoint);
			return Response.status(HttpURLConnection.HTTP_OK).entity(objResponse.toString())
					.type(MediaType.APPLICATION_JSON).build();
		} catch (SQLException e) {

			e.printStackTrace();

			logger.info("SQLException >> " + e.getMessage());
			objResponse.put("message", "Database Error");
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
	 * Get gamification level status for a specific user
	 * 
	 * @param gameId   gameId
	 * @param memberId member id
	 * @return HTTP Response with the returnString
	 */
	@GET
	@Path("/status/{gameId}/{memberId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Game Selected"),
			@ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Game not found"),
			@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad Request"), })
	@ApiOperation(value = "", notes = "Select an Game")
	public Response getStatusOfMember(@ApiParam(value = "Game ID", required = true) @PathParam("gameId") String gameId,
			@ApiParam(value = "Member ID", required = true) @PathParam("memberId") String memberId) {
		long randomLong = new Random().nextLong(); // To be able to match

		JSONObject objResponse = new JSONObject();
		Connection conn = null;

		Agent agent = Context.getCurrent().getMainAgent();
		if (agent instanceof AnonymousAgent) {
			return unauthorizedMessage();
		}

		try {
			conn = dbm.getConnection();
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_32, "" + randomLong, true);

			if (!visualizationAccess.isGameIdExist(conn, gameId)) {
				logger.info("Game not found >> ");
				objResponse.put("message", "Game not found");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			if (!visualizationAccess.isMemberRegistered(conn, memberId)) {
				logger.info("Member ID not found >> ");
				objResponse.put("message", "Member ID not found");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			if (!visualizationAccess.isMemberRegisteredInGame(conn, memberId, gameId)) {
				logger.info("Member is not registered in Game >> ");
				objResponse.put("message", "Member is not registered in Game");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}

			// Get point name
			// RMI call with parameters
			String pointUnitName = "";
			try {
				pointUnitName = (String) Context.getCurrent().invoke(
						"i5.las2peer.services.gamificationPointService.GamificationPointService@0.1", "getUnitNameRMI",
						new Serializable[] { gameId, memberId });

			} catch (Exception e) {
				e.printStackTrace();
				pointUnitName = "";
			}

			// Add Member to Game
			JSONObject obj = visualizationAccess.getMemberStatus(conn, gameId, memberId);
			obj.put("pointUnitName", pointUnitName);
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_33, "" + randomLong, true);
			return Response.status(HttpURLConnection.HTTP_OK).entity(obj.toString())
					.type(MediaType.APPLICATION_JSON).build();
		} catch (SQLException e) {

			e.printStackTrace();

			logger.info("SQLException >> " + e.getMessage());
			objResponse.put("message", "Database Error");
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
	 * Get gamification badges for a specific member
	 * 
	 * @param gameId   gameId
	 * @param memberId member id
	 * @return HTTP Response with the returnString
	 */
	@GET
	@Path("/badges/{gameId}/{memberId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Game Selected"),
			@ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Game not found"),
			@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad Request"), })
	@ApiOperation(value = "", notes = "Select an Game")
	public Response getBadgesOfMember(@ApiParam(value = "Game ID", required = true) @PathParam("gameId") String gameId,
			@ApiParam(value = "Member ID", required = true) @PathParam("memberId") String memberId) {
		long randomLong = new Random().nextLong(); // To be able to match

		JSONObject objResponse = new JSONObject();
		Connection conn = null;
		
		List<BadgeModel> badges = new ArrayList<BadgeModel>();
		Agent agent = Context.getCurrent().getMainAgent();
		if (agent instanceof AnonymousAgent) {
			return unauthorizedMessage();
		}

		try {
			conn = dbm.getConnection();
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_38, "" + randomLong, true);

			if (!visualizationAccess.isGameIdExist(conn, gameId)) {
				logger.info("Game not found >> ");
				objResponse.put("message", "Game not found");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			if (!visualizationAccess.isMemberRegistered(conn, memberId)) {
				logger.info("Member ID not found >> ");
				objResponse.put("message", "Member ID not found");
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			if (!visualizationAccess.isMemberRegisteredInGame(conn, memberId, gameId)) {
				logger.info("Member is not registered in Game >> ");
				objResponse.put("message", "Member is not registered in Game");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			// Add Member to Game
			badges = visualizationAccess.getObtainedBadges(conn, gameId, memberId);
			ObjectMapper objectMapper = new ObjectMapper();
			// Set pretty printing of json
			objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
			String response = objectMapper.writeValueAsString(badges);
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_39, "" + randomLong, true);
			return Response.status(HttpURLConnection.HTTP_OK).entity(response).type(MediaType.APPLICATION_JSON).build();
		} catch (SQLException e) {

			e.printStackTrace();

			logger.info("SQLException >> " + e.getMessage());
			objResponse.put("message", "Database Error");
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
			return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toString())
					.type(MediaType.APPLICATION_JSON).build();
		} catch (JsonProcessingException e) {
			// Object mapper
			e.printStackTrace();

			logger.info("JsonProcessingException >> " + e.getMessage());
			objResponse.put("message", "Failed to parse JSON internally");
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

	public String getBadgesOfMemberRMI(String gameId,
			String memberId) {
		long randomLong = new Random().nextLong(); // To be able to match

		JSONObject objResponse = new JSONObject();
		Connection conn = null;
		
		List<BadgeModel> badges = new ArrayList<BadgeModel>();
		Agent agent = Context.getCurrent().getMainAgent();
		if (agent instanceof AnonymousAgent) {
			return unauthorizedMessage().toString();
		}

		try {
			conn = dbm.getConnection();
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_38, "" + randomLong, true);

			if (!visualizationAccess.isGameIdExist(conn, gameId)) {
				logger.info("Game not found >> ");
				objResponse.put("message", "Game not found");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return objResponse.toString();
			}
			if (!visualizationAccess.isMemberRegistered(conn, memberId)) {
				logger.info("Member ID not found >> ");
				objResponse.put("message", "Member ID not found");
				return objResponse.toString();
			}
			if (!visualizationAccess.isMemberRegisteredInGame(conn, memberId, gameId)) {
				logger.info("Member is not registered in Game >> ");
				objResponse.put("message", "Member is not registered in Game");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return objResponse.toString();
			}
			// Add Member to Game
			badges = visualizationAccess.getObtainedBadges(conn, gameId, memberId);
			for(int i=0; i < badges.size();i++){
				BadgeModel badge = badges.get(i);
				System.out.println("searching for badge image" );
			
				
				try{

					String result =  (Context.getCurrent().invoke(
						"i5.las2peer.services.gamificationBadgeService.GamificationBadgeService@0.1",
						"getBadgeImageMethodRMI", gameId, badge.getId())).toString();
						badge.setBase64(result);
						badges.set(i, badge);
						System.out.println("badge img found" );
				} catch (Exception e){
					e.printStackTrace();
				}
			}
			ObjectMapper objectMapper = new ObjectMapper();
			// Set pretty printing of json
			objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
			String response = objectMapper.writeValueAsString(badges);
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_39, "" + randomLong, true);
			return response;
		} catch (SQLException e) {

			e.printStackTrace();

			logger.info("SQLException >> " + e.getMessage());
			objResponse.put("message", "Database Error");
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
			return objResponse.toString();
		} catch (JsonProcessingException e) {
			// Object mapper
			e.printStackTrace();

			logger.info("JsonProcessingException >> " + e.getMessage());
			objResponse.put("message", "Failed to parse JSON internally");
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
			return objResponse.toString();
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
	 * Get gamification quests with status for a specific member
	 * 
	 * @param gameId   gameId
	 * @param memberId member id
	 * @param statusId quest status
	 * @return HTTP Response with the returnString
	 */
	@GET
	@Path("/quests/{gameId}/{memberId}/status/{statusId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Game Selected"),
			@ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Game not found"),
			@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad Request"), })
	@ApiOperation(value = "", notes = "Select an Game")
	public Response getQuestsWithStatusOfMember(
			@ApiParam(value = "Game ID", required = true) @PathParam("gameId") String gameId,
			@ApiParam(value = "Member ID", required = true) @PathParam("memberId") String memberId,
			@ApiParam(value = "Quest status", required = true) @PathParam("statusId") String statusId) {
		long randomLong = new Random().nextLong(); // To be able to match

		JSONObject objResponse = new JSONObject();
		Connection conn = null;

		List<QuestModel> quests = new ArrayList<QuestModel>();
		Agent agent = Context.getCurrent().getMainAgent();
		if (agent instanceof AnonymousAgent) {
			return unauthorizedMessage();
		}

		try {
			conn = dbm.getConnection();
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_42, "" + randomLong, true);

			if (!visualizationAccess.isGameIdExist(conn, gameId)) {
				logger.info("Game not found >> ");
				objResponse.put("message", "Game not found");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			if (!visualizationAccess.isMemberRegistered(conn, memberId)) {
				logger.info("Member ID not found >> ");
				objResponse.put("message", "Member ID not found");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			if (!visualizationAccess.isMemberRegisteredInGame(conn, memberId, gameId)) {
				logger.info("Member is not registered in Game >> ");
				objResponse.put("message", "Member is not registered in Game");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			// Add Member to Game
			if (statusId.equals("REVEALED") || statusId.equals("COMPLETED")) {
				quests = visualizationAccess.getMemberQuestsWithStatus(conn, gameId, memberId,
						QuestStatus.valueOf(statusId));
			} else if (statusId.equals("ALL")) {
				quests = visualizationAccess.getMemberQuests(conn, gameId, memberId);
			} else {
				logger.info("Status is not recognized >> ");
				objResponse.put("message", "Quest satus is not recognized");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			ObjectMapper objectMapper = new ObjectMapper();
			// Set pretty printing of json
			objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
			String response = objectMapper.writeValueAsString(quests);
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_43, "" + randomLong, true);
			return Response.status(HttpURLConnection.HTTP_OK).entity(response).type(MediaType.APPLICATION_JSON).build();
		} catch (SQLException e) {

			e.printStackTrace();

			logger.info("SQLException >> " + e.getMessage());
			objResponse.put("message", "Database Error");
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
			return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toString())
					.type(MediaType.APPLICATION_JSON).build();
		} catch (JsonProcessingException e) {
			// Object mapper
			e.printStackTrace();

			logger.info("JsonProcessingException >> " + e.getMessage());
			objResponse.put("message", "Failed to parse JSON internally");
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
			return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toString())
					.type(MediaType.APPLICATION_JSON).build();
		} catch (IOException e) {
			e.printStackTrace();
			logger.info("IOException >> " + e.getMessage());
			objResponse.put("message", "Error when getting quests ");
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
	 * Get gamification quests progress for a specific member
	 * 
	 * @param gameId   gameId
	 * @param memberId member id
	 * @param questId  quest id
	 * @return HTTP Response with the returnString
	 */
	@GET
	@Path("/quests/{gameId}/{memberId}/progress/{questId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Game Selected"),
			@ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Game not found"),
			@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad Request"), })
	@ApiOperation(value = "", notes = "Select an Game")
	public Response getQuestProgressOfMember(
			@ApiParam(value = "Game ID", required = true) @PathParam("gameId") String gameId,
			@ApiParam(value = "Member ID", required = true) @PathParam("memberId") String memberId,
			@ApiParam(value = "Quest ID", required = true) @PathParam("questId") String questId) {

		JSONObject objResponse = new JSONObject();
		Connection conn = null;

		Agent agent = Context.getCurrent().getMainAgent();
		if (agent instanceof AnonymousAgent) {
			return unauthorizedMessage();
		}
		try {
			conn = dbm.getConnection();
			if (!visualizationAccess.isGameIdExist(conn, gameId)) {
				logger.info("Game not found >> ");
				objResponse.put("message", "Game not found");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			if (!visualizationAccess.isMemberRegistered(conn, memberId)) {
				logger.info("Member ID not found >> ");
				objResponse.put("message", "Member ID not found");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			if (!visualizationAccess.isMemberRegisteredInGame(conn, memberId, gameId)) {
				logger.info("Member is not registered in Game >> ");
				objResponse.put("message", "Member is not registered in Game");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			JSONObject outObj = visualizationAccess.getMemberQuestProgress(conn, gameId, memberId, questId);
			return Response.status(HttpURLConnection.HTTP_OK).entity(outObj.toString())
					.type(MediaType.APPLICATION_JSON).build();
		} catch (SQLException e) {

			e.printStackTrace();

			logger.info("SQLException >> " + e.getMessage());
			objResponse.put("message", "Database Error");
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
			return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toString())
					.type(MediaType.APPLICATION_JSON).build();
		} catch (JsonProcessingException e) {
			// Object mapper
			e.printStackTrace();

			logger.info("JsonProcessingException >> " + e.getMessage());
			objResponse.put("message", "Failed to parse JSON internally");
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
			return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toString())
					.type(MediaType.APPLICATION_JSON).build();
		} catch (IOException e) {
			e.printStackTrace();
			logger.info("IOException >> " + e.getMessage());
			objResponse.put("message", "Error when getting quests ");
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
	 * Get gamification achievements for a specific member
	 * 
	 * @param gameId   gameId
	 * @param memberId member id
	 * @return HTTP Response with the returnString
	 */
	@GET
	@Path("/achievements/{gameId}/{memberId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Game Selected"),
			@ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Game not found"),
			@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad Request"), })
	@ApiOperation(value = "", notes = "Select an Game")
	public Response getAchievementsOfMember(
			@ApiParam(value = "Game ID", required = true) @PathParam("gameId") String gameId,
			@ApiParam(value = "Member ID", required = true) @PathParam("memberId") String memberId) {
		long randomLong = new Random().nextLong(); // To be able to match

		JSONObject objResponse = new JSONObject();
		Connection conn = null;

		List<AchievementModel> ach = new ArrayList<AchievementModel>();
		Agent agent = Context.getCurrent().getMainAgent();
		if (agent instanceof AnonymousAgent) {
			return unauthorizedMessage();
		}

		try {
			conn = dbm.getConnection();
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_40, "" + randomLong, true);

			if (!visualizationAccess.isGameIdExist(conn, gameId)) {
				logger.info("Game not found >> ");
				objResponse.put("message", "Game not found");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			if (!visualizationAccess.isMemberRegistered(conn, memberId)) {
				logger.info("Member ID not found >> ");
				objResponse.put("message", "Member ID not found");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			if (!visualizationAccess.isMemberRegisteredInGame(conn, memberId, gameId)) {
				logger.info("Member is not registered in Game >> ");
				objResponse.put("message", "Member is not registered in Game");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			ach = visualizationAccess.getMemberAchievements(conn, gameId, memberId);
			ObjectMapper objectMapper = new ObjectMapper();
			// Set pretty printing of json
			objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
			String response = objectMapper.writeValueAsString(ach);
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_41, "" + randomLong, true);
			return Response.status(HttpURLConnection.HTTP_OK).entity(response).type(MediaType.APPLICATION_JSON).build();

		} catch (SQLException e) {

			e.printStackTrace();

			logger.info("SQLException >> " + e.getMessage());
			objResponse.put("message", "Database Error");
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
			return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toString())
					.type(MediaType.APPLICATION_JSON).build();
		} catch (JsonProcessingException e) {
			// Object mapper
			e.printStackTrace();

			logger.info("JsonProcessingException >> " + e.getMessage());
			objResponse.put("message", "Failed to parse JSON internally");
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

	// Invoke services in gamification manager service

	// Get badge image
	/**
	 * Fetch a badge image with specified ID
	 * 
	 * @param gameId   game id
	 * @param badgeId  badge id
	 * @param memberId member id
	 * @return HTTP Response with the return image
	 */
	@GET
	@Path("/badges/{gameId}/{memberId}/{badgeId}/img")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Badges Entry"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Cannot found image") })
	@ApiOperation(value = "", notes = "list of stored badges")
	public Response getBadgeImageDetail(@PathParam("gameId") String gameId, @PathParam("badgeId") String badgeId,
			@ApiParam(value = "Member ID", required = true) @PathParam("memberId") String memberId) {
		JSONObject objResponse = new JSONObject();
		Connection conn = null;

		Agent agent = Context.getCurrent().getMainAgent();
		if (agent instanceof AnonymousAgent) {
			return unauthorizedMessage();
		}
		try {
			conn = dbm.getConnection();
			if (!visualizationAccess.isGameIdExist(conn, gameId)) {
				logger.info("Game not found >> ");
				objResponse.put("message", "Game not found");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			if (!visualizationAccess.isMemberRegistered(conn, memberId)) {
				logger.info("Member ID not found >> ");
				objResponse.put("message", "Member ID not found");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			if (!visualizationAccess.isMemberRegisteredInGame(conn, memberId, gameId)) {
				logger.info("Member is not registered in Game >> ");
				objResponse.put("message", "Member is not registered in Game");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			if (!visualizationAccess.isMemberHasBadge(conn, gameId, memberId, badgeId)) {
				logger.info("Error. member " + memberId + " does not have a badge " + badgeId + ".");
				objResponse.put("message", "Error. member " + memberId + " does not have a badge " + badgeId + ".");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			// RMI call with parameters
			byte[] result;
			try {
				result = (byte[]) Context.getCurrent().invoke(
						"i5.las2peer.services.gamificationBadgeService.GamificationBadgeService@0.1",
						"getBadgeImageMethod", (String) gameId, (String) badgeId);
				if (result != null) {
					return Response.status(HttpURLConnection.HTTP_OK).entity(result).type(MediaType.APPLICATION_JSON)
							.build();
				}
			} catch (Exception e) {
				e.printStackTrace();
				logger.info("Error cannot retrieve file " + e.getMessage());
				objResponse.put("message", "Error cannot retrieve file " + e.getMessage());
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();

			}
			logger.info("Error cannot retrieve file ");
			objResponse.put("message", "Error cannot retrieve file ");
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
			return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toString())
					.type(MediaType.APPLICATION_JSON).build();

		} catch (SQLException e) {
			e.printStackTrace();
			logger.info("Database error >>  " + e.getMessage());
			objResponse.put("message", "Database error " + e.getMessage());
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
			return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
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

	// Get badge information individually
	/**
	 * Get a badge data with specific ID from database
	 * 
	 * @param gameId   gameId
	 * @param badgeId  badge id
	 * @param memberId member id
	 * @return HTTP Response Returned as JSON object
	 */
	@GET
	@Path("/badges/{gameId}/{memberId}/{badgeId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Found a badges"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal Error"),
			@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized") })
	@ApiOperation(value = "Find point for specific Game ID and badge ID", notes = "Returns a badge", response = BadgeModel.class, responseContainer = "List", authorizations = @Authorization(value = "api_key"))
	public Response getBadgeDetailWithId(@ApiParam(value = "Game ID") @PathParam("gameId") String gameId,
			@ApiParam(value = "Badge ID") @PathParam("badgeId") String badgeId,
			@ApiParam(value = "Member ID", required = true) @PathParam("memberId") String memberId) {
		JSONObject objResponse = new JSONObject();
		Connection conn = null;

		Agent agent = Context.getCurrent().getMainAgent();
		if (agent instanceof AnonymousAgent) {
			return unauthorizedMessage();
		}
		try {
			conn = dbm.getConnection();
			if (!visualizationAccess.isGameIdExist(conn, gameId)) {
				logger.info("Game not found >> ");
				objResponse.put("message", "Game not found");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			if (!visualizationAccess.isMemberRegistered(conn, memberId)) {
				logger.info("Member ID not found >> ");
				objResponse.put("message", "Member ID not found");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			if (!visualizationAccess.isMemberRegisteredInGame(conn, memberId, gameId)) {
				logger.info("Member is not registered in Game >> ");
				objResponse.put("message", "Member is not registered in Game");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			if (!visualizationAccess.isMemberHasBadge(conn, gameId, memberId, badgeId)) {
				logger.info("Error. member " + memberId + " does not have a badge " + badgeId + ".");
				objResponse.put("message", "Error. member " + memberId + " does not have a badge " + badgeId + ".");
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			// RMI call with parameters
			String result;
			try {
				result = (String) Context.getCurrent().invoke(
						"i5.las2peer.services.gamificationBadgeService.GamificationBadgeService@0.1",
						"getBadgeWithIdRMI", new Serializable[] { gameId, badgeId });

				System.out.println("BADGE STRING " + result);
				if (result != null) {
					return Response.status(HttpURLConnection.HTTP_OK).entity(result).type(MediaType.APPLICATION_JSON)
							.build();
				}
				logger.info("Cannot find badge with " + badgeId);
				objResponse.put("message", "Cannot find badge with " + badgeId);
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();

			} catch (Exception e) {
				e.printStackTrace();
				logger.info("Cannot find badge with " + badgeId + ". " + e.getMessage());
				objResponse.put("message", "Cannot find badge with " + badgeId + ". " + e.getMessage());
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();

			}
		} catch (SQLException e) {
			e.printStackTrace();
			logger.info("Database Error ");
			objResponse.put("message", "Database Error ");
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

	// Get quest information individually
	/**
	 * Get a quest data with specific ID from database
	 * 
	 * @param gameId   gameId
	 * @param questId  quest id
	 * @param memberId member id
	 * @return HTTP Response Returned as JSON object
	 */
	@GET
	@Path("/quests/{gameId}/{memberId}/{questId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Found a quest"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal Error"),
			@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized") })
	@ApiOperation(value = "Find quest for specific Game ID and quest ID", notes = "Returns a quest", response = QuestModel.class, authorizations = @Authorization(value = "api_key"))
	public Response getQuestDetailWithId(@ApiParam(value = "Game ID") @PathParam("gameId") String gameId,
			@ApiParam(value = "Quest ID") @PathParam("questId") String questId,
			@ApiParam(value = "Member ID", required = true) @PathParam("memberId") String memberId) {
		JSONObject objResponse = new JSONObject();
		Connection conn = null;

		Agent agent = Context.getCurrent().getMainAgent();
		if (agent instanceof AnonymousAgent) {
			return unauthorizedMessage();
		}

		try {
			conn = dbm.getConnection();
			if (!visualizationAccess.isGameIdExist(conn, gameId)) {
				logger.info("Game not found >> ");
				objResponse.put("message", "Game not found");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			if (!visualizationAccess.isMemberRegistered(conn, memberId)) {
				logger.info("Member ID not found >> ");
				objResponse.put("message", "Member ID not found");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			if (!visualizationAccess.isMemberRegisteredInGame(conn, memberId, gameId)) {
				logger.info("Member is not registered in Game >> ");
				objResponse.put("message", "Member is not registered in Game");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			// RMI call with parameters
			String result;
			try {
				result = (String) Context.getCurrent().invoke(
						"i5.las2peer.services.gamificationQuestService.GamificationQuestService@0.1",
						"getQuestWithIdRMI", new Serializable[] { gameId, questId });
				if (result != null) {
					return Response.status(HttpURLConnection.HTTP_OK).entity(result).type(MediaType.APPLICATION_JSON)
							.build();
				}
				logger.info("Cannot find badge with " + questId);
				objResponse.put("message", "Cannot find badge with " + questId);
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();

			} catch (Exception e) {
				e.printStackTrace();
				logger.info("Cannot find badge with " + questId + ". " + e.getMessage());
				objResponse.put("message", "Cannot find badge with " + questId + ". " + e.getMessage());
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();

			}
		} catch (SQLException e) {
			e.printStackTrace();
			logger.info("DB Error >> " + e.getMessage());
			objResponse.put("message", "DB Error. " + e.getMessage());
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

	// Get all quest information of member
	/**
	 * Get a quest data with specific ID from database
	 * 
	 * @param gameId   gameId
	 * @param memberId member id
	 * @return HTTP Response Returned as JSON object
	 */
	@GET
	@Path("/quests/{gameId}/{memberId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Found a quest"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal Error"),
			@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized") })
	@ApiOperation(value = "Find quest for specific Game ID and quest ID", notes = "Returns a quest", response = QuestModel.class, authorizations = @Authorization(value = "api_key"))
	public Response getQuestDetailAll(@ApiParam(value = "Game ID") @PathParam("gameId") String gameId,
			@ApiParam(value = "Member ID", required = true) @PathParam("memberId") String memberId) {
		JSONObject objResponse = new JSONObject();
		Connection conn = null;
		Agent agent = Context.getCurrent().getMainAgent();
		if (agent instanceof AnonymousAgent) {
			return unauthorizedMessage();
		}

		try {
			conn = dbm.getConnection();
			if (!visualizationAccess.isGameIdExist(conn, gameId)) {
				logger.info("Game not found >> ");
				objResponse.put("message", "Game not found");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			if (!visualizationAccess.isMemberRegistered(conn, memberId)) {
				logger.info("Member ID not found >> ");
				objResponse.put("message", "Member ID not found");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			if (!visualizationAccess.isMemberRegisteredInGame(conn, memberId, gameId)) {
				logger.info("Member is not registered in Game >> ");
				objResponse.put("message", "Member is not registered in Game");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			// RMI call with parameters
			String result;
			JSONObject result2;
			JSONObject finalResp  = new JSONObject();
			try {
				result = (String) Context.getCurrent().invoke(
						"i5.las2peer.services.gamificationQuestService.GamificationQuestService@0.1",
						"getQuestListRMI", new Serializable[] { gameId,0,0,""});
				if (result != null) {
					System.out.println(result);
					JSONObject jsonResult = new JSONObject(result);
					for(Object quest : (JSONArray) jsonResult.get("rows")){
						JSONObject jsonQuest = (JSONObject) quest;
						result2 = visualizationAccess.getMemberQuestProgress(conn,gameId,memberId, jsonQuest.getString("id"));
						System.out.println(result2);
						finalResp.put(jsonQuest.getString("id"), result2);

					}
					return Response.status(HttpURLConnection.HTTP_OK).entity(finalResp.toString()).type(MediaType.APPLICATION_JSON)
							.build();
				}
				logger.info("Cannot find badge with ");
				objResponse.put("message", "Cannot find badge with ");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();

			} catch (Exception e) {
				e.printStackTrace();
				logger.info("Cannot find badge with . " + e.getMessage());
				objResponse.put("message", "Cannot find badge with . " + e.getMessage());
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();

			}
		} catch (SQLException e) {
			e.printStackTrace();
			logger.info("DB Error >> " + e.getMessage());
			objResponse.put("message", "DB Error. " + e.getMessage());
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


	// Get all quest information of member
	/**
	 * Get a quest data with specific ID from database
	 * 
	 */
		public String getQuestDetailAllRMI(String gameId,String memberId) {
		JSONObject objResponse = new JSONObject();
		Connection conn = null;
		Agent agent = Context.getCurrent().getMainAgent();
		if (agent instanceof AnonymousAgent) {
			return unauthorizedMessage().toString();
		}

		try {
			conn = dbm.getConnection();
			if (!visualizationAccess.isGameIdExist(conn, gameId)) {
				logger.info("Game not found >> ");
				objResponse.put("message", "Game not found");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return objResponse.toString();
			}
			if (!visualizationAccess.isMemberRegistered(conn, memberId)) {
				logger.info("Member ID not found >> ");
				objResponse.put("message", "Member ID not found");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return objResponse.toString();
			}
			if (!visualizationAccess.isMemberRegisteredInGame(conn, memberId, gameId)) {
				logger.info("Member is not registered in Game >> ");
				objResponse.put("message", "Member is not registered in Game");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return objResponse.toString();
			}
			// RMI call with parameters
			String result;
			JSONObject result2;
			JSONObject finalResp  = new JSONObject();
			try {
				result = (String) Context.getCurrent().invoke(
						"i5.las2peer.services.gamificationQuestService.GamificationQuestService@0.1",
						"getQuestListRMI", new Serializable[] { gameId,0,0,""});
				if (result != null) {
					System.out.println(result);
					JSONObject jsonResult = new JSONObject(result);
					for(Object quest : (JSONArray) jsonResult.get("rows")){
						JSONObject jsonQuest = (JSONObject) quest;
						result2 = visualizationAccess.getMemberQuestProgress(conn,gameId,memberId, jsonQuest.getString("id"));
						System.out.println(result2);
						finalResp.put(jsonQuest.getString("id"), result2);

					}
					return finalResp.toString();
				}
				logger.info("Cannot find badge with ");
				objResponse.put("message", "Cannot find badge with ");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return objResponse.toString();

			} catch (Exception e) {
				e.printStackTrace();
				logger.info("Cannot find badge with . " + e.getMessage());
				objResponse.put("message", "Cannot find badge with . " + e.getMessage());
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return objResponse.toString();

			}
		} catch (SQLException e) {
			e.printStackTrace();
			logger.info("DB Error >> " + e.getMessage());
			objResponse.put("message", "DB Error. " + e.getMessage());
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
			return objResponse.toString();

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

	// Get achievement information individually
	/**
	 * Get an achievement data with specific ID from database
	 * 
	 * @param gameId        gameId
	 * @param achievementId achievement id
	 * @param memberId      member id
	 * @return HTTP Response Returned as JSON object
	 */
	@GET
	@Path("/achievements/{gameId}/{memberId}/{achievementId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Found an achievement"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal Error"),
			@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized") })
	@ApiOperation(value = "Find point for specific Game ID and achievement ID", notes = "Returns a achievement", response = AchievementModel.class, authorizations = @Authorization(value = "api_key"))
	public Response getAchievementDetailWithId(@ApiParam(value = "Game ID") @PathParam("gameId") String gameId,
			@ApiParam(value = "Achievement ID") @PathParam("achievementId") String achievementId,
			@ApiParam(value = "Member ID", required = true) @PathParam("memberId") String memberId) {
		JSONObject objResponse = new JSONObject();
		Connection conn = null;

		Agent agent = Context.getCurrent().getMainAgent();
		if (agent instanceof AnonymousAgent) {
			return unauthorizedMessage();
		}

		try {
			conn = dbm.getConnection();
			if (!visualizationAccess.isGameIdExist(conn, gameId)) {
				logger.info("Game not found >> ");
				objResponse.put("message", "Game not found");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			if (!visualizationAccess.isMemberRegistered(conn, memberId)) {
				logger.info("Member ID not found >> ");
				objResponse.put("message", "Member ID not found");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			if (!visualizationAccess.isMemberRegisteredInGame(conn, memberId, gameId)) {
				logger.info("Member is not registered in Game >> ");
				objResponse.put("message", "Member is not registered in Game");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			if (!visualizationAccess.isMemberHasAchievement(conn, gameId, memberId, achievementId)) {
				logger.info("Error. member " + memberId + " does not have an achievement " + achievementId + ".");
				objResponse.put("message",
						"Member " + memberId + " does not have an achievement " + achievementId + ".");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			// RMI call with parameters
			Object result = new Object();
			try {
				result = Context.getCurrent().invoke(
						"i5.las2peer.services.gamificationAchievementService.GamificationAchievementService@0.1",
						"getAchievementWithIdRMI", new Serializable[] { gameId, achievementId });
				if (result != null) {
					Context.getCurrent().monitorEvent(this, MonitoringEvent.RMI_SUCCESSFUL,
							"Get Achievement with ID RMI success");
					return Response.status(HttpURLConnection.HTTP_OK).entity(result).type(MediaType.APPLICATION_JSON)
							.build();
				}
				Context.getCurrent().monitorEvent(this, MonitoringEvent.RMI_FAILED,
						"Get Achievement with ID RMI failed");
				logger.info("Cannot find achievement with " + achievementId);
				objResponse.put("message", "Cannot find achievement with " + achievementId);
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();

			} catch (Exception e) {
				e.printStackTrace();
				logger.info("Cannot find achievement with " + achievementId + ". " + e.getMessage());
				Context.getCurrent().monitorEvent(this, MonitoringEvent.RMI_FAILED,
						"Get Achievement with ID RMI failed. " + e.getMessage());
				objResponse.put("message", "Cannot find achievement with " + achievementId + ". " + e.getMessage());
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();

			}

		} catch (SQLException e) {
			e.printStackTrace();
			logger.info("DB Error >> " + e.getMessage());
			objResponse.put("message", "DB Error. " + e.getMessage());
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
			return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
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
	 * Trigger an action
	 * 
	 * @param gameId   gameId
	 * @param actionId actionId
	 * @param memberId memberId
	 * @return Notifications in JSON
	 */
	@POST
	@Path("/actions/{gameId}/{actionId}/{memberId}")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "{\"status\": 3, \"message\": \"Action upload success ( (actionid) )\"}"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "{\"status\": 3, \"message\": \"Failed to upload (actionid)\"}"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "{\"status\": 1, \"message\": \"Failed to add the action. Action ID already exist!\"}"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "{\"status\": =, \"message\": \"Action ID cannot be null!\"}"),
			@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "{\"status\": 2, \"message\": \"File content null. Failed to upload (actionid)\"}"),
			@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "{\"status\": 2, \"message\": \"Failed to upload (actionid)\"}"),
			@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "{\"status\": 3, \"message\": \"Action upload success ( (actionid) )}") })
	@ApiOperation(value = "triggerAction", notes = "A method to trigger an ")
	public Response triggerAction(@ApiParam(value = "Game ID", required = true) @PathParam("gameId") String gameId,
			@ApiParam(value = "Action ID", required = true) @PathParam("actionId") String actionId,
			@ApiParam(value = "Member ID", required = true) @PathParam("memberId") String memberId) {
		long randomLong = new Random().nextLong(); // To be able to match
		JSONObject objResponse = new JSONObject();
		Connection conn = null;
		System.out.println("11");
		Agent agent = Context.getCurrent().getMainAgent();
		if (agent instanceof AnonymousAgent) {
			return unauthorizedMessage();
		}
		try {
			System.out.println("1 + " + actionId);
			conn = dbm.getConnection();
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_38, "" + randomLong, true);
			if (!visualizationAccess.isGameIdExist(conn, gameId)) {
				logger.info("Game not found >> ");
				objResponse.put("message", "Game not found");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			System.out.println("2");
			if (!visualizationAccess.isMemberRegistered(conn, memberId)) {
				logger.info("Member ID not found >> ");
				objResponse.put("message", "Member ID not found");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			System.out.println("3");
			if (!visualizationAccess.isMemberRegisteredInGame(conn, memberId, gameId)) {
				logger.info("Member is not registered in Game >> ");
				objResponse.put("message", "Member is not registered in Game");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			System.out.println("4");
			// RMI call with parameters
			String result = "";
			try {
				result = (String) Context.getCurrent().invoke(
						"i5.las2peer.services.gamificationActionService.GamificationActionService@0.1",
						"triggerActionRMI", new Serializable[] { gameId, memberId, actionId });
				if (result != null) {
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_39, "" + randomLong,
							true);
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_44, "" + gameId,
							true);
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_45, "" + memberId,
							true);
							System.out.println("5");
					return Response.status(HttpURLConnection.HTTP_OK).entity(result).type(MediaType.APPLICATION_JSON)
							.build();
				}
				logger.info("Cannot trigger action " + actionId);
				objResponse.put("message", "Cannot trigger action " + actionId);
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();

			} catch (Exception e) {
				e.printStackTrace();
				logger.info("Cannot trigger action " + actionId + ". " + e.getMessage());
				objResponse.put("message", "Cannot trigger action " + actionId + ". " + e.getMessage());
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();

			}

		} catch (SQLException e) {
			e.printStackTrace();
			logger.info("DB Error >> " + e.getMessage());
			objResponse.put("message", "DB Error. " + e.getMessage());
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

	// Leaderboard
	/**
	 * Get local leaderboard
	 * 
	 * @param gameId       gameId
	 * @param memberId     member id
	 * @param currentPage  current cursor page
	 * @param windowSize   size of fetched data
	 * @param searchPhrase search word
	 * @return HHTP Response Returned as JSON object
	 */
	@GET
	@Path("/leaderboard/local/{gameId}/{memberId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Return local leaderboard"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal Error"),
			@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized") })
	@ApiOperation(value = "Get the local leaderboard", notes = "Returns a leaderboard array", authorizations = @Authorization(value = "api_key"))
	public Response getLocalLeaderboard(@ApiParam(value = "Game ID") @PathParam("gameId") String gameId,
			@ApiParam(value = "Member ID", required = true) @PathParam("memberId") String memberId,
			@ApiParam(value = "Page number for retrieving data") @QueryParam("current") int currentPage,
			@ApiParam(value = "Number of data size") @QueryParam("rowCount") int windowSize,
			@ApiParam(value = "Search phrase parameter") @QueryParam("searchPhrase") String searchPhrase) {
		long randomLong = new Random().nextLong(); // To be able to match
		JSONObject objResponse = new JSONObject();
		Connection conn = null;

		Agent agent = Context.getCurrent().getMainAgent();
		if (agent instanceof AnonymousAgent) {
			return unauthorizedMessage();
		}

		try {
			conn = dbm.getConnection();
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_34, "" + randomLong, true);

			if (!visualizationAccess.isGameIdExist(conn, gameId)) {
				logger.info("Game not found >> ");
				objResponse.put("message", "Game not found");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			if (!visualizationAccess.isMemberRegistered(conn, memberId)) {
				logger.info("Member ID not found >> ");
				objResponse.put("message", "Member ID not found");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			if (!visualizationAccess.isMemberRegisteredInGame(conn, memberId, gameId)) {
				logger.info("Member is not registered in Game >> ");
				objResponse.put("message", "Member is not registered in Game");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}

			// Get point name
			// RMI call with parameters
			String pointUnitName = "";
			try {
				pointUnitName = (String) Context.getCurrent().invoke(
						"i5.las2peer.services.gamificationPointService.GamificationPointService@0.1", "getUnitNameRMI",
						new Serializable[] { gameId, memberId });

			} catch (Exception e) {
				e.printStackTrace();
				pointUnitName = "";
			}

			int offset = (currentPage - 1) * windowSize;
			int totalNum = visualizationAccess.getNumberOfMembers(conn, gameId);
			JSONArray arrResult = visualizationAccess.getMemberLocalLeaderboard(conn, gameId);

			for (int i = 0; i < arrResult.length() ; i++) {
				JSONObject object = (JSONObject) arrResult.get(i);
				object.put("pointValue", object.get("pointValue") + " " + pointUnitName);
			}

			objResponse.put("current", currentPage);
			objResponse.put("rowCount", windowSize);
			objResponse.put("rows", arrResult);
			objResponse.put("total", totalNum);
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_35, "" + randomLong, true);
			return Response.status(HttpURLConnection.HTTP_OK).entity(objResponse.toString())
					.type(MediaType.APPLICATION_JSON).build();

		} catch (SQLException e) {
			e.printStackTrace();
			logger.info("DB Error >> " + e.getMessage());
			objResponse.put("message", "DB Error. " + e.getMessage());
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
			return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
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
	 * Get global leaderboard
	 * 
	 * @param gameId       gameId
	 * @param memberId     member id
	 * @param currentPage  current cursor page
	 * @param windowSize   size of fetched data
	 * @param searchPhrase search word
	 * @return HTTP Response Returned as JSON object
	 */
	@GET
	@Path("/leaderboard/global/{gameId}/{memberId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Return global leaderboard"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal Error"),
			@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized") })
	@ApiOperation(value = "Get the local leaderboard", notes = "Returns a leaderboard array", authorizations = @Authorization(value = "api_key"))
	public Response getGlobalLeaderboard(@ApiParam(value = "Game ID") @PathParam("gameId") String gameId,
			@ApiParam(value = "Member ID", required = true) @PathParam("memberId") String memberId,
			@ApiParam(value = "Page number for retrieving data") @QueryParam("current") int currentPage,
			@ApiParam(value = "Number of data size") @QueryParam("rowCount") int windowSize,
			@ApiParam(value = "Search phrase parameter") @QueryParam("searchPhrase") String searchPhrase) {
		long randomLong = new Random().nextLong(); // To be able to match
		JSONObject objResponse = new JSONObject();
		Connection conn = null;

		Agent agent = Context.getCurrent().getMainAgent();
		if (agent instanceof AnonymousAgent) {
			return unauthorizedMessage();
		}
		try {
			conn = dbm.getConnection();
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_36, "" + randomLong, true);

			if (!visualizationAccess.isGameIdExist(conn, gameId)) {
				logger.info("Game not found >> ");
				objResponse.put("message", "Game not found");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			if (!visualizationAccess.isMemberRegistered(conn, memberId)) {
				logger.info("Member ID not found >> ");
				objResponse.put("message", "Member ID not found");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			if (!visualizationAccess.isMemberRegisteredInGame(conn, memberId, gameId)) {
				logger.info("Member is not registered in Game >> ");
				objResponse.put("message", "Member is not registered in Game");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}

			int offset = (currentPage - 1) * windowSize;
			int totalNum = visualizationAccess.getNumberOfMembers(conn, gameId);
			JSONArray arrResult = visualizationAccess.getMemberGlobalLeaderboard(conn, gameId);

			objResponse.put("current", currentPage);
			objResponse.put("rowCount", windowSize);
			objResponse.put("rows", arrResult);
			objResponse.put("total", totalNum);
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_37, "" + randomLong, true);
			return Response.status(HttpURLConnection.HTTP_OK).entity(objResponse.toString())
					.type(MediaType.APPLICATION_JSON).build();

		} catch (SQLException e) {
			e.printStackTrace();
			logger.info("DB Error >> " + e.getMessage());
			objResponse.put("message", "DB Error. " + e.getMessage());
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
			return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
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
	 * Get notification of the members after action has been triggered
	 * 
	 * @param currentPage  currentPage
	 * @param windowSize   windowSize
	 * @param searchPhrase searchPhrase
	 * @param gameId       gameId
	 * @param memberId     member id
	 * @return HTTP Response Returned as JSON object
	 */
	@GET
	@Path("/notifications/{gameId}/{memberId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Return global leaderboard"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal Error"),
			@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized") })
	@ApiOperation(value = "Get the local leaderboard", notes = "Returns a leaderboard array", authorizations = @Authorization(value = "api_key"))
	public Response getNotification(@ApiParam(value = "Game ID") @PathParam("gameId") String gameId,
			@ApiParam(value = "Member ID", required = true) @PathParam("memberId") String memberId,
			@ApiParam(value = "Page number for retrieving data") @QueryParam("current") int currentPage,
			@ApiParam(value = "Number of data size") @QueryParam("rowCount") int windowSize,
			@ApiParam(value = "Search phrase parameter") @QueryParam("searchPhrase") String searchPhrase) {
		JSONObject objResponse = new JSONObject();
		Connection conn = null;

		Agent agent = Context.getCurrent().getMainAgent();
		if (agent instanceof AnonymousAgent) {
			return unauthorizedMessage();
		}

		try {
			conn = dbm.getConnection();
			if (!visualizationAccess.isGameIdExist(conn, gameId)) {
				logger.info("Game not found >> ");
				objResponse.put("message", "Game not found");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			if (!visualizationAccess.isMemberRegistered(conn, memberId)) {
				logger.info("Member ID not found >> ");
				objResponse.put("message", "Member ID not found");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			if (!visualizationAccess.isMemberRegisteredInGame(conn, memberId, gameId)) {
				logger.info("Member is not registered in Game >> ");
				objResponse.put("message", "Member is not registered in Game");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}

			JSONArray arrResult = visualizationAccess.getMemberNotification(conn, gameId, memberId);

			return Response.status(HttpURLConnection.HTTP_OK).entity(arrResult.toString())
					.type(MediaType.APPLICATION_JSON).build();

		} catch (SQLException e) {
			e.printStackTrace();
			logger.info("DB Error >> " + e.getMessage());
			objResponse.put("message", "DB Error. " + e.getMessage());
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
			return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
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
	 * Get gamification streaks for a specific member
	 * 
	 * @param gameId   gameId
	 * @param memberId memberId
	 * @return HTTP Response with the returnString
	 */
	@GET
	@Path("/streaks/{gameId}/{memberId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Game Selected"),
			@ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Game not found"),
			@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad Request"), })
	@ApiOperation(value = "", notes = "Select an Game")
	public Response getStreaksOfMember(@ApiParam(value = "Game ID", required = true) @PathParam("gameId") String gameId,
			@ApiParam(value = "Member ID", required = true) @PathParam("memberId") String memberId) {
		long randomLong = new Random().nextLong(); // To be able to match

		JSONObject objResponse = new JSONObject();
		Connection conn = null;

		Agent agent = Context.getCurrent().getMainAgent();
		if (agent instanceof AnonymousAgent) {
			return unauthorizedMessage();
		}

		try {
			conn = dbm.getConnection();
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_38, "" + randomLong, true);

			if (!visualizationAccess.isGameIdExist(conn, gameId)) {
				logger.info("Game not found >> ");
				objResponse.put("message", "Game not found");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			if (!visualizationAccess.isMemberRegistered(conn, memberId)) {
				logger.info("Member ID not found >> ");
				objResponse.put("message", "Member ID not found");
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			if (!visualizationAccess.isMemberRegisteredInGame(conn, memberId, gameId)) {
				logger.info("Member is not registered in Game >> ");
				objResponse.put("message", "Member is not registered in Game");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			
			JSONArray arr = visualizationAccess.getStreakList(conn, gameId, memberId);
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_39, "" + randomLong, true);
			return Response.status(HttpURLConnection.HTTP_OK).entity(arr.toString()).type(MediaType.APPLICATION_JSON).build();
		} catch (SQLException e) {
			e.printStackTrace();
			logger.info("SQLException >> " + e.getMessage());
			objResponse.put("message", "Database Error");
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
			return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toString())
					.type(MediaType.APPLICATION_JSON).build();
		} catch (JsonProcessingException e) {
			
			e.printStackTrace();
			logger.info("JsonProcessingException >> " + e.getMessage());
			objResponse.put("message", "Failed to parse JSON internally");
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
			return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toString())
					.type(MediaType.APPLICATION_JSON).build();
		} catch (IOException e) {
			e.printStackTrace();
			logger.info("IOException >> " + e.getMessage());
			objResponse.put("message", "Database Error");
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
			return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toString())
					.type(MediaType.APPLICATION_JSON).build();
		}
		// always close connections
		finally {
			try {
				if (conn!=null) {
					conn.close();
				}
			} catch (SQLException e) {
				logger.printStackTrace(e);
			}
		}
	}
	
	/**
	 * Get a streak data with specific ID from database
	 * 
	 * @param gameId        gameId
	 * @param streakId streak id
	 * @param memberId      member id
	 * @return HTTP Response Returned as JSON object
	 */
	@GET
	@Path("/streaks/{gameId}/{memberId}/{streakId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Found a streak"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal Error"),
			@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized") })
	@ApiOperation(value = "Find point for specific Game ID and streak ID", notes = "Returns a streak", response = StreakModel.class, authorizations = @Authorization(value = "api_key"))
	public Response getStreakDetailWithId(@ApiParam(value = "Game ID") @PathParam("gameId") String gameId,
			@ApiParam(value = "Streak ID") @PathParam("streakId") String streakId,
			@ApiParam(value = "Member ID", required = true) @PathParam("memberId") String memberId) {
		JSONObject objResponse = new JSONObject();
		Connection conn = null;

		Agent agent = Context.getCurrent().getMainAgent();
		if (agent instanceof AnonymousAgent) {
			return unauthorizedMessage();
		}

		try {
			conn = dbm.getConnection();
			if (!visualizationAccess.isGameIdExist(conn, gameId)) {
				logger.info("Game not found >> ");
				objResponse.put("message", "Game not found");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			if (!visualizationAccess.isMemberRegistered(conn, memberId)) {
				logger.info("Member ID not found >> ");
				objResponse.put("message", "Member ID not found");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			if (!visualizationAccess.isMemberRegisteredInGame(conn, memberId, gameId)) {
				logger.info("Member is not registered in Game >> ");
				objResponse.put("message", "Member is not registered in Game");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			if (!visualizationAccess.isMemberHasStreak(conn, gameId, memberId, streakId)) {
				logger.info("Error. member " + memberId + " does not have an streak " + streakId + ".");
				objResponse.put("message",
						"Member " + memberId + " does not have an streak " + streakId + ".");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			// RMI call with parameters
			Object result = new Object();
			try {
				result = Context.getCurrent().invoke(
						"i5.las2peer.services.gamificationStreakService.GamificationStreakService@0.1",
						"getStreakWithIdRMI", new Serializable[] { gameId, streakId });
				if (result != null) {
					Context.getCurrent().monitorEvent(this, MonitoringEvent.RMI_SUCCESSFUL,
							"Get Streak with ID RMI success");
					return Response.status(HttpURLConnection.HTTP_OK).entity(result).type(MediaType.APPLICATION_JSON)
							.build();
				}
				Context.getCurrent().monitorEvent(this, MonitoringEvent.RMI_FAILED,
						"Get Streak with ID RMI failed");
				logger.info("Cannot find streak with " + streakId);
				objResponse.put("message", "Cannot find streak with " + streakId);
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();

			} catch (Exception e) {
				e.printStackTrace();
				logger.info("Cannot find streak with " + streakId + ". " + e.getMessage());
				Context.getCurrent().monitorEvent(this, MonitoringEvent.RMI_FAILED,
						"Get Streak with ID RMI failed. " + e.getMessage());
				objResponse.put("message", "Cannot find streak with " + streakId + ". " + e.getMessage());
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();

			}

		} catch (SQLException e) {
			e.printStackTrace();
			logger.info("DB Error >> " + e.getMessage());
			objResponse.put("message", "DB Error. " + e.getMessage());
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
			return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
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
	 * Get gamification streaks progress for a specific member
	 * 
	 * @param gameId   gameId
	 * @param memberId member id
	 * @param streakId  streak id
	 * @return HTTP Response with the returnString
	 */
	@GET
	@Path("/streaks/{gameId}/{memberId}/progress/{streakId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Game Selected"),
			@ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Game not found"),
			@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad Request"), })
	@ApiOperation(value = "", notes = "Select an Game")
	public Response getStreakProgressOfMember(
			@ApiParam(value = "Game ID", required = true) @PathParam("gameId") String gameId,
			@ApiParam(value = "Member ID", required = true) @PathParam("memberId") String memberId,
			@ApiParam(value = "Streak ID", required = true) @PathParam("streakId") String streakId) {

		JSONObject objResponse = new JSONObject();
		Connection conn = null;

		Agent agent = Context.getCurrent().getMainAgent();
		if (agent instanceof AnonymousAgent) {
			return unauthorizedMessage();
		}
		try {
			conn = dbm.getConnection();
			if (!visualizationAccess.isGameIdExist(conn, gameId)) {
				logger.info("Game not found >> ");
				objResponse.put("message", "Game not found");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			if (!visualizationAccess.isMemberRegistered(conn, memberId)) {
				logger.info("Member ID not found >> ");
				objResponse.put("message", "Member ID not found");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			if (!visualizationAccess.isMemberRegisteredInGame(conn, memberId, gameId)) {
				logger.info("Member is not registered in Game >> ");
				objResponse.put("message", "Member is not registered in Game");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			JSONObject outObj = visualizationAccess.getMemberStreakProgress(conn, gameId, memberId, streakId);
			return Response.status(HttpURLConnection.HTTP_OK).entity(outObj.toString())
					.type(MediaType.APPLICATION_JSON).build();
		} catch (SQLException e) {

			e.printStackTrace();

			logger.info("SQLException >> " + e.getMessage());
			objResponse.put("message", "Database Error");
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
			return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toString())
					.type(MediaType.APPLICATION_JSON).build();
		} catch (JsonProcessingException e) {
			// Object mapper
			e.printStackTrace();

			logger.info("JsonProcessingException >> " + e.getMessage());
			objResponse.put("message", "Failed to parse JSON internally");
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
			return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toString())
					.type(MediaType.APPLICATION_JSON).build();
		} catch (IOException e) {
			e.printStackTrace();
			logger.info("IOException >> " + e.getMessage());
			objResponse.put("message", "Error when getting streaks ");
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
	 * Get accumulative information of a streak for a member, e.g  retruns all achievement info instead of achievementId
	 * 
	 * @param gameId   gameId
	 * @param memberId member id
	 * @param streakId  streak id
	 * @return HTTP Response with the returnString
	 */
	@GET
	@Path("/streaks/accumulative/{gameId}/{memberId}/{streakId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Game Selected"),
			@ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Game not found"),
			@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad Request"), })
	@ApiOperation(value = "", notes = "Select an Game")
	public Response getTransitiveStreakProgress(
			@ApiParam(value = "Game ID", required = true) @PathParam("gameId") String gameId,
			@ApiParam(value = "Member ID", required = true) @PathParam("memberId") String memberId,
			@ApiParam(value = "Streak ID", required = true) @PathParam("streakId") String streakId) {

		JSONObject objResponse = new JSONObject();
		Connection conn = null;

		Agent agent = Context.getCurrent().getMainAgent();
		if (agent instanceof AnonymousAgent) {
			return unauthorizedMessage();
		}
		try {
			conn = dbm.getConnection();
			if (!visualizationAccess.isGameIdExist(conn, gameId)) {
				logger.info("Game not found >> ");
				objResponse.put("message", "Game not found");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			if (!visualizationAccess.isMemberRegistered(conn, memberId)) {
				logger.info("Member ID not found >> ");
				objResponse.put("message", "Member ID not found");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			if (!visualizationAccess.isMemberRegisteredInGame(conn, memberId, gameId)) {
				logger.info("Member is not registered in Game >> ");
				objResponse.put("message", "Member is not registered in Game");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			if (!visualizationAccess.isMemberHasStreak(conn, gameId, memberId, streakId)) {
				logger.info("Error. member " + memberId + " does not have a streak " + streakId + ".");
				objResponse.put("message",
						"Member " + memberId + " does not have an streak " + streakId + ".");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
						(String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
						.type(MediaType.APPLICATION_JSON).build();
			}
			JSONObject outObj = visualizationAccess.getAccumulativeStreakData(conn, gameId, memberId, streakId);
			return Response.status(HttpURLConnection.HTTP_OK).entity(outObj.toString())
					.type(MediaType.APPLICATION_JSON).build();
		} catch (SQLException e) {
			e.printStackTrace();
			logger.info("SQLException >> " + e.getMessage());
			objResponse.put("message", "Database Error");
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
			return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toString())
					.type(MediaType.APPLICATION_JSON).build();
		} catch (IOException e) {
			e.printStackTrace();
			logger.info("IOException >> " + e.getMessage());
			objResponse.put("message", "Error when getting streaks ");
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
	
	
	@POST
	@Path("/pause/{gameId}/{memberId}/{streakId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = { @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Found a streak"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal Error"),
			@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized") })
	@ApiOperation(value = "getStreakWithId", notes = "Returns streak detail with specific ID", response = StreakModel.class)
	public Response pauseUserStreak(
			@ApiParam(value = "Game ID") @PathParam("gameId") String gameId,
			@ApiParam(value = "Member ID") @PathParam("memberId") String memberId,
			@ApiParam(value = "Streak ID") @PathParam("streakId") String streakId) {

		// Request log
		Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_99,
				"POST " + "gamification/streaks/pause/" + gameId + "/" + memberId+ "/" + streakId, true);
		long randomLong = new Random().nextLong(); // To be able to match

		Connection conn = null;

		JSONObject objResponse = new JSONObject();
		Agent agent = Context.getCurrent().getMainAgent();
		if (agent instanceof AnonymousAgent) {
			return unauthorizedMessage();
		}
		try {
			conn = dbm.getConnection();
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_16, "" + randomLong, true);

			try {
				if (!visualizationAccess.isGameIdExist(conn, gameId)) {
					objResponse.put("message", "Cannot get streak. Game not found");
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
							(String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
							.type(MediaType.APPLICATION_JSON).build();
				}
				if (!visualizationAccess.isMemberRegistered(conn, memberId)) {
					logger.info("Member ID not found >> ");
					objResponse.put("message", "Member ID not found");
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
							(String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
							.type(MediaType.APPLICATION_JSON).build();
				}
				if (!visualizationAccess.isMemberRegisteredInGame(conn, memberId, gameId)) {
					logger.info("Member is not registered in Game >> ");
					objResponse.put("message", "Member is not registered in Game");
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR,
							(String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toString())
							.type(MediaType.APPLICATION_JSON).build();
				}
				if (!visualizationAccess.isMemberHasStreak(conn, gameId, memberId, streakId)) {
					logger.info("Error. member " + memberId + " does not have a streak " + streakId + ".");
					objResponse.put("message",
							"Member " + memberId + " does not have an streak " + streakId + ".");
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
			visualizationAccess.pauseStreak(conn, gameId, memberId, streakId);
			return Response.status(HttpURLConnection.HTTP_OK).entity("Streak pasued successfully").type(MediaType.APPLICATION_JSON)
					.build();

		} catch (SQLException e) {
			e.printStackTrace();
			objResponse.put("message", "Cannot get streak. DB Error. " + e.getMessage());
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

}

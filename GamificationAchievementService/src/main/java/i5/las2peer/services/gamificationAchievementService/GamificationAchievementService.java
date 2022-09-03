package i5.las2peer.services.gamificationAchievementService;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.MediaType;

import org.apache.commons.fileupload.MultipartStream.MalformedStreamException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import i5.las2peer.logging.L2pLogger;
import i5.las2peer.restMapper.annotations.ServicePath;
import i5.las2peer.restMapper.RESTService;
import i5.las2peer.api.security.Agent;
import i5.las2peer.api.security.AnonymousAgent;
import i5.las2peer.api.security.UserAgent;
import i5.las2peer.api.ManualDeployment;
import i5.las2peer.api.logging.MonitoringEvent;
import i5.las2peer.api.Context;
import i5.las2peer.services.gamificationAchievementService.database.AchievementDAO;
import i5.las2peer.services.gamificationAchievementService.database.AchievementModel;
import i5.las2peer.services.gamificationAchievementService.database.DatabaseManager;
import i5.las2peer.services.gamificationAchievementService.helper.FormDataPart;
import i5.las2peer.services.gamificationAchievementService.helper.MultipartHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.Contact;
import io.swagger.annotations.Info;
import io.swagger.annotations.License;
import io.swagger.annotations.SwaggerDefinition;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.glassfish.jersey.media.multipart.FormDataParam;


/**
 * Gamification Achievement Service
 * 
 * This Gamification Achievement Service is to manage achievement element in Gamification Framework.
 * It uses the LAS2peer Web-Connector for RESTful access to it.
 * 
 * Note:
 * If you plan on using Swagger you should adapt the information below
 * in the ApiInfo annotation to suit your project.
 * If you do not intend to provide a Swagger documentation of your service API,
 * the entire ApiInfo annotation should be removed.
 * 
 */
@Api( value = "/gamification/achievements", authorizations = {
@Authorization(value = "achievements_auth")
})
@SwaggerDefinition(
		info = @Info(
				title = "Gamification Achievement Service",
				version = "0.1",
				description = "Gamification Achievement Service for Gamification Framework",
				termsOfService = "http://your-terms-of-service-url.com",
				contact = @Contact(
						name = "Muhammad Abduh Arifin",
						url = "dbis.rwth-aachen.de",
						email = "arifin@dbis.rwth-aachen.de"
				),
				license = @License(
						name = "your software license name",
						url = "http://your-software-license-url.com"
				)
		))
@ManualDeployment
@ServicePath("/gamification/achievements")
public class GamificationAchievementService extends RESTService {

	// instantiate the logger class
	private final L2pLogger logger = L2pLogger.getInstance(GamificationAchievementService.class.getName());
	/*
	 * Database configuration
	 */
	private String jdbcDriverClassName;
	private String jdbcLogin;
	private String jdbcPass;
	private String jdbcUrl;
	private String jdbcSchema;
	private DatabaseManager dbm;
	
	private AchievementDAO achievementAccess;
	
		
	// this header is not known to javax.ws.rs.core.HttpHeaders
	public static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";
	public static final String HEADER_CONTENT_TYPE = "Content-Type";
	// non HTTP standard headers
	public static final String HEADER_OWNERID = "ownerid";
	public static final String HEADER_CONTENT_DESCRIPTION = "Content-Description";

	public GamificationAchievementService() {
		// read and set properties values
		// IF THE SERVICE CLASS NAME IS CHANGED, THE PROPERTIES FILE NAME NEED TO BE CHANGED TOO!
		setFieldValues();
		dbm = new DatabaseManager(jdbcDriverClassName, jdbcLogin, jdbcPass, jdbcUrl, jdbcSchema);
		this.achievementAccess = new AchievementDAO();
	}
	
		/**
		 * Function to return http unauthorized message
		 * @return HTTP response unauthorized
		 */
		private Response unauthorizedMessage(){
			JSONObject objResponse = new JSONObject();
			objResponse.put("message", "You are not authorized");
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
			return Response.status(HttpURLConnection.HTTP_UNAUTHORIZED).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
		}
		
		
		/**
		 * Create a new achievement.
		 *
		 * @param gameId Game ID obtained from Gamification Game Service
		 * @param achievementid Achievement ID - String (20 chars)
		 * @param achievementname Achievement name - String (20 chars)
		 * @param achievementdesc Achievement Description - String (50 chars)
		 * @param achievementpointvalue Point Value Action - Integer
		 * @param achievementbadgeid The existing badge from Gamification Badge Service - String (20 chars)
		 * @param achievementnotifcheckStr Achievement Notification Boolean - Boolean - Option whether use notification or not. Must be null for 'false' and any other value for 'true'
		 * @param achievementnotifmessage Achievement Notification Message - String
		 *
		 * @return HTTP Response returned as JSON object
		 */
		@POST
		@Path("/{gameId}")
		@Consumes(MediaType.MULTIPART_FORM_DATA)
		@Produces(MediaType.APPLICATION_JSON)
		@ApiResponses(value = {
				@ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "{message:Achievement upload success (achievementid)}"),
				@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "{message:Cannot create achievement. Failed to add the achievement. achievement ID already exist!}"),
				@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "{message:Cannot create achievement. Cannot check whether game ID exist or not. Database error.}"),
				@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "{message:Cannot create achievement. Failed to upload (achievementid) }"),
				@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "{message:Cannot create achievement. Achievement ID cannot be null!}"),
				
				@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "{message:Cannot create achievement. Game not found}"),
				@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "{message:Cannot create achievement. Failed to upload (achievementid)."),
				@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "{message:You are not authorized")
		})
		@ApiOperation(value = "createNewAchievement",
					 notes = "A method to store a new achievement with details (achievement ID, achievement name, achievement description, achievement point value, achievement point id, achievement badge id")
		public Response createNewAchievement(
				@ApiParam(value = "Game ID to store a new achievement", required = true) @PathParam("gameId") String gameId,
				@ApiParam(value = "Achievement ID - String (20 chars)", required = true) @FormDataParam("achievementid") String achievementid,
				@ApiParam(value = "Achievement name - String (20 chars)", required = true) @FormDataParam("achievementname") String achievementname,
				@ApiParam(value = "Achievement Description - String (50 chars)") @FormDataParam("achievementdesc") String achievementdesc,
				@ApiParam(value = "Point Value Action - Integer") @FormDataParam("achievementpointvalue") int achievementpointvalue,
				@ApiParam(value = "The existing badge from Gamification Badge Service - String (20 chars)") @FormDataParam("achievementbadgeid") String achievementbadgeid,
				@ApiParam(value = "Achievement Notification Boolean - Boolean - Option whether use notification or not. NOTE: semantics are a little strange (because of backwards compatibility)! If the parameter is present, any value is considered as true. In order to set the value to value, you have to NOT send the parameter.")
					@FormDataParam("achievementnotificationcheck") String achievementnotifcheckStr,
				@ApiParam(value = "Achievement Notification Message - String") @FormDataParam("achievementnotificationmessage") String achievementnotifmessage
		)  {
			/*
			 * Legacy semantics of 'achievementnotificationcheck' param are the following:
			 * - If parameter has any non-null value (even blank string) -> true
			 * - Otherwise -> false
			 *
			 * TODO Consider breaking change and using default 'boolean' semantics (param needs to be string 'true' for true value)
			 */
			boolean achievementnotifcheck = achievementnotifcheckStr != null;
			
			// Request log
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_99, "POST " + "gamification/achievements/"+gameId, true);
			long randomLong = new Random().nextLong(); //To be able to match 
			
			// parse given multipart form data
			JSONObject objResponse = new JSONObject();

			Connection conn = null;
			
			String name = null;
			Agent agent = Context.getCurrent().getMainAgent();
			if (agent instanceof AnonymousAgent) {
				return unauthorizedMessage();
			}
			else if (agent instanceof UserAgent) {
				UserAgent userAgent = (UserAgent) Context.getCurrent().getMainAgent();
				name = userAgent.getLoginName();
			}
			else {
				name = agent.getIdentifier();
			}
			try {
				conn = dbm.getConnection();
				
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_14, ""+randomLong, true);
				
				// Check the existence of game ID
				try {
					if(!achievementAccess.isGameIdExist(conn,gameId)){
						logger.info("Game not found >> ");
						objResponse.put("message", "Cannot create achievement. Game not found");
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));						
						return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
						
					}
				} catch (SQLException e1) {
					e1.printStackTrace();
					objResponse.put("message", "Cannot create achievement. Cannot check whether game ID exist or not. Database error. " + e1.getMessage());
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
				}

				if (achievementid != null) {
					if(achievementAccess.isAchievementIdExist(conn,gameId, achievementid)){
						// Achievement id already exist
						objResponse.put("message", "Cannot create achievement. Failed to add the achievement. achievement ID already exist!");
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					}

					// treat blank badge ID as 'null' (no badge)
					if(achievementbadgeid.equals("")) {
						achievementbadgeid = null;
					}

					if (achievementnotifmessage == null) {
						achievementnotifmessage = ""; // default: empty message
					}

					AchievementModel achievement = new AchievementModel(achievementid, achievementname, achievementdesc, achievementpointvalue, achievementbadgeid, achievementnotifcheck, achievementnotifmessage);
					
					try{
						achievementAccess.addNewAchievement(conn,gameId, achievement);
						objResponse.put("message", "Achievement upload success (" + achievementid +")");
						
						// Mobsos Logger
						Context.getCurrent().monitorEvent(this,MonitoringEvent.SERVICE_CUSTOM_MESSAGE_15, ""+randomLong, true);
						Context.getCurrent().monitorEvent(this,MonitoringEvent.SERVICE_CUSTOM_MESSAGE_24, ""+name, true);
						Context.getCurrent().monitorEvent(this,MonitoringEvent.SERVICE_CUSTOM_MESSAGE_25, ""+gameId, true);
						return Response.status(HttpURLConnection.HTTP_CREATED).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
						
					} catch (SQLException e) {
						e.printStackTrace();
						objResponse.put("message", "Cannot create achievement. Failed to upload " + achievementid + ". " + e.getMessage());
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					}
				}
				else{
					objResponse.put("message", "Cannot create achievement. Achievement ID cannot be null!");
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
				}
				
				
			} catch (SQLException e) {
				e.printStackTrace();
				objResponse.put("message", "Cannot create achievement. Failed to upload " + achievementid + ". " + e.getMessage());
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();

			} catch (NullPointerException e){
				e.printStackTrace();
				objResponse.put("message", "Cannot create achievement. Failed to upload " + achievementid + ". " + e.getMessage());
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
			}
			 // always close connections
		    finally {
		      try {
		        conn.close();
		      } catch (SQLException e) {
		        logger.printStackTrace(e);
		      }
		    }
		}
		
		/**
		 * Get an achievement data with specific ID from database
		 * @param gameId Game ID obtained from Gamification Game Service
		 * @param achievementId Achievement id to be obtained
		 * @return HTTP Response returned Achievement Model {@link AchievementModel} as JSON object
		 * @see AchievementModel
		 */
		@GET
		@Path("/{gameId}/{achievementId}")
		@Produces(MediaType.APPLICATION_JSON)
		@ApiResponses(value = {
				@ApiResponse(code = HttpURLConnection.HTTP_OK, message = ""),
				@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "{message:Cannot get achievement detail. Cannot check whether game ID exist or not. Database error.}"),
				@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "{message:Cannot get achievement detail. JSON processing error}"),
				@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "{message:Cannot get achievement. Failed to fetch (achievementId)}"),
				
				@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "{message:Cannot get achievement detail. Game not found}"),
				@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "{message:Cannot get achievement detail. Achievement not found"),
				@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "{message:Achievement Null, Cannot find achievement with (achievementId)"),
				@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "{message:Cannot get achievement detail. DB Error."),
				@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "{message:You are not authorized")
		})
		@ApiOperation(value = "getAchievementWithId", 
					  notes = "Get achievement data with specified ID",
					  response = AchievementModel.class
					  )
		public Response getAchievementWithId(
				@ApiParam(value = "Game ID")@PathParam("gameId") String gameId,
				@ApiParam(value = "Achievement ID")@PathParam("achievementId") String achievementId)
		{
			
			// Request log
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_99, "GET " + "gamification/achievements/"+gameId+"/"+achievementId, true);
			long randomLong = new Random().nextLong(); //To be able to match 
			
			
			AchievementModel achievement = null;
			Connection conn = null;
			
			JSONObject objResponse = new JSONObject();
			String name = null;
			Agent agent = Context.getCurrent().getMainAgent();
			if (agent instanceof AnonymousAgent) {
				return unauthorizedMessage();
			}
			else if (agent instanceof UserAgent) {
				UserAgent userAgent = (UserAgent) Context.getCurrent().getMainAgent();
				name = userAgent.getLoginName();
			}
			else {
				name = agent.getIdentifier();
			}
			try {
				conn = dbm.getConnection();
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_16, ""+randomLong, true);
				
				try {
					
					try {
						if(!achievementAccess.isGameIdExist(conn,gameId)){
							objResponse.put("message", "Cannot get achievement detail. Game not found");
							Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
							return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
						}
					} catch (SQLException e1) {
						e1.printStackTrace();
						objResponse.put("message", "Cannot get achievement detail. Cannot check whether game ID exist or not. Database error. " + e1.getMessage());
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					}
					if(!achievementAccess.isAchievementIdExist(conn,gameId, achievementId)){
						objResponse.put("message", "Cannot get achievement detail. Achievement not found");
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					}
					achievement = achievementAccess.getAchievementWithId(conn,gameId, achievementId);
					if(achievement == null){
						objResponse.put("message", "Achievement Null, Cannot find achievement with " + achievementId);
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					}
					ObjectMapper objectMapper = new ObjectMapper();
			    	//Set pretty printing of json
			    	objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
			    	
			    	String achievementString = objectMapper.writeValueAsString(achievement);
			    	Context.getCurrent().monitorEvent(this,MonitoringEvent.SERVICE_CUSTOM_MESSAGE_17, ""+randomLong, true);
			    	Context.getCurrent().monitorEvent(this,MonitoringEvent.SERVICE_CUSTOM_MESSAGE_26, ""+name, true);
			    	Context.getCurrent().monitorEvent(this,MonitoringEvent.SERVICE_CUSTOM_MESSAGE_27, ""+gameId, true);
					return Response.status(HttpURLConnection.HTTP_OK).entity(achievementString).type(MediaType.APPLICATION_JSON).build();
				} 
				catch (SQLException e) {
					e.printStackTrace();
					objResponse.put("message", "Cannot get achievement detail. DB Error. " + e.getMessage());
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();

				}
				
			} catch (JsonProcessingException e) {
				e.printStackTrace();
				objResponse.put("message", "Cannot get achievement detail. JSON processing error. " + e.getMessage());
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(e.getMessage()).type(MediaType.APPLICATION_JSON).build();
			} catch (SQLException e) {
				e.printStackTrace();
				objResponse.put("message", "Cannot get achievement. Failed to fetch " + achievementId + ". " + e.getMessage());
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();

			}		 // always close connections
		    finally {
			      try {
			        conn.close();
			      } catch (SQLException e) {
			        logger.printStackTrace(e);
			      }
			    }
		}

		
		/**
		 * Update an achievement.
		 *
		 * @param gameId Game ID obtained from Gamification Game Service
		 * @param achievementId Achievement ID to be updated
		 * @param gameId Game ID obtained from Gamification Game Service
		 * @param achievementname Achievement name - String (20 chars)
		 * @param achievementdesc Achievement Description - String (50 chars)
		 * @param achievementpointvalue Point Value Action - Integer
		 * @param achievementbadgeid The existing badge from Gamification Badge Service - String (20 chars)
		 * @param achievementnotifcheckStr Achievement Notification Boolean - Boolean - Option whether use notification or not. Must be null for 'false' and any other value for 'true'
		 * @param achievementnotifmessage Achievement Notification Message - String
		 *
		 * @return HTTP Response returned as JSON object
		 */
		@PUT
		@Path("/{gameId}/{achievementId}")
		@Produces(MediaType.APPLICATION_JSON)
		@ApiResponses(value = {
				@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Achievement Updated"),
				@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Error occured"),
				@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad request"),
				@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized")
		})
		@ApiOperation(value = "updateAchievement",
					 notes = "A method to update an achievement with details (achievement ID, achievement name, achievement description, achievement point value, achievement point id, achievement badge id")
		public Response updateAchievement(
				@ApiParam(value = "Game ID to update an achievement", required = true) @PathParam("gameId") String gameId,
				@ApiParam(value = "Achievement ID to be updated", required = true) @PathParam("achievementId") String achievementId,
				@ApiParam(value = "Achievement name - String (20 chars)") @FormDataParam("achievementname") String achievementname,
				@ApiParam(value = "Achievement Description - String (50 chars)") @FormDataParam("achievementdesc") String achievementdesc,
				@ApiParam(value = "Point Value Action - Integer") @FormDataParam("achievementpointvalue") Integer achievementpointvalue,
				@ApiParam(value = "The existing badge from Gamification Badge Service - String (20 chars)") @FormDataParam("achievementbadgeid") String achievementbadgeid,
				@ApiParam(value = "Achievement Notification Boolean - Boolean - Option whether use notification or not. NOTE: semantics are a little strange (because of backwards compatibility)! If the parameter is present, any value is considered as true. In order to set the value to value, you have to NOT send the parameter.")
				@FormDataParam("achievementnotificationcheck") String achievementnotifcheckStr,
				@ApiParam(value = "Achievement Notification Message - String") @FormDataParam("achievementnotificationmessage") String achievementnotifmessage
		)  {
			/*
			 * Legacy semantics of 'achievementnotificationcheck' param are the following:
			 * - If parameter has any non-null value (even blank string) -> true
			 * - Otherwise -> false
			 *
			 * TODO Consider breaking change and using default 'boolean' semantics (param needs to be string 'true' for true value)
			 */
			boolean achievementnotifcheck = achievementnotifcheckStr != null;

			// Request log
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_99, "PUT " + "gamification/achievements/"+gameId+"/"+achievementId, true);
			long randomLong = new Random().nextLong(); //To be able to match 
					
			
			// parse given multipart form data
			JSONObject objResponse = new JSONObject();

			Connection conn = null;
			
			String name = null;
			Agent agent = Context.getCurrent().getMainAgent();
			if (agent instanceof AnonymousAgent) {
				return unauthorizedMessage();
			}
			else if (agent instanceof UserAgent) {
				UserAgent userAgent = (UserAgent) Context.getCurrent().getMainAgent();
				name = userAgent.getLoginName();
			}
			else {
				name = agent.getIdentifier();
			}
			try {
				conn = dbm.getConnection();
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_18, ""+randomLong, true);
				
				if (achievementId == null) {
					objResponse.put("message", "Cannot update achievement. Achievement ID cannot be null");
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
				}
				
				try {
					try {
						if(!achievementAccess.isGameIdExist(conn,gameId)){
							objResponse.put("message", "Cannot update achievement. Game not found");
							Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
							return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
						}
					} catch (SQLException e1) {
						e1.printStackTrace();
						objResponse.put("message", "Cannot update achievement. Cannot check whether game ID exist or not. Database error. " + e1.getMessage());
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					}
					if(!achievementAccess.isAchievementIdExist(conn,gameId, achievementId)){
						objResponse.put("message", "Cannot update achievement. Achievement not found");
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					}
					
					AchievementModel currentAchievement = achievementAccess.getAchievementWithId(conn,gameId, achievementId);

					if(currentAchievement == null){
						// currentAchievement is null
						objResponse.put("message", "Cannot update achievement. Achievement not found in database");
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					}

					if(achievementname != null) {
						currentAchievement.setName(achievementname);
					}

					if(achievementdesc!=null) {
						currentAchievement.setDescription(achievementdesc);
					}

					if (achievementpointvalue != null) {
						currentAchievement.setPointValue(achievementpointvalue);
					}

					if (achievementbadgeid != null) {
						logger.info(achievementbadgeid);
						if(achievementbadgeid.equals("")) {
							achievementbadgeid = null;
						}
						currentAchievement.setBadgeId(achievementbadgeid);
					}

					currentAchievement.useNotification(achievementnotifcheck);

					if (achievementnotifmessage!=null) {
						currentAchievement.setNotificationMessage(achievementnotifmessage);
					}

					achievementAccess.updateAchievement(conn,gameId, currentAchievement);
					objResponse.put("message", "Achievement updated");

					Context.getCurrent().monitorEvent(this,MonitoringEvent.SERVICE_CUSTOM_MESSAGE_19, ""+randomLong, true);
					Context.getCurrent().monitorEvent(this,MonitoringEvent.SERVICE_CUSTOM_MESSAGE_28, ""+name, true);
					Context.getCurrent().monitorEvent(this,MonitoringEvent.SERVICE_CUSTOM_MESSAGE_29, ""+gameId,true);
					return Response.status(HttpURLConnection.HTTP_OK).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();

				} catch (SQLException e) {
					e.printStackTrace();
					objResponse.put("message", "Cannot update achievement. DB Error. " + e.getMessage());
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
				}
				
			} catch (SQLException e) {
				e.printStackTrace();
				objResponse.put("message", "Cannot update achievement. DB Error. " + e.getMessage());
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
			}
			 // always close connections
		    finally {
		      try {
		        conn.close();
		      } catch (SQLException e) {
		        logger.printStackTrace(e);
		      }
		    }

		}
		

		/**
		 * Delete an achievement data with specified ID
		 * @param gameId Game ID obtained from Gamification Game Service
		 * @param achievementId Achievement ID to be deleted
		 * @return HTTP Response returned as JSON object
		 */
		@DELETE
		@Path("/{gameId}/{achievementId}")
		@Produces(MediaType.APPLICATION_JSON)
		@ApiResponses(value = {
				@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Achievement Delete Success"),
				@ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Achievements not found"),
				@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad Request"),
		})
		@ApiOperation(value = "deleteAchievement",
					  notes = "Delete an achievement")
		public Response deleteAchievement(@PathParam("gameId") String gameId,
									 @PathParam("achievementId") String achievementId)
		{
			
			// Request log
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_99, "DELETE " + "gamification/achievements/"+gameId+"/"+achievementId, true);		
			long randomLong = new Random().nextLong(); //To be able to match 

			Connection conn = null;
			JSONObject objResponse = new JSONObject();
			String name = null;
			Agent agent = Context.getCurrent().getMainAgent();
			if (agent instanceof AnonymousAgent) {
				return unauthorizedMessage();
			}
			else if (agent instanceof UserAgent) {
				UserAgent userAgent = (UserAgent) Context.getCurrent().getMainAgent();
				name = userAgent.getLoginName();
			}
			else {
				name = agent.getIdentifier();
			}
			try {
				conn = dbm.getConnection();
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_20, ""+randomLong, true);
				
				try {
					if(!achievementAccess.isGameIdExist(conn,gameId)){
						objResponse.put("message", "Cannot delete achievement. Game not found");
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					}
				} catch (SQLException e1) {
					e1.printStackTrace();
					objResponse.put("message", "Cannot delete achievement. Cannot check whether game ID exist or not. Database error. " + e1.getMessage());
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
				}
				if(!achievementAccess.isAchievementIdExist(conn,gameId, achievementId)){
					objResponse.put("message", "Cannot delete achievement. Achievement not found");
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
				}
				achievementAccess.deleteAchievement(conn,gameId, achievementId);
				
				objResponse.put("message", "Cannot delete achievement. Achievement Deleted");
				Context.getCurrent().monitorEvent(this,MonitoringEvent.SERVICE_CUSTOM_MESSAGE_21, ""+randomLong, true);
				Context.getCurrent().monitorEvent(this,MonitoringEvent.SERVICE_CUSTOM_MESSAGE_30, ""+name, true);
				Context.getCurrent().monitorEvent(this,MonitoringEvent.SERVICE_CUSTOM_MESSAGE_31, ""+gameId, true);
				return Response.status(HttpURLConnection.HTTP_OK).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();

			} catch (SQLException e) {
				
				e.printStackTrace();
				objResponse.put("message", "Cannot delete achievement. Cannot delete Achievement. " + e.getMessage());
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
			}
			 // always close connections
		    finally {
		      try {
		        conn.close();
		      } catch (SQLException e) {
		        logger.printStackTrace(e);
		      }
		    }
		}
		
		
		/**
		 * Get a list of achievements from database, support the features to do pagination and search
		 * @param gameId Game ID obtained from Gamification Game Service
		 * @param currentPage current cursor page
		 * @param windowSize size of fetched data (use -1 to fetch all data)
		 * @param searchPhrase search word
		 * @return HTTP Response returned as JSON object
		 */
		@GET
		@Path("/{gameId}")
		@Produces(MediaType.APPLICATION_JSON)
		@ApiResponses(value = {
				@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Found a list of achievements"),
				@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal Error"),
				@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized")})
		@ApiOperation(value = "getAchievementList", 
					  notes = "Returns a list of achievements",
					  response = AchievementModel.class,
					  responseContainer = "List"
					  )
		public Response getAchievementList(
				@ApiParam(value = "Game ID to return")@PathParam("gameId") String gameId,
				@ApiParam(value = "Page number for retrieving data")@QueryParam("current") int currentPage,
				@ApiParam(value = "Number of data size")@QueryParam("rowCount") int windowSize,
				@ApiParam(value = "Search phrase parameter")@QueryParam("searchPhrase") String searchPhrase)
		{
			
			// Request log
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_99, "GET " + "gamification/achievements/"+gameId, true);		
			long randomLong = new Random().nextLong(); //To be able to match 

			List<AchievementModel> achs = null;
			Connection conn = null;
			JSONObject objResponse = new JSONObject();
			String name = null;
			Agent agent = Context.getCurrent().getMainAgent();
			if (agent instanceof AnonymousAgent) {
				return unauthorizedMessage();
			}
			else if (agent instanceof UserAgent) {
				UserAgent userAgent = (UserAgent) Context.getCurrent().getMainAgent();
				name = userAgent.getLoginName();
			}
			else {
				name = agent.getIdentifier();
			}
			try {
				conn = dbm.getConnection();
				Context.getCurrent().monitorEvent(this,MonitoringEvent.SERVICE_CUSTOM_MESSAGE_46, ""+randomLong, true);
				
				try {
					if(!achievementAccess.isGameIdExist(conn,gameId)){
						objResponse.put("message", "Cannot get achievements. Game not found");
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					}
				} catch (SQLException e1) {
					e1.printStackTrace();
					objResponse.put("message", "Cannot get achievements. Cannot check whether game ID exist or not. Database error. " + e1.getMessage());
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
				}
				int offset = (currentPage - 1) * windowSize;
				int totalNum = achievementAccess.getNumberOfAchievements(conn,gameId);
				
				if(windowSize == -1){
					offset = 0;
					windowSize = totalNum;
				}
				
				achs = achievementAccess.getAchievementsWithOffsetAndSearchPhrase(conn,gameId, offset, windowSize, searchPhrase);

				ObjectMapper objectMapper = new ObjectMapper();
		    	//Set pretty printing of json
		    	objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		    	
		    	String achievementString = objectMapper.writeValueAsString(achs);
				JSONArray achievementArray = (JSONArray) JSONValue.parse(achievementString);
				logger.info(achievementArray.toJSONString());
				objResponse.put("current", currentPage);
				objResponse.put("rowCount", windowSize);
				objResponse.put("rows", achievementArray);
				objResponse.put("total", totalNum);
				
				Context.getCurrent().monitorEvent(this,MonitoringEvent.SERVICE_CUSTOM_MESSAGE_47, ""+randomLong, true);
				Context.getCurrent().monitorEvent(this,MonitoringEvent.SERVICE_CUSTOM_MESSAGE_48, ""+name, true);
				Context.getCurrent().monitorEvent(this,MonitoringEvent.SERVICE_CUSTOM_MESSAGE_49, ""+gameId, true);
				return Response.status(HttpURLConnection.HTTP_OK).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
				
			} catch (SQLException e) {
				e.printStackTrace();
				objResponse.put("message", "Cannot get achievements. Database error. " + e.getMessage());
				// return HTTP Response on error
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
			} catch (JsonProcessingException e) {
				e.printStackTrace();
				objResponse.put("message", "Cannot get achievements. JSON processing error. " + e.getMessage());
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(e.getMessage()).type(MediaType.APPLICATION_JSON).build();

			}
			 // always close connections
		    finally {
		      try {
		        conn.close();
		      } catch (SQLException e) {
		        logger.printStackTrace(e);
		      }
		    }
		}
		
		
		// RMI
		/**
		 * Function to be used by RMI, it returns achievement data with specific ID
		 * @param gameId Game ID obtained from Gamification Game Service
		 * @param achievementId Achievement ID to be retrieved
		 * @return Serialized JSON achievement data or null
		 */
		public String getAchievementWithIdRMI(String gameId, String achievementId)  {
			AchievementModel achievement;

			Connection conn = null;
			try {
				conn = dbm.getConnection();
				
				achievement = achievementAccess.getAchievementWithId(conn,gameId, achievementId);
				if(achievement == null){
					return null;
				}
				ObjectMapper objectMapper = new ObjectMapper();
		    	//Set pretty printing of json
		    	objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		    	
		    	String achievementString = objectMapper.writeValueAsString(achievement);
		    	return achievementString;
			} catch (SQLException e) {
				e.printStackTrace();
				return null;
			} catch (JsonProcessingException e) {
				e.printStackTrace();
				return null;
			}
			 // always close connections
		    finally {
		      try {
		        conn.close();
		      } catch (SQLException e) {
		        logger.printStackTrace(e);
		      }
		    }
			
		}
}

package i5.las2peer.services.gamificationAchievementService;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.MediaType;



import org.apache.commons.fileupload.MultipartStream.MalformedStreamException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import i5.las2peer.api.Context;
import i5.las2peer.logging.L2pLogger;
import i5.las2peer.api.logging.MonitoringEvent;
import i5.las2peer.restMapper.RESTService;
//import i5.las2peer.restMapper.HttpResponse;
//import i5.las2peer.restMapper.MediaType;
//import i5.las2peer.restMapper.RESTMapper;
//import i5.las2peer.restMapper.annotations.ContentParam;
//import i5.las2peer.restMapper.annotations.Version;
//import i5.las2peer.restMapper.tools.ValidationResult;
//import i5.las2peer.restMapper.tools.XMLCheck;
import i5.las2peer.api.security.UserAgent;
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


// TODO Describe your own service
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

@Path("/gamification/achievements")
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

	@Override
	  protected void initResources() {
	    //getResourceConfig().register(Resource.class);
		 System.out.println("jojojoj");
	  }
//	
//	@Path("/") // this is the root resource
//	  public static class Resource {
//	    // put here all your service methods
//		
		/**
		 * Function to return http unauthorized message
		 * @return HTTP response unauthorized
		 */
		private Response unauthorizedMessage(){
			JSONObject objResponse = new JSONObject();
			objResponse.put("message", "You are not authorized");
			L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
			return Response.status(HttpURLConnection.HTTP_UNAUTHORIZED).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
//			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_UNAUTHORIZED);
		}
		
		
		/**
		 * Create a new achievement. 
		 * Name attribute for form data : 
		 * <ul>
		 * 	<li>achievementid - Achievement ID - String (20 chars)
		 *  <li>achievementname - Achievement name - String (20 chars)
		 *  <li>achievementdesc - Achievement Description - String (50 chars)
		 *  <li>achievementpointvalue - Point Value Action - Integer
		 *  <li>achievementbadgeid - The existing badge from Gamification Badge Service - String (20 chars)
		 *  <li>achievementnotificationcheck - Achievement Notification Boolean - Boolean - Option whether use notification or not
		 *  <li>achievementnotificationmessage - Achievement Notification Message - String
		 * </ul>
		 * @param gameId Game ID obtained from Gamification Game Service
		 * @param formData Form data with multipart/form-data type
		 * @param contentType Content type (implicitly sent in header)
		 * @return HTTP Response returned as JSON object
		 */
		@POST
		@Path("/{gameId}")
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
				@ApiParam(value = "Content-type in header", required = true)@HeaderParam(value = HttpHeaders.CONTENT_TYPE) String contentType, 
				@ApiParam(value = "Achievement detail in multiple/form-data type", required = true) byte[] formData)  {
			
			// Request log
			L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_99,Context.getCurrent().getMainAgent(), "POST " + "gamification/achievements/"+gameId);
			long randomLong = new Random().nextLong(); //To be able to match 
			
			// parse given multipart form data
			JSONObject objResponse = new JSONObject();
			String achievementid = null;
			String achievementname = null;
			String achievementdesc = null;
			int achievementpointvalue = 0;
			String achievementbadgeid = null;
			
			boolean achievementnotifcheck = false;
			String achievementnotifmessage = null;
			Connection conn = null;
			
			UserAgent userAgent = (UserAgent) Context.getCurrent().getMainAgent();
			String name = userAgent.getLoginName();
			if(name.equals("anonymous")){
				return unauthorizedMessage();
			}
			try {
				conn = dbm.getConnection();
				
				L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_14,Context.getCurrent().getMainAgent(), ""+randomLong);
				
				// Check the existence of game ID
				try {
					if(!achievementAccess.isGameIdExist(conn,gameId)){
						logger.info("Game not found >> ");
						objResponse.put("message", "Cannot create achievement. Game not found");
						L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
						
						return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
						//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
					}
				} catch (SQLException e1) {
					e1.printStackTrace();
					objResponse.put("message", "Cannot create achievement. Cannot check whether game ID exist or not. Database error. " + e1.getMessage());
					L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
				}
				
				// Parse content of form
				Map<String, FormDataPart> parts = MultipartHelper.getParts(formData, contentType);
				FormDataPart partAchievementID = parts.get("achievementid");
				if (partAchievementID != null) {
					achievementid = partAchievementID.getContent();
					
					if(achievementAccess.isAchievementIdExist(conn,gameId, achievementid)){
						// Achievement id already exist
						objResponse.put("message", "Cannot create achievement. Failed to add the achievement. achievement ID already exist!");
						L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
						//return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_INTERNAL_ERROR);

					}
					FormDataPart partAchievementName = parts.get("achievementname");
					if (partAchievementName != null) {
						achievementname = partAchievementName.getContent();
					}
					FormDataPart partAchievementDesc = parts.get("achievementdesc");
					if (partAchievementDesc != null) {
						// optional description text input form element
						achievementdesc = partAchievementDesc.getContent();
					}
					FormDataPart partAchievementPV = parts.get("achievementpointvalue");
					if (partAchievementPV != null) {
						// optional description text input form element
						achievementpointvalue =  Integer.parseInt(partAchievementPV.getContent());
					}

					FormDataPart partAchievementBID = parts.get("achievementbadgeid");

					if (partAchievementBID != null) {
						// optional description text input form element
						achievementbadgeid = partAchievementBID.getContent();
					}
					if(achievementbadgeid.equals("")){
						achievementbadgeid = null;
					}
					
					FormDataPart partNotificationCheck = parts.get("achievementnotificationcheck");
					if (partNotificationCheck != null) {
						// checkbox is checked
						achievementnotifcheck = true;
						
					}else{
						achievementnotifcheck = false;
					}
					
					FormDataPart partNotificationMsg = parts.get("achievementnotificationmessage");
					if (partNotificationMsg != null) {
						achievementnotifmessage = partNotificationMsg.getContent();
					}else{
						achievementnotifmessage = "";
					}
					AchievementModel achievement = new AchievementModel(achievementid, achievementname, achievementdesc, achievementpointvalue, achievementbadgeid, achievementnotifcheck, achievementnotifmessage);
					
					try{
						achievementAccess.addNewAchievement(conn,gameId, achievement);
						objResponse.put("message", "Achievement upload success (" + achievementid +")");
						
						// Mobsos Logger
						L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_15,Context.getCurrent().getMainAgent(), ""+randomLong);
						L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_24,Context.getCurrent().getMainAgent(), ""+name);
						L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_25,Context.getCurrent().getMainAgent(), ""+gameId);
						return Response.status(HttpURLConnection.HTTP_CREATED).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
						//return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_CREATED);

					} catch (SQLException e) {
						e.printStackTrace();
						objResponse.put("message", "Cannot create achievement. Failed to upload " + achievementid + ". " + e.getMessage());
						L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
						//return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_INTERNAL_ERROR);
					}
				}
				else{
					objResponse.put("message", "Cannot create achievement. Achievement ID cannot be null!");
					L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					//return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_INTERNAL_ERROR);
				}
				
				
			} catch (MalformedStreamException e) {
				// the stream failed to follow required syntax
				objResponse.put("message", "Cannot create achievement. Failed to upload " + achievementid + ". " + e.getMessage());
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
				//return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_BAD_REQUEST);

			} catch (IOException e) {
				// a read or write error occurred
				objResponse.put("message", "Cannot create achievement. Failed to upload " + achievementid + ". " + e.getMessage());
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
				//return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_INTERNAL_ERROR);

			} catch (SQLException e) {
				e.printStackTrace();
				objResponse.put("message", "Cannot create achievement. Failed to upload " + achievementid + ". " + e.getMessage());
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
				//return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_INTERNAL_ERROR);

			}
			catch (NullPointerException e){
				e.printStackTrace();
				objResponse.put("message", "Cannot create achievement. Failed to upload " + achievementid + ". " + e.getMessage());
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
				//return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_INTERNAL_ERROR);
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
			L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_99,Context.getCurrent().getMainAgent(), "GET " + "gamification/achievements/"+gameId+"/"+achievementId);
			long randomLong = new Random().nextLong(); //To be able to match 
			
			
			AchievementModel achievement = null;
			Connection conn = null;
			
			JSONObject objResponse = new JSONObject();
			UserAgent userAgent = (UserAgent) Context.getCurrent().getMainAgent();
			String name = userAgent.getLoginName();
			if(name.equals("anonymous")){
				return unauthorizedMessage();
			}
			try {
				conn = dbm.getConnection();
				L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_16,Context.getCurrent().getMainAgent(), ""+randomLong);
				
				try {
					
					try {
						if(!achievementAccess.isGameIdExist(conn,gameId)){
							objResponse.put("message", "Cannot get achievement detail. Game not found");
							L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
							return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
							//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
						}
					} catch (SQLException e1) {
						e1.printStackTrace();
						objResponse.put("message", "Cannot get achievement detail. Cannot check whether game ID exist or not. Database error. " + e1.getMessage());
						L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
						//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
					}
					if(!achievementAccess.isAchievementIdExist(conn,gameId, achievementId)){
						objResponse.put("message", "Cannot get achievement detail. Achievement not found");
						L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
						//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
					}
					achievement = achievementAccess.getAchievementWithId(conn,gameId, achievementId);
					if(achievement == null){
						objResponse.put("message", "Achievement Null, Cannot find achievement with " + achievementId);
						L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
						//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
					}
					ObjectMapper objectMapper = new ObjectMapper();
			    	//Set pretty printing of json
			    	objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
			    	
			    	String achievementString = objectMapper.writeValueAsString(achievement);
			    	L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_17,Context.getCurrent().getMainAgent(), ""+randomLong);
			    	L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_26,Context.getCurrent().getMainAgent(), ""+name);
					L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_27,Context.getCurrent().getMainAgent(), ""+gameId);
					return Response.status(HttpURLConnection.HTTP_OK).entity(achievementString).type(MediaType.APPLICATION_JSON).build();
					//return new HttpResponse(achievementString, HttpURLConnection.HTTP_OK);
				} catch (SQLException e) {
					e.printStackTrace();
					objResponse.put("message", "Cannot get achievement detail. DB Error. " + e.getMessage());
					L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);

				}
				
			} catch (JsonProcessingException e) {
				e.printStackTrace();
				objResponse.put("message", "Cannot get achievement detail. JSON processing error. " + e.getMessage());
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(e.getMessage()).type(MediaType.APPLICATION_JSON).build();
				//return new HttpResponse(e.getMessage(), HttpURLConnection.HTTP_INTERNAL_ERROR);
			} catch (SQLException e) {
				e.printStackTrace();
				objResponse.put("message", "Cannot get achievement. Failed to fetch " + achievementId + ". " + e.getMessage());
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
				//return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_INTERNAL_ERROR);

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
		 * Name attribute for form data : 
		 * <ul>
		 * 	<li>achievementid - Achievement ID - String (20 chars)
		 *  <li>achievementname - Achievement name - String (20 chars)
		 *  <li>achievementdesc - Achievement Description - String (50 chars)
		 *  <li>achievementpointvalue - Point Value Action - Integer
		 *  <li>achievementbadgeid - The existing badge from Gamification Badge Service - String (20 chars)
		 *  <li>achievementnotificationcheck - Achievement Notification Boolean - Boolean - Option whether use notification or not
		 *  <li>achievementnotificationmessage - Achievement Notification Message - String
		 * </ul>
		 * @param gameId Game ID obtained from Gamification Game Service
		 * @param achievementId Achievement ID to be updated
		 * @param formData Form data with multipart/form-data type
		 * @param contentType Content type (implicitly sent in header)
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
				@PathParam("achievementId") String achievementId,
				@ApiParam(value = "Achievement data in multiple/form-data type", required = true)@HeaderParam(value = HttpHeaders.CONTENT_TYPE) String contentType, 
				byte[] formData)  {
			
			// Request log
			L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_99,Context.getCurrent().getMainAgent(), "PUT " + "gamification/achievements/"+gameId+"/"+achievementId);
			long randomLong = new Random().nextLong(); //To be able to match 
					
			
			// parse given multipart form data
			JSONObject objResponse = new JSONObject();

			String achievementname = null;
			String achievementdesc = null;
			int achievementpointvalue = 0;
			String achievementbadgeid = null;
			
			String achievementnotifmessage = null;
			Connection conn = null;
			
			UserAgent userAgent = (UserAgent) Context.getCurrent().getMainAgent();
			String name = userAgent.getLoginName();
			if(name.equals("anonymous")){
				return unauthorizedMessage();
			}
			try {
				conn = dbm.getConnection();
				L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_18,Context.getCurrent().getMainAgent(), ""+randomLong);
				
				Map<String, FormDataPart> parts = MultipartHelper.getParts(formData, contentType);
				
				if (achievementId == null) {
					objResponse.put("message", "Cannot update achievement. Achievement ID cannot be null");
					L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
				}
				
				try {
					try {
						if(!achievementAccess.isGameIdExist(conn,gameId)){
							objResponse.put("message", "Cannot update achievement. Game not found");
							L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
							return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
							//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
						}
					} catch (SQLException e1) {
						e1.printStackTrace();
						objResponse.put("message", "Cannot update achievement. Cannot check whether game ID exist or not. Database error. " + e1.getMessage());
						L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
						//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
					}
					if(!achievementAccess.isAchievementIdExist(conn,gameId, achievementId)){
						objResponse.put("message", "Cannot update achievement. Achievement not found");
						L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
						//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
					}
					
					AchievementModel currentAchievement = achievementAccess.getAchievementWithId(conn,gameId, achievementId);

					if(currentAchievement == null){
						// currentAchievement is null
						objResponse.put("message", "Cannot update achievement. Achievement not found in database");
						L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
						//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
					}
					
					FormDataPart partName = parts.get("achievementname");
					if (partName != null) {
						achievementname = partName.getContent();
						
						if(achievementname != null){
							currentAchievement.setName(achievementname);
						}
					}
					FormDataPart partDesc = parts.get("achievementdesc");
					if (partDesc != null) {
						// optional description text input form element
						achievementdesc = partDesc.getContent();
						if(achievementdesc!=null){
							currentAchievement.setDescription(achievementdesc);
						}
					}
					FormDataPart partPV = parts.get("achievementpointvalue");
					if (partPV != null) {
						// optional description text input form element
						achievementpointvalue = Integer.parseInt(partPV.getContent());
						currentAchievement.setPointValue(achievementpointvalue);
						
					}
					
					FormDataPart partBID = parts.get("achievementbadgeid");
					if (partBID != null) {
						// optional description text input form element
						achievementbadgeid = partBID.getContent();
						
						logger.info(achievementbadgeid);
						if(achievementbadgeid!=null){
							if(achievementbadgeid.equals("")){
								achievementbadgeid = null;
							}
							currentAchievement.setBadgeId(achievementbadgeid);
						}
					}
					FormDataPart partNotificationCheck = parts.get("achievementnotificationcheck");
					if (partNotificationCheck != null) {
						// checkbox is checked
						currentAchievement.useNotification(true);
					}else{
						currentAchievement.useNotification(false);
					}
					
					FormDataPart partNotificationMsg = parts.get("achievementnotificationmessage");
					if (partNotificationMsg != null) {
						achievementnotifmessage = partNotificationMsg.getContent();
						if(achievementnotifmessage!=null){
							currentAchievement.setNotificationMessage(achievementnotifmessage);
						}
					}
					achievementAccess.updateAchievement(conn,gameId, currentAchievement);
					objResponse.put("message", "Achievement updated");
					L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_19,Context.getCurrent().getMainAgent(), ""+randomLong);
					L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_28,Context.getCurrent().getMainAgent(), ""+name);
					L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_29,Context.getCurrent().getMainAgent(), ""+gameId);
					return Response.status(HttpURLConnection.HTTP_OK).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_OK);

				} catch (SQLException e) {
					e.printStackTrace();
					objResponse.put("message", "Cannot update achievement. DB Error. " + e.getMessage());
					L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);

				}
				
			} catch (MalformedStreamException e) {
				// the stream failed to follow required syntax
				objResponse.put("message", "Cannot update achievement. Failed to upload " + achievementId + ". "+e.getMessage());
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
				//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			} catch (IOException e) {
				// a read or write error occurred
				objResponse.put("message", "Cannot update achievement. Failed to upload " + achievementId + ".");
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
				//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
			} catch (SQLException e) {
				e.printStackTrace();
				objResponse.put("message", "Cannot update achievement. DB Error. " + e.getMessage());
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
				//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);

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
			L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_99,Context.getCurrent().getMainAgent(), "DELETE " + "gamification/achievements/"+gameId+"/"+achievementId);		
			long randomLong = new Random().nextLong(); //To be able to match 

			Connection conn = null;
			JSONObject objResponse = new JSONObject();
			UserAgent userAgent = (UserAgent) Context.getCurrent().getMainAgent();
			String name = userAgent.getLoginName();
			if(name.equals("anonymous")){
				return unauthorizedMessage();
			}
			try {
				conn = dbm.getConnection();
				L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_20,Context.getCurrent().getMainAgent(), ""+randomLong);
				
				try {
					if(!achievementAccess.isGameIdExist(conn,gameId)){
						objResponse.put("message", "Cannot delete achievement. Game not found");
						L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
						//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
					}
				} catch (SQLException e1) {
					e1.printStackTrace();
					objResponse.put("message", "Cannot delete achievement. Cannot check whether game ID exist or not. Database error. " + e1.getMessage());
					L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
				}
				if(!achievementAccess.isAchievementIdExist(conn,gameId, achievementId)){
					objResponse.put("message", "Cannot delete achievement. Achievement not found");
					L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
				}
				achievementAccess.deleteAchievement(conn,gameId, achievementId);
				
				objResponse.put("message", "Cannot delete achievement. Achievement Deleted");
				L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_21,Context.getCurrent().getMainAgent(), ""+randomLong);
				L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_30,Context.getCurrent().getMainAgent(), ""+name);
				L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_31,Context.getCurrent().getMainAgent(), ""+gameId);
				return Response.status(HttpURLConnection.HTTP_OK).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
				//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_OK);

			} catch (SQLException e) {
				
				e.printStackTrace();
				objResponse.put("message", "Cannot delete achievement. Cannot delete Achievement. " + e.getMessage());
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
				//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
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
		
		// TODO Batch Processing  --------------------------------------
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
			L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_99,Context.getCurrent().getMainAgent(), "GET " + "gamification/achievements/"+gameId);		
			long randomLong = new Random().nextLong(); //To be able to match 

			List<AchievementModel> achs = null;
			Connection conn = null;
			JSONObject objResponse = new JSONObject();
			UserAgent userAgent = (UserAgent) Context.getCurrent().getMainAgent();
			String name = userAgent.getLoginName();
			if(name.equals("anonymous")){
				return unauthorizedMessage();
			}
			try {
				conn = dbm.getConnection();
				L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_46,Context.getCurrent().getMainAgent(), ""+randomLong);
				
				try {
					if(!achievementAccess.isGameIdExist(conn,gameId)){
						objResponse.put("message", "Cannot get achievements. Game not found");
						L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
						//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
					}
				} catch (SQLException e1) {
					e1.printStackTrace();
					objResponse.put("message", "Cannot get achievements. Cannot check whether game ID exist or not. Database error. " + e1.getMessage());
					L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
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
				
				L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_47,Context.getCurrent().getMainAgent(), ""+randomLong);
				L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_48,Context.getCurrent().getMainAgent(), ""+name);
				L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_49,Context.getCurrent().getMainAgent(), ""+gameId);
				return Response.status(HttpURLConnection.HTTP_OK).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
				//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_OK);
				
			} catch (SQLException e) {
				e.printStackTrace();
				objResponse.put("message", "Cannot get achievements. Database error. " + e.getMessage());
				// return HTTP Response on error
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
				//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
				objResponse.put("message", "Cannot get achievements. JSON processing error. " + e.getMessage());
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(e.getMessage()).type(MediaType.APPLICATION_JSON).build();
				//return new HttpResponse(e.getMessage(), HttpURLConnection.HTTP_INTERNAL_ERROR);

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
				//logger.warning("Get Achievement with ID RMI failed. " + e.getMessage());
			} catch (JsonProcessingException e) {
				e.printStackTrace();
				return null;
				//logger.warning("Get Achievement with ID RMI failed. " + e.getMessage());
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
		
//	  }

}

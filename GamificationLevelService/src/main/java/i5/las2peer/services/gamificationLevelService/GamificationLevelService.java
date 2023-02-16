package i5.las2peer.services.gamificationLevelService;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Random;

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

import i5.las2peer.logging.L2pLogger;
import i5.las2peer.restMapper.RESTService;
import i5.las2peer.restMapper.annotations.ServicePath;
import i5.las2peer.api.Context;
import i5.las2peer.api.logging.MonitoringEvent;
import i5.las2peer.api.ManualDeployment;
import i5.las2peer.api.security.Agent;
import i5.las2peer.api.security.AnonymousAgent;
import i5.las2peer.api.security.UserAgent;
import i5.las2peer.services.gamificationLevelService.database.LevelDAO;
import i5.las2peer.services.gamificationLevelService.database.LevelModel;
import i5.las2peer.services.gamificationLevelService.database.DatabaseManager;
import i5.las2peer.services.gamificationLevelService.helper.FormDataPart;
import i5.las2peer.services.gamificationLevelService.helper.MultipartHelper;
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
import net.minidev.json.JSONValue;


/**
 * Gamification Level Service
 * 
 * This is Gamification Level service to manage level element in Gamification Framework
 * It uses the LAS2peer Web-Connector for RESTful access to it.
 * 
 * Note:
 * If you plan on using Swagger you should adapt the information below
 * in the ApiInfo annotation to suit your project.
 * If you do not intend to provide a Swagger documentation of your service API,
 * the entire ApiInfo annotation should be removed.
 * 
 */

@Api( value = "/levels", authorizations = {
		@Authorization(value = "levels_auth",
		scopes = {
			@AuthorizationScope(scope = "write:levels", description = "modify levels in your game"),
			@AuthorizationScope(scope = "read:levels", description = "read your levels")
				  })
}, tags = "levels")
@SwaggerDefinition(
		info = @Info(
				title = "Gamification Level Service",
				version = "0.1",
				description = "Gamification Level Service for Gamification Framework",
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
@ServicePath("/gamification/levels")
public class GamificationLevelService extends RESTService {

	// instantiate the logger class
	private final L2pLogger logger = L2pLogger.getInstance(GamificationLevelService.class.getName());
	/*
	 * Database configuration
	 */
	private String jdbcDriverClassName;
	private String jdbcLogin;
	private String jdbcPass;
	private String jdbcUrl;
	private String jdbcSchema;
	private DatabaseManager dbm;
	private LevelDAO levelAccess;
	
	// this header is not known to javax.ws.rs.core.HttpHeaders
	public static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";
	public static final String HEADER_CONTENT_TYPE = "Content-Type";
	// non HTTP standard headers
	public static final String HEADER_OWNERID = "ownerid";
	public static final String HEADER_CONTENT_DESCRIPTION = "Content-Description";

	public GamificationLevelService() {
		// read and set properties values
		// IF THE SERVICE CLASS NAME IS CHANGED, THE PROPERTIES FILE NAME NEED TO BE CHANGED TOO!
		setFieldValues();		
		dbm = new DatabaseManager(jdbcDriverClassName, jdbcLogin, jdbcPass, jdbcUrl, jdbcSchema);
		this.levelAccess = new LevelDAO();

	}

		  /**
			 * Function to return http unauthorized message
			 * @return HTTP Response unauthorized
			 */
			private Response unauthorizedMessage(){
				JSONObject objResponse = new JSONObject();
				objResponse.put("message", "You are not authorized");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_UNAUTHORIZED).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
			}
			
			
			
			// //////////////////////////////////////////////////////////////////////////////////////
			// Service methods.
			// //////////////////////////////////////////////////////////////////////////////////////

			// //////////////////////////////////////////////////////////////////////////////////////
			// Level PART --------------------------------------
			// //////////////////////////////////////////////////////////////////////////////////////
		
			
			/**
			 * Post a new level.
			 * Name attribute for form data : 
			 * <ul>
			 * 	<li>levelnum - Level Number - String (20 chars)
			 *  <li>levelname - Level name - String (20 chars)
			 *  <li>levelpointvalue - Point Value Level - Integer
			 *  <li>levelnotificationcheck - Level Notification Boolean - Boolean - Option whether use notification or not
			 *  <li>levelnotificationmessage - Level Notification Message - String
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
					@ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "{\"status\": 3, \"message\": \"Level upload success ( (levelnum) )\"}"),
					@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "{\"status\": 3, \"message\": \"Failed to upload (levelnum)\"}"),
					@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "{\"status\": 1, \"message\": \"Failed to add the level. levelnum already exist!\"}"),
					@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "{\"status\": =, \"message\": \"Level number cannot be null!\"}"),
					@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "{\"status\": 2, \"message\": \"File content null. Failed to upload (levelnum)\"}"),
					@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "{\"status\": 2, \"message\": \"Failed to upload (levelnum)\"}"),
					@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "{\"status\": 3, \"message\": \"Level upload success ( (levelnum) )}")
			})
			@ApiOperation(value = "createLevel",
						 notes = "A method to store a new level with details (Level number, level name, level point value, level point id)")
			public Response createLevel(
					@ApiParam(value = "Game ID to store a new level", required = true) @PathParam("gameId") String gameId,
					@ApiParam(value = "Content-type in header", required = true)@HeaderParam(value = HttpHeaders.CONTENT_TYPE) String contentType, 
					@ApiParam(value = "Level detail in multiple/form-data type", required = true) byte[] formData)  {
				
				// Request log
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_99, "POST " + "gamification/levels/"+gameId, true);
				long randomLong = new Random().nextLong(); //To be able to match
				
				// parse given multipart form data
				JSONObject objResponse = new JSONObject();
				int levelnum = 0;
				String levelname = null;
				int levelpointvalue = 0;
				
				boolean levelnotifcheck = false;
				String levelnotifmessage = null;
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
					
					try {
						if(!levelAccess.isGameIdExist(conn,gameId)){
							objResponse.put("message", "Cannot create level. Game not found");
							Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
							return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
						}
					} catch (SQLException e1) {
						e1.printStackTrace();
						objResponse.put("message", "Cannot create level. Cannot check whether game ID exist or not. Database error. " + e1.getMessage());
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					}
					Map<String, FormDataPart> parts = MultipartHelper.getParts(formData, contentType);
					FormDataPart partNum = parts.get("levelnum");
					if (partNum != null) {
						levelnum = Integer.parseInt(partNum.getContent());
						if(levelAccess.isLevelNumExist(conn,gameId, levelnum)){
							// level id already exist
							objResponse.put("message", "Cannot create level. Failed to add the level. levelnum already exist!");
							Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
							return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();

						}
						FormDataPart partName = parts.get("levelname");
						if (partName != null) {
							levelname = partName.getContent();
						}
						
						FormDataPart partPV = parts.get("levelpointvalue");
						if (partPV != null) {
							// optional description text input form element
							levelpointvalue =  Integer.parseInt(partPV.getContent());
						}
						FormDataPart partNotificationCheck = parts.get("levelnotificationcheck");
						if (partNotificationCheck != null) {
							// checkbox is checked
							levelnotifcheck = true;
							
						}else{
							levelnotifcheck = false;
						}
						FormDataPart partNotificationMsg = parts.get("levelnotificationmessage");
						if (partNotificationMsg != null) {
							levelnotifmessage = partNotificationMsg.getContent();
						}else{
							levelnotifmessage = "";
						}
						LevelModel model = new LevelModel(levelnum, levelname, levelpointvalue, levelnotifcheck, levelnotifmessage);
						
						try{
							levelAccess.addNewLevel(conn,gameId, model);
							objResponse.put("message", "Level upload success (" + levelnum +")");
							Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_15, ""+randomLong, true);
							Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_24, ""+name, true);
							Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_25, ""+gameId, true);
							return Response.status(HttpURLConnection.HTTP_CREATED).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();

						} catch (SQLException e) {
							e.printStackTrace();
							objResponse.put("message", "Cannot create level. Failed to upload " + levelnum + ". " + e.getMessage());
							Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
							return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
						}
					}
					else{
						objResponse.put("message", "Cannot create level. Level number cannot be null!");
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();

					}
					
					
				} catch (MalformedStreamException e) {
					// the stream failed to follow required syntax
					objResponse.put("message", "Cannot create level. Failed to upload. " + levelnum + e.getMessage());
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();

				} catch (IOException e) {
					// a read or write error occurred
					objResponse.put("message", "Cannot create level. Failed to upload " + levelnum + ". " + e.getMessage());
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();

				} catch (SQLException e) {
					e.printStackTrace();
					objResponse.put("message", "Cannot create level. Failed to upload " + levelnum + ". " + e.getMessage());
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();

				}
				catch (NullPointerException e){
					e.printStackTrace();
					objResponse.put("message", "Cannot create level. Failed to upload " + levelnum + ". " + e.getMessage());
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
			 * Get a level data with specific ID from database
			 * @param gameId gameId
			 * @param levelNum level number
			 * @return HTTP Response Returned as JSON object
			 */
			@GET
			@Path("/{gameId}/{levelNum}")
			@Produces(MediaType.APPLICATION_JSON)
			@ApiResponses(value = {
					@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Found a level"),
					@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal Error"),
					@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized")})
			@ApiOperation(value = "getlevelWithNum", 
						  notes = "Get level details with specific level number",
						  response = LevelModel.class
						  )
			public Response getlevelWithNum(
					@ApiParam(value = "Game ID")@PathParam("gameId") String gameId,
					@ApiParam(value = "Level number")@PathParam("levelNum") int levelNum)
			{
				
				// Request log
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_99, "GET " + "gamification/levels/"+gameId+"/"+levelNum, true);
				long randomLong = new Random().nextLong(); //To be able to match
				
				LevelModel level = null;
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
					try {
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_16, ""+randomLong, true);
						
						try {
							if(!levelAccess.isGameIdExist(conn,gameId)){
								objResponse.put("message", "Cannot fetched level. Game not found");
								Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
								return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
							}
						} catch (SQLException e1) {
							e1.printStackTrace();
							objResponse.put("message", "Cannot fetched level. Cannot check whether game ID exist or not. Database error. " + e1.getMessage());
							Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
							return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
						}
						if(!levelAccess.isLevelNumExist(conn,gameId, levelNum)){
							objResponse.put("message", "Cannot fetched level. level not found");
							Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
							return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
						}
						level = levelAccess.getLevelWithNumber(conn,gameId, levelNum);
						if(level != null){
							ObjectMapper objectMapper = new ObjectMapper();
					    	//Set pretty printing of json
					    	objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
					    	
					    	String levelString = objectMapper.writeValueAsString(level);
					    	Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_17, ""+randomLong, true);
					    	Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_26, ""+name, true);
							Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_27, ""+gameId, true);
							return Response.status(HttpURLConnection.HTTP_OK).entity(levelString).type(MediaType.APPLICATION_JSON).build();
						}
						else{
							objResponse.put("message", "Cannot fetched level. Cannot find level with " + levelNum);
							Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
							return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();

						}
					} catch (SQLException e) {
						e.printStackTrace();
						objResponse.put("message", "Cannot fetched level. DB Error. " + e.getMessage());
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();

					}
					
				} catch (JsonProcessingException e) {
					e.printStackTrace();
					objResponse.put("message", "Cannot fetched level. JSON processing error. " + e.getMessage());
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(e.getMessage()).type(MediaType.APPLICATION_JSON).build();
				} catch (SQLException e) {
					e.printStackTrace();
					objResponse.put("message", "Cannot fetched level. DB Error. " + e.getMessage());
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
			 * Update a level.
			 * Name attribute for form data : 
			 * <ul>
			 * 	<li>levelnum - Level Number - String (20 chars)
			 *  <li>levelname - Level name - String (20 chars)
			 *  <li>levelpointvalue - Point Value Level - Integer
			 *  <li>levelnotificationcheck - Level Notification Boolean - Boolean - Option whether use notification or not
			 *  <li>levelnotificationmessage - Level Notification Message - String
			 * </ul>
			 * @param gameId Game ID obtained from Gamification Game Service
			 * @param levelNum levelNum
			 * @param formData Form data with multipart/form-data type
			 * @param contentType Content type (implicitly sent in header)
			 * @return HTTP Response returned as JSON object
			 */
			@PUT
			@Path("/{gameId}/{levelNum}")
			@Produces(MediaType.APPLICATION_JSON)
			@ApiResponses(value = {
					@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Level Updated"),
					@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Error occured"),
					@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad request"),
					@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized")
			})
			@ApiOperation(value = "updateLevel",
						 notes = "A method to update an level with details (Level number, level name, level point value, level point id)")
			public Response updateLevel(
					@ApiParam(value = "Game ID to store a new level", required = true) @PathParam("gameId") String gameId,
						@PathParam("levelNum") int levelNum,
					@ApiParam(value = "Content type in header", required = true)@HeaderParam(value = HttpHeaders.CONTENT_TYPE) String contentType, 
					@ApiParam(value = "Level detail in multiple/form-data type", required = true) byte[] formData)  {
				
				// Request log
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_99, "PUT " + "gamification/levels/"+gameId+"/"+levelNum, true);
				long randomLong = new Random().nextLong(); //To be able to match
				
				
				// parse given multipart form data
				JSONObject objResponse = new JSONObject();

				String levelname = null;
				int levelpointvalue = 0;
				//boolean levelnotifcheck = false;
				String levelnotifmessage = null;
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
					try {
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_18, ""+randomLong, true);
						
						try {
							if(!levelAccess.isGameIdExist(conn,gameId)){
								objResponse.put("message", "Cannot update level. Game not found");
								Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
								return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
							}
						} catch (SQLException e1) {
							e1.printStackTrace();
							objResponse.put("message", "Cannot update level. Cannot check whether game ID exist or not. Database error. " + e1.getMessage());
							Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
							return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
						}
						if(!levelAccess.isLevelNumExist(conn,gameId, levelNum)){
							objResponse.put("message", "Cannot update level. Level not found");
							Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
							return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();

						}
						
						Map<String, FormDataPart> parts = MultipartHelper.getParts(formData, contentType);
						
						if (levelNum == 0 ) {
							objResponse.put("message", "Cannot update level. Level ID cannot be null");
							Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
							return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();

						}
							
						LevelModel model =levelAccess.getLevelWithNumber(conn,gameId, levelNum);

						if(model != null){
							FormDataPart partName = parts.get("levelname");
							if (partName != null) {
								levelname = partName.getContent();
								
								if(levelname != null){
									model.setName(levelname);
								}
							}
							FormDataPart partPV = parts.get("levelpointvalue");
							if (partPV != null) {
								// optional description text input form element
								levelpointvalue = Integer.parseInt(partPV.getContent());
								model.setPointValue(levelpointvalue);
							}
							FormDataPart partNotificationCheck = parts.get("levelnotificationcheck");
							if (partNotificationCheck != null) {
								// checkbox is checked
								model.useNotification(true);	
							}else{

								model.useNotification(false);
							}
							FormDataPart partNotificationMsg = parts.get("levelnotificationmessage");
							if (partNotificationMsg != null) {
								levelnotifmessage = partNotificationMsg.getContent();
								if(levelnotifmessage != null){
									model.setNotificationMessage(levelnotifmessage);
								}
							}
							try{
								levelAccess.updateLevel(conn,gameId, model);
								objResponse.put("message", "Level updated");
								Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_19, ""+randomLong, true);
								Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_28, ""+name, true);
								Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_29, ""+gameId, true);
								return Response.status(HttpURLConnection.HTTP_OK).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
							} catch (SQLException e) {
								e.printStackTrace();
								objResponse.put("message", "Cannot update level. Cannot connect to database. " + e.getMessage());
								Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
								return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();

							}
						}
						else{
							// model is null
							objResponse.put("message", "Cannot update level. Level not found in database");
							Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
							return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();

						}
					} catch (SQLException e1) {
						e1.printStackTrace();
						objResponse.put("message", "Cannot update level. DB Error " + e1.getMessage());
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					}
					
				} catch (MalformedStreamException e) {
					// the stream failed to follow required syntax
					objResponse.put("message", "Cannot update level. Failed to upload " + levelNum + ". "+e.getMessage());
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
				} catch (IOException e) {
					// a read or write error occurred
					objResponse.put("message", "Cannot update level. Failed to upload " + levelNum + ". " + e.getMessage());
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
				} catch (SQLException e1) {
					e1.printStackTrace();
					objResponse.put("message", "Cannot update level. DB Error " + e1.getMessage());
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
			 * Delete a level data with specified ID
			 * @param gameId gameId
			 * @param levelNum levelNum
			 * @return HTTP Response returned as JSON object
			 */
			@DELETE
			@Path("/{gameId}/{levelNum}")
			@Produces(MediaType.APPLICATION_JSON)
			@ApiResponses(value = {
					@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Level Delete Success"),
					@ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Level not found"),
					@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad Request"),
			})
			@ApiOperation(value = "deleteLevel",
						  notes = "delete a level")
			public Response deleteLevel(
					@ApiParam(value = "Game ID to delete a level", required = true)@PathParam("gameId") String gameId,
					@ApiParam(value = "Level number that will be deleted", required = true)@PathParam("levelNum") int levelNum)
			{
				
				// Request log
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_99, "DELETE " + "gamification/levels/"+gameId+"/"+levelNum, true);
				long randomLong = new Random().nextLong(); //To be able to match
				
				
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
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_20, ""+randomLong, true);
					
					try {
						if(!levelAccess.isGameIdExist(conn,gameId)){
							objResponse.put("message", "Cannot delete level. Game not found");
							Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
							return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
						}
					} catch (SQLException e1) {
						e1.printStackTrace();
						objResponse.put("message", "Cannot delete level. Cannot check whether game ID exist or not. Database error. " + e1.getMessage());
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					}
					if(!levelAccess.isLevelNumExist(conn,gameId, levelNum)){
						objResponse.put("message", "Cannot delete level. Level not found");
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();

					}
					
					levelAccess.deleteLevel(conn,gameId, levelNum);
					objResponse.put("message", "Level Deleted");
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_21, ""+randomLong, true);
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_30, ""+name, true);
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_31, ""+gameId, true);
					return Response.status(HttpURLConnection.HTTP_OK).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();

				} catch (SQLException e) {
					
					e.printStackTrace();
					objResponse.put("message", "Cannot delete level. Cannot delete level. " + e.getMessage());
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
			 * Get a list of levels from database
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
					@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Found a list of levels"),
					@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal Error"),
					@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized")})
			@ApiOperation(value = "getLevelList", 
						  notes = "Returns a list of levels",
						  response = LevelModel.class,
						  responseContainer = "List"
						  )
			public Response getLevelList(
					@ApiParam(value = "Game ID to return")@PathParam("gameId") String gameId,
					@ApiParam(value = "Page number for retrieving data")@QueryParam("current") int currentPage,
					@ApiParam(value = "Number of data size")@QueryParam("rowCount") int windowSize,
					@ApiParam(value = "Search phrase parameter")@QueryParam("searchPhrase") String searchPhrase)
			{
				
				// Request log
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_99, "GET " + "gamification/levels/"+gameId, true);
				long randomLong = new Random().nextLong(); //To be able to match 

				
				List<LevelModel> model = null;
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
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_46, ""+randomLong, true);
					
					try {
						if(!levelAccess.isGameIdExist(conn,gameId)){
							objResponse.put("message", "Cannot get levels. Game not found");
							Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
							return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
						}
					} catch (SQLException e1) {
						e1.printStackTrace();
						objResponse.put("message", "Cannot get levels. Cannot check whether game ID exist or not. Database error. " + e1.getMessage());
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					}
					int offset = (currentPage - 1) * windowSize;
					
					ObjectMapper objectMapper = new ObjectMapper();
			    	//Set pretty printing of json
			    	objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
					
					int totalNum = levelAccess.getNumberOfLevels(conn,gameId);
					
					if(windowSize == -1){
						offset = 0;
						windowSize = totalNum;
					}
					
					model = levelAccess.getLevelsWithOffsetAndSearchPhrase(conn,gameId, offset, windowSize, searchPhrase);
					String modelString = objectMapper.writeValueAsString(model);
					JSONArray modelArray = (JSONArray) JSONValue.parse(modelString);
					logger.info(modelArray.toJSONString());
					objResponse.put("current", currentPage);
					objResponse.put("rowCount", windowSize);
					objResponse.put("rows", modelArray);
					objResponse.put("total", totalNum);

					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_47, ""+randomLong, true);
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_48, ""+name, true);
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_49, ""+gameId, true);
					return Response.status(HttpURLConnection.HTTP_OK).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					
				} catch (SQLException e) {
					e.printStackTrace();
					// return HTTP Response on error
					objResponse.put("message", "Cannot get levels. Database error. " + e.getMessage());
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
				} catch (JsonProcessingException e) {
					e.printStackTrace();
					objResponse.put("message", "Cannot get levels. JSON processing error. " + e.getMessage());
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
	  //}
	

}

package i5.las2peer.services.gamificationActionService;


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

import i5.las2peer.logging.L2pLogger;
import i5.las2peer.restMapper.annotations.ServicePath;
import i5.las2peer.restMapper.RESTService;
import i5.las2peer.api.Context;
import i5.las2peer.api.logging.MonitoringEvent;
import i5.las2peer.api.ManualDeployment;
import i5.las2peer.api.security.UserAgent;
import i5.las2peer.services.gamificationActionService.database.DatabaseManager;
import i5.las2peer.services.gamificationActionService.database.ActionDAO;
import i5.las2peer.services.gamificationActionService.database.ActionModel;
import i5.las2peer.services.gamificationActionService.helper.FormDataPart;
import i5.las2peer.services.gamificationActionService.helper.MultipartHelper;
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
 * Gamification Action Service
 * 
 * This is Gamification Action service to manage action as gamification element in Gamification Framework
 * It uses the LAS2peer Web-Connector for RESTful access to it.
 * 
 * Note:
 * If you plan on using Swagger you should adapt the information below
 * in the ApiInfo annotation to suit your project.
 * If you do not intend to provide a Swagger documentation of your service API,
 * the entire ApiInfo annotation should be removed.
 * 
 */
@Api( value = "/gamification/actions", authorizations = {
		@Authorization(value = "actions_auth",
		scopes = {
			@AuthorizationScope(scope = "write:actions", description = "modify actions in your game"),
			@AuthorizationScope(scope = "read:actions", description = "read your actions")
				  })
}, tags = "actions")
@SwaggerDefinition(
		info = @Info(
				title = "Gamification Action Service",
				version = "0.1",
				description = "Gamification Action Service for Gamification Framework",
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
@ServicePath("/gamification/actions")
public class GamificationActionService extends RESTService {

	// instantiate the logger class
	private final L2pLogger logger = L2pLogger.getInstance(GamificationActionService.class.getName());
	/*
	 * Database configuration
	 */
	private String jdbcDriverClassName;
	private String jdbcUrl;
	private String jdbcSchema;
	private String jdbcLogin;
	private String jdbcPass;
	private DatabaseManager dbm;
	
	private ActionDAO actionAccess;

	// this header is not known to javax.ws.rs.core.HttpHeaders
	public static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";
	public static final String HEADER_CONTENT_TYPE = "Content-Type";
	// non HTTP standard headers
	public static final String HEADER_OWNERID = "ownerid";
	public static final String HEADER_CONTENT_DESCRIPTION = "Content-Description";
	
	
	public GamificationActionService() {
		// read and set properties values
		// IF THE SERVICE CLASS NAME IS CHANGED, THE PROPERTIES FILE NAME NEED TO BE CHANGED TOO!
		setFieldValues();
		jdbcDriverClassName="org.postgresql.Driver";
		jdbcUrl="jdbc:postgresql://127.0.0.1:5432/";
		jdbcSchema="gamification";
		jdbcLogin="gamification";
		jdbcPass="gamification";
		dbm = new DatabaseManager(jdbcDriverClassName, jdbcLogin, jdbcPass, jdbcUrl, jdbcSchema);
		this.actionAccess = new ActionDAO();
	}
	
				/**
				 * Function to return http unauthorized message
				 * @return HTTP Response returned as JSON object
				 */
				private Response unauthorizedMessage(){
					JSONObject objResponse = new JSONObject();
					logger.info("You are not authorized >> " );
					objResponse.put("message", "You are not authorized");
					return Response.status(HttpURLConnection.HTTP_UNAUTHORIZED).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
				}	

				// //////////////////////////////////////////////////////////////////////////////////////
				// Action PART --------------------------------------
				// //////////////////////////////////////////////////////////////////////////////////////

				/**
				 * Post a new action. 
				 * Name attribute for form data : 
				 * <ul>
				 *  <li>actionid - Action ID - String (20 chars)
				 *  <li>actionname - Action name - String (20 chars)
				 *  <li>actiondesc - Action Description - String (50 chars)
				 *  <li>actionpointvalue - Point Value Action - Integer
				 *  <li>actionnotificationcheck - Action Notification Boolean - Boolean - Option whether use notification or not
				 *  <li>actionnotificationmessage - Action Notification Message - String
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
						@ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "{\"status\": 3, \"message\": \"Action upload success ( (actionid) )\"}"),
						@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "{\"status\": 3, \"message\": \"Failed to upload (actionid)\"}"),
						@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "{\"status\": 1, \"message\": \"Failed to add the action. Action ID already exist!\"}"),
						@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "{\"status\": =, \"message\": \"Action ID cannot be null!\"}"),
						@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "{\"status\": 2, \"message\": \"File content null. Failed to upload (actionid)\"}"),
						@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "{\"status\": 2, \"message\": \"Failed to upload (actionid)\"}"),
						@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "{\"status\": 3, \"message\": \"Action upload success ( (actionid) )}")
				})
				@ApiOperation(value = "createNewAction",
							 notes = "A method to store a new action with details (action ID, action name, action description,action point value")
				public Response createNewAction(
						@ApiParam(value = "Game ID to store a new action", required = true) @PathParam("gameId") String gameId,
						@ApiParam(value = "Content-type in header", required = true)@HeaderParam(value = HttpHeaders.CONTENT_TYPE) String contentType, 
						@ApiParam(value = "Action detail in multiple/form-data type", required = true) byte[] formData)  {
					
					// Request log
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_99, "POST " + "gamification/actions/"+gameId, true);
					long randomLong = new Random().nextLong(); //To be able to match 
					
					// parse given multipart form data
					JSONObject objResponse = new JSONObject();
					
					String actionid = null;
					String actionname = null;
					String actiondesc = null;
					int actionpointvalue = 0;
					
					boolean actionnotifcheck = false;
					String actionnotifmessage = null;
					Connection conn = null;

					UserAgent userAgent = (UserAgent) Context.getCurrent().getMainAgent();
					String name = userAgent.getLoginName();
					if(name.equals("anonymous")){
						return unauthorizedMessage();
					}
					try {
						conn = dbm.getConnection();
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_14, ""+randomLong, true);
						
						try {
							if(!actionAccess.isGameIdExist(conn,gameId)){
								objResponse.put("message", "Cannot create action. Game not found");
								Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
								return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
							}
						} catch (SQLException e1) {
							e1.printStackTrace();
							objResponse.put("message", "Cannot create action. Cannot check whether game ID exist or not. Database error. " + e1.getMessage());
							Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
							return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
						}
						Map<String, FormDataPart> parts = MultipartHelper.getParts(formData, contentType);
						FormDataPart partID = parts.get("actionid");
						if (partID != null) {
							actionid = partID.getContent();
							
							if(actionAccess.isActionIdExist(conn,gameId, actionid)){
								objResponse.put("message", "Cannot create action. Failed to add the action. action ID already exist!");
								Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
								return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
							}
							FormDataPart partName = parts.get("actionname");
							if (partName != null) {
								actionname = partName.getContent();
							}
							FormDataPart partDesc = parts.get("actiondesc");
							if (partDesc != null) {
								// optional description text input form element
								actiondesc = partDesc.getContent();
							}
							FormDataPart partPV = parts.get("actionpointvalue");
							if (partPV != null) {
								// optional description text input form element
								actionpointvalue =  Integer.parseInt(partPV.getContent());
							}
							FormDataPart partNotificationCheck = parts.get("actionnotificationcheck");
							if (partNotificationCheck != null) {
								// checkbox is checked
								actionnotifcheck = true;
							}else{
								actionnotifcheck = false;
							}
							FormDataPart partNotificationMsg = parts.get("actionnotificationmessage");
							if (partNotificationMsg != null) {
								actionnotifmessage = partNotificationMsg.getContent();
							}else{
								actionnotifmessage = "";
							}
							ActionModel action = new ActionModel(actionid, actionname, actiondesc, actionpointvalue, actionnotifcheck, actionnotifmessage);
							
							try{
								actionAccess.addNewAction(conn,gameId, action);
								objResponse.put("message", "Action upload success (" + actionid +")");
								Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_15, ""+randomLong, true);
								Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_24, ""+name, true);
								Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_25, ""+gameId, true);
								return Response.status(HttpURLConnection.HTTP_CREATED).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();

							} catch (SQLException e) {
								e.printStackTrace();
								objResponse.put("message", "Cannot create action. Failed to upload " + actionid + ". " + e.getMessage());
								Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
								return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
							}
						}
						else{
							objResponse.put("message", "Cannot create action. Action ID cannot be null!");
							Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
							return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
						}
						
						
					} catch (MalformedStreamException e) {
						// the stream failed to follow required syntax
						objResponse.put("message", "Cannot create action. Failed to upload " + actionid + ". " + e.getMessage());
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();

					} catch (IOException e) {
						// a read or write error occurred
						objResponse.put("message", "Cannot create action. Failed to upload " + actionid + ". " + e.getMessage());
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();

					} catch (SQLException e) {
						e.printStackTrace();
						objResponse.put("message", "Cannot create action. Failed to upload " + actionid + ". " + e.getMessage());
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();

					}
					catch (NullPointerException e){
						e.printStackTrace();
						objResponse.put("message", "Cannot create action. Failed to upload " + actionid + ". " + e.getMessage());
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
				 * Get an action data with specific ID from database
				 * @param gameId gameId
				 * @param actionId Action ID
				 * @return HTTP Response returned Action Model {@link ActionModel} as JSON object
				 * @see ActionModel
				 */
				@GET
				@Path("/{gameId}/{actionId}")
				@Produces(MediaType.APPLICATION_JSON)
				@ApiResponses(value = {
						@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Found an action"),
						@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal Error"),
						@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized")})
				@ApiOperation(value = "Find action for specific Game ID and action ID", 
							  notes = "Returns a action",
							  response = ActionModel.class,
							  authorizations = @Authorization(value = "api_key")
							  )
				public Response getActionWithId(
						@ApiParam(value = "Game ID")@PathParam("gameId") String gameId,
						@ApiParam(value = "Action ID")@PathParam("actionId") String actionId)
				{
					
					// Request log
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_99, "GET " + "gamification/actions/"+gameId+"/"+actionId, true);
					long randomLong = new Random().nextLong(); //To be able to match 
					
					ActionModel action = null;
					Connection conn = null;

					JSONObject objResponse = new JSONObject();
					UserAgent userAgent = (UserAgent) Context.getCurrent().getMainAgent();
					String name = userAgent.getLoginName();
					if(name.equals("anonymous")){
						return unauthorizedMessage();
					}
					try {
						conn = dbm.getConnection();
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_16, ""+randomLong, true);
						
						try {
							
							try {
								if(!actionAccess.isGameIdExist(conn,gameId)){
									objResponse.put("message", "Cannot get action. Game not found");
									Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
									return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
								}
							} catch (SQLException e1) {
								e1.printStackTrace();
								objResponse.put("message", "Cannot get action. Cannot check whether game ID exist or not. Database error. " + e1.getMessage());
								Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
								return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
							}
							if(!actionAccess.isActionIdExist(conn,gameId, actionId)){
								objResponse.put("message", "Cannot get action. Action not found");
								Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
								return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
							}
							action = actionAccess.getActionWithId(conn,gameId, actionId);
							if(action != null){
								ObjectMapper objectMapper = new ObjectMapper();
						    	//Set pretty printing of json
						    	objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
						    	
						    	String actionString = objectMapper.writeValueAsString(action);
						    	Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_17, ""+randomLong, true);
						    	Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_26, ""+name, true);
								Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_27, ""+gameId, true);
								return Response.status(HttpURLConnection.HTTP_OK).entity(actionString).type(MediaType.APPLICATION_JSON).build();
							}
							else{
								objResponse.put("message", "Cannot get action. Cannot find badge with " + actionId);
								Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
								return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();

							}
						} catch (SQLException e) {
							e.printStackTrace();
							objResponse.put("message", "Cannot get action. DB Error. " + e.getMessage());
							Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
							return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();

						}
						
					} catch (JsonProcessingException e) {
						e.printStackTrace();
						objResponse.put("message", "Cannot get action. JSON processing error. " + e.getMessage());
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(e.getMessage()).type(MediaType.APPLICATION_JSON).build();
						
					} catch (SQLException e) {
						e.printStackTrace();
						objResponse.put("message", "Cannot get action. DB Error. " + e.getMessage());
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
				 * Update an action.
				 * <ul>
				 *  <li>actionid - Action ID - String (20 chars)
				 *  <li>actionname - Action name - String (20 chars)
				 *  <li>actiondesc - Action Description - String (50 chars)
				 *  <li>actionpointvalue - Point Value Action - Integer
				 *  <li>actionnotificationcheck - Action Notification Boolean - Boolean - Option whether use notification or not
				 *  <li>actionnotificationmessage - Action Notification Message - String
				 * </ul>
				 * @param gameId Game ID obtained from Gamification Game Service
				 * @param actionId Action ID to be updated
				 * @param formData Form data with multipart/form-data type
				 * @param contentType Content type (implicitly sent in header)
				 * @return HTTP Response returned as JSON object
				 */
				@PUT
				@Path("/{gameId}/{actionId}")
				@Produces(MediaType.APPLICATION_JSON)
				@ApiResponses(value = {
						@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Action Updated"),
						@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Error occured"),
						@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad request"),
						@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized")
				})
				@ApiOperation(value = "Update an action",
							 notes = "A method to update an action with details (action ID, action name, action description, action point value")
				public Response updateAction(
						@ApiParam(value = "Game ID to store an updated action", required = true) @PathParam("gameId") String gameId,
						@PathParam("actionId") String actionId,
						@ApiParam(value = "action detail in multiple/form-data type", required = true)@HeaderParam(value = HttpHeaders.CONTENT_TYPE) String contentType, 
						byte[] formData)  {
					
					// Request log
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_99, "PUT " + "gamification/actions/"+gameId+"/"+actionId, true);
					long randomLong = new Random().nextLong(); //To be able to match 
					
					// parse given multipart form data
					JSONObject objResponse = new JSONObject();
					logger.info(actionId);
					String actionname = null;
					String actiondesc = null;
					int actionpointvalue = 0;
					
					//boolean actionnotifcheck = false;
					String actionnotifmessage = null;
					Connection conn = null;

					
					UserAgent userAgent = (UserAgent) Context.getCurrent().getMainAgent();
					String name = userAgent.getLoginName();
					if(name.equals("anonymous")){
						return unauthorizedMessage();
					}
					try {
						conn = dbm.getConnection();
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_18, ""+randomLong, true);
						
						Map<String, FormDataPart> parts = MultipartHelper.getParts(formData, contentType);
						
						if (actionId == null) {
							logger.info("action ID cannot be null >> " );
							objResponse.put("message", "action ID cannot be null");
							Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
							return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
						}
						try{
							if(!actionAccess.isGameIdExist(conn,gameId)){
								logger.info("Game not found >> ");
								objResponse.put("message", "Game not found");
								Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
								return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
							}
							
							
							ActionModel action = actionAccess.getActionWithId(conn,gameId, actionId);
				
							if(action == null){
								// action is null
								logger.info("action not found in database >> " );
								objResponse.put("message", "action not found in database");
								Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
								return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
							}
							
							FormDataPart partName = parts.get("actionname");
							if (partName != null) {
								actionname = partName.getContent();
								
								if(actionname != null){
									action.setName(actionname);
								}
							}
							FormDataPart partDesc = parts.get("actiondesc");
							if (partDesc != null) {
								// optional description text input form element
								actiondesc = partDesc.getContent();
								if(actiondesc!=null){
									action.setDescription(actiondesc);
								}
							}
							FormDataPart partPV = parts.get("actionpointvalue");
							if (partPV != null) {
								// optional description text input form element
								actionpointvalue = Integer.parseInt(partPV.getContent());
								action.setPointValue(actionpointvalue);
							}
							FormDataPart partNotificationCheck = parts.get("actionnotificationcheck");
							if (partNotificationCheck != null) {
								// checkbox is checked
								action.useNotification(true);
							}else{
								action.useNotification(false);
							}
							FormDataPart partNotificationMsg = parts.get("actionnotificationmessage");
							if (partNotificationMsg != null) {
								actionnotifmessage = partNotificationMsg.getContent();
								if(actionnotifmessage == null){
									action.setNotificationMessage(actionnotifmessage);
								}
							}
							actionAccess.updateAction(conn,gameId, action);
							logger.info("action updated >> ");
							objResponse.put("message", "action updated");
							Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_19, ""+randomLong, true);
							Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_28, ""+name, true);
							Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_29, ""+gameId, true);
							return Response.status(HttpURLConnection.HTTP_OK).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
						} catch (SQLException e) {
							e.printStackTrace();
							System.out.println(e.getMessage());
							logger.info("SQLException >> " + e.getMessage());
							objResponse.put("message", "Cannot connect to database");
							Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
							return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
						}
						
					} catch (MalformedStreamException e) {
						// the stream failed to follow required syntax
						logger.log(Level.SEVERE, e.getMessage(), e);
						logger.info("MalformedStreamException >> " );
						objResponse.put("message", "Failed to upload " + actionId + ". "+e.getMessage());
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					} catch (IOException e) {
						// a read or write error occurred
						logger.log(Level.SEVERE, e.getMessage(), e);
						logger.info("IOException >> " );
						objResponse.put("message", "Failed to upload " + actionId + ".");
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					} catch (SQLException e) {
						e.printStackTrace();
						System.out.println(e.getMessage());
						logger.info("SQLException >> " + e.getMessage());
						objResponse.put("message", "Cannot connect to database");
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
				 * Delete an action data with specified ID
				 * @param gameId Game ID obtained from Gamification Game Service
				 * @param actionId Action ID to be deleted
				 * @return HTTP Response returned as JSON object
				 */
				@DELETE
				@Path("/{gameId}/{actionId}")
				@Produces(MediaType.APPLICATION_JSON)
				@ApiResponses(value = {
						@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Action is deleted"),
						@ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Action not found"),
						@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad Request"),
				})
				@ApiOperation(value = "",
							  notes = "delete an action")
				public Response deleteAction(@PathParam("gameId") String gameId,
											 @PathParam("actionId") String actionId)
				{
					
					// Request log
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_99, "DELETE " + "gamification/actions/"+gameId+"/"+actionId, true);
					long randomLong = new Random().nextLong(); //To be able to match 
					
					JSONObject objResponse = new JSONObject();
					Connection conn = null;

					UserAgent userAgent = (UserAgent) Context.getCurrent().getMainAgent();
					String name = userAgent.getLoginName();
					if(name.equals("anonymous")){
						return unauthorizedMessage();
					}
					try {
						conn = dbm.getConnection();
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_20, ""+randomLong, true);
						
						try {
							if(!actionAccess.isGameIdExist(conn,gameId)){
								objResponse.put("message", "Cannot delete action. Game not found");
								Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
								return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
							}
						} catch (SQLException e1) {
							e1.printStackTrace();
							objResponse.put("message", "Cannot delete action. Cannot check whether game ID exist or not. Database error. " + e1.getMessage());
							Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
							return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
						}
						if(!actionAccess.isActionIdExist(conn,gameId, actionId)){
							objResponse.put("message", "Cannot delete action. Failed to delete the action. Action ID is not exist!");
							Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
							return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
						}
						actionAccess.deleteAction(conn,gameId, actionId);
						
						objResponse.put("message", "Action deleted");
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_21, ""+randomLong, true);
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_30, ""+name, true);
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_31, ""+gameId, true);
						return Response.status(HttpURLConnection.HTTP_OK).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();

					} catch (SQLException e) {
						
						e.printStackTrace();
						objResponse.put("message", "Cannot delete action. Database error. " + e.getMessage());
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
				 * Get a list of actions from database, support the features to do pagination and search
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
						@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Found a list of actions"),
						@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal Error"),
						@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized")})
				@ApiOperation(value = "getActionList", 
							  notes = "Returns a list of actions",
							  response = ActionModel.class,
							  responseContainer = "List"
							  )
				public Response getActionList(
						@ApiParam(value = "Game ID")@PathParam("gameId") String gameId,
						@ApiParam(value = "Page number for retrieving data")@QueryParam("current") int currentPage,
						@ApiParam(value = "Number of data size")@QueryParam("rowCount") int windowSize,
						@ApiParam(value = "Search phrase parameter")@QueryParam("searchPhrase") String searchPhrase)
				{
					// Request log
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_99, "GET " + "gamification/actions/"+gameId, true);
					long randomLong = new Random().nextLong(); //To be able to match 
					
					List<ActionModel> achs = null;
					Connection conn = null;

					JSONObject objResponse = new JSONObject();
					UserAgent userAgent = (UserAgent) Context.getCurrent().getMainAgent();
					String name = userAgent.getLoginName();
					if(name.equals("anonymous")){
						return unauthorizedMessage();
					}
					try {
						conn = dbm.getConnection();
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_46, ""+randomLong, true);
						
						try {
							if(!actionAccess.isGameIdExist(conn,gameId)){
								objResponse.put("message", "Cannot get actions. Game not found");
								Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
								return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
							}
						} catch (SQLException e1) {
							e1.printStackTrace();
							objResponse.put("message", "Cannot get actions. Cannot check whether game ID exist or not. Database error. " + e1.getMessage());
							Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
							return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
						}
						int offset = (currentPage - 1) * windowSize;
						int totalNum = actionAccess.getNumberOfActions(conn,gameId);
						
						if(windowSize == -1){
							offset = 0;
							windowSize = totalNum;
						}
						
						achs = actionAccess.getActionsWithOffsetAndSearchPhrase(conn,gameId, offset, windowSize, searchPhrase);

						ObjectMapper objectMapper = new ObjectMapper();
				    	//Set pretty printing of json
				    	objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
				    	
				    	String actionString = objectMapper.writeValueAsString(achs);
						JSONArray actionArray = (JSONArray) JSONValue.parse(actionString);
						objResponse.put("current", currentPage);
						objResponse.put("rowCount", windowSize);
						objResponse.put("rows", actionArray);
						objResponse.put("total", totalNum);

						logger.info(objResponse.toJSONString());
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_47, ""+randomLong, true);
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_48, ""+name, true);
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_49, ""+gameId, true);
						return Response.status(HttpURLConnection.HTTP_OK).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
						
					} catch (SQLException e) {
						e.printStackTrace();
						
						// return HTTP Response on error
						objResponse.put("message", "Cannot get actions. Database error. " + e.getMessage());
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					} catch (JsonProcessingException e) {
						e.printStackTrace();
						objResponse.put("message","Cannot get actions. JSON processing error. " + e.getMessage());
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

				/**
				 * Function to be accessed via RMI to trigger an action
				 * @param gameId gameId
				 * @param memberId memberId
				 * @param actionId actionId
				 * @return serialized JSON notification data caused by triggered action
				 */
				public String triggerActionRMI(String gameId, String memberId, String actionId)  {

					Connection conn = null;

					try {
						conn = dbm.getConnection();
						JSONObject obj = new JSONObject();
						
						if(!actionAccess.isActionIdExist(conn,gameId, actionId)){
							return null;
						}
						
						obj = actionAccess.triggerAction(conn,gameId, memberId, actionId);
						
						return obj.toJSONString();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
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
				
				/**
				 * Function to be accessed via RMI to get list of action
				 * @param gameId gameId
				 * @return serialized JSON notification data caused by triggered action
				 */
				public String getActionsRMI(String gameId)  {
					List<ActionModel> achs = null;
					Connection conn = null;

					try {
						conn = dbm.getConnection();
						Context.getCurrent().monitorEvent(this, MonitoringEvent.RMI_SUCCESSFUL, "getActionsRMI-1");
						int offset = 0;
						int totalNum = actionAccess.getNumberOfActions(conn,gameId);
						int windowSize = totalNum;

						Context.getCurrent().monitorEvent(this, MonitoringEvent.RMI_SUCCESSFUL, "getActionsRMI-2");
						achs = actionAccess.getActionsWithOffsetAndSearchPhrase(conn,gameId, offset, windowSize, "");
						Context.getCurrent().monitorEvent(this, MonitoringEvent.RMI_SUCCESSFUL, "getActionsRMI-3");
						ObjectMapper objectMapper = new ObjectMapper();
				    	//Set pretty printing of json
						Context.getCurrent().monitorEvent(this, MonitoringEvent.RMI_SUCCESSFUL, "getActionsRMIgetActionsRMI-4");
				    	objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
				    	Context.getCurrent().monitorEvent(this, MonitoringEvent.RMI_SUCCESSFUL, "getActionsRMI-5");
				    	String actionString;
				    	Context.getCurrent().monitorEvent(this, MonitoringEvent.RMI_SUCCESSFUL, "getActionsRMI-6");
						try {
							Context.getCurrent().monitorEvent(this, MonitoringEvent.RMI_SUCCESSFUL, "getActionsRMI-7");
							actionString = objectMapper.writeValueAsString(achs);
							Context.getCurrent().monitorEvent(this, MonitoringEvent.RMI_SUCCESSFUL, "getActionsRMI-8");
							return actionString;
						} catch (JsonProcessingException e) {
							e.printStackTrace();
							return null;
						}
					} catch (SQLException e1) {
						e1.printStackTrace();
						return null;
					}
					 // always close connections
				    finally {
				      try {
				    	  if(conn != null) {
				    		  conn.close();
				    	  }
				      } 
				      catch (SQLException e) {
				        logger.printStackTrace(e);
				      }
				    }
		  }
		

	//}
}

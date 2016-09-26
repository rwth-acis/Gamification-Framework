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

import org.apache.commons.fileupload.MultipartStream.MalformedStreamException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import i5.las2peer.api.Service;
import i5.las2peer.logging.L2pLogger;
import i5.las2peer.logging.NodeObserver.Event;
import i5.las2peer.restMapper.HttpResponse;
import i5.las2peer.restMapper.MediaType;
import i5.las2peer.restMapper.RESTMapper;
import i5.las2peer.restMapper.annotations.ContentParam;
import i5.las2peer.restMapper.annotations.Version;
import i5.las2peer.restMapper.tools.ValidationResult;
import i5.las2peer.restMapper.tools.XMLCheck;
import i5.las2peer.security.UserAgent;
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


// TODO Describe your own service
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
// TODO Adjust the following configuration
@Path("/gamification/actions")
@Version("0.1") // this annotation is used by the XML mapper
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

public class GamificationActionService extends Service {

	// instantiate the logger class
	private final L2pLogger logger = L2pLogger.getInstance(GamificationActionService.class.getName());
	/*
	 * Database configuration
	 */
	private String jdbcDriverClassName;
	private String jdbcLogin;
	private String jdbcPass;
	private String jdbcUrl;
	private String jdbcSchema;
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
		dbm = new DatabaseManager(jdbcDriverClassName, jdbcLogin, jdbcPass, jdbcUrl, jdbcSchema);
		this.actionAccess = new ActionDAO();
		
	}


	/**
	 * Function to return http unauthorized message
	 * @return HTTP response unauthorized
	 */
	private HttpResponse unauthorizedMessage(){
		JSONObject objResponse = new JSONObject();
		logger.info("You are not authorized >> " );
		objResponse.put("message", "You are not authorized");
		return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_UNAUTHORIZED);

	}	

	// //////////////////////////////////////////////////////////////////////////////////////
	// Action PART --------------------------------------
	// //////////////////////////////////////////////////////////////////////////////////////

	// TODO Basic single CRUD ---------------------------
	/**
	 * Post a new action
	 * @param gameId gameId
	 * @param formData form data
	 * @param contentType content type
	 * @return HttpResponse returned as JSON object
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
	public HttpResponse createNewAction(
			@ApiParam(value = "Game ID to store a new action", required = true) @PathParam("gameId") String gameId,
			@ApiParam(value = "Content-type in header", required = true)@HeaderParam(value = HttpHeaders.CONTENT_TYPE) String contentType, 
			@ApiParam(value = "Action detail in multiple/form-data type", required = true)@ContentParam byte[] formData)  {
		
		// Request log
		L2pLogger.logEvent(this, Event.SERVICE_CUSTOM_MESSAGE_99, "POST " + "gamification/actions/"+gameId);
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

		UserAgent userAgent = (UserAgent) getContext().getMainAgent();
		String name = userAgent.getLoginName();
		if(name.equals("anonymous")){
			return unauthorizedMessage();
		}
		try {
			conn = dbm.getConnection();
			L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_14, ""+randomLong);
			
			try {
				if(!actionAccess.isGameIdExist(conn,gameId)){
					objResponse.put("message", "Cannot create action. Game not found");
					L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
					return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
				}
			} catch (SQLException e1) {
				e1.printStackTrace();
				objResponse.put("message", "Cannot create action. Cannot check whether game ID exist or not. Database error. " + e1.getMessage());
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
			}
			Map<String, FormDataPart> parts = MultipartHelper.getParts(formData, contentType);
			FormDataPart partID = parts.get("actionid");
			if (partID != null) {
				actionid = partID.getContent();
				
				if(actionAccess.isActionIdExist(conn,gameId, actionid)){
					objResponse.put("message", "Cannot create action. Failed to add the action. action ID already exist!");
					L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
					return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_INTERNAL_ERROR);
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
					L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_15, ""+randomLong);
					L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_24, ""+name);
					L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_25, ""+gameId);
					
					return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_CREATED);

				} catch (SQLException e) {
					e.printStackTrace();
					objResponse.put("message", "Cannot create action. Failed to upload " + actionid + ". " + e.getMessage());
					L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
					return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_INTERNAL_ERROR);
				}
			}
			else{
				objResponse.put("message", "Cannot create action. Action ID cannot be null!");
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_INTERNAL_ERROR);

			}
			
			
		} catch (MalformedStreamException e) {
			// the stream failed to follow required syntax
			objResponse.put("message", "Cannot create action. Failed to upload " + actionid + ". " + e.getMessage());
			L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
			return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_BAD_REQUEST);

		} catch (IOException e) {
			// a read or write error occurred
			objResponse.put("message", "Cannot create action. Failed to upload " + actionid + ". " + e.getMessage());
			L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
			return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_INTERNAL_ERROR);

		} catch (SQLException e) {
			e.printStackTrace();
			objResponse.put("message", "Cannot create action. Failed to upload " + actionid + ". " + e.getMessage());
			L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
			return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_INTERNAL_ERROR);

		}
		catch (NullPointerException e){
			e.printStackTrace();
			objResponse.put("message", "Cannot create action. Failed to upload " + actionid + ". " + e.getMessage());
			L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
			return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_INTERNAL_ERROR);
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
	 * @param actionId action id
	 * @return HttpResponse returned as JSON object
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
	public HttpResponse getActionWithId(
			@ApiParam(value = "Game ID")@PathParam("gameId") String gameId,
			@ApiParam(value = "Action ID")@PathParam("actionId") String actionId)
	{
		
		// Request log
		L2pLogger.logEvent(this, Event.SERVICE_CUSTOM_MESSAGE_99, "GET " + "gamification/actions/"+gameId+"/"+actionId);
		long randomLong = new Random().nextLong(); //To be able to match 
		
		ActionModel action = null;
		Connection conn = null;

		JSONObject objResponse = new JSONObject();
		UserAgent userAgent = (UserAgent) getContext().getMainAgent();
		String name = userAgent.getLoginName();
		if(name.equals("anonymous")){
			return unauthorizedMessage();
		}
		try {
			conn = dbm.getConnection();
			L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_16, ""+randomLong);
			
			try {
				
				try {
					if(!actionAccess.isGameIdExist(conn,gameId)){
						objResponse.put("message", "Cannot get action. Game not found");
						L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
						return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
					}
				} catch (SQLException e1) {
					e1.printStackTrace();
					objResponse.put("message", "Cannot get action. Cannot check whether game ID exist or not. Database error. " + e1.getMessage());
					L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
					return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
				}
				if(!actionAccess.isActionIdExist(conn,gameId, actionId)){
					objResponse.put("message", "Cannot get action. Action not found");
					L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
					return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
				}
				action = actionAccess.getActionWithId(conn,gameId, actionId);
				if(action != null){
					ObjectMapper objectMapper = new ObjectMapper();
			    	//Set pretty printing of json
			    	objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
			    	
			    	String actionString = objectMapper.writeValueAsString(action);
			    	L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_17, ""+randomLong);
			    	L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_26, ""+name);
					L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_27, ""+gameId);
					return new HttpResponse(actionString, HttpURLConnection.HTTP_OK);
				}
				else{
					objResponse.put("message", "Cannot get action. Cannot find badge with " + actionId);
					L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
					return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);

				}
			} catch (SQLException e) {
				e.printStackTrace();
				objResponse.put("message", "Cannot get action. DB Error. " + e.getMessage());
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);

			}
			
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			objResponse.put("message", "Cannot get action. JSON processing error. " + e.getMessage());
			L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
			return new HttpResponse(e.getMessage(), HttpURLConnection.HTTP_INTERNAL_ERROR);

		} catch (SQLException e) {
			e.printStackTrace();
			objResponse.put("message", "Cannot get action. DB Error. " + e.getMessage());
			L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);

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
	 * Update an action
	 * @param gameId gameId
	 * @param actionId actionId
	 * @param formData form data
	 * @param contentType content type
	 * @return HttpResponse returned as JSON object
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
	public HttpResponse updateAction(
			@ApiParam(value = "Game ID to store an updated action", required = true) @PathParam("gameId") String gameId,
				@PathParam("actionId") String actionId,
			@ApiParam(value = "action detail in multiple/form-data type", required = true)@HeaderParam(value = HttpHeaders.CONTENT_TYPE) String contentType, 
									 @ContentParam byte[] formData)  {
		
		// Request log
		L2pLogger.logEvent(this, Event.SERVICE_CUSTOM_MESSAGE_99, "PUT " + "gamification/actions/"+gameId+"/"+actionId);
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

		
		UserAgent userAgent = (UserAgent) getContext().getMainAgent();
		String name = userAgent.getLoginName();
		if(name.equals("anonymous")){
			return unauthorizedMessage();
		}
		try {
			conn = dbm.getConnection();
			L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_18, ""+randomLong);
			
			Map<String, FormDataPart> parts = MultipartHelper.getParts(formData, contentType);
			
			if (actionId == null) {
				logger.info("action ID cannot be null >> " );
				objResponse.put("message", "action ID cannot be null");
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			try{
				if(!actionAccess.isGameIdExist(conn,gameId)){
					logger.info("Game not found >> ");
					objResponse.put("message", "Game not found");
					L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
					return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
				}
				
				
				ActionModel action = actionAccess.getActionWithId(conn,gameId, actionId);
	
				if(action == null){
					// action is null
					logger.info("action not found in database >> " );
					objResponse.put("message", "action not found in database");
					L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
					return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
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
					if(actionpointvalue!=0){
						action.setPointValue(actionpointvalue);
					}
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
				L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_19, ""+randomLong);
				L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_28, ""+name);
				L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_29, ""+gameId);
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_OK);
			} catch (SQLException e) {
				e.printStackTrace();
				System.out.println(e.getMessage());
				logger.info("SQLException >> " + e.getMessage());
				objResponse.put("message", "Cannot connect to database");
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
			}
			
		} catch (MalformedStreamException e) {
			// the stream failed to follow required syntax
			logger.log(Level.SEVERE, e.getMessage(), e);
			logger.info("MalformedStreamException >> " );
			objResponse.put("message", "Failed to upload " + actionId + ". "+e.getMessage());
			L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
		} catch (IOException e) {
			// a read or write error occurred
			logger.log(Level.SEVERE, e.getMessage(), e);
			logger.info("IOException >> " );
			objResponse.put("message", "Failed to upload " + actionId + ".");
			L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
			logger.info("SQLException >> " + e.getMessage());
			objResponse.put("message", "Cannot connect to database");
			L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
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
	 * @param gameId gameId
	 * @param actionId actionId
	 * @return HttpResponse returned as JSON object
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
	public HttpResponse deleteAction(@PathParam("gameId") String gameId,
								 @PathParam("actionId") String actionId)
	{
		
		// Request log
		L2pLogger.logEvent(this, Event.SERVICE_CUSTOM_MESSAGE_99, "DELETE " + "gamification/actions/"+gameId+"/"+actionId);
		long randomLong = new Random().nextLong(); //To be able to match 
		
		JSONObject objResponse = new JSONObject();
		Connection conn = null;

		UserAgent userAgent = (UserAgent) getContext().getMainAgent();
		String name = userAgent.getLoginName();
		if(name.equals("anonymous")){
			return unauthorizedMessage();
		}
		try {
			conn = dbm.getConnection();
			L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_20, ""+randomLong);
			
			try {
				if(!actionAccess.isGameIdExist(conn,gameId)){
					objResponse.put("message", "Cannot delete action. Game not found");
					L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
					return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
				}
			} catch (SQLException e1) {
				e1.printStackTrace();
				objResponse.put("message", "Cannot delete action. Cannot check whether game ID exist or not. Database error. " + e1.getMessage());
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
			}
			if(!actionAccess.isActionIdExist(conn,gameId, actionId)){
				objResponse.put("message", "Cannot delete action. Failed to delete the action. Action ID is not exist!");
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_BAD_REQUEST);
			}
			actionAccess.deleteAction(conn,gameId, actionId);
			
			objResponse.put("message", "Action deleted");
			L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_21, ""+randomLong);
			L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_30, ""+name);
			L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_31, ""+gameId);
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_OK);

		} catch (SQLException e) {
			
			e.printStackTrace();
			objResponse.put("message", "Cannot delete action. Database error. " + e.getMessage());
			L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
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
	
	// TODO Batch Processing ----------------------------
	/**
	 * Get a list of actions from database
	 * @param gameId gameId
	 * @param currentPage current cursor page
	 * @param windowSize size of fetched data
	 * @param searchPhrase search word
	 * @return HttpResponse returned as JSON object
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
	public HttpResponse getActionList(
			@ApiParam(value = "Game ID")@PathParam("gameId") String gameId,
			@ApiParam(value = "Page number for retrieving data")@QueryParam("current") int currentPage,
			@ApiParam(value = "Number of data size")@QueryParam("rowCount") int windowSize,
			@ApiParam(value = "Search phrase parameter")@QueryParam("searchPhrase") String searchPhrase)
	{
		// Request log
		L2pLogger.logEvent(this, Event.SERVICE_CUSTOM_MESSAGE_99, "GET " + "gamification/actions/"+gameId);
		
		List<ActionModel> achs = null;
		Connection conn = null;

		JSONObject objResponse = new JSONObject();
		UserAgent userAgent = (UserAgent) getContext().getMainAgent();
		String name = userAgent.getLoginName();
		if(name.equals("anonymous")){
			return unauthorizedMessage();
		}
		try {
			conn = dbm.getConnection();
			L2pLogger.logEvent(this, Event.AGENT_GET_STARTED, "Get Actions");
			
			try {
				if(!actionAccess.isGameIdExist(conn,gameId)){
					objResponse.put("message", "Cannot get actions. Game not found");
					L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
					return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
				}
			} catch (SQLException e1) {
				e1.printStackTrace();
				objResponse.put("message", "Cannot get actions. Cannot check whether game ID exist or not. Database error. " + e1.getMessage());
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
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
			logger.info(actionArray.toJSONString());
			objResponse.put("current", currentPage);
			objResponse.put("rowCount", windowSize);
			objResponse.put("rows", actionArray);
			objResponse.put("total", totalNum);
			L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_10, "Actions fetched" + " : " + gameId + " : " + userAgent);
			L2pLogger.logEvent(this, Event.AGENT_GET_SUCCESS, "Actions fetched" + " : " + gameId + " : " + userAgent);
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_OK);
			
		} catch (SQLException e) {
			e.printStackTrace();
			
			// return HTTP Response on error
			objResponse.put("message", "Cannot get actions. Database error. " + e.getMessage());
			L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			objResponse.put("message","Cannot get actions. JSON processing error. " + e.getMessage());
			L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
			return new HttpResponse(e.getMessage(), HttpURLConnection.HTTP_INTERNAL_ERROR);

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
			JSONArray arr = new JSONArray();
			
			if(!actionAccess.isActionIdExist(conn,gameId, actionId)){
				return null;
			}
			
			arr = actionAccess.triggerAction(conn,gameId, memberId, actionId);
			
			return arr.toJSONString();
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
			JSONArray arr = new JSONArray();
			
			int offset = 0;
			int totalNum = actionAccess.getNumberOfActions(conn,gameId);
			int windowSize = totalNum;

			
			achs = actionAccess.getActionsWithOffsetAndSearchPhrase(conn,gameId, offset, windowSize, "");

			ObjectMapper objectMapper = new ObjectMapper();
	    	//Set pretty printing of json
	    	objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
	    	
	    	String actionString;
			try {
				actionString = objectMapper.writeValueAsString(achs);
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
	        conn.close();
	      } catch (SQLException e) {
	        logger.printStackTrace(e);
	      }
	    }
		

	}
	

	// //////////////////////////////////////////////////////////////////////////////////////
	// Methods required by the LAS2peer framework.
	// //////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Method for debugging purposes.
	 * Here the concept of restMapping validation is shown.
	 * It is important to check, if all annotations are correct and consistent.
	 * Otherwise the service will not be accessible by the WebConnector.
	 * Best to do it in the unit tests.
	 * To avoid being overlooked/ignored the method is implemented here and not in the test section.
	 * @return true, if mapping correct
	 */
	public boolean debugMapping() {
		String XML_LOCATION = "./restMapping.xml";
		String xml = getRESTMapping();

		try {
			RESTMapper.writeFile(XML_LOCATION, xml);
		} catch (IOException e) {
			// write error to logfile and console
			logger.log(Level.SEVERE, e.toString(), e);
			// create and publish a monitoring message
			L2pLogger.logEvent(this, Event.SERVICE_ERROR, e.toString());
		}

		XMLCheck validator = new XMLCheck();
		ValidationResult result = validator.validate(xml);

		if (result.isValid()) {
			return true;
		}
		return false;
	}

	/**
	 * This method is needed for every RESTful game in LAS2peer. There is no need to change!
	 * 
	 * @return the mapping
	 */
	public String getRESTMapping() {
		String result = "";
		try {
			result = RESTMapper.getMethodsAsXML(this.getClass());
		} catch (Exception e) {
			// write error to logfile and console
			logger.log(Level.SEVERE, e.toString(), e);
			// create and publish a monitoring message
			L2pLogger.logEvent(this, Event.SERVICE_ERROR, e.toString());
		}
		return result;
	}

}

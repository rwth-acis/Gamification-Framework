package i5.las2peer.services.gamificationActionService;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.imageio.ImageIO;
import javax.ws.rs.Consumes;
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
import org.apache.commons.lang3.tuple.Pair;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Mode;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import i5.las2peer.api.Service;
import i5.las2peer.execution.L2pServiceException;
import i5.las2peer.logging.L2pLogger;
import i5.las2peer.logging.NodeObserver.Event;
import i5.las2peer.p2p.AgentNotKnownException;
import i5.las2peer.p2p.TimeoutException;
import i5.las2peer.restMapper.HttpResponse;
import i5.las2peer.restMapper.MediaType;
import i5.las2peer.restMapper.RESTMapper;
import i5.las2peer.restMapper.annotations.ContentParam;
import i5.las2peer.restMapper.annotations.Version;
import i5.las2peer.restMapper.tools.ValidationResult;
import i5.las2peer.restMapper.tools.XMLCheck;
import i5.las2peer.security.L2pSecurityException;
import i5.las2peer.security.UserAgent;
import i5.las2peer.services.gamificationActionService.database.ActionDAO;
import i5.las2peer.services.gamificationActionService.database.ActionModel;
import i5.las2peer.services.gamificationActionService.database.SQLDatabase;
import i5.las2peer.services.gamificationActionService.helper.FormDataPart;
import i5.las2peer.services.gamificationActionService.helper.MultipartHelper;
import i5.las2peer.services.gamificationApplicationService.database.ApplicationDAO;
import i5.las2peer.services.gamificationActionService.helper.ErrorResponse;
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
import net.minidev.json.parser.ParseException;


// TODO Describe your own service
/**
 * Gamification Action Service
 * 
 * This is Gamification Action service to manage actions in Gamification Framework
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
			@AuthorizationScope(scope = "write:actions", description = "modify actions in your application"),
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
	private String jdbcHost;
	private int jdbcPort;
	private String jdbcSchema;
	private String epURL;
	
	private SQLDatabase DBManager;
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
	}

	private boolean initializeDBConnection() {

		this.DBManager = new SQLDatabase(this.jdbcDriverClassName, this.jdbcLogin, this.jdbcPass, this.jdbcSchema, this.jdbcHost, this.jdbcPort);
		logger.info(jdbcDriverClassName + " " + jdbcLogin);
		try {
				this.DBManager.connect();
				this.actionAccess = new ActionDAO(this.DBManager.getConnection());
				logger.info("Monitoring: Database connected!");
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				logger.info("Monitoring: Could not connect to database!. " + e.getMessage());
				return false;
			}
	}


	private HttpResponse unauthorizedMessage(){
		JSONObject objResponse = new JSONObject();
		logger.info("You are not authorized >> " );
		objResponse.put("message", "You are not authorized");
		return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_UNAUTHORIZED);

	}
	
	
	
	// //////////////////////////////////////////////////////////////////////////////////////
	// Service methods.
	// //////////////////////////////////////////////////////////////////////////////////////
	
	
	// //////////////////////////////////////////////////////////////////////////////////////
	// Application PART --------------------------------------
	// //////////////////////////////////////////////////////////////////////////////////////
	
	// TODO Basic single CRUD -------------------------------------
	
	
	
//	/**
//	 * Get a list of users apps from database
//	 * 
//	 * @param currentPage current cursor page
//	 * @param windowSize size of fetched data
//	 * @param memberId member id
//	 * @return HttpResponse Returned as JSON object
//	 */
//	@GET
//	@Path("/data/{memberId}/{windowSize}")
//	@Produces(MediaType.APPLICATION_JSON)
//	@ApiResponses(value = {
//			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Found a list of badges"),
//			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal Error"),
//			@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized")})
//	@ApiOperation(value = "Find applications", 
//				  notes = "Returns a list of applications",
//				  response = ApplicationModel.class,
//				  responseContainer = "List",
//				  authorizations = @Authorization(value = "api_key")
//				  )
//	public HttpResponse getUsersAppsList(
//			@ApiParam(value = "Page number for retrieving data")@QueryParam("current") int currentPage,
//			@ApiParam(value = "Member ID")@PathParam("memberId") String memberId,
//			@ApiParam(value = "Number of data size")@PathParam("windowSize") int windowSize)
//	{
//		List<ApplicationModel> apps = null;
//		JSONObject objResponse = new JSONObject();
//		UserAgent userAgent = (UserAgent) getContext().getMainAgent();
//		String name = userAgent.getLoginName();
//		if(!name.equals("anonymous")){
//			try {
//				if(!initializeDBConnection()){
//					logger.info("Cannot connect to database >> ");
//					objResponse.put("message", "Cannot connect to database");
//					return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
//				}
//				if(!managerAccess.isMemberRegistered(memberId)){
//					logger.info("No member found >> ");
//					objResponse.put("message", "No member found");
//					return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
//				}
//				int offset = (currentPage - 1) * windowSize;
//				apps = managerAccess.getUsersApplicationsWithOffset(offset, windowSize, memberId);
//				int totalNum = managerAccess.getNumberOfUsersApplications(memberId);
//				
//				JSONArray appArray = new JSONArray();
//				appArray.addAll(apps);
//				
//				objResponse.put("current", currentPage);
//				objResponse.put("rowCount", windowSize);
//				objResponse.put("rows", appArray);
//				objResponse.put("total", totalNum);
//				logger.info(objResponse.toJSONString());
//				
//				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_OK);
//
//			} catch (SQLException e) {
//				e.printStackTrace();
//				String response = "Internal Error. Database connection failed. ";
//				
//				// return HTTP Response on error
//				return new HttpResponse(response+e.getMessage(), HttpURLConnection.HTTP_INTERNAL_ERROR);
//			}
//		}
//		else{
//
//			logger.info("Unauthorized >> ");
//			objResponse.put("success", false);
//			objResponse.put("message", "You are not authorized");
//			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_UNAUTHORIZED);
//
//		}
//		
//	}
	
	

	
	// //////////////////////////////////////////////////////////////////////////////////////
	// Badge PART --------------------------------------
	// //////////////////////////////////////////////////////////////////////////////////////
	
	// TODO Basic single CRUD ---------------------------------
	
	
	
	// TODO Batch processing --------------------
	
	
	
	// TODO Other functions ---------------------
	
	
	
	// //////////////////////////////////////////////////////////////////////////////////////
	// Achievement PART --------------------------------------
	// //////////////////////////////////////////////////////////////////////////////////////

	// TODO  Basic single CRUD --------------------------------------
	
	
	
	// //////////////////////////////////////////////////////////////////////////////////////
	// Level PART --------------------------------------
	// //////////////////////////////////////////////////////////////////////////////////////
	
	// TODO Basic Single CRUD
	
	

	// //////////////////////////////////////////////////////////////////////////////////////
	// Action PART --------------------------------------
	// //////////////////////////////////////////////////////////////////////////////////////

	// TODO Basic single CRUD ---------------------------
	/**
	 * Post a new action
	 * @param appId applicationId
	 * @param formData form data
	 * @param contentType content type
	 * @return HttpResponse with the returnString
	 */
	@POST
	@Path("/{appId}")
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
			@ApiParam(value = "Application ID to store a new action", required = true) @PathParam("appId") String appId,
			@ApiParam(value = "Content-type in header", required = true)@HeaderParam(value = HttpHeaders.CONTENT_TYPE) String contentType, 
			@ApiParam(value = "Action detail in multiple/form-data type", required = true)@ContentParam byte[] formData)  {
		// parse given multipart form data
		JSONObject objResponse = new JSONObject();
		
		String actionid = null;
		String actionname = null;
		String actiondesc = null;
		int actionpointvalue = 0;
		
		boolean actionnotifcheck = false;
		String actionnotifmessage = null;
		
		UserAgent userAgent = (UserAgent) getContext().getMainAgent();
		String name = userAgent.getLoginName();
		if(name.equals("anonymous")){
			return unauthorizedMessage();
		}
		try {
			if(!initializeDBConnection()){
				logger.info("Cannot connect to database >> ");
				objResponse.put("message", "Cannot connect to database");
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
			}
			try {
				if(!isAppWithIdExist(appId)){
					logger.info("App not found >> ");
					objResponse.put("message", "App not found");
					return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
				}
			} catch (AgentNotKnownException | L2pServiceException | L2pSecurityException | InterruptedException
					| TimeoutException e1) {
				e1.printStackTrace();
				logger.info("Cannot check whether application ID exist or not. >> " + e1.getMessage());
				objResponse.put("message", "Cannot check whether application ID exist or not. >> " + e1.getMessage());
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			Map<String, FormDataPart> parts = MultipartHelper.getParts(formData, contentType);
			FormDataPart partID = parts.get("actionid");
			if (partID != null) {
				actionid = partID.getContent();
				
				if(actionAccess.isActionIdExist(appId, actionid)){
					logger.info("Failed to add the action. Action ID already exist!");
					objResponse.put("status", 1);
					objResponse.put("message", "Failed to add the action. action ID already exist!");
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
					actionAccess.addNewAction(appId, action);
					logger.info("action upload success (" + actionid +")");
					objResponse.put("status", 3);
					objResponse.put("message", "Action upload success (" + actionid +")");
					return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_CREATED);

				} catch (SQLException e) {
					e.printStackTrace();
					System.out.println(e.getMessage());
					logger.info("SQLException >> " + e.getMessage());
					objResponse.put("status", 2);
					objResponse.put("message", "Failed to upload " + actionid);
					return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_INTERNAL_ERROR);
				}
			}
			else{
				logger.info("Action ID cannot be null");
				objResponse.put("status", 0);
				objResponse.put("message", "Action ID cannot be null!");
				return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_INTERNAL_ERROR);

			}
			
			
		} catch (MalformedStreamException e) {
			// the stream failed to follow required syntax
			logger.log(Level.SEVERE, e.getMessage(), e);
			System.out.println(e.getMessage());
			logger.info("MalformedStreamException >> " + e.getMessage());
			objResponse.put("status", 2);
			objResponse.put("message", "Failed to upload " + actionid);
			return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_BAD_REQUEST);

		} catch (IOException e) {
			// a read or write error occurred
			logger.log(Level.SEVERE, e.getMessage(), e);
			System.out.println(e.getMessage());
			logger.info("IOException >> " + e.getMessage());
			objResponse.put("status", 2);
			objResponse.put("message", "Failed to upload " + actionid);
			return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_INTERNAL_ERROR);

		} catch (SQLException e) {
			e.printStackTrace();
			logger.info("SQLException >> " + e.getMessage());
			objResponse.put("status", 2);
			objResponse.put("message", "Failed to upload " + actionid);
			return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_INTERNAL_ERROR);

		}
		catch (NullPointerException e){
			e.printStackTrace();
			logger.info("NullPointerException >> " + e.getMessage());
			objResponse.put("status", 2);
			objResponse.put("message", "Failed to upload " + actionid);
			return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_INTERNAL_ERROR);
		}
	}
	
	
	/**
	 * Get an action data with specific ID from database
	 * @param appId applicationId
	 * @param actionId action id
	 * @return HttpResponse Returned as JSON object
	 */
	@GET
	@Path("/{appId}/{actionId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Found an action"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal Error"),
			@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized")})
	@ApiOperation(value = "Find action for specific App ID and action ID", 
				  notes = "Returns a action",
				  response = ActionModel.class,
				  authorizations = @Authorization(value = "api_key")
				  )
	public HttpResponse getActionWithId(
			@ApiParam(value = "Application ID")@PathParam("appId") String appId,
			@ApiParam(value = "Action ID")@PathParam("actionId") String actionId)
	{
		ActionModel action = null;
		JSONObject objResponse = new JSONObject();
		UserAgent userAgent = (UserAgent) getContext().getMainAgent();
		String name = userAgent.getLoginName();
		if(name.equals("anonymous")){
			return unauthorizedMessage();
		}
		try {
			if(!initializeDBConnection()){
				logger.info("Cannot connect to database >> ");
				objResponse.put("message", "Cannot connect to database");
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
			}
			
			try {
				try {
					if(!isAppWithIdExist(appId)){
						logger.info("App not found >> ");
						objResponse.put("message", "App not found");
						return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
					}
				} catch (AgentNotKnownException | L2pServiceException | L2pSecurityException | InterruptedException
						| TimeoutException e1) {
					e1.printStackTrace();
					logger.info("Cannot check whether application ID exist or not. >> " + e1.getMessage());
					objResponse.put("message", "Cannot check whether application ID exist or not. >> " + e1.getMessage());
					return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
				}
				if(!actionAccess.isActionIdExist(appId, actionId)){
					logger.info("Action not found >> ");
					objResponse.put("message", "Action not found");
					return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
				}
				action = actionAccess.getActionWithId(appId, actionId);
				if(action != null){
					ObjectMapper objectMapper = new ObjectMapper();
			    	//Set pretty printing of json
			    	objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
			    	
			    	String actionString = objectMapper.writeValueAsString(action);
					return new HttpResponse(actionString, HttpURLConnection.HTTP_OK);
				}
				else{
					logger.info("Cannot find badge with " + actionId);
					objResponse.put("message", "Cannot find badge with " + actionId);
					return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);

				}
			} catch (SQLException e) {
				e.printStackTrace();
				logger.info("DB Error >> " + e.getMessage());
				objResponse.put("message", "DB Error. " + e.getMessage());
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);

			}
			
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return new HttpResponse(e.getMessage(), HttpURLConnection.HTTP_INTERNAL_ERROR);

		}
	}
	

	/**
	 * Update an action
	 * @param appId applicationId
	 * @param actionId actionId
	 * @param formData form data
	 * @param contentType content type
	 * @return HttpResponse with the returnString
	 */
	@PUT
	@Path("/{appId}/{actionId}")
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
			@ApiParam(value = "Application ID to store an updated action", required = true) @PathParam("appId") String appId,
				@PathParam("actionId") String actionId,
			@ApiParam(value = "action detail in multiple/form-data type", required = true)@HeaderParam(value = HttpHeaders.CONTENT_TYPE) String contentType, 
									 @ContentParam byte[] formData)  {
		// parse given multipart form data
		JSONObject objResponse = new JSONObject();
		logger.info(actionId);
		String actionname = null;
		String actiondesc = null;
		int actionpointvalue = 0;
		
		boolean actionnotifcheck = false;
		String actionnotifmessage = null;
		
		UserAgent userAgent = (UserAgent) getContext().getMainAgent();
		String name = userAgent.getLoginName();
		if(name.equals("anonymous")){
			return unauthorizedMessage();
		}
		try {
			if(!initializeDBConnection()){
				logger.info("Cannot connect to database >> ");
				objResponse.put("message", "Cannot connect to database");
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
			}
			Map<String, FormDataPart> parts = MultipartHelper.getParts(formData, contentType);
			
			if (actionId == null) {
				logger.info("action ID cannot be null >> " );
				objResponse.put("message", "action ID cannot be null");
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			try{
				if(!isAppWithIdExist(appId)){
					logger.info("App not found >> ");
					objResponse.put("message", "App not found");
					return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
				}
				
				ActionModel action = actionAccess.getActionWithId(appId, actionId);
	
				if(action == null){
					// action is null
					logger.info("action not found in database >> " );
					objResponse.put("message", "action not found in database");
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
				actionAccess.updateAction(appId, action);
				logger.info("action updated >> ");
				objResponse.put("message", "action updated");
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_OK);
			} catch (SQLException e) {
				e.printStackTrace();
				System.out.println(e.getMessage());
				logger.info("SQLException >> " + e.getMessage());
				objResponse.put("message", "Cannot connect to database");
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
	
			}catch (AgentNotKnownException | L2pServiceException | L2pSecurityException | InterruptedException
					| TimeoutException e1) {
				e1.printStackTrace();
				logger.info("Cannot check whether application ID exist or not. >> " + e1.getMessage());
				objResponse.put("message", "Cannot check whether application ID exist or not. >> " + e1.getMessage());
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			
		} catch (MalformedStreamException e) {
			// the stream failed to follow required syntax
			logger.log(Level.SEVERE, e.getMessage(), e);
			logger.info("MalformedStreamException >> " );
			objResponse.put("message", "Failed to upload " + actionId + ". "+e.getMessage());
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
		} catch (IOException e) {
			// a read or write error occurred
			logger.log(Level.SEVERE, e.getMessage(), e);
			logger.info("IOException >> " );
			objResponse.put("message", "Failed to upload " + actionId + ".");
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
		}

	}
	

	/**
	 * Delete an action data with specified ID
	 * @param appId applicationId
	 * @param actionId actionId
	 * @return HttpResponse with the returnString
	 */
	@DELETE
	@Path("/{appId}/{actionId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "action Delete Success"),
			@ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "action not found"),
			@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad Request"),
	})
	@ApiOperation(value = "",
				  notes = "delete an action")
	public HttpResponse deleteAction(@PathParam("appId") String appId,
								 @PathParam("actionId") String actionId)
	{
		JSONObject objResponse = new JSONObject();
		UserAgent userAgent = (UserAgent) getContext().getMainAgent();
		String name = userAgent.getLoginName();
		if(name.equals("anonymous")){
			return unauthorizedMessage();
		}
		try {
			if(!initializeDBConnection()){
				logger.info("Cannot connect to database >> ");
				objResponse.put("message", "Cannot connect to database");
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
			}
			try {
				if(!isAppWithIdExist(appId)){
					logger.info("App not found >> ");
					objResponse.put("message", "App not found");
					return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
				}
			} catch (AgentNotKnownException | L2pServiceException | L2pSecurityException | InterruptedException
					| TimeoutException e1) {
				e1.printStackTrace();
				logger.info("Cannot check whether application ID exist or not. >> " + e1.getMessage());
				objResponse.put("message", "Cannot check whether application ID exist or not. >> " + e1.getMessage());
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			if(!actionAccess.isActionIdExist(appId, actionId)){
				logger.info("Failed to delete the action. Action ID is not exist!");
				objResponse.put("status", 1);
				objResponse.put("message", "Failed to delete the action. Action ID is not exist!");
				return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_BAD_REQUEST);
			}
			actionAccess.deleteAction(appId, actionId);
			
			logger.info(" Deleted >> ");
			objResponse.put("message", "action Deleted");
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_OK);

		} catch (SQLException e) {
			
			e.printStackTrace();
			logger.info("Cannot delete action. >> " +e.getMessage());
			objResponse.put("message", "Cannot delete action.");
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
		}
	}
	
	// TODO Batch Processing ----------------------------
	/**
	 * Get a list of actions from database
	 * @param appId applicationId
	 * @param currentPage current cursor page
	 * @param windowSize size of fetched data
	 * @param searchPhrase search word
	 * @return HttpResponse Returned as JSON object
	 */
	@GET
	@Path("/{appId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Found a list of actions"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal Error"),
			@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized")})
	@ApiOperation(value = "Find points for specific App ID", 
				  notes = "Returns a list of points",
				  response = ActionModel.class,
				  responseContainer = "List",
				  authorizations = @Authorization(value = "api_key")
				  )
	public HttpResponse getActionList(
			@ApiParam(value = "Application ID to return")@PathParam("appId") String appId,
			@ApiParam(value = "Page number for retrieving data")@QueryParam("current") int currentPage,
			@ApiParam(value = "Number of data size")@QueryParam("rowCount") int windowSize,
			@ApiParam(value = "Search phrase parameter")@QueryParam("searchPhrase") String searchPhrase)
	{
		List<ActionModel> achs = null;
		JSONObject objResponse = new JSONObject();
		UserAgent userAgent = (UserAgent) getContext().getMainAgent();
		String name = userAgent.getLoginName();
		if(name.equals("anonymous")){
			return unauthorizedMessage();
		}
		try {
			if(!initializeDBConnection()){
				logger.info("Cannot connect to database >> ");
				objResponse.put("message", "Cannot connect to database");
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
			}
			try {
				if(!isAppWithIdExist(appId)){
					logger.info("App not found >> ");
					objResponse.put("message", "App not found");
					return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
				}
			} catch (AgentNotKnownException | L2pServiceException | L2pSecurityException | InterruptedException
					| TimeoutException e1) {
				e1.printStackTrace();
				logger.info("Cannot check whether application ID exist or not. >> " + e1.getMessage());
				objResponse.put("message", "Cannot check whether application ID exist or not. >> " + e1.getMessage());
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			int offset = (currentPage - 1) * windowSize;
			int totalNum = actionAccess.getNumberOfActions(appId);
			achs = actionAccess.getActionsWithOffsetAndSearchPhrase(appId, offset, windowSize, searchPhrase);

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
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_OK);
			
		} catch (SQLException e) {
			e.printStackTrace();
			String response = "Internal Error. Database connection failed. ";
			
			// return HTTP Response on error
			return new HttpResponse(response+e.getMessage(), HttpURLConnection.HTTP_INTERNAL_ERROR);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return new HttpResponse(e.getMessage(), HttpURLConnection.HTTP_INTERNAL_ERROR);

		}
	}
	
//	/**
//	 * Trigger an action
//	 * @param appId applicationId
//	 * @param actionId actionId
//	 * @return HttpResponse with the returnString
//	 */
//	@POST
//	@Path("/{appId}/trigger/{actionId}")
//	@Produces(MediaType.APPLICATION_JSON)
//	@ApiResponses(value = {
//			@ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "{\"status\": 3, \"message\": \"Action upload success ( (actionid) )\"}"),
//			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "{\"status\": 3, \"message\": \"Failed to upload (actionid)\"}"),
//			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "{\"status\": 1, \"message\": \"Failed to add the action. Action ID already exist!\"}"),
//			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "{\"status\": =, \"message\": \"Action ID cannot be null!\"}"),
//			@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "{\"status\": 2, \"message\": \"File content null. Failed to upload (actionid)\"}"),
//			@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "{\"status\": 2, \"message\": \"Failed to upload (actionid)\"}"),
//			@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "{\"status\": 3, \"message\": \"Action upload success ( (actionid) )}")
//	})
//	@ApiOperation(value = "triggerAction",
//				 notes = "A method to trigger an ")
//	public HttpResponse triggerAction(
//			@ApiParam(value = "Application ID", required = true) @PathParam("appId") String appId,
//			@ApiParam(value = "Action ID", required = true) @PathParam("actionId") String actionId)  {
//		// parse given multipart form data
//		JSONObject objResponse = new JSONObject();
//		
//		UserAgent userAgent = (UserAgent) getContext().getMainAgent();
//		String name = userAgent.getLoginName();
//		if(name.equals("anonymous")){
//			return ErrorResponse.Unauthorized(this, logger, objResponse);
//		}
//		// Implicitly retrieve member ID
//		String memberId = name;
//		if(!initializeDBConnection()){
//			logger.info("Cannot connect to database >> ");
//			objResponse.put("message", "Cannot connect to database");
//			return ErrorResponse.InternalError(this, logger, new Exception((String) objResponse.get("message")), objResponse);
//		}
//		
//		JSONArray arr = new JSONArray();
//		try {
//			try {
//				if(!isAppWithIdExist(appId)){
//					objResponse.put("message", "App not found");
//					return ErrorResponse.BadRequest(this, logger, new Exception((String) objResponse.get("message")), objResponse);
//				}
//			} catch (AgentNotKnownException | L2pServiceException | L2pSecurityException | InterruptedException
//					| TimeoutException e1) {
//				e1.printStackTrace();
//				objResponse.put("message", "Cannot check whether application ID exist or not. >> " + e1.getMessage());
//				return ErrorResponse.BadRequest(this, logger, new Exception((String) objResponse.get("message")), objResponse);
//			}
//			if(!actionAccess.isActionIdExist(appId, actionId)){
//				objResponse.put("message", "Action not found");
//				return ErrorResponse.BadRequest(this, logger, new Exception((String) objResponse.get("message")), objResponse);
//			}
//			arr = actionAccess.triggerAction(appId, memberId, actionId);
//			return new HttpResponse(arr.toJSONString(),HttpURLConnection.HTTP_OK);
//			
//		} catch (SQLException e) {
//			e.printStackTrace();
//			objResponse.put("message", "Failed to trigger action " + actionId);
//			return ErrorResponse.InternalError(this, logger, e, objResponse);
//		}
//	}

	

	public String triggerActionRMI(String appId, String memberId, String actionId) throws SQLException  {

		if(!initializeDBConnection()){
			logger.info("Cannot connect to database >> ");
			throw new SQLException("Cannot connect to database >> ");
		}
		
		JSONArray arr = new JSONArray();
	
		if(!actionAccess.isActionIdExist(appId, actionId)){
			throw new SQLException("Action ID is not exist");
		}
		
		arr = actionAccess.triggerAction(appId, memberId, actionId);
		return arr.toJSONString();

	}
	
	
	private boolean isAppWithIdExist(String appId) throws SQLException, AgentNotKnownException, L2pServiceException, L2pSecurityException, InterruptedException, TimeoutException{
		ApplicationDAO applicationAccess = null;
		if(!initializeDBConnection()){
			logger.info("Cannot connect to database >> ");
			return false;
		}
		try {
			applicationAccess = new ApplicationDAO(this.DBManager.getConnection());
			if(applicationAccess.isAppIdExist(appId)){
				L2pLogger.logEvent(this, Event.RMI_SUCCESSFUL, "RMI isAppWithIdExist is invoked");
				return true;
			}
			else{
				L2pLogger.logEvent(this, Event.RMI_SUCCESSFUL, "RMI isAppWithIdExist is invoked");
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
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
	 * This method is needed for every RESTful application in LAS2peer. There is no need to change!
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

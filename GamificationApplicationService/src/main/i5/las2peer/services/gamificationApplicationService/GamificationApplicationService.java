package i5.las2peer.services.gamificationApplicationService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.fileupload.MultipartStream.MalformedStreamException;

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
import i5.las2peer.services.gamificationApplicationService.database.ApplicationDAO;
import i5.las2peer.services.gamificationApplicationService.database.ApplicationModel;
import i5.las2peer.services.gamificationApplicationService.database.MemberModel;
import i5.las2peer.services.gamificationApplicationService.database.SQLDatabase;
import i5.las2peer.services.gamificationApplicationService.helper.ErrorResponse;
import i5.las2peer.services.gamificationApplicationService.helper.FormDataPart;
import i5.las2peer.services.gamificationApplicationService.helper.MultipartHelper;
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
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import java.util.Scanner;
import java.util.Vector;

/**
 * Gamification Application Service
 * 
 * This is Gamification Application service to manage top level application in Gamification Framework
 * It uses the LAS2peer Web-Connector for RESTful access to it.
 * 
 * 
 */

@Path("/gamification/applications")
@Version("0.1")
@Api( value = "/gamification/applications", authorizations = {
		@Authorization(value = "application_auth",
		scopes = {
			@AuthorizationScope(scope = "write:applications", description = "modify apps in your application"),
			@AuthorizationScope(scope = "read:applications", description = "read your apps")
				  })
}, tags = "applications")
@SwaggerDefinition(
		info = @Info(
				title = "Gamification Application Service",
				version = "0.1",
				description = "Gamification Application Service for Gamification Framework",
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


public class GamificationApplicationService extends Service {

	// instantiate the logger class
	private final L2pLogger logger = L2pLogger.getInstance(GamificationApplicationService.class.getName());
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
	private String WEBRES_URL;
	
	private SQLDatabase DBManager;
	private ApplicationDAO applicationAccess;
	
	// this header is not known to javax.ws.rs.core.HttpHeaders
	public static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";
	public static final String HEADER_CONTENT_TYPE = "Content-Type";
	// non HTTP standard headers
	public static final String HEADER_OWNERID = "ownerid";
	public static final String HEADER_CONTENT_DESCRIPTION = "Content-Description";

	public GamificationApplicationService() {
		// read and set properties values
		// IF THE SERVICE CLASS NAME IS CHANGED, THE PROPERTIES FILE NAME NEED TO BE CHANGED TOO!
		setFieldValues();
		
	}

	/**
	 * Initialize database connection
	 * @return true if database is connected
	 */
	private boolean initializeDBConnection() {

		this.DBManager = new SQLDatabase(this.jdbcDriverClassName, this.jdbcLogin, this.jdbcPass, this.jdbcSchema, this.jdbcHost, this.jdbcPort);
		logger.info(jdbcDriverClassName + " " + jdbcLogin);
		try {
			this.DBManager.connect();
			this.applicationAccess = new ApplicationDAO(this.DBManager.getConnection());
			logger.info("Monitoring: Database connected!");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("Monitoring: Could not connect to database!. " + e.getMessage());
			return false;
		}
	}
	
	/**
	 * Function to delete directories of an application in badge service and point service file system
	 * @return true if directories are deleted
	 */
	private boolean cleanStorage(String appId) throws AgentNotKnownException, L2pServiceException, L2pSecurityException, InterruptedException, TimeoutException {

		Object result = this.invokeServiceMethod("i5.las2peer.services.gamificationBadgeService.GamificationBadgeService@0.1", "cleanStorageRMI", new Serializable[] { appId });
		
		if (result != null) {
			if((int)result == 1){
				
				Object res = this.invokeServiceMethod("i5.las2peer.services.gamificationPointService.GamificationPointService@0.1", "cleanStorageRMI", new Serializable[] { appId });
				if (res != null) {
					if((int)res == 1){
						
						return true;
					}
				}
			}
		}
		return false;
	}

	
	// //////////////////////////////////////////////////////////////////////////////////////
	// Application PART --------------------------------------
	// //////////////////////////////////////////////////////////////////////////////////////
	
	// TODO Basic single CRUD -------------------------------------
	
	/**
	 * Create a new app
	 * 
	 * @param contentType form content type
	 * @param formData form data
	 * @return Application data in JSON
	 */
	@POST
	@Path("/data")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "createApplication",
			notes = "Method to create a new application")
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Cannot connect to database"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Database Error"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Error in parsing form data"),
			@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "App ID already exist"),
			@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "App ID cannot be empty"),
			@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Error checking app ID exist"),
			@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
			@ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "New application created")
	})
	public HttpResponse createApplication(
			@ApiParam(value = "Application detail in multiple/form-data type", required = true)@HeaderParam(value = HttpHeaders.CONTENT_TYPE) String contentType,
			@ApiParam(value = "Content of form data", required = true)@ContentParam byte[] formData) {
		JSONObject objResponse = new JSONObject();
		UserAgent userAgent = (UserAgent) getContext().getMainAgent();
		String name = userAgent.getLoginName();
		String appid = null;
		String appdesc = null;
		String commtype = null;
		if(name.equals("anonymous")){
			return ErrorResponse.Unauthorized(this, logger, objResponse);
		}
		if(!initializeDBConnection()){
			logger.info("Cannot connect to database >> ");
			objResponse.put("message", "Cannot connect to database");
			return ErrorResponse.InternalError(this, logger, new Exception((String) objResponse.get("message")), objResponse);
		}
		Map<String, FormDataPart> parts;
		try {
			parts = MultipartHelper.getParts(formData, contentType);
			FormDataPart partAppID = parts.get("appid");
			if (partAppID != null) {
				// these data belong to the (optional) file id text input form element
				appid = partAppID.getContent();
				// appid must be unique
				System.out.println(appid);
				if(applicationAccess.isAppIdExist(appid)){
					// app id already exist
					objResponse.put("message", "App ID already exist");
					return ErrorResponse.BadRequest(this, logger, new Exception((String) objResponse.get("message")), objResponse);
				}
				
				FormDataPart partAppDesc = parts.get("appdesc");
				if (partAppDesc != null) {
					appdesc = partAppDesc.getContent();
				}
				else{
					appdesc = "";
				}
				FormDataPart partCommType = parts.get("commtype");
				if (partAppDesc != null) {
					commtype = partCommType.getContent();
				}
				else{
					commtype = "def_type";
				}
				
				ApplicationModel newApp = new ApplicationModel(appid, appdesc, commtype);

				try{
					applicationAccess.addNewApplication(newApp);
					applicationAccess.addMemberToApp(newApp.getId(), name);
					objResponse.put("message", "New application created");
					logger.info(objResponse.toJSONString());
					return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_CREATED);

				} catch (SQLException e) {
					e.printStackTrace();
					objResponse.put("message", "Database Error");;
					return ErrorResponse.InternalError(this, logger, e, objResponse);
				}
			}
			else{
				// app id cannot be empty
				objResponse.put("message", "App ID cannot be empty");
				return ErrorResponse.BadRequest(this, logger, new Exception((String) objResponse.get("message")), objResponse);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			objResponse.put("message", "Error in parsing form data");
			return ErrorResponse.InternalError(this, logger, e, objResponse);

		} catch (SQLException e) {
				e.printStackTrace();
				objResponse.put("message", "Error checking app ID exist "  + e.getMessage());
				return ErrorResponse.BadRequest(this, logger, e, objResponse);
		} 
	}
	
	

	/**
	 * Get an app data with specified ID
	 * @param appId applicationId
	 * @return Application data in JSON
	 */
	@GET
	@Path("/data/{appId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "getApplicationDetails",
				notes = "Get an application data with specific ID")
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Return application data with specific ID"),
			@ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Method not found"),
			@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "App not found"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Cannot connect to database"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Database Error"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Failed to process JSON")
	})
	public HttpResponse getApplicationDetails(
			@ApiParam(value = "Application ID", required = true)@PathParam("appId") String appId)
	{
		JSONObject objResponse = new JSONObject();
		UserAgent userAgent = (UserAgent) getContext().getMainAgent();
		String name = userAgent.getLoginName();
		if(name.equals("anonymous")){
			return ErrorResponse.Unauthorized(this, logger, objResponse);
		}
		if(!initializeDBConnection()){
			logger.info("Cannot connect to database >> ");
			objResponse.put("message", "Cannot connect to database");
			return ErrorResponse.InternalError(this, logger, new Exception((String) objResponse.get("message")), objResponse);
		}

		try {
			if(!applicationAccess.isAppIdExist(appId)){
				objResponse.put("message", "App not found");
				return ErrorResponse.BadRequest(this, logger, new Exception((String) objResponse.get("message")), objResponse);
			}
			// Add Member to App
			ApplicationModel app = applicationAccess.getApplicationWithId(appId);
			ObjectMapper objectMapper = new ObjectMapper();
	    	//Set pretty printing of json
	    	objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
	    	String appString = objectMapper.writeValueAsString(app);
			return new HttpResponse(appString, HttpURLConnection.HTTP_OK);
		} catch (SQLException e) {
			e.printStackTrace();
			objResponse.put("message", "Database Error");
			return ErrorResponse.InternalError(this, logger, e, objResponse);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			objResponse.put("message", "Failed to process JSON");
			return ErrorResponse.InternalError(this, logger, e, objResponse);
		}
	}
	
	/**
	 * Update an application
	 * @param appId applicationId
	 * @param formData form data
	 * @param contentType content type
	 * @return HttpResponse Application updated status
	 */
	@PUT
	@Path("/data/{appId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "updateApplication",
	 			 	notes = "A method to update an application detail")
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Application Updated"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Cannot connect to database"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Error in Processing Database"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Error in parsing form data"),
			@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "App ID not found"),
			@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Wrong form data"),
			@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized")
	})
	public HttpResponse updateApplication(
			@ApiParam(value = "Application ID to be updated", required = true) @PathParam("appId") String appId,
			@ApiParam(value = "Content type in header of the retrieved application data", required = true)@HeaderParam(value = HttpHeaders.CONTENT_TYPE) String contentType, 
			@ApiParam(value = "Application detail in multiple/form-data type", required = true) @ContentParam byte[] formData)  {
		// parse given multipart form data
		JSONObject objResponse = new JSONObject();

		String appid = null;
		String appdesc = null;
		String commtype = null;
		UserAgent userAgent = (UserAgent) getContext().getMainAgent();
		String name = userAgent.getLoginName();
		if(name.equals("anonymous")){
			return ErrorResponse.Unauthorized(this, logger, objResponse);
		}
		if(!initializeDBConnection()){
			objResponse.put("message", "Cannot connect to database");
			return ErrorResponse.InternalError(this, logger, new Exception((String) objResponse.get("message")), objResponse);
		}
		try {
			if(!applicationAccess.isAppIdExist(appId)){
				objResponse.put("message", "App not found");
				return ErrorResponse.BadRequest(this, logger, new Exception((String) objResponse.get("message")), objResponse);
			}
			Map<String, FormDataPart> parts;parts = MultipartHelper.getParts(formData, contentType);
			ApplicationModel app = applicationAccess.getApplicationWithId(appId);
			
			FormDataPart partDesc = parts.get("appdesc");
			if (partDesc != null) {
				// optional description text input form element
				appdesc = partDesc.getContent();
				if(appdesc!=null){
					app.setDescription(appdesc);
				}
			}
			FormDataPart partComm = parts.get("commtype");
			if (partComm != null) {
				// optional description text input form element
				commtype = partDesc.getContent();
				if(commtype!=null){
					app.setCommType(commtype);
				}
			}
				
			try{
				applicationAccess.updateApplication(app);
				logger.info("Application "+ appid +" updated >> ");
				objResponse.put("message", "Application "+ appid +" updated");
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_OK);
			} catch (SQLException e) {
				e.printStackTrace();
				objResponse.put("message", "Cannot connect to database");
				return ErrorResponse.InternalError(this, logger, e, objResponse);
			}
		} catch (MalformedStreamException e) {
			e.printStackTrace();
			objResponse.put("message", "Wrong Form Data. " + e.getMessage());
			return ErrorResponse.BadRequest(this, logger, e, objResponse);
		} catch (IOException e) {
			e.printStackTrace();
			objResponse.put("message", "Error in parsing form data. " + e.getMessage());
			return ErrorResponse.InternalError(this, logger, e, objResponse);
		} catch (SQLException e) {
			e.printStackTrace();
			objResponse.put("message", "Error in Processing Database. " + e.getMessage());
			return ErrorResponse.InternalError(this, logger, e, objResponse);
		}
	}
		

	
	
	/**
	 * Delete an application data with specified ID
	 * @param appId applicationId
	 * @return HttpResponse with the returnString
	 */
	@DELETE
	@Path("/data/{appId}")
	@Produces(MediaType.APPLICATION_JSON)	
	@ApiOperation(value = "Delete Application",
	  			notes = "This method deletes an App")
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Application Deleted"),
			@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Application not found"),
			@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Application not found"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Cannot connect to database"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Error checking app ID exist"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Database error"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Error delete storage"),
			@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized")})

	public HttpResponse deleteApplication(
			@ApiParam(value = "Application ID", required = true)@PathParam("appId") String appId)
	{
		JSONObject objResponse = new JSONObject();
		UserAgent userAgent = (UserAgent) getContext().getMainAgent();
		String name = userAgent.getLoginName();
		if(name.equals("anonymous")){
			return ErrorResponse.Unauthorized(this, logger, objResponse);
		}
		if(!initializeDBConnection()){
			objResponse.put("message", "Cannot connect to database");
			return ErrorResponse.InternalError(this, logger, new Exception((String) objResponse.get("message")), objResponse);
		}

		try {
			if(!applicationAccess.isAppIdExist(appId)){
				objResponse.put("message", "App not found");
				return ErrorResponse.BadRequest(this, logger, new Exception((String) objResponse.get("message")), objResponse);
			}
			try {
				if(cleanStorage(appId)){
					//if(applicationAccess.removeApplicationInfo(appId)){
						if(applicationAccess.deleteApplicationDB(appId)){
							objResponse.put("message", "Application deleted");
							return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_OK);
						}
					//}
					objResponse.put("message", "Database error. ");
					return ErrorResponse.InternalError(this, logger, new Exception((String) objResponse.get("message")), objResponse);
				}
			} catch (AgentNotKnownException | L2pServiceException | L2pSecurityException | InterruptedException
					| TimeoutException e) {
				e.printStackTrace();
				L2pLogger.logEvent(Event.RMI_FAILED, "Failed to clean storage");
				objResponse.put("message", "RMI error. Failed to clean storage. ");
				return ErrorResponse.InternalError(this, logger, new Exception((String) objResponse.get("message")), objResponse);

			}
		} catch (SQLException e) {
			e.printStackTrace();
			objResponse.put("message", "Error checking app ID exist "  + e.getMessage());
			return ErrorResponse.BadRequest(this, logger, e, objResponse);
		}		
			
			objResponse.put("message", "Error delete storage");
			return ErrorResponse.InternalError(this, logger, new Exception((String) objResponse.get("message")), objResponse);
	}

	
	/**
	 * Get all application list separated into two categories. All apps registered for the member and other apps.
	 * 
	 * 
	 * @return HttpResponse with the returnString
	 */
	@GET
	@Path("/list/separated")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "getSeparateApplicationInfo",
			notes = "Get all application list separated into two categories. All apps registered for the member and other apps.")
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "List of apps"),
			@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Database error"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "JsonProcessingException")
	})
	public HttpResponse getSeparateApplicationInfo() {
		JSONObject objResponse = new JSONObject();
		UserAgent userAgent = (UserAgent) getContext().getMainAgent();
		String name = userAgent.getLoginName();
		if(name.equals("anonymous")){
			return ErrorResponse.Unauthorized(this, logger, objResponse);
		}
		if(!initializeDBConnection()){
			objResponse.put("message", "Cannot connect to database");
			return ErrorResponse.InternalError(this, logger, new Exception((String) objResponse.get("message")), objResponse);
		}
		ObjectMapper objectMapper = new ObjectMapper();
    	//Set pretty printing of json
    	objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		
    	try {
			List<List<ApplicationModel>> allApps = applicationAccess.getSeparateApplicationsWithMemberId(name);

			
			try {
				String response = objectMapper.writeValueAsString(allApps);
				allApps.clear();
				
				return new HttpResponse(response, HttpURLConnection.HTTP_OK);

			
			} catch (JsonProcessingException e) {
				e.printStackTrace();
				
				allApps.clear();
				// return HTTP Response on error
				objResponse.put("message", "Internal Error. JsonProcessingException.");
				return ErrorResponse.InternalError(this, logger, e, objResponse);

			}
		} catch (SQLException e) {
			
			e.printStackTrace();
			objResponse.put("message", "Database error");
			return ErrorResponse.InternalError(this, logger, e, objResponse);
		}
		
	}

	// TODO Other apps functions ----------------------------------
	/**
	 * Remove a member from the application
	 * @param appId applicationId
	 * @param memberId memberId
	 * @return HttpResponse status if a member is removed
	 */
	@DELETE
	@Path("/data/{appId}/{memberId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "removeMemberFromApp",
				notes = "delete a member from an app")
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Member is removed from app"),
			@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "App not found"),
			@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Error checking app ID exist"),
			@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "No member found"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Database error")
	})
	public HttpResponse removeMemberFromApp(
			@ApiParam(value = "Application ID", required = true)@PathParam("appId") String appId,
			@ApiParam(value = "Member ID", required = true)@PathParam("memberId") String memberId)
	{
		JSONObject objResponse = new JSONObject();
		UserAgent userAgent = (UserAgent) getContext().getMainAgent();
		String name = userAgent.getLoginName();
		if(name.equals("anonymous")){
			return ErrorResponse.Unauthorized(this, logger, objResponse);
		}
		if(!initializeDBConnection()){
			objResponse.put("message", "Cannot connect to database");
			return ErrorResponse.InternalError(this, logger, new Exception((String) objResponse.get("message")), objResponse);
		}
		try {
			if(!applicationAccess.isAppIdExist(appId)){
				objResponse.put("message", "App not found");
				return ErrorResponse.BadRequest(this, logger, new Exception((String) objResponse.get("message")), objResponse);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			objResponse.put("message", "Error checking app ID exist "  + e.getMessage());
			return ErrorResponse.BadRequest(this, logger, e, objResponse);
		}		
		try {
			if(!applicationAccess.isMemberRegistered(memberId)){
				objResponse.put("message", "No member found");
				return ErrorResponse.BadRequest(this, logger, new Exception((String) objResponse.get("message")), objResponse);
			}
			applicationAccess.removeMemberFromApp(memberId, appId);
			objResponse.put("success", true);
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_OK);

		}
		catch (SQLException e) {
			e.printStackTrace();
			objResponse.put("message", "Database error");
			return ErrorResponse.InternalError(this, logger, e, objResponse);
		}
	}
	
	/**
	 * Add a member to the application
	 * @param appId applicationId
	 * @param memberId memberId
	 * @return HttpResponse status if the member is added
	 */
	@POST
	@Path("/gamification/applications/data/{appId}/{memberId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Member is Added"),
			@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "App not found"),
			@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Error checking app ID exist"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Database error")
	})
	@ApiOperation(value = "addMemberToApp",
				  notes = "add a member to an app")
	public HttpResponse addMemberToApp(
			@ApiParam(value = "Application ID", required = true)@PathParam("appId") String appId,
			@ApiParam(value = "Member ID", required = true)@PathParam("memberId") String memberId)
	{
		JSONObject objResponse = new JSONObject();
		UserAgent userAgent = (UserAgent) getContext().getMainAgent();
		String name = userAgent.getLoginName();
		if(name.equals("anonymous")){
			return ErrorResponse.Unauthorized(this, logger, objResponse);
		}
		if(!initializeDBConnection()){
			objResponse.put("message", "Cannot connect to database");
			return ErrorResponse.InternalError(this, logger, new Exception((String) objResponse.get("message")), objResponse);
		}
		try {
			if(!applicationAccess.isAppIdExist(appId)){
				objResponse.put("message", "App not found");
				return ErrorResponse.BadRequest(this, logger, new Exception((String) objResponse.get("message")), objResponse);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			objResponse.put("message", "Error checking app ID exist "  + e.getMessage());
			return ErrorResponse.BadRequest(this, logger, e, objResponse);
		}		
		try {
			applicationAccess.addMemberToApp(appId, memberId);
			objResponse.put("success", true);
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_OK);
		}
		catch (SQLException e) {
			
			e.printStackTrace();
			objResponse.put("message", "Database error");
			return ErrorResponse.InternalError(this, logger, e, objResponse);
		}
	}
	
	/**
	 * Validate member and add to the database as the new member if he/she is not registered yet
	 * @return HttpResponse status if validation success
	 */
	@POST
	@Path("/gamification/applications/validation")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "memberLoginValidation",
			notes = "Simple function to validate a member login.")
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Member is registered"),
			@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
			@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "User data error to be retrieved"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Cannot connect to database"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "User data error to be retrieved. Not JSON object")
	})
	public HttpResponse memberLoginValidation() {
		JSONObject objResponse = new JSONObject();
			
			MemberModel member;
			UserAgent userAgent = (UserAgent) getContext().getMainAgent();
			// take username as default name
			String name = userAgent.getLoginName();
			System.out.println("User name : " + name);
			if(name.equals("anonymous")){
				return ErrorResponse.Unauthorized(this, logger, objResponse);
			}
			if(!initializeDBConnection()){
				objResponse.put("message", "Cannot connect to database");
				return ErrorResponse.InternalError(this, logger, new Exception((String) objResponse.get("message")), objResponse);
			}
			// try to fetch firstname/lastname from user data received from OpenID
			Serializable userData = userAgent.getUserData();

			if (userData != null) {
				Object jsonUserData = JSONValue.parse(userData.toString());
				if (jsonUserData instanceof JSONObject) {
					JSONObject obj = (JSONObject) jsonUserData;
					Object firstnameObj = obj.get("given_name");
					Object lastnameObj = obj.get("family_name");
					Object emailObj = obj.get("email");
					String firstname,lastname,email;
					if (firstnameObj != null) {
						firstname = ((String) firstnameObj);
					}
					else{
						firstname = "";
					}
					
					if (lastnameObj != null) {
						lastname = ((String) lastnameObj);
					}
					else{
						lastname = "";
					}
					
					if (emailObj != null) {
						email = ((String) emailObj);
					}
					else{
						email = "";
					}
					
					member = new MemberModel(name,firstname,lastname,email);
					logger.info(member.getId()+" "+member.getFullName()+" "+member.getEmail());
					try {
						if(!applicationAccess.isMemberRegistered(member.getId())){
							applicationAccess.registerMember(member);
							objResponse.put("message", "Welcome " + member.getId() + "!");
							return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_OK);
						}
					} catch (SQLException e) {
						e.printStackTrace();
						objResponse.put("message", "Cannot connect to database");
						return ErrorResponse.InternalError(this, logger, e, objResponse);
					}
				} else {
					logger.warning("Parsing user data failed! Got '" + jsonUserData.getClass().getName() + "' instead of "
							+ JSONObject.class.getName() + " expected!");
					objResponse.put("message", "User data error to be retrieved. Not JSON object.");
					return ErrorResponse.InternalError(this, logger, new Exception((String) objResponse.get("message")), objResponse);
				}
				objResponse.put("message", "Member already registered");
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_OK);
			}
			else{
				objResponse.put("message", "User data error to be retrieved.");
				return ErrorResponse.BadRequest(this, logger, new Exception((String) objResponse.get("message")), objResponse);
			}
				
		
	}
	
	
	/**
	 * Checking whether the application ID is already registered or not.
	 * This function is to be invoked via RMI by another services
	 * 
	 * @param appId application ID
	 * @return 1, if application ID exist
	 */
	public int isAppWithIdExist(String appId){
		if(!initializeDBConnection()){
			logger.info("Cannot connect to database >> ");
			return 0;
		}
		try {
			if(applicationAccess.isAppIdExist(appId)){
				L2pLogger.logEvent(this, Event.RMI_SUCCESSFUL, "RMI isAppWithIdExist is invoked");
				return 1;
			}
			else{
				L2pLogger.logEvent(this, Event.RMI_SUCCESSFUL, "RMI isAppWithIdExist is invoked");
				return 0;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			L2pLogger.logEvent(this, Event.RMI_FAILED, "Exception when checking Application ID exists or not. " + e.getMessage());
			return 0;
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

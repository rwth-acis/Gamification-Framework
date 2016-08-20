package i5.las2peer.services.gamificationAchievementService;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
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
import i5.las2peer.services.gamificationAchievementService.database.AchievementDAO;
import i5.las2peer.services.gamificationAchievementService.database.AchievementModel;

import i5.las2peer.services.gamificationAchievementService.database.SQLDatabase;
import i5.las2peer.services.gamificationAchievementService.helper.FormDataPart;
import i5.las2peer.services.gamificationAchievementService.helper.MultipartHelper;
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
// TODO Adjust the following configuration
@Path("/gamification/achievements")
@Version("0.1") // this annotation is used by the XML mapper
@Api( value = "/gamification/achievements", authorizations = {
		@Authorization(value = "achievements_auth",
		scopes = {
			@AuthorizationScope(scope = "write:achievements", description = "modify achievements in your application"),
			@AuthorizationScope(scope = "read:achievements", description = "read your achievements")
				  })
}, tags = "achievements")
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

// TODO Your own Serviceclass
public class GamificationAchievementService extends Service {

	// instantiate the logger class
	private final L2pLogger logger = L2pLogger.getInstance(GamificationAchievementService.class.getName());
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
				this.achievementAccess = new AchievementDAO(this.DBManager.getConnection());
				logger.info("Monitoring: Database connected!");
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				logger.info("Monitoring: Could not connect to database!. " + e.getMessage());
				return false;
			}
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
	
	
	/**
	 * Post a new achievement
	 * @param appId applicationId
	 * @param formData form data
	 * @param contentType content type
	 * @return HttpResponse returned as JSON object
	 */
	@POST
	@Path("/{appId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "{\"status\": 3, \"message\": \"Achievement upload success ( (achievementid) )\"}"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "{\"status\": 3, \"message\": \"Failed to upload (achievementid)\"}"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "{\"status\": 1, \"message\": \"Failed to add the achievement. Achievement ID already exist!\"}"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "{\"status\": =, \"message\": \"Achievement ID cannot be null!\"}"),
			@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "{\"status\": 2, \"message\": \"File content null. Failed to upload (achievementid)\"}"),
			@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "{\"status\": 2, \"message\": \"Failed to upload (achievementid)\"}"),
			@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "{\"status\": 3, \"message\": \"Achievement upload success ( (achievementid) )}")
	})
	@ApiOperation(value = "createNewAchievement",
				 notes = "A method to store a new achievement with details (achievement ID, achievement name, achievement description, achievement point value, achievement point id, achievement badge id")
	public HttpResponse createNewAchievement(
			@ApiParam(value = "Application ID to store a new achievement", required = true) @PathParam("appId") String appId,
			@ApiParam(value = "Content-type in header", required = true)@HeaderParam(value = HttpHeaders.CONTENT_TYPE) String contentType, 
			@ApiParam(value = "Achievement detail in multiple/form-data type", required = true)@ContentParam byte[] formData)  {
		// parse given multipart form data
		JSONObject objResponse = new JSONObject();
		String achievementid = null;
		String achievementname = null;
		String achievementdesc = null;
		int achievementpointvalue = 0;
		String achievementbadgeid = null;
		
		boolean achievementnotifcheck = false;
		String achievementnotifmessage = null;
		
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
				if(!achievementAccess.isAppIdExist(appId)){
					logger.info("App not found >> ");
					objResponse.put("message", "App not found");
					return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
				}
			} catch (SQLException e1) {
				e1.printStackTrace();
				logger.info("Cannot check whether application ID exist or not. Database error. >> " + e1.getMessage());
				objResponse.put("message", "Cannot check whether application ID exist or not. Database error.>> " + e1.getMessage());
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
			}
			Map<String, FormDataPart> parts = MultipartHelper.getParts(formData, contentType);
			FormDataPart partAchievementID = parts.get("achievementid");
			if (partAchievementID != null) {
				achievementid = partAchievementID.getContent();
				
				if(achievementAccess.isAchievementIdExist(appId, achievementid)){
					// Achievement id already exist
					logger.info("Failed to add the achievement. Achievement ID already exist!");
					objResponse.put("status", 1);
					objResponse.put("message", "Failed to add the achievement. achievement ID already exist!");
					return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_INTERNAL_ERROR);

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
					achievementAccess.addNewAchievement(appId, achievement);
					logger.info("achievement upload success (" + achievementid +")");
					objResponse.put("status", 3);
					objResponse.put("message", "Achievement upload success (" + achievementid +")");
					return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_CREATED);

				} catch (SQLException e) {
					e.printStackTrace();
					System.out.println(e.getMessage());
					logger.info("SQLException >> " + e.getMessage());
					objResponse.put("status", 2);
					objResponse.put("message", "Failed to upload " + achievementid);
					return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_INTERNAL_ERROR);
				}
			}
			else{
				logger.info("Achievement ID cannot be null");
				objResponse.put("status", 0);
				objResponse.put("message", "Achievement ID cannot be null!");
				return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_INTERNAL_ERROR);

			}
			
			
		} catch (MalformedStreamException e) {
			// the stream failed to follow required syntax
			logger.log(Level.SEVERE, e.getMessage(), e);
			System.out.println(e.getMessage());
			logger.info("MalformedStreamException >> " + e.getMessage());
			objResponse.put("status", 2);
			objResponse.put("message", "Failed to upload " + achievementid);
			return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_BAD_REQUEST);

		} catch (IOException e) {
			// a read or write error occurred
			logger.log(Level.SEVERE, e.getMessage(), e);
			System.out.println(e.getMessage());
			logger.info("IOException >> " + e.getMessage());
			objResponse.put("status", 2);
			objResponse.put("message", "Failed to upload " + achievementid);
			return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_INTERNAL_ERROR);

		} catch (SQLException e) {
			e.printStackTrace();
			logger.info("SQLException >> " + e.getMessage());
			objResponse.put("status", 2);
			objResponse.put("message", "Failed to upload " + achievementid);
			return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_INTERNAL_ERROR);

		}
		catch (NullPointerException e){
			e.printStackTrace();
			logger.info("NullPointerException >> " + e.getMessage());
			objResponse.put("status", 2);
			objResponse.put("message", "Failed to upload " + achievementid);
			return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_INTERNAL_ERROR);

		}
	}
	
	/**
	 * Get an achievement data with specific ID from database
	 * @param appId applicationId
	 * @param achievementId achievement id
	 * @return HttpResponse returned as JSON object
	 */
	@GET
	@Path("/{appId}/{achievementId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Found an achievement"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal Error"),
			@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized")})
	@ApiOperation(value = "getAchievementWithId", 
				  notes = "Get achievement data with specified ID",
				  response = AchievementModel.class
				  )
	public HttpResponse getAchievementWithId(
			@ApiParam(value = "Application ID")@PathParam("appId") String appId,
			@ApiParam(value = "Achievement ID")@PathParam("achievementId") String achievementId)
	{
		AchievementModel achievement = null;
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
					if(!achievementAccess.isAppIdExist(appId)){
						logger.info("App not found >> ");
						objResponse.put("message", "App not found");
						return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
					}
				} catch (SQLException e1) {
					e1.printStackTrace();
					logger.info("Cannot check whether application ID exist or not. Database error. >> " + e1.getMessage());
					objResponse.put("message", "Cannot check whether application ID exist or not. Database error.>> " + e1.getMessage());
					return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
				}
				if(!achievementAccess.isAchievementIdExist(appId, achievementId)){
					logger.info("Achievement not found >> ");
					objResponse.put("message", "Achievement not found");
					return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
				}
				achievement = achievementAccess.getAchievementWithId(appId, achievementId);
				if(achievement == null){
					throw new SQLException("Achievement Null, Cannot find achievement with " + achievementId);
				}
				ObjectMapper objectMapper = new ObjectMapper();
		    	//Set pretty printing of json
		    	objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		    	
		    	String achievementString = objectMapper.writeValueAsString(achievement);
		    	
				return new HttpResponse(achievementString, HttpURLConnection.HTTP_OK);
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
	 * Update an achievement
	 * @param appId applicationId
	 * @param achievementId achievementId
	 * @param formData form data
	 * @param contentType content type
	 * @return HttpResponse returned as JSON object
	 */
	@PUT
	@Path("/{appId}/{achievementId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Achievement Updated"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Error occured"),
			@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad request"),
			@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized")
	})
	@ApiOperation(value = "updateAchievement",
				 notes = "A method to update an achievement with details (achievement ID, achievement name, achievement description, achievement point value, achievement point id, achievement badge id")
	public HttpResponse updateAchievement(
			@ApiParam(value = "Application ID to update an achievement", required = true) @PathParam("appId") String appId,
				@PathParam("achievementId") String achievementId,
			@ApiParam(value = "Achievement data in multiple/form-data type", required = true)@HeaderParam(value = HttpHeaders.CONTENT_TYPE) String contentType, 
									 @ContentParam byte[] formData)  {
		// parse given multipart form data
		JSONObject objResponse = new JSONObject();

		String achievementname = null;
		String achievementdesc = null;
		int achievementpointvalue = 0;
		String achievementbadgeid = null;
		
		String achievementnotifmessage = null;
		
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
			
			if (achievementId == null) {
				logger.info("Achievement ID cannot be null >> " );
				objResponse.put("message", "Achievement ID cannot be null");
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			
			try {
				try {
					if(!achievementAccess.isAppIdExist(appId)){
						logger.info("App not found >> ");
						objResponse.put("message", "App not found");
						return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
					}
				} catch (SQLException e1) {
					e1.printStackTrace();
					logger.info("Cannot check whether application ID exist or not. Database error. >> " + e1.getMessage());
					objResponse.put("message", "Cannot check whether application ID exist or not. Database error.>> " + e1.getMessage());
					return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
				}
				if(!achievementAccess.isAchievementIdExist(appId, achievementId)){
					logger.info("Achievement not found >> ");
					objResponse.put("message", "Achievement not found");
					return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
				}
				
				AchievementModel currentAchievement = achievementAccess.getAchievementWithId(appId, achievementId);

				if(currentAchievement == null){
					// currentAchievement is null
					logger.info("Achievement not found in database >> " );
					objResponse.put("message", "Achievement not found in database");
					return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
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
					if(achievementpointvalue!=0){
						currentAchievement.setPointValue(achievementpointvalue);
					}
				}
				
				FormDataPart partBID = parts.get("achievementbadgeid");
				if (partBID != null) {
					// optional description text input form element
					achievementbadgeid = partBID.getContent();
					
					logger.info(achievementbadgeid);
					if(achievementbadgeid!=null){
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
				achievementAccess.updateAchievement(appId, currentAchievement);
				logger.info("Achievement updated >> ");
				objResponse.put("message", "Achievement updated");
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_OK);

			} catch (SQLException e) {
				e.printStackTrace();
				logger.info("DB Error >> " + e.getMessage());
				objResponse.put("message", "DB Error. " + e.getMessage());
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);

			}
			
		} catch (MalformedStreamException e) {
			// the stream failed to follow required syntax
			logger.log(Level.SEVERE, e.getMessage(), e);
			logger.info("MalformedStreamException >> " );
			objResponse.put("message", "Failed to upload " + achievementId + ". "+e.getMessage());
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
		} catch (IOException e) {
			// a read or write error occurred
			logger.log(Level.SEVERE, e.getMessage(), e);
			logger.info("IOException >> " );
			objResponse.put("message", "Failed to upload " + achievementId + ".");
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
		}

	}
	

	/**
	 * Delete an achievement data with specified ID
	 * @param appId applicationId
	 * @param achievementId achievementId
	 * @return HttpResponse returned as JSON object
	 */
	@DELETE
	@Path("/{appId}/{achievementId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Achievement Delete Success"),
			@ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Achievements not found"),
			@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad Request"),
	})
	@ApiOperation(value = "deleteAchievement",
				  notes = "Delete an achievement")
	public HttpResponse deleteAchievement(@PathParam("appId") String appId,
								 @PathParam("achievementId") String achievementId)
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
				if(!achievementAccess.isAppIdExist(appId)){
					logger.info("App not found >> ");
					objResponse.put("message", "App not found");
					return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
				}
			} catch (SQLException e1) {
				e1.printStackTrace();
				logger.info("Cannot check whether application ID exist or not. Database error. >> " + e1.getMessage());
				objResponse.put("message", "Cannot check whether application ID exist or not. Database error.>> " + e1.getMessage());
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
			}
			if(!achievementAccess.isAchievementIdExist(appId, achievementId)){
				logger.info("Achievement not found >> ");
				objResponse.put("message", "Achievement not found");
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			achievementAccess.deleteAchievement(appId, achievementId);
			
			logger.info(" Deleted >> ");
			objResponse.put("message", "Achievement Deleted");
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_OK);

		} catch (SQLException e) {
			
			e.printStackTrace();
			logger.info("Cannot delete Achievement. >> " +e.getMessage());
			objResponse.put("message", "Cannot delete Achievement.");
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
		}
	}
	
	// TODO Batch Processing  --------------------------------------
	/**
	 * Get a list of achievements from database
	 * @param appId applicationId
	 * @param currentPage current cursor page
	 * @param windowSize size of fetched data
	 * @param searchPhrase search word
	 * @return HttpResponse returned as JSON object
	 */
	@GET
	@Path("/{appId}")
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
	public HttpResponse getAchievementList(
			@ApiParam(value = "Application ID to return")@PathParam("appId") String appId,
			@ApiParam(value = "Page number for retrieving data")@QueryParam("current") int currentPage,
			@ApiParam(value = "Number of data size")@QueryParam("rowCount") int windowSize,
			@ApiParam(value = "Search phrase parameter")@QueryParam("searchPhrase") String searchPhrase)
	{
		List<AchievementModel> achs = null;
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
				if(!achievementAccess.isAppIdExist(appId)){
					logger.info("App not found >> ");
					objResponse.put("message", "App not found");
					return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
				}
			} catch (SQLException e1) {
				e1.printStackTrace();
				logger.info("Cannot check whether application ID exist or not. Database error. >> " + e1.getMessage());
				objResponse.put("message", "Cannot check whether application ID exist or not. Database error.>> " + e1.getMessage());
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
			}
			int offset = (currentPage - 1) * windowSize;
			int totalNum = achievementAccess.getNumberOfAchievements(appId);
			
			if(windowSize == -1){
				offset = 0;
				windowSize = totalNum;
			}
			
			achs = achievementAccess.getAchievementsWithOffsetAndSearchPhrase(appId, offset, windowSize, searchPhrase);

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
	
	
	// RMI
	/**
	 * Function to be used by RMI, it returns achievement data with specific ID
	 * @param appId applicationId
	 * @param achievementId achievementId
	 * @return Serialized JSON achievement data 
	 */
	public String getAchievementWithIdRMI(String appId, String achievementId)  {
		AchievementModel achievement;
		try {
			if(!initializeDBConnection()){
				logger.info("Cannot connect to database >> ");
				return null;
			}
			achievement = achievementAccess.getAchievementWithId(appId, achievementId);
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
			logger.warning("Get Achievement with ID RMI failed. " + e.getMessage());
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			logger.warning("Get Achievement with ID RMI failed. " + e.getMessage());
		}
		return null;
		
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

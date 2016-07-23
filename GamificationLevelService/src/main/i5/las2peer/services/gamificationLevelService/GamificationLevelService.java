package i5.las2peer.services.gamificationLevelService;

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
import i5.las2peer.services.gamificationLevelService.database.LevelDAO;
import i5.las2peer.services.gamificationLevelService.database.LevelModel;
import i5.las2peer.services.gamificationLevelService.database.SQLDatabase;
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
// TODO Adjust the following configuration
@Path("/gamification/levels")
@Version("0.1") // this annotation is used by the XML mapper
@Api( value = "/levels", authorizations = {
		@Authorization(value = "levels_auth",
		scopes = {
			@AuthorizationScope(scope = "write:levels", description = "modify levels in your application"),
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

// TODO Your own Serviceclass
public class GamificationLevelService extends Service {

	// instantiate the logger class
	private final L2pLogger logger = L2pLogger.getInstance(GamificationLevelService.class.getName());
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
	}

	private boolean initializeDBConnection() {

		this.DBManager = new SQLDatabase(this.jdbcDriverClassName, this.jdbcLogin, this.jdbcPass, this.jdbcSchema, this.jdbcHost, this.jdbcPort);
		logger.info(jdbcDriverClassName + " " + jdbcLogin);
		try {
			this.DBManager.connect();
			this.levelAccess = new LevelDAO(this.DBManager.getConnection());
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
	// Level PART --------------------------------------
	// //////////////////////////////////////////////////////////////////////////////////////
	
	// TODO Basic Single CRUD
	
	/**
	 * Post a new level
	 * @param appId applicationId
	 * @param formData form data
	 * @param contentType content type
	 * @return HttpResponse with the returnString
	 */
	@POST
	@Path("/{appId}")
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
	@ApiOperation(value = "createNewLevel",
				 notes = "A method to store a new level with details (Level number, level name, level point value, level point id)")
	public HttpResponse createNewLevel(
			@ApiParam(value = "Application ID to store a new level", required = true) @PathParam("appId") String appId,
			@ApiParam(value = "Content-type in header", required = true)@HeaderParam(value = HttpHeaders.CONTENT_TYPE) String contentType, 
			@ApiParam(value = "Level detail in multiple/form-data type", required = true)@ContentParam byte[] formData)  {
		// parse given multipart form data
		JSONObject objResponse = new JSONObject();
		int levelnum = 0;
		String levelname = null;
		int levelpointvalue = 0;
		
		boolean levelnotifcheck = false;
		String levelnotifmessage = null;
		
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
			FormDataPart partNum = parts.get("levelnum");
			if (partNum != null) {
				levelnum = Integer.parseInt(partNum.getContent());
				if(levelAccess.isLevelNumExist(appId, levelnum)){
					// level id already exist
					logger.info("Failed to add the level. Level num already exist!");
					objResponse.put("status", 1);
					objResponse.put("message", "Failed to add the level. levelnum already exist!");
					return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_INTERNAL_ERROR);

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
					levelAccess.addNewLevel(appId, model);
					logger.info("level upload success (" + levelnum +")");
					objResponse.put("status", 3);
					objResponse.put("message", "Level upload success (" + levelnum +")");
					return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_CREATED);

				} catch (SQLException e) {
					e.printStackTrace();
					System.out.println(e.getMessage());
					logger.info("SQLException >> " + e.getMessage());
					objResponse.put("status", 2);
					objResponse.put("message", "Failed to upload " + levelnum);
					return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_INTERNAL_ERROR);
				}
			}
			else{
				logger.info("Level number cannot be null");
				objResponse.put("status", 0);
				objResponse.put("message", "Level number cannot be null!");
				return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_INTERNAL_ERROR);

			}
			
			
		} catch (MalformedStreamException e) {
			// the stream failed to follow required syntax
			logger.log(Level.SEVERE, e.getMessage(), e);
			System.out.println(e.getMessage());
			logger.info("MalformedStreamException >> " + e.getMessage());
			objResponse.put("status", 2);
			objResponse.put("message", "Failed to upload " + levelnum);
			return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_BAD_REQUEST);

		} catch (IOException e) {
			// a read or write error occurred
			logger.log(Level.SEVERE, e.getMessage(), e);
			System.out.println(e.getMessage());
			logger.info("IOException >> " + e.getMessage());
			objResponse.put("status", 2);
			objResponse.put("message", "Failed to upload " + levelnum);
			return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_INTERNAL_ERROR);

		} catch (SQLException e) {
			e.printStackTrace();
			logger.info("SQLException >> " + e.getMessage());
			objResponse.put("status", 2);
			objResponse.put("message", "Failed to upload " + levelnum);
			return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_INTERNAL_ERROR);

		}
		catch (NullPointerException e){
			e.printStackTrace();
			logger.info("NullPointerException >> " + e.getMessage());
			objResponse.put("status", 2);
			objResponse.put("message", "Failed to upload " + levelnum);
			return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_INTERNAL_ERROR);

		}
	}
	
	/**
	 * Get a level data with specific ID from database
	 * @param appId applicationId
	 * @param levelNum level number
	 * @return HttpResponse Returned as JSON object
	 */
	@GET
	@Path("/{appId}/{levelNum}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Found a level"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal Error"),
			@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized")})
	@ApiOperation(value = "Find level for specific App ID and level ID", 
				  notes = "Returns a level",
				  response = LevelModel.class,
				  authorizations = @Authorization(value = "api_key")
				  )
	public HttpResponse getlevelWithNum(
			@ApiParam(value = "Application ID")@PathParam("appId") String appId,
			@ApiParam(value = "Level number")@PathParam("levelNum") int levelNum)
	{
		LevelModel level = null;
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
				if(!levelAccess.isLevelNumExist(appId, levelNum)){
					logger.info("level not found >> ");
					objResponse.put("message", "level not found");
					return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
				}
				level = levelAccess.getLevelWithNumber(appId, levelNum);
				if(level != null){
					ObjectMapper objectMapper = new ObjectMapper();
			    	//Set pretty printing of json
			    	objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
			    	
			    	String levelString = objectMapper.writeValueAsString(level);
					return new HttpResponse(levelString, HttpURLConnection.HTTP_OK);
				}
				else{
					logger.info("Cannot find level with " + levelNum);
					objResponse.put("message", "Cannot find level with " + levelNum);
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
	 * Update a level
	 * @param appId applicationId
	 * @param levelNum levelNum
	 * @param formData form data
	 * @param contentType content type
	 * @return HttpResponse with the returnString
	 */
	@PUT
	@Path("/{appId}/{levelNum}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Level Updated"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Error occured"),
			@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad request"),
			@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized")
	})
	@ApiOperation(value = "Update a level",
				 notes = "A method to update an level with details (Level number, level name, level point value, level point id)")
	public HttpResponse updateLevel(
			@ApiParam(value = "Application ID to store a new level", required = true) @PathParam("appId") String appId,
				@PathParam("levelNum") int levelNum,
			@ApiParam(value = "Level detail in multiple/form-data type", required = true)@HeaderParam(value = HttpHeaders.CONTENT_TYPE) String contentType, 
									 @ContentParam byte[] formData)  {
		// parse given multipart form data
		JSONObject objResponse = new JSONObject();

		String levelname = null;
		int levelpointvalue = 0;
		boolean levelnotifcheck = false;
		String levelnotifmessage = null;
		
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
				if(!levelAccess.isLevelNumExist(appId, levelNum)){
					logger.info("Level not found >> ");
					objResponse.put("message", "Level not found");
					return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);

				}
				
				Map<String, FormDataPart> parts = MultipartHelper.getParts(formData, contentType);
				
				if (levelNum == 0 ) {
					logger.info("Level ID cannot be null >> " );
					objResponse.put("message", "Level ID cannot be null");
					return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);

				}
					
				LevelModel model =levelAccess.getLevelWithNumber(appId, levelNum);

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
						if(levelpointvalue!=0){
							model.setPointValue(levelpointvalue);
						}
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
						levelAccess.updateLevel(appId, model);
						logger.info("Level updated >> ");
						objResponse.put("message", "Level updated");
						return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_OK);
					} catch (SQLException e) {
						e.printStackTrace();
						System.out.println(e.getMessage());
						logger.info("SQLException >> " + e.getMessage());
						objResponse.put("message", "Cannot connect to database");
						return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);

					}
				}
				else{
					// model is null
					logger.info("Level not found in database >> " );
					objResponse.put("message", "Level not found in database");
					return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);

				}
			} catch (SQLException e1) {
				e1.printStackTrace();
				logger.info("DB Error >> " + e1.getMessage());
				objResponse.put("message", "DB Error " + e1.getMessage());
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
			}
			
		} catch (MalformedStreamException e) {
			// the stream failed to follow required syntax
			logger.log(Level.SEVERE, e.getMessage(), e);
			logger.info("MalformedStreamException >> " );
			objResponse.put("message", "Failed to upload " + levelNum + ". "+e.getMessage());
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
		} catch (IOException e) {
			// a read or write error occurred
			logger.log(Level.SEVERE, e.getMessage(), e);
			logger.info("IOException >> " );
			objResponse.put("message", "Failed to upload " + levelNum + ".");
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
		}

	}
	

	/**
	 * Delete a level data with specified ID
	 * @param appId applicationId
	 * @param levelNum levelNum
	 * @return HttpResponse with the returnString
	 */
	@DELETE
	@Path("/{appId}/{levelNum}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Level Delete Success"),
			@ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Level not found"),
			@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad Request"),
	})
	@ApiOperation(value = "",
				  notes = "delete a level")
	public HttpResponse deleteLevel(@PathParam("appId") String appId,
								 @PathParam("levelNum") int levelNum)
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
			if(!levelAccess.isLevelNumExist(appId, levelNum)){
				logger.info("Level not found >> ");
				objResponse.put("message", "Level not found");
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);

			}
			
			levelAccess.deleteLevel(appId, levelNum);
			logger.info(" Deleted >> ");
			objResponse.put("message", "Level Deleted");
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_OK);

		} catch (SQLException e) {
			
			e.printStackTrace();
			logger.info("Cannot delete level. >> " +e.getMessage());
			objResponse.put("message", "Cannot delete level.");
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
		}
	}

	
	// TODO Batch Processing
	/**
	 * Get a list of levels from database
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
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Found a list of levels"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal Error"),
			@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized")})
	@ApiOperation(value = "Find levels for specific App ID", 
				  notes = "Returns a list of levels",
				  response = LevelModel.class,
				  responseContainer = "List",
				  authorizations = @Authorization(value = "api_key")
				  )
	public HttpResponse getLevelList(
			@ApiParam(value = "Application ID to return")@PathParam("appId") String appId,
			@ApiParam(value = "Page number for retrieving data")@QueryParam("current") int currentPage,
			@ApiParam(value = "Number of data size")@QueryParam("rowCount") int windowSize,
			@ApiParam(value = "Search phrase parameter")@QueryParam("searchPhrase") String searchPhrase)
	{
		List<LevelModel> model = null;
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
			
			ObjectMapper objectMapper = new ObjectMapper();
	    	//Set pretty printing of json
	    	objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
			
			int totalNum = levelAccess.getNumberOfLevels(appId);
			model = levelAccess.getLevelsWithOffsetAndSearchPhrase(appId, offset, windowSize, searchPhrase);
			String modelString = objectMapper.writeValueAsString(model);
			JSONArray modelArray = (JSONArray) JSONValue.parse(modelString);
			logger.info(modelArray.toJSONString());
			objResponse.put("current", currentPage);
			objResponse.put("rowCount", windowSize);
			objResponse.put("rows", modelArray);
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

	public boolean isAppWithIdExist(String appId) throws SQLException, AgentNotKnownException, L2pServiceException, L2pSecurityException, InterruptedException, TimeoutException{
		
		Object result = this.invokeServiceMethod("i5.las2peer.services.gamificationApplicationService.GamificationApplicationService@0.1", "isAppWithIdExist", new Serializable[] { appId });
		
		if (result != null) {
			if((int)result == 1){
				return true;
			}
		}
		return false;
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

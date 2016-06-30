package i5.las2peer.services.gamificationManagerService;

import java.io.IOException;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;

import org.apache.commons.fileupload.MultipartStream.MalformedStreamException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import i5.las2peer.api.Service;
import i5.las2peer.logging.L2pLogger;
import i5.las2peer.logging.NodeObserver.Event;
import i5.las2peer.p2p.AgentAlreadyRegisteredException;
import i5.las2peer.p2p.AgentNotKnownException;
import i5.las2peer.p2p.Node;
import i5.las2peer.restMapper.HttpResponse;
import i5.las2peer.restMapper.MediaType;
import i5.las2peer.restMapper.RESTMapper;
import i5.las2peer.restMapper.*;
import i5.las2peer.restMapper.annotations.ContentParam;
import i5.las2peer.restMapper.annotations.Version;
import i5.las2peer.restMapper.tools.ValidationResult;
import i5.las2peer.restMapper.tools.XMLCheck;
import i5.las2peer.security.Agent;
import i5.las2peer.security.AgentException;
import i5.las2peer.security.GroupAgent;
import i5.las2peer.security.L2pSecurityException;
import i5.las2peer.security.UserAgent;
import i5.las2peer.services.badgeService.database.BadgeDAO;
import i5.las2peer.services.badgeService.database.BadgeModel;
import i5.las2peer.services.badgeService.helper.FormDataPart;
import i5.las2peer.services.badgeService.helper.MultipartHelper;
import i5.las2peer.services.gamificationManagerService.database.ApplicationManagerDAO;
import i5.las2peer.services.gamificationManagerService.database.ApplicationModel;
import i5.las2peer.services.gamificationManagerService.database.SQLDatabase;
import i5.las2peer.services.gamificationManagerService.database.MemberModel;
import i5.las2peer.services.gamificationManagerService.helper.IdGenerator;
import i5.las2peer.tools.CryptoException;
import i5.las2peer.tools.SerializationException;
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
import net.minidev.json.parser.ParseException;


// TODO Describe your own service
/**
 * LAS2peer Service
 * 
 * This is a template for a very basic LAS2peer service
 * that uses the LAS2peer Web-Connector for RESTful access to it.
 * 
 * Note:
 * If you plan on using Swagger you should adapt the information below
 * in the ApiInfo annotation to suit your project.
 * If you do not intend to provide a Swagger documentation of your service API,
 * the entire ApiInfo annotation should be removed.
 * 
 */
// TODO Adjust the following configuration
@Path("/manager")
@Version("0.1") // this annotation is used by the XML mapper
@Api( value = "/manager", authorizations = {
		@Authorization(value = "game_auth",
		scopes = {
			@AuthorizationScope(scope = "write:manager", description = "modify apps in your application"),
			@AuthorizationScope(scope = "read:manager", description = "read your apps")
				  })
}, tags = "manager")
@SwaggerDefinition(
		info = @Info(
				title = "Gamification Manager Service",
				version = "0.1",
				description = "Gamification Manager Service for Gamification Framework",
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
public class GamificationManagerService extends Service {

	// instantiate the logger class
	private final L2pLogger logger = L2pLogger.getInstance(GamificationManagerService.class.getName());
	/*
	 * Database configuration
	 */
	private String jdbcDriverClassName;
	private String jdbcLogin;
	private String jdbcPass;
	private String jdbcHost;
	private int jdbcPort;
	private String jdbcSchema;
	private String jdbcManagerSchema;
	private String epURL;

	private SQLDatabase managerDBManager;
	private ApplicationManagerDAO managerAccess;
	
	public GamificationManagerService() {
		// read and set properties values
		// IF THE SERVICE CLASS NAME IS CHANGED, THE PROPERTIES FILE NAME NEED TO BE CHANGED TOO!
		setFieldValues();
	}

	private void initializeManagerDBConnection() throws SQLException {
		
		
		
		this.managerDBManager = new SQLDatabase(this.jdbcDriverClassName, this.jdbcLogin, this.jdbcPass, this.jdbcManagerSchema, this.jdbcHost, this.jdbcPort);
		logger.info(jdbcDriverClassName + " " + jdbcLogin);
		try {
				this.managerDBManager.connect();
				this.managerAccess = new ApplicationManagerDAO(this.managerDBManager.getConnection());
				logger.info("Monitoring: Database connected!");
			} catch (Exception e) {
				e.printStackTrace();
				logger.info("Monitoring: Could not connect to database!. " + e.getMessage());
				
			}
		
	}
	// //////////////////////////////////////////////////////////////////////////////////////
	// Service methods.
	// //////////////////////////////////////////////////////////////////////////////////////
	
	
	// TODO OWN METHODS
	
//	/**
//	 * Template of a get function.
//	 * 
//	 * @return HttpResponse with the returnString
//	 */
//	@GET
//	@Produces(MediaType.TEXT_HTML)
//	@Path("/page")
//	public HttpResponse getGamificationManagerHTML(
//			@HeaderParam(HttpHeaders.ACCEPT_LANGUAGE) String lang){
//		String onAction = "retrieving Gamification Manager HTML";
//
//		// only respond with template; nothing to be adapted
//		try {
//			// load template
////			String html = new Scanner(new File("./etc/html/questionnaires-template.html")).useDelimiter("\\A").next();
//			String html = new Scanner(new File("./frontend/gamificationmanager.html")).useDelimiter("\\A").next();
//
////			// localize template
////			html = i18n(html, lang);
////
////			// fill in placeholders
////			html = fillPlaceHolder(html,"EP_URL", epUrl);
////			html = fillPlaceHolder(html,"SC_URL", staticContentUrl);
////
////			html = fillPlaceHolder(html,"OIDC_PROV_NAME", oidcProviderName);
////			html = fillPlaceHolder(html,"OIDC_PROV_LOGO", oidcProviderLogo);
////			html = fillPlaceHolder(html,"OIDC_PROV_URL", oidcProviderUrl);
////			html = fillPlaceHolder(html,"OIDC_CLNT_ID", oidcClientId);
//
//			// finally return resulting HTML
//			return new HttpResponse(html, HttpURLConnection.HTTP_OK);
//		} catch (FileNotFoundException e) {
//			return new HttpResponse(onAction, HttpURLConnection.HTTP_INTERNAL_ERROR);
//		}
//	}
	
	/**
	 * 
	 * @return HttpResponse 
	 */
	@GET
	@Path("/validation")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Member Validation",
			notes = "Simple function to validate a member login.")
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Validation Confirmation"),
			@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized")
	})
	public HttpResponse memberLoginValidation() {
		JSONObject objResponse = new JSONObject();
			
			MemberModel member;
			UserAgent userAgent = (UserAgent) getContext().getMainAgent();
			// take username as default name
			String name = userAgent.getLoginName();
			if(!name.equals("anonymous")){
				try {
					initializeManagerDBConnection();
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
							if(!managerAccess.isMemberRegistered(member.getId())){
								managerAccess.registerMember(member);
								objResponse.put("message", "Welcome " + member.getId() + "!");
								return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_OK);
							}
						} else {
							logger.warning("Parsing user data failed! Got '" + jsonUserData.getClass().getName() + "' instead of "
									+ JSONObject.class.getName() + " expected!");
							objResponse.put("message", "User data error to be retrieved. Not JSON object.");
							return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
						}
						objResponse.put("message", "Member already registered");
						return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_OK);
					}
					else{
						objResponse.put("message", "User data error to be retrieved.");
						return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
					}
				
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					objResponse.put("message", "Database Error.");
					return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
				}
			}
			else
			{
				objResponse.put("message", "You are anonymous.");
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_UNAUTHORIZED);
			}
				
		
	}

	/**
	 * Get all application list
	 * 
	 * @return HttpResponse with the returnString
	 */
	@GET
	@Path("/apps")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "REPLACE THIS WITH AN APPROPRIATE FUNCTION NAME",
			notes = "REPLACE THIS WITH YOUR NOTES TO THE FUNCTION")
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "REPLACE THIS WITH YOUR OK MESSAGE"),
			@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized")
	})
	public HttpResponse getAllAppInfo() {
		JSONObject objResponse = new JSONObject();
		UserAgent userAgent = (UserAgent) getContext().getMainAgent();
		String name = userAgent.getLoginName();
		if(!name.equals("anonymous")){
			try {
				initializeManagerDBConnection();
				ObjectMapper objectMapper = new ObjectMapper();
		    	//Set pretty printing of json
		    	objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
				

				List<List<ApplicationModel>> allApps = managerAccess.getSeparateApplicationsWithMemberId(name);
				for(ApplicationModel app: allApps.get(0)){
					logger.info(app.getId());					
				}
				for(ApplicationModel app: allApps.get(1)){
					logger.info(app.getId());					
				}
				
				try {
					String response = objectMapper.writeValueAsString(allApps);
					allApps.clear();
					
					return new HttpResponse(response, HttpURLConnection.HTTP_OK);

				
				} catch (JsonProcessingException e) {
					e.printStackTrace();
					
					allApps.clear();
					// return HTTP Response on error
					objResponse.put("success", false);
					objResponse.put("message", "Internal Error. JsonProcessingException.");
					return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_OK);

				}
			} catch (SQLException e) {
				
				e.printStackTrace();

				objResponse.put("success", false);
				objResponse.put("message", "Database error");
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_OK);
			}
		}
		else{
			objResponse.put("success", false);
			objResponse.put("message", "You are not authorized");
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_UNAUTHORIZED);

		}
	}

//	/**
//	 * Get all application for the user
//	 * 
//	 * @return HttpResponse with the returnString
//	 */
//	@GET
//	@Path("/users/{userId}/apps")
//	@Produces(MediaType.APPLICATION_JSON)
//	@ApiOperation(value = "REPLACE THIS WITH AN APPROPRIATE FUNCTION NAME",
//			notes = "REPLACE THIS WITH YOUR NOTES TO THE FUNCTION")
//	@ApiResponses(value = {
//			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "REPLACE THIS WITH YOUR OK MESSAGE"),
//			@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized")
//	})
//	public HttpResponse getAppInfoForUser(@PathParam("userId") String userId) {
//		// userId is not really useful because we use las2peer useragent loginname
//		JSONObject objResponse = new JSONObject();
//		UserAgent userAgent = (UserAgent) getContext().getMainAgent();
//		String name = userAgent.getLoginName();
//		if(!name.equals("anonymous")){
//			String response;
//			try {
//				initializeManagerDBConnection();
//				ObjectMapper objectMapper = new ObjectMapper();
//		    	//Set pretty printing of json
//		    	objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
//				List<ApplicationModel> apps = userAccess.getAllApplicationsOfUser(name);
//
//				try {
//					response = objectMapper.writeValueAsString(apps);
//					apps.clear();
//					
//					return new HttpResponse(response, HttpURLConnection.HTTP_OK);
//				
//				} catch (JsonProcessingException e) {
//					e.printStackTrace();
//					apps.clear();
//					// return HTTP Response on error
//
//					logger.info("JsonProcessingException >> ");
//					objResponse.put("message", "JsonProcessingException");
//					return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
//
//				}
//			} catch (SQLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//				logger.info("SQLException >> ");
//				objResponse.put("message", "SQL Exception");
//				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
//			}
//			
//		}
//		else{
//			logger.info("Unauthorized >> ");
//			objResponse.put("message", "You are not authorized");
//			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_UNAUTHORIZED);
//
//		}	
//		
//		
//	}
	
	/**
	 * Create a new app
	 * 
	 * @param contentType form content type
	 * @param formData form data
	 * @return HttpResponse with the returnString
	 */
	@POST
	@Path("/apps")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Create a new application",
			notes = "Method to create a new application")
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "OK"),
			@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized")
	})
	public HttpResponse createNewApp(
			@ApiParam(value = "App detail in multiple/form-data type", required = true)@HeaderParam(value = HttpHeaders.CONTENT_TYPE) String contentType,
			@ContentParam byte[] formData) {
		JSONObject objResponse = new JSONObject();
		UserAgent userAgent = (UserAgent) getContext().getMainAgent();
		String name = userAgent.getLoginName();
		String appid = null;
		String appdesc = null;
		String commtype = null;
		if(!name.equals("anonymous")){
			try {
				initializeManagerDBConnection();
				Map<String, FormDataPart> parts = MultipartHelper.getParts(formData, contentType);
				
				FormDataPart partAppID = parts.get("appid");
				if (partAppID != null) {
					// these data belong to the (optional) file id text input form element
					appid = partAppID.getContent();
					// appid must be unique
					if(!managerAccess.isAppIdExist(appid)){
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
						
						

						// Create new app db
						if(managerAccess.createNewApplicationDB(appid)){
							// Create a new GroupAgent
							GroupAgent groupAgent = GroupAgent.createGroupAgent(new Agent[]{userAgent});
							groupAgent.unlockPrivateKey(userAgent);
							groupAgent.setName(appid);
							getContext().getLocalNode().storeAgent(groupAgent);
							logger.info("Group Agent " + groupAgent.getName() + " " + groupAgent.toString() + " " + groupAgent.isMember(userAgent));
							
							ApplicationModel newApp = new ApplicationModel(appid, appdesc, commtype);
							managerAccess.addAppToDB(newApp);
							
							// add Member to App
							managerAccess.addMemberToApp(appid, name);
							
							objResponse.put("success", true);

							System.out.println(objResponse.toJSONString());
							logger.info(objResponse.toJSONString());
							return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_CREATED);
						}
						else{
							objResponse.put("message", "Error cannot create database");

							System.out.println(objResponse.toJSONString());
							logger.info(objResponse.toJSONString());
							return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
						}
						
						
					}
					else{
						// app id already exist
						objResponse.put("message", "App ID already exist");

						System.out.println(objResponse.toJSONString());
						logger.info(objResponse.toJSONString());
						return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
					}
				}
				else{
					// app id cannot be empty
					objResponse.put("message", "App id cannot be empty");

					System.out.println(objResponse.toJSONString());
					logger.info(objResponse.toJSONString());
					return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);

				}
				
			} catch (MalformedStreamException e) {
				// the stream failed to follow required syntax
				logger.log(Level.SEVERE, e.getMessage(), e);
				logger.info("MalformedStreamException >> " + e.getMessage());
				objResponse.put("success", false);
				objResponse.put("message", "MalformedStreamException");
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.info("SQLException >> " + e.getMessage());
				objResponse.put("success", false);
				objResponse.put("message", "Database error");
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.info("IOException >> " + e.getMessage());
				objResponse.put("success", false);
				objResponse.put("message", "IO Exception");
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);

			} catch (L2pSecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.info("L2pSecurityException >> " + e.getMessage());
				objResponse.put("success", false);
				objResponse.put("message", "Las2peer security exception");
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);

			} catch (CryptoException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.info("CryptoException >> " + e.getMessage());
				objResponse.put("success", false);
				objResponse.put("message", "CryptoException");
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);

			} catch (SerializationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.info("SerializationException >> " + e.getMessage());
				objResponse.put("success", false);
				objResponse.put("message", "SerializationException");
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);

			} catch (AgentAlreadyRegisteredException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.info("AgentAlreadyRegisteredException >> " + e.getMessage());
				objResponse.put("success", false);
				objResponse.put("message", "AgentAlreadyRegisteredException");
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);

			} catch (AgentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.info("AgentException >> " + e.getMessage());
				objResponse.put("success", false);
				objResponse.put("message", "AgentException");
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);

			}
			
		}
		else{
			logger.info("You are not authorized >> " );
			objResponse.put("success", false);
			objResponse.put("message", "You are not authorized");
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_UNAUTHORIZED);

		}
	}
	
	

	/**
	 * Get an app data with specified ID
	 * @param appId applicationId
	 * @return HttpResponse with the returnString
	 */
	@GET
	@Path("/apps/{appId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "App Selected"),
			@ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "App not found"),
			@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad Request"),
	})
	@ApiOperation(value = "",
				  notes = "Select an App")
	public HttpResponse getAppDetails(@PathParam("appId") String appId)
	{
		JSONObject objResponse = new JSONObject();
		UserAgent userAgent = (UserAgent) getContext().getMainAgent();
		String name = userAgent.getLoginName();
		if(!name.equals("anonymous")){
			try {
				initializeManagerDBConnection();
//				appAccess.deleteApp(appId);
				// Add Member to App
				managerAccess.addMemberToApp(appId, name);
				
				objResponse.put("message", "Application selected");
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_OK);
			} catch (SQLException e) {
				
				e.printStackTrace();

				logger.info("SQLException >> " + e.getMessage());
				objResponse.put("success", false);
				objResponse.put("message", "Database error");
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_OK);
			}
		}
		else{
			logger.info("Unauthorized >> ");
			objResponse.put("success", false);
			objResponse.put("message", "You are not authorized");
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_UNAUTHORIZED);

		}	
		}
	
	/**
	 * Remove a member from the application
	 * @param appId applicationId
	 * @param memberId memberId
	 * @return HttpResponse with the returnString
	 */
	@DELETE
	@Path("/apps/{appId}/{memberId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "App Delete Success"),
			@ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "App not found"),
			@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad Request"),
	})
	@ApiOperation(value = "",
				  notes = "delete a user from an app")
	public HttpResponse removeUserFromApp(@PathParam("appId") String appId,@PathParam("memberId") String memberId)
	{
		JSONObject objResponse = new JSONObject();
		UserAgent userAgent = (UserAgent) getContext().getMainAgent();
		String name = userAgent.getLoginName();
		if(!name.equals("anonymous")){
			try {
//				initializeDBConnection();
				initializeManagerDBConnection();
				managerAccess.removeMemberFromApp(name, appId);
				objResponse.put("success", true);
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_OK);

				}
		 catch (SQLException e) {
				
				e.printStackTrace();

				logger.info("SQLException >> " + e.getMessage());
				objResponse.put("success", false);
				objResponse.put("message", "Database error");
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_OK);
			}
		}
		else{

			logger.info("Unauthorized >> ");
			objResponse.put("success", false);
			objResponse.put("message", "You are not authorized");
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_UNAUTHORIZED);

		}
	}
	
	/**
	 * Delete an app data with specified ID
	 * @param appId applicationId
	 * @return HttpResponse with the returnString
	 */
	@DELETE
	@Path("/apps/{appId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "App Delete Success"),
			@ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "App not found"),
			@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad Request"),
	})
	@ApiOperation(value = "",
				  notes = "delete an App")
	public HttpResponse deleteApp(@PathParam("appId") String appId)
	{
		JSONObject objResponse = new JSONObject();
		UserAgent userAgent = (UserAgent) getContext().getMainAgent();
		String name = userAgent.getLoginName();
		if(!name.equals("anonymous")){
			try {
//				initializeDBConnection();
				initializeManagerDBConnection();
				
					// DROP App DB
					// Remove Group Agent
					
					GroupAgent gAgent = null;
					GroupAgent[] gAgents = getContext().getGroupAgents();
					for(GroupAgent g : gAgents){
						if(g.getName().equals(appId)){
							logger.info(g.getName());
							gAgent = g;
							break;
						}
					}
					if(gAgent != null){
						logger.info("Group agent null");
						Node agentNode = gAgent.getRunningAtNode();
						agentNode.unregisterAgent(gAgent);
						if(managerAccess.deleteApp(appId)){
							objResponse.put("message", "Application deleted");
							return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_OK);
						}
						else{
							logger.info("Database error >> ");
							objResponse.put("success", false);
							objResponse.put("message", "Database error");
							return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
						}
					}
					else{
						objResponse.put("message", "Group agent null");
						return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
					}
				
			} catch (SQLException e) {
				
				e.printStackTrace();

				logger.info("SQLException >> " + e.getMessage());
				objResponse.put("success", false);
				objResponse.put("message", "Database error");
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
			}

			 catch (AgentNotKnownException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.info("AgentNotKnownException >> " + e.getMessage());
				objResponse.put("success", false);
				objResponse.put("message", "AgentNotKnownException");
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);

			}
		}
		else{

			logger.info("Unauthorized >> ");
			objResponse.put("success", false);
			objResponse.put("message", "You are not authorized");
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_UNAUTHORIZED);

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

package i5.las2peer.services.gamificationPointService;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.sql.SQLException;
import java.util.logging.Level;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import i5.las2peer.api.Service;
import i5.las2peer.execution.L2pServiceException;
import i5.las2peer.logging.L2pLogger;
import i5.las2peer.logging.NodeObserver.Event;
import i5.las2peer.p2p.AgentNotKnownException;
import i5.las2peer.p2p.TimeoutException;
import i5.las2peer.restMapper.HttpResponse;
import i5.las2peer.restMapper.MediaType;
import i5.las2peer.restMapper.RESTMapper;
import i5.las2peer.restMapper.annotations.Version;
import i5.las2peer.restMapper.tools.ValidationResult;
import i5.las2peer.restMapper.tools.XMLCheck;
import i5.las2peer.security.L2pSecurityException;
import i5.las2peer.security.UserAgent;
import i5.las2peer.services.gamificationApplicationService.database.ApplicationDAO;
import i5.las2peer.services.gamificationApplicationService.database.SQLDatabase;
import i5.las2peer.services.gamificationPointService.helper.LocalFileManager;
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

// TODO Describe your own service
/**
 * Gamification Point Service
 * 
 * This is Gamification Point service to manage point in Gamification Framework
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
@Path("/gamification/points")
@Version("0.1") // this annotation is used by the XML mapper
@Api( value = "/gamification/points", authorizations = {
		@Authorization(value = "points_auth",
		scopes = {
			@AuthorizationScope(scope = "write:points", description = "modify point in your application"),
			@AuthorizationScope(scope = "read:points", description = "read your point configuration")
				  })
}, tags = "points")
@SwaggerDefinition(
		info = @Info(
				title = "Gamification Point Service",
				version = "0.1",
				description = "Gamification Point Service for Gamification Framework",
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
public class GamificationPointService extends Service {

	// instantiate the logger class
	private final L2pLogger logger = L2pLogger.getInstance(GamificationPointService.class.getName());
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
	
	public GamificationPointService() {
		// read and set properties values
		// IF THE SERVICE CLASS NAME IS CHANGED, THE PROPERTIES FILE NAME NEED TO BE CHANGED TOO!
		setFieldValues();
	}

	private boolean initializeDBConnection() {

		this.DBManager = new SQLDatabase(this.jdbcDriverClassName, this.jdbcLogin, this.jdbcPass, this.jdbcSchema, this.jdbcHost, this.jdbcPort);
		logger.info(jdbcDriverClassName + " " + jdbcLogin);
		try {
				this.DBManager.connect();
				logger.info("Monitoring: Database connected!");
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				logger.info("Monitoring: Could not connect to database!. " + e.getMessage());
				return false;
			}
	}
	
	private JSONObject fetchConfigurationToSystem(String appId) throws IOException {
		String confPath = LocalFileManager.getBasedir()+"/"+appId+"/conf.json";
		// RMI call without parameters
		File appFolder = new File(LocalFileManager.getBasedir()+"/"+appId);
		if(!appFolder.exists()){
			if(appFolder.mkdir()){
				System.out.println("New directory "+ appId +" is created!");
			}
			else{
				System.out.println("Failed to create directory");
			}
		}
		File fileconf = new File(confPath);
		if(!fileconf.exists()) {
			if(fileconf.createNewFile()){
				// Initialize
				LocalFileManager.writeFile(confPath, "{}");
				System.out.println("New file is created!");
			}
			else{
				System.out.println("Failed to create file");
			}
		} 
		String confJSONByte = new String(LocalFileManager.getFile(appId+"/conf.json"));
		return (JSONObject) JSONValue.parse(confJSONByte);
	}
	
	/**
	 * Function to store configuration
	 * @param appId appId
	 * @param obj JSON object
	 * @throws IOException 
	 */
	private void storeConfigurationToSystem(String appId, JSONObject obj) throws IOException{
		String confPath = LocalFileManager.getBasedir()+"/"+appId+"/conf.json";
			// RMI call without parameters
		File appFolder = new File(LocalFileManager.getBasedir()+"/"+appId);
		if(!appFolder.exists()){
			if(appFolder.mkdir()){
				System.out.println("New directory "+ appId +" is created!");
			}
			else{
				System.out.println("Failed to create directory");
			}
		}
		File fileconf = new File(confPath);
		if(!fileconf.exists()) {
			if(fileconf.createNewFile()){
				// Initialize
				LocalFileManager.writeFile(confPath, "{}");
				System.out.println("New file is created!");
			}
			else{
				System.out.println("Failed to create file");
			}
		} 
		LocalFileManager.writeFile(LocalFileManager.getBasedir()+"/"+appId+"/conf.json", obj.toJSONString());
	}
	
	/**
	 * Function to store configuration
	 * @param appId appId
	 * @throws IOException 
	 */
	private boolean cleanStorage(String appId){
			// RMI call without parameters
		File appFolder = new File(LocalFileManager.getBasedir()+"/"+appId);
		
		try {
			recursiveDelete(appFolder);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

    }
	
	private void recursiveDelete(File appFolder) throws IOException{
		if(appFolder.isDirectory()){
    		//directory is empty, then delete it
    		if(appFolder.list().length==0){
    			appFolder.delete();
    		   System.out.println("Directory is deleted : " 
                                                 + appFolder.getAbsolutePath());
    		}else{
    			
    		   //list all the directory contents
        	   String files[] = appFolder.list();
     
        	   for (String temp : files) {
        	      //construct the file structure
        	      File fileDelete = new File(appFolder, temp);
        		 
        	      //recursive delete
        	      recursiveDelete(fileDelete);
        	   }
        		
        	   //check the directory again, if empty then delete it
        	   if(appFolder.list().length==0){
        		   appFolder.delete();
        	     System.out.println("Directory is deleted : " + appFolder.getAbsolutePath());
        	   }
    		}
    	}else{
    		//if file, then delete it
    		appFolder.delete();
    		System.out.println("File is deleted : " + appFolder.getAbsolutePath());
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
	// POINT PART --------------------------------------
	// //////////////////////////////////////////////////////////////////////////////////////
	// TODO
	/**
	 * Change point unit name
	 * @param appId applicationId
	 * @param unitName point unit name
	 * @return HttpResponse Returned as JSON object
	 */
	@PUT
	@Path("/{appId}/name/{unitName}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Unit name changed"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal Error"),
			@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized")})
	@ApiOperation(value = "", 
				  notes = "",
				  responseContainer = "",
				  authorizations = @Authorization(value = "api_key")
				  )
	public HttpResponse changeUnitName(
			@ApiParam(value = "Application ID to return")@PathParam("appId") String appId,
			@ApiParam(value = "Point unit name")@PathParam("unitName") String unitName)
	{
		JSONObject objResponse = new JSONObject();
		UserAgent userAgent = (UserAgent) getContext().getMainAgent();
		String name = userAgent.getLoginName();
		if(name.equals("anonymous")){
			return unauthorizedMessage();
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
		} catch (SQLException e) {
			e.printStackTrace();
			logger.info( "Internal Error. Database connection failed. >> " + e.getMessage());
			objResponse.put("message",  "Internal Error. Database connection failed. " + e.getMessage());
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
		}
		if(unitName != null){
			JSONObject objRetrieve;
			try {
				objRetrieve = fetchConfigurationToSystem(appId);
				objRetrieve.put("pointUnitName", unitName);
				storeConfigurationToSystem(appId, objRetrieve);
				logger.info(objRetrieve.toJSONString());
				objResponse.put("message", "Unit name "+unitName+" is updated");
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_OK);
			} catch (IOException e) {
				e.printStackTrace();
				String response = "IO Exception. ";
				
				// return HTTP Response on error
				return new HttpResponse(response+e.getMessage(), HttpURLConnection.HTTP_INTERNAL_ERROR);
			}
		}
		else{
			// Unit name is null
			String response = "Unit Name cannot be null";
			
			// return HTTP Response on error
			return new HttpResponse(response, HttpURLConnection.HTTP_INTERNAL_ERROR);

		}
	}
	
	/**
	 * Fetch point unit name
	 * @param appId applicationId
	 * @return HttpResponse Returned as JSON object
	 */
	@GET
	@Path("/{appId}/name")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Unit name"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal Error"),
			@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized")})
	@ApiOperation(value = "", 
				  notes = "",
				  responseContainer = "",
				  authorizations = @Authorization(value = "api_key")
				  )
	public HttpResponse getUnitName(
			@ApiParam(value = "Application ID to return")@PathParam("appId") String appId)
	{
		JSONObject objResponse = new JSONObject();
		UserAgent userAgent = (UserAgent) getContext().getMainAgent();
		String name = userAgent.getLoginName();
		if(name.equals("anonymous")){
			return unauthorizedMessage();
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
		} catch (SQLException e) {
			e.printStackTrace();
			logger.info( "Internal Error. Database connection failed. >> " + e.getMessage());
			objResponse.put("message",  "Internal Error. Database connection failed. " + e.getMessage());
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
		}
		JSONObject objRetrieve;
		try {
			objRetrieve = fetchConfigurationToSystem(appId);
			String pointUnitName = (String) objRetrieve.get("pointUnitName");
			if(pointUnitName==null){
				objRetrieve.put("pointUnitName", "");
			}
			return new HttpResponse(objRetrieve.toJSONString(), HttpURLConnection.HTTP_OK);
	

		} catch (IOException e) {
			e.printStackTrace();
			String response = "IO Exception. ";
			
			// return HTTP Response on error
			return new HttpResponse(response+e.getMessage(), HttpURLConnection.HTTP_INTERNAL_ERROR);

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

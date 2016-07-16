package i5.las2peer.services.gamificationApplicationService;

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
import i5.las2peer.services.gamificationApplicationService.database.ApplicationDAO;
import i5.las2peer.services.gamificationApplicationService.database.ApplicationModel;
import i5.las2peer.services.gamificationApplicationService.database.MemberModel;
import i5.las2peer.services.gamificationApplicationService.database.SQLDatabase;
import i5.las2peer.services.gamificationApplicationService.helper.FormDataPart;
import i5.las2peer.services.gamificationApplicationService.helper.LocalFileManager;
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
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import net.minidev.json.parser.ParseException;


/**
 * Gamification Application Service
 * 
 * This is Gamification Application service to manage top level application in Gamification Framework
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
@Path("/gamification/applications")
@Version("0.1") // this annotation is used by the XML mapper
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

// TODO Your own Serviceclass
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
	
	private SQLDatabase DBManager;
	private ApplicationDAO managerAccess;
	
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

	private boolean initializeDBConnection() {

		this.DBManager = new SQLDatabase(this.jdbcDriverClassName, this.jdbcLogin, this.jdbcPass, this.jdbcSchema, this.jdbcHost, this.jdbcPort);
		logger.info(jdbcDriverClassName + " " + jdbcLogin);
		try {
				this.DBManager.connect();
				this.managerAccess = new ApplicationDAO(this.DBManager.getConnection());
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
	
	private static String stringfromJSON(JSONObject obj, String key) throws IOException {
		String s = (String) obj.get(key);
		if (s == null) {
			throw new IOException("Key " + key + " is missing in JSON");
		}
		return s;
	}

	private static List<String> stringArrayfromJSON(JSONObject obj, String key) throws IOException {
		JSONArray arr = (JSONArray)obj.get(key);
		List<String> ss = new ArrayList<String>();
		if (arr == null) {
			throw new IOException("Key " + key + " is missing in JSON");
		}
		for(int i = 0; i < arr.size(); i++){
			ss.add((String) arr.get(i));
		}
		return ss;
	}

	private static int intfromJSON(JSONObject obj, String key) {
		return (int) obj.get(key);
	}

	private static boolean boolfromJSON(JSONObject obj, String key) {
		try {
			return (boolean) obj.get(key);
		} catch (Exception e) {
			String b = (String) obj.get(key);
			if (b.equals("1")) {
				return true;
			}
			return (boolean)Boolean.parseBoolean(b);
		}
	}
	
	private static List<Pair<String, Integer>> listPairfromJSON(JSONObject obj, String mainkey, String keykey, String keyval) throws IOException {
		
		List<Pair<String, Integer>> listpair = new ArrayList<Pair<String, Integer>>();
		JSONArray arr = (JSONArray)obj.get(mainkey);
		
		if (arr == null) {
			throw new IOException("Key " + mainkey + " is missing in JSON");
		}
		for (int i = 0; i < arr.size(); i++)
	    {
	      JSONObject objectInArray = (JSONObject) arr.get(i);
	      String key = (String) objectInArray.get(keykey);
	      if (key == null) {
				throw new IOException("Key " + keykey + " is missing in JSON");
			}
	      Integer val = (Integer) objectInArray.get(keyval);
	      if (val == null) {
				throw new IOException("Key " + keyval + " is missing in JSON");
			}
	      listpair.add(Pair.of(key,val));
	    }
		return listpair;
	}
	
	private static JSONArray listPairtoJSONArray(List<Pair<String, Integer>> listpair) throws IOException {
		JSONArray arr = new JSONArray();

		if (listpair.isEmpty() || listpair.equals(null)) {
			throw new IOException("List pair is empty");
		}
		for(Pair<String, Integer> pair : listpair){
			JSONObject obj = new JSONObject();
			obj.put("actionId", pair.getLeft());
			obj.put("times", pair.getRight());
			arr.add(obj);
		}
		return arr;
	}
	
	// //////////////////////////////////////////////////////////////////////////////////////
	// Service methods.
	// //////////////////////////////////////////////////////////////////////////////////////
	
	
	// //////////////////////////////////////////////////////////////////////////////////////
	// Application PART --------------------------------------
	// //////////////////////////////////////////////////////////////////////////////////////
	
	// TODO Basic single CRUD -------------------------------------
	
	/**
	 * Create a new app
	 * 
	 * @param contentType form content type
	 * @param formData form data
	 * @return HttpResponse with the returnString
	 */
	@POST
	@Path("/data")
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
		if(name.equals("anonymous")){
			return unauthorizedMessage();
		}
		if(!initializeDBConnection()){
			logger.info("Cannot connect to database >> ");
			objResponse.put("message", "Cannot connect to database");
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
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
				if(managerAccess.isAppIdExist(appid)){
					// app id already exist
					objResponse.put("message", "App ID already exist");

					System.out.println(objResponse.toJSONString());
					logger.info(objResponse.toJSONString());
					return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
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
//					if(managerAccess.addNewApplicationInfo(newApp)){
//						// add Member to App
//						if(managerAccess.createApplicationDB(newApp.getId())){
//							try {
//								managerAccess.addMemberToApp(newApp.getId(), name);
//								objResponse.put("message", "New schema created");
//								logger.info(objResponse.toJSONString());
//								return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_CREATED);
//
//							} catch (SQLException e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//								logger.info("SQLException >> " + e.getMessage());
//								objResponse.put("message", "Database Error");
//								return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
//
//							}
//						
//						}
//					}
//					objResponse.put("message", "Cannot add new application");
//
//					System.out.println(objResponse.toJSONString());
//					logger.info(objResponse.toJSONString());
//					return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
				try{
					managerAccess.addNewApplication(newApp);
					managerAccess.addMemberToApp(newApp.getId(), name);
					objResponse.put("message", "New schema created");
					logger.info(objResponse.toJSONString());
					return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_CREATED);

				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					logger.info("SQLException >> " + e.getMessage());
					objResponse.put("message", "Database Error");
					return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);

				}
			}
			else{
				// app id cannot be empty
				objResponse.put("message", "App ID cannot be empty");

				System.out.println(objResponse.toJSONString());
				logger.info(objResponse.toJSONString());
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
			}
			
		}
		catch (IOException e) {
			e.printStackTrace();
			logger.info("IOException >> " + e.getMessage());
			objResponse.put("message", "IO Exception");
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
		} catch (SQLException e) {
			e.printStackTrace();
				e.printStackTrace();
				logger.info("Error checking app ID exist >> " + e.getMessage());
				objResponse.put("message", "Error checking app ID exist "  + e.getMessage());
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			
		} 
	}
	
	

	/**
	 * Get an app data with specified ID
	 * @param appId applicationId
	 * @return HttpResponse with the returnString
	 */
	@GET
	@Path("/data/{appId}")
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
		if(name.equals("anonymous")){
			return unauthorizedMessage();
		}
		if(!initializeDBConnection()){
			logger.info("Cannot connect to database >> ");
			objResponse.put("message", "Cannot connect to database");
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
		}

		try {
			if(!managerAccess.isAppIdExist(appId)){
				logger.info("App not found >> ");
				objResponse.put("message", "App not found");
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			// Add Member to App
			ApplicationModel app = managerAccess.getApplicationWithId(appId);
			ObjectMapper objectMapper = new ObjectMapper();
	    	//Set pretty printing of json
	    	objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
	    	
	    	String appString = objectMapper.writeValueAsString(app);
			
			return new HttpResponse(appString, HttpURLConnection.HTTP_OK);
		} catch (SQLException e) {
			
			e.printStackTrace();

			logger.info("SQLException >> " + e.getMessage());
			objResponse.put("message", "Database Error");
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.info("JsonProcessingException >> " + e.getMessage());
			objResponse.put("message", "Failed to process JSON");
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);

		}
	}
	
	/**
	 * Update an application
	 * @param appId applicationId
	 * @param formData form data
	 * @param contentType content type
	 * @return HttpResponse with the returnString
	 */
	@PUT
	@Path("/data/{appId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Application Updated"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Error occured"),
			@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "App ID not found"),
			@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized")
	})
	@ApiOperation(value = "Update an application",
				 notes = "A method to update an application with detail")
	public HttpResponse updateApplication(
			@ApiParam(value = "Application ID to be updated", required = true) @PathParam("appId") String appId,
			@ApiParam(value = "Application detail in multiple/form-data type", required = true)@HeaderParam(value = HttpHeaders.CONTENT_TYPE) String contentType, 
			@ApiParam(value = "Form of application detail", required = true) @ContentParam byte[] formData)  {
		// parse given multipart form data
		JSONObject objResponse = new JSONObject();

		String appid = null;
		String appdesc = null;
		String commtype = null;
		UserAgent userAgent = (UserAgent) getContext().getMainAgent();
		String name = userAgent.getLoginName();
		if(name.equals("anonymous")){
			return unauthorizedMessage();
		}
		if(!initializeDBConnection()){
			logger.info("Cannot connect to database >> ");
			objResponse.put("message", "Cannot connect to database");
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
		}
		try {
			if(!managerAccess.isAppIdExist(appId)){
				logger.info("App not found >> ");
				objResponse.put("message", "App not found");
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			Map<String, FormDataPart> parts;parts = MultipartHelper.getParts(formData, contentType);
			ApplicationModel app = managerAccess.getApplicationWithId(appId);
			
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
				managerAccess.updateApplication(app);
				logger.info("Application "+ appid +" updated >> ");
				objResponse.put("message", "Application "+ appid +" updated");
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_OK);
			} catch (SQLException e) {
				e.printStackTrace();
				System.out.println(e.getMessage());
				logger.info("SQLException >> " + e.getMessage());
				objResponse.put("message", "Cannot connect to database");
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
			}
		} catch (MalformedStreamException e1) {
			e1.printStackTrace();
			logger.info("MalformedStreamException >> " + e1.getMessage());
			objResponse.put("message", "Wrong Form Data. " + e1.getMessage());
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);

		} catch (IOException e1) {
			e1.printStackTrace();
			logger.info("IOException >> " + e1.getMessage());
			objResponse.put("message", "Error in IO. " + e1.getMessage());
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			logger.info("SQLException >> " + e1.getMessage());
			objResponse.put("message", "Error in Processing Database. " + e1.getMessage());
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);

		}
	}
		

	
	
	/**
	 * Delete an app data with specified ID
	 * @param appId applicationId
	 * @return HttpResponse with the returnString
	 */
	@DELETE
	@Path("/data/{appId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "App Delete Success"),
			@ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "App not found"),
			@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad Request"),
			@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized")})
	@ApiOperation(value = "",
				  notes = "delete an App")
	public HttpResponse deleteApp(@PathParam("appId") String appId)
	{
		JSONObject objResponse = new JSONObject();
		UserAgent userAgent = (UserAgent) getContext().getMainAgent();
		String name = userAgent.getLoginName();
		if(name.equals("anonymous")){
			return unauthorizedMessage();
		}
		if(!initializeDBConnection()){
			logger.info("Cannot connect to database >> ");
			objResponse.put("message", "Cannot connect to database");
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
		}

		try {
			if(!managerAccess.isAppIdExist(appId)){
				logger.info("App not found >> ");
				objResponse.put("message", "App not found");
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			logger.info("Error checking app ID exist >> " + e.getMessage());
			objResponse.put("message", "Error checking app ID exist "  + e.getMessage());
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
		}

		
		
			if(cleanStorage(appId)){
				if(managerAccess.removeApplicationInfo(appId)){
					if(managerAccess.deleteApplicationDB(appId)){
						objResponse.put("message", "Application deleted");
						return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_OK);
					}
				}
				logger.info("Database error >> ");
				objResponse.put("message", "Database error. ");
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
			}
			logger.info("Error delete storage >> ");
			objResponse.put("message", "Error delete storage");
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
	}
	// -------------------------------------------------------
	// TODO Batch processing --------------------------------------
//	/**
//	 * Get a list of apps from database
//	 * 
//	 * @param currentPage current cursor page
//	 * @param windowSize size of fetched data
//	 * @return HttpResponse Returned as JSON object
//	 */
//	@GET
//	@Path("/apps/list/{windowSize}")
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
//	public HttpResponse getAppList(
//			@ApiParam(value = "Page number for retrieving data")@QueryParam("current") int currentPage,
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
//				int offset = (currentPage - 1) * windowSize;
//				apps = managerAccess.getApplicationsWithOffset(offset, windowSize);
//				int totalNum = managerAccess.getNumberOfApplications();
//				
//				for(ApplicationModel a : apps){
//					logger.info(a.getId());
//				}
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
	
	/**
	 * Get all application list
	 * 
	 * @return HttpResponse with the returnString
	 */
	@GET
	@Path("/list/separated")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "REPLACE THIS WITH AN APPROPRIATE FUNCTION NAME",
			notes = "REPLACE THIS WITH YOUR NOTES TO THE FUNCTION")
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "REPLACE THIS WITH YOUR OK MESSAGE"),
			@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized")
	})
	public HttpResponse getSeparateAppInfo() {
		JSONObject objResponse = new JSONObject();
		UserAgent userAgent = (UserAgent) getContext().getMainAgent();
		String name = userAgent.getLoginName();
		if(name.equals("anonymous")){
			return unauthorizedMessage();
		}
		if(!initializeDBConnection()){
			logger.info("Cannot connect to database >> ");
			objResponse.put("message", "Cannot connect to database");
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
		}
		ObjectMapper objectMapper = new ObjectMapper();
    	//Set pretty printing of json
    	objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		
    	try {
			List<List<ApplicationModel>> allApps = managerAccess.getSeparateApplicationsWithMemberId(name);

			
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

	// TODO Other apps functions ----------------------------------
	/**
	 * Remove a member from the application
	 * @param appId applicationId
	 * @param memberId memberId
	 * @return HttpResponse with the returnString
	 */
	@DELETE
	@Path("/data/{appId}/{memberId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "App Delete Success"),
			@ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "App not found"),
			@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad Request"),
	})
	@ApiOperation(value = "",
				  notes = "delete a user from an app")
	public HttpResponse removeMemberFromApp(@PathParam("appId") String appId,@PathParam("memberId") String memberId)
	{
		JSONObject objResponse = new JSONObject();
		UserAgent userAgent = (UserAgent) getContext().getMainAgent();
		String name = userAgent.getLoginName();
		if(name.equals("anonymous")){
			return unauthorizedMessage();
		}
		if(!initializeDBConnection()){
			logger.info("Cannot connect to database >> ");
			objResponse.put("message", "Cannot connect to database");
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
		}
		try {
			if(!managerAccess.isAppIdExist(appId)){
				logger.info("App not found >> ");
				objResponse.put("message", "App not found");
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			if(!managerAccess.isMemberRegistered(memberId)){
				logger.info("No member found >> ");
				objResponse.put("message", "No member found");
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			managerAccess.removeMemberFromApp(memberId, appId);
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
	
	/**
	 * Add a member to the application
	 * @param appId applicationId
	 * @param memberId memberId
	 * @return HttpResponse with the returnString
	 */
	@POST
	@Path("/data/{appId}/{memberId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Member Added"),
			@ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Service not found"),
			@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad Request"),
	})
	@ApiOperation(value = "",
				  notes = "add a member to an app")
	public HttpResponse addMemberToApp(@PathParam("appId") String appId,@PathParam("memberId") String memberId)
	{
		JSONObject objResponse = new JSONObject();
		UserAgent userAgent = (UserAgent) getContext().getMainAgent();
		String name = userAgent.getLoginName();
		if(name.equals("anonymous")){
			return unauthorizedMessage();
		}
		if(!initializeDBConnection()){
			logger.info("Cannot connect to database >> ");
			objResponse.put("message", "Cannot connect to database");
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
		}
		try {
			if(!managerAccess.isAppIdExist(appId)){
				
				logger.info("App not found >> ");
				objResponse.put("message", "App not found");
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			logger.info(appId);
			managerAccess.addMemberToApp(appId, memberId);
			objResponse.put("success", true);
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_OK);

		}
		catch (SQLException e) {
			
			e.printStackTrace();

			logger.info("SQLException >> " + e.getMessage());
			objResponse.put("message", "Database error");
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
		}
	}
	
	/**
	 * 
	 * @return HttpResponse 
	 */
	@POST
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
			if(name.equals("anonymous")){
				return unauthorizedMessage();
			}
				
			if(!initializeDBConnection()){
				logger.info("Cannot connect to database >> ");
				objResponse.put("message", "Cannot connect to database");
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
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
						if(!managerAccess.isMemberRegistered(member.getId())){
							managerAccess.registerMember(member);
							objResponse.put("message", "Welcome " + member.getId() + "!");
							return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_OK);
						}
					} catch (SQLException e) {
						e.printStackTrace();
						logger.info("Cannot connect to database >> ");
						objResponse.put("message", "Cannot connect to database");
						return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
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
				
		
	}
	
	

	
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

//	public boolean isAppWithIdExist(String appId){
//		try {
//			if(managerAccess.isAppIdExist(appId)){
//				return true;
//			}
//			else{
//				return false;
//			}
//		} catch (SQLException e) {
//			e.printStackTrace();
//			logger.info("Exception when checking Application ID exists or not. " + e.getMessage());
//			return false;
//		}
//	}
	
	// //////////////////////////////////////////////////////////////////////////////////////
	// Quest PART --------------------------------------
	// //////////////////////////////////////////////////////////////////////////////////////

	// Functions to be invoked via RMI
	public int isAppWithIdExist(String appId){
		if(!initializeDBConnection()){
			logger.info("Cannot connect to database >> ");
			return 0;
		}
		try {
			if(managerAccess.isAppIdExist(appId)){
				return 1;
			}
			else{
				return 0;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			logger.info("Exception when checking Application ID exists or not. " + e.getMessage());
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

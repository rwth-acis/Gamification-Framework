package i5.las2peer.services.gamificationManagerService;

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
import i5.las2peer.services.gamificationManagerService.achievement.AchievementDAO;
import i5.las2peer.services.gamificationManagerService.achievement.AchievementModel;
import i5.las2peer.services.gamificationManagerService.action.ActionDAO;
import i5.las2peer.services.gamificationManagerService.action.ActionModel;
import i5.las2peer.services.gamificationManagerService.badge.BadgeDAO;
import i5.las2peer.services.gamificationManagerService.badge.BadgeModel;
import i5.las2peer.services.gamificationManagerService.database.ApplicationManagerDAO;
import i5.las2peer.services.gamificationManagerService.database.ApplicationModel;
import i5.las2peer.services.gamificationManagerService.database.SQLDatabase;
import i5.las2peer.services.gamificationManagerService.helper.FormDataPart;
import i5.las2peer.services.gamificationManagerService.helper.LocalFileManager;
import i5.las2peer.services.gamificationManagerService.helper.MultipartHelper;
import i5.las2peer.services.gamificationManagerService.level.LevelDAO;
import i5.las2peer.services.gamificationManagerService.level.LevelModel;
import i5.las2peer.services.gamificationManagerService.quest.QuestDAO;
import i5.las2peer.services.gamificationManagerService.quest.QuestModel;
import i5.las2peer.services.gamificationManagerService.quest.QuestModel.QuestStatus;
import i5.las2peer.services.gamificationManagerService.database.MemberModel;
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
 * Gamification Manager Service
 * 
 * This is Gamification Manager service to manage top level application in Gamification Framework
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
@Path("/games")
@Version("0.1") // this annotation is used by the XML mapper
@Api( value = "/games", authorizations = {
		@Authorization(value = "gamemanager_auth",
		scopes = {
			@AuthorizationScope(scope = "write:games", description = "modify apps in your application"),
			@AuthorizationScope(scope = "read:games", description = "read your apps")
				  })
}, tags = "games")
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

	private SQLDatabase DBManager;
	private ApplicationManagerDAO managerAccess;
	private AchievementDAO achievementAccess;
	private LevelDAO levelAccess;
	private ActionDAO actionAccess;
	private BadgeDAO badgeAccess;
	private QuestDAO questAccess;
	
	// Static variables 
	private String badgeImageURIBase = null;	
	// this header is not known to javax.ws.rs.core.HttpHeaders
	public static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";
	public static final String HEADER_CONTENT_TYPE = "Content-Type";
	// non HTTP standard headers
	public static final String HEADER_OWNERID = "ownerid";
	public static final String HEADER_CONTENT_DESCRIPTION = "Content-Description";

	public GamificationManagerService() {
		// read and set properties values
		// IF THE SERVICE CLASS NAME IS CHANGED, THE PROPERTIES FILE NAME NEED TO BE CHANGED TOO!
		setFieldValues();
	}

	private boolean initializeDBConnection() {

		this.DBManager = new SQLDatabase(this.jdbcDriverClassName, this.jdbcLogin, this.jdbcPass, this.jdbcSchema, this.jdbcHost, this.jdbcPort);
		logger.info(jdbcDriverClassName + " " + jdbcLogin);
		try {
				this.DBManager.connect();
				this.managerAccess = new ApplicationManagerDAO(this.DBManager.getConnection());
				this.badgeAccess = new BadgeDAO(this.DBManager.getConnection());
				this.achievementAccess = new AchievementDAO(this.DBManager.getConnection());
				this.levelAccess = new LevelDAO(this.DBManager.getConnection());
				this.actionAccess = new ActionDAO(this.DBManager.getConnection());
				this.questAccess = new QuestDAO(this.DBManager.getConnection());
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
	
	/**
	 * Function to resize image
	 * @param inputImageRaw input image in byte array
	 * @return return resized image in byte array
	 * @throws IOException IO exception
	 * @throws NUllPointerException null pointer exception
	 */
	private byte[] resizeImage(byte[] inputImageRaw) throws IOException, NullPointerException{
		logger.info("Resize 2: " + inputImageRaw.toString());
		  for (int i = 0; i < inputImageRaw.length; i++) {
		       	System.out.print(inputImageRaw[i]);
	            }
		BufferedImage img = ImageIO.read(new ByteArrayInputStream(inputImageRaw));
		logger.info("Resize 3: " + img);
		BufferedImage newImg = Scalr.resize(img,Mode.AUTOMATIC,300,300);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(newImg, "png", baos);
		baos.flush();
		byte[] output = baos.toByteArray();
		baos.close();
		return output;
		
	}
	
	/**
	 * Function to store badge image in storage
	 * @param appId application id
	 * @param badgeId badge id
	 * @param filename file name
	 * @param filecontent file data
	 * @param mimeType mime type code
	 * @param description description of the badge image
	 * @return HttpResponse with the return image
	 * @throws IOException 
	 */
	private void storeBadgeDataToSystem(String appId, String badgeid, String filename, byte[] filecontent, String mimeType, String description) throws AgentNotKnownException, L2pServiceException, L2pSecurityException, InterruptedException, TimeoutException, IOException{
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
		LocalFileManager.writeByteArrayToFile(LocalFileManager.getBasedir()+"/"+appId+"/"+badgeid, filecontent);

//		Object result = this.invokeServiceMethod("i5.las2peer.services.fileService.FileService@1.0", "storeFile", new Serializable[] {(String) badgeid, (String) filename, (byte[]) filecontent, (String) mimeType, (String) description});
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
	@Path("/apps/data")
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
		} 
	}
	
	

	/**
	 * Get an app data with specified ID
	 * @param appId applicationId
	 * @return HttpResponse with the returnString
	 */
	@GET
	@Path("/apps/data/{appId}")
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
	@Path("/apps/data/{appId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Application Updated"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Error occured"),
			@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "App ID not found"),
			@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized")
	})
	@ApiOperation(value = "Update an application",
				 notes = "A method to update an application with detail")
	public HttpResponse updateAchievement(
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
			if(!isAppWithIdExist(appId)){
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
	@Path("/apps/data/{appId}")
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

		if(!managerAccess.isAppIdExist(appId)){
			logger.info("App not found >> ");
			objResponse.put("message", "App not found");
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
	@Path("/apps/list/separated")
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

	// TODO Other apps functions ----------------------------------
	/**
	 * Remove a member from the application
	 * @param appId applicationId
	 * @param memberId memberId
	 * @return HttpResponse with the returnString
	 */
	@DELETE
	@Path("/apps/data/{appId}/{memberId}")
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
	@Path("/apps/data/{appId}/{memberId}")
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
	@Path("/points/{appId}/name/{unitName}")
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
	@Path("/points/{appId}/name")
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

	
	// //////////////////////////////////////////////////////////////////////////////////////
	// Badge PART --------------------------------------
	// //////////////////////////////////////////////////////////////////////////////////////
	
	// TODO Basic single CRUD ---------------------------------
	
	/**
	 * Post a new badge
	 * @param appId application id
	 * @param formData form data
	 * @param contentType content type
	 * @return HttpResponse Returned as JSON object
	 */
	@POST
	@Path("/badges/{appId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "{\"status\": 3, \"message\": \"Badge upload success ( (badgeid) )\"}"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "{\"status\": 3, \"message\": \"Failed to upload (badgeid)\"}"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "{\"status\": 1, \"message\": \"Failed to add the badge. Badge ID already exist!\"}"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "{\"status\": =, \"message\": \"Badge ID cannot be null!\"}"),
			@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "{\"status\": 2, \"message\": \"File content null. Failed to upload (badgeid)\"}"),
			@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "{\"status\": 2, \"message\": \"Failed to upload (badgeid)\"}"),
			@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "{\"status\": 3, \"message\": \"Badge upload success ( (badgeid) )}")
	})
	@ApiOperation(value = "createNewBadge",
				 notes = "A method to store a new badge with details (badge ID, badge name, badge description, and badge image")
	public HttpResponse createNewBadge(
			@ApiParam(value = "Application ID to store a new badge", required = true) @PathParam("appId") String appId,
			@ApiParam(value = "Content-type in header", required = true)@HeaderParam(value = HttpHeaders.CONTENT_TYPE) String contentType, 
			@ApiParam(value = "Badge detail in multiple/form-data type", required = true)@ContentParam byte[] formData)  {
		// parse given multipart form data
		JSONObject objResponse = new JSONObject();
		String filename = null;
		byte[] filecontent = null;
		String mimeType = null;
		String badgeid = null;
		// Badge ID for the filesystem is appended with app id to make sure it is unique
		String badgename = null;
		String badgedescription = null;
		String badgeImageURI = null;
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
			if(!isAppWithIdExist(appId)){
				logger.info("App not found >> ");
				objResponse.put("message", "App not found");
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			Map<String, FormDataPart> parts = MultipartHelper.getParts(formData, contentType);
			FormDataPart partBadgeID = parts.get("badgeid");
			if (partBadgeID != null) {
				// these data belong to the (optional) file id text input form element
				badgeid = partBadgeID.getContent();
				badgeImageURI = badgeImageURIBase + appId + "/" + badgeid + "/img";
				
				if(badgeAccess.isBadgeIdExist(appId, badgeid)){
					// Badge id already exist
					logger.info("Failed to add the badge. Badge ID already exist!");
					objResponse.put("status", 1);
					objResponse.put("message", "Failed to add the badge. Badge ID already exist!");
					return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_INTERNAL_ERROR);

				}
				FormDataPart partFilecontent = parts.get("badgeimageinput");
				if (partFilecontent != null) {
					// these data belong to the file input form element
						filename = partFilecontent.getHeader(HEADER_CONTENT_DISPOSITION).getParameter("filename");
						byte[] filecontentbefore = partFilecontent.getContentRaw();
//								 validate input
						if (filecontentbefore == null) {
							logger.info("File content null");
							objResponse.put("status", 2);
							objResponse.put("message", "File content null. Failed to upload " + badgeid);
							return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_BAD_REQUEST);
						}
						
						// in unit test, resize image will turn the image into null BufferedImage
						// but, it works in web browser
						FormDataPart partDev = parts.get("dev");
						if (partDev != null) {
							filecontent = filecontentbefore;
						}
						else{
							filecontent = resizeImage(filecontentbefore);
						}
						mimeType = partFilecontent.getContentType();
						logger.info("upload request (" + filename + ") of mime type '" + mimeType + "' with content length "
								+ filecontent.length);
				}

				FormDataPart partBadgeName = parts.get("badgename");
				if (partBadgeName != null) {
					badgename = partBadgeName.getContent();
				}
				FormDataPart partDescription = parts.get("badgedesc");
				if (partDescription != null) {
					// optional description text input form element
					badgedescription = partDescription.getContent();
				}
				BadgeModel badge = new BadgeModel(badgeid, badgename, badgedescription, badgeImageURI);
				
				try {
					
					storeBadgeDataToSystem(appId, badgeid, filename, filecontent,mimeType , badgedescription);
					try{
						badgeAccess.addNewBadge(appId, badge);
						logger.info("Badge upload success (" + badgeid +")");
						objResponse.put("status", 3);
						objResponse.put("message", "Badge upload success (" + badgeid +")");
						return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_CREATED);

					} catch (SQLException e) {
						e.printStackTrace();
						System.out.println(e.getMessage());
						logger.info("SQLException >> " + e.getMessage());
						objResponse.put("status", 2);
						objResponse.put("message", "Failed to upload " + badgeid);
						return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_INTERNAL_ERROR);
					}
					
				} catch (AgentNotKnownException | L2pServiceException | L2pSecurityException | InterruptedException
						| TimeoutException e) {
					e.printStackTrace();
					L2pLogger.logEvent(this, Event.SERVICE_ERROR, e.toString());
					logger.info("Other Exception >> " + e.getMessage());
					objResponse.put("status", 2);
					objResponse.put("message", "Failed to upload " + badgeid);
					return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_INTERNAL_ERROR);

				}
			}
			else{
				logger.info("Badge ID cannot be null");
				objResponse.put("status", 0);
				objResponse.put("message", "Badge ID cannot be null!");
				return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_INTERNAL_ERROR);

			}
			
			
		} catch (MalformedStreamException e) {
			// the stream failed to follow required syntax
			logger.log(Level.SEVERE, e.getMessage(), e);
			System.out.println(e.getMessage());
			logger.info("MalformedStreamException >> " + e.getMessage());
			objResponse.put("status", 2);
			objResponse.put("message", "Failed to upload " + badgeid);
			return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_BAD_REQUEST);

		} catch (IOException e) {
			// a read or write error occurred
			logger.log(Level.SEVERE, e.getMessage(), e);
			System.out.println(e.getMessage());
			logger.info("IOException >> " + e.getMessage());
			objResponse.put("status", 2);
			objResponse.put("message", "Failed to upload " + badgeid);
			return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_INTERNAL_ERROR);

		} catch (SQLException e) {
			e.printStackTrace();
			logger.info("SQLException >> " + e.getMessage());
			objResponse.put("status", 2);
			objResponse.put("message", "Failed to upload " + badgeid);
			return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_INTERNAL_ERROR);

		}
		catch (NullPointerException e){
			e.printStackTrace();
			logger.info("NullPointerException >> " + e.getMessage());
			objResponse.put("status", 2);
			objResponse.put("message", "Failed to upload " + badgeid);
			return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_INTERNAL_ERROR);

		}
	}
	
	
	/**
	 * Update a badge
	 * @param appId application id
	 * @param badgeId badge id
	 * @param formData form data
	 * @param contentType content type
	 * @return HttpResponse Returned as JSON object
	 */
	@PUT
	@Path("/badges/{appId}/{badgeId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Badge Updated"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Error occured"),
			@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad request"),
			@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized")
	})
	@ApiOperation(value = "Update a badge",
				 notes = "A method to update a badge with details (badge ID, badge name, badge description, and badge image")
	public HttpResponse updateBadge(
			@ApiParam(value = "Application ID to store a new badge", required = true) @PathParam("appId") String appId,
				@PathParam("badgeId") String badgeId,
			@ApiParam(value = "Badge detail in multiple/form-data type", required = true)@HeaderParam(value = HttpHeaders.CONTENT_TYPE) String contentType, 
									 @ContentParam byte[] formData)  {
		// parse given multipart form data
		JSONObject objResponse = new JSONObject();
		String filename = null;
		byte[] filecontent = null;
		String mimeType = null;
		// Badge ID for the filesystem is appended with app id to make sure it is unique
		String badgename = null;
		String badgedescription = null;
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
			if(!isAppWithIdExist(appId)){
				logger.info("App not found >> ");
				objResponse.put("message", "App not found");
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			Map<String, FormDataPart> parts = MultipartHelper.getParts(formData, contentType);
			
			if (badgeId == null) {
				logger.info("Badge ID cannot be null >> " );
				objResponse.put("message", "Badge ID cannot be null");
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			BadgeModel currentBadge = badgeAccess.getBadgeWithId(appId, badgeId);
			if(currentBadge == null){
				// currentBadge is null
				logger.info("Badge not found >> " );
				objResponse.put("message", "Badge not found");
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);

			}
			FormDataPart partBadgeName = parts.get("badgename");
			if (partBadgeName != null) {
				badgename = partBadgeName.getContent();
				if(badgename != null){
					currentBadge.setName(badgename);
				}
			}
			FormDataPart partDescription = parts.get("badgedesc");
			if (partDescription != null) {
				// optional description text input form element
				badgedescription = partDescription.getContent();
				logger.info("desc " + badgedescription);
				if(badgedescription!=null){
					currentBadge.setDescription(badgedescription);
				}
			}
			FormDataPart partFilecontent = parts.get("badgeimageinput");
			if (partFilecontent != null) {
				// these data belong to the file input form element
					filename = partFilecontent.getHeader(HEADER_CONTENT_DISPOSITION).getParameter("filename");
					byte[] filecontentbefore = partFilecontent.getContentRaw();
					mimeType = partFilecontent.getContentType();
//						 validate input
					if (filecontentbefore != null) {
						try {
							// in unit test, resize image will turn the image into null BufferedImage
							// but, it works in web browser
							FormDataPart partDev = parts.get("dev");
							if (partDev != null) {
								filecontent = filecontentbefore;
							}
							else{
								filecontent = resizeImage(filecontentbefore);
							}
							storeBadgeDataToSystem(appId, badgeId, filename, filecontent,mimeType , badgedescription);

						} catch (AgentNotKnownException | L2pServiceException | L2pSecurityException | InterruptedException
								| TimeoutException e) {
							e.printStackTrace();
							L2pLogger.logEvent(this, Event.SERVICE_ERROR, e.toString());
							System.out.println(e.getMessage());
							logger.info("OtherException. "+e.getMessage());
							return new HttpResponse("Failed to upload " + badgeId + ".",
									HttpURLConnection.HTTP_INTERNAL_ERROR);
						}
						logger.info("upload request (" + filename + ") of mime type '" + mimeType + "' with content length "
								+ filecontent.length);
					}		
			}
			try{
				badgeAccess.updateBadge(appId, currentBadge);
				logger.info("Badge updated >> ");
				objResponse.put("message", "Badge updated");
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_OK);
			} catch (SQLException e) {
				e.printStackTrace();
				System.out.println(e.getMessage());
				logger.info("SQLException >> " + e.getMessage());
				objResponse.put("message", "Database Error ");
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);

			}
			
		} catch (MalformedStreamException e) {
			// the stream failed to follow required syntax
			logger.log(Level.SEVERE, e.getMessage(), e);
			logger.info("MalformedStreamException >> " );
			objResponse.put("message", "Failed to upload " + badgeId + ". "+e.getMessage());
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
		} catch (IOException e) {
			// a read or write error occurred
			logger.log(Level.SEVERE, e.getMessage(), e);
			logger.info("IOException >> " );
			objResponse.put("message", "Failed to upload " + badgeId + ".");
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
		} catch (SQLException e1) {
			e1.printStackTrace();
			logger.info("SQLException >> " + e1.getMessage());
			objResponse.put("message", "Database Error ");
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);

		}
	}
	
	/**
	 * Get a badge data with specific ID from database
	 * @param appId applicationId
	 * @param badgeId badge id
	 * @return HttpResponse Returned as JSON object
	 */
	@GET
	@Path("/badges/{appId}/{badgeId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Found a badges"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal Error"),
			@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized")})
	@ApiOperation(value = "Find point for specific App ID and badge ID", 
				  notes = "Returns a badge",
				  response = BadgeModel.class,
				  responseContainer = "List",
				  authorizations = @Authorization(value = "api_key")
				  )
	public HttpResponse getBadgeWithId(
			@ApiParam(value = "Application ID")@PathParam("appId") String appId,
			@ApiParam(value = "Badge ID")@PathParam("badgeId") String badgeId)
	{
		BadgeModel badge = null;
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
			if(!isAppWithIdExist(appId)){
				logger.info("App not found >> ");
				objResponse.put("message", "App not found");
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			badge = badgeAccess.getBadgeWithId(appId, badgeId);
			if(badge != null){
				ObjectMapper objectMapper = new ObjectMapper();
		    	//Set pretty printing of json
		    	objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		    	
		    	String badgeString = objectMapper.writeValueAsString(badge);
				return new HttpResponse(badgeString, HttpURLConnection.HTTP_OK);
			}
			else{
				logger.info("Cannot find badge with " + badgeId);
				objResponse.put("message", "Cannot find badge with " + badgeId);
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);

			}
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return new HttpResponse(e.getMessage(), HttpURLConnection.HTTP_INTERNAL_ERROR);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.info("Database Error ");
			objResponse.put("message", "Database Error ");
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);

		}
	}
	
	/**
	 * Delete a badge data with specified ID
	 * @param appId application id
	 * @param badgeId badge id
	 * @return HttpResponse Returned as JSON object
	 */
	@DELETE
	@Path("/badges/{appId}/{badgeId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Badge Delete Success"),
			@ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Badges not found"),
			@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad Request"),
	})
	@ApiOperation(value = "",
				  notes = "delete a badge")
	public HttpResponse deleteBadge(@PathParam("appId") String appId,
								 @PathParam("badgeId") String badgeId)
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
			if(!isAppWithIdExist(appId)){
				logger.info("App not found >> ");
				objResponse.put("message", "App not found");
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			if(!badgeAccess.isBadgeIdExist(appId, badgeId)){
				logger.info("Badge not found >> ");
				objResponse.put("message", "Badge not found");
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			badgeAccess.deleteBadge(appId, badgeId);
			if(!LocalFileManager.deleteFile(LocalFileManager.getBasedir()+"/"+appId+"/"+badgeId)){
				
				logger.info("Delete File Failed >> ");
				objResponse.put("message", "Delete File Failed");
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);

			}
			logger.info("File Deleted >> ");
			objResponse.put("message", "File Deleted");
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_OK);

		} catch (SQLException e) {
			
			e.printStackTrace();
			logger.info("Cannot delete file. >> " +e.getMessage());
			objResponse.put("message", "Cannot delete file.");
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_OK);
		}
		
	}
	
	// TODO Batch processing --------------------
	
	/**
	 * Get a list of badges from database
	 * @param appId application id
	 * @param currentPage current cursor page
	 * @param windowSize size of fetched data
	 * @param searchPhrase search word
	 * @return HttpResponse Returned as JSON object
	 */
	@GET
	@Path("/badges/{appId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(
			value = {
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal Error"),
			@ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Badge not found"),
			@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized")}
	)
	@ApiOperation(
			value = "Find badges for specific App ID", 
			notes = "Returns a list of badges",
			response = BadgeModel.class,
			responseContainer = "List",
			authorizations = @Authorization(value = "api_key")
	)
	public HttpResponse getBadgeList(
			@ApiParam(value = "Application ID that contains badges", required = true)@PathParam("appId") String appId,
			@ApiParam(value = "Page number cursor for retrieving data")@QueryParam("current") int currentPage,
			@ApiParam(value = "Number of data size per fetch")@QueryParam("rowCount") int windowSize,
			@ApiParam(value = "Search phrase parameter")@QueryParam("searchPhrase") String searchPhrase)
	{
		List<BadgeModel> badges = null;
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
			if(!isAppWithIdExist(appId)){
				logger.info("App not found >> ");
				objResponse.put("message", "App not found");
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			// Check app id exist or not
			
			int offset = (currentPage - 1) * windowSize;
			badges = badgeAccess.getBadgesWithOffsetAndSearchPhrase(appId, offset, windowSize, searchPhrase);
			int totalNum = badgeAccess.getNumberOfBadges(appId);
			
			ObjectMapper objectMapper = new ObjectMapper();
	    	//Set pretty printing of json
	    	objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
	    	
	    	String badgeString = objectMapper.writeValueAsString(badges);
			JSONArray badgeArray = (JSONArray) JSONValue.parse(badgeString);

			objResponse.put("current", currentPage);
			objResponse.put("rowCount", windowSize);
			objResponse.put("rows", badgeArray);
			objResponse.put("total", totalNum);
			logger.info(objResponse.toJSONString());
			
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
	
	// TODO Other functions ---------------------
	
	/**
	 * Fetch a badge image with specified ID
	 * @param appId application id
	 * @param badgeId badge id
	 * @return HttpResponse with the return image
	 */
	@GET
	@Path("/badges/{appId}/{badgeId}/img")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Badges Entry"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Cannot found image")
	})
	@ApiOperation(value = "",
				  notes = "list of stored badges")
	public HttpResponse getBadgeImage(@PathParam("appId") String appId,
								 @PathParam("badgeId") String badgeId)
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
			if(!isAppWithIdExist(appId)){
				logger.info("App not found >> ");
				objResponse.put("message", "App not found");
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			if(!badgeAccess.isBadgeIdExist(appId, badgeId)){
				logger.info("Badge not found >> ");
				objResponse.put("message", "Badge not found");
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			byte[] filecontent = LocalFileManager.getFile(appId+"/"+badgeId);
			return new HttpResponse(filecontent, HttpURLConnection.HTTP_OK);
		} catch (SQLException e) {
			e.printStackTrace();
			logger.info("Database error >>  " + e.getMessage());
			objResponse.put("message", "Database error " + e.getMessage());
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);

		}
	}
	
	// //////////////////////////////////////////////////////////////////////////////////////
	// Achievement PART --------------------------------------
	// //////////////////////////////////////////////////////////////////////////////////////

	// TODO  Basic single CRUD --------------------------------------
	
	/**
	 * Post a new achievement
	 * @param appId applicationId
	 * @param formData form data
	 * @param contentType content type
	 * @return HttpResponse with the returnString
	 */
	@POST
	@Path("/achievements/{appId}")
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
			if(!isAppWithIdExist(appId)){
				logger.info("App not found >> ");
				objResponse.put("message", "App not found");
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
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

				AchievementModel achievement = new AchievementModel(achievementid, achievementname, achievementdesc, achievementpointvalue, achievementbadgeid);
				
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
	 * @return HttpResponse Returned as JSON object
	 */
	@GET
	@Path("/achievements/{appId}/{achievementId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Found an achievement"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal Error"),
			@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized")})
	@ApiOperation(value = "Find point for specific App ID and achievement ID", 
				  notes = "Returns a achievement",
				  response = AchievementModel.class,
				  authorizations = @Authorization(value = "api_key")
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
				if(!isAppWithIdExist(appId)){
					logger.info("App not found >> ");
					objResponse.put("message", "App not found");
					return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
				}
				if(!achievementAccess.isAchievementIdExist(appId, achievementId)){
					logger.info("Achievement not found >> ");
					objResponse.put("message", "Achievement not found");
					return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
				}
				achievement = achievementAccess.getAchievementWithId(appId, achievementId);
				if(achievement == null){
					logger.info("Cannot find achievement with " + achievementId);
					objResponse.put("message", "Cannot find achievement with " + achievementId);
					return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
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
	 * @return HttpResponse with the returnString
	 */
	@PUT
	@Path("/achievements/{appId}/{achievementId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Achievement Updated"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Error occured"),
			@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad request"),
			@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized")
	})
	@ApiOperation(value = "Update an achievement",
				 notes = "A method to update an achievement with details (achievement ID, achievement name, achievement description, achievement point value, achievement point id, achievement badge id")
	public HttpResponse updateAchievement(
			@ApiParam(value = "Application ID to store a new achievement", required = true) @PathParam("appId") String appId,
				@PathParam("achievementId") String achievementId,
			@ApiParam(value = "Achievement detail in multiple/form-data type", required = true)@HeaderParam(value = HttpHeaders.CONTENT_TYPE) String contentType, 
									 @ContentParam byte[] formData)  {
		// parse given multipart form data
		JSONObject objResponse = new JSONObject();

		String achievementname = null;
		String achievementdesc = null;
		int achievementpointvalue = 0;
		String achievementbadgeid = null;
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
				if(!isAppWithIdExist(appId)){
					logger.info("App not found >> ");
					objResponse.put("message", "App not found");
					return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
				}
				if(!achievementAccess.isAchievementIdExist(appId, achievementId)){
					logger.info("Achievement not found >> ");
					objResponse.put("message", "Achievement not found");
					return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
				}
			} catch (SQLException e) {
				e.printStackTrace();
				logger.info("DB Error >> " + e.getMessage());
				objResponse.put("message", "DB Error. " + e.getMessage());
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
			
			
			try{
				achievementAccess.updateAchievement(appId, currentAchievement);
				logger.info("Achievement updated >> ");
				objResponse.put("message", "Achievement updated");
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_OK);
			} catch (SQLException e) {
				e.printStackTrace();
				System.out.println(e.getMessage());
				logger.info("SQLException >> " + e.getMessage());
				objResponse.put("message", "Cannot connect to database");
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);

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
	 * Delete a achievement data with specified ID
	 * @param appId applicationId
	 * @param achievementId achievementId
	 * @return HttpResponse with the returnString
	 */
	@DELETE
	@Path("/achievements/{appId}/{achievementId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Achievement Delete Success"),
			@ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Achievements not found"),
			@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad Request"),
	})
	@ApiOperation(value = "",
				  notes = "delete a achievement")
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
			if(!isAppWithIdExist(appId)){
				logger.info("App not found >> ");
				objResponse.put("message", "App not found");
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
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
	 * @return HttpResponse Returned as JSON object
	 */
	@GET
	@Path("/achievements/{appId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Found a list of achievements"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal Error"),
			@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized")})
	@ApiOperation(value = "Find achievements for specific App ID", 
				  notes = "Returns a list of achievements",
				  response = AchievementModel.class,
				  responseContainer = "List",
				  authorizations = @Authorization(value = "api_key")
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
			if(!isAppWithIdExist(appId)){
				logger.info("App not found >> ");
				objResponse.put("message", "App not found");
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			int offset = (currentPage - 1) * windowSize;
			int totalNum = achievementAccess.getNumberOfAchievements(appId);
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
	// TODO Other functions ----------------------------------------
	
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
	@Path("/levels/{appId}")
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
			if(!isAppWithIdExist(appId)){
				logger.info("App not found >> ");
				objResponse.put("message", "App not found");
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
			
				
				LevelModel model = new LevelModel(levelnum, levelname, levelpointvalue);
				
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
	@Path("/levels/{appId}/{levelNum}")
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
				if(!isAppWithIdExist(appId)){
					logger.info("App not found >> ");
					objResponse.put("message", "App not found");
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
	@Path("/levels/{appId}/{levelNum}")
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
				if(!levelAccess.isLevelNumExist(appId, levelNum)){
					logger.info("Level not found >> ");
					objResponse.put("message", "Level not found");
					return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);

				}
			} catch (SQLException e1) {
				e1.printStackTrace();
				logger.info("DB Error >> " + e1.getMessage());
				objResponse.put("message", "DB Error " + e1.getMessage());
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
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
	@Path("/levels/{appId}/{levelNum}")
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
			if(!isAppWithIdExist(appId)){
				logger.info("App not found >> ");
				objResponse.put("message", "App not found");
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
	@Path("/levels/{appId}")
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
			if(!isAppWithIdExist(appId)){
				logger.info("App not found >> ");
				objResponse.put("message", "App not found");
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			int offset = (currentPage - 1) * windowSize;
			int totalNum = levelAccess.getNumberOfLevels(appId);
			model = levelAccess.getLevelsWithOffsetAndSearchPhrase(appId, offset, windowSize, searchPhrase);

			ObjectMapper objectMapper = new ObjectMapper();
	    	//Set pretty printing of json
	    	objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
	    	
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
	// TODO Other Functions

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
	@Path("/actions/{appId}")
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

				
				ActionModel action = new ActionModel(actionid, actionname, actiondesc, actionpointvalue);
				
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
	@Path("/actions/{appId}/{actionId}")
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
				if(!isAppWithIdExist(appId)){
					logger.info("App not found >> ");
					objResponse.put("message", "App not found");
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
	@Path("/actions/{appId}/{actionId}")
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
			
			try{
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
	@Path("/actions/{appId}/{actionId}")
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
	@Path("/actions/{appId}")
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
	
	// //////////////////////////////////////////////////////////////////////////////////////
	// Quest PART --------------------------------------
	// //////////////////////////////////////////////////////////////////////////////////////

	// TODO Basic single CRUD ---------------------------
	/**
	 * Post a new quest
	 * @param appId applicationId
	 * @param contentB content JSON
	 * @return HttpResponse with the returnString
	 */
	@POST
	@Path("/quests/{appId}")
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "{\"status\": 3, \"message\": \"Quests upload success ( (questid) )\"}"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "{\"status\": 3, \"message\": \"Failed to upload (questid)\"}"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "{\"status\": 1, \"message\": \"Failed to add the quest. Quest ID already exist!\"}"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "{\"status\": =, \"message\": \"Quest ID cannot be null!\"}"),
	})
	@ApiOperation(value = "createNewQuest",
				 notes = "A method to store a new quest with details")
	public HttpResponse createNewQuest(
			@ApiParam(value = "Application ID to store a new quest", required = true) @PathParam("appId") String appId,
			@ApiParam(value = "Quest detail in JSON", required = true)@ContentParam byte[] contentB)  {
		// parse given multipart form data
		String textResponse = null;
		String content = new String(contentB);
		if(content.equals(null)){
			logger.info("Cannot parse json data into string >> ");
			textResponse = "Cannot parse json data into string";
			return new HttpResponse(textResponse,HttpURLConnection.HTTP_INTERNAL_ERROR);

		}
		String questid = null;
		String questname;
		String questdescription;
		String queststatus;
		String questachievementid;
		boolean questquestflag = false;
		String questquestidcompleted;
		boolean questpointflag = false;
		int questpointvalue = 0;
		List<Pair<String, Integer>> questactionids = new ArrayList<Pair<String, Integer>>();
		
		UserAgent userAgent = (UserAgent) getContext().getMainAgent();
		String name = userAgent.getLoginName();
//		if(name.equals("anonymous")){
//			return unauthorizedMessage();
//		}
		try {
			if(!initializeDBConnection()){
				logger.info("Cannot connect to database >> ");
				textResponse = "Cannot connect to database";
				return new HttpResponse(textResponse, HttpURLConnection.HTTP_INTERNAL_ERROR);
			}
			JSONObject obj = (JSONObject) JSONValue.parseWithException(content);
			questid = stringfromJSON(obj,"questid");
			if(questAccess.isQuestIdExist(appId, questid)){
				logger.info("Failed to add the quest. Quest ID already exist!");
				textResponse = "Failed to add the quest. Quest ID already exist!";
				return new HttpResponse(textResponse,HttpURLConnection.HTTP_INTERNAL_ERROR);
			}
			questname = stringfromJSON(obj,"questname");
			queststatus = stringfromJSON(obj,"queststatus");
			questachievementid = stringfromJSON(obj,"questachievementid");
			questquestflag = boolfromJSON(obj,"questquestflag");
			questpointflag = boolfromJSON(obj,"questpointflag");
			questpointvalue = intfromJSON(obj,"questpointvalue");
			questactionids = listPairfromJSON(obj,"questactionids","action","times");
			// OK to be null
			questdescription = (String) obj.get("questdescription");
			if(questdescription.equals(null)){
				questdescription = "";
			}
			questquestidcompleted = (String) obj.get("questidcompleted");
			if(questquestflag && questquestidcompleted.equals(null)){
				textResponse = "Completed quest ID cannot be null if it is selected";
				return new HttpResponse(textResponse,HttpURLConnection.HTTP_INTERNAL_ERROR);
			}
			
			QuestModel model = new QuestModel(questid, questname, questdescription, QuestStatus.valueOf(queststatus), questachievementid, questquestflag,questquestidcompleted,questpointflag,questpointvalue);
			
			model.setActionIds(questactionids);
			questAccess.addNewQuest(appId, model);
			logger.info("New quest created ");
			textResponse = "New quest created " + questid;
			return new HttpResponse(textResponse,HttpURLConnection.HTTP_CREATED);

		} catch (MalformedStreamException e) {
			// the stream failed to follow required syntax
			logger.log(Level.SEVERE, e.getMessage(), e);
			System.out.println(e.getMessage());
			logger.info("MalformedStreamException >> " + e.getMessage());
			textResponse = "Failed to upload " + questid;
			return new HttpResponse(textResponse,HttpURLConnection.HTTP_BAD_REQUEST);

		} catch (IOException e) {
			// a read or write error occurred
			logger.log(Level.SEVERE, e.getMessage(), e);
			System.out.println(e.getMessage());
			logger.info("IOException >> " + e.getMessage());
			textResponse = "Failed to upload " + questid;
			return new HttpResponse(textResponse,HttpURLConnection.HTTP_INTERNAL_ERROR);

		} catch (SQLException e) {
			e.printStackTrace();
			logger.info("SQLException >> " + e.getMessage());
			textResponse = "Failed to upload " + questid;
			return new HttpResponse(textResponse,HttpURLConnection.HTTP_INTERNAL_ERROR);

		}
		catch (NullPointerException e){
			e.printStackTrace();
			logger.info("NullPointerException >> " + e.getMessage());
			textResponse = "Failed to upload " + questid;
			return new HttpResponse(textResponse,HttpURLConnection.HTTP_INTERNAL_ERROR);

		} catch (ParseException e) {
			e.printStackTrace();
			logger.info("ParseException >> " + e.getMessage());
			textResponse = "Failed to parse JSON";
			return new HttpResponse(textResponse,HttpURLConnection.HTTP_INTERNAL_ERROR);
		}
	}
	

	/**
	 * Get a quest data with specific ID from database
	 * @param appId applicationId
	 * @param questId quest id
	 * @return HttpResponse Returned as JSON object
	 */
	@GET
	@Path("/quests/{appId}/{questId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Found a quest"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal Error"),
			@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized")})
	@ApiOperation(value = "Find quest for specific App ID and quest ID", 
				  notes = "Returns a quest",
				  response = QuestModel.class,
				  authorizations = @Authorization(value = "api_key")
				  )
	public HttpResponse getQuestWithId(
			@ApiParam(value = "Application ID")@PathParam("appId") String appId,
			@ApiParam(value = "Quest ID")@PathParam("questId") String questId)
	{
		QuestModel quest = null;
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
			if(!isAppWithIdExist(appId)){
				logger.info("App not found >> ");
				objResponse.put("message", "App not found");
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			if(!questAccess.isQuestIdExist(appId, questId)){
				logger.info("Quest not found >> ");
				objResponse.put("message", "Quest not found");
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			quest = questAccess.getQuestWithId(appId, questId);
			if(quest != null){
				ObjectMapper objectMapper = new ObjectMapper();
		    	//Set pretty printing of json
		    	objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		    	
		    	String questString = objectMapper.writeValueAsString(quest);
				return new HttpResponse(questString, HttpURLConnection.HTTP_OK);
			}
			else{
				logger.info("Cannot find quest with " + questId);
				objResponse.put("message", "Cannot find quest with " + questId);
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);

			}
		} catch (SQLException e) {
			e.printStackTrace();
			logger.info("DB Error >> " + e.getMessage());
			objResponse.put("message", "DB Error. " + e.getMessage());
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);

		} catch (IOException e) {
			e.printStackTrace();
			logger.info("Problem in the model >> " + e.getMessage());
			objResponse.put("message", "Problem in the quest model. " + e.getMessage());
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);

		}
		
	}
	

	/**
	 * Update a quest
	 * @param appId applicationId
	 * @param questId questId
	 * @param contentB data
	 * @return HttpResponse with the returnString
	 */
	@PUT
	@Path("/quests/{appId}/{questId}")
	@Produces(MediaType.TEXT_PLAIN)
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Quest Updated"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Error occured"),
			@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad request"),
			@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized")
	})
	@ApiOperation(value = "Update a quest",
				 notes = "A method to update a quest with details")
	public HttpResponse updateQuest(
			@ApiParam(value = "Application ID to store a new quest", required = true) @PathParam("appId") String appId,
			@ApiParam(value = "Quest ID")@PathParam("questId") String questId,
			@ApiParam(value = "Quest detail in JSON", required = true)@ContentParam byte[] contentB) {
		// parse given multipart form data
		String textResponse = null;
		
		String content = new String(contentB);
		if(content.equals(null)){
			logger.info("Cannot parse json data into string >> ");
			textResponse = "Cannot parse json data into string";
			return new HttpResponse(textResponse,HttpURLConnection.HTTP_INTERNAL_ERROR);

		}
		
		String questname;
		String questdescription;
		String queststatus;
		String questachievementid;
		boolean questquestflag = false;
		String questquestidcompleted;
		boolean questpointflag = false;
		int questpointvalue = 0;
		List<Pair<String, Integer>> questactionids = new ArrayList<Pair<String, Integer>>();
		

		UserAgent userAgent = (UserAgent) getContext().getMainAgent();
		String name = userAgent.getLoginName();
		if(name.equals("anonymous")){
			return unauthorizedMessage();
		}
		try {
			if(!initializeDBConnection()){
				logger.info("Cannot connect to database >> ");
				textResponse = "Cannot connect to database";
				return new HttpResponse(textResponse,HttpURLConnection.HTTP_INTERNAL_ERROR);
			}
			
			if (questId == null) {
				logger.info("quest ID cannot be null >> " );
				textResponse = "quest ID cannot be null";
				return new HttpResponse(textResponse,HttpURLConnection.HTTP_BAD_REQUEST);
			}
				
				QuestModel quest = questAccess.getQuestWithId(appId, questId);
				if(!questAccess.isQuestIdExist(appId, questId)){
					logger.info("Failed to update the quest. Quest ID is not exist!");
					textResponse = "Failed to update the quest. Quest ID is not exist!";
					return new HttpResponse(textResponse,HttpURLConnection.HTTP_INTERNAL_ERROR);
				}
				JSONObject obj = (JSONObject) JSONValue.parseWithException(content);
				
				try {
					questname = stringfromJSON(obj,"questname");
					quest.setName(questname);
				} catch (IOException e) {
					e.printStackTrace();
					logger.info("Cannot parse questname");
				}
				try {
					queststatus = stringfromJSON(obj,"queststatus");
					quest.setStatus(QuestStatus.valueOf(queststatus));
				} catch (IOException e) {
					e.printStackTrace();
				}
				try {
					questachievementid = stringfromJSON(obj,"questachievementid");
					quest.setAchievementId(questachievementid);
				} catch (IOException e) {
					e.printStackTrace();
				}
				questdescription = (String) obj.get("questdescription");
				if(!questdescription.equals(null)){
					quest.setDescription(questdescription);
				}
				questquestidcompleted = (String) obj.get("questidcompleted");
				if(questquestflag && questquestidcompleted.equals(null)){
					textResponse = "Completed quest ID cannot be null if it is selected";
					return new HttpResponse(textResponse,HttpURLConnection.HTTP_INTERNAL_ERROR);
				}
				questquestflag = boolfromJSON(obj,"questquestflag");
				questpointflag = boolfromJSON(obj,"questpointflag");
				questpointvalue = intfromJSON(obj,"questpointvalue");
				
				quest.setQuestFlag(questquestflag);
				quest.setPointFlag(questpointflag);
				quest.setQuestIdCompleted(questquestidcompleted);
				quest.setPointValue(questpointvalue);
				try {
					questactionids = listPairfromJSON(obj,"questactionids","action","times");
					quest.setActionIds(questactionids);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				questAccess.updateQuest(appId, quest);
				logger.info("Quest Updated ");
				textResponse = "Quest updated " + questId;
				return new HttpResponse(textResponse,HttpURLConnection.HTTP_OK);
			
		} catch (SQLException e) {
			e.printStackTrace();
			logger.info("DB Error >> " + e.getMessage());
			textResponse = "DB Error. " + e.getMessage();
			return new HttpResponse(textResponse,HttpURLConnection.HTTP_INTERNAL_ERROR);

		} catch (ParseException e) {
			e.printStackTrace();			
			logger.info("ParseException >> " + e.getMessage());
			textResponse = "ParseExceptionr. " + e.getMessage();
			return new HttpResponse(textResponse,HttpURLConnection.HTTP_INTERNAL_ERROR);
		} catch (IOException e) {
			e.printStackTrace();
			logger.info("IOException Problem with the model >> " + e.getMessage());
			textResponse = "Problem with the model. " + e.getMessage();
			return new HttpResponse(textResponse,HttpURLConnection.HTTP_INTERNAL_ERROR);

		} 

	}
	

	/**
	 * Delete a quest data with specified ID
	 * @param appId applicationId
	 * @param questId questId
	 * @return HttpResponse with the returnString
	 */
	@DELETE
	@Path("/quests/{appId}/{questId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "quest Delete Success"),
			@ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "quest not found"),
			@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad Request"),
	})
	@ApiOperation(value = "",
				  notes = "delete a quest")
	public HttpResponse deleteQuest(@PathParam("appId") String appId,
								 @PathParam("questId") String questId)
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
			if(!questAccess.isQuestIdExist(appId, questId)){
				logger.info("Failed to delete the quest. Quest ID is not exist!");
				objResponse.put("status", 1);
				objResponse.put("message", "Failed to delete the quest. Quest ID is not exist!");
				return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_BAD_REQUEST);
			}
			questAccess.deleteQuest(appId, questId);
			
			logger.info(" Deleted >> ");
			objResponse.put("message", "quest Deleted");
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_OK);

		} catch (SQLException e) {
			
			e.printStackTrace();
			logger.info("Cannot delete quest. >> " +e.getMessage());
			objResponse.put("message", "Cannot delete quest.");
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
		}
	}
	
	// TODO Batch Processing ----------------------------
	/**
	 * Get a list of quests from database
	 * @param appId applicationId
	 * @param currentPage current cursor page
	 * @param windowSize size of fetched data
	 * @param searchPhrase search word
	 * @return HttpResponse Returned as JSON object
	 */
	@GET
	@Path("/quests/{appId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Found a list of quests"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal Error"),
			@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized")})
	@ApiOperation(value = "Find points for specific App ID", 
				  notes = "Returns a list of quests",
				  response = QuestModel.class,
				  responseContainer = "List",
				  authorizations = @Authorization(value = "api_key")
				  )
	public HttpResponse getQuestList(
			@ApiParam(value = "Application ID to return")@PathParam("appId") String appId,
			@ApiParam(value = "Page number for retrieving data")@QueryParam("current") int currentPage,
			@ApiParam(value = "Number of data size")@QueryParam("rowCount") int windowSize,
			@ApiParam(value = "Search phrase parameter")@QueryParam("searchPhrase") String searchPhrase)
	{
		List<QuestModel> qs = null;
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
			int offset = (currentPage - 1) * windowSize;
			int totalNum = actionAccess.getNumberOfActions(appId);

			qs = questAccess.getQuestsWithOffsetAndSearchPhrase(appId, offset, windowSize, searchPhrase);

			ObjectMapper objectMapper = new ObjectMapper();
	    	//Set pretty printing of json
	    	objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
	    	
	    	String questString = objectMapper.writeValueAsString(qs);
			JSONArray questArray = (JSONArray) JSONValue.parse(questString);
			//logger.info(questArray.toJSONString());
			
			
			for(int i = 0; i < qs.size(); i++){
				JSONArray actionArray = listPairtoJSONArray(qs.get(i).getActionIds());
				
				JSONObject questObject = (JSONObject) questArray.get(i);
				
				questObject.replace("actionIds", actionArray);
				
				questArray.set(i,questObject);
			}
			
		
			objResponse.put("current", currentPage);
			objResponse.put("rowCount", windowSize);
			objResponse.put("rows", questArray);
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

		} catch (IOException e) {
			e.printStackTrace();
			logger.info("Failed to parse action objects >> ");
			objResponse.put("message", "Failed to parse JSON");
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);

		}
	}
	// TODO Other Functions -----------------------------
	// Functions to be invoked via RMI
	public boolean isAppWithIdExist(String appId) throws SQLException{
		if(managerAccess.isAppIdExist(appId)){
			return true;
		}
		else{
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

package i5.las2peer.services.gamificationQuestService;

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

import org.apache.commons.fileupload.MultipartStream.MalformedStreamException;
import org.apache.commons.lang3.tuple.Pair;

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
import i5.las2peer.services.gamificationQuestService.database.ActionDAO;
import i5.las2peer.services.gamificationQuestService.database.ActionModel;
import i5.las2peer.services.gamificationQuestService.database.QuestDAO;
import i5.las2peer.services.gamificationQuestService.database.QuestModel;
import i5.las2peer.services.gamificationQuestService.database.SQLDatabase;
import i5.las2peer.services.gamificationQuestService.database.QuestModel.QuestStatus;
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
 * Gamification Quest Service
 * 
 * This is Gamification Quest Service to manage quest element in Gamification Framework
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
@Path("/gamification/quests")
@Version("0.1") // this annotation is used by the XML mapper
@Api( value = "/quests", authorizations = {
		@Authorization(value = "quests_auth",
		scopes = {
			@AuthorizationScope(scope = "write:quests", description = "modify quests in your application"),
			@AuthorizationScope(scope = "read:quests", description = "read your quests")
				  })
}, tags = "quests")
@SwaggerDefinition(
		info = @Info(
				title = "Gamification Quest Service",
				version = "0.1",
				description = "Gamification Quest Service for Gamification Framework",
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
public class GamificationQuestService extends Service {

	// instantiate the logger class
	private final L2pLogger logger = L2pLogger.getInstance(GamificationQuestService.class.getName());
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
	private QuestDAO questAccess;
	

	public GamificationQuestService() {
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
				this.questAccess = new QuestDAO(this.DBManager.getConnection());
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
	@Path("/{appId}")
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
		String questquestidcompleted = null;
		boolean questpointflag = false;
		int questpointvalue = 0;
		List<Pair<String, Integer>> questactionids = new ArrayList<Pair<String, Integer>>();
		
		boolean questnotifcheck = false;
		String questnotifmessage = "";
		
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
			try {
				if(!isAppWithIdExist(appId)){
					logger.info("App not found >> ");
					textResponse = "App not found";
					return new HttpResponse(textResponse, HttpURLConnection.HTTP_BAD_REQUEST);
				}
			} catch (AgentNotKnownException | L2pServiceException | L2pSecurityException | InterruptedException
					| TimeoutException e1) {
				e1.printStackTrace();
				logger.info("Cannot check whether application ID exist or not. >> " + e1.getMessage());
				textResponse = "Cannot check whether application ID exist or not. >> " + e1.getMessage();
				return new HttpResponse(textResponse, HttpURLConnection.HTTP_BAD_REQUEST);
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
			if(questquestflag){
				if(questquestidcompleted.equals(null)){
					textResponse = "Completed quest ID cannot be null if it is selected";
					return new HttpResponse(textResponse,HttpURLConnection.HTTP_INTERNAL_ERROR);
				}
			}else{
				questquestidcompleted = null;
			}
			questnotifcheck = boolfromJSON(obj, "questnotificationcheck");
			if(questnotifcheck){
				questnotifmessage = stringfromJSON(obj, "questnotificationmessage");
			}
			System.out.println(questquestidcompleted);
			QuestModel model = new QuestModel(questid, questname, questdescription, QuestStatus.valueOf(queststatus), questachievementid, questquestflag,questquestidcompleted,questpointflag,questpointvalue, questnotifcheck, questnotifmessage);
			
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
	@Path("/{appId}/{questId}")
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
			if(!questAccess.isQuestIdExist(appId, questId)){
				logger.info("Quest not found >> ");
				objResponse.put("message", "Quest not found");
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			
	    	String questString = getQuestWithIdMethod( appId, questId);
			return new HttpResponse(questString, HttpURLConnection.HTTP_OK);
			
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
	
	public String getQuestWithIdMethod(String appId, String questId) throws SQLException, IOException {
		QuestModel quest = questAccess.getQuestWithId(appId, questId);
		if(quest == null){
			throw new SQLException("Quest Null, Cannot find quest with " + questId);
		}
		ObjectMapper objectMapper = new ObjectMapper();
    	//Set pretty printing of json
    	objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    	
    	String questString = objectMapper.writeValueAsString(quest);
    	return questString;
	}
	/**
	 * Update a quest
	 * @param appId applicationId
	 * @param questId questId
	 * @param contentB data
	 * @return HttpResponse with the returnString
	 */
	@PUT
	@Path("/{appId}/{questId}")
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
		String questquestidcompleted = null;
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
			try {
				if(!isAppWithIdExist(appId)){
					logger.info("App not found >> ");
					textResponse = "App not found";
					return new HttpResponse(textResponse, HttpURLConnection.HTTP_BAD_REQUEST);
				}
			} catch (AgentNotKnownException | L2pServiceException | L2pSecurityException | InterruptedException
					| TimeoutException e1) {
				e1.printStackTrace();
				logger.info("Cannot check whether application ID exist or not. >> " + e1.getMessage());
				textResponse = "Cannot check whether application ID exist or not. >> " + e1.getMessage();
				return new HttpResponse(textResponse, HttpURLConnection.HTTP_BAD_REQUEST);
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
				if(questquestflag){
					if(questquestidcompleted.equals(null) || questquestidcompleted.equals("")){
						textResponse = "Completed quest ID cannot be null if it is selected";
						return new HttpResponse(textResponse,HttpURLConnection.HTTP_INTERNAL_ERROR);
					}	
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
	@Path("/{appId}/{questId}")
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
	@Path("/{appId}")
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

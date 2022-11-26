package i5.las2peer.services.gamificationQuestService;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.MediaType;

import i5.las2peer.services.gamification.commons.database.DatabaseManager;
import org.apache.commons.fileupload.MultipartStream.MalformedStreamException;
import org.apache.commons.lang3.tuple.Pair;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import i5.las2peer.logging.L2pLogger;
import i5.las2peer.restMapper.RESTService;
import i5.las2peer.restMapper.annotations.ServicePath;
import i5.las2peer.api.Context;
import i5.las2peer.api.logging.MonitoringEvent;
import i5.las2peer.api.ManualDeployment;
import i5.las2peer.api.security.Agent;
import i5.las2peer.api.security.AnonymousAgent;
import i5.las2peer.api.security.UserAgent;
import i5.las2peer.services.gamificationQuestService.database.QuestDAO;
import i5.las2peer.services.gamificationQuestService.database.QuestModel;
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
@Api( value = "/quests", authorizations = {
		@Authorization(value = "quests_auth",
		scopes = {
			@AuthorizationScope(scope = "write:quests", description = "modify quests in your game"),
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
@ManualDeployment
@ServicePath("/gamification/quests")
public class GamificationQuestService extends RESTService {

	// instantiate the logger class
	private final L2pLogger logger = L2pLogger.getInstance(GamificationQuestService.class.getName());
	/*
	 * Database configuration
	 */
	private String jdbcDriverClassName;
	private String jdbcLogin;
	private String jdbcPass;
	private String jdbcUrl;
	private String jdbcSchema;
	private DatabaseManager dbm;
	
	private QuestDAO questAccess;
	

	public GamificationQuestService() {
		// read and set properties values
		// IF THE SERVICE CLASS NAME IS CHANGED, THE PROPERTIES FILE NAME NEED TO BE CHANGED TOO!
		setFieldValues();
		dbm = DatabaseManager.getInstance(jdbcDriverClassName, jdbcLogin, jdbcPass, jdbcUrl, jdbcSchema);
		this.questAccess = new QuestDAO();

	}
		  /**
			 * Function to return http unauthorized message
			 * @return HTTP Response unauthorized
			 */
			private Response unauthorizedMessage(){
				JSONObject objResponse = new JSONObject();
				objResponse.put("message", "You are not authorized");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_UNAUTHORIZED).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
			}
			
			/**
			 * Get an element of JSON object with specified key as string
			 * @return string value
			 * @throws IOException IO exception
			 */
			private static String stringfromJSON(JSONObject obj, String key) throws IOException {
				String s = (String) obj.get(key);
				if (s == null) {
					throw new IOException("Key " + key + " is missing in JSON");
				}
				return s;
			}

			/**
			 * Get an element of JSON object with specified key as integer
			 * @return integer value
			 * @throws IOException IO exception
			 */
			private static int intfromJSON(JSONObject obj, String key) {
				return (int) obj.get(key);
			}

			/**
			 * Get an element of JSON object with specified key as boolean
			 * @return boolean value
			 * @throws IOException IO exception
			 */
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
			
			/**
			 * Get an element of JSON object with specified key as list of pair string and integer
			 * @return list of pair string and integer value
			 * @throws IOException IO exception
			 */
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
			
			/**
			 * Convert list of pair string and integer to JSON array
			 * @return JSON array list of pair string and integer
			 * @throws IOException IO exception
			 */
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
			// Quest PART --------------------------------------
			// //////////////////////////////////////////////////////////////////////////////////////

			// TODO Basic single CRUD ---------------------------
			/**
			 * Post a new quest. It consumes JSON data.
			 * Name attribute for JSON data : 
			 * <ul>
			 * 	<li>questid - Quest ID - String (20 chars)
			 *  <li>questname - Quest name - String (20 chars)
			 *  <li>queststatus - Quest Status - COMPLETED, HIDDEN, or REVEALED
			 *  <li>questachievementid - Achievement name obtained Gamification Achievement Service - String (20 chars)
			 *  <li>questquestflag - Quest flag dependency boolean - Boolean
			 *  <li>questpointflag - Point flag dependency boolean - String (20 chars)
			 *  <li>questidcompleted - Completed Quest ID if the quest flag dependency is true - String (20 chars)
			 *  <li>questpointvalue - Point value if the point flag dependency boolean is true - Integer
			 *  <li>questactionids - Array of object {action: , times: }(times must have a positive value)
			 *  <li>questdescription - Quest Description - String (50 chars)
			 *  <li>questnotificationcheck - Quest Notification Boolean - Boolean - Option whether use notification or not
			 *  <li>questnotificationmessage - Quest Notification Message - String
			 * </ul>
			 * @param gameId gameId
			 * @param contentB content JSON
			 * @return HTTP Response returned as JSON object
			 */
			@POST
			@Path("/{gameId}")
			@Produces(MediaType.APPLICATION_JSON)
			@Consumes(MediaType.APPLICATION_JSON)
			@ApiResponses(value = {
					@ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "{\"status\": 3, \"message\": \"Quests upload success ( (questid) )\"}"),
					@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "{\"status\": 3, \"message\": \"Failed to upload (questid)\"}"),
					@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "{\"status\": 1, \"message\": \"Failed to add the quest. Quest ID already exist!\"}"),
					@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "{\"status\": =, \"message\": \"Quest ID cannot be null!\"}"),
			})
			@ApiOperation(value = "createNewQuest",
						 notes = "A method to store a new quest with details")
			public Response createNewQuest(
					@ApiParam(value = "Game ID to store a new quest", required = true) @PathParam("gameId") String gameId,
					@ApiParam(value = "Quest detail in JSON", required = true) byte[] contentB)  {

				// Request log
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_99, "POST " + "gamification/quests/"+gameId, true);
				long randomLong = new Random().nextLong(); //To be able to match
				
				
				// parse given multipart form data
				JSONObject objResponse = new JSONObject();
				
				String name = null;
				Agent agent = Context.getCurrent().getMainAgent();
				if (agent instanceof AnonymousAgent) {
					return unauthorizedMessage();
				}
				else if (agent instanceof UserAgent) {
					UserAgent userAgent = (UserAgent) Context.getCurrent().getMainAgent();
					name = userAgent.getLoginName();
				}
				else {
					name = agent.getIdentifier();
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
				Connection conn = null;

				

				try {
					conn = dbm.getConnection();
					
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_14, ""+randomLong, true);
					
					
					String content = new String(contentB);
					if(content.equals(null)){
						objResponse.put("message", "Cannot create quest. Cannot parse json data into string");
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();

					}
					
					JSONObject obj = (JSONObject) JSONValue.parseWithException(content);
					try {
						if(!questAccess.isGameIdExist(conn,gameId)){
							objResponse.put("message", "Cannot create quest. Game not found");
							Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
							return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
						}
					} catch (SQLException e1) {
						e1.printStackTrace();
						objResponse.put("message", "Cannot create quest. Cannot check whether game ID exist or not. Database error. " + e1.getMessage());
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					}
					questid = stringfromJSON(obj,"questid");
					if(questAccess.isQuestIdExist(conn,gameId, questid)){
						objResponse.put("message", "Cannot create quest. Failed to add the quest. Quest ID already exist! ");
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
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
							objResponse.put("message", "Cannot create quest. Completed quest ID cannot be null if it is selected");
							Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
							return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
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
					questAccess.addNewQuest(conn,gameId, model);
					objResponse.put("message", "New quest created " + questid);
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_15, ""+randomLong, true);
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_24, ""+name, true);
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_25, ""+gameId, true);
					return Response.status(HttpURLConnection.HTTP_CREATED).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();

				} catch (MalformedStreamException e) {
					// the stream failed to follow required syntax
					objResponse.put("message", "Cannot create quest. MalformedStreamException. Failed to upload " + questid + ". " + e.getMessage());
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();

				} catch (IOException e) {
					// a read or write error occurred
					objResponse.put("message", "Cannot create quest. IO Exception. Failed to upload " + questid + ". " + e.getMessage());
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();

				} catch (SQLException e) {
					e.printStackTrace();
					objResponse.put("message", "Cannot create quest. Database error. " + e.getMessage());
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();

				}
				catch (NullPointerException e){
					e.printStackTrace();
					objResponse.put("message", "Cannot create quest. NullPointerException. Failed to upload " + questid + ". " + e.getMessage());
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();

				} catch (ParseException e) {
					e.printStackTrace();
					objResponse.put("message", "Cannot create quest. ParseException. Failed to parse JSON. " + e.getMessage());
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
				}		 
				// always close connections
			    finally {
			      try {
					  if (conn != null) {
						  conn.close();
					  }
				  } catch (SQLException e) {
			        logger.printStackTrace(e);
			      }
			    }
			}
			

			/**
			 * Get a quest data with specific ID from database
			 * @param gameId gameId
			 * @param questId quest id
			 * @return HTTP Response returned as JSON object
			 */
			@GET
			@Path("/{gameId}/{questId}")
			@Produces(MediaType.APPLICATION_JSON)
			@ApiResponses(value = {
					@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Found a quest"),
					@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal Error"),
					@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized")})
			@ApiOperation(value = "getQuestWithId", 
						  notes = "Returns quest detail with specific ID",
						  response = QuestModel.class
						  )
			public Response getQuestWithId(
					@ApiParam(value = "Game ID")@PathParam("gameId") String gameId,
					@ApiParam(value = "Quest ID")@PathParam("questId") String questId)
			{
				
				// Request log
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_99, "GET " + "gamification/quests/"+gameId+"/"+questId, true);
				long randomLong = new Random().nextLong(); //To be able to match
				
				QuestModel quest = null;
				Connection conn = null;

				JSONObject objResponse = new JSONObject();
				String name = null;
				Agent agent = Context.getCurrent().getMainAgent();
				if (agent instanceof AnonymousAgent) {
					return unauthorizedMessage();
				}
				else if (agent instanceof UserAgent) {
					UserAgent userAgent = (UserAgent) Context.getCurrent().getMainAgent();
					name = userAgent.getLoginName();
				}
				else {
					name = agent.getIdentifier();
				}
				try {
					conn = dbm.getConnection();
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_16, ""+randomLong, true);
					
					try {
						if(!questAccess.isGameIdExist(conn,gameId)){
							objResponse.put("message", "Cannot get quest. Game not found");
							Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
							return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
						}
					} catch (SQLException e1) {
						e1.printStackTrace();
						objResponse.put("message", "Cannot get quest. Cannot check whether game ID exist or not. Database error. " + e1.getMessage());
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					}
					if(!questAccess.isQuestIdExist(conn,gameId, questId)){
						objResponse.put("message", "Cannot get quest. Quest not found");
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					}
					quest = questAccess.getQuestWithId(conn,gameId, questId);

					if(quest == null){
						objResponse.put("message", "Cannot get quest. Quest Null, Cannot find quest with " + questId);
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					}
					ObjectMapper objectMapper = new ObjectMapper();
			    	//Set pretty printing of json
			    	objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
			    	
			    	String questString = objectMapper.writeValueAsString(quest);
			    	Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_17, ""+randomLong, true);
			    	Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_26, ""+name, true);
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_27, ""+gameId, true);
					return Response.status(HttpURLConnection.HTTP_OK).entity(questString).type(MediaType.APPLICATION_JSON).build();
					
				} catch (SQLException e) {
					e.printStackTrace();
					objResponse.put("message", "Cannot get quest. DB Error. " + e.getMessage());
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();

				} catch (IOException e) {
					e.printStackTrace();
					objResponse.put("message", "Cannot get quest. Problem in the quest model. " + e.getMessage());
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();

				}		 
				// always close connections
			    finally {
			      try {
					  if (conn != null) {
						  conn.close();
					  }
				  } catch (SQLException e) {
			        logger.printStackTrace(e);
			      }
			    }
				
			}
			

			/**
			 * Update a quest.
			 * Name attribute for JSON data : 
			 * <ul>
			 * 	<li>questid - Quest ID - String (20 chars)
			 *  <li>questname - Quest name - String (20 chars)
			 *  <li>queststatus - Quest Status - COMPLETED, HIDDEN, or REVEALED
			 *  <li>questachievementid - Achievement name obtained Gamification Achievement Service - String (20 chars)
			 *  <li>questquestflag - Quest flag dependency boolean - Boolean
			 *  <li>questpointflag - Point flag dependency boolean - String (20 chars)
			 *  <li>questidcompleted - Completed Quest ID if the quest flag dependency is true - String (20 chars)
			 *  <li>questpointvalue - Point value if the point flag dependency boolean is true - Integer
			 *  <li>questactionids - Array of object {action: , time: }
			 *  <li>questdescription - Quest Description - String (50 chars)
			 *  <li>questnotificationcheck - Quest Notification Boolean - Boolean - Option whether use notification or not
			 *  <li>questnotificationmessage - Quest Notification Message - String
			 * </ul>
			 * @param gameId gameId
			 * @param questId questId
			 * @param contentB JSON data
			 * @return HTTP Response returned as JSON object
			 */
			@PUT
			@Path("/{gameId}/{questId}")
			@Produces(MediaType.APPLICATION_JSON)
			@Consumes(MediaType.APPLICATION_JSON)
			@ApiResponses(value = {
					@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Quest Updated"),
					@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Error occured"),
					@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad request"),
					@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized")
			})
			@ApiOperation(value = "updateQuest",
						 notes = "A method to update a quest with details")
			public Response updateQuest(
					@ApiParam(value = "Game ID to store a new quest", required = true) @PathParam("gameId") String gameId,
					@ApiParam(value = "Quest ID")@PathParam("questId") String questId,
					@ApiParam(value = "Quest detail in JSON", required = true) byte[] contentB) {

				// Request log
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_99, "PUT " + "gamification/quests/"+gameId+"/"+questId, true);
				long randomLong = new Random().nextLong(); //To be able to match
				
				// parse given multipart form data
				JSONObject objResponse = new JSONObject();
				Connection conn = null;

				
				String content = new String(contentB);
				if(content.equals(null)){
					objResponse.put("message", "Cannot update quest. Cannot parse json data into string");
					
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();

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
				
				boolean questnotifcheck = false;
				String questnotifmessage = "";
				
				String name = null;
				Agent agent = Context.getCurrent().getMainAgent();
				if (agent instanceof AnonymousAgent) {
					return unauthorizedMessage();
				}
				else if (agent instanceof UserAgent) {
					UserAgent userAgent = (UserAgent) Context.getCurrent().getMainAgent();
					name = userAgent.getLoginName();
				}
				else {
					name = agent.getIdentifier();
				}
				try {
					conn = dbm.getConnection();
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_18, ""+randomLong, true);
					
					try {
						if(!questAccess.isGameIdExist(conn,gameId)){
							logger.info("Game not found >> ");
							objResponse.put("message", "Cannot update quest. Game not found");
							Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
							return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
						}
					} catch (SQLException e1) {
						e1.printStackTrace();
						logger.info("Cannot check whether game ID exist or not. Database error. >> " + e1.getMessage());
						objResponse.put("message", "Cannot check whether game ID exist or not. Database error. " + e1.getMessage());
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					}
					if (questId == null) {
						logger.info("quest ID cannot be null >> " );
						objResponse.put("message", "Cannot update quest. quest ID cannot be null");
						
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					}
						
						QuestModel quest = questAccess.getQuestWithId(conn,gameId, questId);
						if(!questAccess.isQuestIdExist(conn,gameId, questId)){
							objResponse.put("message", "Cannot update quest. Failed to update the quest. Quest ID is not exist!");
							Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
							return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
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
								objResponse.put("message", "Cannot update quest. Completed quest ID cannot be null if it is selected");
								Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
								return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
							}	
						}
						questquestflag = boolfromJSON(obj,"questquestflag");
						questpointflag = boolfromJSON(obj,"questpointflag");
						questpointvalue = intfromJSON(obj,"questpointvalue");
						questnotifcheck = boolfromJSON(obj, "questnotificationcheck");
						questnotifmessage = stringfromJSON(obj, "questnotificationmessage");
						
						quest.setQuestFlag(questquestflag);
						quest.setPointFlag(questpointflag);
						quest.setQuestIdCompleted(questquestidcompleted);
						quest.setPointValue(questpointvalue);
						quest.useNotification(questnotifcheck);
						quest.setNotificationMessage(questnotifmessage);
						try {
							questactionids = listPairfromJSON(obj,"questactionids","action","times");
							quest.setActionIds(questactionids);
						} catch (IOException e) {
							
							e.printStackTrace();
						}
						questAccess.updateQuest(conn,gameId, quest);
						logger.info("Quest Updated ");
						objResponse.put("message", "Quest updated " + questId);
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_19, ""+randomLong, true);
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_28, ""+name, true);
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_29, ""+gameId, true);
						return Response.status(HttpURLConnection.HTTP_OK).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					
				} catch (SQLException e) {
					e.printStackTrace();
					objResponse.put("message", "Cannot update quest. DB Error. " + e.getMessage());
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();

				} catch (ParseException e) {
					e.printStackTrace();			
					objResponse.put("message", "Cannot update quest. ParseExceptionr. " + e.getMessage());
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
				} catch (IOException e) {
					e.printStackTrace();
					objResponse.put("message", "Cannot update quest. Problem with the model. " + e.getMessage());
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
				} 		 
				// always close connections
			    finally {
			      try {
					  if (conn != null) {
						  conn.close();
					  }
				  } catch (SQLException e) {
			        logger.printStackTrace(e);
			      }
			    }
			}
			

			/**
			 * Delete a quest data with specified ID
			 * @param gameId gameId
			 * @param questId questId
			 * @return HTTP Response returned as JSON object
			 */
			@DELETE
			@Path("/{gameId}/{questId}")
			@Produces(MediaType.APPLICATION_JSON)
			@ApiResponses(value = {
					@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "quest Delete Success"),
					@ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "quest not found"),
					@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad Request"),
			})
			@ApiOperation(value = "deleteQuest",
						  notes = "delete a quest")
			public Response deleteQuest(@PathParam("gameId") String gameId,
										 @PathParam("questId") String questId)
			{
				
				// Request log
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_99, "DELETE" + "gamification/quests/"+gameId+"/"+questId, true);
				long randomLong = new Random().nextLong(); //To be able to match
				
				JSONObject objResponse = new JSONObject();
				Connection conn = null;

				String name = null;
				Agent agent = Context.getCurrent().getMainAgent();
				if (agent instanceof AnonymousAgent) {
					return unauthorizedMessage();
				}
				else if (agent instanceof UserAgent) {
					UserAgent userAgent = (UserAgent) Context.getCurrent().getMainAgent();
					name = userAgent.getLoginName();
				}
				else {
					name = agent.getIdentifier();
				}
				try {
					conn = dbm.getConnection();
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_20, ""+randomLong, true);
					
					try {
						if(!questAccess.isGameIdExist(conn,gameId)){
							objResponse.put("message", "Cannot delete quest. Game not found");
							Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
							return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
						}
					} catch (SQLException e1) {
						e1.printStackTrace();
						objResponse.put("message", "Cannot delete quest. Cannot check whether game ID exist or not. Database error. " + e1.getMessage());
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					}
					if(!questAccess.isQuestIdExist(conn,gameId, questId)){
						objResponse.put("message", "Cannot delete quest. Failed to delete the quest. Quest ID is not exist!");
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					}
					questAccess.deleteQuest(conn,gameId, questId);
					
					objResponse.put("message", "quest Deleted");
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_21, ""+randomLong, true);
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_30, ""+name, true);
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_31, ""+gameId, true);
					return Response.status(HttpURLConnection.HTTP_OK).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();

				} catch (SQLException e) {
					
					e.printStackTrace();
					objResponse.put("message", "Cannot delete quest. Database error. " + e.getMessage());
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
				}		 
				// always close connections
			    finally {
			      try {
					  if (conn != null) {
						  conn.close();
					  }
				  } catch (SQLException e) {
			        logger.printStackTrace(e);
			      }
			    }
			}
			
			/**
			 * Get a list of quests from database
			 * @param gameId Game ID obtained from Gamification Game Service
			 * @param currentPage current cursor page
			 * @param windowSize size of fetched data (use -1 to fetch all data)
			 * @param searchPhrase search word
			 * @return HTTP Response returned as JSON object
			 */
			@GET
			@Path("/{gameId}")
			@Produces(MediaType.APPLICATION_JSON)
			@ApiResponses(value = {
					@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Found a list of quests"),
					@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal Error"),
					@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized")})
			@ApiOperation(value = "getQuestList", 
						  notes = "Returns a list of quests",
						  response = QuestModel.class,
						  responseContainer = "List"
						  )
			public Response getQuestList(
					@ApiParam(value = "Game ID to return")@PathParam("gameId") String gameId,
					@ApiParam(value = "Page number for retrieving data")@QueryParam("current") int currentPage,
					@ApiParam(value = "Number of data size")@QueryParam("rowCount") int windowSize,
					@ApiParam(value = "Search phrase parameter")@QueryParam("searchPhrase") String searchPhrase)
			{
				
				// Request log
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_99, "GET " + "gamification/quests/"+gameId, true);
				long randomLong = new Random().nextLong(); //To be able to match 

				List<QuestModel> qs = null;
				Connection conn = null;

				JSONObject objResponse = new JSONObject();
				String name = null;
				Agent agent = Context.getCurrent().getMainAgent();
				if (agent instanceof AnonymousAgent) {
					return unauthorizedMessage();
				}
				else if (agent instanceof UserAgent) {
					UserAgent userAgent = (UserAgent) Context.getCurrent().getMainAgent();
					name = userAgent.getLoginName();
				}
				else {
					name = agent.getIdentifier();
				}
				try {
					conn = dbm.getConnection();			
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_46, ""+randomLong, true);

					
					try {
						if(!questAccess.isGameIdExist(conn,gameId)){
							objResponse.put("message", "Cannot get quests. Game not found");
							Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
							return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
						}
					} catch (SQLException e1) {
						e1.printStackTrace();
						objResponse.put("message", "Cannot get quests. Cannot check whether game ID exist or not. Database error. " + e1.getMessage());
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					}
					int offset = (currentPage - 1) * windowSize;
					int totalNum = questAccess.getNumberOfQuests(conn,gameId);

					if(windowSize == -1){
						offset = 0;
						windowSize = totalNum;
					}
					
					qs = questAccess.getAllQuests(conn,gameId);

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

					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_47, ""+randomLong, true);
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_48, ""+name, true);
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_49, ""+gameId, true);
					return Response.status(HttpURLConnection.HTTP_OK).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					
				} catch (SQLException e) {
					e.printStackTrace();
					objResponse.put("message", "Cannot get quests. Database error. " + e.getMessage());
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
				} catch (JsonProcessingException e) {
					e.printStackTrace();
					objResponse.put("message", "Cannot get quests. JSON process error. " + e.getMessage());
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(e.getMessage()).type(MediaType.APPLICATION_JSON).build();

				} catch (IOException e) {
					e.printStackTrace();
					objResponse.put("message", "Cannot get quests. IO Exception. " + e.getMessage());
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();

				}		 
				// always close connections
			    finally {
			      try {
					  if (conn != null) {
						  conn.close();
					  }
				  } catch (SQLException e) {
			        logger.printStackTrace(e);
			      }
			    }
			}


			// RMI
			/**
			 * RMI function to get quest detail with specific ID
			 * @param gameId gameId
			 * @param questId questId
			 * @return Serialized JSON string of a quest detail
			 */
			public String getQuestWithIdRMI(String gameId, String questId) {
				QuestModel quest;
				Connection conn = null;

				try {
					conn = dbm.getConnection();
					quest = questAccess.getQuestWithId(conn,gameId, questId);
					if(quest == null){
						return null;
					}
					ObjectMapper objectMapper = new ObjectMapper();
			    	//Set pretty printing of json
			    	objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
			    	
			    	String questString = objectMapper.writeValueAsString(quest);
			    	return questString;
				} catch (SQLException e) {
					e.printStackTrace();
					logger.warning("Get Quest with ID RMI failed. " + e.getMessage());
					return null;
				} catch (JsonProcessingException e) {
					e.printStackTrace();
					logger.warning("Get Quest with ID RMI failed. " + e.getMessage());
					return null;
				} catch (IOException e) {
					e.printStackTrace();
					logger.warning("Get Quest with ID RMI failed. " + e.getMessage());
					return null;
				}		 
				// always close connections
			    finally {
			      try {
					  if (conn != null) {
						  conn.close();
					  }
				  } catch (SQLException e) {
			        logger.printStackTrace(e);
			      }
			    }
			}
	  //}
}

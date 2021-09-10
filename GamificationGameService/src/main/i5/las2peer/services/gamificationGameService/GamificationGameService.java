package i5.las2peer.services.gamificationGameService;

import java.io.IOException;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.MediaType;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import i5.las2peer.api.Context;
import i5.las2peer.execution.L2pServiceException;
import i5.las2peer.logging.L2pLogger;
import i5.las2peer.logging.NodeObserver.Event;
import i5.las2peer.p2p.AgentNotKnownException;
import i5.las2peer.p2p.TimeoutException;
import i5.las2peer.restMapper.RESTService;
//import i5.las2peer.restMapper.HttpResponse;
//import i5.las2peer.restMapper.MediaType;
//import i5.las2peer.restMapper.RESTMapper;
//import i5.las2peer.restMapper.annotations.ContentParam;
//import i5.las2peer.restMapper.annotations.Version;
//import i5.las2peer.restMapper.tools.ValidationResult;
//import i5.las2peer.restMapper.tools.XMLCheck;
import i5.las2peer.security.L2pSecurityException;
import i5.las2peer.security.UserAgent;
import i5.las2peer.services.gamificationGameService.database.GameDAO;
import i5.las2peer.services.gamificationGameService.database.GameModel;
import i5.las2peer.services.gamificationGameService.database.DatabaseManager;
import i5.las2peer.services.gamificationGameService.database.MemberModel;
import i5.las2peer.services.gamificationGameService.helper.FormDataPart;
import i5.las2peer.services.gamificationGameService.helper.MultipartHelper;
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
 * Gamification Game Service
 * 
 * This is Gamification Game service to manage top level game in Gamification Framework
 * It uses the LAS2peer Web-Connector for RESTful access to it.
 * 
 * 
 */

@Path("/gamification/games")
@Api( value = "/gamification/games", authorizations = {
		@Authorization(value = "game_auth",
		scopes = {
			@AuthorizationScope(scope = "write:games", description = "modify games in your game"),
			@AuthorizationScope(scope = "read:games", description = "read your games")
				  })
}, tags = "games")
@SwaggerDefinition(
		info = @Info(
				title = "Gamification Game Service",
				version = "0.1",
				description = "Gamification Game Service for Gamification Framework",
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


public class GamificationGameService extends RESTService {

	// instantiate the logger class
	private final L2pLogger logger = L2pLogger.getInstance(GamificationGameService.class.getName());
	/*
	 * Database configuration
	 */
	private String jdbcDriverClassName;
	private String jdbcLogin;
	private String jdbcPass;
	private String jdbcUrl;
	private String jdbcSchema;
	private DatabaseManager dbm;

	private GameDAO gameAccess;
	
	// this header is not known to javax.ws.rs.core.HttpHeaders
	public static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";
	public static final String HEADER_CONTENT_TYPE = "Content-Type";
	// non HTTP standard headers
	public static final String HEADER_OWNERID = "ownerid";
	public static final String HEADER_CONTENT_DESCRIPTION = "Content-Description";

	public GamificationGameService() {
		// read and set properties values
		// IF THE SERVICE CLASS NAME IS CHANGED, THE PROPERTIES FILE NAME NEED TO BE CHANGED TOO!
		setFieldValues();
		dbm = new DatabaseManager(jdbcDriverClassName, jdbcLogin, jdbcPass, jdbcUrl, jdbcSchema);
		this.gameAccess = new GameDAO();
	}

	@Override
	  protected void initResources() {
	    //getResourceConfig().register(Resource.class);
		 System.out.println("jojojoj");
	  }
//
//	  @Path("/") // this is the root resource
//	  public static class Resource {
//	    // put here all your service methods
//		  
		  /**
			 * Function to delete directories of an game in badge service and point service file system
			 * @return true if directories are deleted
			 */
			private boolean cleanStorage(String gameId) throws AgentNotKnownException, L2pServiceException, L2pSecurityException, InterruptedException, TimeoutException {

				Object result = this.invokeServiceMethod("i5.las2peer.services.gamificationBadgeService.GamificationBadgeService@0.1", "cleanStorageRMI", new Serializable[] { gameId });
				
				if (result != null) {
					L2pLogger.logEvent(this, Event.RMI_SENT, "Clean Badge Service Storage : " + gameId);
					
					if((int)result == 1){
						L2pLogger.logEvent(this, Event.RMI_SUCCESSFUL, "Clean Badge Service Storage : " + gameId);
						
						Object res = this.invokeServiceMethod("i5.las2peer.services.gamificationPointService.GamificationPointService@0.1", "cleanStorageRMI", new Serializable[] { gameId });
						if (res != null) {
							L2pLogger.logEvent(this, Event.RMI_SENT, "Clean Point Service Storage : " + gameId);
							if((int)res == 1){
								L2pLogger.logEvent(this, Event.RMI_SUCCESSFUL, "Clean Point Service Storage : " + gameId);
								
								return true;
							}
						}
					}
				}
				L2pLogger.logEvent(this, Event.RMI_FAILED, "Clean Badge or Point Service Storage : " + gameId);
				
				return false;
			}
			
			/**
			 * Function to return http unauthorized message
			 * @return HTTP Response returns JSON objects
			 */
			private Response unauthorizedMessage(){
				JSONObject objResponse = new JSONObject();
				//logger.info("You are not authorized >> " );
				objResponse.put("message", "You are not authorized");
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, "Not Authorized");
				return Response.status(HttpURLConnection.HTTP_UNAUTHORIZED).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
				//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_UNAUTHORIZED);

			}
			
			// //////////////////////////////////////////////////////////////////////////////////////
			// Game PART --------------------------------------
			// //////////////////////////////////////////////////////////////////////////////////////
			
			// TODO Basic single CRUD -------------------------------------
			
			/**
			 * Create a new game. 
			 * Name attribute for form data : 
			 * <ul>
			 * 	<li>gameid - Game ID - String (20 chars)
			 *  <li>commtype - Community Type - String (20 chars)
			 *  <li>gamedesc - Game Description - String (50 chars)
			 * </ul>
			 * 
			 * @param contentType form content type
			 * @param formData Form data with multipart/form-data type
			 * @return Game data in JSON
			 */
			@POST
			@Path("/data")
			@Produces(MediaType.APPLICATION_JSON)
			@ApiOperation(value = "createGame",
					notes = "Method to create a new game")
			@ApiResponses(value = {
					@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Cannot connect to database"),
					@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Database Error"),
					@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Error in parsing form data"),
					@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Game ID already exist"),
					@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Game ID cannot be empty"),
					@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Error checking app ID exist"),
					@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
					@ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "New game created")
			})
			public Response createGame(
					@ApiParam(value = "Game detail in multiple/form-data type", required = true)@HeaderParam(value = HttpHeaders.CONTENT_TYPE) String contentType,
					@ApiParam(value = "Content of form data", required = true) byte[] formData) {
				
				// Request log
				L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_99,Context.getCurrent().getMainAgent(), "POST " + "gamification/games/data");
				long randomLong = new Random().nextLong(); //To be able to match 

				JSONObject objResponse = new JSONObject();
				UserAgent userAgent = (UserAgent) Context.getCurrent().getMainAgent();
				String name = userAgent.getLoginName();
				String gameid = null;
				String gamedesc = null;
				String commtype = null;
				Connection conn = null;

				
				if(name.equals("anonymous")){
					return unauthorizedMessage();
				}
				
				Map<String, FormDataPart> parts;
				try {
					conn = dbm.getConnection();
					
					parts = MultipartHelper.getParts(formData, contentType);
					FormDataPart partGameID = parts.get("gameid");
					if (partGameID != null) {
						// these data belong to the (optional) file id text input form element
						gameid = partGameID.getContent();
						// gameid must be unique
						System.out.println(gameid);
						if(gameAccess.isGameIdExist(conn,gameid)){
							// game id already exist
							objResponse.put("message", "Game ID already exist");
							L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
							return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
							//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
						}
						
						FormDataPart partGameDesc = parts.get("gamedesc");
						if (partGameDesc != null) {
							gamedesc = partGameDesc.getContent();
						}
						else{
							gamedesc = "";
						}
						FormDataPart partCommType = parts.get("commtype");
						if (partGameDesc != null) {
							commtype = partCommType.getContent();
						}
						else{
							commtype = "def_type";
						}
						
						GameModel newGame = new GameModel(gameid, gamedesc, commtype);

						try{
							L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_1,Context.getCurrent().getMainAgent(), ""+randomLong);
							gameAccess.addNewGame(conn,newGame);
							gameAccess.addMemberToGame(conn,newGame.getId(), name);
							L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_2,Context.getCurrent().getMainAgent(), ""+randomLong);
							L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_3,Context.getCurrent().getMainAgent(), ""+name);
							L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_4,Context.getCurrent().getMainAgent(), ""+newGame.getId());
							objResponse.put("message", "New game created");
							return Response.status(HttpURLConnection.HTTP_CREATED).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
							//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_CREATED);

						} catch (SQLException e) {
							e.printStackTrace();
							objResponse.put("message", "Cannot Add New Game. Database Error. " + e.getMessage());
							L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
							return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
							//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
						}
					}
					else{
						// game id cannot be empty
						objResponse.put("message", "Cannot Add New Game. Game ID cannot be empty.");
						L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
						//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
					}
				}
				catch (IOException e) {
					e.printStackTrace();
					objResponse.put("message", "Cannot Add New Game. Error in parsing form data. " + e.getMessage());
					L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
				} catch (SQLException e) {
					e.printStackTrace();
					objResponse.put("message", "Cannot Add New Game. Error checking game ID exist. "  + e.getMessage());
					L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
				} 		 
				// always close connections
			    finally {
			      try {
			        conn.close();
			      } catch (SQLException e) {
			        logger.printStackTrace(e);
			      }
			    }
			}

			/**
			 * Get a game data with specified ID
			 * @param gameId Game ID
			 * @return Game model {@link GameModel} data in JSON
			 */
			@GET
			@Path("/data/{gameId}")
			@Produces(MediaType.APPLICATION_JSON)
			@ApiOperation(value = "getGameDetails",
						notes = "Get an game data with specific ID")
			@ApiResponses(value = {
					@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Return game data with specific ID"),
					@ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Method not found"),
					@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Game not found"),
					@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Cannot connect to database"),
					@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Database Error"),
					@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Failed to process JSON")
			})
			public Response getGameDetails(
					@ApiParam(value = "Game ID", required = true)@PathParam("gameId") String gameId)
			{
				// Request log
				L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_99,Context.getCurrent().getMainAgent(), "GET "+ "gamification/games/data/"+gameId);
						
				JSONObject objResponse = new JSONObject();
				Connection conn = null;

				UserAgent userAgent = (UserAgent) Context.getCurrent().getMainAgent();
				String name = userAgent.getLoginName();
				if(name.equals("anonymous")){
					return unauthorizedMessage();
				}
				
				try {
					conn = dbm.getConnection();
					if(!gameAccess.isGameIdExist(conn,gameId)){
						objResponse.put("message", "Cannot get Game detail. Game not found");
						L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
						//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
					}
					// Add Member to Game
					GameModel game = gameAccess.getGameWithId(conn,gameId);
					ObjectMapper objectMapper = new ObjectMapper();
			    	//Set pretty printing of json
			    	objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
			    	String gameString = objectMapper.writeValueAsString(game);
			    	return Response.status(HttpURLConnection.HTTP_OK).entity(gameString).type(MediaType.APPLICATION_JSON).build();
			    	//return new HttpResponse(gameString, HttpURLConnection.HTTP_OK);
				} catch (SQLException e) {
					e.printStackTrace();
					objResponse.put("message", "Cannot get Game detail. Database Error. " + e.getMessage());
					L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
				} catch (JsonProcessingException e) {
					e.printStackTrace();
					objResponse.put("message", "Cannot get Game detail. Failed to process JSON. " + e.getMessage());
					L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
				}		 
				// always close connections
			    finally {
			      try {
			        conn.close();
			      } catch (SQLException e) {
			        logger.printStackTrace(e);
			      }
			    }
			}
			
			/**
			 * Delete a game data with specified ID
			 * @param gameId Game ID
			 * @return HTTP Response with the returnString
			 */
			@DELETE
			@Path("/data/{gameId}")
			@Produces(MediaType.APPLICATION_JSON)	
			@ApiOperation(value = "Delete Game",
			  			notes = "This method deletes an Game")
			@ApiResponses(value = {
					@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Game Deleted"),
					@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Game not found"),
					@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Game not found"),
					@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Cannot connect to database"),
					@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Error checking game ID exist"),
					@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Database error"),
					@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Error delete storage"),
					@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized")})

			public Response deleteGame(
					@ApiParam(value = "Game ID", required = true)@PathParam("gameId") String gameId)
			{
				// Request log
				L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_99,Context.getCurrent().getMainAgent(), "DELETE " + "gamification/games/data/"+gameId);
						
				JSONObject objResponse = new JSONObject();
				Connection conn = null;

				UserAgent userAgent = (UserAgent) Context.getCurrent().getMainAgent();
				String name = userAgent.getLoginName();
				if(name.equals("anonymous")){
					return unauthorizedMessage();
				}

				try {
					conn = dbm.getConnection();
					if(!gameAccess.isGameIdExist(conn,gameId)){
						objResponse.put("message", "Cannot delete Game. Game not found.");
						L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
						//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
					}
					try {
						if(cleanStorage(gameId)){
							//if(gameAccess.removeGameInfo(gameId)){

								L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_12,Context.getCurrent().getMainAgent(), ""+name);
								if(gameAccess.deleteGameDB(conn,gameId)){
									objResponse.put("message", "Game deleted");
									L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_13,Context.getCurrent().getMainAgent(), ""+name);
									return Response.status(HttpURLConnection.HTTP_OK).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
									//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_OK);
								}
							//}
							objResponse.put("message", "Cannot delete Game. Database error. ");
							L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
							return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
							//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
						}
					} catch (AgentNotKnownException | L2pServiceException | L2pSecurityException | InterruptedException
							| TimeoutException e) {
						e.printStackTrace();
						L2pLogger.logEvent(Event.RMI_FAILED, "Failed to clean storage");
						objResponse.put("message", "RMI error. Failed to clean storage. ");
						return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
						//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);

					}
				} catch (SQLException e) {
					e.printStackTrace();
					objResponse.put("message", "Cannot delete Game. Error checking game ID exist. "  + e.getMessage());
					L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
				}			 
				// always close connections
			    finally {
			      try {
			        conn.close();
			      } catch (SQLException e) {
			        logger.printStackTrace(e);
			      }
			    }	
					
				objResponse.put("message", "Cannot delete Game. Error delete storage.");
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
				//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
			}

			
			/**
			 * Get all game list separated into two categories. All games registered for the member and other games.
			 * 
			 * 
			 * @return HTTP Response with the returnString
			 */
			@GET
			@Path("/list/separated")
			@Produces(MediaType.APPLICATION_JSON)
			@ApiOperation(value = "getSeparateGameInfo",
					notes = "Get all game list separated into two categories. All games registered for the member and other games.")
			@ApiResponses(value = {
					@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "List of games"),
					@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
					@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Database error"),
					@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "JsonProcessingException")
			})
			public Response getSeparateGameInfo() {
				// Request log
				L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_99,Context.getCurrent().getMainAgent(), "GET " + "gamification/games/list/separated");
						
				JSONObject objResponse = new JSONObject();
				Connection conn = null;

				UserAgent userAgent = (UserAgent) Context.getCurrent().getMainAgent();
				String name = userAgent.getLoginName();
				if(name.equals("anonymous")){
					return unauthorizedMessage();
				}
				ObjectMapper objectMapper = new ObjectMapper();
		    	//Set pretty printing of json
		    	objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		    	try {
					conn = dbm.getConnection();
					//List<List<GameModel>> allGames = gameAccess.getSeparateGamesWithMemberId(conn,name);
					JSONArray allGames = gameAccess.getAllGamesWithMemberInformation(conn, name);
					L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_11,Context.getCurrent().getMainAgent(), ""+name);
					return Response.status(HttpURLConnection.HTTP_OK).entity(allGames.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					//return new HttpResponse(allGames.toJSONString(), HttpURLConnection.HTTP_OK);

//					try {
//						String response = objectMapper.writeValueAsString(allGames);
//						allGames.clear();
//						L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_11,Context.getCurrent().getMainAgent(), ""+name);
//						
//						return new HttpResponse(response, HttpURLConnection.HTTP_OK);
//					
//					} catch (JsonProcessingException e) {
//						e.printStackTrace();
//						
//						allGames.clear();
//						// return HTTP Response on error
//						objResponse.put("message", "Cannot delete Game. JsonProcessingException." + e.getMessage());
//						L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
//						return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
//					}
				} catch (SQLException e) {
					
					e.printStackTrace();
					objResponse.put("message", "Database error");
					L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
				}		 
				// always close connections
			    finally {
			      try {
			        conn.close();
			      } catch (SQLException e) {
			        logger.printStackTrace(e);
			      }
			    }
				
			}

			// TODO Other games functions ----------------------------------
			/**
			 * Remove a member from the game
			 * @param gameId gameId
			 * @param memberId memberId
			 * @return HTTP Response status if a member is removed
			 */
			@DELETE
			@Path("/data/{gameId}/{memberId}")
			@Produces(MediaType.APPLICATION_JSON)
			@ApiOperation(value = "removeMemberFromGame",
						notes = "delete a member from an game")
			@ApiResponses(value = {
					@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Member is removed from game"),
					@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Game not found"),
					@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Error checking game ID exist"),
					@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "No member found"),
					@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Database error")
			})
			public Response removeMemberFromGame(
					@ApiParam(value = "Game ID", required = true)@PathParam("gameId") String gameId,
					@ApiParam(value = "Member ID", required = true)@PathParam("memberId") String memberId)
			{
				// Request log
				L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_99,Context.getCurrent().getMainAgent(), "DELETE " + "gamification/games/data/"+gameId+"/"+memberId);
						
				JSONObject objResponse = new JSONObject();
				Connection conn = null;

				UserAgent userAgent = (UserAgent) Context.getCurrent().getMainAgent();
				String name = userAgent.getLoginName();
				if(name.equals("anonymous")){
					return unauthorizedMessage();
				}
				try {
					conn = dbm.getConnection();
					if(!gameAccess.isGameIdExist(conn,gameId)){
						objResponse.put("message", "Cannot remove member from Game. Game not found");
						L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
						//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
					}
					try {
						if(!gameAccess.isMemberRegistered(conn,memberId)){
							objResponse.put("message", "Cannot remove member from Game. No member found");
							L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
							return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
							//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
						}
						gameAccess.removeMemberFromGame(conn,memberId, gameId);
						objResponse.put("message", memberId + "is removed from " + gameId);

						L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_5,Context.getCurrent().getMainAgent(), ""+ memberId);
						L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_6,Context.getCurrent().getMainAgent(), ""+ gameId);
						return Response.status(HttpURLConnection.HTTP_OK).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
						//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_OK);

					}
					catch (SQLException e) {
						e.printStackTrace();
						objResponse.put("message", "Cannot remove member from Game. Database error. " + e.getMessage());
						L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
						//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
					}
				} catch (SQLException e) {
					e.printStackTrace();
					objResponse.put("message", "Cannot remove member from Game. Error checking game ID exist "  + e.getMessage());
					L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
				}		 
				// always close connections
			    finally {
			      try {
			        conn.close();
			      } catch (SQLException e) {
			        logger.printStackTrace(e);
			      }
			    }

			}
			
			/**
			 * Add a member to the game
			 * @param gameId gameId
			 * @param memberId memberId
			 * @return HTTP Response status if the member is added
			 */
			@POST
			@Path("/data/{gameId}/{memberId}")
			@Produces(MediaType.APPLICATION_JSON)
			@ApiResponses(value = {
					@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Member is Added"),
					@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Game not found"),
					@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Error checking game ID exist"),
					@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Database error")
			})
			@ApiOperation(value = "addMemberToGame",
						  notes = "add a member to an game")
			public Response addMemberToGame(
					@ApiParam(value = "Game ID", required = true)@PathParam("gameId") String gameId,
					@ApiParam(value = "Member ID", required = true)@PathParam("memberId") String memberId)
			{
				// Request log
				L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_99,Context.getCurrent().getMainAgent(), "POST " + "gamification/games/data/"+gameId+"/"+memberId);
				
				JSONObject objResponse = new JSONObject();
				Connection conn = null;

				UserAgent userAgent = (UserAgent) Context.getCurrent().getMainAgent();
				String name = userAgent.getLoginName();
				if(name.equals("anonymous")){
					return unauthorizedMessage();
				}
				try {
					conn = dbm.getConnection();
					if(!gameAccess.isGameIdExist(conn,gameId)){
						objResponse.put("message", "Cannot add member to Game. Game not found");
						L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
						//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
					}
					try {
						gameAccess.addMemberToGame(conn,gameId, memberId);
						objResponse.put("success", memberId + " is added to " + gameId);
						L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_3,Context.getCurrent().getMainAgent(), ""+memberId);
						L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_4,Context.getCurrent().getMainAgent(), ""+gameId);
						return Response.status(HttpURLConnection.HTTP_OK).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
						//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_OK);
					}
					catch (SQLException e) {
						
						e.printStackTrace();
						objResponse.put("message", "Cannot add member to Game. Database error. " + e.getMessage());
						L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
						//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
					}
				} catch (SQLException e) {
					e.printStackTrace();
					objResponse.put("message", "Cannot add member to Game. Error checking game ID exist. "  + e.getMessage());
					L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
				}		 
				// always close connections
			    finally {
			      try {
			        conn.close();
			      } catch (SQLException e) {
			        logger.printStackTrace(e);
			      }
			    }		
				
			}
			
			/**
			 * Validate member and add to the database as the new member if he/she is not registered yet
			 * @return HTTP Response status if validation success
			 */
			@POST
			@Path("/validation")
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
			public Response memberLoginValidation() {
				// Request log
				L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_99,Context.getCurrent().getMainAgent(), "POST " + "gamification/games/validation");
				
				JSONObject objResponse = new JSONObject();
					
					MemberModel member;
					Connection conn = null;

					UserAgent userAgent = (UserAgent) Context.getCurrent().getMainAgent();
					// take username as default name
					String name = userAgent.getLoginName();
					System.out.println("User name : " + name);
					if(name.equals("anonymous")){
						return unauthorizedMessage();
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
							//logger.info(member.getId()+" "+member.getFullName()+" "+member.getEmail());
							try {
								conn = dbm.getConnection();
								if(!gameAccess.isMemberRegistered(conn,member.getId())){
									gameAccess.registerMember(conn,member);
									objResponse.put("message", "Welcome " + member.getId() + "!");
									L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_7,Context.getCurrent().getMainAgent(), ""+member.getId());
									return Response.status(HttpURLConnection.HTTP_OK).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
									//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_OK);
								}
							} catch (SQLException e) {
								e.printStackTrace();
								objResponse.put("message", "Cannot validate member login. Database Error. " + e.getMessage());
								L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
								return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
								//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
							}		 
							// always close connections
						    finally {
						      try {
						        conn.close();
						      } catch (SQLException e) {
						        logger.printStackTrace(e);
						      }
						    }	
						} else {
							//logger.warning("Parsing user data failed! Got '" + jsonUserData.getClass().getName() + " instead of "+ JSONObject.class.getName() + " expected!");
							objResponse.put("message", "Cannot validate member login. User data error to be retrieved. Not JSON object.");
							L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
							return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
							//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
						}
						objResponse.put("message", "Member already registered");
						L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_8,Context.getCurrent().getMainAgent(), ""+member.getId());
						return Response.status(HttpURLConnection.HTTP_OK).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
						//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_OK);
					}
					else{
						objResponse.put("message", "Cannot validate member login. User data error to be retrieved.");
						L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
						//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
					}
						
				
			}
			
			
			/**
			 * Checking whether the game ID is already registered or not.
			 * This function is to be invoked via RMI by another services
			 * 
			 * @param gameId game ID
			 * @return 1, if game ID exist
			 */
			public int isGameWithIdExist(String gameId){
				Connection conn = null;

				try {
					conn = dbm.getConnection();
					if(gameAccess.isGameIdExist(conn,gameId)){
						L2pLogger.logEvent(this, Event.RMI_SUCCESSFUL, "RMI isGameWithIdExist is invoked");
						return 1;
					}
					else{
						L2pLogger.logEvent(this, Event.RMI_SUCCESSFUL, "RMI isGameWithIdExist is invoked");
						return 0;
					}
				} catch (SQLException e) {
					e.printStackTrace();
					L2pLogger.logEvent(this, Event.RMI_FAILED, "Exception when checking Game ID exists or not. " + e.getMessage());
					return 0;
				}		 
				// always close connections
			    finally {
			      try {
			        conn.close();
			      } catch (SQLException e) {
			        logger.printStackTrace(e);
			      }
			    }	
			}
	  //}
	
	
	

}

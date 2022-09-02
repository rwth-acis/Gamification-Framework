package i5.las2peer.services.gamificationGameService;

import java.io.IOException;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.MediaType;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import i5.las2peer.logging.L2pLogger;
import i5.las2peer.p2p.TimeoutException;
import i5.las2peer.restMapper.RESTService;
import i5.las2peer.restMapper.annotations.ServicePath;
import i5.las2peer.api.Context;
import i5.las2peer.api.logging.MonitoringEvent;
import i5.las2peer.api.security.Agent;
import i5.las2peer.api.security.AgentNotFoundException;
import i5.las2peer.api.security.AnonymousAgent;
import i5.las2peer.api.ManualDeployment;
import i5.las2peer.api.security.UserAgent;
import i5.las2peer.api.execution.InternalServiceException;
import i5.las2peer.services.gamificationGameService.database.GameDAO;
import i5.las2peer.services.gamificationGameService.database.GameModel;
import i5.las2peer.services.gamificationGameService.database.DatabaseManager;
import i5.las2peer.services.gamificationGameService.database.MemberModel;
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
import org.glassfish.jersey.media.multipart.FormDataParam;


/**
 * Gamification Game Service
 * 
 * This is Gamification Game service to manage top level game in Gamification Framework
 * It uses the LAS2peer Web-Connector for RESTful access to it.
 * 
 * 
 */
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
@ManualDeployment
@ServicePath("/gamification/games")
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

	public static final String DEFAULT_COMM_TYPE = "def_type";

	public GamificationGameService() {
		// read and set properties values
		// IF THE SERVICE CLASS NAME IS CHANGED, THE PROPERTIES FILE NAME NEED TO BE CHANGED TOO!
		setFieldValues();
		dbm = new DatabaseManager(jdbcDriverClassName, jdbcLogin, jdbcPass, jdbcUrl, jdbcSchema);
		this.gameAccess = new GameDAO();
	}

		  /**
			 * Function to delete directories of an game in badge service and point service file system
			 * @return true if directories are deleted
			 */
			private boolean cleanStorage(String gameId) throws AgentNotFoundException, InternalServiceException,
			InterruptedException, TimeoutException {
				Object result = null;
				try {
					result = Context.getCurrent().invoke("i5.las2peer.services.gamificationBadgeService.GamificationBadgeService@0.1", "cleanStorageRMI", new Serializable[] { gameId });
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (result != null) {
					Context.getCurrent().monitorEvent(this, MonitoringEvent.RMI_SENT, "Clean Badge Service Storage : " + gameId + result);
					
					if(((Integer)result) == 1){
						Context.getCurrent().monitorEvent(this, MonitoringEvent.RMI_SUCCESSFUL, "Clean Badge Service Storage : " + gameId);
						Object res=null;
						try {
							res = Context.getCurrent().invoke("i5.las2peer.services.gamificationPointService.GamificationPointService@0.1", "cleanStorageRMI", new Serializable[] { gameId });
						}
						catch (Exception e) {
							e.printStackTrace();
						}
						if (res != null) {
							Context.getCurrent().monitorEvent(this, MonitoringEvent.RMI_SENT, "Clean Point Service Storage : " + gameId + res);
							if(((Integer)res )== 1){
								Context.getCurrent().monitorEvent(this, MonitoringEvent.RMI_SUCCESSFUL, "Clean Point Service Storage : " + gameId);
								
								return true;
							}
						}
					}
				}
				Context.getCurrent().monitorEvent(this, MonitoringEvent.RMI_FAILED, "Clean Badge or Point Service Storage : " + gameId);
				
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
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, "Not Authorized");
				return Response.status(HttpURLConnection.HTTP_UNAUTHORIZED).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();

			}
			
			// //////////////////////////////////////////////////////////////////////////////////////
			// Game PART --------------------------------------
			// //////////////////////////////////////////////////////////////////////////////////////
			
			
			/**
			 * Create a new game.
			 *
			 * @param gameid Game ID - String (20 chars, only lower case!)
			 * @param commtype Community Type - String (20 chars)
			 * @param gamedesc Game Description - String (50 chars)
			 * @return Game data in JSON
			 */
			@POST
			@Path("/data")
			@Consumes(MediaType.MULTIPART_FORM_DATA)
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
					@ApiParam(value = "Game ID - String (20 chars, only lower case!)", required = true) @FormDataParam("gameid") String gameid,
					@ApiParam(value = "Community Type - String (20 chars)", defaultValue = DEFAULT_COMM_TYPE) @FormDataParam("commtype") String commtype,
					@ApiParam(value = "Game Description - String (50 chars)", defaultValue = "") @FormDataParam("gamedesc") String gamedesc
			) {
				// Request log
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_99, "POST " + "gamification/games/data", true);
				long randomLong = new Random().nextLong(); //To be able to match 

				JSONObject objResponse = new JSONObject();
				String name = null;
				Connection conn = null;
				
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

					if (!isGameIdValid(gameid)) {
						objResponse.put("message", "Invalid game ID. Game ID MUST NOT be blank and MUST NOT contain upper case characters. Max length is 20.");
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					}

					// gameid must be unique
					System.out.println(gameid);
					if(gameAccess.isGameIdExist(conn,gameid)){
						// game id already exist
						objResponse.put("message", "Game ID already exist");
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					}

					gamedesc = Objects.requireNonNullElse(gamedesc, "");
					commtype = Objects.requireNonNullElse(commtype, DEFAULT_COMM_TYPE);

					GameModel newGame = new GameModel(gameid, gamedesc, commtype);

					try{
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_1, ""+randomLong, true);
						gameAccess.addNewGame(conn,newGame);
						gameAccess.addMemberToGame(conn,newGame.getId(), name);
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_2, ""+randomLong, true);
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_3, ""+name, true);
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_4, ""+newGame.getId(), true);
						objResponse.put("message", "New game created");
						return Response.status(HttpURLConnection.HTTP_CREATED).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();

					} catch (SQLException e) {
						e.printStackTrace();
						objResponse.put("message", "Cannot Add New Game. Database Error. " + e.getMessage());
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					}
				} catch (SQLException e) {
					e.printStackTrace();
					objResponse.put("message", "Cannot Add New Game. Error checking game ID exist. "  + e.getMessage());
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
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

			private boolean isGameIdValid(String gameId) {
				// ID must not be blank
				if (gameId == null || gameId.isBlank()) {
					return false;
				}
				/*
				 * This constraint is caused by the internal SQL functions, which use the
				 * LOWER_CASE game ID as a database schema name. However, not all functions
				 * implement this correctly. For example, a function might try to query a concrete game
				 * using a schema name:
				 *     SELECT * FROM manager.game_info WHERE game_id = schema_name
				 * This query will fail if the game ID is 'testGameId' because the schema name will be 'testgameid'.
				 *
				 * Instead of fixing this in the SQL code, which is very hard to maintain, we will enforce this
				 * constraint on game IDs.
				 *
				 * Check https://github.com/rwth-acis/Gamification-Framework/issues/29 for more.
				 */
				// ID must not contain upper case characters
				if (gameId.chars().anyMatch(Character::isUpperCase)) {
					return false;
				}
				// max length is 20
				if (gameId.length() > 20) {
					return false;
				}
				return true;
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
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_99, "GET "+ "gamification/games/data/"+gameId, true);
						
				JSONObject objResponse = new JSONObject();
				Connection conn = null;
				
				
				Agent agent = Context.getCurrent().getMainAgent();
				if (agent instanceof AnonymousAgent) {
					return unauthorizedMessage();
				}
				
				
				try {
					conn = dbm.getConnection();
					if(!gameAccess.isGameIdExist(conn,gameId)){
						objResponse.put("message", "Cannot get Game detail. Game not found");
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					}
					// Add Member to Game
					GameModel game = gameAccess.getGameWithId(conn,gameId);
					ObjectMapper objectMapper = new ObjectMapper();
			    	//Set pretty printing of json
			    	objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
			    	String gameString = objectMapper.writeValueAsString(game);
			    	return Response.status(HttpURLConnection.HTTP_OK).entity(gameString).type(MediaType.APPLICATION_JSON).build();
				} catch (SQLException e) {
					e.printStackTrace();
					objResponse.put("message", "Cannot get Game detail. Database Error. " + e.getMessage());
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
				} catch (JsonProcessingException e) {
					e.printStackTrace();
					objResponse.put("message", "Cannot get Game detail. Failed to process JSON. " + e.getMessage());
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
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
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_99, "DELETE " + "gamification/games/data/"+gameId, true);
						
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
					if(!gameAccess.isGameIdExist(conn,gameId)){
						objResponse.put("message", "Cannot delete Game. Game not found.");
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					}
					try {
						if(cleanStorage(gameId)){
							//if(gameAccess.removeGameInfo(gameId)){

								Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_12, ""+name, true);
								if(gameAccess.deleteGameDB(conn,gameId)){
									objResponse.put("message", "Game deleted");
									Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_13, ""+name, true);
									return Response.status(HttpURLConnection.HTTP_OK).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
								}
							//}
							objResponse.put("message", "Cannot delete Game. Database error. ");
							Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
							return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
						}
					} catch (AgentNotFoundException | InternalServiceException |
							InterruptedException | TimeoutException e) {
						e.printStackTrace();
						Context.getCurrent().monitorEvent(this, MonitoringEvent.RMI_FAILED, "Failed to clean storage");
						objResponse.put("message", "RMI error. Failed to clean storage. ");
						return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();

					}
				} catch (SQLException e) {
					e.printStackTrace();
					objResponse.put("message", "Cannot delete Game. Error checking game ID exist. "  + e.getMessage());
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
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
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
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
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_99, "GET " + "gamification/games/list/separated", true);
						
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
				ObjectMapper objectMapper = new ObjectMapper();
		    	//Set pretty printing of json
		    	objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		    	try {
					conn = dbm.getConnection();
					//List<List<GameModel>> allGames = gameAccess.getSeparateGamesWithMemberId(conn,name);
					JSONArray allGames = gameAccess.getAllGamesWithMemberInformation(conn, name);
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_11, ""+name, true);
					return Response.status(HttpURLConnection.HTTP_OK).entity(allGames.toJSONString()).type(MediaType.APPLICATION_JSON).build();

				} catch (SQLException e) {
					
					e.printStackTrace();
					objResponse.put("message", "Database error");
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
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
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_99, "DELETE " + "gamification/games/data/"+gameId+"/"+memberId, true);
						
				JSONObject objResponse = new JSONObject();
				Connection conn = null;

				
				Agent agent = Context.getCurrent().getMainAgent();
				if (agent instanceof AnonymousAgent) {
					return unauthorizedMessage();
				}
				
				try {
					conn = dbm.getConnection();
					if(!gameAccess.isGameIdExist(conn,gameId)){
						objResponse.put("message", "Cannot remove member from Game. Game not found");
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					}
					try {
						if(!gameAccess.isMemberRegistered(conn,memberId)){
							objResponse.put("message", "Cannot remove member from Game. No member found");
							Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
							return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
						}
						gameAccess.removeMemberFromGame(conn,memberId, gameId);
						objResponse.put("message", memberId + "is removed from " + gameId);

						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_5, ""+ memberId, true);
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_6, ""+ gameId, true);
						return Response.status(HttpURLConnection.HTTP_OK).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();

					}
					catch (SQLException e) {
						e.printStackTrace();
						objResponse.put("message", "Cannot remove member from Game. Database error. " + e.getMessage());
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					}
				} catch (SQLException e) {
					e.printStackTrace();
					objResponse.put("message", "Cannot remove member from Game. Error checking game ID exist "  + e.getMessage());
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
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
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_99, "POST " + "gamification/games/data/"+gameId+"/"+memberId, true);
				
				JSONObject objResponse = new JSONObject();
				Connection conn = null;

				
				Agent agent = Context.getCurrent().getMainAgent();
				if (agent instanceof AnonymousAgent) {
					return unauthorizedMessage();
				}
				
				try {
					conn = dbm.getConnection();
					if(!gameAccess.isGameIdExist(conn,gameId)){
						objResponse.put("message", "Cannot add member to Game. Game not found");
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					}
					try {
						gameAccess.addMemberToGame(conn,gameId, memberId);
						objResponse.put("success", memberId + " is added to " + gameId);
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_3, ""+memberId, true);
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_4, ""+gameId, true);
						return Response.status(HttpURLConnection.HTTP_OK).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					}
					catch (SQLException e) {
						
						e.printStackTrace();
						objResponse.put("message", "Cannot add member to Game. Database error. " + e.getMessage());
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					}
				} catch (SQLException e) {
					e.printStackTrace();
					objResponse.put("message", "Cannot add member to Game. Error checking game ID exist. "  + e.getMessage());
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
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
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_99, "POST " + "gamification/games/validation", true);
				
				JSONObject objResponse = new JSONObject();
					
					MemberModel member;
					Connection conn = null;
					
					String name = null;
					String email = null;
					Agent agent = Context.getCurrent().getMainAgent();
					if (agent instanceof AnonymousAgent) {
						return unauthorizedMessage();
					}
					else if (agent instanceof UserAgent) {
						UserAgent userAgent = (UserAgent) Context.getCurrent().getMainAgent();
						name = userAgent.getLoginName();
						email = userAgent.getEmail();
					}
					else {
						name = agent.getIdentifier();
					}

					if (name != "" && email != "") {//userData != null
						if (name != "" && email != ""){//jsonUserData instanceof JSONObject
							String lastname ="";
							String firstname ="";
							member = new MemberModel(name,firstname,lastname,email);
							try {
								conn = dbm.getConnection();
								if(!gameAccess.isMemberRegistered(conn,member.getId())){
									gameAccess.registerMember(conn,member);
									objResponse.put("message", "Welcome " + member.getId() + "!");
									Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_7, ""+member.getId(), true);
									return Response.status(HttpURLConnection.HTTP_OK).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
								}
							} catch (SQLException e) {
								e.printStackTrace();
								objResponse.put("message", "Cannot validate member login. Database Error. " + e.getMessage());
								Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
								return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
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
							objResponse.put("message", "Cannot validate member login. User data error to be retrieved. Not JSON object.");
							Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
							return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
						}
						objResponse.put("message", "Member already registered");
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_8, ""+member.getId(), true);
						return Response.status(HttpURLConnection.HTTP_OK).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					}
					else{
						objResponse.put("message", "Cannot validate member login. User data error to be retrieved.");
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
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
						Context.getCurrent().monitorEvent(this, MonitoringEvent.RMI_SUCCESSFUL, "RMI isGameWithIdExist is invoked");
						return 1;
					}
					else{
						Context.getCurrent().monitorEvent(this, MonitoringEvent.RMI_SUCCESSFUL, "RMI isGameWithIdExist is invoked");
						return 0;
					}
				} catch (SQLException e) {
					e.printStackTrace();
					Context.getCurrent().monitorEvent(this, MonitoringEvent.RMI_FAILED, "Exception when checking Game ID exists or not. " + e.getMessage());
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

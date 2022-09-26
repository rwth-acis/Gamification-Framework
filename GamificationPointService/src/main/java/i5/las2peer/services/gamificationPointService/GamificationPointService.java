package i5.las2peer.services.gamificationPointService;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Random;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.MediaType;

import i5.las2peer.logging.L2pLogger;
import i5.las2peer.restMapper.RESTService;
import i5.las2peer.restMapper.annotations.ServicePath;
import i5.las2peer.api.Context;
import i5.las2peer.api.logging.MonitoringEvent;
import i5.las2peer.api.ManualDeployment;
import i5.las2peer.api.security.Agent;
import i5.las2peer.api.security.AnonymousAgent;
import i5.las2peer.api.security.UserAgent;
import i5.las2peer.services.gamification.commons.database.DatabaseManager;
import i5.las2peer.services.gamificationPointService.database.PointDAO;
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

/**
 * Gamification Point Service
 * 
 * This is Gamification Point service to manage point element in Gamification Framework
 * It uses the LAS2peer Web-Connector for RESTful access to it.
 * 
 * Note:
 * If you plan on using Swagger you should adapt the information below
 * in the ApiInfo annotation to suit your project.
 * If you do not intend to provide a Swagger documentation of your service API,
 * the entire ApiInfo annotation should be removed.
 * 
 */
@Api( value = "/gamification/points", authorizations = {
		@Authorization(value = "points_auth",
		scopes = {
			@AuthorizationScope(scope = "write:points", description = "modify point in your game"),
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
@ManualDeployment
@ServicePath("/gamification/points")
public class GamificationPointService extends RESTService {

	// instantiate the logger class
	private final L2pLogger logger = L2pLogger.getInstance(GamificationPointService.class.getName());
	/*
	 * Database configuration
	 */
	private String jdbcDriverClassName;
	private String jdbcLogin;
	private String jdbcPass;
	private String jdbcUrl;
	private String jdbcSchema;
	private DatabaseManager dbm;
	private PointDAO pointAccess;
	
	public GamificationPointService() {
		// read and set properties values
		// IF THE SERVICE CLASS NAME IS CHANGED, THE PROPERTIES FILE NAME NEED TO BE CHANGED TOO!
		setFieldValues();
		dbm = DatabaseManager.getInstance(jdbcDriverClassName, jdbcLogin, jdbcPass, jdbcUrl, jdbcSchema);
		this.pointAccess = new PointDAO();
	}
	  
		  /**
			 * Fetch configuration data from file system
			 * @param gameId game ID
			 * @return point service configuration as JSON Object
			 * @throws IOException IO Exception
			 */
			private JSONObject fetchConfigurationFromSystem(String gameId) throws IOException {
				String confPath = LocalFileManager.getBasedir()+"/"+gameId+"/conf.json";
				// RMI call without parameters
				File gameFolder = new File(LocalFileManager.getBasedir()+"/"+gameId);
				if(!gameFolder.exists()){
					if(gameFolder.mkdir()){
						System.out.println("New directory "+ gameId +" is created!");
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
				String confJSONByte = new String(LocalFileManager.getFile(gameId+"/conf.json"));
				return (JSONObject) JSONValue.parse(confJSONByte);
			}
			
			/**
			 * Function to store configuration
			 * @param gameId gameId
			 * @param obj JSON object
			 * @throws IOException 
			 */
			private void storeConfigurationToSystem(String gameId, JSONObject obj) throws IOException{
				String confPath = LocalFileManager.getBasedir()+"/"+gameId+"/conf.json";
					// RMI call without parameters
				File gameFolder = new File(LocalFileManager.getBasedir()+"/"+gameId);
				if(!gameFolder.exists()){
					if(gameFolder.mkdir()){
						System.out.println("New directory "+ gameId +" is created!");
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
				LocalFileManager.writeFile(LocalFileManager.getBasedir()+"/"+gameId+"/conf.json", obj.toJSONString());
			}
			
//			/**
//			 * Function to store configuration
//			 * @param gameId gameId
//			 * @throws IOException 
//			 */
//			private boolean cleanStorage(String gameId){
//					// RMI call without parameters
//				File gameFolder = new File(LocalFileManager.getBasedir()+"/"+gameId);
//				
//				try {
//					recursiveDelete(gameFolder);
//					return true;
//				} catch (IOException e) {
//					e.printStackTrace();
//					return false;
//				}
		//
//		    }
			/**
			 * Function to delete a folder in the file system
			 * @param gameFolder folder path
			 * @throws IOException IO exception
			 */
			private void recursiveDelete(File gameFolder) throws IOException{
				if(gameFolder.isDirectory()){
		    		//directory is empty, then delete it
		    		if(gameFolder.list().length==0){
		    			gameFolder.delete();
		    		   System.out.println("Directory is deleted : " 
		                                                 + gameFolder.getAbsolutePath());
		    		}else{
		    			
		    		   //list all the directory contents
		        	   String files[] = gameFolder.list();
		     
		        	   for (String temp : files) {
		        	      //construct the file structure
		        	      File fileDelete = new File(gameFolder, temp);
		        		 
		        	      //recursive delete
		        	      recursiveDelete(fileDelete);
		        	   }
		        		
		        	   //check the directory again, if empty then delete it
		        	   if(gameFolder.list().length==0){
		        		   gameFolder.delete();
		        	     System.out.println("Directory is deleted : " + gameFolder.getAbsolutePath());
		        	   }
		    		}
		    	}else{
		    		//if file, then delete it
		    		gameFolder.delete();
		    		System.out.println("File is deleted : " + gameFolder.getAbsolutePath());
		    	}
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
			
			
			
			// //////////////////////////////////////////////////////////////////////////////////////
			// Service methods.
			// //////////////////////////////////////////////////////////////////////////////////////
			
			// //////////////////////////////////////////////////////////////////////////////////////
			// POINT PART --------------------------------------
			// //////////////////////////////////////////////////////////////////////////////////////
			/**
			 * Change point unit name
			 * @param gameId gameId
			 * @param unitName point unit name
			 * @return HTTP Response Returned as JSON object
			 */
			@PUT
			@Path("/{gameId}/name/{unitName}")
			@Produces(MediaType.APPLICATION_JSON)
			@ApiResponses(value = {
					@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Unit name changed"),
					@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal Error"),
					@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized")})
			@ApiOperation(value = "changeUnitName", 
						  notes = "Change unit name"
						  )
			public Response changeUnitName(
					@ApiParam(value = "Game ID to return")@PathParam("gameId") String gameId,
					@ApiParam(value = "Point unit name")@PathParam("unitName") String unitName)
			{
				
				// Request log
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_99, "PUT " + "gamification/points/"+gameId+"/name/"+unitName, true);
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
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_18, ""+randomLong, true);
					
					if(!pointAccess.isGameIdExist(conn,gameId)){
						objResponse.put("message", "Cannot update point unit name. Game not found");
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					}
					if(unitName != null){
						JSONObject objRetrieve;
						try {
							objRetrieve = fetchConfigurationFromSystem(gameId);
							objRetrieve.put("pointUnitName", unitName);
							storeConfigurationToSystem(gameId, objRetrieve);
							logger.info(objRetrieve.toJSONString());
							objResponse.put("message", "Unit name "+unitName+" is updated");
							Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_19, ""+randomLong, true);
							Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_28, ""+name, true);
							Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_29, ""+gameId, true);
							return Response.status(HttpURLConnection.HTTP_OK).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
						} catch (IOException e) {
							e.printStackTrace();
							// return HTTP Response on error
							objResponse.put("message", "Cannot update point unit name. IO Exception. " + e.getMessage());
							Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
							return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
						}
					}
					else{
						// Unit name is null
						objResponse.put("message", "Cannot update point unit name. Unit Name cannot be null. " );
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();

					}
				} catch (SQLException e1) {
					e1.printStackTrace();
					objResponse.put("message", "Cannot update point unit name. Cannot check whether game ID exist or not. Database error. " + e1.getMessage());
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
			 * Fetch point unit name
			 * @param gameId gameId
			 * @return HTTP Response Returned as JSON object
			 */
			@GET
			@Path("/{gameId}/name")
			@Produces(MediaType.APPLICATION_JSON)
			@ApiResponses(value = {
					@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Unit name"),
					@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal Error"),
					@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized")})
			@ApiOperation(value = "getUnitName", 
						  notes = "Get unit name"
						  )
			public Response getUnitName(
					@ApiParam(value = "Game ID to return")@PathParam("gameId") String gameId)
			{
				
				// Request log
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_99, "GET " + "gamification/points/"+gameId+"/name", true);
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
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_16, ""+randomLong, true);
					
					if(!pointAccess.isGameIdExist(conn,gameId)){
						objResponse.put("message", "Cannot get point unit name. Game not found");
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					}
					JSONObject objRetrieve;
					try {
						objRetrieve = fetchConfigurationFromSystem(gameId);
						String pointUnitName = (String) objRetrieve.get("pointUnitName");
						if(pointUnitName==null){
							objRetrieve.put("pointUnitName", "");
						}
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_17, ""+randomLong, true);
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_26, ""+name, true);
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_27, ""+gameId, true);
						return Response.status(HttpURLConnection.HTTP_OK).entity(objRetrieve.toJSONString()).type(MediaType.APPLICATION_JSON).build();
				

					} catch (IOException e) {
						e.printStackTrace();
						
						// return HTTP Response on error
						objResponse.put("message", "Cannot get point unit name. IO Exception. " + e.getMessage());
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();

					}
				} catch (SQLException e1) {
					e1.printStackTrace();
					objResponse.put("message", "Cannot get point unit name. Cannot check whether game ID exist or not. Database error. " + e1.getMessage());
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
			
			public String getUnitNameRMI(String gameId, String memberId){
				JSONObject objRetrieve;
				try {
					objRetrieve = fetchConfigurationFromSystem(gameId);
					String pointUnitName = (String) objRetrieve.get("pointUnitName");
					if(pointUnitName==null){
						pointUnitName = "";
					}
						
					return pointUnitName;	

				} catch (IOException e) {
					e.printStackTrace();
					return "";
				}
			}

			/**
			 * RMI function to delete directory of an game in the point service file system
			 * @param gameId gameId
			 * @return 1 if the directory is deleted
			 */
			public Integer cleanStorageRMI(String gameId) {
				File gameFolder = new File(LocalFileManager.getBasedir()+"/"+gameId);
				
				try {
					recursiveDelete(gameFolder);
					return 1;
				} catch (IOException e) {
					e.printStackTrace();
					return 0;
				}
			}
	  //}

}

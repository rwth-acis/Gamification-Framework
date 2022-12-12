package i5.las2peer.services.gamificationBadgeService;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;
import java.util.Base64;

import javax.imageio.ImageIO;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.MediaType;


import i5.las2peer.services.gamification.commons.database.DatabaseManager;
import org.apache.commons.fileupload.MultipartStream.MalformedStreamException;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Mode;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import i5.las2peer.logging.L2pLogger;
import i5.las2peer.p2p.TimeoutException;
import i5.las2peer.restMapper.annotations.ServicePath;
import i5.las2peer.restMapper.RESTService;
import i5.las2peer.api.ManualDeployment;
import i5.las2peer.api.Context;
import i5.las2peer.api.logging.MonitoringEvent;
import i5.las2peer.api.security.Agent;
import i5.las2peer.api.security.AgentNotFoundException;
import i5.las2peer.api.security.AnonymousAgent;
import i5.las2peer.api.security.UserAgent;
import i5.las2peer.api.execution.InternalServiceException;
import i5.las2peer.services.gamificationBadgeService.database.BadgeDAO;
import i5.las2peer.services.gamificationBadgeService.database.BadgeModel;
import i5.las2peer.services.gamificationBadgeService.helper.LocalFileManager;
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
 * Gamification Badge Service
 * 
 * This is Gamification Badge service to manage badge elements in Gamification Framework
 * It uses the LAS2peer Web-Connector for RESTful access to it.
 * 
 * Note:
 * If you plan on using Swagger you should adapt the information below
 * in the ApiInfo annotation to suit your project.
 * If you do not intend to provide a Swagger documentation of your service API,
 * the entire ApiInfo annotation should be removed.
 * 
 */


@Api( value = "/gamification/badges", authorizations = {
		@Authorization(value = "badges_auth",
		scopes = {
			@AuthorizationScope(scope = "write:badges", description = "modify badges in your game"),
			@AuthorizationScope(scope = "read:badges", description = "read your badges in game")
				  })
}, tags = "badges")
@SwaggerDefinition(
		info = @Info(
				title = "Gamification Badges Service",
				version = "0.1",
				description = "Gamification Badges Service for Gamification Framework",
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
@ServicePath("/gamification/badges")
public class GamificationBadgeService extends RESTService {

	// instantiate the logger class
	private final L2pLogger logger = L2pLogger.getInstance(GamificationBadgeService.class.getName());
	/*
	 * Database configuration
	 */
	private String jdbcDriverClassName;
	private String jdbcLogin;
	private String jdbcPass;
	private String jdbcUrl;
	private String jdbcSchema;
	private DatabaseManager dbm;
	private BadgeDAO badgeAccess;

	
	// this header is not known to javax.ws.rs.core.HttpHeaders
	public static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";
	public static final String HEADER_CONTENT_TYPE = "Content-Type";
	// non HTTP standard headers
	public static final String HEADER_OWNERID = "ownerid";
	public static final String HEADER_CONTENT_DESCRIPTION = "Content-Description";

	public GamificationBadgeService() {
		// read and set properties values
		// IF THE SERVICE CLASS NAME IS CHANGED, THE PROPERTIES FILE NAME NEED TO BE CHANGED TOO!
		//super();
		setFieldValues();
		dbm = DatabaseManager.getInstance(jdbcDriverClassName, jdbcLogin, jdbcPass, jdbcUrl, jdbcSchema);
		this.badgeAccess = new BadgeDAO();
		
	}
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
		 * Function to resize image
		 * @param inputImageRaw input image as raw InputStream
		 * @return return resized image in byte array
		 * @throws IllegalArgumentException Illegal argument exception
		 * @throws IOException IO exception
		 * @throws NullPointerException null pointer exception
		 */
		private byte[] resizeImage(InputStream inputImageRaw) throws IllegalArgumentException, IOException, NullPointerException{
			System.out.println("resizing image");
			BufferedImage img = ImageIO.read(inputImageRaw);
			BufferedImage newImg = Scalr.resize(img,Mode.AUTOMATIC,200,200);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(newImg, "png", baos);
			baos.flush();
			byte[] output = baos.toByteArray();
			baos.close();
			return output;
			
		}
		
		/**
		 * Function to store badge image in storage
		 * @param gameId game id
		 * @param badgeId badge id
		 * @param filename file name
		 * @param filecontent file data
		 * @param mimeType mime type code
		 * @param description description of the badge image
		 * @return Response with the return image
		 * @throws IOException 
		 */
		private void storeBadgeDataToSystem(String gameId, String badgeid, String filename, byte[] filecontent, String mimeType, String description) throws AgentNotFoundException, InternalServiceException,
		InterruptedException, TimeoutException, IOException{
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
			LocalFileManager.writeByteArrayToFile(LocalFileManager.getBasedir()+"/"+gameId+"/"+badgeid, filecontent);

		}
		
		/**
		 * Function to return http unauthorized message
		 * @return HTTP Response returns JSON object
		 */
		private Response unauthorizedMessage(){
			JSONObject objResponse = new JSONObject();
			objResponse.put("message", "You are not authorized");
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, "Not Authorized");
			return Response.status(HttpURLConnection.HTTP_UNAUTHORIZED).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();

		}	

		
		// //////////////////////////////////////////////////////////////////////////////////////
		// Badge PART --------------------------------------
		// //////////////////////////////////////////////////////////////////////////////////////
		
		/**
		 * Post a new badge.
		 *
		 * @param gameId Game ID obtained from Gamification Game Service
		 * @param badgeid Badge ID - String (20 chars)
		 * @param badgename Badge Name - String (20 chars)
		 * @param badgedescription Badge Description - String (50 chars)
		 * @param badgeusenotificationStr Badge Notification Boolean - Boolean - Option whether use notification or not. Must be null for 'false' and any other value for 'true'
		 * @param badgenotificationmessage Badge Notification - String
		 * @param badgeImageData Badge Image - InputStream containing the data
		 * @param badgeImagePart Badge Image - detailed form part information (file name, media type)
		 * @param devFlag (no official API parameter!) used only in unit tests
		 *
		 * @return HTTP Response returned as JSON object
		 */
		@POST
		@Path("/{gameId}")
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
		public Response createNewBadge(
				@ApiParam(value = "Game ID to store a new badge", required = true) @PathParam("gameId") String gameId,
				@ApiParam(value = "Badge ID - String (20 chars)", required = true) @FormDataParam("badgeid") String badgeid,
				@ApiParam(value = "Badge Name - String (20 chars)", required = true) @FormDataParam("badgename") String badgename,
				@ApiParam(value = "Badge Description - String (50 chars)") @DefaultValue("") @FormDataParam("badgedesc") String badgedescription,
				@ApiParam(value = "Badge Notification Boolean - Boolean - Option whether use notification or not.  - Option whether use notification or not. NOTE: semantics are a little strange (because of backwards compatibility)! If the parameter is present, any value is considered as true. In order to set the value to value, you have to NOT send the parameter.")
					@FormDataParam("badgenotificationcheck") String badgeusenotificationStr,
				@ApiParam(value = "Badge Notification - String") @DefaultValue("" )@FormDataParam("badgenotificationmessage") String badgenotificationmessage,
				@ApiParam(value = "Badge Image - Image byte", required = true) @FormDataParam("badgeimageinput") InputStream badgeImageData,
				@ApiParam(value = "Badge Image - Image byte", required = true) @FormDataParam("badgeimageinput") FormDataBodyPart badgeImagePart,
				@FormDataParam("dev") String devFlag // only for unit tests
		)  {
			/*
			 * TODO Consider breaking change and using default 'boolean' semantics (param needs to be string 'true' for true value)
			 */
			boolean badgeusenotification = legacyBooleanParameterFromFormParam(badgeusenotificationStr);
			
			// Request log
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_99, "POST " + "gamification/badges/"+gameId, true);
			long randomLong = new Random().nextLong(); //To be able to match 
			
			// parse given multipart form data
			JSONObject objResponse = new JSONObject();
			String filename = null;
			byte[] filecontent = null;
			String mimeType = null;
			// Badge ID for the filesystem is appended with game id to make sure it is unique

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
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_14, ""+randomLong, true);
				
				try {
					if(!badgeAccess.isGameIdExist(conn,gameId)){
						logger.info("Game not found >> ");
						objResponse.put("message", "Cannot create badge. Game not found");
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					}
				} catch (SQLException e1) {
					e1.printStackTrace();
					logger.info("Cannot check whether game ID exist or not. Database error. >> " + e1.getMessage());
					objResponse.put("message", "Cannot create badge. Cannot check whether game ID exist or not. Database error. " + e1.getMessage());
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
				}

				if (badgeid != null) {
					if(badgeAccess.isBadgeIdExist(conn,gameId, badgeid)){
						// Badge id already exist
						logger.info("Failed to add the badge. Badge ID already exist!");
						objResponse.put("message", "Cannot create badge. Failed to add the badge. Badge ID already exist!.");
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();

					}

					if (badgeImagePart != null) {
						// these data belong to the file input form element
							filename = badgeImagePart.getFormDataContentDisposition().getFileName();
							
							// in unit test, resize image will turn the image into null BufferedImage
							// but, it works in web browser
							if (devFlag != null) {
								try {
									filecontent = badgeImageData.readAllBytes();
								} catch (IOException e) {
									logger.warning("Failed to read image input: " + e.getMessage());
									logger.printStackTrace(e);
									objResponse.put("message", "Cannot create badge. Failed to read image data. Failed to upload " + badgeid);
									Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
									return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
								}
							} else {
								filecontent = resizeImage(badgeImageData);
							}
							mimeType = badgeImagePart.getMediaType().toString();
							logger.info("upload request (" + filename + ") of mime type '" + mimeType + "' with content length "
									+ filecontent.length);
					} else {
						logger.info("No badge image");
						objResponse.put("message", "Cannot create badge. Missing badge image. Failed to upload " + badgeid);
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					}
					
					
					try {

						storeBadgeDataToSystem(gameId, badgeid, filename, filecontent,mimeType , badgedescription);
						BadgeModel badge = new BadgeModel(badgeid, badgename, badgedescription, badgeusenotification, badgenotificationmessage);
						
						try{
							badgeAccess.addNewBadge(conn,gameId, badge);
							objResponse.put("message", "Badge upload success (" + badgeid +")");
							Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_15, ""+randomLong, true);
							Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_24, ""+name, true);
							Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_25, ""+gameId, true);
							return Response.status(HttpURLConnection.HTTP_CREATED).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();

						} catch (SQLException e) {
							e.printStackTrace();
							objResponse.put("message", "Cannot create badge. Failed to upload " + badgeid + ". " + e.getMessage());
							Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
							return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
						}
						
					} catch (AgentNotFoundException | InternalServiceException | 
							InterruptedException | TimeoutException e) {
						e.printStackTrace();
						objResponse.put("message", "Cannot create badge. Failed to upload " + badgeid + ". " + e.getMessage());
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();

					}
				}
				else{
					logger.info("Badge ID cannot be null");
					objResponse.put("message", "Cannot create badge. Badge ID cannot be null!");
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
				}
				
				
			} catch (MalformedStreamException e) {
				// the stream failed to follow required syntax
				objResponse.put("message", "Cannot create badge. Failed to upload " + badgeid + ". " + e.getMessage());
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();

			} catch (IOException e) {
				// a read or write error occurred
				objResponse.put("message", "Cannot create badge. Failed to upload " + badgeid + ". " + e.getMessage());
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();

			} catch (SQLException e) {
				e.printStackTrace();
				objResponse.put("message", "Cannot create badge. Failed to upload " + badgeid + ". " + e.getMessage());
				Context.getCurrent().monitorEvent(this, MonitoringEvent.AGENT_UPLOAD_FAILED, (String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();

			}
			catch (NullPointerException e){
				e.printStackTrace();
				objResponse.put("message", "Cannot create badge. Failed to upload " + badgeid + ". " + e.getMessage());
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
		 * Update a badge.
		 *
		 * @param gameId Game ID obtained from Gamification Game Service
		 * @param badgeId badge id
		 * @param badgename Badge Name - String (20 chars)
		 * @param badgedescription Badge Description - String (50 chars)
		 * @param badgeusenotificationStr Badge Notification Boolean - Boolean - Option whether use notification or not. Must be null for 'false' and any other value for 'true'
		 * @param badgenotificationmessage Badge Notification - String
		 * @param badgeImageData Badge Image - InputStream containing the data
		 * @param badgeImagePart Badge Image - detailed form part information (file name, media type)
		 * @param devFlag (no official API parameter!) used only in unit tests
		 *
		 * @return HTTP Response returned as JSON object
		 */
		@PUT
		@Path("/{gameId}/{badgeId}")
		@Produces(MediaType.APPLICATION_JSON)
		@ApiResponses(value = {
				@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Badge Updated"),
				@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Error occured"),
				@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad request"),
				@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized")
		})
		@ApiOperation(value = "Update a badge",
					 notes = "A method to update a badge with details (badge ID, badge name, badge description, and badge image")
		public Response updateBadge(
				@ApiParam(value = "Game ID to store a new badge", required = true) @PathParam("gameId") String gameId,
				@PathParam("badgeId") String badgeId,
				@ApiParam(value = "Badge Name - String (20 chars)", required = true) @FormDataParam("badgename") String badgename,
				@ApiParam(value = "Badge Description - String (50 chars)") @FormDataParam("badgedesc") String badgedescription,
				@ApiParam(value = "Badge Notification Boolean - Boolean - Option whether use notification or not.  - Option whether use notification or not. NOTE: semantics are a little strange (because of backwards compatibility)! If the parameter is present, any value is considered as true. In order to set the value to value, you have to NOT send the parameter.")
				@FormDataParam("badgenotificationcheck") String badgeusenotificationStr,
				@ApiParam(value = "Badge Notification - String") @FormDataParam("badgenotificationmessage") String badgenotificationmessage,
				@ApiParam(value = "Badge Image - Image byte", required = true) @FormDataParam("badgeimageinput") InputStream badgeImageData,
				@ApiParam(value = "Badge Image - Image byte", required = true) @FormDataParam("badgeimageinput") FormDataBodyPart badgeImagePart,
				@FormDataParam("dev") String devFlag // only for unit tests
		)  {
			/*
			 * TODO Consider breaking change and using default 'boolean' semantics (param needs to be string 'true' for true value)
			 */
			boolean badgeusenotification = legacyBooleanParameterFromFormParam(badgeusenotificationStr);
			
			// Request log
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_99, "PUT " + "gamification/badges/"+gameId+"/"+badgeId, true);
			long randomLong = new Random().nextLong(); //To be able to match 
			
			// parse given multipart form data
			JSONObject objResponse = new JSONObject();
			String filename = null;
			byte[] filecontent = null;
			String mimeType = null;
			// Badge ID for the filesystem is appended with game id to make sure it is unique

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
				
				try {
					if(!badgeAccess.isGameIdExist(conn,gameId)){
						objResponse.put("message", "Cannot update badge. Game not found");
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					}
				} catch (SQLException e1) {
					e1.printStackTrace();
					objResponse.put("message", "Cannot update badge. Cannot check whether game ID exist or not. Database error. " + e1.getMessage());
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
				}
				
				if (badgeId == null) {
					objResponse.put("message", "Cannot update badge. Badge ID cannot be null");
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
				}
				BadgeModel currentBadge = badgeAccess.getBadgeWithId(conn,gameId, badgeId);
				if(currentBadge == null){
					// currentBadge is null
					objResponse.put("message", "Cannot update badge. Badge not found");
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();

				}

				if(badgename != null){
					currentBadge.setName(badgename);
				}
				if(badgedescription!=null){
					currentBadge.setDescription(badgedescription);
				}

				if (badgeImagePart != null) {
					// these data belong to the file input form element
					filename = badgeImagePart.getFormDataContentDisposition().getFileName();
					mimeType = badgeImagePart.getMediaType().toString();

					try {
						// in unit test, resize image will turn the image into null BufferedImage
						// but, it works in web browser
						if (devFlag != null) {
							try {
								filecontent = badgeImageData.readAllBytes();
							} catch (IOException e) {
								logger.warning("Failed to read image input: " + e.getMessage());
								logger.printStackTrace(e);
								objResponse.put("message", "Cannot create badge. Failed to read image data. Failed to upload " + badgeId);
								Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
								return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
							}
						} else{
							filecontent = resizeImage(badgeImageData);
						}
						storeBadgeDataToSystem(gameId, badgeId, filename, filecontent,mimeType , badgedescription);
						logger.info("upload request (" + filename + ") of mime type '" + mimeType + "' with content length "
								+ filecontent.length);

					} catch (AgentNotFoundException | InternalServiceException |
							InterruptedException | TimeoutException e) {
						e.printStackTrace();
						objResponse.put("message", "Cannot update badge. Failed to upload " + badgeId + ". " + e.getMessage() );
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					}
					catch (IllegalArgumentException e){
						objResponse.put("message", "Cannot update badge. Badge image is not updated. " + e.getMessage());
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();

					}
				}

				currentBadge.useNotification(badgeusenotification);

				if(badgenotificationmessage != null){
					currentBadge.setNotificationMessage(badgenotificationmessage);
				}
				
				try{
					badgeAccess.updateBadge(conn,gameId, currentBadge);
					objResponse.put("message", "Badge updated");
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_19, ""+randomLong, true);
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_28, ""+name, true);
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_29, ""+gameId, true);
					return Response.status(HttpURLConnection.HTTP_OK).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
				} catch (SQLException e) {
					e.printStackTrace();
					objResponse.put("message", "Cannot update badge. Database Error. " + e.getMessage());
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();

				}
				
			} catch (MalformedStreamException e) {
				// the stream failed to follow required syntax
				objResponse.put("message", "Cannot update badge. Failed to upload " + badgeId + ". "+e.getMessage());
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
			} catch (IOException e) {
				// a read or write error occurred
				objResponse.put("message", "Cannot update badge. Failed to upload " + badgeId + "." + e.getMessage());
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
			} catch (SQLException e1) {
				e1.printStackTrace();
				objResponse.put("message", "Cannot update badge. Database Error. " + e1.getMessage());
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
		 * Get a badge data with specific ID from database
		 * @param gameId Game ID obtained from Gamification Game Service
		 * @param badgeId badge id
		 * @return HTTP Response returned as JSON object
		 */
		@GET
		@Path("/{gameId}/{badgeId}")
		@Produces(MediaType.APPLICATION_JSON)
		@ApiResponses(value = {
				@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Found a badges"),
				@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal Error"),
				@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized")})
		@ApiOperation(value = "Find point for specific Game ID and badge ID", 
					  notes = "Returns a badge",
					  response = BadgeModel.class,
					  responseContainer = "List",
					  authorizations = @Authorization(value = "api_key")
					  )
		public Response getBadgeWithId(
				@ApiParam(value = "Game ID")@PathParam("gameId") String gameId,
				@ApiParam(value = "Badge ID")@PathParam("badgeId") String badgeId)
		{
			
			// Request log
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_99, "GET " + "gamification/badges/"+gameId+"/"+badgeId, true);
			long randomLong = new Random().nextLong(); //To be able to match 
			
			BadgeModel badge = null;
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
					if(!badgeAccess.isGameIdExist(conn,gameId)){
						objResponse.put("message", "Cannot get badge. Game not found");
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					}
				} catch (SQLException e1) {
					e1.printStackTrace();
					objResponse.put("message", "Cannot get badge. Cannot check whether game ID exist or not. Database error. " + e1.getMessage());
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
				}
				badge = badgeAccess.getBadgeWithId(conn,gameId, badgeId);
				if(badge == null){
					objResponse.put("message", "Cannot get badge. Badge model is null.");
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
				}
				ObjectMapper objectMapper = new ObjectMapper();
		    	//Set pretty printing of json
		    	objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		    	
		    	String badgeString = objectMapper.writeValueAsString(badge);
		    	Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_17, ""+randomLong, true);
		    	Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_26, ""+name, true);
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_27, ""+gameId, true);
				return Response.status(HttpURLConnection.HTTP_OK).entity(badgeString).type(MediaType.APPLICATION_JSON).build();

			} catch (JsonProcessingException e) {
				e.printStackTrace();
				objResponse.put("message", "Cannot get badge. Cannot process JSON." + e.getMessage());
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(e.getMessage()).type(MediaType.APPLICATION_JSON).build();

			} catch (SQLException e) {
				e.printStackTrace();
				objResponse.put("message", "Cannot get badge. Database Error. " + e.getMessage());
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
		 * Delete a badge data with specified ID
		 * @param gameId Game ID obtained from Gamification Game Service
		 * @param badgeId badge id
		 * @return HTTP Response returned as JSON object
		 */
		@DELETE
		@Path("/{gameId}/{badgeId}")
		@Produces(MediaType.APPLICATION_JSON)
		@ApiResponses(value = {
				@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Badge Delete Success"),
				@ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Badges not found"),
				@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad Request"),
		})
		@ApiOperation(value = "",
					  notes = "delete a badge")
		public Response deleteBadge(@PathParam("gameId") String gameId,
									 @PathParam("badgeId") String badgeId)
		{
			
			// Request log
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_99, "DELETE " + "gamification/badges/"+gameId+"/"+badgeId, true);
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
					if(!badgeAccess.isGameIdExist(conn,gameId)){
						objResponse.put("message", "Cannot delete badge. Game not found");
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					}
				} catch (SQLException e1) {
					e1.printStackTrace();
					objResponse.put("message", "Cannot delete badge. Cannot check whether game ID exist or not. Database error. " + e1.getMessage());
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
				}
				if(!badgeAccess.isBadgeIdExist(conn,gameId, badgeId)){
					logger.info("Badge not found >> ");
					objResponse.put("message", "Cannot delete badge. Badge not found");
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
				}
				badgeAccess.deleteBadge(conn,gameId, badgeId);
				if(!LocalFileManager.deleteFile(LocalFileManager.getBasedir()+"/"+gameId+"/"+badgeId)){
					
					logger.info("Delete File Failed >> ");
					System.out.println("WHoopsy nothing to delete");
					objResponse.put("message", "Cannot delete badge. Delete File Failed");
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
				//	return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();

				}
				objResponse.put("message", "File Deleted");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_21, ""+randomLong, true);
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_30, ""+name, true);
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_31, ""+gameId, true);
				return Response.status(HttpURLConnection.HTTP_OK).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();

			} catch (SQLException e) {
				
				e.printStackTrace();
				objResponse.put("message", "Cannot delete badge. Cannot delete file. " + e.getMessage());
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_OK).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
			}		 // always close connections
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
		 * Get a list of badges from database
		 * @param gameId Game ID obtained from Gamification Game Service
		 * @param currentPage current cursor page
		 * @param windowSize size of fetched data (use -1 to fetch all data)
		 * @param searchPhrase search word
		 * @return HTTP Response returned as JSON object
		 */
		@GET
		@Path("/{gameId}")
		@Produces(MediaType.APPLICATION_JSON)
		@ApiResponses(
				value = {
				@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal Error"),
				@ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Badge not found"),
				@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized")}
		)
		@ApiOperation(
				value = "Find badges for specific Game ID", 
				notes = "Returns a list of badges",
				response = BadgeModel.class,
				responseContainer = "List",
				authorizations = @Authorization(value = "api_key")
		)
		public Response getBadgeList(
				@ApiParam(value = "Game ID that contains badges", required = true)@PathParam("gameId") String gameId,
				@ApiParam(value = "Page number cursor for retrieving data")@QueryParam("current") int currentPage,
				@ApiParam(value = "Number of data size per fetch")@QueryParam("rowCount") int windowSize,
				@ApiParam(value = "Search phrase parameter")@QueryParam("searchPhrase") String searchPhrase)
		{
			
			// Request log
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_99, "GET " + "gamification/badges/"+gameId, true);
			long randomLong = new Random().nextLong(); //To be able to match 
			
			List<BadgeModel> badges = null;
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
					if(!badgeAccess.isGameIdExist(conn,gameId)){
						objResponse.put("message", "Cannot get badges. Game not found");
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					}
				} catch (SQLException e1) {
					e1.printStackTrace();
					logger.info("Cannot check whether game ID exist or not. Database error. >> " + e1.getMessage());
					objResponse.put("message", "Cannot get badges. Cannot check whether game ID exist or not. Database error. " + e1.getMessage());
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
				}
				// Check game id exist or not
				
				int offset = (currentPage - 1) * windowSize;
				
				int totalNum = badgeAccess.getNumberOfBadges(conn,gameId);
				
				if(windowSize == -1){
					offset = 0;
					windowSize = totalNum;
				}
				
				badges = badgeAccess.getAllBadges(conn,gameId);
				
				
				ObjectMapper objectMapper = new ObjectMapper();
		    	//Set pretty printing of json
		    	objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		    	
		    	
				for(int i=0; i < badges.size();i++){
					BadgeModel badge = badges.get(i);
					System.out.println("searching for badge image" );
					byte[] filecontent = getBadgeImageMethod(gameId, badge.getId());
					
					try{
						byte[] encode = Base64.getEncoder().encode(filecontent);
						String result = new String(encode);
							badge.setBase64(result);
							badges.set(i, badge);
							System.out.println("badge img found" );
					} catch (Exception e){
						e.printStackTrace();
					}
				}
				String badgeString = objectMapper.writeValueAsString(badges);
				JSONArray badgeArray = (JSONArray) JSONValue.parse(badgeString);

				objResponse.put("current", currentPage);
				objResponse.put("rowCount", windowSize);
				objResponse.put("rows", badgeArray);
				objResponse.put("total", totalNum);
				logger.info(objResponse.toJSONString());

				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_47, ""+randomLong, true);
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_48, ""+name, true);
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_49, ""+gameId, true);
				return Response.status(HttpURLConnection.HTTP_OK).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();

			} catch (SQLException e) {
				e.printStackTrace();
				objResponse.put("message", "Cannot get badges. Internal Error. Database connection failed. " + e.getMessage());
				
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
			} catch (JsonProcessingException e) {
				e.printStackTrace();
				objResponse.put("message", "Cannot get badges. Cannot connect to database. " + e.getMessage() );
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
		 * Fetch a badge image with specified ID
		 * @param gameId Game ID obtained from Gamification Game Service
		 * @param badgeId badge id
		 * @return HTTP Response and return the image
		 */
		@GET
		@Path("/{gameId}/{badgeId}/img")
		@Produces(MediaType.APPLICATION_OCTET_STREAM)
		@ApiResponses(value = {
				@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Badges Entry"),
				@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Cannot found image")
		})
		@ApiOperation(value = "",
					  notes = "list of stored badges")
		public Response getBadgeImage(@PathParam("gameId") String gameId,
									 @PathParam("badgeId") String badgeId)
		{
			
			// Request log
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_99, "GET " + "gamification/badges/"+gameId+"/"+badgeId+"/img", true);
			
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
				Context.getCurrent().monitorEvent(this, MonitoringEvent.AGENT_GET_STARTED, "Get Badge Image");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.ARTIFACT_FETCH_STARTED,"Get Badge Image");
				
				try {
					if(!badgeAccess.isGameIdExist(conn,gameId)){
						objResponse.put("message", "Cannot get badge image. Game not found");
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					}
				} catch (SQLException e1) {
					e1.printStackTrace();
					objResponse.put("message", "Cannot get badge image. Cannot check whether game ID exist or not. Database error. " + e1.getMessage());
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
				}
				if(!badgeAccess.isBadgeIdExist(conn,gameId, badgeId)){
					objResponse.put("message", "Cannot get badge image. Badge not found");
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
				}
				byte[] filecontent = getBadgeImageMethod(gameId, badgeId);

				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_25, "Badge image fetched : " + badgeId + " : " + gameId + " : " + name, true);
				Context.getCurrent().monitorEvent(this, MonitoringEvent.ARTIFACT_RECEIVED, "Badge image fetched : " + badgeId + " : " + gameId + " : " + name);
				return Response.status(HttpURLConnection.HTTP_OK).entity(filecontent).type(MediaType.APPLICATION_OCTET_STREAM).build();
			} catch (SQLException e) {
				e.printStackTrace();
				objResponse.put("message", "Cannot get badge image. Database error. " + e.getMessage());
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
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
		
		//RMI
		/**
		 * RMI function to get the badge image
		 * @param gameId Game ID obtained from Gamification Game Service
		 * @param badgeId badge id
		 * @return badge image as byte array
		 */
		public byte[] getBadgeImageMethod(String gameId, String badgeId){
			byte[] filecontent = LocalFileManager.getFile(gameId+"/"+badgeId);
			return filecontent;
		}

				/**
		 * RMI function to get the badge image
		 * @param gameId Game ID obtained from Gamification Game Service
		 * @param badgeId badge id
		 * @return badge image as byte array
		 */
		public String getBadgeImageMethodRMI(String gameId, String badgeId){
			byte[] filecontent = LocalFileManager.getFile(gameId+"/"+badgeId);
			
			byte[] encode = Base64.getEncoder().encode(filecontent);
			String result = new String(encode);
			return result;
		}
		
		// RMI
		/**
		 * RMI function to get badge data detail with specific ID
		 * @param gameId game id
		 * @param badgeId badge id
		 * @return serialized JSON badge data
		 */
		public String getBadgeWithIdRMI(String gameId, String badgeId) {
			BadgeModel badge;
			Connection conn = null;

			try {
				conn = dbm.getConnection();
				badge = badgeAccess.getBadgeWithId(conn,gameId, badgeId);
				if(badge == null){
					return null;
				}
				ObjectMapper objectMapper = new ObjectMapper();
		    	//Set pretty printing of json
		    	objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		    	
		    	String badgeString = objectMapper.writeValueAsString(badge);
		    	return badgeString;
			} catch (SQLException e) {
				e.printStackTrace();
				return null;		
			} catch (JsonProcessingException e) {
				e.printStackTrace();
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
		
		/**
		 * RMI function to clean the directory in badge service file system
		 * @param gameId game id
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

		/**
		 * Legacy semantics of the 'achievementnotificationcheck' parameter are the following:
		 * - If parameter has any non-null value (even blank string) -> true
		 * - Otherwise -> false
		 *
		 * @param paramValue the raw string value of the form param (may be null if parameter is not set)
		 * @return
		 */
		private static boolean legacyBooleanParameterFromFormParam(String paramValue) {
			/*
			 * TODO Consider breaking change and using default 'boolean' semantics (param needs to be string 'true' for true value)
			 */
			return paramValue != null;
		}
	
}

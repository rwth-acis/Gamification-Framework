package i5.las2peer.services.gamificationBadgeService;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;

import javax.imageio.ImageIO;
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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.MediaType;



import org.apache.commons.fileupload.MultipartStream.MalformedStreamException;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Mode;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import i5.las2peer.restMapper.annotations.ServicePath;
import i5.las2peer.api.Context;
//import i5.las2peer.execution.L2pServiceException;
import i5.las2peer.logging.L2pLogger;
import i5.las2peer.api.logging.MonitoringEvent;
import i5.las2peer.api.security.AgentNotFoundException;
import i5.las2peer.p2p.TimeoutException;
import i5.las2peer.restMapper.RESTService;
//import i5.las2peer.restMapper.HttpResponse;
//import i5.las2peer.restMapper.MediaType;
//import i5.las2peer.restMapper.RESTMapper;
//import i5.las2peer.restMapper.annotations.ContentParam;
//import i5.las2peer.restMapper.annotations.Version;
//import i5.las2peer.restMapper.tools.ValidationResult;
//import i5.las2peer.restMapper.tools.XMLCheck;
//import i5.las2peer.security.L2pSecurityException;
import i5.las2peer.api.security.UserAgent;
import i5.las2peer.services.gamificationBadgeService.database.BadgeDAO;
import i5.las2peer.services.gamificationBadgeService.database.BadgeModel;
import i5.las2peer.services.gamificationBadgeService.database.DatabaseManager;
import i5.las2peer.services.gamificationBadgeService.helper.FormDataPart;
import i5.las2peer.services.gamificationBadgeService.helper.LocalFileManager;
import i5.las2peer.services.gamificationBadgeService.helper.MultipartHelper;
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

// TODO Describe your own service
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
// TODO Adjust the following configuration
@Path("/gamification/badges")
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
//@ServicePath("service")
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
		dbm = new DatabaseManager(jdbcDriverClassName, jdbcLogin, jdbcPass, jdbcUrl, jdbcSchema);
		this.badgeAccess = new BadgeDAO();
		
	}
	
	 @Override
	  protected void initResources() {
	    //getResourceConfig().register(Resource.class);
		 System.out.println("jojojoj");
	  }


//	@Path("/") // this is the root resource
//	  public static class Resource {
//	    // put here all your service methods
//		
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
		 * @param inputImageRaw input image in byte array
		 * @return return resized image in byte array
		 * @throws IllegalArgumentException Illegal argument exception
		 * @throws IOException IO exception
		 * @throws NUllPointerException null pointer exception
		 */
		private byte[] resizeImage(byte[] inputImageRaw) throws IllegalArgumentException, IOException, NullPointerException{

			BufferedImage img = ImageIO.read(new ByteArrayInputStream(inputImageRaw));
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
		 * @param gameId game id
		 * @param badgeId badge id
		 * @param filename file name
		 * @param filecontent file data
		 * @param mimeType mime type code
		 * @param description description of the badge image
		 * @return HttpResponse with the return image
		 * @throws IOException 
		 */
		private void storeBadgeDataToSystem(String gameId, String badgeid, String filename, byte[] filecontent, String mimeType, String description) throws AgentNotFoundException, //L2pServiceException, //L2pSecurityException,
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
			L2pLogger.logEvent(this, Event.SERVICE_ERROR, "Not Authorized");
			return Response.status(HttpURLConnection.HTTP_UNAUTHORIZED).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
			//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_UNAUTHORIZED);

		}	

		
		// //////////////////////////////////////////////////////////////////////////////////////
		// Badge PART --------------------------------------
		// //////////////////////////////////////////////////////////////////////////////////////
		
		// TODO Basic single CRUD ---------------------------------
		
		/**
		 * Post a new badge.
		 * Name attribute for form data : 
		 * <ul>
		 * 	<li>badgeid - Badge ID - String (20 chars)
		 *  <li>badgeimageinput - Badge Image - Image byte
		 * 	<li>badgename - Badge Name - String (20 chars)
		 *  <li>badgedesc - Badge Description - String (50 chars)
		 *  <li>badgenotificationcheck - Badge Notification Boolean - Boolean - Option whether use notification or not
		 *  <li>badgenotificationmessage - Badnge Notification - String
		 * </ul>
		 * @param gameId Game ID obtained from Gamification Game Service
		 * @param formData Form data with multipart/form-data type
		 * @param contentType Content type (implicitly sent in header)
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
				@ApiParam(value = "Content-type in header", required = true)@HeaderParam(value = HttpHeaders.CONTENT_TYPE) String contentType, 
				@ApiParam(value = "Badge detail in multiple/form-data type", required = true) byte[] formData)  {
			
			// Request log
			L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_99,Context.getCurrent().getMainAgent(), "POST " + "gamification/badges/"+gameId);
			long randomLong = new Random().nextLong(); //To be able to match 
			
			// parse given multipart form data
			JSONObject objResponse = new JSONObject();
			String filename = null;
			byte[] filecontent = null;
			String mimeType = null;
			String badgeid = null;
			// Badge ID for the filesystem is appended with game id to make sure it is unique
			String badgename = null;
			String badgedescription = null;
			boolean badgeusenotification = false;
			String badgenotificationmessage = null;
			Connection conn = null;

			
			UserAgent userAgent = (UserAgent) Context.getCurrent().getMainAgent();
			String name = userAgent.getLoginName();
			if(name.equals("anonymous")){
				return unauthorizedMessage();
			}
			try {
				conn = dbm.getConnection();
				L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_14,Context.getCurrent().getMainAgent(), ""+randomLong);
				
				try {
					if(!badgeAccess.isGameIdExist(conn,gameId)){
						logger.info("Game not found >> ");
						objResponse.put("message", "Cannot create badge. Game not found");
						L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
						//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
					}
				} catch (SQLException e1) {
					e1.printStackTrace();
					logger.info("Cannot check whether game ID exist or not. Database error. >> " + e1.getMessage());
					objResponse.put("message", "Cannot create badge. Cannot check whether game ID exist or not. Database error. " + e1.getMessage());
					L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
				}
				
				Map<String, FormDataPart> parts = MultipartHelper.getParts(formData, contentType);
				FormDataPart partBadgeID = parts.get("badgeid");
				if (partBadgeID != null) {
					// these data belong to the (optional) file id text input form element
					badgeid = partBadgeID.getContent();
					
					if(badgeAccess.isBadgeIdExist(conn,gameId, badgeid)){
						// Badge id already exist
						logger.info("Failed to add the badge. Badge ID already exist!");
						objResponse.put("message", "Cannot create badge. Failed to add the badge. Badge ID already exist!.");
						L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
						//return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_INTERNAL_ERROR);

					}
					FormDataPart partFilecontent = parts.get("badgeimageinput");
					if (partFilecontent != null) {
						//System.out.println(partFilecontent.getContent());
						// these data belong to the file input form element
							filename = partFilecontent.getHeader(HEADER_CONTENT_DISPOSITION).getParameter("filename");
							byte[] filecontentbefore = partFilecontent.getContentRaw();
//									 validate input
							if (filecontentbefore == null) {
								logger.info("File content null");
								objResponse.put("message", "Cannot create badge. File content null. Failed to upload " + badgeid);
								L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
								return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
								//return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_BAD_REQUEST);
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
					
					FormDataPart partNotificationCheck = parts.get("badgenotificationcheck");
					if (partNotificationCheck != null) {
						// checkbox is checked
						badgeusenotification = true;
					}else{
						badgeusenotification = false;
					}
					FormDataPart partNotificationMsg = parts.get("badgenotificationmessage");
					if (partNotificationMsg != null) {
						badgenotificationmessage = partNotificationMsg.getContent();
					}else{
						badgenotificationmessage = "";
					}
					
					
					try {

						storeBadgeDataToSystem(gameId, badgeid, filename, filecontent,mimeType , badgedescription);
						BadgeModel badge = new BadgeModel(badgeid, badgename, badgedescription, badgeusenotification, badgenotificationmessage);
						
						try{
							badgeAccess.addNewBadge(conn,gameId, badge);
							objResponse.put("message", "Badge upload success (" + badgeid +")");
							L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_15,Context.getCurrent().getMainAgent(), ""+randomLong);
							L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_24,Context.getCurrent().getMainAgent(), ""+name);
							L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_25,Context.getCurrent().getMainAgent(), ""+gameId);
							return Response.status(HttpURLConnection.HTTP_CREATED).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
							//return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_CREATED);

						} catch (SQLException e) {
							e.printStackTrace();
							objResponse.put("message", "Cannot create badge. Failed to upload " + badgeid + ". " + e.getMessage());
							L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
							return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
							//return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_INTERNAL_ERROR);
						}
						
					} catch (AgentNotFoundException | //L2pServiceException | //L2pSecurityException | 
							InterruptedException | TimeoutException e) {
						e.printStackTrace();
						objResponse.put("message", "Cannot create badge. Failed to upload " + badgeid + ". " + e.getMessage());
						L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
						//return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_INTERNAL_ERROR);

					}
				}
				else{
					logger.info("Badge ID cannot be null");
					objResponse.put("message", "Cannot create badge. Badge ID cannot be null!");
					L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					//return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_INTERNAL_ERROR);
				}
				
				
			} catch (MalformedStreamException e) {
				// the stream failed to follow required syntax
				objResponse.put("message", "Cannot create badge. Failed to upload " + badgeid + ". " + e.getMessage());
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
				//return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_BAD_REQUEST);

			} catch (IOException e) {
				// a read or write error occurred
				objResponse.put("message", "Cannot create badge. Failed to upload " + badgeid + ". " + e.getMessage());
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
				//return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_INTERNAL_ERROR);

			} catch (SQLException e) {
				e.printStackTrace();
				objResponse.put("message", "Cannot create badge. Failed to upload " + badgeid + ". " + e.getMessage());
				//L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				L2pLogger.logEvent(this, Event.AGENT_UPLOAD_FAILED, (String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
				//return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_INTERNAL_ERROR);

			}
			catch (NullPointerException e){
				e.printStackTrace();
				objResponse.put("message", "Cannot create badge. Failed to upload " + badgeid + ". " + e.getMessage());
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
				//return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_INTERNAL_ERROR);
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
		 * Update a badge.
		 * Name attribute for form data : 
		 * <ul>
		 * 	<li>badgeid - Badge ID - String (20 chars)
		 *  <li>badgeimageinput - Badge Image - Image byte
		 * 	<li>badgename - Badge Name - String (20 chars)
		 *  <li>badgedesc - Badge Description - String (50 chars)
		 *  <li>badgenotificationcheck - Badge Notification Boolean - Boolean - Option whether use notification or not
		 *  <li>badgenotificationmessage - Badge Notification Message - String
		 * </ul>
		 * @param gameId Game ID obtained from Gamification Game Service
		 * @param badgeId badge id
		 * @param formData Form data with multipart/form-data type
		 * @param contentType Content type (implicitly sent in header)
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
				@ApiParam(value = "Badge detail in multiple/form-data type", required = true)@HeaderParam(value = HttpHeaders.CONTENT_TYPE) String contentType, 
				byte[] formData)  {
			
			// Request log
			L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_99,Context.getCurrent().getMainAgent(), "PUT " + "gamification/badges/"+gameId+"/"+badgeId);
			long randomLong = new Random().nextLong(); //To be able to match 
			
			// parse given multipart form data
			JSONObject objResponse = new JSONObject();
			String filename = null;
			byte[] filecontent = null;
			String mimeType = null;
			// Badge ID for the filesystem is appended with game id to make sure it is unique
			String badgename = null;
			String badgedescription = null;
			//boolean badgeusenotification = false;
			String badgenotificationmessage = null;
			Connection conn = null;

			
			UserAgent userAgent = (UserAgent) Context.getCurrent().getMainAgent();
			String name = userAgent.getLoginName();
			if(name.equals("anonymous")){
				return unauthorizedMessage();
			}
			try {
				conn = dbm.getConnection();
				L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_18,Context.getCurrent().getMainAgent(), ""+randomLong);
				
				try {
					if(!badgeAccess.isGameIdExist(conn,gameId)){
						objResponse.put("message", "Cannot update badge. Game not found");
						L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
						//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
					}
				} catch (SQLException e1) {
					e1.printStackTrace();
					objResponse.put("message", "Cannot update badge. Cannot check whether game ID exist or not. Database error. " + e1.getMessage());
					L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
				}
				Map<String, FormDataPart> parts = MultipartHelper.getParts(formData, contentType);
				
				if (badgeId == null) {
					objResponse.put("message", "Cannot update badge. Badge ID cannot be null");
					L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
				}
				BadgeModel currentBadge = badgeAccess.getBadgeWithId(conn,gameId, badgeId);
				if(currentBadge == null){
					// currentBadge is null
					objResponse.put("message", "Cannot update badge. Badge not found");
					L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);

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
//							 validate input
						
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
								//filecontent = resizeImage(filecontentbefore);
								storeBadgeDataToSystem(gameId, badgeId, filename, filecontent,mimeType , badgedescription);
								logger.info("upload request (" + filename + ") of mime type '" + mimeType + "' with content length "
										+ filecontent.length);
								
							} catch (AgentNotFoundException | //L2pServiceException | //L2pSecurityException | 
									InterruptedException | TimeoutException e) {
								e.printStackTrace();
								objResponse.put("message", "Cannot update badge. Failed to upload " + badgeId + ". " + e.getMessage() );
								L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
								return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
								//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
							}
							catch (IllegalArgumentException e){
								objResponse.put("message", "Cannot update badge. Badge image is not updated. " + e.getMessage());
								L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
								return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
								//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
								
							}
						}		
				}
				FormDataPart partNotificationCheck = parts.get("badgenotificationcheck");
				
				if (partNotificationCheck != null) {
					currentBadge.useNotification(true);
					
				}else{
					currentBadge.useNotification(false);
					
				}
				FormDataPart partNotificationMsg = parts.get("badgenotificationmessage");

				if (partNotificationMsg != null) {
					badgenotificationmessage = partNotificationMsg.getContent();
					if(badgenotificationmessage != null){
						currentBadge.setNotificationMessage(badgenotificationmessage);
					}
				}
				
				try{
					badgeAccess.updateBadge(conn,gameId, currentBadge);
					objResponse.put("message", "Badge updated");
					L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_19,Context.getCurrent().getMainAgent(), ""+randomLong);
					L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_28,Context.getCurrent().getMainAgent(), ""+name);
					L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_29,Context.getCurrent().getMainAgent(), ""+gameId);
					return Response.status(HttpURLConnection.HTTP_OK).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_OK);
				} catch (SQLException e) {
					e.printStackTrace();
					objResponse.put("message", "Cannot update badge. Database Error. " + e.getMessage());
					L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);

				}
				
			} catch (MalformedStreamException e) {
				// the stream failed to follow required syntax
				objResponse.put("message", "Cannot update badge. Failed to upload " + badgeId + ". "+e.getMessage());
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
				//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			} catch (IOException e) {
				// a read or write error occurred
				objResponse.put("message", "Cannot update badge. Failed to upload " + badgeId + "." + e.getMessage());
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
				//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
			} catch (SQLException e1) {
				e1.printStackTrace();
				objResponse.put("message", "Cannot update badge. Database Error. " + e1.getMessage());
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
			L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_99,Context.getCurrent().getMainAgent(), "GET " + "gamification/badges/"+gameId+"/"+badgeId);
			long randomLong = new Random().nextLong(); //To be able to match 
			
			BadgeModel badge = null;
			Connection conn = null;

			JSONObject objResponse = new JSONObject();
			UserAgent userAgent = (UserAgent) Context.getCurrent().getMainAgent();
			String name = userAgent.getLoginName();
			if(name.equals("anonymous")){
				return unauthorizedMessage();
			}
			try {
				conn = dbm.getConnection();
				L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_16,Context.getCurrent().getMainAgent(), ""+randomLong);
				
				try {
					if(!badgeAccess.isGameIdExist(conn,gameId)){
						objResponse.put("message", "Cannot get badge. Game not found");
						L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
						//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
					}
				} catch (SQLException e1) {
					e1.printStackTrace();
					objResponse.put("message", "Cannot get badge. Cannot check whether game ID exist or not. Database error. " + e1.getMessage());
					L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
				}
				badge = badgeAccess.getBadgeWithId(conn,gameId, badgeId);
				if(badge == null){
					objResponse.put("message", "Cannot get badge. Badge model is null.");
					L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
				}
				ObjectMapper objectMapper = new ObjectMapper();
		    	//Set pretty printing of json
		    	objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		    	
		    	String badgeString = objectMapper.writeValueAsString(badge);
		    	L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_17,Context.getCurrent().getMainAgent(), ""+randomLong);
		    	L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_26,Context.getCurrent().getMainAgent(), ""+name);
				L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_27,Context.getCurrent().getMainAgent(), ""+gameId);
				return Response.status(HttpURLConnection.HTTP_OK).entity(badgeString).type(MediaType.APPLICATION_JSON).build();
				//return new HttpResponse(badgeString, HttpURLConnection.HTTP_OK);

			} catch (JsonProcessingException e) {
				e.printStackTrace();
				objResponse.put("message", "Cannot get badge. Cannot process JSON." + e.getMessage());
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(e.getMessage()).type(MediaType.APPLICATION_JSON).build();
				//return new HttpResponse(e.getMessage(), HttpURLConnection.HTTP_INTERNAL_ERROR);

			} catch (SQLException e) {
				e.printStackTrace();
				objResponse.put("message", "Cannot get badge. Database Error. " + e.getMessage());
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
			L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_99,Context.getCurrent().getMainAgent(), "DELETE " + "gamification/badges/"+gameId+"/"+badgeId);
			long randomLong = new Random().nextLong(); //To be able to match 
			
			JSONObject objResponse = new JSONObject();
			Connection conn = null;

			UserAgent userAgent = (UserAgent) Context.getCurrent().getMainAgent();
			String name = userAgent.getLoginName();
			if(name.equals("anonymous")){
				return unauthorizedMessage();
			}
			try {
				conn = dbm.getConnection();
				L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_20,Context.getCurrent().getMainAgent(), ""+randomLong);
				
				try {
					if(!badgeAccess.isGameIdExist(conn,gameId)){
						objResponse.put("message", "Cannot delete badge. Game not found");
						L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
						//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
					}
				} catch (SQLException e1) {
					e1.printStackTrace();
					objResponse.put("message", "Cannot delete badge. Cannot check whether game ID exist or not. Database error. " + e1.getMessage());
					L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
				}
				if(!badgeAccess.isBadgeIdExist(conn,gameId, badgeId)){
					logger.info("Badge not found >> ");
					objResponse.put("message", "Cannot delete badge. Badge not found");
					L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
				}
				badgeAccess.deleteBadge(conn,gameId, badgeId);
				if(!LocalFileManager.deleteFile(LocalFileManager.getBasedir()+"/"+gameId+"/"+badgeId)){
					
					logger.info("Delete File Failed >> ");
					objResponse.put("message", "Cannot delete badge. Delete File Failed");
					L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);

				}
				objResponse.put("message", "File Deleted");
				L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_21,Context.getCurrent().getMainAgent(), ""+randomLong);
				L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_30,Context.getCurrent().getMainAgent(), ""+name);
				L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_31,Context.getCurrent().getMainAgent(), ""+gameId);
				return Response.status(HttpURLConnection.HTTP_OK).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
				//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_OK);

			} catch (SQLException e) {
				
				e.printStackTrace();
				objResponse.put("message", "Cannot delete badge. Cannot delete file. " + e.getMessage());
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_OK).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
				//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_OK);
			}		 // always close connections
		    finally {
			      try {
			        conn.close();
			      } catch (SQLException e) {
			        logger.printStackTrace(e);
			      }
			    }
			
		}
		
		// TODO Batch processing --------------------
		
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
			L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_99,Context.getCurrent().getMainAgent(), "GET " + "gamification/badges/"+gameId);
			long randomLong = new Random().nextLong(); //To be able to match 
			
			List<BadgeModel> badges = null;
			Connection conn = null;

			JSONObject objResponse = new JSONObject();
			UserAgent userAgent = (UserAgent) Context.getCurrent().getMainAgent();
			String name = userAgent.getLoginName();
			if(name.equals("anonymous")){
				return unauthorizedMessage();
			}
			try {
				conn = dbm.getConnection();
				L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_46,Context.getCurrent().getMainAgent(), ""+randomLong);
				
				try {
					if(!badgeAccess.isGameIdExist(conn,gameId)){
						objResponse.put("message", "Cannot get badges. Game not found");
						L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
						//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
					}
				} catch (SQLException e1) {
					e1.printStackTrace();
					logger.info("Cannot check whether game ID exist or not. Database error. >> " + e1.getMessage());
					objResponse.put("message", "Cannot get badges. Cannot check whether game ID exist or not. Database error. " + e1.getMessage());
					L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
				}
				// Check game id exist or not
				
				int offset = (currentPage - 1) * windowSize;
				
				int totalNum = badgeAccess.getNumberOfBadges(conn,gameId);
				
				if(windowSize == -1){
					offset = 0;
					windowSize = totalNum;
				}
				
				badges = badgeAccess.getBadgesWithOffsetAndSearchPhrase(conn,gameId, offset, windowSize, searchPhrase);
				
				
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

				L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_47,Context.getCurrent().getMainAgent(), ""+randomLong);
				L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_48,Context.getCurrent().getMainAgent(), ""+name);
				L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_49,Context.getCurrent().getMainAgent(), ""+gameId);
				return Response.status(HttpURLConnection.HTTP_OK).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
				//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_OK);

			} catch (SQLException e) {
				e.printStackTrace();
				objResponse.put("message", "Cannot get badges. Internal Error. Database connection failed. " + e.getMessage());
				
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
				//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
				objResponse.put("message", "Cannot get badges. Cannot connect to database. " + e.getMessage() );
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
		
		// TODO Other functions ---------------------
		
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
			L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_99,Context.getCurrent().getMainAgent(), "GET " + "gamification/badges/"+gameId+"/"+badgeId+"/img");
			
			JSONObject objResponse = new JSONObject();
			Connection conn = null;

			UserAgent userAgent = (UserAgent) Context.getCurrent().getMainAgent();
			String name = userAgent.getLoginName();
			if(name.equals("anonymous")){
				return unauthorizedMessage();
			}
			try {
				conn = dbm.getConnection();
				L2pLogger.logEvent(this, Event.AGENT_GET_STARTED, "Get Badge Image");
				L2pLogger.logEvent(this, Event.ARTIFACT_FETCH_STARTED,"Get Badge Image");
				
				try {
					if(!badgeAccess.isGameIdExist(conn,gameId)){
						objResponse.put("message", "Cannot get badge image. Game not found");
						L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
						//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
					}
				} catch (SQLException e1) {
					e1.printStackTrace();
					objResponse.put("message", "Cannot get badge image. Cannot check whether game ID exist or not. Database error. " + e1.getMessage());
					L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
				}
				if(!badgeAccess.isBadgeIdExist(conn,gameId, badgeId)){
					objResponse.put("message", "Cannot get badge image. Badge not found");
					L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_BAD_REQUEST).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
					//return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
				}
				byte[] filecontent = getBadgeImageMethod(gameId, badgeId);

				L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_25,Context.getCurrent().getMainAgent(), "Badge image fetched : " + badgeId + " : " + gameId + " : " + userAgent);
				L2pLogger.logEvent(this, Event.ARTIFACT_RECEIVED, "Badge image fetched : " + badgeId + " : " + gameId + " : " + userAgent);
				return Response.status(HttpURLConnection.HTTP_OK).entity(filecontent).type(MediaType.APPLICATION_JSON).build();
				//return new HttpResponse(filecontent, HttpURLConnection.HTTP_OK);
			} catch (SQLException e) {
				e.printStackTrace();
				objResponse.put("message", "Cannot get badge image. Database error. " + e.getMessage());
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
		        conn.close();
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
	
}

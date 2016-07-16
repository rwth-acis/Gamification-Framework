package i5.las2peer.services.gamificationBadgeService;

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
import i5.las2peer.services.gamificationBadgeService.database.BadgeDAO;
import i5.las2peer.services.gamificationBadgeService.database.BadgeModel;
import i5.las2peer.services.gamificationBadgeService.database.SQLDatabase;
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
import net.minidev.json.parser.ParseException;


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
@Version("0.1") // this annotation is used by the XML mapper
@Api( value = "/gamification/badges", authorizations = {
		@Authorization(value = "badges_auth",
		scopes = {
			@AuthorizationScope(scope = "write:badges", description = "modify badges in your application"),
			@AuthorizationScope(scope = "read:badges", description = "read your badges in app")
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

// TODO Your own Serviceclass
public class GamificationBadgeService extends Service {

	// instantiate the logger class
	private final L2pLogger logger = L2pLogger.getInstance(GamificationBadgeService.class.getName());
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
	private BadgeDAO badgeAccess;

	// Static variables 
	private String badgeImageURIBase = null;	
	
	// this header is not known to javax.ws.rs.core.HttpHeaders
	public static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";
	public static final String HEADER_CONTENT_TYPE = "Content-Type";
	// non HTTP standard headers
	public static final String HEADER_OWNERID = "ownerid";
	public static final String HEADER_CONTENT_DESCRIPTION = "Content-Description";

	public GamificationBadgeService() {
		// read and set properties values
		// IF THE SERVICE CLASS NAME IS CHANGED, THE PROPERTIES FILE NAME NEED TO BE CHANGED TOO!
		setFieldValues();
		badgeImageURIBase = epURL + "gamification/badges/";
	}

	private boolean initializeDBConnection() {

		this.DBManager = new SQLDatabase(this.jdbcDriverClassName, this.jdbcLogin, this.jdbcPass, this.jdbcSchema, this.jdbcHost, this.jdbcPort);
		logger.info(jdbcDriverClassName + " " + jdbcLogin);
		try {
				this.DBManager.connect();
				this.badgeAccess = new BadgeDAO(this.DBManager.getConnection());
				logger.info("Monitoring: Database connected!");
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				logger.info("Monitoring: Could not connect to database!. " + e.getMessage());
				return false;
			}
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
	
	/**
	 * Post a new badge
	 * @param appId application id
	 * @param formData form data
	 * @param contentType content type
	 * @return HttpResponse Returned as JSON object
	 */
	@POST
	@Path("/{appId}")
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
		boolean badgeusenotification = false;
		String badgenotificationmessage = null;
		
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

					System.out.println(badgeImageURI);
					storeBadgeDataToSystem(appId, badgeid, filename, filecontent,mimeType , badgedescription);
					BadgeModel badge = new BadgeModel(badgeid, badgename, badgedescription, badgeImageURI, badgeusenotification, badgenotificationmessage);
					
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
	@Path("/{appId}/{badgeId}")
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
		boolean badgeusenotification = false;
		String badgenotificationmessage = null;

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
					badgeImageURI = badgeImageURIBase + appId + "/" + badgeId + "/img";
					
					if (filecontentbefore != null) {
						try {
							filecontent = resizeImage(filecontentbefore);
							System.out.println(badgeImageURI);
							storeBadgeDataToSystem(appId, badgeId, filename, filecontent,mimeType , badgedescription);
							currentBadge.setImagePath(badgeImageURI);
							logger.info("upload request (" + filename + ") of mime type '" + mimeType + "' with content length "
									+ filecontent.length);
						} catch (AgentNotKnownException | L2pServiceException | L2pSecurityException | InterruptedException
								| TimeoutException e) {
							e.printStackTrace();
							L2pLogger.logEvent(this, Event.SERVICE_ERROR, e.toString());
							System.out.println(e.getMessage());
							logger.info("OtherException. "+e.getMessage());
							return new HttpResponse("Failed to upload " + badgeId + ".",
									HttpURLConnection.HTTP_INTERNAL_ERROR);
						}
						catch (IllegalArgumentException e){
							logger.info("Badge image is not updated. Null"+e.getMessage());
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
	@Path("/{appId}/{badgeId}")
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
			String badgeString = getBadgeWithIdMethod(appId, badgeId);
			return new HttpResponse(badgeString, HttpURLConnection.HTTP_OK);

		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return new HttpResponse(e.getMessage(), HttpURLConnection.HTTP_INTERNAL_ERROR);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.info("Database Error. " + e.getMessage());
			objResponse.put("message", "Database Error " + e.getMessage());
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);

		}
	}

	public String getBadgeWithIdMethod(String appId, String badgeId) throws SQLException, JsonProcessingException {
		BadgeModel badge = badgeAccess.getBadgeWithId(appId, badgeId);
		if(badge != null){
			ObjectMapper objectMapper = new ObjectMapper();
	    	//Set pretty printing of json
	    	objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
	    	
	    	String badgeString = objectMapper.writeValueAsString(badge);
	    	return badgeString;
					}
		else{
			throw new SQLException("Badge Model is null");
			
		}
	}
	
	/**
	 * Delete a badge data with specified ID
	 * @param appId application id
	 * @param badgeId badge id
	 * @return HttpResponse Returned as JSON object
	 */
	@DELETE
	@Path("/{appId}/{badgeId}")
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
	@Path("/{appId}")
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
	@Path("/{appId}/{badgeId}/img")
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
			if(!badgeAccess.isBadgeIdExist(appId, badgeId)){
				logger.info("Badge not found >> ");
				objResponse.put("message", "Badge not found");
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			byte[] filecontent = getBadgeImageMethod(appId, badgeId);
			return new HttpResponse(filecontent, HttpURLConnection.HTTP_OK);
		} catch (SQLException e) {
			e.printStackTrace();
			logger.info("Database error >>  " + e.getMessage());
			objResponse.put("message", "Database error " + e.getMessage());
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);

		}
	}
	
	public byte[] getBadgeImageMethod(String appId, String badgeId){
		byte[] filecontent = LocalFileManager.getFile(appId+"/"+badgeId);
		return filecontent;
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

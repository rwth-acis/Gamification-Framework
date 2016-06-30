package i5.las2peer.services.actionService;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
import javax.ws.rs.core.HttpHeaders;

import org.apache.commons.fileupload.MultipartStream.MalformedStreamException;
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
import i5.las2peer.p2p.Node;
import i5.las2peer.p2p.StorageException;
import i5.las2peer.p2p.TimeoutException;
import i5.las2peer.restMapper.HttpResponse;
import i5.las2peer.restMapper.MediaType;
import i5.las2peer.restMapper.RESTMapper;
import i5.las2peer.restMapper.annotations.ContentParam;
import i5.las2peer.restMapper.annotations.Version;
import i5.las2peer.restMapper.tools.ValidationResult;
import i5.las2peer.restMapper.tools.XMLCheck;
import i5.las2peer.security.GroupAgent;
import i5.las2peer.security.L2pSecurityException;
import i5.las2peer.security.UserAgent;
import i5.las2peer.services.badgeService.database.BadgeDAO;
import i5.las2peer.services.badgeService.database.BadgeModel;
import i5.las2peer.services.badgeService.helper.*;
import i5.las2peer.services.gamificationManagerService.database.SQLDatabase;
import i5.las2peer.tools.SimpleTools;
import io.swagger.annotations.*;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Contact;
import io.swagger.annotations.Info;
import io.swagger.annotations.License;
import io.swagger.annotations.SwaggerDefinition;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;


/**
 * Badge Service
 * 
 * This is Badge service to access and manipulate the Badges in Gamification Framework
 * It uses the LAS2peer Web-Connector for RESTful access to it.
 * 
 * Note:
 * If you plan on using Swagger you should adapt the information below
 * in the ApiInfo annotation to suit your project.
 * If you do not intend to provide a Swagger documentation of your service API,
 * the entire ApiInfo annotation should be removed.
 * 
 */

@Path("/badges")
@Version("0.1") // this annotation is used by the XML mapper
@Api( value = "/badges", authorizations = {
		@Authorization(value = "game_auth",
		scopes = {
			@AuthorizationScope(scope = "write:badges", description = "modify badges in your application"),
			@AuthorizationScope(scope = "read:badges", description = "read your badges")
				  })
}, tags = "badges")
@SwaggerDefinition(
		info = @Info(
				title = "Gamification Badge Service",
				version = "0.1",
				description = "Badge Service for Gamification Framework",
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

public class ActionService extends Service {

	// instantiate the logger class
	private final L2pLogger logger = L2pLogger.getInstance(ActionService.class.getName());
	
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
	
	private SQLDatabase dbManager;
	private BadgeDAO badgeAccess;
	
	// Static variables 
	private String badgeImageURIBase = null;	
	// this header is not known to javax.ws.rs.core.HttpHeaders
	public static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";
	public static final String HEADER_CONTENT_TYPE = "Content-Type";
	// non HTTP standard headers
	public static final String HEADER_OWNERID = "ownerid";
	public static final String HEADER_CONTENT_DESCRIPTION = "Content-Description";
	private static final SimpleDateFormat RFC2822FMT = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z (zzz)");

	// Constructor
	public ActionService() {
		// read and set properties values
		// IF THE SERVICE CLASS NAME IS CHANGED, THE PROPERTIES FILE NAME NEED TO BE CHANGED TOO!
		setFieldValues();
		badgeImageURIBase = epURL + "badges/";

	}
	
	// TODO Change appId type into UUID
	// TODO change database login
	private void initializeDBConnection(String appId) throws SQLException {
		
		// Adapt database schema to the specified app id
//		this.jdbcSchema = this.jdbcSchema + Integer.toString(appId);
		this.jdbcSchema = "db"+appId;
		
//		this.dbManager = new DatabaseManager(this.jdbcDriverClassName, this.jdbcLogin, this.jdbcPass, this.jdbcUrl, this.jdbcSchema);
		this.dbManager = new SQLDatabase(this.jdbcDriverClassName, this.jdbcLogin, this.jdbcPass, this.jdbcSchema, this.jdbcHost, this.jdbcPort);
		logger.info(jdbcDriverClassName + " " + jdbcLogin);
		try {
				this.dbManager.connect();
				this.badgeAccess = new BadgeDAO(this.dbManager.getConnection());
				logger.info("Monitoring: Database connected!");
			} catch (Exception e) {
				e.printStackTrace();
				logger.info("Monitoring: Could not connect to database!. " + e.getMessage());
				
			}
		
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	// Service methods.
	// //////////////////////////////////////////////////////////////////////////////////////
	
	
	// TODO OWN METHODS


	/**
	 * Get a list of badges from database
	 * @param appId applicationId
	 * @return HttpResponse Returned as JSON object
	 */
	@GET
	@Path("/items/{appId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Found a list of badges"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal Error"),
			@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized")})
	@ApiOperation(value = "Find badges for specific App ID", 
				  notes = "Returns a list of badges",
				  response = BadgeModel.class,
				  responseContainer = "List",
				  authorizations = @Authorization(value = "api_key")
				  )
	public HttpResponse getBadgeList(
			@ApiParam(value = "Application ID to return")@PathParam("appId") String appId)
	{
		List<BadgeModel> badges;
		JSONObject objResponse = new JSONObject();
		UserAgent userAgent = (UserAgent) getContext().getMainAgent();
		String name = userAgent.getLoginName();
		if(!name.equals("anonymous")){
			try {
				initializeDBConnection(appId);
				ObjectMapper objectMapper = new ObjectMapper();
		    	//Set pretty printing of json
		    	objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
				
				badges = badgeAccess.getAllBadges();
				logger.info(badges.toString());
				String response;
				try {
					response = objectMapper.writeValueAsString(badges);
					badges.clear();
					
					return new HttpResponse(response, HttpURLConnection.HTTP_OK);
				
				} catch (JsonProcessingException e) {
					e.printStackTrace();
					response = "Internal Error. JsonProcessingException. ";
					
					badges.clear();
					// return HTTP Response on error
					return new HttpResponse(response+e.getMessage(), HttpURLConnection.HTTP_INTERNAL_ERROR);

				}
			} catch (SQLException e) {
				e.printStackTrace();
				String response = "Internal Error. Database connection failed. ";
				
				// return HTTP Response on error
				return new HttpResponse(response+e.getMessage(), HttpURLConnection.HTTP_INTERNAL_ERROR);
			}
		}
		else{

			logger.info("Unauthorized >> ");
			objResponse.put("success", false);
			objResponse.put("message", "You are not authorized");
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_UNAUTHORIZED);

		}
		
	}
	
	/**
	 * Post a new badge
	 * @param appId applicationId
	 * @param formData form data
	 * @param contentType content type
	 * @return HttpResponse with the returnString
	 */
	@POST
	@Path("/items/{appId}")
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
		if(!name.equals("anonymous")){
			try {
				initializeDBConnection(appId);
				Map<String, FormDataPart> parts = MultipartHelper.getParts(formData, contentType);
				FormDataPart partBadgeID = parts.get("badgeid");
				if (partBadgeID != null) {
					// these data belong to the (optional) file id text input form element
					badgeid = partBadgeID.getContent();
					badgeImageURI = badgeImageURIBase + appId + "/" + badgeid + "/img";
					
					if(!badgeAccess.isBadgeIdExist(badgeid)){
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
								
								filecontent = resizeImage(filecontentbefore);
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
								badgeAccess.addNewBadge(badge);
							} catch (SQLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								System.out.println(e.getMessage());
								logger.info("SQLException >> " + e.getMessage());
								objResponse.put("status", 2);
								objResponse.put("message", "Failed to upload " + badgeid);
								return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_INTERNAL_ERROR);
							}
							logger.info("Badge upload success (" + badgeid +")");
							objResponse.put("status", 3);
							objResponse.put("message", "Badge upload success (" + badgeid +")");
							return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_CREATED);

						} catch (AgentNotKnownException | L2pServiceException | L2pSecurityException | InterruptedException
								| TimeoutException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							L2pLogger.logEvent(this, Event.SERVICE_ERROR, e.toString());
							logger.info("Other Exception >> " + e.getMessage());
							objResponse.put("status", 2);
							objResponse.put("message", "Failed to upload " + badgeid);
							return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_INTERNAL_ERROR);

						}
						
					}
					else{
						// Badge id already exist
						logger.info("Failed to add the badge. Badge ID already exist!");
						objResponse.put("status", 1);
						objResponse.put("message", "Failed to add the badge. Badge ID already exist!");
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
				// TODO Auto-generated catch block
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
		else{

			logger.info("Unauthorized >> ");
			objResponse.put("success", false);
			objResponse.put("message", "You are not authorized");
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_UNAUTHORIZED);

		}
		
		
	
	}
	
	private byte[] resizeImage(byte[] inputImageRaw) throws IOException, NullPointerException{
		logger.info("Resize 2: " + inputImageRaw.toString());
		BufferedImage img = ImageIO.read(new ByteArrayInputStream(inputImageRaw));
		logger.info("Resize 3: " + img.toString());
		BufferedImage newImg = Scalr.resize(img,Mode.AUTOMATIC,300,300);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(newImg, "png", baos);
		baos.flush();
		byte[] output = baos.toByteArray();
		baos.close();
		return output;
		
	}
	
	/**
	 * Function to invoke method in las2peer-FileService
	 * @param appId appId
	 * @param badgeId badgeId
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
	
	/**
	 * Update a badge
	 * @param appId applicationId
	 * @param badgeId badgeId
	 * @param formData form data
	 * @param contentType content type
	 * @return HttpResponse with the returnString
	 */
	@PUT
	@Path("/items/{appId}/{badgeId}")
	@Produces(MediaType.TEXT_PLAIN)
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
				@PathParam("appId") String badgeId,
			@ApiParam(value = "Badge detail in multiple/form-data type", required = true)@HeaderParam(value = HttpHeaders.CONTENT_TYPE) String contentType, 
									 @ContentParam byte[] formData)  {
		// parse given multipart form data
		
		String filename = null;
		byte[] filecontent = null;
		String mimeType = null;
		String badgeid = null;
		// Badge ID for the filesystem is appended with app id to make sure it is unique
		String badgename = null;
		String badgedescription = null;
		String badgeImageURI = null;
		try {
			initializeDBConnection(appId);
			Map<String, FormDataPart> parts = MultipartHelper.getParts(formData, contentType);
			FormDataPart partBadgeID = parts.get("badgeid");
			if (partBadgeID != null) {
				// these data belong to the (optional) file id text input form element
				badgeid = partBadgeID.getContent();
				badgeImageURI = badgeImageURIBase + appId + "/" + badgeid + "/img";
				
				BadgeModel currentBadge = badgeAccess.getBadgeWithId(badgeid);

				FormDataPart partBadgeName = parts.get("badgename");
				if (partBadgeName != null) {
					badgename = partBadgeName.getContent();
					currentBadge.setName(badgename);
				}
				FormDataPart partDescription = parts.get("badgedesc");
				if (partDescription != null) {
					// optional description text input form element
					badgedescription = partDescription.getContent();
					currentBadge.setDescription(badgedescription);
				}
				FormDataPart partFilecontent = parts.get("badgeimageinput");
				if (partFilecontent != null) {
					// these data belong to the file input form element
						filename = partFilecontent.getHeader(HEADER_CONTENT_DISPOSITION).getParameter("filename");
						filecontent = partFilecontent.getContentRaw();
						mimeType = partFilecontent.getContentType();
//						 validate input
						if (filecontent == null) {
							return new HttpResponse(
									"Failed to upload " + badgeid + " " + badgename + " " + badgedescription + ". No content provided. ",
									HttpURLConnection.HTTP_BAD_REQUEST);
						}
						try {
							
							storeBadgeDataToSystem(appId, badgeid, filename, filecontent,mimeType , badgedescription);

						} catch (AgentNotKnownException | L2pServiceException | L2pSecurityException | InterruptedException
								| TimeoutException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							L2pLogger.logEvent(this, Event.SERVICE_ERROR, e.toString());
							System.out.println(e.getMessage());
							logger.info("OtherException. "+e.getMessage());
							return new HttpResponse("Failed to upload " + badgeid + ".",
									HttpURLConnection.HTTP_INTERNAL_ERROR);
						}
						logger.info("upload request (" + filename + ") of mime type '" + mimeType + "' with content length "
								+ filecontent.length);
				}
				try{
					badgeAccess.updateBadge(currentBadge);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.out.println(e.getMessage());
					logger.info("SQLException. "+e.getMessage());
					return new HttpResponse("Database Error.", HttpURLConnection.HTTP_INTERNAL_ERROR);
				}
				
				return new HttpResponse("Badge updated", HttpURLConnection.HTTP_OK);
			}
			else{
				return new HttpResponse("Badge ID cannot be null", HttpURLConnection.HTTP_BAD_REQUEST);
			}
			
		} catch (MalformedStreamException e) {
			// the stream failed to follow required syntax
			logger.log(Level.SEVERE, e.getMessage(), e);
			System.out.println(e.getMessage());
			logger.info("MalformedStreamException. "+e.getMessage());
			return new HttpResponse("Failed to upload " + badgeid + ". "+e.getMessage(),
					HttpURLConnection.HTTP_BAD_REQUEST);
		} catch (IOException e) {
			// a read or write error occurred
			logger.log(Level.SEVERE, e.getMessage(), e);
			System.out.println(e.getMessage());
			logger.info("IOException. "+e.getMessage());
			return new HttpResponse("Failed to upload " + badgeid + ".",
					HttpURLConnection.HTTP_INTERNAL_ERROR);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			logger.info("SQLException. "+e1.getMessage());
			return new HttpResponse("Failed to upload " + badgeid + ". Database error.",
					HttpURLConnection.HTTP_INTERNAL_ERROR);
		}

	}
	
	/**
	 * Get a badge image with specified ID
	 * @param appId applicationId
	 * @param badgeId badgeId
	 * @return HttpResponse with the return image
	 */
	@GET
	@Path("/items/{appId}/{badgeId}/img")
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
		byte[] filecontent = LocalFileManager.getFile(appId+"/"+badgeId);
		return new HttpResponse(filecontent, HttpURLConnection.HTTP_OK);
		
	}
	
	/**
	 * Delete a badge data with specified ID
	 * @param appId applicationId
	 * @param badgeId badgeId
	 * @return HttpResponse with the returnString
	 */
	//TODO Delete from network storage
	@DELETE
	@Path("/items/{appId}/{badgeId}")
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
		if(!name.equals("anonymous")){
			try {
				initializeDBConnection(appId);
				badgeAccess.deleteBadge(badgeId);
				if(!LocalFileManager.deleteFile(LocalFileManager.getBasedir()+"/"+appId+"/"+badgeId)){
					// TODO Delete file in storage
					logger.info("Delete File Failed >> ");
					objResponse.put("message", "Delete File Failed");
					return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);

				}
				// TODO Delete file in storage
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
		else{

			logger.info("Unauthorized >> ");
			objResponse.put("success", false);
			objResponse.put("message", "You are not authorized");
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_UNAUTHORIZED);

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

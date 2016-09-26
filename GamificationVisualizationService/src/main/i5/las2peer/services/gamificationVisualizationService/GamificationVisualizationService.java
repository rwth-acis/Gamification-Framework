package i5.las2peer.services.gamificationVisualizationService;

import java.io.IOException;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

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
import i5.las2peer.restMapper.annotations.Version;
import i5.las2peer.restMapper.tools.ValidationResult;
import i5.las2peer.restMapper.tools.XMLCheck;
import i5.las2peer.security.L2pSecurityException;
import i5.las2peer.security.UserAgent;
import i5.las2peer.services.gamificationVisualizationService.database.DatabaseManager;
import i5.las2peer.services.gamificationVisualizationService.database.AchievementModel;
import i5.las2peer.services.gamificationVisualizationService.database.ApplicationDAO;
import i5.las2peer.services.gamificationVisualizationService.database.BadgeModel;
import i5.las2peer.services.gamificationVisualizationService.database.MemberDAO;
import i5.las2peer.services.gamificationVisualizationService.database.QuestModel;
import i5.las2peer.services.gamificationVisualizationService.database.QuestModel.QuestStatus;
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


// TODO Describe your own service
/**
 * Member Service
 * 
 * This is Gamification Member service to fetch the data about members
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
@Path("/visualization")
@Version("0.1") // this annotation is used by the XML mapper
@Api( value = "/members", authorizations = {
		@Authorization(value = "members_auth",
		scopes = {
			//@AuthorizationScope(scope = "write:members", description = "modify apps in your application"),
			@AuthorizationScope(scope = "read:members", description = "Get data about members")
				  })
}, tags = "members")
@SwaggerDefinition(
		info = @Info(
				title = "Members Service",
				version = "0.1",
				description = "Member Service for Gamification Framework",
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
public class GamificationVisualizationService extends Service {

	// instantiate the logger class
	private final L2pLogger logger = L2pLogger.getInstance(GamificationVisualizationService.class.getName());
	/*
	 * Database configuration
	 */
	private String jdbcDriverClassName;
	private String jdbcLogin;
	private String jdbcPass;
	private String jdbcUrl;
	private String jdbcSchema;
	private DatabaseManager dbm;
	private ApplicationDAO managerAccess;
	private MemberDAO memberAccess;
	
	// this header is not known to javax.ws.rs.core.HttpHeaders
	public static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";
	public static final String HEADER_CONTENT_TYPE = "Content-Type";
	// non HTTP standard headers
	public static final String HEADER_OWNERID = "ownerid";
	public static final String HEADER_CONTENT_DESCRIPTION = "Content-Description";

	public GamificationVisualizationService() {
		// read and set properties values
		// IF THE SERVICE CLASS NAME IS CHANGED, THE PROPERTIES FILE NAME NEED TO BE CHANGED TOO!
		setFieldValues();
		dbm = new DatabaseManager(jdbcDriverClassName, jdbcLogin, jdbcPass, jdbcUrl, jdbcSchema);
		this.managerAccess = new ApplicationDAO();
		this.memberAccess = new MemberDAO();
	}


	private HttpResponse unauthorizedMessage(){
		JSONObject objResponse = new JSONObject();
		objResponse.put("message", "You are not authorized");
		L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
		return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_UNAUTHORIZED);

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	// Service methods.
	// //////////////////////////////////////////////////////////////////////////////////////
	
	
	// //////////////////////////////////////////////////////////////////////////////////////
	// Application PART --------------------------------------
	// //////////////////////////////////////////////////////////////////////////////////////
	
	
	/**
	 * Get member point
	 * @param appId applicationId
	 * @param memberId member id
	 * @return HttpResponse with the returnString
	 */
	@GET
	@Path("/points/{appId}/{memberId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "App Selected"),
			@ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "App not found"),
			@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad Request"),
	})
	@ApiOperation(value = "",
				  notes = "Select an App")
	public HttpResponse getPointOfMember(
			@ApiParam(value = "Application ID", required = true)@PathParam("appId") String appId,
			@ApiParam(value = "Member ID", required = true)@PathParam("memberId") String memberId)
	{
		JSONObject objResponse = new JSONObject();
		Connection conn = null;

		UserAgent userAgent = (UserAgent) getContext().getMainAgent();
		String name = userAgent.getLoginName();
		if(name.equals("anonymous")){
			return unauthorizedMessage();
		}

		try {
			conn = dbm.getConnection();
			if(!managerAccess.isAppIdExist(conn,appId)){
				logger.info("App not found >> ");
				objResponse.put("message", "App not found");
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			if(!managerAccess.isMemberRegistered(conn,memberId)){
				logger.info("Member ID not found >> ");
				objResponse.put("message", "Member ID not found");
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			if(!managerAccess.isMemberRegisteredInApp(conn,memberId,appId)){
				logger.info("Member is not registered in App >> ");
				objResponse.put("message", "Member is not registered in App");
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			// Add Member to App
			Integer memberPoint = memberAccess.getMemberPoint(conn,appId, memberId);
			objResponse.put("message", memberPoint);
			
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_OK);
		} catch (SQLException e) {
			
			e.printStackTrace();

			logger.info("SQLException >> " + e.getMessage());
			objResponse.put("message", "Database Error");
			L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
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
	 * Get gamification level status for a specific user
	 * @param appId applicationId
	 * @param memberId member id
	 * @return HttpResponse with the returnString
	 */
	@GET
	@Path("/status/{appId}/{memberId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "App Selected"),
			@ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "App not found"),
			@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad Request"),
	})
	@ApiOperation(value = "",
				  notes = "Select an App")
	public HttpResponse getStatusOfMember(
			@ApiParam(value = "Application ID", required = true)@PathParam("appId") String appId,
			@ApiParam(value = "Member ID", required = true)@PathParam("memberId") String memberId)
	{
		long randomLong = new Random().nextLong(); //To be able to match 
		
		JSONObject objResponse = new JSONObject();
		Connection conn = null;

		UserAgent userAgent = (UserAgent) getContext().getMainAgent();
		String name = userAgent.getLoginName();
		if(name.equals("anonymous")){
			return unauthorizedMessage();
		}

		try {
			conn = dbm.getConnection();
			L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_32, ""+randomLong);
			
			if(!managerAccess.isAppIdExist(conn,appId)){
				logger.info("App not found >> ");
				objResponse.put("message", "App not found");
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			if(!managerAccess.isMemberRegistered(conn,memberId)){
				logger.info("Member ID not found >> ");
				objResponse.put("message", "Member ID not found");
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			if(!managerAccess.isMemberRegisteredInApp(conn,memberId,appId)){
				logger.info("Member is not registered in App >> ");
				objResponse.put("message", "Member is not registered in App");
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			
			// Get point name
			// RMI call with parameters
			String pointUnitName = "";
			try {
				pointUnitName = (String) this.invokeServiceMethod("i5.las2peer.services.gamificationPointService.GamificationPointService@0.1", "getUnitNameRMI",
						new Serializable[] { appId, memberId });

				
			} catch (AgentNotKnownException | L2pServiceException | L2pSecurityException | InterruptedException
					| TimeoutException e) {
				e.printStackTrace();
				pointUnitName = "";
			}
			
			// Add Member to App
			JSONObject obj = memberAccess.getMemberStatus(conn,appId, memberId);
			obj.put("pointUnitName", pointUnitName);
			L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_33, ""+randomLong);
			
			return new HttpResponse(obj.toJSONString(), HttpURLConnection.HTTP_OK);
		} catch (SQLException e) {
			
			e.printStackTrace();

			logger.info("SQLException >> " + e.getMessage());
			objResponse.put("message", "Database Error");
			L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
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
	 * Get gamification badges for a specific member
	 * @param appId applicationId
	 * @param memberId member id
	 * @return HttpResponse with the returnString
	 */
	@GET
	@Path("/badges/{appId}/{memberId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "App Selected"),
			@ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "App not found"),
			@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad Request"),
	})
	@ApiOperation(value = "",
				  notes = "Select an App")
	public HttpResponse getBadgesOfMember(
			@ApiParam(value = "Application ID", required = true)@PathParam("appId") String appId,
			@ApiParam(value = "Member ID", required = true)@PathParam("memberId") String memberId)
	{
		long randomLong = new Random().nextLong(); //To be able to match 
		
		JSONObject objResponse = new JSONObject();
		Connection conn = null;

		List<BadgeModel> badges = new ArrayList<BadgeModel>();
		UserAgent userAgent = (UserAgent) getContext().getMainAgent();
		String name = userAgent.getLoginName();
		if(name.equals("anonymous")){
			return unauthorizedMessage();
		}

		try {
			conn = dbm.getConnection();
			L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_38, ""+randomLong);
			
			if(!managerAccess.isAppIdExist(conn,appId)){
				logger.info("App not found >> ");
				objResponse.put("message", "App not found");
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			if(!managerAccess.isMemberRegistered(conn,memberId)){
				logger.info("Member ID not found >> ");
				objResponse.put("message", "Member ID not found");
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			if(!managerAccess.isMemberRegisteredInApp(conn,memberId,appId)){
				logger.info("Member is not registered in App >> ");
				objResponse.put("message", "Member is not registered in App");
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			// Add Member to App
			badges = memberAccess.getObtainedBadges(conn,appId, memberId);
			ObjectMapper objectMapper = new ObjectMapper();
	    	//Set pretty printing of json
	    	objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
			String response =  objectMapper.writeValueAsString(badges);
			L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_39, ""+randomLong);
			return new HttpResponse(response, HttpURLConnection.HTTP_OK);
		} catch (SQLException e) {
			
			e.printStackTrace();

			logger.info("SQLException >> " + e.getMessage());
			objResponse.put("message", "Database Error");
			L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
		} catch (JsonProcessingException e) {
			// Object mapper
			e.printStackTrace();

			logger.info("JsonProcessingException >> " + e.getMessage());
			objResponse.put("message", "Failed to parse JSON internally");
			L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
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
	 * Get gamification quests with status for a specific member
	 * @param appId applicationId
	 * @param memberId member id
	 * @param statusId quest status
	 * @return HttpResponse with the returnString
	 */
	@GET
	@Path("/quests/{appId}/{memberId}/status/{statusId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "App Selected"),
			@ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "App not found"),
			@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad Request"),
	})
	@ApiOperation(value = "",
				  notes = "Select an App")
	public HttpResponse getQuestsWithStatusOfMember(
			@ApiParam(value = "Application ID", required = true)@PathParam("appId") String appId,
			@ApiParam(value = "Member ID", required = true)@PathParam("memberId") String memberId,
			@ApiParam(value = "Quest status", required = true)@PathParam("statusId") String statusId)
	{
		long randomLong = new Random().nextLong(); //To be able to match 
		
		JSONObject objResponse = new JSONObject();
		Connection conn = null;

		List<QuestModel> quests = new ArrayList<QuestModel>();
		UserAgent userAgent = (UserAgent) getContext().getMainAgent();
		String name = userAgent.getLoginName();
		if(name.equals("anonymous")){
			return unauthorizedMessage();
		}

		try {
			conn = dbm.getConnection();
			L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_42, ""+randomLong);
			
			if(!managerAccess.isAppIdExist(conn,appId)){
				logger.info("App not found >> ");
				objResponse.put("message", "App not found");
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			if(!managerAccess.isMemberRegistered(conn,memberId)){
				logger.info("Member ID not found >> ");
				objResponse.put("message", "Member ID not found");
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			if(!managerAccess.isMemberRegisteredInApp(conn,memberId,appId)){
				logger.info("Member is not registered in App >> ");
				objResponse.put("message", "Member is not registered in App");
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			// Add Member to App
			if(statusId.equals("REVEALED")||statusId.equals("COMPLETED")){
				quests = memberAccess.getMemberQuestsWithStatus(conn,appId, memberId, QuestStatus.valueOf(statusId));				
			}
			else if(statusId.equals("ALL")){
				quests = memberAccess.getMemberQuests(conn,appId, memberId);				
			}
			else{
				logger.info("Status is not recognized >> ");
				objResponse.put("message", "Quest satus is not recognized");
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			ObjectMapper objectMapper = new ObjectMapper();
	    	//Set pretty printing of json
	    	objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
			String response =  objectMapper.writeValueAsString(quests);
			L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_43, ""+randomLong);
			return new HttpResponse(response, HttpURLConnection.HTTP_OK);
		} catch (SQLException e) {
			
			e.printStackTrace();

			logger.info("SQLException >> " + e.getMessage());
			objResponse.put("message", "Database Error");
			L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
		} catch (JsonProcessingException e) {
			// Object mapper
			e.printStackTrace();

			logger.info("JsonProcessingException >> " + e.getMessage());
			objResponse.put("message", "Failed to parse JSON internally");
			L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
		} catch (IOException e) {
			e.printStackTrace();
			logger.info("IOException >> " + e.getMessage());
			objResponse.put("message", "Error when getting quests ");
			L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
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
	 * Get gamification quests progress for a specific member
	 * @param appId applicationId
	 * @param memberId member id
	 * @param questId quest id
	 * @return HttpResponse with the returnString
	 */
	@GET
	@Path("/quests/{appId}/{memberId}/progress/{questId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "App Selected"),
			@ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "App not found"),
			@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad Request"),
	})
	@ApiOperation(value = "",
				  notes = "Select an App")
	public HttpResponse getQuestProgressOfMember(
			@ApiParam(value = "Application ID", required = true)@PathParam("appId") String appId,
			@ApiParam(value = "Member ID", required = true)@PathParam("memberId") String memberId,
			@ApiParam(value = "Quest ID", required = true)@PathParam("questId") String questId)
	{
		
		JSONObject objResponse = new JSONObject();
		Connection conn = null;

		UserAgent userAgent = (UserAgent) getContext().getMainAgent();
		String name = userAgent.getLoginName();
		if(name.equals("anonymous")){
			return unauthorizedMessage();
		}

		try {
			conn = dbm.getConnection();
			if(!managerAccess.isAppIdExist(conn,appId)){
				logger.info("App not found >> ");
				objResponse.put("message", "App not found");
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			if(!managerAccess.isMemberRegistered(conn,memberId)){
				logger.info("Member ID not found >> ");
				objResponse.put("message", "Member ID not found");
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			if(!managerAccess.isMemberRegisteredInApp(conn,memberId,appId)){
				logger.info("Member is not registered in App >> ");
				objResponse.put("message", "Member is not registered in App");
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			JSONObject outObj = memberAccess.getMemberQuestProgress(conn,appId, memberId, questId);
			return new HttpResponse(outObj.toJSONString(), HttpURLConnection.HTTP_OK);
		} catch (SQLException e) {
			
			e.printStackTrace();

			logger.info("SQLException >> " + e.getMessage());
			objResponse.put("message", "Database Error");
			L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
		} catch (JsonProcessingException e) {
			// Object mapper
			e.printStackTrace();

			logger.info("JsonProcessingException >> " + e.getMessage());
			objResponse.put("message", "Failed to parse JSON internally");
			L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
		} catch (IOException e) {
			e.printStackTrace();
			logger.info("IOException >> " + e.getMessage());
			objResponse.put("message", "Error when getting quests ");
			L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
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
	 * Get gamification achievements for a specific member
	 * @param appId applicationId
	 * @param memberId member id
	 * @return HttpResponse with the returnString
	 */
	@GET
	@Path("/achievements/{appId}/{memberId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "App Selected"),
			@ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "App not found"),
			@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad Request"),
	})
	@ApiOperation(value = "",
				  notes = "Select an App")
	public HttpResponse getAchievementsOfMember(
			@ApiParam(value = "Application ID", required = true)@PathParam("appId") String appId,
			@ApiParam(value = "Member ID", required = true)@PathParam("memberId") String memberId)
	{
		long randomLong = new Random().nextLong(); //To be able to match 
		
		JSONObject objResponse = new JSONObject();
		Connection conn = null;

		List<AchievementModel> ach = new ArrayList<AchievementModel>();
		UserAgent userAgent = (UserAgent) getContext().getMainAgent();
		String name = userAgent.getLoginName();
		if(name.equals("anonymous")){
			return unauthorizedMessage();
		}

		try {
			conn = dbm.getConnection();
			L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_40, ""+randomLong);
			
			if(!managerAccess.isAppIdExist(conn,appId)){
				logger.info("App not found >> ");
				objResponse.put("message", "App not found");
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			if(!managerAccess.isMemberRegistered(conn,memberId)){
				logger.info("Member ID not found >> ");
				objResponse.put("message", "Member ID not found");
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			if(!managerAccess.isMemberRegisteredInApp(conn,memberId,appId)){
				logger.info("Member is not registered in App >> ");
				objResponse.put("message", "Member is not registered in App");
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
				ach = memberAccess.getMemberAchievements(conn,appId, memberId);
				ObjectMapper objectMapper = new ObjectMapper();
		    	//Set pretty printing of json
		    	objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
				String response =  objectMapper.writeValueAsString(ach);
				L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_41, ""+randomLong);
				return new HttpResponse(response, HttpURLConnection.HTTP_OK);

		} catch (SQLException e) {
			
			e.printStackTrace();

			logger.info("SQLException >> " + e.getMessage());
			objResponse.put("message", "Database Error");
			L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
		} catch (JsonProcessingException e) {
			// Object mapper
			e.printStackTrace();

			logger.info("JsonProcessingException >> " + e.getMessage());
			objResponse.put("message", "Failed to parse JSON internally");
			L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
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
	
	// Invoke services in gamification manager service
	
	// Get badge image
	/**
	 * Fetch a badge image with specified ID
	 * @param appId application id
	 * @param badgeId badge id
	 * @param memberId member id
	 * @return HttpResponse with the return image
	 */
	@GET
	@Path("/badges/{appId}/{memberId}/{badgeId}/img")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Badges Entry"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Cannot found image")
	})
	@ApiOperation(value = "",
				  notes = "list of stored badges")
	public HttpResponse getBadgeImageDetail(@PathParam("appId") String appId,
								 @PathParam("badgeId") String badgeId,
@ApiParam(value = "Member ID", required = true)@PathParam("memberId") String memberId)
	{
		JSONObject objResponse = new JSONObject();
		Connection conn = null;

		UserAgent userAgent = (UserAgent) getContext().getMainAgent();
		String name = userAgent.getLoginName();
		if(name.equals("anonymous")){
			return unauthorizedMessage();
		}
		try {
			conn = dbm.getConnection();
			if(!managerAccess.isAppIdExist(conn,appId)){
				logger.info("App not found >> ");
				objResponse.put("message", "App not found");
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			if(!managerAccess.isMemberRegistered(conn,memberId)){
				logger.info("Member ID not found >> ");
				objResponse.put("message", "Member ID not found");
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			if(!managerAccess.isMemberRegisteredInApp(conn,memberId,appId)){
				logger.info("Member is not registered in App >> ");
				objResponse.put("message", "Member is not registered in App");
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			if(!memberAccess.isMemberHasBadge(conn,appId, memberId, badgeId)){
				logger.info("Error. member "+ memberId +" does not have a badge " + badgeId +".");
				objResponse.put("message", "Error. member "+ memberId +" does not have a badge " + badgeId +".");
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			// RMI call with parameters
			byte[] result;
			try {
				result = (byte[]) this.invokeServiceMethod("i5.las2peer.services.gamificationBadgeService.GamificationBadgeService@0.1", "getBadgeImageMethod", (String) appId, (String) badgeId );
				if (result != null) {
					return new HttpResponse(result, HttpURLConnection.HTTP_OK);
				}
			} catch (AgentNotKnownException | L2pServiceException | L2pSecurityException | InterruptedException
					| TimeoutException e) {
				e.printStackTrace();
				logger.info("Error cannot retrieve file " + e.getMessage());
				objResponse.put("message", "Error cannot retrieve file " + e.getMessage());
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);

			}
			logger.info("Error cannot retrieve file ");
			objResponse.put("message", "Error cannot retrieve file ");
			L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);

		} catch (SQLException e) {
			e.printStackTrace();
			logger.info("Database error >>  " + e.getMessage());
			objResponse.put("message", "Database error " + e.getMessage());
			L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);

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
	
	// Get badge information individually
	/**
	 * Get a badge data with specific ID from database
	 * @param appId applicationId
	 * @param badgeId badge id
	 * @param memberId member id
	 * @return HttpResponse Returned as JSON object
	 */
	@GET
	@Path("/badges/{appId}/{memberId}/{badgeId}")
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
	public HttpResponse getBadgeDetailWithId(
			@ApiParam(value = "Application ID")@PathParam("appId") String appId,
			@ApiParam(value = "Badge ID")@PathParam("badgeId") String badgeId,
			@ApiParam(value = "Member ID", required = true)@PathParam("memberId") String memberId)
	{
		JSONObject objResponse = new JSONObject();
		Connection conn = null;

		UserAgent userAgent = (UserAgent) getContext().getMainAgent();
		String name = userAgent.getLoginName();
		if(name.equals("anonymous")){
			return unauthorizedMessage();
		}
		try {
			conn = dbm.getConnection();
			if(!managerAccess.isAppIdExist(conn,appId)){
				logger.info("App not found >> ");
				objResponse.put("message", "App not found");
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			if(!managerAccess.isMemberRegistered(conn,memberId)){
				logger.info("Member ID not found >> ");
				objResponse.put("message", "Member ID not found");
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			if(!managerAccess.isMemberRegisteredInApp(conn,memberId,appId)){
				logger.info("Member is not registered in App >> ");
				objResponse.put("message", "Member is not registered in App");
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			if(!memberAccess.isMemberHasBadge(conn,appId, memberId, badgeId)){
				logger.info("Error. member "+ memberId +" does not have a badge " + badgeId +".");
				objResponse.put("message", "Error. member "+ memberId +" does not have a badge " + badgeId +".");
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			// RMI call with parameters
			String result;
			try {
				result = (String) this.invokeServiceMethod("i5.las2peer.services.gamificationBadgeService.GamificationBadgeService@0.1", "getBadgeWithIdRMI",
						new Serializable[] { appId, badgeId });

				System.out.println("BADGE STRING " + result);
				if (result != null) {
					return new HttpResponse(result, HttpURLConnection.HTTP_OK);
				}
				logger.info("Cannot find badge with " + badgeId);
				objResponse.put("message", "Cannot find badge with " + badgeId);
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);

			} catch (AgentNotKnownException | L2pServiceException | L2pSecurityException | InterruptedException
					| TimeoutException e) {
				e.printStackTrace();
				logger.info("Cannot find badge with " + badgeId + ". " + e.getMessage());
				objResponse.put("message", "Cannot find badge with " + badgeId + ". " + e.getMessage());
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);

			}
		} catch (SQLException e) {
			e.printStackTrace();
			logger.info("Database Error ");
			objResponse.put("message", "Database Error ");
			L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);

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
	
	// Get quest information individually
	/**
	 * Get a quest data with specific ID from database
	 * @param appId applicationId
	 * @param questId quest id
	 * @param memberId member id
	 * @return HttpResponse Returned as JSON object
	 */
	@GET
	@Path("/quests/{appId}/{memberId}/{questId}")
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
	public HttpResponse getQuestDetailWithId(
			@ApiParam(value = "Application ID")@PathParam("appId") String appId,
			@ApiParam(value = "Quest ID")@PathParam("questId") String questId,
			@ApiParam(value = "Member ID", required = true)@PathParam("memberId") String memberId)
	{
		JSONObject objResponse = new JSONObject();
		Connection conn = null;

		UserAgent userAgent = (UserAgent) getContext().getMainAgent();
		String name = userAgent.getLoginName();
		if(name.equals("anonymous")){
			return unauthorizedMessage();
		}
		
		try {
			conn = dbm.getConnection();
			if(!managerAccess.isAppIdExist(conn,appId)){
				logger.info("App not found >> ");
				objResponse.put("message", "App not found");
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			if(!managerAccess.isMemberRegistered(conn,memberId)){
				logger.info("Member ID not found >> ");
				objResponse.put("message", "Member ID not found");
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			if(!managerAccess.isMemberRegisteredInApp(conn,memberId,appId)){
				logger.info("Member is not registered in App >> ");
				objResponse.put("message", "Member is not registered in App");
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			// RMI call with parameters
			String result;
			try {
				result = (String) this.invokeServiceMethod("i5.las2peer.services.gamificationQuestService.GamificationQuestService@0.1", "getQuestWithIdRMI",
						new Serializable[] { appId, questId });
				if (result != null) {
					return new HttpResponse(result, HttpURLConnection.HTTP_OK);
				}
				logger.info("Cannot find badge with " + questId);
				objResponse.put("message", "Cannot find badge with " + questId);
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);

			} catch (AgentNotKnownException | L2pServiceException | L2pSecurityException | InterruptedException
					| TimeoutException e) {
				e.printStackTrace();
				logger.info("Cannot find badge with " + questId + ". " + e.getMessage());
				objResponse.put("message", "Cannot find badge with " + questId + ". " + e.getMessage());
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);

			}
		} catch (SQLException e) {
			e.printStackTrace();
			logger.info("DB Error >> " + e.getMessage());
			objResponse.put("message", "DB Error. " + e.getMessage());
			L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);

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
	
	// Get achievement information individually
	/**
	 * Get an achievement data with specific ID from database
	 * @param appId applicationId
	 * @param achievementId achievement id
	 * @param memberId member id
	 * @return HttpResponse Returned as JSON object
	 */
	@GET
	@Path("/achievements/{appId}/{memberId}/{achievementId}")
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
	public HttpResponse getAchievementDetailWithId(
			@ApiParam(value = "Application ID")@PathParam("appId") String appId,
			@ApiParam(value = "Achievement ID")@PathParam("achievementId") String achievementId,
			@ApiParam(value = "Member ID", required = true)@PathParam("memberId") String memberId)
	{
		JSONObject objResponse = new JSONObject();
		Connection conn = null;

		UserAgent userAgent = (UserAgent) getContext().getMainAgent();
		String name = userAgent.getLoginName();
		if(name.equals("anonymous")){
			return unauthorizedMessage();
		}

		
		try {	
			conn = dbm.getConnection();
			if(!managerAccess.isAppIdExist(conn,appId)){
				logger.info("App not found >> ");
				objResponse.put("message", "App not found");
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			if(!managerAccess.isMemberRegistered(conn,memberId)){
				logger.info("Member ID not found >> ");
				objResponse.put("message", "Member ID not found");
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			if(!managerAccess.isMemberRegisteredInApp(conn,memberId,appId)){
				logger.info("Member is not registered in App >> ");
				objResponse.put("message", "Member is not registered in App");
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			if(!memberAccess.isMemberHasAchievement(conn,appId, memberId, achievementId)){
				logger.info("Error. member "+ memberId +" does not have an achievement " + achievementId +".");
				objResponse.put("message", "Member "+ memberId +" does not have an achievement " + achievementId +".");
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			// RMI call with parameters
			try {
				Object result = this.invokeServiceMethod("i5.las2peer.services.gamificationAchievementService.GamificationAchievementService@0.1", "getAchievementWithIdRMI",
						new Serializable[] { appId, achievementId });
				if (result != null) {
					L2pLogger.logEvent(Event.RMI_SUCCESSFUL, "Get Achievement with ID RMI success");
					return new HttpResponse((String) result, HttpURLConnection.HTTP_OK);
				}
				L2pLogger.logEvent(Event.RMI_FAILED, "Get Achievement with ID RMI failed");
				logger.info("Cannot find achievement with " + achievementId);
				objResponse.put("message", "Cannot find achievement with " + achievementId);
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);

			} catch (AgentNotKnownException | L2pServiceException | L2pSecurityException | InterruptedException
					| TimeoutException e) {
				e.printStackTrace();
				logger.info("Cannot find achievement with " + achievementId + ". " + e.getMessage());
				L2pLogger.logEvent(Event.RMI_FAILED, "Get Achievement with ID RMI failed. " + e.getMessage());
				objResponse.put("message", "Cannot find achievement with " + achievementId + ". " + e.getMessage());
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);

			}

		} catch (SQLException e) {
			e.printStackTrace();
			logger.info("DB Error >> " + e.getMessage());
			objResponse.put("message", "DB Error. " + e.getMessage());
			L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);

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
	 * Trigger an action
	 * @param appId applicationId
	 * @param actionId actionId
	 * @param memberId memberId
	 * @return Notifications in JSON
	 */
	@POST
	@Path("/actions/{appId}/{actionId}/{memberId}")
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
	@ApiOperation(value = "triggerAction",
				 notes = "A method to trigger an ")
	public HttpResponse triggerAction(
			@ApiParam(value = "Application ID", required = true) @PathParam("appId") String appId,
			@ApiParam(value = "Action ID", required = true) @PathParam("actionId") String actionId,
			@ApiParam(value = "Member ID", required = true) @PathParam("memberId") String memberId)  {
		long randomLong = new Random().nextLong(); //To be able to match 
		JSONObject objResponse = new JSONObject();
		Connection conn = null;

		UserAgent userAgent = (UserAgent) getContext().getMainAgent();
		String name = userAgent.getLoginName();
		if(name.equals("anonymous")){
			return unauthorizedMessage();
		}
		try {
			conn = dbm.getConnection();
			L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_38, ""+randomLong);
			if(!managerAccess.isAppIdExist(conn,appId)){
				logger.info("App not found >> ");
				objResponse.put("message", "App not found");
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_BAD_REQUEST);
			}
			if(!managerAccess.isMemberRegistered(conn,memberId)){
				logger.info("Member ID not found >> ");
				objResponse.put("message", "Member ID not found");
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_BAD_REQUEST);
			}
			if(!managerAccess.isMemberRegisteredInApp(conn,memberId,appId)){
				logger.info("Member is not registered in App >> ");
				objResponse.put("message", "Member is not registered in App");
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_BAD_REQUEST);
			}
			
			// RMI call with parameters
			String result;
			try {
				result = (String) this.invokeServiceMethod("i5.las2peer.services.gamificationActionService.GamificationActionService@0.1", "triggerActionRMI",
						new Serializable[] { appId, memberId, actionId });
				if (result != null) {
					L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_39, ""+randomLong);
					L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_44, ""+appId);
					L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_45, ""+memberId);
					
					return new HttpResponse(result, HttpURLConnection.HTTP_OK);
				}
				logger.info("Cannot trigger action " + actionId);
				objResponse.put("message", "Cannot trigger action " + actionId);
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_INTERNAL_ERROR);

			} catch (AgentNotKnownException | L2pServiceException | L2pSecurityException | InterruptedException
					| TimeoutException e) {
				e.printStackTrace();
				logger.info("Cannot trigger action " + actionId + ". " + e.getMessage());
				objResponse.put("message", "Cannot trigger action " + actionId + ". " + e.getMessage());
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_INTERNAL_ERROR);

			}

		} catch (SQLException e) {
			e.printStackTrace();
			logger.info("DB Error >> " + e.getMessage());
			objResponse.put("message", "DB Error. " + e.getMessage());
			L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
			return new HttpResponse(objResponse.toJSONString(),HttpURLConnection.HTTP_INTERNAL_ERROR);

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
	
	// Leaderboard
	/**
	 * Get local leaderboard
	 * @param appId applicationId
	 * @param memberId member id
	 * @param currentPage current cursor page
	 * @param windowSize size of fetched data
	 * @param searchPhrase search word
	 * @return HttpResponse Returned as JSON object
	 */
	@GET
	@Path("/leaderboard/local/{appId}/{memberId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Return local leaderboard"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal Error"),
			@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized")})
	@ApiOperation(value = "Get the local leaderboard", 
				  notes = "Returns a leaderboard array",
				  authorizations = @Authorization(value = "api_key")
				  )
	public HttpResponse getLocalLeaderboard(
			@ApiParam(value = "Application ID")@PathParam("appId") String appId,
			@ApiParam(value = "Member ID", required = true)@PathParam("memberId") String memberId,
			@ApiParam(value = "Page number for retrieving data")@QueryParam("current") int currentPage,
			@ApiParam(value = "Number of data size")@QueryParam("rowCount") int windowSize,
			@ApiParam(value = "Search phrase parameter")@QueryParam("searchPhrase") String searchPhrase)
	{
		long randomLong = new Random().nextLong(); //To be able to match 
		JSONObject objResponse = new JSONObject();
		Connection conn = null;

		UserAgent userAgent = (UserAgent) getContext().getMainAgent();
		String name = userAgent.getLoginName();
		if(name.equals("anonymous")){
			return unauthorizedMessage();
		}
		
		try {
			conn = dbm.getConnection();
			L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_34, ""+randomLong);
			
			if(!managerAccess.isAppIdExist(conn,appId)){
				logger.info("App not found >> ");
				objResponse.put("message", "App not found");
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			if(!managerAccess.isMemberRegistered(conn,memberId)){
				logger.info("Member ID not found >> ");
				objResponse.put("message", "Member ID not found");
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			if(!managerAccess.isMemberRegisteredInApp(conn,memberId,appId)){
				logger.info("Member is not registered in App >> ");
				objResponse.put("message", "Member is not registered in App");
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			
			// Get point name
			// RMI call with parameters
			String pointUnitName = "";
			try {
				pointUnitName = (String) this.invokeServiceMethod("i5.las2peer.services.gamificationPointService.GamificationPointService@0.1", "getUnitNameRMI",
						new Serializable[] { appId, memberId });

				
			} catch (AgentNotKnownException | L2pServiceException | L2pSecurityException | InterruptedException
					| TimeoutException e) {
				e.printStackTrace();
				pointUnitName = "";
			}
			
			int offset = (currentPage - 1) * windowSize;
			int totalNum = memberAccess.getNumberOfMembers(conn,appId);
			JSONArray arrResult = memberAccess.getMemberLocalLeaderboard(conn,appId, offset, windowSize, searchPhrase);
			
			for(int i = 0; i < arrResult.size(); i++){
				JSONObject object = (JSONObject) arrResult.get(i);
				object.replace("pointValue", object.get("pointValue")+" "+pointUnitName);
			}
			
			objResponse.put("current", currentPage);
			objResponse.put("rowCount", windowSize);
			objResponse.put("rows", arrResult);
			objResponse.put("total", totalNum);
			L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_35, ""+randomLong);
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_OK);

		} catch (SQLException e) {
			e.printStackTrace();
			logger.info("DB Error >> " + e.getMessage());
			objResponse.put("message", "DB Error. " + e.getMessage());
			L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);

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
	 * Get global leaderboard
	 * @param appId applicationId
	 * @param memberId member id
	 * @param currentPage current cursor page
	 * @param windowSize size of fetched data
	 * @param searchPhrase search word
	 * @return HttpResponse Returned as JSON object
	 */
	@GET
	@Path("/leaderboard/global/{appId}/{memberId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Return global leaderboard"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal Error"),
			@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized")})
	@ApiOperation(value = "Get the local leaderboard", 
				  notes = "Returns a leaderboard array",
				  authorizations = @Authorization(value = "api_key")
				  )
	public HttpResponse getGlobalLeaderboard(
			@ApiParam(value = "Application ID")@PathParam("appId") String appId,
			@ApiParam(value = "Member ID", required = true)@PathParam("memberId") String memberId,
			@ApiParam(value = "Page number for retrieving data")@QueryParam("current") int currentPage,
			@ApiParam(value = "Number of data size")@QueryParam("rowCount") int windowSize,
			@ApiParam(value = "Search phrase parameter")@QueryParam("searchPhrase") String searchPhrase)
	{
		long randomLong = new Random().nextLong(); //To be able to match 
		JSONObject objResponse = new JSONObject();
		Connection conn = null;

		UserAgent userAgent = (UserAgent) getContext().getMainAgent();
		String name = userAgent.getLoginName();
		if(name.equals("anonymous")){
			return unauthorizedMessage();
		}
		try {
			conn = dbm.getConnection();
			L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_36, ""+randomLong);
			
			if(!managerAccess.isAppIdExist(conn,appId)){
				logger.info("App not found >> ");
				objResponse.put("message", "App not found");
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			if(!managerAccess.isMemberRegistered(conn,memberId)){
				logger.info("Member ID not found >> ");
				objResponse.put("message", "Member ID not found");
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			if(!managerAccess.isMemberRegisteredInApp(conn,memberId,appId)){
				logger.info("Member is not registered in App >> ");
				objResponse.put("message", "Member is not registered in App");
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			
			int offset = (currentPage - 1) * windowSize;
			int totalNum = memberAccess.getNumberOfMembers(conn,appId);
			JSONArray arrResult = memberAccess.getMemberGlobalLeaderboard(conn,appId, offset, windowSize, searchPhrase);
			
			objResponse.put("current", currentPage);
			objResponse.put("rowCount", windowSize);
			objResponse.put("rows", arrResult);
			objResponse.put("total", totalNum);
			L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_37, ""+randomLong);
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_OK);

		} catch (SQLException e) {
			e.printStackTrace();
			logger.info("DB Error >> " + e.getMessage());
			objResponse.put("message", "DB Error. " + e.getMessage());
			L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);

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
	
	@GET
	@Path("/notifications/{appId}/{memberId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Return global leaderboard"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal Error"),
			@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized")})
	@ApiOperation(value = "Get the local leaderboard", 
				  notes = "Returns a leaderboard array",
				  authorizations = @Authorization(value = "api_key")
				  )
	public HttpResponse getNotification(
			@ApiParam(value = "Application ID")@PathParam("appId") String appId,
			@ApiParam(value = "Member ID", required = true)@PathParam("memberId") String memberId,
			@ApiParam(value = "Page number for retrieving data")@QueryParam("current") int currentPage,
			@ApiParam(value = "Number of data size")@QueryParam("rowCount") int windowSize,
			@ApiParam(value = "Search phrase parameter")@QueryParam("searchPhrase") String searchPhrase)
	{
		JSONObject objResponse = new JSONObject();
		Connection conn = null;

		UserAgent userAgent = (UserAgent) getContext().getMainAgent();
		String name = userAgent.getLoginName();
		if(name.equals("anonymous")){
			return unauthorizedMessage();
		}
		
		try {
			conn = dbm.getConnection();
			if(!managerAccess.isAppIdExist(conn,appId)){
				logger.info("App not found >> ");
				objResponse.put("message", "App not found");
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			if(!managerAccess.isMemberRegistered(conn,memberId)){
				logger.info("Member ID not found >> ");
				objResponse.put("message", "Member ID not found");
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			if(!managerAccess.isMemberRegisteredInApp(conn,memberId,appId)){
				logger.info("Member is not registered in App >> ");
				objResponse.put("message", "Member is not registered in App");
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);
			}
			
			JSONArray arrResult = memberAccess.getMemberNotification(conn,appId,memberId);
			
			
			return new HttpResponse(arrResult.toJSONString(), HttpURLConnection.HTTP_OK);

		} catch (SQLException e) {
			e.printStackTrace();
			logger.info("DB Error >> " + e.getMessage());
			objResponse.put("message", "DB Error. " + e.getMessage());
			L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
			return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_BAD_REQUEST);

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

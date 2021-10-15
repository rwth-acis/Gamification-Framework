package i5.las2peer.services.gamification.listener;

import java.net.HttpURLConnection;
import java.sql.Connection;
import java.util.Random;

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

import i5.las2peer.logging.L2pLogger;
import i5.las2peer.restMapper.annotations.ServicePath;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.Contact;
import io.swagger.annotations.Info;
import io.swagger.annotations.License;
import io.swagger.annotations.SwaggerDefinition;
import net.minidev.json.JSONObject;
import i5.las2peer.restMapper.RESTService;
import i5.las2peer.api.security.UserAgent;
import i5.las2peer.api.ManualDeployment;
import i5.las2peer.api.logging.MonitoringEvent;
import i5.las2peer.api.Context;

/**
 * ListenerConfigurator Service
 * 
 * This service is used to configure the LRSListener Service.
 * It uses the LAS2peer Web-Connector for RESTful access to it.
 * 
 * Note:
 * If you plan on using Swagger you should adapt the information below
 * in the ApiInfo annotation to suit your project.
 * If you do not intend to provide a Swagger documentation of your service API,
 * the entire ApiInfo annotation should be removed.
 * 
 */
@Api( value = "/gamification/configurator", authorizations = {
@Authorization(value = "achievements_auth")
})
@SwaggerDefinition(
		info = @Info(
				title = "ListenerConfigurator Service",
				version = "0.1",
				description = "Configurator for Listener Service",
				termsOfService = "http://your-terms-of-service-url.com",
				contact = @Contact(
						name = "Marc Belsch",
						url = "dbis.rwth-aachen.de",
						email = "marc.belsch.rwth-aachen.de"
				),
				license = @License(
						name = "your software license name",
						url = "http://your-software-license-url.com"
				)
		))


@ServicePath("/gamification/configurator")
@SuppressWarnings("unused")
public class ListenerConfigurator extends RESTService{

	private final L2pLogger logger = L2pLogger.getInstance(ListenerConfigurator.class.getName());
	
	// this header is not known to javax.ws.rs.core.HttpHeaders
	public static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";
	public static final String HEADER_CONTENT_TYPE = "Content-Type";
	// non HTTP standard headers
	public static final String HEADER_OWNERID = "ownerid";
	public static final String HEADER_CONTENT_DESCRIPTION = "Content-Description";
	
	/**
	 * Function to return http unauthorized message
	 * @return HTTP response unauthorized
	 */
	private Response unauthorizedMessage(){
		JSONObject objResponse = new JSONObject();
		objResponse.put("message", "You are not authorized");
		Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
		return Response.status(HttpURLConnection.HTTP_UNAUTHORIZED).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
	}
	
	/**
	 * Create a new configuration. 
	 * Name attribute for form data : 
	 * <ul>
	 * 
	 * </ul>
	 * @param configId Config ID obtained from LMS
	 * @param formData Form data with multipart/form-data type
	 * @param contentType Content type (implicitly sent in header)
	 * @return HTTP Response returned as JSON object
	 */
	@POST
	@Path("/{configId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_CREATED, message = "{message:Configuration upload success}"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "{message:Cannot create configuration. Configuration already exist!}"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "{message:Cannot create configuration. Configuration cannot be null!}"),
			
			@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "{message:Cannot create configuration. Failed to upload configuration."),
			@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "{message:You are not authorized")
	})
	@ApiOperation(value = "createNewConfig",
				 notes = "A method to store a new configuration with details (achievement ID, achievement name, achievement description, achievement point value, achievement point id, achievement badge id")
	public Response createNewConfig(
			@ApiParam(value = "Config ID to store a new config", required = true) @PathParam("configId") String configId,
			@ApiParam(value = "Content-type in header", required = true)@HeaderParam(value = HttpHeaders.CONTENT_TYPE) String contentType, 
			@ApiParam(value = "Configuration detail in multiple/form-data type", required = true) byte[] formData)  {
		
		// Request log
		Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_99, "POST " + "gamification/configurator/" + configId, true);
		long randomLong = new Random().nextLong(); //To be able to match 
		
		UserAgent userAgent = (UserAgent) Context.getCurrent().getMainAgent();
		String name = userAgent.getLoginName();
		if(name.equals("anonymous")){
			return unauthorizedMessage();
		}
		return null;
	}
	
	
	/**
	 * Get a configuration with specific ID 
	 * @param configId Config ID obtained from LMS
	 * @param achievementId Achievement id to be obtained
	 * @return HTTP Response returned Config Model {@link ConfigModel} as JSON object
	 * @see AchievementModel
	 */
	@GET
	@Path("/{configId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = ""),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "{message:Cannot get config detail. JSON processing error}"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "{message:Cannot get config. Failed to fetch config}"),
			
			@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "{message:Cannot get config detail. Config not found}"),
			@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "{message:Config Null, Cannot find config with (achievementId)"),
			@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "{message:You are not authorized")
	})
	@ApiOperation(value = "getConfigWithId", 
				  notes = "Get config data with specified ID",
				  response = ConfigModel.class
				  )
	public Response getConfigWithId(
			@ApiParam(value = "Config ID")@PathParam("configId") String configId)
	{
		
		// Request log
		Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_99, "GET " + "gamification/configurator/"+ configId, true);
		long randomLong = new Random().nextLong(); //To be able to match 
		
		UserAgent userAgent = (UserAgent) Context.getCurrent().getMainAgent();
		String name = userAgent.getLoginName();
		if(name.equals("anonymous")){
			return unauthorizedMessage();
		}
		return null;
	}
	
	/**
	 * Update an configuration.
	 * Name attribute for form data : 
	 * <ul>
	 * 
	 * </ul>
	 * @param configId Config ID obtained from LMS
	 * @param formData Form data with multipart/form-data type
	 * @param contentType Content type (implicitly sent in header)
	 * @return HTTP Response returned as JSON object
	 */
	@PUT
	@Path("/{gameId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Config Updated"),
			@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Error occured"),
			@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad request"),
			@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized")
	})
	@ApiOperation(value = "updateConfiguration",
				 notes = "A method to update a configuration with details (achievement ID, achievement name, achievement description, achievement point value, achievement point id, achievement badge id")
	public Response updateConfiguration(
			@ApiParam(value = "Config ID to update a configuration", required = true) @PathParam("gameId") String configId,
			@ApiParam(value = "Configuration data in multiple/form-data type", required = true)@HeaderParam(value = HttpHeaders.CONTENT_TYPE) String contentType, 
			byte[] formData)  {
		
		// Request log
		Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_99, "PUT " + "gamification/achievements/"+configId, true);
		long randomLong = new Random().nextLong(); //To be able to match 
				
		// parse given multipart form data
		JSONObject objResponse = new JSONObject();
		
		UserAgent userAgent = (UserAgent) Context.getCurrent().getMainAgent();
		String name = userAgent.getLoginName();
		if(name.equals("anonymous")){
			return unauthorizedMessage();
		}
		return null;
	}
	
	/**
	 * Delete an configuration data with specified ID
	 * @param configId Config ID obtained from LMS
	 * @return HTTP Response returned as JSON object
	 */
	@DELETE
	@Path("/{configId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(value = {
			@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Config Delete Success"),
			@ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = "Config not found"),
			@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "Bad Request"),
	})
	@ApiOperation(value = "deleteConfiguration",
				  notes = "Delete an configuration")
	public Response deleteAchievement(@PathParam("configId") String configId)
	{
		
		// Request log
		Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_99, "DELETE " + "gamification/achievements/" + configId, true);		
		long randomLong = new Random().nextLong(); //To be able to match 

		Connection conn = null;
		JSONObject objResponse = new JSONObject();
		UserAgent userAgent = (UserAgent) Context.getCurrent().getMainAgent();
		String name = userAgent.getLoginName();
		if(name.equals("anonymous")){
			return unauthorizedMessage();
		}
		return null;
	}
}

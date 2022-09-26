package i5.las2peer.services.gamificationGamifierService;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.util.Random;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.treewalk.TreeWalk;

import i5.las2peer.api.Context;
import i5.las2peer.api.ManualDeployment;
import i5.las2peer.api.logging.MonitoringEvent;
import i5.las2peer.api.security.Agent;
import i5.las2peer.api.security.AnonymousAgent;
import i5.las2peer.api.security.UserAgent;
//import i5.las2peer.logging.L2pLogger;
import i5.las2peer.restMapper.RESTService;
import i5.las2peer.restMapper.annotations.ServicePath;
import i5.las2peer.services.gamificationGamifierService.exception.GitHubException;
import i5.las2peer.services.gamificationGamifierService.helper.RepositoryHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Contact;
import io.swagger.annotations.Info;
import io.swagger.annotations.License;
import io.swagger.annotations.SwaggerDefinition;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import net.minidev.json.parser.ParseException;


/**
 * Gamification Gamifier Service
 * 
 * This is Gamification Gamifier service to manage badge elements in Gamification Framework
 * It uses the LAS2peer Web-Connector for RESTful access to it.
 * 
 * Note:
 * If you plan on using Swagger you should adapt the information below
 * in the ApiInfo annotation to suit your project.
 * If you do not intend to provide a Swagger documentation of your service API,
 * the entire ApiInfo annotation should be removed.
 * 
 */
@Api(tags = "gamifier")
@SwaggerDefinition(
		info = @Info(
				title = "las2peer Example Service",
				version = "0.1",
				description = "A LAS2peer Example Service for demonstration purposes.",
				termsOfService = "http://your-terms-of-service-url.com",
				contact = @Contact(
						name = "John Doe",
						url = "provider.com",
						email = "john.doe@provider.com"
				),
				license = @License(
						name = "your software license name",
						url = "http://your-software-license-url.com"
				)
		))
@ManualDeployment
@ServicePath("/gamification/gamifier")
public class GamificationGamifierService extends RESTService {

//	// instantiate the logger class
//	private final L2pLogger logger = L2pLogger.getInstance(GamificationGamifierService.class.getName());
	/*
	 * Database configuration
	 */

	private String gitHubOrganizationOrigin;
	  
	private String gitHubUserNewRepo;
	private String gitHubUserMailNewRepo;
	private String gitHubOrganizationNewRepo;
	private String gitHubPasswordNewRepo;

	public GamificationGamifierService() {
		// read and set properties values
		// IF THE SERVICE CLASS NAME IS CHANGED, THE PROPERTIES FILE NAME NEED TO BE CHANGED TOO!
		setFieldValues();
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
			 * Function to return http unauthorized message
			 * @return HTTP Response returns JSON object
			 */
			private Response unauthorizedMessage(){
				JSONObject objResponse = new JSONObject();
				objResponse.put("message", "You are not authorized");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, "Not Authorized");
				return Response.status(HttpURLConnection.HTTP_UNAUTHORIZED).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();

			}	
			
			// Get action information 
			/**
			 * Get action data from database via Gamification Action Service
			 * @param gameId gameId
			 * @return HTTP Response returned as JSON object
			 */
			@GET
			@Path("/actions/{gameId}")
			@Produces(MediaType.APPLICATION_JSON)
			@ApiOperation(value = "getActions",
					notes = "Function to get actions from the game.")
			@ApiResponses(value = {
					@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Fetch the actions"),
					@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal Error"),
					@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized")})
			public Response getActions(@ApiParam(value = "Game ID")@PathParam("gameId") String gameId){
				JSONObject objResponse = new JSONObject();
			
				Agent agent = Context.getCurrent().getMainAgent();
				if (agent instanceof AnonymousAgent) {
					return unauthorizedMessage();
				}
				

					// RMI call with parameters
					Object result =null;
					try {
						result = Context.getCurrent().invoke("i5.las2peer.services.gamificationActionService.GamificationActionService@0.1", "getActionsRMI",
								new Serializable[] { gameId });
						if (result != null) {
							Context.getCurrent().monitorEvent(this, MonitoringEvent.RMI_SUCCESSFUL, "Get Actions RMI success");
							return Response.status(HttpURLConnection.HTTP_OK).entity((String) result).type(MediaType.APPLICATION_JSON).build();
						}
						Context.getCurrent().monitorEvent(this, MonitoringEvent.RMI_FAILED, "Get Actions RMI failed");
						objResponse.put("message", "Cannot find actions");
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();

					} catch (Exception e) {
						e.printStackTrace();
						Context.getCurrent().monitorEvent(this, MonitoringEvent.RMI_FAILED, "Get Actions RMI failed. " + e.getMessage());
						objResponse.put("message", "Cannot find Actions. " + e.getMessage());
						Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
						return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();

					}

			}	
			
			/**
			 * Update repository. It consumes JSON data.
			 * Element of consumed JSON object: 
			 * <ul>
			 * 	<li>originRepositoryName - The name of origin application repository
			 *  <li>newRepositoryName - The new repository name of gamified version
			 *  <li>gameId - Game ID
			 *  <li>aopScript - Aspect-Oriented Programming script part that is going to be added in Gamifier.js
			 * </ul>
			 * @param contentB Content Byte JSON
			 * @return HTTP Response Returned as JSON object
			 */
			@SuppressWarnings("resource")
			@POST
			@Path("/repo")
			@Produces(MediaType.APPLICATION_JSON)
			@ApiOperation(value = "updateRepository",
					notes = "Simple function to update repository.")
			@ApiResponses(value = {
					@ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Member is registered"),
					@ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = "Unauthorized"),
					@ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = "User data error to be retrieved"),
					@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Cannot connect to database"),
					@ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "User data error to be retrieved. Not JSON object")
			})
			public Response updateRepository(
					@ApiParam(value = "Data in JSON", required = true) byte[] contentB) {
				// Request log
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_99, "POST " + "gamification/gamifier/repo", true);
				long randomLong = new Random().nextLong(); //To be able to match
				UserAgent userAgent = (UserAgent) Context.getCurrent().getMainAgent();
				// take username as default name
				String name = userAgent.getLoginName();
				System.out.println("User name : " + name);
				if(name.equals("anonymous")){
					return unauthorizedMessage();
				}
				
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_9, "" + randomLong, true);

				JSONObject objResponse = new JSONObject();
				String content = new String(contentB);
				if(content.equals(null)){
					objResponse.put("message", "Cannot update repository. Cannot parse json data into string");
					//Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
					Context.getCurrent().monitorEvent(this, MonitoringEvent.AGENT_UPLOAD_FAILED, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
				}
				
				
				JSONObject obj;
				String originRepositoryName;
				String newRepositoryName;
				String gameId;
				String epURL;
				String aopScript;
				
				try {
					obj = (JSONObject) JSONValue.parseWithException(content);
					originRepositoryName = stringfromJSON(obj,"originRepositoryName");
					newRepositoryName = stringfromJSON(obj,"newRepositoryName");
					gameId = stringfromJSON(obj,"gameId");
					epURL = stringfromJSON(obj,"epURL");
					aopScript = stringfromJSON(obj,"aopScript");
				} catch (ParseException e) {
					e.printStackTrace();
					objResponse.put("message", "Cannot update repository. Cannot parse json data into string. " + e.getMessage());
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
				} catch (IOException e) {
					e.printStackTrace();		
					objResponse.put("message", "Cannot update repository. Cannot parse json data into string. " + e.getMessage());
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
				}
				// check if repo exist
				TreeWalk treeWalk = null;
				Repository newRepository = null;	
				Repository originRepository = null;			
				      
				// helper variables
			    // variables holding content to be modified and added to repository later
			    String widget = null;
				try {
					RepositoryHelper.deleteRemoteRepository(newRepositoryName, gitHubOrganizationNewRepo, gitHubUserNewRepo, gitHubPasswordNewRepo);
				} catch (GitHubException e) {
					//e.printStackTrace();		
				}
				
			    try {

					PersonIdent caeUser = new PersonIdent(gitHubUserNewRepo, gitHubUserMailNewRepo);
					
					originRepository = RepositoryHelper.getRemoteRepository(originRepositoryName, gitHubOrganizationOrigin);
					newRepository = RepositoryHelper.generateNewRepository(newRepositoryName, gitHubOrganizationNewRepo, gitHubUserNewRepo, gitHubPasswordNewRepo);
					File originDir = originRepository.getDirectory();
			        // now load the TreeWalk containing the origin repository content
					treeWalk = RepositoryHelper.getRepositoryContent(originRepositoryName, gitHubOrganizationOrigin);
				
					 //System.out.println("PATH " + treeWalk.getPathString());
					 System.out.println("PATH2 " + originDir.getParent());
					 System.out.println("PATH3 " + newRepository.getDirectory().getParent());
			        // treeWalk.setFilter(PathFilter.create("frontend/"));
				    ObjectReader reader = treeWalk.getObjectReader();
				    // walk through the tree and retrieve the needed templates
			    	while (treeWalk.next()) {
						ObjectId objectId = treeWalk.getObjectId(0);
						ObjectLoader loader = reader.open(objectId);
							switch (treeWalk.getNameString()) {
					            case "widget.xml":
					              widget = new String(loader.getBytes(), "UTF-8");
					              break;
							}
			        }
				    	
			    	// replace widget.xml 
					//widget = createWidgetCode(widget, htmlElementTemplate, yjsImports, gitHubOrganization, repositoryName, frontendComponent);
					widget = RepositoryHelper.appendWidget(widget, gitHubOrganizationNewRepo, newRepositoryName);
			    	
					RepositoryHelper.copyFolder(originRepository.getDirectory().getParentFile(), newRepository.getDirectory().getParentFile());
			    	
					String aopfilestring = RepositoryHelper.readFile("../GamificationGamifierService/jsfiles/aop.pack.js", Charset.forName("UTF-8"));
					String oidcwidgetfilestring = RepositoryHelper.readFile("../GamificationGamifierService/jsfiles/oidc-widget.js", Charset.forName("UTF-8"));
					String gamifierstring = RepositoryHelper.readFile("../GamificationGamifierService/jsfiles/gamifier.js", Charset.forName("UTF-8"));
					
					gamifierstring = gamifierstring.replace("$Game_Id$", gameId);
					gamifierstring = gamifierstring.replace("$Endpoint_URL$", epURL);
					gamifierstring = gamifierstring.replace("$AOP_Script$", aopScript);
					
					// add files to new repository
					newRepository = RepositoryHelper.createTextFileInRepository(newRepository, "", "widget.xml", widget);
					newRepository = RepositoryHelper.createTextFileInRepository(newRepository, "gamification/", "aop.pack.js", aopfilestring);
					newRepository = RepositoryHelper.createTextFileInRepository(newRepository, "gamification/", "oidc-widget.js", oidcwidgetfilestring);
					newRepository = RepositoryHelper.createTextFileInRepository(newRepository, "gamification/", "gamifier.js", gamifierstring);

					 // stage file
				    Git.wrap(newRepository).add().addFilepattern(".").call();
					   
					// commit files
					Git.wrap(newRepository).commit()
					.setMessage("Generated new repo  ")
					.setCommitter(caeUser).call();
					
					// push (local) repository content to GitHub repository "gh-pages" branch
					RepositoryHelper.pushToRemoteRepository(newRepository, gitHubUserNewRepo, gitHubPasswordNewRepo, "master", "gh-pages");


			      // close all open resources
			    } catch (GitHubException e1) {
					e1.printStackTrace();		
					objResponse.put("message", "Cannot update repository. Github exception. " + e1.getMessage());
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
				} catch (IOException e1) {
					e1.printStackTrace();
					objResponse.put("message", "Cannot update repository. Github exception. " + e1.getMessage());
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
				} catch (Exception e) {
			        objResponse.put("message", "Cannot update repository. Github exception. " + e.getMessage());
					Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
					return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();
				}
			    finally {
					if (newRepository != null) {
						newRepository.close();
					}
					if (originRepository != null) {
						originRepository.close();
					}
					if (treeWalk != null) {
						treeWalk.close();
					}
				}
			  
				objResponse.put("message", "Updated");
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_10, "" + randomLong, true);
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_22, "" + gameId, true);
				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_23, "" + name, true);
				return Response.status(HttpURLConnection.HTTP_OK).entity(objResponse.toJSONString()).type(MediaType.APPLICATION_JSON).build();

			}
	  //}

}

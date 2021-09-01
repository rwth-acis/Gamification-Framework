package i5.las2peer.services.gamificationGamifierService;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.util.Random;
import java.util.logging.Level;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.treewalk.TreeWalk;


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
import i5.las2peer.services.gamificationGamifierService.helper.RepositoryHelper;
import i5.las2peer.services.gamificationGamifierService.exception.GitHubException;
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
import net.minidev.json.JSONValue;
import net.minidev.json.parser.ParseException;

// TODO Describe your own service
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
// TODO Adjust the following configuration
@Path("/gamification/gamifier")
@Version("0.1") // this annotation is used by the XML mapper
@Api
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
// TODO Your own Serviceclass
public class GamificationGamifierService extends RESTService {

	// instantiate the logger class
	private final L2pLogger logger = L2pLogger.getInstance(GamificationGamifierService.class.getName());
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

	
	@Override
	  protected void initResources() {
	    getResourceConfig().register(Resource.class);
	  }

	  @Path("/") // this is the root resource
	  public static class Resource {
	    // put here all your service methods
		  
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
			 * @return HTTP response unauthorized
			 */
			private HttpResponse unauthorizedMessage(){
				JSONObject objResponse = new JSONObject();
				objResponse.put("message", "You are not authorized");
				L2pLogger.logEvent(this, Event.SERVICE_ERROR, "Not Authorized");
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_UNAUTHORIZED);

			}	
			
			// Get action information 
			/**
			 * Get action data from database via Gamification Action Service
			 * @param gameId gameId
			 * @return HttpResponse Returned as JSON object
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
			public HttpResponse getActions(@ApiParam(value = "Game ID")@PathParam("gameId") String gameId){
				JSONObject objResponse = new JSONObject();
				UserAgent userAgent = (UserAgent) getContext().getMainAgent();
				String name = userAgent.getLoginName();
				if(name.equals("anonymous")){
					return unauthorizedMessage();
				}

					// RMI call with parameters
					try {
						Object result = this.invokeServiceMethod("i5.las2peer.services.gamificationActionService.GamificationActionService@0.1", "getActionsRMI",
								new Serializable[] { gameId });
						if (result != null) {
							L2pLogger.logEvent(Event.RMI_SUCCESSFUL, "Get Actions RMI success");
							return new HttpResponse((String) result, HttpURLConnection.HTTP_OK);
						}
						L2pLogger.logEvent(Event.RMI_FAILED, "Get Actions RMI failed");
						objResponse.put("message", "Cannot find actions");
						L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
						return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);

					} catch (AgentNotKnownException | L2pServiceException | L2pSecurityException | InterruptedException
							| TimeoutException e) {
						e.printStackTrace();
						L2pLogger.logEvent(Event.RMI_FAILED, "Get Actions RMI failed. " + e.getMessage());
						objResponse.put("message", "Cannot find Actions. " + e.getMessage());
						L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
						return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);

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
			 * @return HttpResponse Returned as JSON object
			 */
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
			public HttpResponse updateRepository(
					@ApiParam(value = "Data in JSON", required = true)@ContentParam byte[] contentB) {
				// Request log
				L2pLogger.logEvent( Event.SERVICE_CUSTOM_MESSAGE_99,getContext().getMainAgent(), "POST " + "gamification/gamifier/repo");
				long randomLong = new Random().nextLong(); //To be able to match
				UserAgent userAgent = (UserAgent) getContext().getMainAgent();
				// take username as default name
				String name = userAgent.getLoginName();
				System.out.println("User name : " + name);
				if(name.equals("anonymous")){
					return unauthorizedMessage();
				}
				
				L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_9,getContext().getMainAgent(), "" + randomLong);

				JSONObject objResponse = new JSONObject();
				String content = new String(contentB);
				if(content.equals(null)){
					objResponse.put("message", "Cannot update repository. Cannot parse json data into string");
					//L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
					L2pLogger.logEvent(this, Event.AGENT_UPLOAD_FAILED, (String) objResponse.get("message"));
					return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
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
					//fileContent = stringfromJSON(obj,"fileContent");
					gameId = stringfromJSON(obj,"gameId");
					epURL = stringfromJSON(obj,"epURL");
					aopScript = stringfromJSON(obj,"aopScript");
				} catch (ParseException e) {
					e.printStackTrace();
					objResponse.put("message", "Cannot update repository. Cannot parse json data into string. " + e.getMessage());
					L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
					return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
				} catch (IOException e) {
					e.printStackTrace();		
					objResponse.put("message", "Cannot update repository. Cannot parse json data into string. " + e.getMessage());
					L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
					return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
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
					// TODO Auto-generated catch block
					e1.printStackTrace();		
					objResponse.put("message", "Cannot update repository. Github exception. " + e1.getMessage());
					L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
					return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
				} catch (IOException e1) {
					e1.printStackTrace();
					objResponse.put("message", "Cannot update repository. Github exception. " + e1.getMessage());
					L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
					return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
				} catch (Exception e) {
			        objResponse.put("message", "Cannot update repository. Github exception. " + e.getMessage());
					L2pLogger.logEvent(this, Event.SERVICE_ERROR, (String) objResponse.get("message"));
					return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_INTERNAL_ERROR);
				}
			    finally {
				  newRepository.close();
				  originRepository.close();
			      treeWalk.close();
			    }
			  
				objResponse.put("message", "Updated");
				L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_10,getContext().getMainAgent(), "" + randomLong);
				L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_22,getContext().getMainAgent(), "" + gameId);
				L2pLogger.logEvent(Event.SERVICE_CUSTOM_MESSAGE_23,getContext().getMainAgent(), "" + name);
				
				return new HttpResponse(objResponse.toJSONString(), HttpURLConnection.HTTP_OK);

			}
	  }

}

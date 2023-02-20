package i5.las2peer.services.gamificationDevopsService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import i5.las2peer.api.Context;
import i5.las2peer.api.ManualDeployment;
import i5.las2peer.api.execution.InternalServiceException;
import i5.las2peer.api.execution.ServiceAccessDeniedException;
import i5.las2peer.api.execution.ServiceInvocationFailedException;
import i5.las2peer.api.execution.ServiceMethodNotFoundException;
import i5.las2peer.api.execution.ServiceNotAuthorizedException;
import i5.las2peer.api.execution.ServiceNotAvailableException;
import i5.las2peer.api.execution.ServiceNotFoundException;
import i5.las2peer.api.logging.MonitoringEvent;
import i5.las2peer.apiTestModel.BodyAssertion;
import i5.las2peer.apiTestModel.BodyAssertionOperator;
import i5.las2peer.apiTestModel.RequestAssertion;
import i5.las2peer.apiTestModel.ResponseBodyOperator;
import i5.las2peer.apiTestModel.StatusCodeAssertion;
import i5.las2peer.apiTestModel.TestCase;
import i5.las2peer.apiTestModel.TestRequest;
import i5.las2peer.connectors.ConnectorException;
import i5.las2peer.connectors.webConnector.client.ClientResponse;
import i5.las2peer.restMapper.RESTService;
import i5.las2peer.restMapper.annotations.ServicePath;
import i5.las2peer.services.gamificationDevopsService.database.DatabaseManager;
import i5.las2peer.services.gamificationDevopsService.helper.Pair;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Contact;
import io.swagger.annotations.Info;
import io.swagger.annotations.License;
import io.swagger.annotations.SwaggerDefinition;
import rice.p2p.util.Base64;

/**
 * las2peer-Template-Service
 * 
 * This is a template for a very basic las2peer service that uses the las2peer WebConnector for RESTful access to it.
 * 
 * Note: If you plan on using Swagger you should adapt the information below in the SwaggerDefinition annotation to suit
 * your project. If you do not intend to provide a Swagger documentation of your service API, the entire Api and
 * SwaggerDefinition annotation should be removed.
 * 
 */
@Api
@SwaggerDefinition(
		info = @Info(
				title = "las2peer Gamification Devops Service",
				version = "1.0.0",
				description = "A las2peer Service for gamifying a devops model.",
				contact = @Contact(
						name = "David Almeida",
						email = "david.almeida@rwth-aachen.com"),
				license = @License(
						name = "your software license name",
						url = "http://your-software-license-url.com")))
@ManualDeployment
@ServicePath("gamification/devops")
public class GamificationDevopsService extends RESTService{


	public static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";

	private String jdbcDriverClassName;
	private String jdbcLogin;
	private String jdbcPass;	
	private String jdbcUrl;
	private String jdbcSchema;
	private DatabaseManager gamificationDb;

	private String devopsServices;


	public GamificationDevopsService(){
		setFieldValues(); // This sets the values of the configuration file

		gamificationDb = new DatabaseManager(jdbcDriverClassName, jdbcLogin
				, jdbcPass, jdbcUrl, jdbcSchema);
	}

	
	public Response handleGamifyTestsRequest(String gameId,
			String member,
			String contentType,byte[] formData){
		Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_CUSTOM_MESSAGE_99, "POST " + "gamification/devops/"+gameId, true);

		GamificationDevopsServiceState.create(devopsServices);
		
		
		JSONObject objResponse = new JSONObject();

		//execute the tests. if a test is successful execute its action with the given member

		//add user to the game if he does not exist
		//		if(!GamificationDevopsServiceState.getInstance().getGameIds().contains(gameId)) {
		//			ClientResponse response = GamificationDevopsServiceState.getInstance().sendRequest("POST"
		//					, "gamification/games/data/" + gameId + "/" + GAMIFICATION_MEMBER_ID);
		//			if(response.getHttpCode() != 200) {
		//				objResponse.put("message", response.getResponse());
		//				Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
		//				return Response.serverError().entity(objResponse).build();
		//			}
		//		}

		List<String> failedTestActions = new LinkedList<>(); //actions that failed because not all tests have passsed
		List<Integer> failedTestAssertions = new LinkedList<>(); //tests that failed its assertions

		try {
			String json = new String(formData, StandardCharsets.UTF_8);
			JSONParser parser = new JSONParser();
			JSONObject data = (JSONObject)parser.parse(json);

			//[{test:bytes,gamificationObject:map},...]

			String boundary =  "--32532twtfaweafwsgfaegfawegf4"; 
			String gamificationContentType = "multipart/form-data; boundary=" + boundary;
			Map<String, String> gamificationHeaders = new HashMap<>();

			gamificationHeaders.put("Accept-Encoding","gzip, deflate");
			gamificationHeaders.put("Accept-Language","en-GB,en-US;q=0.8,en;q=0.6");

			JSONArray jsonArray = (JSONArray)parser.parse((String)data.get("allTests"));
			for(Object aux: jsonArray) {
				JSONObject testContentJson = (JSONObject)aux;

				TestCase test = (TestCase)deserialize
						(Base64.decode((String)testContentJson.get("test")));
				Map<String,String> gamificationObject = (Map<String,String>)testContentJson.get("gamificationObject");				

				String actionId = gamificationObject.get("actionid");
				double rating = (double)Double.parseDouble(testContentJson.get("starRating") + "") / 5;
				String scope = (String)testContentJson.get("scope");


				boolean noErrors = true;
				for(TestRequest request: test.getRequests()) {
					String url = request.getUrl();

					JSONObject params = request.getPathParams();
					Object[] args = new Object[params.size()];
					int i = 0;
					for(Object value: params.values())
						args[i++] = value;

					//						Pair<Integer,String> result = httpClient.sendGETRequest(url);
					String testBoundary = request.getPathParams().get("boundary") != null ?
							(String) request.getPathParams().get("boundary") : "";
					Map<String,String> headers = request.getPathParams().get("request") != null ?
							(Map<String,String>)request.getPathParams().get("request") : new HashMap<>();
					String testContent = request.getPathParams().get("content") != null ?
							(String)request.getPathParams().get("content") : "";
					String testContentType = request.getPathParams().get("contentType") != null ?
							(String) request.getPathParams().get("contentType") : "";

					ClientResponse testResult = GamificationDevopsServiceState.getInstance()
							.sendRequest(request.getType(), 
									request.getUrl(), testContent, testContentType, headers);

					Pair<Integer,String> result = new Pair<Integer,String>(testResult.getHttpCode(),
							testResult.getResponse());
					for(RequestAssertion ass: request.getAssertions()) {
						if(!solveRequestAssertion(ass,result)) {
							noErrors = false;
							break;
						}
					}
				}

				if(noErrors) { //if the has passed trigger its action
					//first insert the action then trigger it

					//create the gamification object
					MultipartEntityBuilder builder = MultipartEntityBuilder.create();
					builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
					builder.setBoundary(boundary);

					for(String key: gamificationObject.keySet()) {
						String value = gamificationObject.get(key); 
						if(key.equals("actionpointvalue")) {
							value = (int)(Float.parseFloat(value) * rating) + "";
						}
						builder.addPart(key,new StringBody(value, ContentType.TEXT_PLAIN));
					}
					HttpEntity entity = builder.build();
					ByteArrayOutputStream out = new ByteArrayOutputStream();

					entity.writeTo(out);

					ClientResponse response = GamificationDevopsServiceState.getInstance().sendRequest("POST"
							,"gamification/actions/" + gameId, out.toString(), gamificationContentType, gamificationHeaders);
					if(response.getHttpCode() == 201) {
						Context.getCurrent().invoke("i5.las2peer.services.gamificationActionService.GamificationActionService"
								, "triggerActionRMI", gameId, member, actionId);	
						try(PreparedStatement stmn = gamificationDb.getConnection().prepareStatement("INSERT INTO " + gameId + ".devops_model VALUES(?,?,?,?,?)")){
							stmn.setString(1, scope);
							stmn.setString(2, actionId);
							stmn.setDouble(3, rating * 5);
							stmn.setString(4, gameId);
							stmn.setString(5, member);
							if(stmn.executeUpdate() <= 0) {
								//added successfully
								objResponse.put("message", "All tests passed and were all gamified however their "
										+ "info could not be added to the gamification database");
								
								return Response.ok().entity(objResponse).build();
							}
						}catch(SQLException e) {
							e.printStackTrace();
						}
					}else {
						failedTestActions.add(actionId);
					}
				}else {
					failedTestAssertions.add(test.getId());
				}
			}

		} catch (ParseException | IOException | ClassNotFoundException | ServiceNotFoundException | ServiceNotAvailableException | InternalServiceException | ServiceMethodNotFoundException | ServiceInvocationFailedException | ServiceAccessDeniedException | ServiceNotAuthorizedException e) {
			e.printStackTrace();
			objResponse.put("message", e.getMessage());
			Context.getCurrent().monitorEvent(this, MonitoringEvent.SERVICE_ERROR, (String) objResponse.get("message"));
			return Response.serverError().entity(objResponse).build();
		}

		if(failedTestActions.isEmpty() && failedTestAssertions.isEmpty())
			objResponse.put("message", "All tests passed and were all gamified");
		else {
			StringBuilder sb = new StringBuilder("Some tests associated with the actions: ");
			failedTestActions.stream().forEach(action -> sb.append(action + ","));
			sb.append(" have failed.");

			sb.append("\nSome tests assertions have failed: ");
			failedTestAssertions.stream().forEach(test -> sb.append(test + ","));

			sb.append(".\nThe rest were successfully gamified");
			objResponse.put("message", sb.toString());
			return Response.serverError().entity(objResponse).build();
		}
		return Response.ok().entity(objResponse).build();
	}

	/**
	 * Adds a success awareness model to be gamified.
	 * 
	 * @param gameId the id of the game
	 * @param member the member that is gamifying
	 * @return Returns an HTTP response with 200 if everything was performed successfully, or 500 if something 
	 * unexpected happened.
	 */
	@POST
	@Path("/{gameId}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiResponses(
			value = { @ApiResponse(
					code = HttpURLConnection.HTTP_OK,
					message = "Added the gamified successfully") })
	@ApiOperation(
			value = "addTests",
			notes = "This method gamifies the given tests")
	public Response gamifyTests(@PathParam("gameId") String gameId,
			@ApiParam(value = "member") @QueryParam("member") String member,
			@ApiParam(value = "Achievement data in multiple/form-data type", required = true)
	@HeaderParam(value = HttpHeaders.CONTENT_TYPE) String contentType, 
	byte[] formData) {
		Response res = handleGamifyTestsRequest(gameId,member,contentType,formData);
		try {
			GamificationDevopsServiceState.getInstance().stop();
		} catch (ConnectorException e1) {
			e1.printStackTrace();
		}
		return res;
	}

	private static boolean solveRequestAssertion(RequestAssertion ass, Pair<Integer,String> result) {
		//since the request assertion code doesn't actually contain the code to to compare the results
		//it must be done manually...
		if(ass == null)
			return false;

		if(ass instanceof StatusCodeAssertion) {
			StatusCodeAssertion statusCodeAssertion = (StatusCodeAssertion)ass;
			int statusCode = result.getFirst();

			switch(statusCodeAssertion.getComparisonOperator()) {
			case StatusCodeAssertion.COMPARISON_OPERATOR_EQUALS:
				return statusCodeAssertion.getStatusCodeValue() == statusCode;
			}
		}else {
			//it is a body assertion
			BodyAssertion bodyAssertion = (BodyAssertion)ass;
			BodyAssertionOperator current = bodyAssertion.getOperator();

			String response = result.getSecond();
			JSONParser parser = new JSONParser();

			/*
			 * HAS_TYPE: Whether the body (or field value) has a specific type (e.g., if it is a JSONObject, a Number, ...)
			 * HAS_FIELD: Whether it contains a field (e.g., if the JSONObject contains a field "id") 
			 * 
			 * HAS_LIST_ENTRY_THAT:
			 * Whether it is a list and contains an entry of a specific type
			 * Whether it is a list and contains an entry that contains a specific field
			 * 
			 * ALL_LIST_ENTRIES:
			 * Whether it is a list and all entries have a specific type
			 * Whether it is a list and all entries contain a specific field
			 */
			boolean currentResult = false;
			while(current != null) {
				try {
					if(current.getOperatorId() == ResponseBodyOperator.HAS_TYPE.getId()) {
						currentResult = hasType(((JSONObject)parser.parse(response)).values(),current.getInputType());			
					}else if(current.getOperatorId() == ResponseBodyOperator.HAS_FIELD.getId()) {
						currentResult = hasField((JSONObject)parser.parse(response),current.getInputValue());
					}else if(current.getOperatorId() == ResponseBodyOperator.HAS_LIST_ENTRY_THAT.getId()) {
						Object aux = parser.parse(response);
						if(!(aux instanceof JSONArray)) {
							currentResult = false;
						}else {
							JSONArray array = (JSONArray)aux;
							if(!hasType((Collection<Object>) array.stream().collect(Collectors.toList()),current.getInputType())) {
								for(Object obj: array) {
									try {
										if(hasField((JSONObject)obj,current.getInputValue())) {
											currentResult = true;
											break;
										}
									}catch(ClassCastException e) {
										continue;
									}
								}
							}else {
								currentResult = true;
							}
						}
					}else {
						Object aux = parser.parse(response);
						if(!(aux instanceof JSONArray)) {
							currentResult = false;
						}else {
							JSONArray array = (JSONArray)aux;
							boolean everyEntryHasField = true;
							for(Object obj: array) {
								try {
									if(!hasField((JSONObject)obj,current.getInputValue())) {
										everyEntryHasField = false;
										break;
									}
								}catch(ClassCastException e) {
									everyEntryHasField = false;
									break;
								}
							}
							boolean everyEntryHasType = true;
							if(!everyEntryHasField) { //not every entry has certain field. check if every entry is of a certain type 
								for(Object obj: array) {
									if(classNameToInputType(obj.getClass().getSimpleName()) != current.getInputType()) {
										everyEntryHasType = false;
										break;
									}
								}
							}
							currentResult = everyEntryHasType || everyEntryHasField;
						}
					}
				}catch (Exception e){
					currentResult = false;
				}
				if(!currentResult)
					return false;
				current = current.getFollowingOperator();
			}
			return true; //currentResult was always true
		}
		return false;
	}


	private static boolean hasField(JSONObject jsonObj, String field) {
		return jsonObj.containsKey(field);
	}

	private static boolean hasType(Collection<Object> jsonObj, int type) {
		for(Object value: jsonObj) {
			if(classNameToInputType(value.getClass().getSimpleName()) == type)
				return true;
		}
		return false;
	}

	private static int classNameToInputType(String className) {
		switch (className) {
		case "JSONObject":
			return 2;
		case "JSONArray":
			return 3;
		case "String":
			return 4;
		case "Number":
			return 5;
		case "Boolean":
			return 6;
		default:
			return -1;
		}
	}


	private static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		ObjectInputStream is = new ObjectInputStream(in);
		return is.readObject();
	}

	public DatabaseManager getGamificationDatabase() {
		return gamificationDb;
	}

}

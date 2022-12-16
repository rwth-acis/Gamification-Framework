package i5.las2peer.services.gamificationDevopsService;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.postgresql.shaded.com.ongres.scram.common.bouncycastle.base64.Base64;

import i5.las2peer.api.p2p.ServiceNameVersion;
import i5.las2peer.apiTestModel.BodyAssertion;
import i5.las2peer.apiTestModel.BodyAssertionOperator;
import i5.las2peer.apiTestModel.ResponseBodyOperator;
import i5.las2peer.apiTestModel.StatusCodeAssertion;
import i5.las2peer.apiTestModel.TestCase;
import i5.las2peer.apiTestModel.TestRequest;
import i5.las2peer.connectors.webConnector.WebConnector;
import i5.las2peer.connectors.webConnector.client.ClientResponse;
import i5.las2peer.connectors.webConnector.client.MiniClient;
import i5.las2peer.p2p.PastryNodeImpl;
import i5.las2peer.security.UserAgentImpl;
import i5.las2peer.services.gamificationActionService.GamificationActionService;
import i5.las2peer.services.gamificationDevopsService.database.DatabaseManager;
import i5.las2peer.services.gamificationGameService.GamificationGameService;
import i5.las2peer.testing.MockAgentFactory;
import i5.las2peer.testing.TestSuite;

/**
 *
 * Tests for the Monitoring Data Provision Service. Mostly just prints out results of method invocations, since it is
 * not predictable which data is stored at the time these tests are run.
 *
 * @author Peter de Lange
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GamificationDevopsServiceTest {

	private static final int HTTP_PORT = WebConnector.DEFAULT_HTTP_PORT;

	public static PastryNodeImpl node;
	private static WebConnector connector;
	private static MiniClient c1;
	private static UserAgentImpl user1;
	private static UserAgentImpl templateAgent;
	private ByteArrayOutputStream logStream;

	private static final String adamsPass = "adamspass";

	private static GamificationDevopsService service;

	private static final String GAME_ID = "test";
	private static final String ACTION_ID = "action_test_2";
	private static final String MEASURE_NAME = "Gamified Messages";
	private static final String MEMBER_NAME = "MemberSuccessTest";


	private static final String TESTING_URL = "gamification/devopstest/test";
	
	@Before
	public void startServer() throws Exception {
		// start Node
		node = TestSuite.launchNetwork(1).get(0);
		user1 = MockAgentFactory.getAdam();
		user1.unlock(adamsPass);
		node.storeAgent(user1);


		// start service
		ServiceNameVersion gamificationServiceName = new ServiceNameVersion(GamificationDevopsService.class.getName(), "0.1"); 
		node.startService(gamificationServiceName, "a pass");
		service = (GamificationDevopsService)node.getLocalServiceAgent(gamificationServiceName).getServiceInstance();

		node.startService(new ServiceNameVersion(GamificationActionService.class.getName(), "0.1"), "a pass");
		node.startService(new ServiceNameVersion(GamificationGameService.class.getName(), "0.1"), "a pass");
	
		// start connector
		logStream = new ByteArrayOutputStream();
		connector = new WebConnector(true, HTTP_PORT, false, 1000);
		connector.setLogStream(new PrintStream(logStream));
		connector.start(node);
		
		Thread.sleep(1000); // wait a second for the connector to become ready

		c1 = new MiniClient();
		c1.setConnectorEndpoint(connector.getHttpEndpoint());
		c1.setLogin(user1.getIdentifier(), "adamspass");
	}

	@After
	public void shutDownServer() throws Exception {

		if (connector != null) {
			connector.stop();
		}
		if (node != null) {
			node.shutDown();
		}

		connector = null;
		node = null;

		System.out.println("Connector-Log:");
		System.out.println("--------------");

		if (logStream != null) {
			System.out.println(logStream.toString());
		}
	}

	@Test
	public void test1AddTests() {
		try {			
			/*
			 * CREATE A NEW GAME
			 */

			String boundary =  "--32532twtfaweafwsgfaegfawegf4"; 

			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
			builder.setBoundary(boundary);


			builder.addPart("gameid", new StringBody(GAME_ID, ContentType.TEXT_PLAIN));
			builder.addPart("gamedesc", new StringBody("New Game", ContentType.TEXT_PLAIN));
			builder.addPart("commtype", new StringBody("com_type", ContentType.TEXT_PLAIN));

			HttpEntity formData = builder.build();
			ByteArrayOutputStream out = new ByteArrayOutputStream();

			formData.writeTo(out);

			Map<String, String> headers = new HashMap<>();

			headers.put("Accept-Encoding","gzip, deflate");
			headers.put("Accept-Language","en-GB,en-US;q=0.8,en;q=0.6");

			ClientResponse result = c1.sendRequest("POST", "gamification/games/data", out.toString(), "multipart/form-data; boundary="+boundary, "*/*", headers);

			Assert.assertEquals(201, result.getHttpCode());

			//add member to game
			int res =  service.getGamificationDatabase().getConnection().createStatement().executeUpdate("INSERT INTO manager.member_info (member_id,first_name,last_name,email) "
					+ "VALUES (\'" + MEMBER_NAME + "\',\'" + "test" + "\',\'" + "test" + "\',\'" + "test@test.com" + "\')"
					+ "ON CONFLICT DO NOTHING");
			Assert.assertEquals(1, res);
			result = c1.sendRequest("POST"
					, "gamification/games/data/" + GAME_ID + "/" + MEMBER_NAME,"");
			Assert.assertEquals(200, result.getHttpCode());
			
			//add the member to the game
			result = c1.sendRequest("POST"
					, "gamification/games/data/" + GAME_ID + "/" + MEMBER_NAME,"");
			Assert.assertEquals(result.getHttpCode(), 200);			

			/*
			 * ADD the gamification elements
			 */

			JSONArray allTests = new JSONArray();
			
			JSONObject testingObj = new JSONObject();
			StatusCodeAssertion ass = new StatusCodeAssertion(StatusCodeAssertion.COMPARISON_OPERATOR_EQUALS, 200);
//			BodyAssertion ass2 = new BodyAssertion(-1,-1,-1
//					,new BodyAssertionOperator(-1, ResponseBodyOperator.HAS_TYPE.getId(), -1,4 , null, null));
//			BodyAssertion ass3 = new BodyAssertion(-1,-1,-1
//					,new BodyAssertionOperator(-1, ResponseBodyOperator.HAS_FIELD.getId(), -1,4,"test", null));
			BodyAssertionOperator followingOperator = new BodyAssertionOperator(-1, ResponseBodyOperator.ALL_LIST_ENTRIES.getId(), -1,2,"test", null);
			BodyAssertion ass4 = new BodyAssertion(-1,-1,-1
					,new BodyAssertionOperator(-1, ResponseBodyOperator.HAS_LIST_ENTRY_THAT.getId(), -1,4,"test", followingOperator));
			
			//			TestRequest request = new TestRequest("GET", "http://kubernetes.docker.internal:8080/gamification/devops/test?member=MemberSuccessTest",
//					Arrays.asList(ass));
			
			
			JSONObject pathParams = new JSONObject();
			pathParams.put("boundary", boundary);
			pathParams.put("contentType", "multipart/form-data");
			pathParams.put("headers", headers);
			
			TestRequest request = new TestRequest("GET", "gamification/devopstest/test",
					pathParams,-1,null,Arrays.asList(ass,ass4));
			TestCase test = new TestCase("test", Arrays.asList(request));
			
			ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
		    ObjectOutputStream os = new ObjectOutputStream(byteArrayOut);
		    os.writeObject(test);
						
			testingObj.put("test",Base64.toBase64String(byteArrayOut.toByteArray()));

			//	0-5 star rating
			Map<String,String> action = new HashMap<>();
			action.put("actionid", ACTION_ID);
			action.put("actionname", "action name");
			action.put("actiondesc", "action description");
			action.put("actionpointvalue", "50");
			action.put("actionnotificationcheck", "true");
			action.put("actionnotificationmessage", "This is notification message");
			testingObj.put("gamificationObject", action);
			testingObj.put("starRating", 1.5);
			testingObj.put("scope", "test");
			
			allTests.add(testingObj);
			
			JSONObject toSend = new JSONObject();
			toSend.put("agentId", user1.getIdentifier());
			toSend.put("allTests", allTests.toJSONString());
			
			
			headers = new HashMap<>();

			headers.put("Accept-Encoding","gzip, deflate");
			headers.put("Accept-Language","en-GB,en-US;q=0.8,en;q=0.6");
			headers.put("Content-Type","application/json");


//			Field f = c1.getClass().getDeclaredField("serverAddress");
//			f.setAccessible(true);
//			System.out.println(f.get(c1));
//			
//			URL url = new URL(String.format("%s/%s", f.get(c1), "gamification/devops/" + GAME_ID + "?member=" + MEMBER_NAME));
//			System.out.println(url.getPath());
//			
//			f = c1.getClass().getDeclaredField("authorization");
//			f.setAccessible(true);
//			System.out.println(f.get(c1));
			
			
			result = c1.sendRequest("POST", "gamification/devops/" + GAME_ID + "?member=" + MEMBER_NAME, toSend.toJSONString(),
					"multipart/form-data; boundary="+boundary,"*/*",headers);
			Assert.assertEquals(200, result.getHttpCode());

			//			make the service listen
			//			result = c1.sendRequest("POST", "successawarenessmodel/listen", "");
			System.out.println("Result of 'testGet': " + result.getResponse().trim());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.toString());
		}
	}	
	
	
	@AfterClass
	public static void cleanDB() {
		DatabaseManager gamificationDb = service.getGamificationDatabase();
		DatabaseManager modelPersistenceDb = service.getModelPersistenceDatabase();


		try(Connection conn = gamificationDb.getConnection()) {	
			Statement stmn = conn.createStatement();
			stmn.executeUpdate("DELETE FROM manager.game_info WHERE game_id=\'"+ GAME_ID + "\'");
			stmn.executeUpdate("DELETE FROM manager.member_game WHERE game_id=\'"+ GAME_ID + "\'");
			stmn.executeUpdate("DELETE FROM manager.member_info WHERE member_id=\'"+ MEMBER_NAME  + "\'");
			stmn.executeUpdate("DELETE FROM manager.member_info WHERE member_id=\'"+ GamificationDevopsService.GAMIFICATION_MEMBER_ID  + "\'");	
			stmn.executeUpdate("DELETE FROM " + GAME_ID + ".member WHERE member_id=\'"+ MEMBER_NAME  + "\'");
			stmn.executeUpdate("DELETE FROM " + GAME_ID + ".member WHERE member_id=\'"+ GamificationDevopsService.GAMIFICATION_MEMBER_ID  + "\'");
			stmn.executeUpdate("DELETE FROM " + GAME_ID + ".success_awareness_gamified_measure WHERE game_id=\'" + GAME_ID + "\'");
			stmn.executeUpdate("DROP SCHEMA " + GAME_ID + " CASCADE");
			
			stmn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		System.out.println("clean db...");
	}

}

package i5.las2peer.services.gamificationSuccessAwarenessModelService;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;

import i5.las2peer.api.p2p.ServiceNameVersion;
import i5.las2peer.connectors.webConnector.WebConnector;
import i5.las2peer.connectors.webConnector.client.ClientResponse;
import i5.las2peer.connectors.webConnector.client.MiniClient;
import i5.las2peer.p2p.PastryNodeImpl;
import i5.las2peer.security.ServiceAgentImpl;
import i5.las2peer.security.UserAgentImpl;
import i5.las2peer.services.gamificationActionService.GamificationActionService;
import i5.las2peer.services.gamificationGameService.GamificationGameService;
import i5.las2peer.services.gamificationSuccessAwarenessModelService.database.DatabaseManager;
import i5.las2peer.services.mobsos.dataProcessing.MobSOSDataProcessingService;
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
public class GamificationSuccessAwarenessModelServiceTest {

	private static final int HTTP_PORT = WebConnector.DEFAULT_HTTP_PORT;

	public static PastryNodeImpl node;
	private static WebConnector connector;
	private static MiniClient c1;
	private static UserAgentImpl user1;
	private static UserAgentImpl templateAgent;
	private ByteArrayOutputStream logStream;

	private static final String adamsPass = "adamspass";

	private static GamificationSuccessAwarenessModelService service;
	private static final ServiceNameVersion daataProcessingServiceClass = new ServiceNameVersion(
			MobSOSDataProcessingService.class.getCanonicalName(), "0.8.3");

	private static final String GAME_ID = "test2";
	private static final String ACTION_ID = "action_test_2";
	private static final String MEASURE_NAME = "Gamified Messages";
	private static final String MEMBER_NAME = "MemberSuccessTest";

	private ServiceAgentImpl dataProcessingService;

	@Before
	public void startServer() throws Exception {
		// start Node
		node = TestSuite.launchNetwork(1).get(0);
		user1 = MockAgentFactory.getAdam();
		user1.unlock(adamsPass);
		node.storeAgent(user1);


		/*
		 * Data processing service
		 */
		dataProcessingService = ServiceAgentImpl.createServiceAgent(daataProcessingServiceClass, "a pass");
		dataProcessingService.unlock("a pass");
		node.registerReceiver(dataProcessingService);



		// start service
		ServiceNameVersion gamificationServiceName = new ServiceNameVersion(GamificationSuccessAwarenessModelService.class.getName(), "0.1"); 
		node.startService(gamificationServiceName, "a pass");
		service = (GamificationSuccessAwarenessModelService)node.getLocalServiceAgent(gamificationServiceName).getServiceInstance();

		node.startService(new ServiceNameVersion(GamificationActionService.class.getName(), "0.1"), "a pass");
		node.startService(new ServiceNameVersion(GamificationGameService.class.getName(), "0.1"), "a pass");

		//add to the service when built - add to read.me
//		node.addObserver((GamificationSuccessAwarenessModelService)service);

		// start connector
		logStream = new ByteArrayOutputStream();
		connector = new WebConnector(true, 8764, false, 1000);
		connector.setLogStream(new PrintStream(logStream));

		connector.start(node);

		Thread.sleep(2000); // wait a second for the connector to become ready

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

	private final static String sNode = "1234567891011";
	private final static String dNode = "1234567891022";
	private final static String sAgent = "c4ca4238a0b923820dcc509a6f75849b"; // md5 for 1


	private static String readFile(String file) throws FileNotFoundException{
		try(Scanner sc = new Scanner(new File(file))){
			StringBuilder sb = new StringBuilder();
			while(sc.hasNextLine()) {
				sb.append(sc.nextLine() + " ");
			}
			if(sb.length() > 1)
				sb.deleteCharAt(sb.length() - 1);
			return sb.toString();
		} catch (FileNotFoundException e) {
			throw e;
		}
	}


	@Test
	public void test1AddSuccessAwareness() {
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

			/*
			 * ADD the gamification elements
			 */

			JSONObject jsonObj = new JSONObject();
			jsonObj.put("catalog", readFile("measure_catalogs/myGroupID/measure_catalog-mysql.xml"));

			Map<String, Map<String,Object>> content = new HashMap<>();
			jsonObj.put("content", content);

			Map<String,Object> action = new HashMap<>();
			content.put("Gamified Messages", action);
			action.put("service", "action");
			action.put("valueToTrigger", "100");

			Map<String,String> actionGamificationObject = new HashMap<>();

			actionGamificationObject.put("actionid", ACTION_ID);
			actionGamificationObject.put("actionname", "action name");
			actionGamificationObject.put("actiondesc", "action description");
			actionGamificationObject.put("actionpointvalue", "50");
			actionGamificationObject.put("actionnotificationcheck", "true");
			actionGamificationObject.put("actionnotificationmessage", "This is notification message");

			action.put("gamificationObject", actionGamificationObject);

			headers = new HashMap<>();

			headers.put("Accept-Encoding","gzip, deflate");
			headers.put("Accept-Language","en-GB,en-US;q=0.8,en;q=0.6");
			headers.put("Content-Type","application/json");


			builder = MultipartEntityBuilder.create();
			builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
			builder.setBoundary(boundary);

			for(String key: actionGamificationObject.keySet()) {
				builder.addPart(key,new StringBody(actionGamificationObject.get(key), ContentType.TEXT_PLAIN));
			}

			HttpEntity data = builder.build();
			out = new ByteArrayOutputStream();

			data.writeTo(out);

			System.out.println(jsonObj.toJSONString());
			
			result = c1.sendRequest("POST", "gamification/successawarenessmodel/setup", "",
					"multipart/form-data; boundary="+boundary,"*/*",headers);
			Assert.assertEquals(200, result.getHttpCode());
			
			result = c1.sendRequest("POST", "gamification/successawarenessmodel/" + GAME_ID + "?member=" + MEMBER_NAME, jsonObj.toJSONString(),
					"multipart/form-data; boundary="+boundary,"*/*",headers);
			Assert.assertEquals(200, result.getHttpCode());
			
			System.out.println("Result of 'testGet': " + result.getResponse().trim());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.toString());
		}
	}	

	@Test
	public void test2DeleteSuccessAwarenessMeasure() {
		try {
			ClientResponse result = c1.sendRequest("DELETE", "gamification/successawarenessmodel/" + GAME_ID 
					+ "/" + MEASURE_NAME.replace(" ", "%20"),"");
			Assert.assertEquals(200, result.getHttpCode());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.toString());
		}
	}

	@Test
	public void test3DeleteSuccessAwarenessGame() {
		try {
			ClientResponse result = c1.sendRequest("DELETE", "gamification/successawarenessmodel/" + GAME_ID ,"");
			Assert.assertEquals(200, result.getHttpCode());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.toString());
		}
	}

	@Test
	public void test4DeleteSuccessAwarenessMember() {
		try {
			ClientResponse result = c1.sendRequest("DELETE", "gamification/successawarenessmodel/member/" + GAME_ID ,"");
			Assert.assertEquals(200, result.getHttpCode());
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.toString());
		}
	}
	
	@AfterClass
	public static void cleanDB() {
		DatabaseManager gamificationDb = service.getGamificationDatabase();
		DatabaseManager successAwarenessModeldb = service.getSuccessAwarenessModelDatabase();


		try(Connection conn = gamificationDb.getConnection()) {	
			Statement stmn = conn.createStatement();
			stmn.executeUpdate("DELETE FROM manager.game_info WHERE game_id=\'"+ GAME_ID + "\'");
			stmn.executeUpdate("DELETE FROM manager.member_game WHERE game_id=\'"+ GAME_ID + "\'");
			stmn.executeUpdate("DELETE FROM manager.member_info WHERE member_id=\'"+ MEMBER_NAME  + "\'");
			stmn.executeUpdate("DELETE FROM manager.member_info WHERE member_id=\'"+ GamificationSuccessAwarenessModelService.GAMIFICATION_MEMBER_ID  + "\'");	
			stmn.executeUpdate("DELETE FROM " + GAME_ID + ".member WHERE member_id=\'"+ MEMBER_NAME  + "\'");
			stmn.executeUpdate("DELETE FROM " + GAME_ID + ".member WHERE member_id=\'"+ GamificationSuccessAwarenessModelService.GAMIFICATION_MEMBER_ID  + "\'");
			stmn.executeUpdate("DELETE FROM " + GAME_ID + ".success_awareness_gamified_measure WHERE game_id=\'" + GAME_ID + "\'");
			stmn.executeUpdate("DROP SCHEMA " + GAME_ID + " CASCADE");
			
			stmn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		try(Connection conn = successAwarenessModeldb.getConnection()) {	
			Statement stmn = conn.createStatement();
			stmn.executeUpdate("DELETE FROM message");
			stmn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		System.out.println("clean db...");
	}

}

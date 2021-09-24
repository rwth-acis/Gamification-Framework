package i5.las2peer.services.gamificationAchievementService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import i5.las2peer.p2p.LocalNode;
import i5.las2peer.p2p.LocalNodeManager;
import i5.las2peer.api.p2p.ServiceNameVersion;
import i5.las2peer.api.security.ServiceAgent;
import i5.las2peer.security.UserAgentImpl;
import i5.las2peer.services.gamificationAchievementService.GamificationAchievementService;
import i5.las2peer.testing.MockAgentFactory;
import i5.las2peer.connectors.webConnector.WebConnector;
import i5.las2peer.connectors.webConnector.client.ClientResponse;
import i5.las2peer.connectors.webConnector.client.MiniClient;
import net.minidev.json.JSONObject;

/**
 * Example Test Class demonstrating a basic JUnit test structure.
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GamificationAchievementServiceTest {

	private static final String HTTP_ADDRESS = "http://127.0.0.1";
//	private static final int HTTP_PORT = WebConnector.DEFAULT_HTTP_PORT;
	private static final int HTTP_PORT = 8081;
	
	private static LocalNode node;
	private static WebConnector connector;
	private static ByteArrayOutputStream logStream;

	private static MiniClient c1, c2, c3, ac;

	private static UserAgentImpl user1, user2, user3;// anon;
	
//	 during testing, the specified service version does not matter
//	private static final ServiceNameVersion testGamificationAchievementService = new ServiceNameVersion(GamificationAchievementService.class.getCanonicalName(),"0.1");

	private static String gameId = "test";
	private static String badgeId = "badge1";
	private static String achievementId = "ach_test_id";
	private static final String mainPath = "gamification/achievements/";
	
	// to fetch data per batch
	int currentPage = 1;
	int windowSize = 10;
	String searchParam = "";
	
	String unitName = "dollar";
	/**
	 * Called before the tests start.
	 * 
	 * Sets up the node and initializes connector and users that can be used throughout the tests.
	 * 
	 * @throws Exception
	 */
	@Before
	public void startServer() throws Exception {

		// start node
		//node = LocalNode.newNode();
		
		node = new LocalNodeManager().newNode();
		node.launch();
		
		user1 = MockAgentFactory.getAdam();
		user2 = MockAgentFactory.getAbel();
		user3 = MockAgentFactory.getEve();
//		anon = MockAgentFactory.getAnonymous();
		
		user1.unlock("adamspass"); // agent must be unlocked in order to be stored 
		user2.unlock("abelspass");
		user3.unlock("evespass");
		
//		JSONObject user1Data = new JSONObject();
//		user1Data.put("given_name", "Adam");
//		user1Data.put("family_name", "Jordan");
//		user1Data.put("email", "adam@example.com");
//		user1.setUserData(user1Data);
//		
//		JSONObject user2Data = new JSONObject();
//		user2Data.put("given_name", "Abel");
//		user2Data.put("family_name", "leba");
//		user2Data.put("email", "abel@example.com");
//		user2.setUserData(user2Data);
//		
//		JSONObject user3Data = new JSONObject();
//		user3Data.put("given_name", "Eve");
//		user3Data.put("family_name", "vev");
//		user3Data.put("email", "eve@example.com");
//		user3.setUserData(user3Data);
		
		node.storeAgent(user1);
		node.storeAgent(user2);
		node.storeAgent(user3);

		
//		ServiceAgent testService = ServiceAgent.createServiceAgent(testGamificationAchievementService, "a pass");
//		testService.unlockPrivateKey("a pass");
//		node.registerReceiver(testService);
		node.startService(new ServiceNameVersion(GamificationAchievementService.class.getName(), "0.1"), "a pass");
		
		// start connector
		logStream = new ByteArrayOutputStream();

		connector = new WebConnector(true, HTTP_PORT, false, 1000);
		connector.setLogStream(new PrintStream(logStream));
		connector.start(node);
		Thread.sleep(1000); // wait a second for the connector to become ready

		// legacy
//		connector.updateServiceList();
		
		c1 = new MiniClient();
		c1.setConnectorEndpoint(connector.getHttpEndpoint());
		c1.setLogin(user1.getIdentifier(), "adamspass");
		
		c2 = new MiniClient();
		c2.setConnectorEndpoint(connector.getHttpEndpoint());
		c2.setLogin(user2.getIdentifier(), "abelspass");

		c3 = new MiniClient();
		c3.setConnectorEndpoint(connector.getHttpEndpoint());
		c3.setLogin(user3.getIdentifier(), "evespass");

//		ac = new MiniClient();
//		ac.setConnectorEndpoint(connector.getHttpEndpoint());

// legacy		
//		// avoid timing errors: wait for the repository manager to get all services before continuing
//		try
//		{
//			System.out.println("waiting..");
//			Thread.sleep(10000);
//		} catch (InterruptedException e)
//		{
//			e.printStackTrace();
//		}

	}

	/**
	 * Called after the test has finished. Shuts down the server and prints out the connector log file for reference.
	 * 
	 * @throws Exception
	 */
	@After
	public void shutDownServer() throws Exception {
		if (connector != null) {
			connector.stop();
			connector = null;
		}
		if (node != null) {
			node.shutDown();
			node = null;
		}
		if (logStream != null) {
			System.out.println("Connector-Log:");
			System.out.println("--------------");
			System.out.println(logStream.toString());
			logStream = null;
		}
	}

	
	// Achievement Test
	@Test
	public void testC1_createNewAchievement(){
		System.out.println("Test --- Create New Achievement");
		// Depend on badge
		try
		{
			
			String boundary =  "--32532twtfaweafwsgfaegfawegf4"; 
			
			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
			builder.setBoundary(boundary);
			
			builder.addPart("achievementid", new StringBody(achievementId, ContentType.TEXT_PLAIN));
			builder.addPart("achievementname", new StringBody("achievement", ContentType.TEXT_PLAIN));
			builder.addPart("achievementdesc", new StringBody("achievement description", ContentType.TEXT_PLAIN));
			builder.addPart("achievementpointvalue", new StringBody("50", ContentType.TEXT_PLAIN));
			builder.addPart("achievementbadgeid", new StringBody(badgeId, ContentType.TEXT_PLAIN));

			builder.addPart("achievementnotificationcheck", new StringBody("true", ContentType.TEXT_PLAIN));
			builder.addPart("achievementnotificationmessage", new StringBody("This is notification message", ContentType.TEXT_PLAIN));
			HttpEntity formData = builder.build();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			
			formData.writeTo(out);
		
			Map<String, String> headers = new HashMap<>();
			
			headers.put("Accept-Encoding","gzip, deflate");
			headers.put("Accept-Language","en-GB,en-US;q=0.8,en;q=0.6");
			
			ClientResponse result = c1.sendRequest("POST", mainPath + "" + gameId, out.toString(), "multipart/form-data; boundary="+boundary, "*/*", headers);

			System.out.println(result.getResponse());
			if(result.getHttpCode()==HttpURLConnection.HTTP_OK){
				assertEquals(HttpURLConnection.HTTP_OK,result.getHttpCode());
			}
			else{

				assertEquals(HttpURLConnection.HTTP_CREATED,result.getHttpCode());
			}
		} catch (Exception e)
		{
			e.printStackTrace();
			System.out.println(e.getMessage());
			
			fail("Exception: " + e);
			System.exit(0);
		}
	}
	
	@Test
	public void testC2_getAchievementWithId(){

		System.out.println("Test --- Get Achievement With Id");
		try
		{
			ClientResponse result = c1.sendRequest("GET",  mainPath + "" + gameId + "/" + achievementId, "");
	        assertEquals(200, result.getHttpCode());
		} catch (Exception e)
		{
			e.printStackTrace();
			fail("Exception: " + e);
			System.exit(0);
		}

	}
	
	@Test
	public void testC2_updateAchievement(){

		System.out.println("Test --- Update Achievement");
		try
		{
			String boundary =  "--32532twtfaweafwsgfaegfawegf4"; 
			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
			builder.setBoundary(boundary);
			
			builder.addPart("achievementid", new StringBody(achievementId, ContentType.TEXT_PLAIN));
			builder.addPart("achievementname", new StringBody("achievement", ContentType.TEXT_PLAIN));
			builder.addPart("achievementdesc", new StringBody("achievement description", ContentType.TEXT_PLAIN));
			builder.addPart("achievementpointvalue", new StringBody("50", ContentType.TEXT_PLAIN));
			builder.addPart("achievementbadgeid", new StringBody(badgeId, ContentType.TEXT_PLAIN));
				

			builder.addPart("achievementnotificationcheck", new StringBody("true", ContentType.TEXT_PLAIN));
			builder.addPart("achievementnotificationmessage", new StringBody("This is notification message", ContentType.TEXT_PLAIN));
			
			HttpEntity formData = builder.build();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			
			formData.writeTo(out);
		
			Map<String, String> headers = new HashMap<>();
			headers.put("Accept-Encoding","gzip, deflate");
			headers.put("Accept-Language","en-GB,en-US;q=0.8,en;q=0.6");
			
			ClientResponse result = c1.sendRequest("PUT", mainPath + "" + gameId +"/"+ achievementId, out.toString(), "multipart/form-data; boundary="+boundary, "*/*", headers);

			System.out.println(result.getResponse());
			assertEquals(HttpURLConnection.HTTP_OK,result.getHttpCode());
			
		} catch (Exception e)
		{
			e.printStackTrace();
			System.out.println(e.getMessage());
			
			fail("Exception: " + e);
			System.exit(0);
		}
	}
	

	
	/**
	 * 
	 * get achievement list
	 * 
	 */
	@Test
	public void testC2_getAchievementList()
	{
		System.out.println("Test --- Get Achievement List");
		try
		{
			ClientResponse result = c1.sendRequest("GET", mainPath + "" + gameId + "?current=1&rowCount=10&searchPhrase=", "");
	        assertEquals(200, result.getHttpCode());
		} catch (Exception e)
		{
			e.printStackTrace();
			fail("Exception: " + e);
			System.exit(0);
		}
	
	}

	//------- CLEAN UP --------------

	@Test
	public void testZ6_deleteAchievement(){
		System.out.println("Test --- Delete Achievement");
		try
		{
			ClientResponse result = c1.sendRequest("DELETE",  mainPath + "" + gameId + "/" + achievementId, "");
	        assertEquals(200, result.getHttpCode());
		} catch (Exception e)
		{
			e.printStackTrace();
			fail("Exception: " + e);
			System.exit(0);
		}
	}

	
	
// legacy	
//	/**
//	 * Test the TemplateService for valid rest mapping.
//	 * Important for development.
//	 */
//	@Test
//	public void testDebugMapping()
//	{
//		GamificationAchievementService cl = new GamificationAchievementService();
//		assertTrue(cl.debugMapping());
//	}

}

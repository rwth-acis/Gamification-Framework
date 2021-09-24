package i5.las2peer.services.gamificationBadgeService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
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
import i5.las2peer.services.gamificationBadgeService.GamificationBadgeService;
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
public class GamificationBadgeServiceTest {

	private static final String HTTP_ADDRESS = "http://127.0.0.1";
//	private static final int HTTP_PORT = WebConnector.DEFAULT_HTTP_PORT;
	private static final int HTTP_PORT = 8081;
	
	private static LocalNode node;
	private static WebConnector connector;
	private static ByteArrayOutputStream logStream;

	private static MiniClient c1, c2, c3, ac;
	
	private static UserAgentImpl user1, user2, user3;// anon;

//	// during testing, the specified service version does not matter
//	private static final ServiceNameVersion testGamificationBadgeService = new ServiceNameVersion(GamificationBadgeService.class.getCanonicalName(),"0.1");

	private static String gameId = "test";
	private static String badgeId = "badge_test_id";
	private static final String mainPath = "gamification/badges/";
	
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

		
//		ServiceAgent testService = ServiceAgent.createServiceAgent(testGamificationBadgeService, "a pass");
//		testService.unlockPrivateKey("a pass");
//		node.registerReceiver(testService);
		node.startService(new ServiceNameVersion(GamificationBadgeService.class.getName(), "0.1"), "a pass");
		
		// start connector
		logStream = new ByteArrayOutputStream();

		connector = new WebConnector(true, HTTP_PORT, false, 1000);
		connector.setLogStream(new PrintStream(logStream));
		connector.start(node);
		Thread.sleep(1000); // wait a second for the connector to become ready

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

	// Badge Test --------------------------------------------------
	@Test
	public void testB1_createNewBadge(){
		System.out.println("Test --- Create New Badge");
		try
		{
			File badgeImage = new File("../GamificationBadgeService/files/logo.png");
			String boundary =  "----WebKitFormBoundaryuK41JdjQK2kdEBDn"; 
			
			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
			builder.setBoundary(boundary);
			
			builder.addPart("badgeid", new StringBody(badgeId, ContentType.TEXT_PLAIN));
			builder.addPart("badgename", new StringBody("Badge name", ContentType.TEXT_PLAIN));
			builder.addPart("badgedesc", new StringBody("Badge description", ContentType.TEXT_PLAIN));
			builder.addPart("badgeimageinput", new FileBody(badgeImage, ContentType.create("image/png"), "logo.png"));
			builder.addPart("dev", new StringBody("yes", ContentType.TEXT_PLAIN));

			builder.addPart("badgenotificationcheck", new StringBody("true", ContentType.TEXT_PLAIN));
			builder.addPart("badgenotificationmessage", new StringBody("This is notification message", ContentType.TEXT_PLAIN));
		
			HttpEntity formData = builder.build();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			
			formData.writeTo(out);
			Map<String, String> headers = new HashMap<>();
			headers.put("1", "");
			headers.put("2", "");

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
	
	/**
	 * update badge
	 * 
	 */
	@Test
	public void testB2_updateBadge(){
		System.out.println("Test --- Update Badge");
		try
		{
			File badgeImage = new File("../GamificationBadgeService/files/logo.png");
			String boundary =  "--32532twtfaweafwsgfaegfawegf4"; 
			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
			builder.setBoundary(boundary);
			
			builder.addPart("badgeid", new StringBody(badgeId, ContentType.TEXT_PLAIN));
			builder.addPart("badgename", new StringBody("Badge name", ContentType.TEXT_PLAIN));
			builder.addPart("badgedesc", new StringBody("Badge description", ContentType.TEXT_PLAIN));
			builder.addPart("badgeimageinput", new FileBody(badgeImage, ContentType.create("image/png"), "logo.png"));
			builder.addPart("dev", new StringBody("yes", ContentType.TEXT_PLAIN));


			builder.addPart("badgenotificationcheck", new StringBody("true", ContentType.TEXT_PLAIN));
			builder.addPart("badgenotificationmessage", new StringBody("This is notification message", ContentType.TEXT_PLAIN));
			HttpEntity formData = builder.build();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			
			formData.writeTo(out);

			ClientResponse result = c1.sendRequest("PUT", mainPath + "" + gameId +"/" + badgeId, out.toString(), "multipart/form-data; boundary="+boundary, "*/*",new HashMap<String, String>());
			
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
	
	@Test
	public void testB2_getBadgeWithId(){
		System.out.println("Test --- Get Badge With Id");
		try
		{
			ClientResponse result = c1.sendRequest("GET",  mainPath + "" + gameId + "/" + badgeId, "");
	        assertEquals(200, result.getHttpCode());
		} catch (Exception e)
		{
			e.printStackTrace();
			fail("Exception: " + e);
			System.exit(0);
		}
	}
	
	/**
	 * 
	 * get badge list
	 * 
	 */
	@Test
	public void testB2_getBadgeList()
	{
		System.out.println("Test --- Get Badge List");
		try
		{
			JSONObject obj = new JSONObject();
			obj.put("current", 1);
			obj.put("rowCount", 10);
			obj.put("searchPhrase", "");
			ClientResponse result = c1.sendRequest("GET", mainPath + "" + gameId + "?current=1&rowCount=10&searchPhrase=", "");
	        assertEquals(200, result.getHttpCode());
		} catch (Exception e)
		{
			e.printStackTrace();
			fail("Exception: " + e);
			System.exit(0);
		}
	
	}
	
	/**
	 * 
	 * get badge image
	 * 
	 */
	@Test
	public void testB2_getBadgeImage()
	{
		System.out.println("Test --- Get Badge Image");
		try
		{
			ClientResponse result = c1.sendRequest("GET", mainPath + "" + gameId + "/" + badgeId + "/img", "");
	        assertEquals(200, result.getHttpCode());
		} catch (Exception e)
		{
			e.printStackTrace();
			fail("Exception: " + e);
			System.exit(0);
		}
	}
	
	

	@Test
	public void testZ7_deleteBadge()
	{
		System.out.println("Test --- Delete Badge");
		try
		{
			ClientResponse result = c1.sendRequest("DELETE",  mainPath + "" + gameId + "/" + badgeId, "");
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
//	public void testDebugMapgameg()
//	{
//		GamificationBadgeService cl = new GamificationBadgeService();
//		assertTrue(cl.debugMapping());
//	}

}
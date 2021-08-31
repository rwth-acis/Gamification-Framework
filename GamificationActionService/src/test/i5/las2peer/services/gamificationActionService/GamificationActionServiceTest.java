package i5.las2peer.services.gamificationActionService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;

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
import i5.las2peer.p2p.ServiceNameVersion;
import i5.las2peer.restMapper.data.Pair;
import i5.las2peer.security.ServiceAgent;
import i5.las2peer.security.UserAgent;
import i5.las2peer.services.gamificationActionService.GamificationActionService;
import i5.las2peer.testing.MockAgentFactory;
import i5.las2peer.webConnector.WebConnector;
import i5.las2peer.webConnector.client.ClientResponse;
import i5.las2peer.webConnector.client.MiniClient;
import net.minidev.json.JSONObject;
/**
 * Example Test Class demonstrating a basic JUnit test structure.
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GamificationActionServiceTest {

	private static final String HTTP_ADDRESS = "http://127.0.0.1";
//	private static final int HTTP_PORT = WebConnector.DEFAULT_HTTP_PORT;
	private static final int HTTP_PORT = 8081;
	
	private static LocalNode node;
	private static WebConnector connector;
	private static ByteArrayOutputStream logStream;

	private static MiniClient c1, c2, c3, ac;

	private static UserAgent user1, user2, user3, anon;

	// during testing, the specified service version does not matter
	private static final ServiceNameVersion testGamificationActionService = new ServiceNameVersion(GamificationActionService.class.getCanonicalName(),"0.1");

	private static String gameId = "test";
	private static String actionId = "action_test_2";

	private static final String mainPath = "gamification/actions/";
	
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
	@BeforeClass
	public static void startServer() throws Exception {

		// start node
		node = LocalNode.newNode();
		
		user1 = MockAgentFactory.getAdam();
		user2 = MockAgentFactory.getAbel();
		user3 = MockAgentFactory.getEve();
		anon = MockAgentFactory.getAnonymous();
		
		user1.unlockPrivateKey("adamspass"); // agent must be unlocked in order to be stored 
		user2.unlockPrivateKey("abelspass");
		user3.unlockPrivateKey("evespass");
		
		JSONObject user1Data = new JSONObject();
		user1Data.put("given_name", "Adam");
		user1Data.put("family_name", "Jordan");
		user1Data.put("email", "adam@example.com");
		user1.setUserData(user1Data);
		
		JSONObject user2Data = new JSONObject();
		user2Data.put("given_name", "Abel");
		user2Data.put("family_name", "leba");
		user2Data.put("email", "abel@example.com");
		user2.setUserData(user2Data);
		
		JSONObject user3Data = new JSONObject();
		user3Data.put("given_name", "Eve");
		user3Data.put("family_name", "vev");
		user3Data.put("email", "eve@example.com");
		user3.setUserData(user3Data);
		
		node.storeAgent(user1);
		node.storeAgent(user2);
		node.storeAgent(user3);

		node.launch();
		
		ServiceAgent testService = ServiceAgent.createServiceAgent(testGamificationActionService, "a pass");
		testService.unlockPrivateKey("a pass");
		node.registerReceiver(testService);

		
		// start connector
		logStream = new ByteArrayOutputStream();

		connector = new WebConnector(true, HTTP_PORT, false, 1000);
		connector.setLogStream(new PrintStream(logStream));
		connector.start(node);
		Thread.sleep(1000); // wait a second for the connector to become ready

//		connector.updateServiceList();
		
		c1 = new MiniClient();
		c1.setAddressPort(HTTP_ADDRESS, HTTP_PORT);
		c1.setLogin(Long.toString(user1.getId()), "adamspass");
		
		c2 = new MiniClient();
		c2.setAddressPort(HTTP_ADDRESS, HTTP_PORT);
		c2.setLogin(Long.toString(user2.getId()), "abelspass");

		c3 = new MiniClient();
		c3.setAddressPort(HTTP_ADDRESS, HTTP_PORT);
		c3.setLogin(Long.toString(user3.getId()), "evespass");

		ac = new MiniClient();
		ac.setAddressPort(HTTP_ADDRESS, HTTP_PORT);
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
	 * Called after the tests have finished.
	 * Shuts down the server and prints out the connector log file for reference.
	 * 
	 * @throws Exception
	 */
	@AfterClass
	public static void shutDownServer() throws Exception {

		connector.stop();
		node.shutDown();

		connector = null;
		node = null;

		LocalNode.reset();

		System.out.println("Connector-Log:");
		System.out.println("--------------");

		System.out.println(logStream.toString());

	}
	
	// Action Test
	@Test
	public void testE1_createNewAction(){
		System.out.println("Test --- Create New Action");
		try
		{
			
			String boundary =  "--32532twtfaweafwsgfaegfawegf4"; 
			
			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
			builder.setBoundary(boundary);
			
			builder.addPart("actionid", new StringBody(actionId, ContentType.TEXT_PLAIN));
			builder.addPart("actionname", new StringBody("action name", ContentType.TEXT_PLAIN));
			builder.addPart("actiondesc", new StringBody("action description", ContentType.TEXT_PLAIN));
			builder.addPart("actionpointvalue", new StringBody("50", ContentType.TEXT_PLAIN));
			
			builder.addPart("actionnotificationcheck", new StringBody("true", ContentType.TEXT_PLAIN));
			builder.addPart("actionnotificationmessage", new StringBody("This is notification message", ContentType.TEXT_PLAIN));
			
			HttpEntity formData = builder.build();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			
			formData.writeTo(out);
		
			Pair<String>[] headers = new Pair[2];
			
			headers[0] = new Pair<String>("Accept-Encoding","gzip, deflate");
			headers[1] = new Pair<String>("Accept-Language","en-GB,en-US;q=0.8,en;q=0.6");
			
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
	public void testE2_getActionWithId(){
		System.out.println("Test --- Get Action With Id");
		try
		{
			ClientResponse result = c1.sendRequest("GET",  mainPath + "" + gameId + "/" + actionId, "");
	        assertEquals(200, result.getHttpCode());
		} catch (Exception e)
		{
			e.printStackTrace();
			fail("Exception: " + e);
			System.exit(0);
		}

	}
	
	@Test
	public void testE2_updateAction(){
		System.out.println("Test --- Update Action");
		try
		{
			String boundary =  "--32532twtfaweafwsgfaegfawegf4"; 
			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
			builder.setBoundary(boundary);
			
			builder.addPart("actionid", new StringBody(actionId, ContentType.TEXT_PLAIN));
			builder.addPart("actionname", new StringBody("action name", ContentType.TEXT_PLAIN));
			builder.addPart("actiondesc", new StringBody("action description", ContentType.TEXT_PLAIN));
			builder.addPart("actionpointvalue", new StringBody("50", ContentType.TEXT_PLAIN));

			builder.addPart("actionnotificationcheck", new StringBody("true", ContentType.TEXT_PLAIN));
			builder.addPart("actionnotificationmessage", new StringBody("This is notification message", ContentType.TEXT_PLAIN));

			HttpEntity formData = builder.build();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			
			formData.writeTo(out);
		
			Pair<String>[] headers = new Pair[2];
			
			headers[0] = new Pair<String>("Accept-Encoding","gzip, deflate");
			headers[1] = new Pair<String>("Accept-Language","en-GB,en-US;q=0.8,en;q=0.6");
			
			ClientResponse result = c1.sendRequest("PUT", mainPath + "" + gameId +"/"+ actionId, out.toString(), "multipart/form-data; boundary="+boundary, "*/*", headers);
			
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
	 * get action list
	 * 
	 */
	@Test
	public void testE21_getActionList()
	{
		System.out.println("Test --- Get ACtion List");
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
	public void testZ4_deleteAction(){
		try
		{
			ClientResponse result = c1.sendRequest("DELETE",  mainPath + "" + gameId + "/" + actionId, "");
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
//		GamificationActionService cl = new GamificationActionService();
//		assertTrue(cl.debugMapping());
//	}

}

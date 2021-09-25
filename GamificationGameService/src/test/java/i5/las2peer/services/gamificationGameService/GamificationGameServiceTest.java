package i5.las2peer.services.gamificationGameService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.After;
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
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

import i5.las2peer.p2p.LocalNode;
import i5.las2peer.p2p.LocalNodeManager;
import i5.las2peer.api.p2p.ServiceNameVersion;
import i5.las2peer.security.UserAgentImpl;
import i5.las2peer.services.gamificationBadgeService.GamificationBadgeService;
//import i5.las2peer.services.gamificationGameService.GamificationGameService;
import i5.las2peer.services.gamificationPointService.GamificationPointService;
import i5.las2peer.testing.MockAgentFactory;
import i5.las2peer.connectors.webConnector.WebConnector;
import i5.las2peer.connectors.webConnector.client.ClientResponse;
import i5.las2peer.connectors.webConnector.client.MiniClient;

/**
 * Example Test Class demonstrating a basic JUnit test structure.
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GamificationGameServiceTest {


	private static final int HTTP_PORT = 8081;
	
	private static LocalNode node;
	private static WebConnector connector;
	private static ByteArrayOutputStream logStream;

	private static MiniClient c1, c2, c3;
	
	private static UserAgentImpl user1, user2, user3;

	private static String gameId = "game_test_id";
	private static final String mainPath = "gamification/games/";
	
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
		node = new LocalNodeManager().newNode();
		node.launch();
		
		user1 = MockAgentFactory.getAdam();
		user2 = MockAgentFactory.getAbel();
		user3 = MockAgentFactory.getEve();
		
		// agent must be unlocked in order to be stored 
		user1.unlock("adamspass"); 
		user2.unlock("abelspass");
		user3.unlock("evespass");
		
		node.storeAgent(user1);
		node.storeAgent(user2);
		node.storeAgent(user3);

		node.startService(new ServiceNameVersion(GamificationGameService.class.getName(), "0.1"), "a pass");

		node.startService(new ServiceNameVersion(GamificationBadgeService.class.getName(), "0.1"), "a pass");

		node.startService(new ServiceNameVersion(GamificationPointService.class.getName(), "0.1"), "a pass");

		logStream = new ByteArrayOutputStream();
		connector = new WebConnector(true, HTTP_PORT, false, 1000);
		connector.setLogStream(new PrintStream(logStream));
		connector.start(node);
		Thread.sleep(1000); // wait a second for the connector to become ready

		c1 = new MiniClient();
		c1.setConnectorEndpoint(connector.getHttpEndpoint());
		c1.setLogin(user1.getIdentifier(), "adamspass");
		
		c2 = new MiniClient();
		c2.setConnectorEndpoint(connector.getHttpEndpoint());
		c2.setLogin(user2.getIdentifier(), "abelspass");

		c3 = new MiniClient();
		c3.setConnectorEndpoint(connector.getHttpEndpoint());
		c3.setLogin(user3.getIdentifier(), "evespass");
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

	// Game Test
	/**
	 * 
	 * Validate user, register if not registered yet
	 * 
	 */
	@Test
	public void testA1_userLoginValidation()
	{

		System.out.println("Test --- User Login Validation");
		try
		{
			ClientResponse result = c1.sendRequest("POST", mainPath + "validation", ""); // testInput is
			System.out.println(result.getResponse());
			assertEquals(200, result.getHttpCode());
			result = c2.sendRequest("POST", mainPath + "validation", ""); // testInput is
			System.out.println(result.getResponse());
			assertEquals(200, result.getHttpCode());
			result = c3.sendRequest("POST", mainPath + "validation", ""); // testInput is
			System.out.println(result.getResponse());
			assertEquals(200, result.getHttpCode());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: " + e);
			System.exit(0);
		}
	}
	
	@Test
	public void testA2_createNewGame(){
		System.out.println("Test --- Create New Game");
		try
		{
			String boundary =  "--32532twtfaweafwsgfaegfawegf4"; 
			
			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
			builder.setBoundary(boundary);
			

			builder.addPart("gameid", new StringBody(gameId, ContentType.TEXT_PLAIN));
			builder.addPart("gamedesc", new StringBody("New Game", ContentType.TEXT_PLAIN));
			builder.addPart("commtype", new StringBody("com_type", ContentType.TEXT_PLAIN));
			
			HttpEntity formData = builder.build();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			
			formData.writeTo(out);
		
			Map<String, String> headers = new HashMap<>();
			
			headers.put("Accept-Encoding","gzip, deflate");
			headers.put("Accept-Language","en-GB,en-US;q=0.8,en;q=0.6");
			
			ClientResponse result = c1.sendRequest("POST", mainPath + "data", out.toString(), "multipart/form-data; boundary="+boundary, "*/*", headers);
			System.out.println(result.getResponse());
			if(result.getHttpCode()==HttpURLConnection.HTTP_OK){
				assertEquals(HttpURLConnection.HTTP_OK,result.getHttpCode());
			}
			else{
				assertEquals(HttpURLConnection.HTTP_CREATED,result.getHttpCode());
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: " + e);
			System.exit(0);
		}
	}

	@Test
	public void testA3_removeMemberFromGame()
	{

		System.out.println("Test --- Remove Member From Game");
		try
		{
			String memberId = user1.getLoginName();
			ClientResponse result = c1.sendRequest("DELETE",  mainPath + "data/"+gameId+"/"+memberId, ""); // testInput is
			System.out.println(result.getResponse());
			assertEquals(200, result.getHttpCode());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: " + e);
			System.exit(0);
		}
	}
	@Test
	public void testA4_addMemberToGame()
	{
		// Add user 2 to game
		System.out.println("Test --- Add Member To Game");
		try
		{
			String memberId = user2.getLoginName();
			ClientResponse result = c2.sendRequest("POST",  mainPath + "data/"+gameId+"/"+memberId, ""); // testInput is
			System.out.println(result.getResponse());
			assertEquals(200, result.getHttpCode());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: " + e);
			System.exit(0);
		}
	}

	@Test
	public void testA4_getGameWithId(){

		System.out.println("Test --- Get Game With Id");
		try
		{
			ClientResponse result = c1.sendRequest("GET",  mainPath + "data/" + gameId, "");
	        assertEquals(HttpURLConnection.HTTP_OK, result.getHttpCode());
		} catch (Exception e)
		{
			e.printStackTrace();
			fail("Exception: " + e);
			System.exit(0);
		}
	}
	
	@Test
	public void testA4_getGameListSeparated(){

		System.out.println("Test --- Get Game List Separated");
		try
		{
			ClientResponse result = c1.sendRequest("GET",  mainPath + "list/separated", "");
	        assertEquals(HttpURLConnection.HTTP_OK, result.getHttpCode());
		} catch (Exception e)
		{
			e.printStackTrace();
			fail("Exception: " + e);
			System.exit(0);
		}
	}
	
	
	@Test
	public void testZ8_removeMemberFromGame()
	{

		System.out.println("Test --- Remove Member From Game");
		try
		{
			String memberId = user1.getLoginName();
			ClientResponse result = c1.sendRequest("DELETE",  mainPath + "data/"+gameId+"/"+memberId, ""); // testInput is
			System.out.println(result.getResponse());
			assertEquals(200, result.getHttpCode());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: " + e);
			System.exit(0);
		}
	}
	
	@Test
	public void testZ9_deleteGame(){

		System.out.println("Test --- Delete Game");
		try
		{
			ClientResponse result = c1.sendRequest("DELETE",  mainPath + "data/" + gameId, "");
	        assertEquals(HttpURLConnection.HTTP_OK, result.getHttpCode());
		} catch (Exception e)
		{
			e.printStackTrace();
			fail("Exception: " + e);
			System.exit(0);
		}
	}
}

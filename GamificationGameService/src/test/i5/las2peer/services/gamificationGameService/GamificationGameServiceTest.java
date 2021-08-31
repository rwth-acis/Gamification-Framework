package i5.las2peer.services.gamificationGameService;

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
import i5.las2peer.services.gamificationBadgeService.GamificationBadgeService;
import i5.las2peer.services.gamificationGameService.GamificationGameService;
import i5.las2peer.services.gamificationPointService.GamificationPointService;
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
public class GamificationGameServiceTest {

	private static final String HTTP_ADDRESS = "http://127.0.0.1";
//	private static final int HTTP_PORT = WebConnector.DEFAULT_HTTP_PORT;
	private static final int HTTP_PORT = 8081;
	
	private static LocalNode node;
	private static WebConnector connector;
	private static ByteArrayOutputStream logStream;

	private static MiniClient c1, c2, c3, ac;
	
	private static UserAgent user1, user2, user3, anon;

	// during testing, the specified service version does not matter
	private static final ServiceNameVersion testGamificationGameService = new ServiceNameVersion(GamificationGameService.class.getCanonicalName(),"0.1");

	private static final ServiceNameVersion testBadgeService = new ServiceNameVersion(GamificationBadgeService.class.getCanonicalName(),"0.1");
	private static final ServiceNameVersion testPointService = new ServiceNameVersion(GamificationPointService.class.getCanonicalName(),"0.1");

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
		
		ServiceAgent testService = ServiceAgent.createServiceAgent(testGamificationGameService, "a pass");
		testService.unlockPrivateKey("a pass");
		node.registerReceiver(testService);
		
		ServiceAgent badgeService = ServiceAgent.createServiceAgent(testBadgeService, "a pass");
		badgeService.unlockPrivateKey("a pass");
		node.registerReceiver(badgeService);
		
		ServiceAgent pointService = ServiceAgent.createServiceAgent(testPointService, "a pass");
		pointService.unlockPrivateKey("a pass");
		node.registerReceiver(pointService);

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
		
			Pair<String>[] headers = new Pair[2];
			
			headers[0] = new Pair<String>("Accept-Encoding","gzip, deflate");
			headers[1] = new Pair<String>("Accept-Language","en-GB,en-US;q=0.8,en;q=0.6");
			
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
	 // Remove a member from the game
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
	
	
	// Remove a member from the game
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
	
	
// legacy	
//	/**
//	 * Test the TemplateService for valid rest mapping.
//	 * Important for development.
//	 */
//	@Test
//	public void testDebugMapping()
//	{
//		GamificationGameService cl = new GamificationGameService();
//		assertTrue(cl.debugMapping());
//	}

}

package i5.las2peer.services.gamificationVisualizationService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

import i5.las2peer.p2p.LocalNode;
import i5.las2peer.p2p.LocalNodeManager;
import i5.las2peer.api.p2p.ServiceNameVersion;
import i5.las2peer.api.security.ServiceAgent;
import i5.las2peer.security.UserAgentImpl;
import i5.las2peer.services.gamificationAchievementService.GamificationAchievementService;
import i5.las2peer.services.gamificationActionService.GamificationActionService;
import i5.las2peer.services.gamificationBadgeService.GamificationBadgeService;
import i5.las2peer.services.gamificationGameService.GamificationGameService;
import i5.las2peer.services.gamificationLevelService.GamificationLevelService;
import i5.las2peer.services.gamificationPointService.GamificationPointService;
import i5.las2peer.services.gamificationQuestService.GamificationQuestService;
import i5.las2peer.services.gamificationVisualizationService.GamificationVisualizationService;
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
public class GamificationVisualizationServiceTest {

//	private static final String HTTP_ADDRESS = "http://127.0.0.1";
//	private static final int HTTP_PORT = WebConnector.DEFAULT_HTTP_PORT;
	private static final int HTTP_PORT = 8081;
	
	private static LocalNode node;
	private static WebConnector connector;
	private static ByteArrayOutputStream logStream;

	private static MiniClient c1, c2, c3;//, ac;

	private static UserAgentImpl user1, user2, user3;// anon;
	
//	// during testing, the specified service version does not matter
//	private static final ServiceNameVersion testGamificationVisualizationService = new ServiceNameVersion(GamificationVisualizationService.class.getCanonicalName(),"0.1");
//	private static final ServiceNameVersion testBadgeService = new ServiceNameVersion(GamificationBadgeService.class.getCanonicalName(),"0.1");
//	private static final ServiceNameVersion testPointService = new ServiceNameVersion(GamificationPointService.class.getCanonicalName(),"0.1");
//	private static final ServiceNameVersion testLevelService = new ServiceNameVersion(GamificationLevelService.class.getCanonicalName(),"0.1");
//	private static final ServiceNameVersion testAchievementService = new ServiceNameVersion(GamificationAchievementService.class.getCanonicalName(),"0.1");
//	private static final ServiceNameVersion testActionService = new ServiceNameVersion(GamificationActionService.class.getCanonicalName(),"0.1");
//	private static final ServiceNameVersion testQuestService = new ServiceNameVersion(GamificationQuestService.class.getCanonicalName(),"0.1");

	private static String appId = "test";
	private static String memberId = "user1";
	private static String questId = "quest1";
	private static String badgeId = "badge1";
	private static String actionId = "action1";
	private static String achievementId = "achievement1";
	
	private static final String mainPath = "visualization/";
	

	
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

		
//		ServiceAgent testService = ServiceAgent.createServiceAgent(testGamificationVisualizationService, "a pass");
//		testService.unlockPrivateKey("a pass");
//		node.registerReceiver(testService);
		node.startService(new ServiceNameVersion(GamificationVisualizationService.class.getName(), "0.1"), "a pass");

//		ServiceAgent badgeService = ServiceAgent.createServiceAgent(testBadgeService, "a pass");
//		badgeService.unlockPrivateKey("a pass");
//		node.registerReceiver(badgeService);
		node.startService(new ServiceNameVersion(GamificationBadgeService.class.getName(), "0.1"), "a pass");

//		ServiceAgent pointService = ServiceAgent.createServiceAgent(testPointService, "a pass");
//		pointService.unlockPrivateKey("a pass");
//		node.registerReceiver(pointService);
		node.startService(new ServiceNameVersion(GamificationPointService.class.getName(), "0.1"), "a pass");

//		ServiceAgent levelService = ServiceAgent.createServiceAgent(testLevelService, "a pass");
//		levelService.unlockPrivateKey("a pass");
//		node.registerReceiver(levelService);
		node.startService(new ServiceNameVersion(GamificationLevelService.class.getName(), "0.1"), "a pass");

//		ServiceAgent achievementService = ServiceAgent.createServiceAgent(testAchievementService, "a pass");
//		achievementService.unlockPrivateKey("a pass");
//		node.registerReceiver(achievementService);
		node.startService(new ServiceNameVersion(GamificationAchievementService.class.getName(), "0.1"), "a pass");

//		ServiceAgent actionService = ServiceAgent.createServiceAgent(testActionService, "a pass");
//		actionService.unlockPrivateKey("a pass");
//		node.registerReceiver(actionService);
		node.startService(new ServiceNameVersion(GamificationActionService.class.getName(), "0.1"), "a pass");

//		ServiceAgent questService = ServiceAgent.createServiceAgent(testQuestService, "a pass");
//		questService.unlockPrivateKey("a pass");
//		node.registerReceiver(questService);
		node.startService(new ServiceNameVersion(GamificationQuestService.class.getName(), "0.1"), "a pass");
				
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

	
	@Test
	public void testA1_getPointOfMember()
	{
		System.out.println("Test --- Get Point Of Member");
		try
		{
			ClientResponse result = c1.sendRequest("GET", mainPath + "points/"+appId+"/"+memberId, ""); // testInput is
			System.out.println(result.getResponse());
			assertEquals(200, result.getHttpCode());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: " + e);
			System.exit(0);
		}
	}
	
	@Test
	public void testA2_getStatusOfMember()
	{
		System.out.println("Test --- Get Point And Level Of Member");
		try
		{
			ClientResponse result = c1.sendRequest("GET", mainPath + "status/"+appId+"/"+memberId, ""); // testInput is
			System.out.println(result.getResponse());
			assertEquals(200, result.getHttpCode());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: " + e);
			System.exit(0);
		}
	}
	
	@Test
	public void testA3_getBadgesOfMember()
	{
		System.out.println("Test --- Get Badges Of Member");
		try
		{
			ClientResponse result = c1.sendRequest("GET", mainPath + "badges/"+appId+"/"+memberId, ""); // testInput is
			System.out.println(result.getResponse());
			assertEquals(200, result.getHttpCode());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: " + e);
			System.exit(0);
		}
	}
	
	@Test
	public void testA4_getQuestsWithStatusOfMember()
	{
		System.out.println("Test --- Get Quests With Status Of Member");
		try
		{
			ClientResponse result = c1.sendRequest("GET", mainPath + "quests/"+appId+"/"+memberId+"/status/COMPLETED", ""); // testInput is
			System.out.println(result.getResponse());
			assertEquals(200, result.getHttpCode());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: " + e);
			System.exit(0);
		}
		try
		{
			ClientResponse result = c1.sendRequest("GET", mainPath + "quests/"+appId+"/"+memberId+"/status/REVEALED", ""); // testInput is
			System.out.println(result.getResponse());
			assertEquals(200, result.getHttpCode());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: " + e);
			System.exit(0);
		}
	}

	@Test
	public void testA5_getQuestProgressOfMember()
	{
		System.out.println("Test --- Get Quests With Progress Of Member");
		try
		{
			ClientResponse result = c1.sendRequest("GET", mainPath + "quests/"+appId+"/"+memberId+"/progress/" +  questId, ""); // testInput is
			System.out.println(result.getResponse());
			assertEquals(200, result.getHttpCode());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: " + e);
			System.exit(0);
		}
	}
	
	@Test
	public void testA6_getAchievementsOfMember()
	{
		System.out.println("Test --- Get Quests With Progress Of Member");
		try
		{
			ClientResponse result = c1.sendRequest("GET", mainPath + "achievements/"+appId+"/"+memberId, ""); // testInput is
			System.out.println(result.getResponse());
			assertEquals(200, result.getHttpCode());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: " + e);
			System.exit(0);
		}
	}
	
	@Test
	public void testA7_getBadgeImageDetail()
	{
		System.out.println("Test --- Get Badge Image Detail");
		try
		{
			ClientResponse result = c1.sendRequest("GET", mainPath + "badges/"+appId+"/"+memberId+"/"+badgeId+"/img", ""); // testInput is
			System.out.println(result.getResponse());
			assertEquals(200, result.getHttpCode());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: " + e);
			System.exit(0);
		}
	}
	
	@Test
	public void testA8_getBadgeDetailWithId()
	{
		System.out.println("Test --- Get Badge Detail With ID");
		try
		{
			ClientResponse result = c1.sendRequest("GET", mainPath + "badges/"+appId+"/"+memberId+"/"+badgeId, ""); // testInput is
			System.out.println(result.getResponse());
			assertEquals(200, result.getHttpCode());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: " + e);
			System.exit(0);
		}
	}
	
	@Test
	public void testA9_getQuestDetailWithId()
	{
		System.out.println("Test --- Get Quest Detail With ID");
		try
		{
			ClientResponse result = c1.sendRequest("GET", mainPath + "quests/"+appId+"/"+memberId+"/"+questId, ""); // testInput is
			System.out.println(result.getResponse());
			assertEquals(200, result.getHttpCode());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: " + e);
			System.exit(0);
		}
	}
	
	@Test
	public void testB1_getAchievementDetailWithId()
	{
		System.out.println("Test --- Get Achievement Detail With ID");
		try
		{
			ClientResponse result = c1.sendRequest("GET", mainPath + "achievements/"+appId+"/"+memberId+"/"+achievementId, ""); // testInput is
			System.out.println(result.getResponse());
			assertEquals(200, result.getHttpCode());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: " + e);
			System.exit(0);
		}
	}
	
	@Test
	public void testB2_triggerAction()
	{
		System.out.println("Test --- Trigger Action");
		try
		{
			ClientResponse result = c1.sendRequest("POST", mainPath + "actions/"+appId+"/"+actionId+"/"+memberId, ""); // testInput is
			System.out.println(result.getResponse());
			assertEquals(200, result.getHttpCode());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: " + e);
			System.exit(0);
		}
	}
	
	@Test
	public void testB3_getLocalLeaderboard()
	{
		System.out.println("Test --- Get Local Leaderboard");
		
		try
		{
			ClientResponse result = c1.sendRequest("GET", mainPath + "leaderboard/local/"+appId+"/"+memberId+"?current=1&rowCount=10&searchPhrase=", ""); // testInput is
			System.out.println(result.getResponse());
			assertEquals(200, result.getHttpCode());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: " + e);
			System.exit(0);
		}
	}
	
	@Test
	public void testB4_getGlobalLeaderboard()
	{
		System.out.println("Test --- Get Global Leaderboard");
		
		try
		{
			ClientResponse result = c1.sendRequest("GET", mainPath + "leaderboard/global/"+appId+"/"+memberId+"?current=1&rowCount=10&searchPhrase=", ""); // testInput is
			System.out.println(result.getResponse());
			assertEquals(200, result.getHttpCode());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: " + e);
			System.exit(0);
		}
	}
//	// Badge Test --------------------------------------------------
//	@Test
//	public void testB1_createNewBadge(){
//		System.out.println("Test --- Create New Badge");
//		try
//		{
//			File badgeImage = new File("./frontend/webapps/ROOT/img/logo.png");
//			String boundary =  "----WebKitFormBoundaryuK41JdjQK2kdEBDn"; 
//			
//			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
//			builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
//			builder.setBoundary(boundary);
//			
//			builder.addPart("badgeid", new StringBody(badgeId, ContentType.TEXT_PLAIN));
//			builder.addPart("badgename", new StringBody("Badge name", ContentType.TEXT_PLAIN));
//			builder.addPart("badgedesc", new StringBody("Badge description", ContentType.TEXT_PLAIN));
//			builder.addPart("badgeimageinput", new FileBody(badgeImage, ContentType.create("image/png"), "logo.png"));
//			builder.addPart("dev", new StringBody("yes", ContentType.TEXT_PLAIN));
//
//		
//			HttpEntity formData = builder.build();
//			ByteArrayOutputStream out = new ByteArrayOutputStream();
//			
//			formData.writeTo(out);
//
//			ClientResponse result = c1.sendRequest("POST", mainPath + "badges/" + appId, out.toString(), "multipart/form-data; boundary="+boundary, "*/*",new Pair[]{});
//			
//			System.out.println(result.getResponse());
//			if(result.getHttpCode()==HttpURLConnection.HTTP_OK){
//				assertEquals(HttpURLConnection.HTTP_OK,result.getHttpCode());
//			}
//			else{
//
//				assertEquals(HttpURLConnection.HTTP_CREATED,result.getHttpCode());
//			}
//		} catch (Exception e)
//		{
//			e.printStackTrace();
//			System.out.println(e.getMessage());
//			
//			fail("Exception: " + e);
//			System.exit(0);
//		}
//	}
//	
//	/**
//	 * update badge
//	 * 
//	 */
//	@Test
//	public void testB2_updateBadge(){
//		System.out.println("Test --- Update Badge");
//		try
//		{
//			File badgeImage = new File("./frontend/webapps/ROOT/img/logo.png");
//			String boundary =  "--32532twtfaweafwsgfaegfawegf4"; 
//			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
//			builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
//			builder.setBoundary(boundary);
//			
//			builder.addPart("badgeid", new StringBody(badgeId, ContentType.TEXT_PLAIN));
//			builder.addPart("badgename", new StringBody("Badge name", ContentType.TEXT_PLAIN));
//			builder.addPart("badgedesc", new StringBody("Badge description", ContentType.TEXT_PLAIN));
//			builder.addPart("badgeimageinput", new FileBody(badgeImage, ContentType.create("image/png"), "logo.png"));
//			builder.addPart("dev", new StringBody("yes", ContentType.TEXT_PLAIN));
//
//		
//			HttpEntity formData = builder.build();
//			ByteArrayOutputStream out = new ByteArrayOutputStream();
//			
//			formData.writeTo(out);
//
//			ClientResponse result = c1.sendRequest("PUT", mainPath + "badges/" + appId +"/" + badgeId, out.toString(), "multipart/form-data; boundary="+boundary, "*/*",new Pair[]{});
//			
//			System.out.println(result.getResponse());
//			assertEquals(HttpURLConnection.HTTP_OK,result.getHttpCode());
//			
//		} catch (Exception e)
//		{
//			e.printStackTrace();
//			System.out.println(e.getMessage());
//			
//			fail("Exception: " + e);
//			System.exit(0);
//		}
//	}
//	
//	@Test
//	public void testB2_getBadgeWithId(){
//		System.out.println("Test --- Get Badge With Id");
//		try
//		{
//			ClientResponse result = c1.sendRequest("GET",  mainPath + "badges/" + appId + "/" + badgeId, "");
//	        assertEquals(200, result.getHttpCode());
//		} catch (Exception e)
//		{
//			e.printStackTrace();
//			fail("Exception: " + e);
//			System.exit(0);
//		}
//	}
//	
//	/**
//	 * 
//	 * get badge list
//	 * 
//	 */
//	@Test
//	public void testB2_getBadgeList()
//	{
//		System.out.println("Test --- Get Badge List");
//		try
//		{
//			JSONObject obj = new JSONObject();
//			obj.put("current", 1);
//			obj.put("rowCount", 10);
//			obj.put("searchPhrase", "");
//			ClientResponse result = c1.sendRequest("GET", mainPath + "badges/" + appId + "?current=1&rowCount=10&searchPhrase=", "");
//	        assertEquals(200, result.getHttpCode());
//		} catch (Exception e)
//		{
//			e.printStackTrace();
//			fail("Exception: " + e);
//			System.exit(0);
//		}
//	
//	}
//	
//	/**
//	 * 
//	 * get badge image
//	 * 
//	 */
//	@Test
//	public void testB2_getBadgeImage()
//	{
//		System.out.println("Test --- Get Badge Image");
//		try
//		{
//			ClientResponse result = c1.sendRequest("GET", mainPath + "badges/" + appId + "/" + badgeId + "/img", "");
//	        assertEquals(200, result.getHttpCode());
//		} catch (Exception e)
//		{
//			e.printStackTrace();
//			fail("Exception: " + e);
//			System.exit(0);
//		}
//	}
//	
//	
//
//	// Achievement Test
//	@Test
//	public void testC1_createNewAchievement(){
//		System.out.println("Test --- Create New Achievement");
//		// Depend on badge
//		try
//		{
//			
//			String boundary =  "--32532twtfaweafwsgfaegfawegf4"; 
//			
//			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
//			builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
//			builder.setBoundary(boundary);
//			
//			builder.addPart("achievementid", new StringBody(achievementId, ContentType.TEXT_PLAIN));
//			builder.addPart("achievementname", new StringBody("achievement", ContentType.TEXT_PLAIN));
//			builder.addPart("achievementdesc", new StringBody("achievement description", ContentType.TEXT_PLAIN));
//			builder.addPart("achievementpointvalue", new StringBody("50", ContentType.TEXT_PLAIN));
//			builder.addPart("achievementbadgeid", new StringBody(badgeId, ContentType.TEXT_PLAIN));
//			
//			HttpEntity formData = builder.build();
//			ByteArrayOutputStream out = new ByteArrayOutputStream();
//			
//			formData.writeTo(out);
//		
//			Pair<String>[] headers = new Pair[2];
//			
//			headers[0] = new Pair<String>("Accept-Encoding","gzip, deflate");
//			headers[1] = new Pair<String>("Accept-Language","en-GB,en-US;q=0.8,en;q=0.6");
//			
//			ClientResponse result = c1.sendRequest("POST", mainPath + "achievements/" + appId, out.toString(), "multipart/form-data; boundary="+boundary, "*/*", headers);
//
//			System.out.println(result.getResponse());
//			if(result.getHttpCode()==HttpURLConnection.HTTP_OK){
//				assertEquals(HttpURLConnection.HTTP_OK,result.getHttpCode());
//			}
//			else{
//
//				assertEquals(HttpURLConnection.HTTP_CREATED,result.getHttpCode());
//			}
//		} catch (Exception e)
//		{
//			e.printStackTrace();
//			System.out.println(e.getMessage());
//			
//			fail("Exception: " + e);
//			System.exit(0);
//		}
//	}
//	
//	@Test
//	public void testC2_getAchievementWithId(){
//
//		System.out.println("Test --- Get Achievement With Id");
//		try
//		{
//			ClientResponse result = c1.sendRequest("GET",  mainPath + "achievements/" + appId + "/" + achievementId, "");
//	        assertEquals(200, result.getHttpCode());
//		} catch (Exception e)
//		{
//			e.printStackTrace();
//			fail("Exception: " + e);
//			System.exit(0);
//		}
//
//	}
//	
//	@Test
//	public void testC2_updateAchievement(){
//
//		System.out.println("Test --- Update Achievement");
//		try
//		{
//			String boundary =  "--32532twtfaweafwsgfaegfawegf4"; 
//			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
//			builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
//			builder.setBoundary(boundary);
//			
//			builder.addPart("achievementid", new StringBody(achievementId, ContentType.TEXT_PLAIN));
//			builder.addPart("achievementname", new StringBody("achievement", ContentType.TEXT_PLAIN));
//			builder.addPart("achievementdesc", new StringBody("achievement description", ContentType.TEXT_PLAIN));
//			builder.addPart("achievementpointvalue", new StringBody("50", ContentType.TEXT_PLAIN));
//			builder.addPart("achievementbadgeid", new StringBody(badgeId, ContentType.TEXT_PLAIN));
//				
//			HttpEntity formData = builder.build();
//			ByteArrayOutputStream out = new ByteArrayOutputStream();
//			
//			formData.writeTo(out);
//		
//			Pair<String>[] headers = new Pair[2];
//			
//			headers[0] = new Pair<String>("Accept-Encoding","gzip, deflate");
//			headers[1] = new Pair<String>("Accept-Language","en-GB,en-US;q=0.8,en;q=0.6");
//			
//			ClientResponse result = c1.sendRequest("PUT", mainPath + "achievements/" + appId +"/"+ achievementId, out.toString(), "multipart/form-data; boundary="+boundary, "*/*", headers);
//
//			System.out.println(result.getResponse());
//			assertEquals(HttpURLConnection.HTTP_OK,result.getHttpCode());
//			
//		} catch (Exception e)
//		{
//			e.printStackTrace();
//			System.out.println(e.getMessage());
//			
//			fail("Exception: " + e);
//			System.exit(0);
//		}
//	}
//	
//
//	
//	/**
//	 * 
//	 * get achievement list
//	 * 
//	 */
//	@Test
//	public void testC2_getAchievementList()
//	{
//		System.out.println("Test --- Get Achievement List");
//		try
//		{
//			ClientResponse result = c1.sendRequest("GET", mainPath + "achievements/" + appId + "?current=1&rowCount=10&searchPhrase=", "");
//	        assertEquals(200, result.getHttpCode());
//		} catch (Exception e)
//		{
//			e.printStackTrace();
//			fail("Exception: " + e);
//			System.exit(0);
//		}
//	
//	}
//	
//	// Level Test
//	@Test
//	public void testD1_createNewLevel(){
//		System.out.println("Test --- Create New Level");
//		try
//		{
//			
//			String boundary =  "--32532twtfaweafwsgfaegfawegf4"; 
//			
//			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
//			builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
//			builder.setBoundary(boundary);
//			
//			builder.addPart("levelnum", new StringBody(Integer.toString(levelId), ContentType.TEXT_PLAIN));
//			builder.addPart("levelname", new StringBody("level name", ContentType.TEXT_PLAIN));
//			builder.addPart("levelpointvalue", new StringBody("50", ContentType.TEXT_PLAIN));
//			
//			HttpEntity formData = builder.build();
//			ByteArrayOutputStream out = new ByteArrayOutputStream();
//			
//			formData.writeTo(out);
//		
//			Pair<String>[] headers = new Pair[2];
//			
//			headers[0] = new Pair<String>("Accept-Encoding","gzip, deflate");
//			headers[1] = new Pair<String>("Accept-Language","en-GB,en-US;q=0.8,en;q=0.6");
//			
//			ClientResponse result = c1.sendRequest("POST", mainPath + "levels/" + appId, out.toString(), "multipart/form-data; boundary="+boundary, "*/*", headers);
//
//			System.out.println(result.getResponse());
//			if(result.getHttpCode()==HttpURLConnection.HTTP_OK){
//				assertEquals(HttpURLConnection.HTTP_OK,result.getHttpCode());
//			}
//			else{
//
//				assertEquals(HttpURLConnection.HTTP_CREATED,result.getHttpCode());
//			}
//		} catch (Exception e)
//		{
//			e.printStackTrace();
//			System.out.println(e.getMessage());
//			
//			fail("Exception: " + e);
//			System.exit(0);
//		}
//	}
//	
//	@Test
//	public void testD2_getLevelWithId(){
//		System.out.println("Test --- Get Level With Id");
//		try
//		{
//			ClientResponse result = c1.sendRequest("GET",  mainPath + "levels/" + appId + "/" + levelId, "");
//	        assertEquals(200, result.getHttpCode());
//		} catch (Exception e)
//		{
//			e.printStackTrace();
//			fail("Exception: " + e);
//			System.exit(0);
//		}
//
//	}
//	
//	@Test
//	public void testD2_updateLevel(){
//		System.out.println("Test --- Update Level");
//		try
//		{
//			String boundary =  "--32532twtfaweafwsgfaegfawegf4"; 
//			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
//			builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
//			builder.setBoundary(boundary);
//			
//			builder.addPart("levelnum", new StringBody(Integer.toString(levelId), ContentType.TEXT_PLAIN));
//			builder.addPart("levelname", new StringBody("level name", ContentType.TEXT_PLAIN));
//			builder.addPart("levelpointvalue", new StringBody("50", ContentType.TEXT_PLAIN));
//					
//			HttpEntity formData = builder.build();
//			ByteArrayOutputStream out = new ByteArrayOutputStream();
//			
//			formData.writeTo(out);
//		
//			Pair<String>[] headers = new Pair[2];
//			
//			headers[0] = new Pair<String>("Accept-Encoding","gzip, deflate");
//			headers[1] = new Pair<String>("Accept-Language","en-GB,en-US;q=0.8,en;q=0.6");
//			
//			ClientResponse result = c1.sendRequest("PUT", mainPath + "levels/" + appId +"/"+ levelId, out.toString(), "multipart/form-data; boundary="+boundary, "*/*", headers);
//
//			System.out.println(result.getResponse());
//			assertEquals(HttpURLConnection.HTTP_OK,result.getHttpCode());
//			
//		} catch (Exception e)
//		{
//			e.printStackTrace();
//			System.out.println(e.getMessage());
//			
//			fail("Exception: " + e);
//			System.exit(0);
//		}
//	}
//	
//	/**
//	 * 
//	 * get level list
//	 * 
//	 */
//	@Test
//	public void testD2_getLevelList()
//	{
//		System.out.println("Test --- Get Level List");
//		try
//		{
//			ClientResponse result = c1.sendRequest("GET", mainPath + "levels/" + appId + "?current=1&rowCount=10&searchPhrase=", "");
//	        assertEquals(200, result.getHttpCode());
//		} catch (Exception e)
//		{
//			e.printStackTrace();
//			fail("Exception: " + e);
//			System.exit(0);
//		}
//	
//	}
//	
//	// Action Test
//	@Test
//	public void testE1_createNewAction(){
//		System.out.println("Test --- Create New Action");
//		try
//		{
//			
//			String boundary =  "--32532twtfaweafwsgfaegfawegf4"; 
//			
//			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
//			builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
//			builder.setBoundary(boundary);
//			
//			builder.addPart("actionid", new StringBody(actionId, ContentType.TEXT_PLAIN));
//			builder.addPart("actionname", new StringBody("action name", ContentType.TEXT_PLAIN));
//			builder.addPart("actiondesc", new StringBody("action description", ContentType.TEXT_PLAIN));
//			builder.addPart("actionpointvalue", new StringBody("50", ContentType.TEXT_PLAIN));
//			
//			HttpEntity formData = builder.build();
//			ByteArrayOutputStream out = new ByteArrayOutputStream();
//			
//			formData.writeTo(out);
//		
//			Pair<String>[] headers = new Pair[2];
//			
//			headers[0] = new Pair<String>("Accept-Encoding","gzip, deflate");
//			headers[1] = new Pair<String>("Accept-Language","en-GB,en-US;q=0.8,en;q=0.6");
//			
//			ClientResponse result = c1.sendRequest("POST", mainPath + "actions/" + appId, out.toString(), "multipart/form-data; boundary="+boundary, "*/*", headers);
//
//			System.out.println(result.getResponse());
//			if(result.getHttpCode()==HttpURLConnection.HTTP_OK){
//				assertEquals(HttpURLConnection.HTTP_OK,result.getHttpCode());
//			}
//			else{
//
//				assertEquals(HttpURLConnection.HTTP_CREATED,result.getHttpCode());
//			}
//		} catch (Exception e)
//		{
//			e.printStackTrace();
//			System.out.println(e.getMessage());
//			
//			fail("Exception: " + e);
//			System.exit(0);
//		}
//	}
//	
//	@Test
//	public void testE2_getActionWithId(){
//		System.out.println("Test --- Get Action With Id");
//		try
//		{
//			ClientResponse result = c1.sendRequest("GET",  mainPath + "actions/" + appId + "/" + actionId, "");
//	        assertEquals(200, result.getHttpCode());
//		} catch (Exception e)
//		{
//			e.printStackTrace();
//			fail("Exception: " + e);
//			System.exit(0);
//		}
//
//	}
//	
//	@Test
//	public void testE2_updateAction(){
//		System.out.println("Test --- Update Action");
//		try
//		{
//			String boundary =  "--32532twtfaweafwsgfaegfawegf4"; 
//			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
//			builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
//			builder.setBoundary(boundary);
//			
//			builder.addPart("actionid", new StringBody(actionId, ContentType.TEXT_PLAIN));
//			builder.addPart("actionname", new StringBody("action name", ContentType.TEXT_PLAIN));
//			builder.addPart("actiondesc", new StringBody("action description", ContentType.TEXT_PLAIN));
//			builder.addPart("actionpointvalue", new StringBody("50", ContentType.TEXT_PLAIN));
//						
//			HttpEntity formData = builder.build();
//			ByteArrayOutputStream out = new ByteArrayOutputStream();
//			
//			formData.writeTo(out);
//		
//			Pair<String>[] headers = new Pair[2];
//			
//			headers[0] = new Pair<String>("Accept-Encoding","gzip, deflate");
//			headers[1] = new Pair<String>("Accept-Language","en-GB,en-US;q=0.8,en;q=0.6");
//			
//			ClientResponse result = c1.sendRequest("PUT", mainPath + "actions/" + appId +"/"+ actionId, out.toString(), "multipart/form-data; boundary="+boundary, "*/*", headers);
//			
//			System.out.println(result.getResponse());
//			assertEquals(HttpURLConnection.HTTP_OK,result.getHttpCode());
//			
//		} catch (Exception e)
//		{
//			e.printStackTrace();
//			System.out.println(e.getMessage());
//			
//			fail("Exception: " + e);
//			System.exit(0);
//		}
//	}
//	
//	/**
//	 * 
//	 * get action list
//	 * 
//	 */
//	@Test
//	public void testE21_getActionList()
//	{
//		System.out.println("Test --- Get ACtion List");
//		try
//		{
//			ClientResponse result = c1.sendRequest("GET", mainPath + "actions/" + appId + "?current=1&rowCount=10&searchPhrase=", "");
//			assertEquals(200, result.getHttpCode());
//		} catch (Exception e)
//		{
//			e.printStackTrace();
//			fail("Exception: " + e);
//			System.exit(0);
//		}
//	
//	}
//	
//	// quest Test
//	@Test
//	public void testF1_createNewQuest(){
//		System.out.println("Test --- Create New Quest");
//		try
//		{
//			JSONArray actionids = new JSONArray();
////			for(int i = 0; i < 5; i++){
//				JSONObject o = new JSONObject();
//				o.put("action", actionId);
//				o.put("times", 5);
//				actionids.add(o);
////			}
//			JSONObject obj = new JSONObject();
//			obj.put("questid", questId);
//			obj.put("questname", "quest_name");
//			obj.put("questdescription", "This is quest");
//			obj.put("queststatus", "REVEALED");
//			obj.put("questpointflag", true);
//			obj.put("questpointvalue", 46);
//			obj.put("questquestflag", true);
//			obj.put("questidcompleted", questId);
//			obj.put("questactionids", actionids);
//			obj.put("questachievementid", achievementId);
//		
//			Pair<String>[] headers = new Pair[2];
//			
//			headers[0] = new Pair<String>("Accept-Encoding","gzip, deflate");
//			headers[1] = new Pair<String>("Accept-Language","en-GB,en-US;q=0.8,en;q=0.6");
//			
//			ClientResponse result = c1.sendRequest("POST", mainPath + "quests/" + appId, obj.toJSONString(), "application/json", "*/*", headers);
//
//			System.out.println(result.getResponse());
//			if(result.getHttpCode()==HttpURLConnection.HTTP_OK){
//				assertEquals(HttpURLConnection.HTTP_OK,result.getHttpCode());
//			}
//			else{
//
//				assertEquals(HttpURLConnection.HTTP_CREATED,result.getHttpCode());
//			}
//		} catch (Exception e)
//		{
//			e.printStackTrace();
//			System.out.println(e.getMessage());
//			
//			fail("Exception: " + e);
//			System.exit(0);
//		}
//	}
//	
//	@Test
//	public void testF2_getQuestWithId(){
//		System.out.println("Test --- Get Quest With Id");
//		try
//		{
//			ClientResponse result = c1.sendRequest("GET",  mainPath + "quests/" + appId + "/" + questId, "");
//	        assertEquals(200, result.getHttpCode());
//		} catch (Exception e)
//		{
//			e.printStackTrace();
//			fail("Exception: " + e);
//			System.exit(0);
//		}
//
//	}
//	
//	@Test
//	public void testF2_updateQuest(){
//		System.out.println("Test --- Update Quest");
//		try
//		{
//			JSONArray actionids = new JSONArray();
////			for(int i = 0; i < 5; i++){
//				JSONObject o = new JSONObject();
//				o.put("actionId", actionId);
//				o.put("times", 5);
//				actionids.add(o);
////			}
//			JSONObject obj = new JSONObject();
//			obj.put("questid", questId);
//			obj.put("questname", "quest_name");
//			obj.put("questdescription", "This is quest");
//			obj.put("queststatus", "REVEALED");
//			obj.put("questpointflag", true);
//			obj.put("questpointvalue", 46);
//			obj.put("questquestflag", true);
//			obj.put("questidcompleted", questId);
//			obj.put("questactionids", actionids);
//			obj.put("questachievementid", achievementId);
//		
//			Pair<String>[] headers = new Pair[2];
//			
//			headers[0] = new Pair<String>("Accept-Encoding","gzip, deflate");
//			headers[1] = new Pair<String>("Accept-Language","en-GB,en-US;q=0.8,en;q=0.6");
//			
//			ClientResponse result = c1.sendRequest("PUT", mainPath + "quests/" + appId +"/"+ questId, obj.toJSONString(), "application/json", "*/*", headers);
//			
//			System.out.println(result.getResponse());
//			assertEquals(HttpURLConnection.HTTP_OK,result.getHttpCode());
//			
//		} catch (Exception e)
//		{
//			e.printStackTrace();
//			System.out.println(e.getMessage());
//			
//			fail("Exception: " + e);
//			System.exit(0);
//		}
//	}
//	
//	/**
//	 * 
//	 * get action list
//	 * 
//	 */
//	@Test
//	public void testF2_getQuestList()
//	{
//		System.out.println("Test --- Get Quest List");
//		try
//		{
//			ClientResponse result = c1.sendRequest("GET", mainPath + "quests/" + appId + "?current=1&rowCount=10&searchPhrase=", "");
//			assertEquals(200, result.getHttpCode());
//		} catch (Exception e)
//		{
//			e.printStackTrace();
//			fail("Exception: " + e);
//			System.exit(0);
//		}
//	
//	}
//	
//
//	//------- CLEAN UP --------------
//	@Test
//	public void testZ3_deleteQuest(){
//		try
//		{
//			ClientResponse result = c1.sendRequest("DELETE",  mainPath + "quests/" + appId + "/" + questId, "");
//	        assertEquals(200, result.getHttpCode());
//		} catch (Exception e)
//		{
//			e.printStackTrace();
//			fail("Exception: " + e);
//			System.exit(0);
//		}
//	}
//	
//	@Test
//	public void testZ4_deleteAction(){
//		try
//		{
//			ClientResponse result = c1.sendRequest("DELETE",  mainPath + "actions/" + appId + "/" + actionId, "");
//	        assertEquals(200, result.getHttpCode());
//		} catch (Exception e)
//		{
//			e.printStackTrace();
//			fail("Exception: " + e);
//			System.exit(0);
//		}
//	}
//	
//	@Test
//	public void testZ5_deleteLevel(){
//		System.out.println("Test --- Delete Level");
//		try
//		{
//			ClientResponse result = c1.sendRequest("DELETE",  mainPath + "levels/" + appId + "/" + levelId, "");
//	        assertEquals(200, result.getHttpCode());
//		} catch (Exception e)
//		{
//			e.printStackTrace();
//			fail("Exception: " + e);
//			System.exit(0);
//		}
//	}
//	
//	@Test
//	public void testZ6_deleteAchievement(){
//		System.out.println("Test --- Delete Achievement");
//		try
//		{
//			ClientResponse result = c1.sendRequest("DELETE",  mainPath + "achievements/" + appId + "/" + achievementId, "");
//	        assertEquals(200, result.getHttpCode());
//		} catch (Exception e)
//		{
//			e.printStackTrace();
//			fail("Exception: " + e);
//			System.exit(0);
//		}
//	}
//	
//	
//	@Test
//	public void testZ7_deleteBadge()
//	{
//		System.out.println("Test --- Delete Badge");
//		try
//		{
//			ClientResponse result = c1.sendRequest("DELETE",  mainPath + "badges/" + appId + "/" + badgeId, "");
//	        assertEquals(200, result.getHttpCode());
//		} catch (Exception e)
//		{
//			e.printStackTrace();
//			fail("Exception: " + e);
//			System.exit(0);
//		}
//	}
//	
//	 // Remove a member from the application
//	@Test
//	public void testZ8_removeMemberFromApp()
//	{
//
//		System.out.println("Test --- Remove Member From App");
//		try
//		{
//			String memberId = user1.getLoginName();
//			ClientResponse result = c1.sendRequest("DELETE",  mainPath + "apps/data/"+appId+"/"+memberId, ""); // testInput is
//			System.out.println(result.getResponse());
//			assertEquals(200, result.getHttpCode());
//		} catch (Exception e) {
//			e.printStackTrace();
//			fail("Exception: " + e);
//			System.exit(0);
//		}
//	}
//	
//	@Test
//	public void testZ9_deleteApp(){
//
//		System.out.println("Test --- Delete App");
//		try
//		{
//			ClientResponse result = c1.sendRequest("DELETE",  mainPath + "apps/data/" + appId, "");
//	        assertEquals(HttpURLConnection.HTTP_OK, result.getHttpCode());
//		} catch (Exception e)
//		{
//			e.printStackTrace();
//			fail("Exception: " + e);
//			System.exit(0);
//		}
//	}
//	
	
//legacy	
//	/**
//	 * Test the TemplateService for valid rest mapping.
//	 * Important for development.
//	 */
//	@Test
//	public void testDebugMapping()
//	{
//		GamificationVisualizationService cl = new GamificationVisualizationService();
//		assertTrue(cl.debugMapping());
//	}

}
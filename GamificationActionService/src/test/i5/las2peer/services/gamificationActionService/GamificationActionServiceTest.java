package i5.las2peer.services.gamificationActionService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;

import javax.imageio.ImageIO;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
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
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;

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

	private static String appId = "app_test_id";
	private static String badgeId = "badge_test_id";
	private static String achievementId = "ach_test_id";
	private static int levelId = 1343;
	private static String actionId = "action_test_id";
	private static String questId = "quest_test_id";
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

		connector.updateServiceList();
		
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
		
		// avoid timing errors: wait for the repository manager to get all services before continuing
		try
		{
			System.out.println("waiting..");
			Thread.sleep(10000);
		} catch (InterruptedException e)
		{
			e.printStackTrace();
		}

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

//	// Application Test
//	/**
//	 * 
//	 * Validate user, register if not registered yet
//	 * 
//	 */
//	@Test
//	public void testA1_userLoginValidation()
//	{
//
//		System.out.println("Test --- User Login Validation");
//		try
//		{
//			ClientResponse result = c1.sendRequest("POST", mainPath + "validation", ""); // testInput is
//			System.out.println(result.getResponse());
//			assertEquals(200, result.getHttpCode());
//			result = c2.sendRequest("POST", mainPath + "validation", ""); // testInput is
//			System.out.println(result.getResponse());
//			assertEquals(200, result.getHttpCode());
//			result = c3.sendRequest("POST", mainPath + "validation", ""); // testInput is
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
//	public void testA2_createNewApp(){
//		System.out.println("Test --- Create New App");
//		try
//		{
//			String boundary =  "--32532twtfaweafwsgfaegfawegf4"; 
//			
//			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
//			builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
//			builder.setBoundary(boundary);
//			
//
//			builder.addPart("appid", new StringBody(appId, ContentType.TEXT_PLAIN));
//			builder.addPart("appdesc", new StringBody("New App", ContentType.TEXT_PLAIN));
//			builder.addPart("commtype", new StringBody("com_type", ContentType.TEXT_PLAIN));
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
//			ClientResponse result = c1.sendRequest("POST", mainPath + "apps/data", out.toString(), "multipart/form-data; boundary="+boundary, "*/*", headers);
//			System.out.println(result.getResponse());
//			if(result.getHttpCode()==HttpURLConnection.HTTP_OK){
//				assertEquals(HttpURLConnection.HTTP_OK,result.getHttpCode());
//			}
//			else{
//
//				assertEquals(HttpURLConnection.HTTP_CREATED,result.getHttpCode());
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			fail("Exception: " + e);
//			System.exit(0);
//		}
//	}
//	 // Remove a member from the application
//	@Test
//	public void testA3_removeMemberFromApp()
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
//	@Test
//	public void testA4_addMemberToApp()
//	{
//		// Add user 2 to app
//		System.out.println("Test --- Add Member To App");
//		try
//		{
//			String memberId = user2.getLoginName();
//			ClientResponse result = c2.sendRequest("POST",  mainPath + "apps/data/"+appId+"/"+memberId, ""); // testInput is
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
//	public void testA4_getAppWithId(){
//
//		System.out.println("Test --- Get App With Id");
//		try
//		{
//			ClientResponse result = c1.sendRequest("GET",  mainPath + "apps/data/" + appId, "");
//	        assertEquals(HttpURLConnection.HTTP_OK, result.getHttpCode());
//		} catch (Exception e)
//		{
//			e.printStackTrace();
//			fail("Exception: " + e);
//			System.exit(0);
//		}
//	}
//	
//	@Test
//	public void testA4_getAppListSeparated(){
//
//		System.out.println("Test --- Get App List Separated");
//		try
//		{
//			ClientResponse result = c1.sendRequest("GET",  mainPath + "apps/list/separated", "");
//	        assertEquals(HttpURLConnection.HTTP_OK, result.getHttpCode());
//		} catch (Exception e)
//		{
//			e.printStackTrace();
//			fail("Exception: " + e);
//			System.exit(0);
//		}
//	}
//	
//	
//	// Point Test -----------------------------------------------------
//	@Test
//	public void testA4_changeUnitName()
//	{
//		System.out.println("Test --- Change Unit Name");
//		try
//		{
//			String memberId = user1.getLoginName();
//			ClientResponse result = c1.sendRequest("PUT", mainPath + "points/"+appId+"/name/"+unitName, ""); // testInput is
//			System.out.println(result.getResponse());
//			assertEquals(200, result.getHttpCode());
//		} catch (Exception e) {
//			e.printStackTrace();
//			fail("Exception: " + e);
//		}
//	}
//	
//	@Test
//	public void testA4_getUnitName()
//	{
//		System.out.println("Test --- Get Unit Name");
//		try
//		{
//			ClientResponse result = c1.sendRequest("GET", mainPath + "points/"+appId+"/name", ""); // testInput is
//			System.out.println(result.getResponse());
//			assertEquals(200, result.getHttpCode());
////			JSONParser parse = new JSONParser(JSONParser.ACCEPT_NON_QUOTE|JSONParser.ACCEPT_SIMPLE_QUOTE);
////			JSONObject obj = (JSONObject) parse.parse(result.getResponse());
////			assertEquals(unitName, obj.get("pointUnitName"));
//		} catch (Exception e) {
//			e.printStackTrace();
//			fail("Exception: " + e);
//			System.exit(0);
//		}
//	}
//
//	// Badge Test --------------------------------------------------
//	@Test
//	public void testB1_createNewBadge(){
//		System.out.println("Test --- Create New Badge");
//		try
//		{
//			File badgeImage = new File("./frontend/webapps/ROOT/manager/img/logo.png");
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
//			builder.addPart("badgenotificationcheck", new StringBody("true", ContentType.TEXT_PLAIN));
//			builder.addPart("badgenotificationmessage", new StringBody("This is notification message", ContentType.TEXT_PLAIN));
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
//			File badgeImage = new File("./frontend/webapps/ROOT/manager/img/logo.png");
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
//			builder.addPart("badgenotificationcheck", new StringBody("true", ContentType.TEXT_PLAIN));
//			builder.addPart("badgenotificationmessage", new StringBody("This is notification message", ContentType.TEXT_PLAIN));
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
//			builder.addPart("achievementnotificationcheck", new StringBody("true", ContentType.TEXT_PLAIN));
//			builder.addPart("achievementnotificationmessage", new StringBody("This is notification message", ContentType.TEXT_PLAIN));
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
//
//			builder.addPart("achievementnotificationcheck", new StringBody("true", ContentType.TEXT_PLAIN));
//			builder.addPart("achievementnotificationmessage", new StringBody("This is notification message", ContentType.TEXT_PLAIN));
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
//
//			builder.addPart("levelnotificationcheck", new StringBody("true", ContentType.TEXT_PLAIN));
//			builder.addPart("levelnotificationmessage", new StringBody("This is notification message", ContentType.TEXT_PLAIN));
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
//			builder.addPart("levelnotificationcheck", new StringBody("true", ContentType.TEXT_PLAIN));
//			builder.addPart("levelnotificationmessage", new StringBody("This is notification message", ContentType.TEXT_PLAIN));
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
//			builder.addPart("actionnotificationcheck", new StringBody("true", ContentType.TEXT_PLAIN));
//			builder.addPart("actionnotificationmessage", new StringBody("This is notification message", ContentType.TEXT_PLAIN));
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
//			builder.addPart("actionnotificationcheck", new StringBody("true", ContentType.TEXT_PLAIN));
//			builder.addPart("actionnotificationmessage", new StringBody("This is notification message", ContentType.TEXT_PLAIN));
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
//			obj.put("questnotificationcheck", true);
//			obj.put("questnotificationmessage", "This is notification message");
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
//			obj.put("questnotificationcheck", true);
//			obj.put("questnotificationmessage", "This is notification message");
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
	
	
	
	/**
	 * Test the TemplateService for valid rest mapping.
	 * Important for development.
	 */
	@Test
	public void testDebugMapping()
	{
		GamificationActionService cl = new GamificationActionService();
		assertTrue(cl.debugMapping());
	}

}

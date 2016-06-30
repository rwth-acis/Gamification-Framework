package i5.las2peer.services.badgeService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;

import javax.imageio.ImageIO;

import org.apache.http.HttpEntity;
import org.apache.http.entity.*;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import i5.las2peer.p2p.LocalNode;
import i5.las2peer.p2p.ServiceNameVersion;
import i5.las2peer.restMapper.data.Pair;
import i5.las2peer.security.GroupAgent;
import i5.las2peer.security.ServiceAgent;
import i5.las2peer.security.UserAgent;
import i5.las2peer.testing.MockAgentFactory;
import i5.las2peer.webConnector.WebConnector;
import i5.las2peer.webConnector.client.ClientResponse;
import i5.las2peer.webConnector.client.MiniClient;

/**
 * Example Test Class demonstrating a basic JUnit test structure.
 *
 */
public class BadgeServiceTest {

	private static final String HTTP_ADDRESS = "http://127.0.0.1";
//	private static final int HTTP_PORT = WebConnector.DEFAULT_HTTP_PORT;
	private static final int HTTP_PORT = 8081;
	
	private static LocalNode node;
	private static WebConnector connector;
	private static ByteArrayOutputStream logStream;

	private static MiniClient c1, c2, c3, ac;

	private static UserAgent user1, user2, user3, anon;

	// during testing, the specified service version does not matter
	private static final ServiceNameVersion testBadgeServiceClass = new ServiceNameVersion(BadgeService.class.getCanonicalName(),"0.1");
	
	private static final String mainPath = "badges/";

	private static final String appId = "testIdtestt";
	private static final String badgeId = "badgeIdTest";
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
		
		node.storeAgent(user1);
		node.storeAgent(user2);
		node.storeAgent(user3);

		node.launch();

		ServiceAgent testBadgeService = ServiceAgent.createServiceAgent(testBadgeServiceClass, "a pass");
		
		testBadgeService.unlockPrivateKey("a pass");
		
		node.registerReceiver(testBadgeService);

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
	
//	void createNewBadge(){
//		try
//		{
//			File badgeImage = new File("./frontend/webapps/ROOT/img/logo.png");
//			String boundary =  "--32532twtfaweafwsgfaegfawegf4"; 
//			
//			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
//			builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
//			builder.setBoundary(boundary);
//			
//			builder.addPart("badgeid", new StringBody("badgeIdTest", ContentType.TEXT_PLAIN));
//			builder.addPart("badgename", new StringBody("Badge name", ContentType.TEXT_PLAIN));
//			builder.addPart("badgedesc", new StringBody("Badge description", ContentType.TEXT_PLAIN));
//			builder.addPart("badgeimageinput", new FileBody(badgeImage, ContentType.create("image/png"), "logo.png"));
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
//			ClientResponse result = c1.sendRequest("POST", mainPath + "items/" + appId, out.toString(), "multipart/form-data; boundary="+boundary, "*/*", headers);
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
//		}
//	}
//	void deleteBadge(){
//		try
//		{
//			ClientResponse result = c1.sendRequest("DELETE",  mainPath + "items/" + appId + "/" + badgeId, "");
//	        assertEquals(200, result.getHttpCode());
//		} catch (Exception e)
//		{
//			e.printStackTrace();
//			fail("Exception: " + e);
//		}
//	}
//	/**
//	 * 
//	 * Tests get badge list
//	 * 
//	 */
//	@Test
//	public void testGetBadgeList()
//	{
//		String appId = "testIdtestt";
//		try
//		{
//			ClientResponse result = c1.sendRequest("GET", mainPath + "items/" + appId, "");
//	        assertEquals(200, result.getHttpCode());
//		} catch (Exception e)
//		{
//			e.printStackTrace();
//			fail("Exception: " + e);
//		}
//	
//	}
//	
//	/**
//	 * Tests the post new badge
//	 * 
//	 */
//	@Test
//	public void testCreateDeleteBadge()
//	{
//		createNewBadge();
//		deleteBadge();
//	}
//	
//	/**
//	 * Tests the update badge
//	 * 
//	 */
//	@Test
//	public void testUpdateBadge(){
//
//		createNewBadge();
//		try
//		{
//			File badgeImage = new File("./frontend/webapps/ROOT/img/logo.png");
//			String boundary =  "--32532twtfaweafwsgfaegfawegf4"; 
//			String badgeId = "badgeIdTest";
//			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
//			builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
//			builder.setBoundary(boundary);
//			
//			builder.addPart("badgeid", new StringBody(badgeId, ContentType.TEXT_PLAIN));
//			builder.addPart("badgename", new StringBody("Badge name", ContentType.TEXT_PLAIN));
//			builder.addPart("badgedesc", new StringBody("Badge description", ContentType.TEXT_PLAIN));
//			builder.addPart("badgeimageinput", new FileBody(badgeImage, ContentType.create("image/png"), "logo.png"));
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
//			ClientResponse result = c1.sendRequest("PUT", mainPath + "items/" + appId +"/"+ badgeId, out.toString(), "multipart/form-data; boundary="+boundary, "*/*", headers);
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
//		}
//		deleteBadge();
//		
//	}
//	
//	/**
//	 * 
//	 * Tests get badge image
//	 * 
//	 */
//	@Test
//	public void testGetBadgeImage()
//	{
//		createNewBadge();
//		try
//		{
//			ClientResponse result = c1.sendRequest("GET", mainPath + "items/" + appId + "/" + badgeId + "/img", "");
//	        assertEquals(200, result.getHttpCode());
//		} catch (Exception e)
//		{
//			e.printStackTrace();
//			fail("Exception: " + e);
//		}
//		deleteBadge();
//
//	}


	/**
	 * Test the TemplateService for valid rest mapping.
	 * Important for development.
	 */
	@Test
	public void testDebugMapping()
	{
		BadgeService cl = new BadgeService();
		assertTrue(cl.debugMapping());
	}

}

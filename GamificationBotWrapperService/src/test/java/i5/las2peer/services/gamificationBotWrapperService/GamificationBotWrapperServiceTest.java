package i5.las2peer.services.gamificationBotWrapperService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

import i5.las2peer.p2p.LocalNode;
import i5.las2peer.p2p.LocalNodeManager;
import i5.las2peer.api.p2p.ServiceNameVersion;
import i5.las2peer.security.UserAgentImpl;
import i5.las2peer.testing.MockAgentFactory;
import i5.las2peer.connectors.webConnector.WebConnector;
import i5.las2peer.connectors.webConnector.client.ClientResponse;
import i5.las2peer.connectors.webConnector.client.MiniClient;

/**
 * Example Test Class demonstrating a basic JUnit test structure.
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GamificationBotWrapperServiceTest {

	private static final int HTTP_PORT = 8081;

	private static LocalNode node;
	private static WebConnector connector;
	private static ByteArrayOutputStream logStream;

	private static MiniClient c1, c2, c3;

	private static UserAgentImpl user1, user2, user3;

	private static String appId = "test";
	private static String memberId = "user1";
	private static String questId = "quest1";
	private static String badgeId = "badge1";
	private static String actionId = "action1";
	private static String achievementId = "achievement1";
	private static String streakId = "streak1";

	private static final String mainPath = "gamification/bots/";

	// to fetch data per batch
	int currentPage = 1;
	int windowSize = 10;
	String searchParam = "";

	String unitName = "dollar";

	/**
	 * Called before the tests start.
	 * 
	 * Sets up the node and initializes connector and users that can be used
	 * throughout the tests.
	 * 
	 * @throws Exception
	 */
	@Before
	public void startServer() throws Exception {

		// start node
		// node = LocalNode.newNode();

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
	 * Called after the test has finished. Shuts down the server and prints out the
	 * connector log file for reference.
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
	public void testA1_getPointOfMember() {
		System.out.println("Test --- Get Point Of Member");
		try {
			ClientResponse result = c1.sendRequest("GET", mainPath + "points/" + appId + "/" + memberId, ""); // testInput
																												// is
			System.out.println(result.getResponse());
			assertEquals(200, result.getHttpCode());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: " + e);
			System.exit(0);
		}
	}

	@Test
	public void testA2_getStatusOfMember() {
		System.out.println("Test --- Get Point And Level Of Member");
		try {
			ClientResponse result = c1.sendRequest("GET", mainPath + "status/" + appId + "/" + memberId, ""); // testInput
																												// is
			System.out.println(result.getResponse());
			assertEquals(200, result.getHttpCode());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: " + e);
			System.exit(0);
		}
	}

	@Test
	public void testA3_getBadgesOfMember() {
		System.out.println("Test --- Get Badges Of Member");
		try {
			ClientResponse result = c1.sendRequest("GET", mainPath + "badges/" + appId + "/" + memberId, ""); // testInput
																												// is
			System.out.println(result.getResponse());
			assertEquals(200, result.getHttpCode());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: " + e);
			System.exit(0);
		}
	}

	@Test
	public void testA4_getQuestsWithStatusOfMember() {
		System.out.println("Test --- Get Quests With Status Of Member");
		try {
			ClientResponse result = c1.sendRequest("GET",
					mainPath + "quests/" + appId + "/" + memberId + "/status/COMPLETED", ""); 
			System.out.println(result.getResponse());
			assertEquals(200, result.getHttpCode());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: " + e);
			System.exit(0);
		}
		try {
			ClientResponse result = c1.sendRequest("GET",
					mainPath + "quests/" + appId + "/" + memberId + "/status/REVEALED", ""); 
			System.out.println(result.getResponse());
			assertEquals(200, result.getHttpCode());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: " + e);
			System.exit(0);
		}
	}

	@Test
	public void testA5_getQuestProgressOfMember() {
		System.out.println("Test --- Get Quests With Progress Of Member");
		try {
			ClientResponse result = c1.sendRequest("GET",
					mainPath + "quests/" + appId + "/" + memberId + "/progress/" + questId, ""); 
			System.out.println(result.getResponse());
			assertEquals(200, result.getHttpCode());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: " + e);
			System.exit(0);
		}
	}

	@Test
	public void testA6_getAchievementsOfMember() {
		System.out.println("Test --- Get Quests With Progress Of Member");
		try {
			ClientResponse result = c1.sendRequest("GET", mainPath + "achievements/" + appId + "/" + memberId, ""); // testInput
																													// is
			System.out.println(result.getResponse());
			assertEquals(200, result.getHttpCode());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: " + e);
			System.exit(0);
		}
	}

	@Test
	public void testA7_getBadgeImageDetail() {
		System.out.println("Test --- Get Badge Image Detail");
		try {
			ClientResponse result = c1.sendRequest("GET",
					mainPath + "badges/" + appId + "/" + memberId + "/" + badgeId + "/img", ""); 
			System.out.println(result.getResponse());
			assertEquals(200, result.getHttpCode());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: " + e);
			System.exit(0);
		}
	}

	@Test
	public void testA8_getBadgeDetailWithId() {
		System.out.println("Test --- Get Badge Detail With ID");
		try {
			ClientResponse result = c1.sendRequest("GET", mainPath + "badges/" + appId + "/" + memberId + "/" + badgeId,
					""); 
			System.out.println(result.getResponse());
			assertEquals(200, result.getHttpCode());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: " + e);
			System.exit(0);
		}
	}

	@Test
	public void testA9_getQuestDetailWithId() {
		System.out.println("Test --- Get Quest Detail With ID");
		try {
			ClientResponse result = c1.sendRequest("GET", mainPath + "quests/" + appId + "/" + memberId + "/" + questId,
					""); 
			System.out.println(result.getResponse());
			assertEquals(200, result.getHttpCode());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: " + e);
			System.exit(0);
		}
	}

	@Test
	public void testB1_getAchievementDetailWithId() {
		System.out.println("Test --- Get Achievement Detail With ID");
		try {
			ClientResponse result = c1.sendRequest("GET",
					mainPath + "achievements/" + appId + "/" + memberId + "/" + achievementId, ""); 
			System.out.println(result.getResponse());
			assertEquals(200, result.getHttpCode());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: " + e);
			System.exit(0);
		}
	}

	@Test
	public void testB2_triggerAction() {
		System.out.println("Test --- Trigger Action");
		try {
			ClientResponse result = c1.sendRequest("POST",
					mainPath + "actions/" + appId + "/" + actionId + "/" + memberId, ""); 
			System.out.println(result.getResponse());
			assertEquals(200, result.getHttpCode());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: " + e);
			System.exit(0);
		}
	}

	@Test
	public void testB3_getLocalLeaderboard() {
		System.out.println("Test --- Get Local Leaderboard");

		try {
			ClientResponse result = c1.sendRequest("GET",
					mainPath + "leaderboard/local/" + appId + "/" + memberId + "?current=1&rowCount=10&searchPhrase=",
					""); 
			System.out.println(result.getResponse());
			assertEquals(200, result.getHttpCode());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: " + e);
			System.exit(0);
		}
	}

	@Test
	public void testB4_getGlobalLeaderboard() {
		System.out.println("Test --- Get Global Leaderboard");

		try {
			ClientResponse result = c1.sendRequest("GET",
					mainPath + "leaderboard/global/" + appId + "/" + memberId + "?current=1&rowCount=10&searchPhrase=",
					""); 
			System.out.println(result.getResponse());
			assertEquals(200, result.getHttpCode());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: " + e);
			System.exit(0);
		}
	}

	@Test
	public void testC6_getMemberStreaks() {
		System.out.println("Test --- Get Streaks fo Memeber");

		try {
			ClientResponse result = c1.sendRequest("GET", mainPath + "streaks/" + appId + "/" + memberId, "");
			System.out.println(result.getResponse());
			assertEquals(200, result.getHttpCode());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: " + e);
			System.exit(0);
		}
	}

	@Test
	public void testC7_getStreakDetailWithId() {
		System.out.println("Test --- Get Streak Detail With ID");
		try {
			ClientResponse result = c1.sendRequest("GET",
					mainPath + "streaks/" + appId + "/" + memberId + "/" + streakId, ""); 
			System.out.println(result.getResponse());
			assertEquals(200, result.getHttpCode());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: " + e);
			System.exit(0);
		}
	}

	@Test
	public void testC8_getStreakProgressOfMember() {
		System.out.println("Test --- Get Streak Progress Of Member");
		try {
			ClientResponse result = c1.sendRequest("GET",
					mainPath + "streaks/" + appId + "/" + memberId + "/progress/" + streakId, ""); 
			System.out.println(result.getResponse());
			assertEquals(200, result.getHttpCode());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: " + e);
			System.exit(0);
		}
	}
	
	@Test
	public void testC9_getTransitiveStreakProgressOfMember() {
		System.out.println("Test --- Get Streak Progress Of Member");
		try {
			ClientResponse result = c1.sendRequest("GET",
					mainPath + "streaks/accumulative/" + appId + "/" + memberId + "/" + streakId, ""); 
			System.out.println(result.getResponse());
			assertEquals(200, result.getHttpCode());
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: " + e);
			System.exit(0);
		}
	}
}

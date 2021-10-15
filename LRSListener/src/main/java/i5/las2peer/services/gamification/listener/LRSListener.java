package i5.las2peer.services.gamification.listener;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import java.io.Reader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;

@SuppressWarnings("unused")
public class LRSListener {
	/**
	 * Main method. Starts the application and contains the listening loop.
	 * 
	 * @param args arguments will be ignored
	 * @throws SQLException
	 */
	public static void main(String[] args) throws SQLException {
		HttpClient client = new HttpClient();
		client.setToken(
				"Basic NjM2NmZiZDgzNDU5M2M3MDU5ODU3ZTg4ODQwYjMyZGRmMTY1NjQwMzo0MGUxZjRlZmRlNDFlM2JlZTFiMWJlOTIxNDQ1ODc5OWEwMWZhNDAy\"");
		client.setBaseURL("https://lrs.tech4comp.dbis.rwth-aachen.de/api/connection/statement");
		while (true) {
			try {
				// Get xAPI statements
				HashMap<String, String> map = new HashMap<>();
				map.put("Content-Type", "application/json; charset=utf-8");
				map.put("Authorization", "");
				String result = client.executeGet(
						"?filter=%7B%22%24and%22%3A%5B%7B%22%24comment%22%3A%22%7B%5C%22criterionLabel%5C%22%3A%5C%22A%5C%22%2C%5C%22criteriaPath%5C%22%3A%5B%5C%22person%5C%22%5D%7D%22%2C%22person._id%22%3A%7B%22%24in%22%3A%5B%7B%22%24oid%22%3A%225db02311c5dcd9003d904ba4%22%7D%5D%7D%7D%5D%7D&sort=%7B%22timestamp%22%3A-1%2C%22_id%22%3A1%7D",
						map);
				client.disconnect();
				if (result != null) {
					JSONObject[] relevantStatements = parseToJson(result);
					// This is the correct usage, but for testing, we only use one statement
//					for (JSONObject jsonObject : relevantStatements) {
					// Test code begins here
					JSONObject jsonObject = relevantStatements[0];
					String[] gameDetails = getGameDetails(jsonObject);
//					System.out.println(gameDetails[0] + " " + gameDetails[1] + " " + gameDetails[2]);
					gameDetails[0] = "Marc Belsch";
					gameDetails[1] = "created";
					gameDetails[2] = "gamifiication/game";
					// Test Code ends here
					System.out.println(gameDetails[0] + " " + gameDetails[1] + " " + gameDetails[2]);
					HttpClient gameClient = new HttpClient();
					gameClient.setBaseURL("http://localhost:8080/");
					gameClient.setToken("");
					HashMap<String, String> gameMap = new HashMap<>();
					gameMap.put("Content-Type", "multipart/form-data; boundary=-");
					gameMap.put("Authorization", "");
					executeGamification(gameDetails, gameClient, gameMap);
//					}
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						System.out.println("Interrupted sleep. How rude!");
					}
				}
				System.out.println("Still running");
			} catch (Exception e) {
				System.out.println("Uppsi got an error");
				System.err.println(e.getMessage());
				System.err.println(e.getStackTrace());
			} finally {
				client.disconnect();
			}
		}
	}

	/**
	 * 
	 * @param jsonObject
	 * @return values to node.statement.actor/verb/object
	 */
	private static String[] getGameDetails(JSONObject jsonObject) {
		String[] result = new String[3];
		JSONObject statement = jsonObject.getJSONObject("node").getJSONObject("statement");
		result[0] = statement.getJSONObject("actor").getString("name");
		result[1] = statement.getJSONObject("verb").getJSONObject("display").getString("en-US");
		result[2] = statement.getJSONObject("object").getJSONObject("definition").getJSONObject("name")
				.getString("en-US");
		return result;
	}

	//This will be used after testing
//	/**
//	 * 
//	 * @param jsonObject
//	 * @return values to node.statement.actor/verb/object
//	 */
//	private static JSONObject[] getGameDetails(JSONObject jsonObject) {
//		String[] result = new String[3];
//		JSONObject statement = jsonObject.getJSONObject("node").getJSONObject("statement");
//		result[0] = statement.getJSONObject("actor");
//		result[1] = statement.getJSONObject("verb").getJSONObject("display");
//		result[2] = statement.getJSONObject("object").getJSONObject("definition").getJSONObject("name");
//		result[3] = statement.getJSONObject("object").getJSONObject("metadata");
//		return result;
//	}

	/**
	 * 
	 * @param line
	 * @return JSONObject Array, containing JSONObjects, each starting with
	 *         {"cursor":...
	 */
	private static JSONObject[] parseToJson(String line) {
		JSONObject object = new JSONObject(line);
		JSONArray array = object.getJSONArray("edges");
		JSONObject[] objectArray = new JSONObject[array.length()];
		for (int i = 0; i < array.length(); i++) {
			objectArray[i] = array.getJSONObject(i);
		}
		return objectArray;
	}

	/**
	 * 
	 * @param gameClient
	 * @param gameMap
	 * @return URL address as String to fetch all xAPI statements related to
	 *         Gamification Framework activities
	 */

	private static void executeGamification(String[] gamificationDetails, HttpClient gameClient,
			Map<String, String> gameMap) {
		String who = gamificationDetails[0];
		String did = gamificationDetails[1];
		String what = gamificationDetails[2];
		JSONObject metadata = new JSONObject();
//		String who = gamificationDetails[0].getString("name");
//		String did = gamificationDetails[1].getString("en-US");
//		String what = gamificationDetails[2].getString("en-US");
//		JSONObject metadata = new JSONObject();
		if (who != null && did != null && what != null) {
			try {
				switch (what) {
				case "gamifiication/game":
					executeGame(who, did, what, metadata, gameClient, gameMap);
					break;
				case "gamifiication/gamifier":
					executeGamifier(who, did, what, metadata, gameClient, gameMap);
					break;
				case "gamifiication/visualization":
					executeVisualization(who, did, what, metadata, gameClient, gameMap);
					break;
				case "gamifiication/achievement":
					executeAchievement(who, did, what, metadata, gameClient, gameMap);
					break;
				case "gamifiication/quest":
					executeQuest(who, did, what, metadata, gameClient, gameMap);
					break;
				case "gamifiication/badge":
					executeBadge(who, did, what, metadata, gameClient, gameMap);
					break;
				case "gamifiication/level":
					executeLevel(who, did, what, metadata, gameClient, gameMap);
					break;
				case "gamifiication/action":
					executeAction(who, did, what, metadata, gameClient, gameMap);
					break;
				case "gamifiication/point":
					executePoint(who, did, what, metadata, gameClient, gameMap);
					break;
				case "gamifiication/streak":
					executeStreak(who, did, what, metadata, gameClient, gameMap);
					break;
				default:
					throw new IllegalArgumentException("Unexpected value: " + what);
				}
			} catch (IllegalArgumentException e) {
				System.out.println("Activity " + what + " is not supported");
			}
		}
	}

	private static void executeStreak(String who, String did, String what, JSONObject metadata, HttpClient client,
			Map<String, String> gameMap) {
		try {
			gameMap.put("loginName", who);
			//TODO: parse metadata
			String result = null;
			String body = null;
			switch (did) {
			case "created":
				System.out.println("created");
				result = client.executePost("gamification/streaks/data", gameMap, body);
				break;
			case "submitted":
				System.out.println("submitted");
				break;
			default:
				throw new IllegalArgumentException("Unexpected value: " + what);
			}
		} catch (IllegalArgumentException e) {
			System.out.println("Verb " + did + "is not suported for activity " + what);
		}catch (IOException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	private static void executeGame(String who, String did, String what, JSONObject metadata, HttpClient client,
			Map<String, String> gameMap) {
		try {
			gameMap.put("loginName", who);
			//TODO: parse metadata
			String result = null;
			String gameId = null;
			String memberId = null;
			String gameDescription = null;
			String commType = null;
			switch (did) {
			case "created":
				System.out.println("created");
				String[] gameDetails = new String[6];
				gameDetails[0] = "gameID";
				gameDetails[1] = gameId;
				gameDetails[2] = "gamedesc";
				gameDetails[3] = gameDescription;
				gameDetails[4] = "commtype";
				gameDetails[5] = commType;
				String body = buildMultiPart(gameDetails);
				result = client.executePost("gamification/games/data", gameMap, body);
				System.out.println(result);
				break;
			case "deleted":
				System.out.println("deleted");
				result = client.executeDelete("gamification/games/data/" + gameId);
				System.out.println(result);
				break;
			case "addedUser":
				System.out.println("deleted");
				result = client.executePost("gamification/games/data/" + gameId + "/" + memberId);
				System.out.println(result);
				break;
			case "removedUser":
				System.out.println("deleted");
				result = client.executeDelete("gamification/games/data/" + gameId + "/" + memberId);
				System.out.println(result);
				break;
			case "vaildatedUser":
				System.out.println("validatedUser");
				result = client.executePost("gamification/games/validation");
				System.out.println(result);
				break;
			default:
				throw new IllegalArgumentException("Unexpected value: " + what);
			}
		} catch (IllegalArgumentException e) {
			System.out.println("Verb " + did + "is not suported for activity " + what);
		} catch (IOException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	private static void executeQuest(String who, String did, String what, JSONObject metadata, HttpClient client,
			Map<String, String> gameMap) {
		try {
			gameMap.put("loginName", who);
			//TODO: parse metadata
			String result = null;
			String gameId = null;
			String questId = null;
			String achievementId = null;
			String actionids = null;
			String questName = null;
			String questDiscription = null;
			String questStatus = null;
			String questPointFlag = null;
			String questPointValue = null;
			String questQuestFlag = null;
			String questNotificationCheck = null;
			String questNotificationMessage = null;
			JSONObject body = new JSONObject();
			body.put("questid", questId);
			body.put("questname", questName);
			body.put("questdescription", questDiscription);
			body.put("queststatus", questStatus);
			body.put("questpointflag", questPointFlag);
			body.put("questpointvalue", questPointValue);
			body.put("questquestflag", questQuestFlag);
			body.put("questidcompleted", questId);
			body.put("questactionids", actionids);
			body.put("questachievementid", achievementId);
			body.put("questnotificationcheck", questNotificationCheck);
			body.put("questnotificationmessage", questNotificationMessage);
			switch (did) {
			case "created":
				System.out.println("created");
				result = client.executePost("gamification/quests/" + gameId, gameMap, body);
				System.out.println(result);
				break;
			case "updated":
				System.out.println("updated");
				result = client.executePut("gamification/quests/" + gameId + "/" + questId, gameMap, body);
				System.out.println(result);
				break;
			case "deleted":
				System.out.println("deleted");
				result = client.executeDelete("gamification/quests/" + gameId + "/" + questId);
				System.out.println(result);
				break;
			default:
				throw new IllegalArgumentException("Unexpected value: " + what);
			}
		} catch (IllegalArgumentException e) {
			System.out.println("Verb " + did + "is not suported for activity " + what);
		} catch (IOException e) {
			e.getMessage();
			e.printStackTrace();
		}
	}

	private static void executeAchievement(String who, String did, String what, JSONObject metadata, HttpClient client,
			Map<String, String> gameMap) {
		try {
			gameMap.put("loginName", who);
			//TODO: parse metadata
			String result = null;
			String gameId = null;
			String achievementId = null;
			String achievementName = null;
			String achievementDescription = null;
			String achievementPointValue = null;
			String achievementBadgeId = null;
			String achievementNotificationCheck = null;
			String achievementNotificationMessage = null;
			String[] gameDetails = new String[14];
			gameDetails[0] = "achievementid";
			gameDetails[1] = achievementId;
			gameDetails[2] = "achievementname";
			gameDetails[3] = achievementName;
			gameDetails[4] = "achievementdesc";
			gameDetails[5] = achievementDescription;
			gameDetails[0] = "achievementpointvalue";
			gameDetails[1] = achievementPointValue;
			gameDetails[2] = "achievementbadgeid";
			gameDetails[3] = achievementBadgeId;
			gameDetails[4] = "achievementnotificationcheck";
			gameDetails[5] = achievementNotificationCheck;
			gameDetails[4] = "achievementnotificationmessage";
			gameDetails[5] = achievementNotificationMessage;
			String body = buildMultiPart(gameDetails);
			switch (did) {
			case "created":
				System.out.println("created");
				result = client.executePost("gamification/achievements/" + gameId, gameMap, body);
				System.out.println(result);
				break;
			case "updated":
				System.out.println("updated");
				result = client.executePut("gamification/achievements/" + gameId + "/" + achievementId, gameMap, body);
				System.out.println(result);
				break;
			case "deleted":
				System.out.println("deleted");
				result = client.executeDelete("gamification/achievements/" + gameId + "/" + achievementId);
				System.out.println(result);
				break;
			default:
				throw new IllegalArgumentException("Unexpected value: " + what);
			}
		} catch (IllegalArgumentException e) {
			System.out.println("Verb " + did + "is not suported for activity " + what);
		} catch (IOException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	private static void executeBadge(String who, String did, String what, JSONObject metadata, HttpClient client,
			Map<String, String> gameMap) {
		try {
			gameMap.put("loginName", who);
			//TODO: parse metadata
			String result = null;
			String gameId = null;
			String badgeId = null;
			String badgeName = null;
			String badgeDescription = null;
			String badgeImageInput = null;
			String dev = null;
			String badgeNotificationCheck = null;
			String badgeNotificationMessage = null;
			String[] gameDetails = new String[14];
			gameDetails[0] = "badgeid";
			gameDetails[1] = badgeId;
			gameDetails[2] = "badgename";
			gameDetails[3] = badgeName;
			gameDetails[4] = "badgedesc";
			gameDetails[5] = badgeDescription;
			gameDetails[0] = "badgeimageinput";
			gameDetails[1] = badgeImageInput;
			gameDetails[2] = "dev";
			gameDetails[3] = dev;
			gameDetails[4] = "badgenotificationcheck";
			gameDetails[5] = badgeNotificationCheck;
			gameDetails[4] = "badgenotificationmessage";
			gameDetails[5] = badgeNotificationMessage;
			//TODO : checkBadgeFile upload
			String body = buildMultiPart(gameDetails);
			switch (did) {
			case "created":
				System.out.println("created");
				result = client.executePost("gamification/badges/" + gameId, gameMap, body);
				System.out.println(result);
				break;
			case "updated":
				System.out.println("updated");
				result = client.executePut("gamification/badges/" + gameId + "/" + badgeId, gameMap, body);
				System.out.println(result);
				break;
			case "deleted":
				System.out.println("deleted");
				result = client.executeDelete("gamification/badges/" + gameId + "/" + badgeId);
				System.out.println(result);
				break;
			default:
				throw new IllegalArgumentException("Unexpected value: " + what);
			}
		} catch (IllegalArgumentException e) {
			System.out.println("Verb " + did + "is not suported for activity " + what);
		} catch (IOException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	private static void executeAction(String who, String did, String what, JSONObject metadata, HttpClient client,
			Map<String, String> gameMap) {
		try {
			gameMap.put("loginName", who);
			//TODO: parse metadata
			String result = null;
			String gameId = null;
			String actionId = null;
			String actionName = null;
			String actionDescription = null;
			String actionPointValue = null;
			String actionNotificationCheck = null;
			String actionNotificationMessage = null;
			String[] gameDetails = new String[12];
			gameDetails[0] = "actionid";
			gameDetails[1] = actionId;
			gameDetails[2] = "actionname";
			gameDetails[3] = actionName;
			gameDetails[4] = "actiondesc";
			gameDetails[5] = actionDescription;
			gameDetails[6] = "actionpointvalue";
			gameDetails[7] = actionPointValue;
			gameDetails[8] = "actionnotificationcheck";
			gameDetails[9] = actionNotificationCheck;
			gameDetails[10] = "actionnotificationmessage";
			gameDetails[11] = actionNotificationMessage;
			String body = buildMultiPart(gameDetails);
			switch (did) {
			case "created":
				System.out.println("created");
				result = client.executePost("gamification/actions/" + gameId, gameMap, body);
				System.out.println(result);
				break;
			case "updated":
				System.out.println("updated");
				result = client.executePut("gamification/actions/" + gameId + "/" + actionId, gameMap, body);
				System.out.println(result);
				break;
			case "deleted":
				System.out.println("deleted");
				result = client.executeDelete("gamification/actions/" + gameId + "/" + actionId);
				System.out.println(result);
				break;
			default:
				throw new IllegalArgumentException("Unexpected value: " + what);
			}
		} catch (IllegalArgumentException e) {
			System.out.println("Verb " + did + "is not suported for activity " + what);
		} catch (IOException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	private static void executeLevel(String who, String did, String what, JSONObject metadata, HttpClient client,
			Map<String, String> gameMap) {
		try {
			gameMap.put("loginName", who);
			//TODO: parse metadata
			String result = null;
			String gameId = null;
			String levelId = null;
			String levelName = null;
			String levelPointValue = null;
			String levelNotificationCheck = null;
			String levelNotificationMessage = null;
			String[] gameDetails = new String[10];
			gameDetails[0] = "levelnum";
			gameDetails[1] = levelId;
			gameDetails[2] = "levelname";
			gameDetails[3] = levelName;
			gameDetails[4] = "levelpointvalue";
			gameDetails[5] = levelPointValue;
			gameDetails[6] = "levelnotificationcheck";
			gameDetails[7] = levelNotificationCheck;
			gameDetails[8] = "levelnotificationmessage";
			gameDetails[9] = levelNotificationMessage;
			String body = buildMultiPart(gameDetails);
			switch (did) {
			case "created":
				System.out.println("created");
				result = client.executePost("gamification/levels/" + gameId, gameMap, body);
				System.out.println(result);
				break;
			case "updated":
				System.out.println("updated");
				result = client.executePut("gamification/levels/" + gameId + "/" + levelId, gameMap, body);
				System.out.println(result);
				break;
			case "deleted":
				System.out.println("deleted");
				result = client.executeDelete("gamification/levels/" + gameId + "/" + levelId);
				System.out.println(result);
				break;
			default:
				throw new IllegalArgumentException("Unexpected value: " + what);
			}
		} catch (IllegalArgumentException e) {
			System.out.println("Verb " + did + "is not suported for activity " + what);
		} catch (IOException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	private static void executePoint(String who, String did, String what, JSONObject metadata, HttpClient client,
			Map<String, String> gameMap) {
		try {
			//TODO: parse metadata
			String result = null;
			String gameId = null;
			String unitName = null;
			switch (did) {
			case "updated":
				System.out.println("updated");
				result = client.executePut("gamification/points/" + gameId + "/name/" + unitName);
				System.out.println(result);
				break;
			default:
				throw new IllegalArgumentException("Unexpected value: " + what);
			}
		} catch (IllegalArgumentException e) {
			System.out.println("Verb " + did + "is not suported for activity " + what);
		} catch (IOException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	private static void executeVisualization(String who, String did, String what, JSONObject metadata,
			HttpClient client, Map<String, String> gameMap) {
		System.out.println("VisualizationService not supported yet");
	}

	private static void executeGamifier(String who, String did, String what, JSONObject metadata, HttpClient client,
			Map<String, String> gameMap) {
		System.out.println("GamifierService not supported yet");
	}

	private static String buildMultiPart(String[] details) {
		StringBuilder sb = new StringBuilder();
		if (details.length % 2 == 0) {
			for (int i = 0; i < details.length; i += 2) {
				sb.append("---\r\n");
				sb.append("Content-Disposition: form-data; name=\"" + details[i] + "\"");
				sb.append("Content-Type: text/plain; charset=\"utf-8\"");
				sb.append("\r\n");
				sb.append(details[i + 1]);
				sb.append("\r\n\"");
			}
		}
		return sb.toString();
	}

	public static class HttpClient {
		private HttpURLConnection connection;
		private String token;
		private String baseURL;

		private void setHeaders(Map<String, String> map) {
			for (Map.Entry<String, String> entry : map.entrySet()) {
				if (entry.getKey().equals("Authorization")) {
					connection.setRequestProperty(entry.getKey(), getToken());
				} else {
					connection.setRequestProperty(entry.getKey(), entry.getValue());
				}
			}
		}

		private void setBody(Object body) {
			try {
				OutputStream output = connection.getOutputStream();
				output.write(body.toString().getBytes("utf-8"));
				output.flush();
			} catch (Exception e) {
				e.getMessage();
				e.printStackTrace();
			}
		}

		public void disconnect() {
			if (connection != null) {
				connection.disconnect();
			}
		}

		public String getToken() {
			return token;
		}

		public void setToken(String token) {
			this.token = token;
		}

		public String getBaseURL() {
			return baseURL;
		}

		public void setBaseURL(String baseURL) {
			this.baseURL = baseURL;
		}

		public String executePost(String path) throws IOException {
			return executePost(path, null, null);
		}

		public String executePost(String path, Map<String, String> map, Object body) throws IOException {
			return execute("POST", path, map, body);
		}

		public String executeGet(String path, Map<String, String> map) throws IOException {
			return execute("GET", path, map, null);
		}

		public String executePut(String path) throws IOException {
			return executePut(path, null, null);
		}

		public String executePut(String path, Map<String, String> gameMap, Object body) throws IOException {
			return execute("PUT", path, null, body);
		}

		public String executeDelete(String path) throws IOException {
			return execute("DELETE", path, null, null);
		}

		public String execute(String method, String path, Map<String, String> map, Object body) throws IOException {
			URL url = new URL(getBaseURL() + path);
			connection = (HttpURLConnection) url.openConnection();
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setRequestMethod(method);
			if (map != null) {
				setHeaders(map);
			}
			if (method.equals("POST") && body != null) {
				setBody(body);
			}
			StringBuilder sb = new StringBuilder();
			BufferedReader br = null;
			if (!(connection.getResponseCode() <= 299 && connection.getResponseCode() >= 200)) {
				br = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
			} else {
				br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			}
			String line = null;
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			return sb.toString();
		}

	}
}

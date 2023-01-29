package i5.las2peer.services.gamificationBotWrapperService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import java.sql.Connection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.ws.rs.core.MediaType;

import ch.qos.logback.core.net.server.Client;
import i5.las2peer.api.Context;
import i5.las2peer.api.execution.InternalServiceException;
import i5.las2peer.api.execution.ServiceAccessDeniedException;
import i5.las2peer.api.execution.ServiceInvocationFailedException;
import i5.las2peer.api.execution.ServiceMethodNotFoundException;
import i5.las2peer.api.execution.ServiceNotAuthorizedException;
import i5.las2peer.api.execution.ServiceNotAvailableException;
import i5.las2peer.api.execution.ServiceNotFoundException;
import i5.las2peer.api.security.AgentAccessDeniedException;
import i5.las2peer.api.security.AgentAlreadyExistsException;
import i5.las2peer.api.security.AgentLockedException;
import i5.las2peer.api.security.AgentOperationFailedException;
import i5.las2peer.connectors.webConnector.client.ClientResponse;
import i5.las2peer.connectors.webConnector.client.MiniClient;
import i5.las2peer.restMapper.RESTService;
import i5.las2peer.security.BotAgent;
import i5.las2peer.tools.CryptoException;

import i5.las2peer.services.gamification.commons.database.DatabaseManager;

import i5.las2peer.services.gamificationBotWrapperService.database.ProfileDAO;

public class LrsBotWorker implements Runnable {

	private DatabaseManager dbm;

	private String timeStamp = "0";
	private String lrsToken = "";
	private String botName = "";
	private String game = "";
	private double streakReminder;
	private HashMap<String, Boolean> users = new HashMap<String, Boolean>();
	private HashMap<String, String> usersChannel = new HashMap<String, String>();

	private BotAgent restarterBot = null;
	private HashMap<String, JSONArray> actionVerbs = new HashMap<String, JSONArray>();

	private HashMap<String, JSONObject> userStreaks = new HashMap<String, JSONObject>();
	private HashMap<String, JSONObject> achievementsMap = new HashMap<String, JSONObject>();

	private HashMap<String, JSONArray> userLRS = new HashMap<String, JSONArray>();

	private String sbmURL = "";
	private String gameURL = "";
	private String streakMessage = "";

	private ProfileDAO profileAcess;

	public LrsBotWorker(String game, String botName, BotAgent restarterBot, String lrsToken,
			HashMap<String, JSONArray> actionVerbs, HashMap<String, JSONObject> achievementsMap, String streakReminder,
			String sbmURL, String gameURL,
			String streakMessage, DatabaseManager dbm) {

		this.game = game;
		this.botName = botName;
		this.restarterBot = restarterBot;
		this.lrsToken = lrsToken;
		this.actionVerbs = actionVerbs;
		this.achievementsMap = achievementsMap;
		this.streakReminder = Double.valueOf(streakReminder);
		this.sbmURL = sbmURL;
		this.gameURL = gameURL;
		this.streakMessage = streakMessage;
		this.profileAcess = new ProfileDAO();
		this.dbm = dbm;
	}

	public String getGameURL() {
		return this.gameURL;
	}

	public void addUsers(String email, String channel, DatabaseManager dbm) {
		this.users.put(email, false);
		this.usersChannel.put(email, channel);

		JSONArray arr = new JSONArray();
		try {
			Connection conn = dbm.getConnection();
			arr = this.profileAcess.fetchStatemnets(conn, this.game, email);
			if (conn != null) {
				conn.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("size is"+arr.size());
		this.userLRS.put(email, arr);
	}

	public HashMap<String, JSONArray> getActionVerbs() {
		return actionVerbs;
	}

	// here, the whole process of communicating with the GF should take place to
	// trigger actions
	@Override
	public void run() {
		System.out.println("10" + this.botName);
		while (!Thread.currentThread().isInterrupted()) {
			// will need to do timestamp per user and not per instance of bot
			// will need to check for the "moreStatements" field in the response for more
			// statements...
			for (String user : users.keySet()) {
				System.out.println("Fetching for: " + user);
				JSONArray storedStatements = this.userLRS.get(user);
				JSONParser parser = new JSONParser(0);
				try {
					JSONParser p = new JSONParser(JSONParser.MODE_PERMISSIVE);
					JSONObject acc = (JSONObject) p.parse(new String("{'account': { 'name': '" +
							user
							+ "', 'homePage': 'https://chat.tech4comp.dbis.rwth-aachen.de'}}"));
					URL url = new URL("https://lrs.tech4comp.dbis.rwth-aachen.de" +
							"/data/xAPI/statements?agent="
							+ acc.toString() + "&since=" + timeStamp);

					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					conn.setRequestMethod("GET");
					conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
					conn.setRequestProperty("X-Experience-API-Version", "1.0.3");
					conn.setRequestProperty("Authorization", "Basic "
							+
							this.lrsToken);
					conn.setRequestProperty("Cache-Control", "no-cache");
					conn.setUseCaches(false);
					BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
					String inputLine;
					StringBuffer response = new StringBuffer();
					while ((inputLine = in.readLine()) != null) {
						response.append(inputLine);
					}
					in.close();
					conn.disconnect();
					JSONObject jsonBody = (JSONObject) p.parse(response.toString());

					JSONArray statements = (JSONArray) jsonBody.get("statements");
					System.out.println("statements coming" + statements.size());
					if (statements.size() > 0) {
						int occ = 0;
						for (Object statement : statements) {
							JSONObject s = (JSONObject) statement;
							String statementId = s.get("id").toString();
							// check whether statement has already been processed
							boolean contains = false;
							for (Object id : storedStatements) {
								String sId = id.toString();
								if (sId.equals(statementId)) {
									contains = true;
									break;
								}
							}
							if (storedStatements != null && contains) {
								continue;
							} else {
								Connection connDB = dbm.getConnection();
								this.profileAcess.addStatemnetId(connDB, this.game, user, statementId);
								if (connDB != null) {
									connDB.close();
								}
							}
							String verbId = ((JSONObject) s.get("verb")).get("id").toString();
							String objectId = ((JSONObject) ((JSONObject) ((JSONObject) s.get("object"))
									.get("definition")).get("name")).get("en-US").toString();
							if (this.actionVerbs.keySet().contains(verbId.split("verb/")[1])) {
								for (Object o : this.actionVerbs.get(verbId.split("verb/")[1])) {

									JSONObject jsonO = (JSONObject) o;
									System.out.println("checking stuff for action id: " + jsonO.get("id").toString());
									if (jsonO.containsKey("lrsAttribute")
											&& !jsonO.get("lrsAttribute").toString().equals("")) {
										System.out.println(
												"to match attribute is:+ " + jsonO.get("lrsAttribute").toString()
														+ " with value " + jsonO.get("lrsAttributeValue").toString());
										if (!jsonO.get("lrsAttributeValue").toString()
												.equals(recursive(s, jsonO.get("lrsAttribute").toString()))) {
											System.out.println("attributes do not match");
											continue;
										}
									}
									try {
										String actionId = jsonO.get("id").toString();
										System.out.println(this.gameURL +
												"/gamification/visualization/actions/"
												+ this.game + "/" + actionId + ":" + objectId + "/"
												+ user);
										MiniClient client = new MiniClient();

										client.setConnectorEndpoint(this.gameURL +
												"/gamification/visualization/actions/"
												+ this.game + "/" + actionId + ":" + objectId.replaceAll("\\s", "")
												+ "/"
												+ user);

										HashMap<String, String> headers = new HashMap<String, String>();
										System.out.println("user");
										try {
											client.setLogin(restarterBot.getLoginName(), restarterBot.getPassphrase());
											ClientResponse result = client.sendRequest("POST", "", "");
											System.out.println(result.toString());
											System.out.println(result.getHttpCode());
											System.out.println(result.getRawResponse());
											System.out.println(result.getResponse());
											JSONObject rJSON = (JSONObject) p.parse(result.getResponse());
											if (rJSON.containsKey("notification")) {
												this.sendNotification(user, (JSONArray) rJSON.get("notification"));
											}

											// JSONObject asnswer = (JSONObject) parser.parse(result.getResponse());
											// System.out.println(answer);
										} catch (Exception e) {
											e.printStackTrace();
											System.out.println("pepe");
										}
										// Context.get().invokeInternally(
										// "i5.las2peer.services.gamificationVisualizationService.GamificationVisualizationService",
										// "triggerAction", this.game, verbId.split("verb/")[1], user);
										System.out.println("done triggering action1");
									} catch (Exception e3) {
										e3.printStackTrace();
										System.out.println("done triggering action");
									}

									occ++;
								}

							}
						}

						timeStamp = ((JSONObject) statements.get(0)).get("timestamp").toString();
						System.out.println("found " + occ + "statemetns");

					} else {
						System.out.println("no statements");
					}
					if (userStreaks.containsKey(user) && userStreaks.get(user) != null) {
						System.out.print(userStreaks.get(user));
						for (String streakId : userStreaks.get(user).keySet()) {

							JSONObject streak = (JSONObject) ((JSONObject) userStreaks.get(user)).get(streakId);
							LocalDateTime now = LocalDateTime.now();
							System.out.println("difference in hourse is " + ChronoUnit.MINUTES.between(now,
									LocalDateTime.parse(streak.get("dueDate").toString())) + " "
									+ ChronoUnit.MINUTES.between(LocalDateTime.parse(streak.get("dueDate").toString()),
											now));
							if (ChronoUnit.MINUTES.between(now,
									LocalDateTime.parse(streak.get("dueDate").toString())) < streakReminder * 60) {
								String action = ((JSONObject) ((JSONArray) streak.get("openActions")).get(0))
										.get("actionId").toString();
								String message = streakMessage.replace("[streakAction]", action)
										.replace("[streakCount]", streak.get("currentStreakLevel").toString());
								JSONArray notification = new JSONArray();
								JSONObject jsonObject = new JSONObject();
								jsonObject.put("message", message);
								notification.add(jsonObject);
								sendNotification(user, notification);
								userStreaks.get(user).remove(streakId);
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

	}

	public void sendNotification(String user, JSONArray notification) throws AgentLockedException {
		MiniClient client = new MiniClient();
		// http://127.0.0.1:8090/SBFManager/bots/Botty/webhook
		client.setConnectorEndpoint(this.sbmURL +
				"/SBFManager/bots/"
				+ this.botName + "/webhook");
		System.out.println("http://host.docker.internal:8090/SBFManager/bots/"
				+ this.botName + "/webhook");
		HashMap<String, String> headers = new HashMap<String, String>();
		System.out.println("user");
		String message = "";
		JSONObject information = new JSONObject();
		for (Object o : notification) {
			JSONObject json = (JSONObject) o;
			if (json.containsKey("type") && json.get("type").toString().equals("STREAK")) {
				MiniClient client2 = new MiniClient();
				System.out.println(json);
				client2.setConnectorEndpoint(this.gameURL +
						"/gamification/visualization/streaks/"
						+ this.game + "/" + user + "/progress/"
						+ json.get("typeId").toString());
				client2.setLogin(restarterBot.getLoginName(), restarterBot.getPassphrase());
				ClientResponse result1 = client2.sendRequest("GET", "", "");

				JSONParser parser = new JSONParser(JSONParser.MODE_PERMISSIVE);

				JSONObject jsonResult = new JSONObject();
				System.out.println(result1.getResponse());
				System.out.println(result1.getHttpCode());
				try {
					System.out.println("parsing jsonresult now");
					jsonResult = (JSONObject) parser.parse(result1.getResponse());
					System.out.println("parsing worked");
					System.out.println(jsonResult);
					if (userStreaks.containsKey(user)) {
						userStreaks.put(user,
								(JSONObject) userStreaks.get(user).put(json.get("typeId").toString(), jsonResult));
					} else {
						JSONObject allStreaks = new JSONObject();
						allStreaks.put(json.get("typeId").toString(), jsonResult);
						userStreaks.put(user, allStreaks);
					}

				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				continue;
			}
			if (json.containsKey("type") && json.get("type").toString().equals("ACHIEVEMENT")) {
				String id = json.get("typeId").toString();
				JSONObject ach = this.achievementsMap.get(id);
				message += "*ACHIEVEMENT UNLOCKED: "+ach.get("name").toString()+"* \n";	
			}
			if (!json.get("message").toString().equals("")) {
				message += "*" + json.get("message").toString() + "* \n";
			}
			if (json.containsKey("type") && json.get("type").toString().equals("ACHIEVEMENT")) {
				String id = json.get("typeId").toString();
				JSONObject ach = this.achievementsMap.get(id);
				String pointReward = ach.get("pointValue").toString();
				message += "*REWARDS:* \n";
				message += "*- "+pointReward +" Points* \n";
				
				if(ach.get("badgeId") != null){
					String	badge = ach.get("badgeId").toString();
					message += "*- "+badge +" BADGE* \n";
				}
				
			}
		}
		information.put("message", message);
		information.put("channel", this.usersChannel.get(user));
		information.put("event", "chat_message");
		try {
			ClientResponse result = client.sendRequest("POST", "",
					information.toJSONString(), MediaType.TEXT_PLAIN, "", headers);
			System.out.println(result.toString());
			System.out.println(result.getHttpCode());
			System.out.println(result.getRawResponse());
			System.out.println(result.getResponse());
			// JSONObject answer = (JSONObject) parser.parse(result.getResponse());
			// System.out.println(answer);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("pepe");
		}
	}

	public void addMember(String user) {
		try {
			System.out.println("attempting to add player");
			try {
				Context.get().invokeInternally("i5.las2peer.services.gamificationGameService.GamificationGameService",
						"memberLoginValidation", user);
			} catch (Exception e1) {
				e1.printStackTrace();
			}

			System.out.println("worked?");
			Context.get().invokeInternally("i5.las2peer.services.gamificationGameService.GamificationGameService",
					"addMemberToGame", this.game, user);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// seaerch through entire json for attribute value
	public String recursive(JSONObject j, String key) {
		if (j.containsKey(key)) {
			return j.get(key).toString();
		} else {
			for (String s : j.keySet()) {
				try {
					String r = recursive((JSONObject) j.get(s), key);
					if (!r.equals("")) {
						return r;
					}
				} catch (Exception e) {
				}
			}
		}
		return "";
	}

	public BotAgent getBotAgent() {
		return this.restarterBot;
	}

	public String getGame() {
		return game;
	}

	public HashMap<String, Boolean> getUsers() {
		return users;
	}

	public Boolean isRegistered(String user) {
		if (this.users.keySet().contains(user)) {
			return true;
		}
		return false;
	}

}
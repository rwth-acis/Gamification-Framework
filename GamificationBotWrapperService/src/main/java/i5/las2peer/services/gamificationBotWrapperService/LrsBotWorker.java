package i5.las2peer.services.gamificationBotWrapperService;

import java.time.LocalDateTime;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

import java.util.HashMap;
import java.util.HashSet;

import javax.ws.rs.core.MediaType;

import ch.qos.logback.core.net.server.Client;
import i5.las2peer.api.Context;
import i5.las2peer.api.security.AgentAccessDeniedException;
import i5.las2peer.api.security.AgentAlreadyExistsException;
import i5.las2peer.api.security.AgentLockedException;
import i5.las2peer.api.security.AgentOperationFailedException;
import i5.las2peer.connectors.webConnector.client.ClientResponse;
import i5.las2peer.connectors.webConnector.client.MiniClient;
import i5.las2peer.security.BotAgent;
import i5.las2peer.tools.CryptoException;

public class LrsBotWorker implements Runnable {
	private String timeStamp = "0";
	private String lrsToken = "";
	private String botName = "";
	private String game = "";

	private HashMap<String, Boolean> users = new HashMap<String, Boolean>();
	private HashMap<String, String> usersChannel = new HashMap<String, String>();

	private BotAgent restarterBot = null;
	private HashMap<String,JSONArray> actionVerbs = new HashMap<String,JSONArray>();



	public LrsBotWorker(String game, String botName, BotAgent restarterBot, String lrsToken,
	HashMap<String,JSONArray> actionVerbs) {
		this.game = game;
		this.botName = botName;
		this.restarterBot = restarterBot;
		this.lrsToken = lrsToken;
		this.actionVerbs = actionVerbs;
	}

	public void addUsers(String email, String channel) {
		this.users.put(email, false);
		this.usersChannel.put(email, channel);
	}

	public HashMap<String, JSONArray> getActionVerbs() {
		return actionVerbs;
	}

	// here, the whole process of communicating with the GF should take place
	// as to not repeat statements, it would be interesting to also store the
	// statement id
	// but this would not always work, as a student should not always get a point
	// for the same action
	// like prior knowledge activsation exercised
	@Override
	public void run() {
		System.out.println("10" + this.botName);
		while (!Thread.currentThread().isInterrupted()) {
			// will need to do timestamp per user and not per instance of bot
			for (String user : users.keySet()) {
				System.out.println("Fetching for: " + user);
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
							String verbId = ((JSONObject) s.get("verb")).get("id").toString();
							String objectId = ((JSONObject) ((JSONObject) ((JSONObject) s.get("object"))
									.get("definition")).get("name")).get("en-US").toString();
							if (this.actionVerbs.keySet().contains(verbId.split("verb/")[1])) {
								for(Object o : this.actionVerbs.get(verbId.split("verb/")[1])){
									
									JSONObject jsonO = (JSONObject) o;
									System.out.println("checking stuff for action id: " + jsonO.get("id").toString());
									if(jsonO.containsKey("lrsAttribute") && !jsonO.get("lrsAttribute").toString().equals("")){
										System.out.println("to match attribute is:+ "+ jsonO.get("lrsAttribute").toString()  +" with value "+jsonO.get("lrsAttributeValue").toString());
										if(!jsonO.get("lrsAttributeValue").toString().equals(recursive(s, jsonO.get("lrsAttribute").toString()))){
											System.out.println("attributes do not match");
											continue;
										}
									}
									try {
										String actionId = jsonO.get("id").toString();
										System.out.println(
												"http://host.docker.internal:8080/gamification/visualization/actions/"
														+ this.game + "/" + actionId + ":" + objectId + "/"
														+ user);
										MiniClient client = new MiniClient();
	
										client.setConnectorEndpoint(
												"http://host.docker.internal:8080/gamification/visualization/actions/"
														+ this.game + "/" + actionId + ":" + objectId + "/"
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
											if(rJSON.containsKey("notification")){
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

	public void sendNotification(String user, JSONArray notification) {
		MiniClient client = new MiniClient();
		http://127.0.0.1:8090/SBFManager/bots/Botty/webhook
		client.setConnectorEndpoint(
				"http://host.docker.internal:8090/SBFManager/bots/"
						+ this.botName + "/webhook");
		System.out.println("http://host.docker.internal:8090/SBFManager/bots/"
		+ this.botName + "/webhook");
		HashMap<String, String> headers = new HashMap<String, String>();
		System.out.println("user");
		String message = "";
		JSONObject information = new JSONObject();
		for(Object o : notification){
			JSONObject json = (JSONObject) o;
			message += "*"+json.get("message").toString()+"* \n";

		}
		information.put("message", message);
		information.put("channel",this.usersChannel.get(user));
		information.put("event","chat_message");
		try {
			ClientResponse result = client.sendRequest("POST", "",
			information.toJSONString(), MediaType.TEXT_PLAIN , "", headers);
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
	public String recursive(JSONObject j, String key){
		if(j.containsKey(key)){
			return j.get(key).toString();
		} else {
			for(String s : j.keySet()){
				try{
					String r = recursive((JSONObject) j.get(s), key);
					if(!r.equals("")){
						return r;
					} 
				} catch(Exception e){				
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
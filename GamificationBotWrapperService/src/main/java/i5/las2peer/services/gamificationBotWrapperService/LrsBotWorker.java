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
import  net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

public class LrsBotWorker implements Runnable{
	private String timeStamp = "0";
	private String lrsToken = "";
	
	public LrsBotWorker(){
	}


	// here, the whole process of communicating with the GF should take place
	// as to not repeat statements, it would be interesting to also store the statement id
	// but this would not always work, as a student should not always get a point for the same action
	// like prior knowledge activsation exercised
	@Override
	public void run() {
		System.out.println("10");
		while (!Thread.currentThread().isInterrupted() ) {
			try{
			JSONParser p = new JSONParser(JSONParser.MODE_PERMISSIVE);	
			JSONObject acc = (JSONObject) p.parse(new String("{'account': { 'name': '" + "f0cab264281fb07f6368f44064162fee95f4d511505dd09264131c6a19b6bd349d0cd8eb751d6363325d5432802690b5"
				+ "', 'homePage': 'https://chat.tech4comp.dbis.rwth-aachen.de'}}"));
		URL url = new URL("https://lrs.tech4comp.dbis.rwth-aachen.de" + "/data/xAPI/statements?agent=" + acc.toString() + "&since=" + timeStamp );
		
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
		conn.setRequestProperty("X-Experience-API-Version", "1.0.3");
		conn.setRequestProperty("Authorization", "Basic " + "IxNGI2NThkNDMyOWI4MmQ2ODEyZmJmNmM3MjVhY2JkZjdjMDIzYToxMmZiMGIwNTY4YzgwNmYyZmQ3NmNjODc2MGVhODQxMTVlOWNkNzkw");
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
		System.out.println("10");
		JSONObject jsonBody = (JSONObject) p.parse(response.toString());

		JSONArray statements = (JSONArray) jsonBody.get("statements");
		System.out.println("statements coming" + statements.size());
		for(Object statement : statements){
			if(statement.toString().toLowerCase().contains("gamification")){
				System.out.println("gamified statement");
			}
		}
		if(statements.size() > 0){
			System.out.println(statements.get(0));
			System.out.println(((JSONObject) statements.get(0)).get("timestamp"));
			timeStamp = ((JSONObject) statements.get(0)).get("timestamp").toString();

		} else {
			System.out.println("no statements");
		}
		} catch (Exception e){
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

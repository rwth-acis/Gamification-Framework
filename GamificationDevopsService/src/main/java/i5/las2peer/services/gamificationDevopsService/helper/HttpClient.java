package i5.las2peer.services.gamificationDevopsService.helper;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import i5.las2peer.security.AnonymousAgentImpl;

public final class HttpClient {

	private static HttpClient INSTANCE;
	
	public static HttpClient create(String agentId) {
		INSTANCE = new HttpClient(agentId);
		return getInstance();
	}
	
	public static HttpClient getInstance() {
		return INSTANCE;
	}
	
	
	private final String agentId;
	private HttpClient(String agentId) {
		this.agentId = agentId;
	}

	
	
	public Pair<Integer,String> sendPOSTResquest(String targetUrl,String urlParameters) throws IOException {
		HttpURLConnection  connection = buildURLConnection("POST",targetUrl);
		
		connection.setDoOutput(true);

		//Send request
		DataOutputStream wr = new DataOutputStream (
				connection.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.close();
		
		return readHttpResponse(connection);
	}

	public Pair<Integer,String> sendGETRequest(String targetUrl) throws IOException{
		HttpURLConnection connection = buildURLConnection("GET",targetUrl);
		
		return readHttpResponse(connection);
	}
	
	private HttpURLConnection buildURLConnection(String method, String targetUrl) throws IOException{
		URL url = new URL(targetUrl);
		HttpURLConnection  connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod(method);
		connection.setRequestProperty("Content-Type", 
				"text/plain");
		connection.setRequestProperty("Accept", 
				"*/*");

		//set agent id
		connection.setRequestProperty("Authorization", "Basic " + agentId);
		
		connection.setUseCaches(false);
		
		return connection;
	}

	
	private static Pair<Integer,String> readHttpResponse(HttpURLConnection connection) throws IOException {
		//Get Response  
	    InputStream is = connection.getInputStream();
	    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
	    StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
	    String line;
	    while ((line = rd.readLine()) != null) {
	      response.append(line);
	      response.append('\r');
	    }
	    rd.close();
		return new Pair<Integer,String>(connection.getResponseCode(),response.toString());
	}

}

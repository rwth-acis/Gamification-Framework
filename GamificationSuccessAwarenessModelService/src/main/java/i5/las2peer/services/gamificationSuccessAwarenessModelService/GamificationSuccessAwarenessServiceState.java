package i5.las2peer.services.gamificationSuccessAwarenessModelService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import i5.las2peer.api.p2p.ServiceNameVersion;
import i5.las2peer.api.security.AgentException;
import i5.las2peer.connectors.ConnectorException;
import i5.las2peer.connectors.webConnector.WebConnector;
import i5.las2peer.connectors.webConnector.client.ClientResponse;
import i5.las2peer.connectors.webConnector.client.MiniClient;
import i5.las2peer.p2p.LocalNode;
import i5.las2peer.p2p.LocalNodeManager;
import i5.las2peer.p2p.NodeException;
import i5.las2peer.security.ServiceAgentImpl;
import i5.las2peer.security.UserAgentImpl;
import i5.las2peer.serialization.MalformedXMLException;
import i5.las2peer.services.gamificationSuccessAwarenessModelService.helper.GamifiedMeasure;
import i5.las2peer.services.mobsos.dataProcessing.MobSOSDataProcessingService;
import i5.las2peer.testing.MockAgentFactory;
import i5.las2peer.tools.CryptoException;

public class GamificationSuccessAwarenessServiceState {

	private static final GamificationSuccessAwarenessServiceState INSTANCE = new GamificationSuccessAwarenessServiceState();

	public synchronized static GamificationSuccessAwarenessServiceState getInstance() {
		return INSTANCE;
	}

	private Map<String,Set<GamifiedMeasure>> gamifiedMeasures;
	private boolean canListen;
	private LocalNode node;
	private MiniClient client;

	private GamificationSuccessAwarenessServiceState(){
		gamifiedMeasures = new HashMap<>();
		canListen = true;
		
		try {
			node = new LocalNodeManager().newNode();
			node.launch();
			
			
			UserAgentImpl user1 = MockAgentFactory.getEve();
			user1.unlock("evespass");
			node.storeAgent(user1);
			ServiceNameVersion testServiceClass = new ServiceNameVersion(
					GamificationSuccessAwarenessModelService.class.getCanonicalName(), "0.1");
			ServiceAgentImpl testService = ServiceAgentImpl.createServiceAgent(testServiceClass, "a pass");
			testService.unlock("a pass");
			node.registerReceiver(testService);
			node.startService(new ServiceNameVersion("i5.las2peer.services.mobsos.dataProcessing.MobSOSDataProcessingService", "1.0"), "a pass");
			node.startService(new ServiceNameVersion("i5.las2peer.services.gamificationActionService.GamificationActionService","1.0"), "a pass");
			node.startService(new ServiceNameVersion("i5.las2peer.services.gamificationGameService.GamificationGameService","1.0"), "a pass");

			final int connectorPort = 9999;
			// start connector
			ByteArrayOutputStream logStream = new ByteArrayOutputStream();
			WebConnector connector = new WebConnector(true, connectorPort, false, 1000);
			connector.setLogStream(new PrintStream(logStream));

			connector.start(node);

			Thread.sleep(1000); // wait a second for the connector to become ready

			client = new MiniClient();
			client.setConnectorEndpoint(connector.getHttpEndpoint());
			client.setLogin(user1.getIdentifier(), "evespass");
		} catch (CryptoException | AgentException | ConnectorException | InterruptedException | NodeException | MalformedXMLException | IOException e) {
			e.printStackTrace();
		}
	}

	public Set<String> getGameIds(){
		return new HashSet<>(gamifiedMeasures.keySet());
	}

	public void addGamifiedMeasure(String gameId, GamifiedMeasure measure) {
		Set<GamifiedMeasure> measures = gamifiedMeasures.get(gameId);
		if(measures == null) {
			measures = new HashSet<>();
			gamifiedMeasures.put(gameId, measures);
		}
		measures.add(measure);
	}

	public Set<GamifiedMeasure> getGamifiedMeasures(String gameId){
		return new HashSet<>(gamifiedMeasures.get(gameId));
	}
	
	public GamifiedMeasure getGamifiedMeasure(String gameId,String measureName){
		GamifiedMeasure foundMeasure = null;
		for(GamifiedMeasure measure: gamifiedMeasures.get(gameId)) {
			if(measure.getMeasure().getName().equals(measureName)) {
				foundMeasure = measure;
				break;
			}
		}
		return foundMeasure;
	}
	
	public void removeMeasureFromGame(String gameId, String measureName) {
		GamifiedMeasure foundMeasure = getGamifiedMeasure(gameId,measureName);
		if(foundMeasure != null) {
			gamifiedMeasures.get(gameId).remove(foundMeasure);
		}
	}
	
	public void removeGame(String gameId) {
		gamifiedMeasures.remove(gameId);
	}

	public boolean canListen() {
		return canListen;
	}

	public void stopListening() {
		canListen = false;
	}
	
	public ClientResponse sendRequest(String verb, String path, String content, String  contentType, Map<String,String> header) {
		return client.sendRequest(verb, path,content, contentType,"*/*",header);
	}
	
	public ClientResponse sendRequest(String verb, String path) {
		return client.sendRequest(verb, path, "");
	}
	
	public LocalNode getNode() {
		return node;
	}

}

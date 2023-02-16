package i5.las2peer.services.gamificationDevopsService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import i5.las2peer.api.p2p.ServiceNameVersion;
import i5.las2peer.api.security.AgentException;
import i5.las2peer.connectors.ConnectorException;
import i5.las2peer.connectors.webConnector.WebConnector;
import i5.las2peer.connectors.webConnector.client.ClientResponse;
import i5.las2peer.connectors.webConnector.client.MiniClient;
import i5.las2peer.p2p.LocalNode;
import i5.las2peer.p2p.LocalNodeManager;
import i5.las2peer.p2p.NodeException;
import i5.las2peer.security.UserAgentImpl;
import i5.las2peer.serialization.MalformedXMLException;
import i5.las2peer.services.gamificationActionService.GamificationActionService;
import i5.las2peer.testing.MockAgentFactory;
import i5.las2peer.tools.CryptoException;

public class GamificationDevopsServiceState {

	private static GamificationDevopsServiceState INSTANCE;

	
	public synchronized static GamificationDevopsServiceState create(String devopsServices) {
		String[] servicesInfo = devopsServices.split(",");
		List<String[]> services = new LinkedList<>();
		for(String info: servicesInfo) {
			String[] aux = info.split(":");
			services.add(aux);
		}
		
		INSTANCE = new GamificationDevopsServiceState(services);
		return INSTANCE;
	}
	
	public synchronized static GamificationDevopsServiceState getInstance() {
		return INSTANCE;
	}

	private LocalNode node;
	private MiniClient client;
	private WebConnector connector;
	

	private GamificationDevopsServiceState(List<String[]> services){
		
		try {
			node = new LocalNodeManager().newNode();
			node.launch();
			
			
			UserAgentImpl user1 = MockAgentFactory.getEve();
			user1.unlock("evespass");
			node.storeAgent(user1);
			
			//0 - service name, 1 - service version, 2 - service pass
			for(String[] serviceInfo: services) {
				node.startService(new ServiceNameVersion(serviceInfo[0],serviceInfo[1]), serviceInfo[2]);				
			}
			node.startService(new ServiceNameVersion(GamificationActionService.class.getName(),"0.1"), "a pass");

			final int connectorPort = 9999;
			// start connector
			ByteArrayOutputStream logStream = new ByteArrayOutputStream();
			connector = new WebConnector(true, connectorPort, false, 1000);
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

	public ClientResponse sendRequest(String verb, String path, String content, String  contentType, Map<String,String> header) {
		return client.sendRequest(verb, path,content, contentType,"*/*",header);
	}
	
	public ClientResponse sendRequest(String verb, String path) {
		return client.sendRequest(verb, path, "");
	}
	
	public void stop() throws ConnectorException {
		connector.stop();
	}
}

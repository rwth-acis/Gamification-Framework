package i5.las2peer.services.mobsos.dataProcessing;

import org.apache.commons.codec.digest.DigestUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import i5.las2peer.api.logging.MonitoringEvent;
import i5.las2peer.logging.monitoring.MonitoringMessage;

/**
 * 
 * Data class that takes a {@link i5.las2peer.logging.monitoring.MonitoringMessage} and encrypts the source and
 * destination agents as an MD5 hash-string for privacy reasons. Service Messages will have an encrypted remarks field
 * as well to prevent service developers from gathering user specific information via their custom service messages.
 * This is how data will be persisted in the database later on.
 * 
 * @author Peter de Lange
 *
 */
public class MonitoringMessageWithEncryptedAgents {

	private Long timestamp;
	private MonitoringEvent event;
	private String sourceNode;
	private String sourceAgentId = null;
	private String destinationNode;
	private String destinationAgentId = null;
	private String remarks;
	private String jsonRemarks;

	/**
	 * 
	 * Constructor of a MonitoringMessageWithEncryptedAgents.
	 * 
	 * @param monitoringMessage a {@link i5.las2peer.logging.monitoring.MonitoringMessage}
	 * @param hashRemarks Whether you want to hash the remarks or not
	 * 
	 */
	public MonitoringMessageWithEncryptedAgents(MonitoringMessage monitoringMessage, boolean hashRemarks) {
		this.timestamp = monitoringMessage.getTimestamp();
		this.event = monitoringMessage.getEvent();
		this.sourceNode = monitoringMessage.getSourceNode();
		if (monitoringMessage.getSourceAgentId() != null)
			this.sourceAgentId = DigestUtils.md5Hex((monitoringMessage.getSourceAgentId().toString()));
		this.destinationNode = monitoringMessage.getDestinationNode();
		if (monitoringMessage.getDestinationAgentId() != null)
			this.destinationAgentId = DigestUtils.md5Hex((monitoringMessage.getDestinationAgentId().toString()));
		// Custom service messages
		if (Math.abs(this.getEvent().getCode()) >= 7500 && (Math.abs(this.getEvent().getCode()) < 7600)
				&& hashRemarks) {
			this.remarks = "{\"msg\":\"" + DigestUtils.md5Hex((monitoringMessage.getRemarks())) + "\"}";
		} else {
			if (monitoringMessage.getRemarks() == null) {
				this.remarks = "";
				this.jsonRemarks = "{}";
			} else {
				this.remarks = monitoringMessage.getRemarks();
				if (isJSONValid(monitoringMessage.getRemarks())) {
					this.jsonRemarks = monitoringMessage.getRemarks();
				} else {
					this.jsonRemarks = "{\"msg\":\"" + JSONObject.escape(monitoringMessage.getRemarks()) + "\"}";
				}
			}
		}
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public MonitoringEvent getEvent() {
		return event;
	}

	public String getSourceNode() {
		return sourceNode;
	}

	public String getSourceAgentId() {
		return sourceAgentId;
	}

	public String getDestinationNode() {
		return destinationNode;
	}

	public String getDestinationAgentId() {
		return destinationAgentId;
	}

	public String getRemarks() {
		return remarks;
	}

	public String getJsonRemarks() {
		return jsonRemarks;
	}

	private boolean isJSONValid(String jsonString) {
		try {
			JSONParser parser = new JSONParser();
			parser.parse(jsonString);
		} catch (ParseException ex) {
			return false;
		}
		return true;
	}

}

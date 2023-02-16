package i5.las2peer.services.mobsos.dataProcessing;

import java.io.Serializable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import i5.las2peer.api.Context;
import i5.las2peer.api.ManualDeployment;
import i5.las2peer.api.Service;
import i5.las2peer.api.execution.InternalServiceException;
import i5.las2peer.api.execution.ServiceAccessDeniedException;
import i5.las2peer.api.execution.ServiceInvocationFailedException;
import i5.las2peer.api.execution.ServiceMethodNotFoundException;
import i5.las2peer.api.execution.ServiceNotAuthorizedException;
import i5.las2peer.api.execution.ServiceNotAvailableException;
import i5.las2peer.api.execution.ServiceNotFoundException;
import i5.las2peer.api.logging.MonitoringEvent;
import i5.las2peer.api.security.Agent;
import i5.las2peer.api.security.AgentException;
import i5.las2peer.logging.L2pLogger;
import i5.las2peer.logging.bot.BotMessage;
import i5.las2peer.logging.monitoring.MonitoringMessage;
import i5.las2peer.security.BotAgent;
import i5.las2peer.security.MonitoringAgent;
import i5.las2peer.security.ServiceAgentImpl;
import i5.las2peer.services.mobsos.dataProcessing.database.DatabaseInsertStatement;
import i5.las2peer.services.mobsos.dataProcessing.database.DatabaseQuery;
import i5.las2peer.services.mobsos.dataProcessing.database.SQLDatabase;
import i5.las2peer.services.mobsos.dataProcessing.database.SQLDatabaseType;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

/**
 * This service is responsible for processing incoming monitoring data. It tests the data for correctness and stores
 * them in a relational database. The provision is done by the Monitoring Data Provision Service.
 */
@ManualDeployment
public class MobSOSDataProcessingService extends Service {
	private static final String AGENT_PASS = "ProcessingAgentPass"; // The pass phrase for the receivingAgent
	private MonitoringAgent receivingAgent; // This agent will be responsible for receiving all incoming message
	private Map<String, String> monitoredServices = new HashMap<String, String>(); // A list of services that are
	// monitored
	private HashSet<BotAgent> actingAgents;
	private Set<String> triggerFunctions = new HashSet<String>();
	private ArrayList<BotMessage> botMessages = new ArrayList<BotMessage>();
	private ArrayList<String> xAPIstatements = new ArrayList<String>();
	//private boolean sendStatementsToBots = true; // True if xAPI statements should also be sent to the social bot manager service

	/**
	 * Configuration parameters, values will be set by the configuration file.
	 */
	private String databaseName;
	private int databaseTypeInt; // See SQLDatabaseType for more information
	private SQLDatabaseType databaseType;
	private String databaseHost;
	private int databasePort;
	private String databaseUser;
	private String databasePassword;
	private boolean hashRemarks;
	private boolean sendToLRS; // Added for LRS
	private Connection con;
	private SQLDatabase database; // The database instance to write to.

	private int messageCount;

	/**
	 * Constructor of the Service. Loads the database values from a property file and tries to connect to the database.
	 */
	public MobSOSDataProcessingService() {
		setFieldValues(); // This sets the values of the configuration file
		this.databaseType = SQLDatabaseType.getSQLDatabaseType(databaseTypeInt);
		this.database = new SQLDatabase(this.databaseType, this.databaseUser, this.databasePassword, this.databaseName,
				this.databaseHost, this.databasePort);
		try {
			con = database.getDataSource().getConnection();
		} catch (SQLException e) {
			System.out.println("Failed to Connect: " + e.getMessage());
		}
		if (actingAgents == null) {
			actingAgents = new HashSet<BotAgent>();
		}
		L2pLogger.setGlobalConsoleLevel(Level.WARNING);
	}

	/**
	 * Will be called by the receiving {@link i5.las2peer.security.MonitoringAgent} of this service, if it receives a
	 * message from a monitored node.
	 *
	 * @param messages an array of {@link i5.las2peer.logging.monitoring.MonitoringMessage}s
	 * @return true, if message persistence did work
	 */
	public boolean getMessages(MonitoringMessage[] messages) {
		Agent requestingAgent = Context.getCurrent().getMainAgent();
		if (receivingAgent == null) {
			System.out.println("Monitoring: Agent not registered yet, this invocation must be false!");
			return false;
		}
		if (!requestingAgent.getIdentifier().equals(receivingAgent.getIdentifier())) {
			System.out.println("Monitoring: I only take messages from my own agent!");
			return false;
		}
		messageCount = 0;
		for (int i = 0; i < messages.length; ++i) {
			if (messages[i] != null)
				messageCount++;
		}
		System.out.println("Monitoring: Got " + messageCount + " monitoring messages!");
		return processMessages(messages);
	}

	/**
	 * Checks the messages content and calls {@link #persistMessage(MonitoringMessage, String)} with the corresponding
	 * values.
	 *
	 * @param messages an array of {@link i5.las2peer.logging.monitoring.MonitoringMessage}
	 * @return true, if message persistence did work
	 */
	private boolean processMessages(MonitoringMessage[] messages) {
		boolean returnStatement = true;
		int counter = 0;
		botMessages = new ArrayList<BotMessage>();
		HashMap<String, JSONObject> webhookCalls = new HashMap<>();
		for (MonitoringMessage message : messages) {
			// Happens when a node has sent its last messages
			if (message == null) {
				counter++;
			} else if (message.getEvent() == MonitoringEvent.BOT_ADD_TO_MONITORING) {
				try {
					JSONObject jsonRemarks = new JSONObject();
					JSONParser jsonParser = new JSONParser(JSONParser.MODE_PERMISSIVE);

					jsonRemarks = (JSONObject) jsonParser.parse(message.getRemarks());
					JSONArray botIds = (JSONArray) jsonRemarks.get("botIds");
					ServiceAgentImpl sa = (ServiceAgentImpl) Context.get().getServiceAgent();
					BotAgent bAgent = null;
					for (int i = 0; i < botIds.size(); i++) {
						try {
							bAgent = (BotAgent) sa.getRunningAtNode().getAgent((String) botIds.get(i));
						} catch (AgentException e) {
							// TODO Maybe monitor that the Agent could not be found etc.
							e.printStackTrace();
						}
						actingAgents.add(bAgent);
						System.out.println("\u001B[32mBot " + bAgent.getLoginName() + " added.\u001B[0m");
					}
					JSONArray jra = (JSONArray) jsonRemarks.get("triggerFunctions");
					for (int i = 0; i < jra.size(); i++) {
						triggerFunctions.add(((String) jra.get(i)).toLowerCase());
					}
						
					returnStatement = persistMessage(message, "MESSAGE");
					if (!returnStatement)
						counter++;
				} catch (ParseException e) {
					e.printStackTrace();
				}

				// Add node to database (running means we got an id representation)
			} else if ((message.getEvent() == MonitoringEvent.NODE_STATUS_CHANGE
					&& message.getRemarks().equals("RUNNING"))) {
				returnStatement = persistMessage(message, "NODE");
				if (!returnStatement)
					counter++;

				returnStatement = persistMessage(message, "MESSAGE");
				if (!returnStatement)
					counter++;
			}

			// Add unregister date to all registered agents at this node
			else if (message.getEvent() == MonitoringEvent.NODE_STATUS_CHANGE
					&& message.getRemarks().equals("CLOSING")) {
				returnStatement = persistMessage(message, "REGISTERED_AT");
				if (!returnStatement)
					counter++;
				returnStatement = persistMessage(message, "MESSAGE");
				if (!returnStatement)
					counter++;
			}

			// Add service to monitored service list and add service, service agent,
			// 'registered at' and message to
			// database
			else if (message.getEvent() == MonitoringEvent.SERVICE_ADD_TO_MONITORING) {
				monitoredServices.put(message.getSourceAgentId(), message.getRemarks());
				returnStatement = persistMessage(message, "AGENT");
				if (!returnStatement)
					counter++;

				returnStatement = persistMessage(message, "SERVICE");
				if (!returnStatement)
					counter++;

				returnStatement = persistMessage(message, "REGISTERED_AT");
				if (!returnStatement)
					counter++;

				returnStatement = persistMessage(message, "MESSAGE");
				if (!returnStatement)
					counter++;
			}
			// Add agent to database
			else if (message.getEvent() == MonitoringEvent.AGENT_REGISTERED
					&& !message.getRemarks().equals("ServiceAgent")
					&& !message.getRemarks().equals("ServiceInfoAgent")) {
				returnStatement = persistMessage(message, "AGENT");
				if (!returnStatement)
					counter++;

				returnStatement = persistMessage(message, "REGISTERED_AT");
				if (!returnStatement)
					counter++;

				returnStatement = persistMessage(message, "MESSAGE");
				if (!returnStatement)
					counter++;
			}

			// Connector requests are only logged for monitored services or if they
			// do not give any information on the service itself
			else if (message.getEvent() == MonitoringEvent.HTTP_CONNECTOR_REQUEST) {
				if (message.getSourceAgentId() == null || monitoredServices.containsKey(message.getSourceAgentId())) {
					returnStatement = persistMessage(message, "MESSAGE");
					if (!returnStatement)
						counter++;
				}
			}

			// If enabled for monitoring, add service message to database
			else if (Math.abs(message.getEvent().getCode()) >= 7000
					&& (Math.abs(message.getEvent().getCode()) < 8000)) {
				if (message.getEvent() == MonitoringEvent.SERVICE_SHUTDOWN) {
					returnStatement = persistMessage(message, "REGISTERED_AT");
					if (!returnStatement)
						counter++;
				}
				returnStatement = persistMessage(message, "MESSAGE");
				if (!returnStatement)
					counter++;

				if (message.getRemarks() != null) {
					String serviceClassName = monitoredServices.get(message.getSourceAgentId());
					System.out.println("\u001B[33mDebug --- Monitored: \u001B[0m");
					for (String svc : monitoredServices.values()) {
						System.out.println("\u001B[33m" + svc + "\u001B[0m");
					}
					/*
					if (sendToLRS && serviceClassName != null
							&& (serviceClassName.contains(
									"i5.las2peer.services.moodleDataProxyService.MoodleDataProxyService@1.3.0")
									|| serviceClassName.contains(
											"i5.las2peer.services.onyxDataProxyService.OnyxDataProxyService@1.0.0")
									|| serviceClassName.contains(
											"i5.las2peer.services.AssessmentHandler.AssessmentHandlerService@1.0.0")
									|| serviceClassName.contains("i5.las2peer.services.tmitocar"))) {
          */
						String statement = message.getRemarks();
						if (statement.contains("actor") && statement.contains("verb") && statement.contains("object")) {
							
							xAPIstatements.add(statement);
							System.out.println("Statement added");
						}
					//}
					JSONParser p = new JSONParser(JSONParser.MODE_PERMISSIVE);
					try {
						Object obj = p.parse(message.getRemarks());
						if (obj instanceof JSONObject) {
							JSONObject jsonObj = (JSONObject) obj;

							// check if the monitoring message should trigger a webhook call
							if(jsonObj.containsKey("webhook")) {
								JSONObject webhook = (JSONObject) jsonObj.get("webhook");
								String url = webhook.getAsString("url");
								JSONObject payload = (JSONObject) webhook.get("payload");
								webhookCalls.put(url, payload);
							}

							String function = jsonObj.getAsString("functionName");
							if (function != null && hasBot() && triggerFunctions.contains(function.toLowerCase())) {
								BotMessage m = new BotMessage(message.getTimestamp(), message.getEvent(),
										message.getSourceNode(), message.getSourceAgentId(),
										message.getDestinationNode(), message.getDestinationAgentId(),
										message.getRemarks());
								botMessages.add(m);
							}
						}
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
			} else if (message.getEvent() == MonitoringEvent.AGENT_REMOVED) {
				returnStatement = persistMessage(message, "REGISTERED_AT");
				if (!returnStatement)
					counter++;

				returnStatement = persistMessage(message, "MESSAGE");
				if (!returnStatement)
					counter++;
			}
			// Just log the message
			else {
				returnStatement = persistMessage(message, "MESSAGE");
				if (!returnStatement)
					counter++;
			}
		}

		if (!xAPIstatements.isEmpty()) {
			try {
				Context.get().invoke("i5.las2peer.services.learningLockerService.LearningLockerService",
						"sendXAPIstatement", (Serializable) xAPIstatements);
				
//				if (monitoredServices.values().stream().anyMatch(s ->
//						s.contains("i5.las2peer.services.socialBotManagerService.SocialBotManagerService"))) {
//					Context.getCurrent().invoke("i5.las2peer.services.socialBotManagerService.SocialBotManagerService",
//							"getXapiStatements", (Serializable) xAPIstatements);
//				}
				if (hasBot()) {
					Context.getCurrent().invoke("i5.las2peer.services.socialBotManagerService.SocialBotManagerService",
							"getXapiStatements", (Serializable) xAPIstatements);
				}
				
				// TODO Handle Exceptions!
			} catch (ServiceNotFoundException e) {
				e.printStackTrace();
			} catch (ServiceNotAvailableException e) {
				e.printStackTrace();
			} catch (InternalServiceException e) {
				e.printStackTrace();
			} catch (ServiceMethodNotFoundException e) {
				e.printStackTrace();
			} catch (ServiceInvocationFailedException e) {
				e.printStackTrace();
			} catch (ServiceAccessDeniedException e) {
				e.printStackTrace();
			} catch (ServiceNotAuthorizedException e) {
				e.printStackTrace();
			}
			xAPIstatements.clear();
		}

		if (!botMessages.isEmpty()) {
			try {
				Context.getCurrent().invoke("i5.las2peer.services.socialBotManagerService.SocialBotManagerService",
						"getMessages", (Serializable) botMessages);
				// actingAgent.receiveMessage(m,
				// actingAgent.getRunningAtNode().getAgentContext(actingAgent));
				// TODO Handle Exceptions
			} catch (ServiceNotFoundException e) {
				e.printStackTrace();
			} catch (ServiceNotAvailableException e) {
				e.printStackTrace();
			} catch (InternalServiceException e) {
				e.printStackTrace();
			} catch (ServiceMethodNotFoundException e) {
				e.printStackTrace();
			} catch (ServiceInvocationFailedException e) {
				e.printStackTrace();
			} catch (ServiceAccessDeniedException e) {
				e.printStackTrace();
			} catch (ServiceNotAuthorizedException e) {
				e.printStackTrace();
			}
		}

		// perform webhook calls
		if(!webhookCalls.isEmpty()) {
			HttpClient client = HttpClient.newBuilder()
			.followRedirects(HttpClient.Redirect.ALWAYS).version(HttpClient.Version.HTTP_1_1).build();
			for (Map.Entry<String, JSONObject> entry : webhookCalls.entrySet()) {
				String url = entry.getKey();
				JSONObject payload = entry.getValue();

				HttpRequest request = HttpRequest.newBuilder()
						.uri(URI.create(url))
						.POST(HttpRequest.BodyPublishers.ofString(payload.toJSONString()))
						.build();

				try {
					client.send(request,
							HttpResponse.BodyHandlers.ofString());
				} catch (Exception e) {
                    System.out.println("Unable to call webhook");
				}
			}
		}

		System.out.println((messages.length - counter) + "/" + messageCount + " messages were handled.");
		return returnStatement;
	}

	private boolean persistMessage(MonitoringMessage message, String table) {
		return this.persistMessage(message, table, true);
	}

	/**
	 * This method constructs SQL-statements by calling the {@link DatabaseInsertStatement} helper class. It then calls
	 * the database for persistence.
	 *
	 * @param message a {@link i5.las2peer.logging.monitoring.MonitoringMessage}
	 * @param table the table to insert to. This parameter does determine what action will be performed on the database
	 *            (insert an agent, a message, a node..).
	 * @param exceptionHandling Set to true to enable exception recovery after a failed database interaction.
	 * @return true, if message persistence did work
	 */
	private boolean persistMessage(MonitoringMessage message, String table, boolean exceptionHandling) {
		boolean returnStatement = false;
		if (con == null) {
			return false;
		}
		try {
			PreparedStatement insertStatement = DatabaseInsertStatement.returnInsertStatement(con, message,
					database.getJdbcInfo(), table, hashRemarks);
			int result = insertStatement.executeUpdate();
			if (result >= 0) {
				returnStatement = true;
			}
			insertStatement.close();
		} catch (Exception e) {
			if (exceptionHandling) {
				try {
					// attempt recovery
					if ("REGISTERED_AT".equals(table)) {
						// sometimes the node is not present at the node table
						PreparedStatement nodeStatement = DatabaseQuery.returnNodeQueryStatement(con,
								message.getSourceNode(), database.getJdbcInfo());
						ResultSet result = nodeStatement.executeQuery();
						if (!result.next()) {
							// node is actually not present in NODE table
							System.out.println("Monitoring: Source node " + message.getSourceNode() + " is unknown. "
									+ "Adding to node list and reattempting to process the message.");
							PreparedStatement insertStatement = DatabaseInsertStatement.returnInsertStatement(con,
									message, database.getJdbcInfo(), "NODE", hashRemarks);
							insertStatement.executeUpdate();
							return persistMessage(message, table, false);
						}
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			e.printStackTrace();
		}
		return returnStatement;
	}

	/**
	 * Returns the id of this monitoring agent (that will be responsible for message receiving). Creates one if not
	 * existent.
	 *
	 * @param greetings will be printed in the console and is only used to control registering
	 * @return the id
	 */
	public String getReceivingAgentId(String greetings) {
		System.out.println("Monitoring: Service requests receiving agent id: " + greetings);
		if (receivingAgent == null) {
			try {
				receivingAgent = MonitoringAgent.createMonitoringAgent(AGENT_PASS);
				receivingAgent.unlock(AGENT_PASS);
				Context.getCurrent().storeAgent(receivingAgent);
				Context.getCurrent().registerReceiver(receivingAgent);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return this.receivingAgent.getIdentifier();
	}

	public boolean hasBot() {
		return actingAgents != null && !actingAgents.isEmpty();
	}
}

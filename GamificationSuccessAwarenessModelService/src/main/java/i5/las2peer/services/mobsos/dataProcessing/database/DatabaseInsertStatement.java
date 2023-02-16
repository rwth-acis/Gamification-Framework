package i5.las2peer.services.mobsos.dataProcessing.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import i5.las2peer.api.logging.MonitoringEvent;
import i5.las2peer.logging.monitoring.MonitoringMessage;
import i5.las2peer.services.mobsos.dataProcessing.MonitoringMessageWithEncryptedAgents;

/**
 *
 * Helper Class that provides (static) methods to formulate a SQL statement
 * according to the given specifications. The statements are formulated
 * according to the database scheme that can be found in the "scripts" folder
 * provided with this project.
 *
 * @author Peter de Lange
 *
 */
public class DatabaseInsertStatement {

  /**
   *
   * The entry point to this class.
   *
   * @param con               a SQLDatabse connection
   * @param monitoringMessage a
   *                          {@link i5.las2peer.logging.monitoring.MonitoringMessage}
   *                          that contains the information to be stored
   * @param databaseType      the database type the statement should be formulated
   *                          for
   * @param table             the name of the table the query should be inserted
   *                          to
   * @param hashRemarks       whether you want to hash the remarks or not
   *
   * @return a SQL statement
   *
   * @throws Exception if the given information was not correct or sufficient for
   *                   the desired table entry
   *
   */
  public static PreparedStatement returnInsertStatement(Connection con, MonitoringMessage monitoringMessage,
      SQLDatabaseType databaseType, String table, boolean hashRemarks) throws Exception {
    MonitoringMessageWithEncryptedAgents message = new MonitoringMessageWithEncryptedAgents(monitoringMessage,
        hashRemarks);
    if (databaseType == SQLDatabaseType.MySQL) {
      if (table.equals("MESSAGE")) {
        return returnMySQLMessageStatement(con, message);
      }

      else if (table.equals("AGENT")) {
        return returnMySQLAgentStatement(con, message);
      }

      else if (table.equals("SERVICE")) {
        return returnMySQLServiceStatement(con, message);
      }

      else if (table.equals("NODE")) {
        return returnMySQLNodeStatement(con, message);
      }

      else if (table.equals("REGISTERED_AT")) {
        return returnMySQLRegisteredAtStatement(con, message);
      }

      else {
        throw new Exception("Don't know table!");
      }

    }

    else {
      throw new Exception("Not supported database type!");
    }

  }

  /**
   *
   * Returns a MySQL statement for the message table.
   *
   * @param message a {@link i5.las2peer.logging.monitoring.MonitoringMessage}
   *                that contains the information to be stored
   *
   * @return a MySQL statement
   *
   */
  private static PreparedStatement returnMySQLMessageStatement(Connection con,
      MonitoringMessageWithEncryptedAgents message) {
    PreparedStatement statement = null;
    try {
      statement = con.prepareStatement("INSERT INTO MESSAGE (`EVENT`, `TIME_STAMP`, `SOURCE_NODE`, `SOURCE_AGENT`, "
          + "`DESTINATION_NODE`, `DESTINATION_AGENT`, `REMARKS`) VALUES (?,?,?,?,?,?,?);");
      statement.setString(1, message.getEvent().toString()); // EVENT
      statement.setString(2, new Timestamp(message.getTimestamp()).toString()); // TIME_STAMP
      if (message.getSourceNode() != null) {
        statement.setString(3, message.getSourceNode().substring(0, 12)); // SOURCE_NODE
      } else {
        statement.setString(3, "");
      }
      if (message.getSourceAgentId() != null) {
        statement.setString(4, message.getSourceAgentId().toString()); // SOURCE_AGENT
      } else {
        statement.setString(4, "");
      }
      if (message.getDestinationNode() != null) {
        statement.setString(5, message.getDestinationNode().substring(0, 12)); // DESTINATION_NODE
      } else {
        statement.setString(5, "");
      }
      if (message.getDestinationAgentId() != null) {
        statement.setString(6, message.getDestinationAgentId().toString()); // DESTINATION_AGENT
      } else {
        statement.setString(6, "");
      }
      if (message.getRemarks() != null) {
        statement.setString(7, message.getJsonRemarks()); // REMARKS AS JSON
      } else {
        statement.setString(7, "");
      }
    } catch (Exception e) {
      // TODO LOG
      System.out.println(e.getMessage());
      e.printStackTrace();
    }
    return statement;
  }

  /**
   *
   * Returns a MySQL statement for the Agent table.
   *
   * @param monitoringMessage a
   *                          {@link i5.las2peer.logging.monitoring.MonitoringMessage}
   *                          that contains the information to be stored
   *
   * @return a MySQL statement
   *
   * @throws Exception if the given information was not correct or sufficient for
   *                   the desired table entry
   *
   */
  private static PreparedStatement returnMySQLAgentStatement(Connection con,
      MonitoringMessageWithEncryptedAgents monitoringMessage) throws Exception {
    PreparedStatement statement = null;
    try {
      statement = con
          .prepareStatement("INSERT INTO AGENT(AGENT_ID, TYPE) VALUES(?,?) ON DUPLICATE KEY UPDATE AGENT_ID=AGENT_ID;");

      if (monitoringMessage.getSourceNode() == null || monitoringMessage.getSourceAgentId() == null
          || monitoringMessage.getRemarks() == null)
        throw new Exception("Missing information for persisting agent entity!");

      String agentType = null;
      if (monitoringMessage.getEvent() == MonitoringEvent.SERVICE_ADD_TO_MONITORING)
        agentType = "SERVICE";
      else if (monitoringMessage.getEvent() == MonitoringEvent.AGENT_REGISTERED) {
        if (monitoringMessage.getRemarks().equals("ServiceAgent")) {
          throw new Exception("ServiceAgents are only persisted when added to monitoring!");
        } else if (monitoringMessage.getRemarks().contains("UserAgent")) {
          agentType = "USER";
        } else if (monitoringMessage.getRemarks().contains("GroupAgent")) {
          agentType = "GROUP";
        } else if (monitoringMessage.getRemarks().contains("MonitoringAgent")) {
          agentType = "MONITORING";
        } else if (monitoringMessage.getRemarks().contains("BotAgent")) {
          agentType = "BOT";
        } else if (monitoringMessage.getRemarks().contains("ServiceInfoAgent")) {
          agentType = "SERVICE_INFO";
        } else if (monitoringMessage.getRemarks().contains("Mediator")) {
          // Thats right, we treat mediators as agents (as from a monitoring point of
          // view, this is the same)
          agentType = "MEDIATOR";
        } else {
          throw new Exception("Unknown remarks entry for persisting agent entity: " + monitoringMessage.getRemarks());
        }
      } else {
        throw new Exception("Agent entities will only be persisted if registered at a node!");
      }
      statement.setString(1, monitoringMessage.getSourceAgentId());
      statement.setString(2, agentType);
    } catch (Exception e) {
      // TODO LOG
      e.printStackTrace();
    }
    return statement;
  }

  /**
   *
   * Returns a MySQL statement for the Service table.
   *
   * @param monitoringMessage a
   *                          {@link i5.las2peer.logging.monitoring.MonitoringMessage}
   *                          that contains the information to be stored
   *
   * @return a MySQL statement
   *
   * @throws Exception if the given information was not correct or sufficient for
   *                   the desired table entry
   *
   */
  private static PreparedStatement returnMySQLServiceStatement(Connection con,
      MonitoringMessageWithEncryptedAgents monitoringMessage) throws Exception {
    PreparedStatement statement = null;
    try {
      statement = con.prepareStatement(
          "INSERT INTO SERVICE(AGENT_ID, SERVICE_CLASS_NAME, SERVICE_PATH) VALUES(?,?,?) ON DUPLICATE KEY UPDATE AGENT_ID=AGENT_ID;");

      if (monitoringMessage.getSourceAgentId() == null || monitoringMessage.getJsonRemarks() == null)
        throw new Exception("Missing information for persisting service entity!");
      statement.setString(1, monitoringMessage.getSourceAgentId());
      JSONParser parser = new JSONParser();
      JSONObject params = (JSONObject) parser.parse(monitoringMessage.getJsonRemarks());

      String serviceName = (String) params.get("serviceName");
      String serviceAlias = (String) params.get("serviceAlias");
      statement.setString(2, serviceName);
      if (!serviceAlias.equals("null")) {
        statement.setString(3, serviceAlias);
      } else {
        statement.setString(3, "");
      }
    } catch (Exception e) {
      // TODO LOG
      e.printStackTrace();
    }
    return statement;
  }

  /**
   *
   * Returns a MySQL statement for the "Registered At" table.
   *
   * @param message a {@link i5.las2peer.logging.monitoring.MonitoringMessage}
   *                that contains the information to be stored
   *
   * @return a MySQL statement
   *
   * @throws Exception if the given information was not correct or sufficient for
   *                   the desired table entry
   *
   */
  private static PreparedStatement returnMySQLRegisteredAtStatement(Connection con,
      MonitoringMessageWithEncryptedAgents message) throws Exception {
    PreparedStatement statement = null;
    try {
      if (message.getTimestamp() == null || message.getSourceNode() == null)
        throw new Exception("Missing information for 'registered at' entity!");

      String timestamp = new Timestamp(message.getTimestamp()).toString();
      String nodeId = message.getSourceNode().substring(0, 12);

      if (message.getEvent() == MonitoringEvent.AGENT_REGISTERED
          || message.getEvent() == MonitoringEvent.SERVICE_ADD_TO_MONITORING) {
        if (message.getSourceAgentId() == null)
          throw new Exception("Missing information for persisting 'registered at' entity!");
        statement = con
            .prepareStatement("INSERT INTO REGISTERED_AT(REGISTRATION_DATE, AGENT_ID, RUNNING_AT) VALUES(?, ?, ?);");
        statement.setString(1, timestamp);
        statement.setString(2, message.getSourceAgentId());
        statement.setString(3, nodeId);
      } else if (message.getEvent() == MonitoringEvent.AGENT_REMOVED
          || message.getEvent() == MonitoringEvent.SERVICE_SHUTDOWN) {
        statement = con.prepareStatement(
            "UPDATE REGISTERED_AT SET UNREGISTRATION_DATE=? WHERE RUNNING_AT=? AND AGENT_ID=? AND UNREGISTRATION_DATE IS NULL;");
        statement.setString(1, timestamp);
        statement.setString(2, nodeId);
        statement.setString(3, message.getSourceAgentId());
      } else { // We need to unregister those who have not yet unregistered -> update
               // statement!
        statement = con.prepareStatement(
            "UPDATE REGISTERED_AT SET UNREGISTRATION_DATE=? WHERE RUNNING_AT=? AND UNREGISTRATION_DATE IS NULL;");
        statement.setString(1, timestamp);
        statement.setString(2, nodeId);
      }
    } catch (Exception e) {
      // TODO LOG
      e.printStackTrace();
    }
    return statement;
  }

  /**
   *
   * Returns a MySQL statement for the Node table.
   *
   * @param monitoringMessage a
   *                          {@link i5.las2peer.logging.monitoring.MonitoringMessage}
   *                          that contains the information to be stored
   *
   * @return a MySQL statement
   *
   * @throws Exception if the given information was not correct or sufficient for
   *                   the desired table entry
   *
   */
  private static PreparedStatement returnMySQLNodeStatement(Connection con,
      MonitoringMessageWithEncryptedAgents monitoringMessage) throws Exception {
    PreparedStatement statement = null;
    try {
      statement = con.prepareStatement(
          "INSERT INTO NODE(NODE_ID, NODE_LOCATION) VALUES(?,?) ON DUPLICATE KEY UPDATE NODE_ID = NODE_ID;");
      if (monitoringMessage.getSourceNode() == null)
        throw new Exception("Missing information for persisting node entity!");
      String nodeId = monitoringMessage.getSourceNode().substring(0, 12);
      int startingLocationPosition = monitoringMessage.getSourceNode().lastIndexOf("/") + 1;
      String nodeLocation = monitoringMessage.getSourceNode().substring(startingLocationPosition);
      statement.setString(1, nodeId);
      statement.setString(2, nodeLocation);
    } catch (Exception e) {
      // TODO LOG
      e.printStackTrace();
    }
    return statement;
  }

}

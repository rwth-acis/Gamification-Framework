//package i5.las2peer.services.gamificationManagerService.database;
//
//import i5.las2peer.api.Service;
//import i5.las2peer.logging.NodeObserver.Event;
//import i5.las2peer.p2p.Node;
//import i5.las2peer.security.Agent;
//
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.LinkedList;
//import java.util.List;
//
///**
// * 
// * SQLDatabaseManager.java
// *<br>
// * The Manager of the SQLDatabases. This Class provides methods to handle the users databases.
// * 
// */
//public class SQLDatabaseManager {
//	// used to store (and retrieve during execution) the settings for the users' databases
//	private HashMap<String, SQLDatabaseSettings> userDatabaseMap = new HashMap<String, SQLDatabaseSettings>();
//	private HashMap<String, SQLDatabase> loadedDatabases = new HashMap<String, SQLDatabase>();
//	
//		
//	private SQLDatabase storageDatabase = null;
//	private boolean connected = false;
//	private Service service = null;
//	
//	private boolean connect() {
//		try {
//			storageDatabase.connect();
//			connected = true;
//		} catch (Exception e) {
//			e.printStackTrace();
//			return false;
//		}
//		return true;
//	}
//	
//	private void disconnect(boolean wasConnected) {
//		if (!wasConnected && connected) {
//			storageDatabase.disconnect();
//			connected = false;
//		}
//	}
//
//	private void disconnect() {
//		disconnect(false);
//	}
//	
//	
//	private boolean initializeUser() {
//		if (connected || connect()) {
//			try {
//				PreparedStatement p = storageDatabase.prepareStatement("SELECT DISTINCT ID FROM QVS.USERS WHERE ID = ?");
//				p.setLong(1, getActiveAgent().getId());
//				ResultSet s = p.executeQuery();
//				if (!s.next()) {
//					p = storageDatabase.prepareStatement("REPLACE INTO USERS (ID) VALUES (?)");
//					p.setLong(1, getActiveAgent().getId());
//					p.executeUpdate();
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//				return false;
//			}
//			return true;
//		}
//		return false;
//	}
//	
//	
//	/*************** "service" helper methods *************************/
//	
//	/**
//	 * get the current l2p thread
//	 * @return the L2pThread we're currently running in
//	 */
//	
//	/** 
//	 * get the currently active agent
//	 * @return active agent
//	 */
//	protected Agent getActiveAgent () {
//		return service.getContext().getMainAgent();
//	}
//	
//	/**
//	 * write a log message
//	 * 
//	 * @param message
//	 */
//	protected void logMessage ( String message ) {
//		getActiveNode().observerNotice(Event.SERVICE_MESSAGE, this.getClass().getName() + ": " + message);
//	}
//	
//
//	/**
//	 * get the currently active l2p node (from the current thread context)
//	 * 
//	 * @return 	the currently active las2peer node
//	 */
//	protected Node getActiveNode() {
//		return service.getContext().getLocalNode();
//	}
//	
//	/************************** end of service helper methods ************************************/
//	
//	/**
//	 * get an id String for the envelope stored for an user
//	 * 
//	 * @param user
//	 */
//	public static String getEnvelopeId ( Agent user ) {
//		return "userDBs-" + user.getId();
//	}	
//	
//	
//	public SQLDatabaseManager(Service service, SQLDatabase storageDatabase) {
//		this.service = service;
//		this.storageDatabase = storageDatabase;
//		// get the user's security object which contains the database information
//
//		initializeUser();
//		
//		SQLDatabaseSettings[] settings = null;
//		
//		try {
//			connect();
//			PreparedStatement p = storageDatabase.prepareStatement(
//					"SELECT * FROM QVS.DATABASE_CONNECTIONS WHERE USER = ?;");
//			p.setLong(1, getActiveAgent().getId());
//			ResultSet databases = p.executeQuery();
//			settings = SQLDatabaseSettings.fromResultSet(databases);
//		} catch ( Exception e ) {
//			logMessage("Failed to get the users' SQL settings. " + e.getMessage());
//		} finally {
//			disconnect();
//		}
//		
//		for ( SQLDatabaseSettings setting: settings )
//			userDatabaseMap.put ( setting.getKey(), setting);
//	}
//	
//	// add database to users' security object
//	public boolean addDatabase(String appId, String username, String password, String database, String host, int port) throws Exception {
//		try {			
//			//TODO: sanity checks for the parameters
//			if(databaseExists(key)) {
//				throw new Exception("Database with key " + key + " already exists!");
//			}
//			
//			SQLDatabaseSettings databaseSettings = new SQLDatabaseSettings(key, jdbcInfo, username, password, database, host, port);
//			if (!connect()) {
//				throw new Exception("Could not connect to the database");
//			}
//			PreparedStatement p = storageDatabase.prepareStatement(
//					"REPLACE INTO `DATABASE_CONNECTIONS`(`JDBCINFO`, `KEY`, `USERNAME`, `PASSWORD`,"
//					+ "`DATABASE`, `HOST`, `PORT`, `USER`) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
//			p.setInt(1, databaseSettings.getJdbcInfo().getCode());
//			p.setString(2, databaseSettings.getKey());
//			p.setString(3, databaseSettings.getUsername());
//			p.setString(4, databaseSettings.getPassword());
//			p.setString(5, databaseSettings.getDatabase());
//			p.setString(6, databaseSettings.getHost());
//			p.setInt(7, databaseSettings.getPort());
//			p.setLong(8, getActiveAgent().getId());
//			p.executeUpdate();
//			disconnect();
//			userDatabaseMap.put(databaseSettings.getKey(), databaseSettings);
//			
//			return true;
//		}
//		catch(Exception e) {
//			e.printStackTrace();
//			logMessage(e.getMessage());
//			throw e;
//		}
//	}
//	
//	// removes a database from the hashmap and the security objects
//	public boolean removeDatabase(String key) throws Exception {
//		try {
//			if(!databaseExists(key)) {
////				throw new Exception("Database with key " + key + " does not exists!");
//				return false;
//			}
//			
//			if(userDatabaseMap != null && userDatabaseMap.containsKey( key )) {
//				// delete from hash map and database
//				removeDB(key);
//				userDatabaseMap.remove(key);
//			}
//			
//			return true;
//		}
//		catch(Exception e) {
//			e.printStackTrace();
//			logMessage(e.getMessage());
//			throw e;
//		}
//	}
//	
//	public boolean databaseExists(String key) {
//		try {
//			return (userDatabaseMap.get(key) != null);
//		}
//		catch(Exception e) {
//			e.printStackTrace();
//			logMessage(e.getMessage());
//		}
//		return false;
//	}
//	
//	public int getDatabaseCount() {
//		return this.userDatabaseMap.size();
//	}
//	
//	// get a list of the names of all databases of the user
//	public List<String> getDatabaseKeyList() {
//		try {
//			LinkedList<String> keyList = new LinkedList<String>();
//			Iterator<String> iterator = this.userDatabaseMap.keySet().iterator();
//			while(iterator.hasNext()) {
//				keyList.add(iterator.next());
//			}
//			
//			return keyList;
//		}
//		catch(Exception e) {
//			e.printStackTrace();
//			logMessage(e.getMessage());
//		}
//		return null;
//	}
//	
//	// returns a list of all database settings elements
//	public List<SQLDatabaseSettings> getDatabaseSettingsList() {
//		try {
//			LinkedList<SQLDatabaseSettings> settingsList = new LinkedList<SQLDatabaseSettings>();
//			Iterator<SQLDatabaseSettings> iterator = this.userDatabaseMap.values().iterator();
//			while(iterator.hasNext()) {
//				settingsList.add(iterator.next());
//			}
//			
//			return settingsList;
//		}
//		catch(Exception e) {
//			e.printStackTrace();
//			logMessage(e.getMessage());
//		}
//		return null;
//	}
//	
//	public String getDatabaseIdString(String databaseKey) throws Exception {
//		try {
//			SQLDatabaseSettings databaseSettings = userDatabaseMap.get(databaseKey);
//			
//			if(databaseSettings == null) {
//				return null;
//			}
//			
//			return databaseSettings.getHost() + ":" +databaseSettings.getPort() + "/" + databaseSettings.getDatabase();
//		}
//		catch(Exception e) {
//			e.printStackTrace();
//			logMessage(e.getMessage());
//			throw e;
//		}
//	}
//	
//    /**
//     * Get Settings of a database by its database name
//     */
//	public SQLDatabaseSettings getDatabaseByName(String databasName) throws Exception {
//		for (SQLDatabaseSettings db : userDatabaseMap.values()) {
//			if (db.getDatabase().equals(databasName)) {
//				return db;
//			}
//		}
//		return null;
//	}
//
//    /**
//     * Get Settings of a database by its database name
//     */
//	public SQLDatabaseSettings getDatabaseSettings(String databasKey) throws Exception {
//		return userDatabaseMap.get(databasKey);
//	}
//
//	// get a instance of a SQL database (JDBC based)
//	public SQLDatabase getDatabaseInstance(String databaseKey) throws Exception {
//		try {
//			SQLDatabase sqlDatabase = loadedDatabases.get(databaseKey);
//
//			if(sqlDatabase != null) {
//				//TODO: check that the database is still open/valid
//			} else {
//				SQLDatabaseSettings databaseSettings = userDatabaseMap.get(databaseKey);
//
//				if(databaseSettings == null) {
//					// the requested database is not known
//					String dbKeyListString = "";
//					Iterator<String> iterator = getDatabaseKeyList().iterator();
//					while(iterator.hasNext()) {
//						dbKeyListString += " " + iterator.next();
//					}
//
//					throw new Exception("The requested database is not known/configured! (Requested:" + databaseKey + ", Available: "+dbKeyListString + ")");
//				}
//
//				sqlDatabase = new SQLDatabase(databaseSettings);
//
//				// try to connect ...
//				sqlDatabase.connect();
//			}
//			return sqlDatabase;
//		}
//		catch(Exception e) {
//			e.printStackTrace();
//			logMessage(e.getMessage());
//			throw e;
//		}
//	}
//	
//	
//	// closes/disconnects all loaded databases
//	public boolean closeAllDatabaseInstances() {
//		try {
//			boolean noErrorOccurred = true;
//			Iterator<SQLDatabase> iterator = loadedDatabases.values().iterator();
//			
//			while(iterator.hasNext()) {
//				SQLDatabase sqlDatabase = iterator.next();
//				if(!sqlDatabase.disconnect()) {
//					noErrorOccurred = false;
//				}
//			}
//			
//			return noErrorOccurred;
//		}
//		catch(Exception e) {
//			e.printStackTrace();
//			logMessage(e.getMessage());
//		}
//		
//		return false;
//	}
//		
//	/**
//	 * Remove given database from the database
//	 */
//	private void removeDB(String databaseKey) throws SQLException {
//		try {
//			connect();
//			PreparedStatement s = storageDatabase.prepareStatement("DELETE FROM `DATABASE_CONNECTIONS` WHERE `KEY` = ? AND `USER` = ?");
//			s.setString(1, databaseKey);
//			s.setLong(2, getActiveAgent().getId());
//			s.executeUpdate();
//			disconnect();
//		} catch (Exception e) {
//			logMessage("Error removing the Database! " + e);
//			System.out.println ( "QV critical:");
//			e.printStackTrace();
//		}
//	}
//}

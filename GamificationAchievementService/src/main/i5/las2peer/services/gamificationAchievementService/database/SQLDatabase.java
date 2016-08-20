package i5.las2peer.services.gamificationAchievementService.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;


/**
 * 
 * Stores the database credentials and provides access to query execution.
 * The original code was taken from the QueryVisualizationService.
 * 
 * @author Peter de Lange
 * 
 */
public class SQLDatabase{
	
	private Connection connection = null;
	private boolean isConnected = false;
	
	private String driverClassName = null;
	private String username = null;
	private String password = null;
	private String database = null;
	private String host = null;
	private int port = -1;
	
	
	/**
	 * 
	 * Constructor for a database instance.
	 * 
	 * @param driverClassName class name
	 * @param username username
	 * @param password password
	 * @param database database
	 * @param host host
	 * @param port port
	 * 
	 */
	public SQLDatabase(String driverClassName, String username, String password, String database, String host, int port){		
		this.driverClassName = driverClassName;
		this.username = username;
		this.password = password;
		this.host = host;
		this.port = port;
		this.database = database;
	}
	
	
	/**
	 * 
	 * Connects to the database.
	 * 
	 * @return true, if connected
	 * 
	 * @throws ClassNotFoundException if the driver was not found
	 * @throws SQLException if connecting did not work
	 * 
	 */
	public boolean connect() throws Exception{
		//if(this.connection.isClosed() || this.connection == null){
			try {
				Class.forName(driverClassName).newInstance();
				String url = this.host + ":" + this.port + "/" + this.database;
				this.connection = DriverManager.getConnection(url, this.username, this.password);
				
				if(!this.connection.isClosed()){
					this.isConnected = true;
					return true;
				}
				else
				{
					return false;
				}
			
			} 
			catch (ClassNotFoundException e){
				throw new Exception("JDBC-Driver for requested database type not found! Make sure the library is defined in the settings and is placed in the library folder!", e);
			}
			catch (SQLException e){
				throw e;
			}
		//}
		//else{
		//	return false;
		//}
	}
	
	
	/**
	 * 
	 * Disconnects from the database.
	 * 
	 * @return true, if correctly disconnected
	 * 
	 */
	public boolean disconnect(){
		try{
			this.connection.close();
			this.isConnected = false;
			this.connection = null;
			
			return true;
		} 
		catch (SQLException e){
			e.printStackTrace();
			this.isConnected = false;
			this.connection = null;
		}
		return false;
	}
	
	
	/**
	 * 
	 * Checks, if this database instance is currently connected.
	 * 
	 * @return true, if connected
	 * 
	 */
	public boolean isConnected(){
		try{
			return (this.isConnected && this.connection != null && !this.connection.isClosed());
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	
	/**
	 * 
	 * Executes a SQL statement to insert an entry into the database.
	 * 
	 * @param SQLStatement sql statement
	 * 
	 * @return true, if correctly inserted
	 * 
	 * @throws SQLException problems inserting
	 * 
	 */
	public boolean store(String SQLStatement) throws SQLException{
		// make sure one is connected to a database
		if(!isConnected()){
			System.err.println("No database connection.");
			return false;
		}
		
		Statement statement = connection.createStatement();
		statement.executeUpdate(SQLStatement);
		return true;
		
	}
	
	/**
	 * Get user name that access the database
	 * 
	 * @return user name
	 */
	public String getUser(){
		return this.username;
	}
	
	/**
	 * Get password used to access the database
	 * 
	 * @return password
	 */
	public String getPassword(){
		return this.password;
	}
	
	/**
	 * Get database name
	 * 
	 * @return database name
	 */
	public String getDatabase(){
		return this.database;
	}
	
	/**
	 * Get host of database
	 * 
	 * @return host of database
	 */
	public String getHost(){
		return this.host;
	}
	
	/**
	 * Get port of host of database
	 * 
	 * @return port of host of database
	 */
	public int getPort(){
		return this.port;
	}
	
	/**
	 * Get connection to access the database
	 * 
	 * @return connection
	 */
	public Connection getConnection(){
		return this.connection;
	}
	
	
}

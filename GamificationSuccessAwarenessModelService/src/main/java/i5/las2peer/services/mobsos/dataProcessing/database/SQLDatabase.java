package i5.las2peer.services.mobsos.dataProcessing.database;

import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.dbcp2.BasicDataSource;

/**
 *
 * Stores the database credentials and provides access to query execution. The
 * original code was taken from the QueryVisualizationService.
 *
 * @author Peter de Lange
 *
 */
public class SQLDatabase {

  private BasicDataSource dataSource;

  private SQLDatabaseType jdbcInfo = null;
  private String username = null;
  private String password = null;
  private String database = null;
  private String host = null;
  private int port = -1;

  /**
   *
   * Constructor for a database instance.
   *
   * @param jdbcInfo the driver you are using
   * @param username login name
   * @param password password
   * @param database database name
   * @param host     host for the connection
   * @param port     port of the SQL server
   *
   */
  public SQLDatabase(SQLDatabaseType jdbcInfo, String username, String password, String database, String host,
      int port) {

    this.jdbcInfo = jdbcInfo;
    this.username = username;
    this.password = password;
    this.host = host;
    this.port = port;
    this.database = database;

    BasicDataSource ds = new BasicDataSource();
    String urlPrefix = jdbcInfo.getURLPrefix(this.host, this.database, this.port)
        + "?autoReconnect=true&useSSL=false&serverTimezone=UTC";
    ds.setUrl(urlPrefix);
    ds.setUsername(username);
    ds.setPassword(password);
    ds.setDriverClassName(jdbcInfo.getDriverName());
    ds.setPoolPreparedStatements(true);
    ds.setTestOnBorrow(true);
    ds.setRemoveAbandonedOnBorrow(true);
    ds.setRemoveAbandonedOnMaintenance(true);
    ds.setMaxOpenPreparedStatements(100);
    ds.setMaxConnLifetimeMillis(1000 * 60 * 60);

    dataSource = ds;
    setValidationQuery();
  }

  /**
   *
   * Executes a SQL statement to insert an entry into the database.
   *
   * @param SQLStatment a SQLStatement
   *
   * @return true, if correctly inserted
   *
   * @throws SQLException problems inserting
   *
   */
  @Deprecated
  public boolean store(String SQLStatment) throws SQLException {
    // make sure one is connected to a database
    if (!dataSource.getConnection().isValid(5000)) {
      System.err.println("No database connection.");
      return false;
    }
    Statement statement = dataSource.getConnection().createStatement();
    statement.executeUpdate(SQLStatment);
    return true;

  }

  public String getUser() {
    return this.username;
  }

  public String getPassword() {
    return this.password;
  }

  public String getDatabase() {
    return this.database;
  }

  public String getHost() {
    return this.host;
  }

  public int getPort() {
    return this.port;
  }

  public SQLDatabaseType getJdbcInfo() {
    return jdbcInfo;
  }

  public BasicDataSource getDataSource() {
    return dataSource;
  }

  private void setValidationQuery() {
    switch (jdbcInfo.getCode()) {
    case 1:
      dataSource.setValidationQuery("SELECT 1;");
    }
  }

}

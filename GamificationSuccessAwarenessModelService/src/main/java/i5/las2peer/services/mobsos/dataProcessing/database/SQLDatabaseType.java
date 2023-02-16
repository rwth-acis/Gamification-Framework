package i5.las2peer.services.mobsos.dataProcessing.database;

/**
 *
 * Enumeration class that provides the right drivers according to the database
 * type. The original code was taken from the QueryVisualizationService.
 *
 * This implementation currently only supports MySQL.
 *
 */
public enum SQLDatabaseType {

  /**
   * A MySQL database. Works with the "mysqlConnectorJava-8.0.13.jar" archive.
   */
  MySQL(1, "com.mysql.cj.jdbc.Driver", "mysql");

  private final int code;
  private final String driver;
  private final String jdbc;

  SQLDatabaseType(int code, String driverName, String jdbc) {
    this.driver = driverName;
    this.jdbc = jdbc;
    this.code = code;
  }

  /**
   *
   * Returns the code of the database.
   *
   * @return a code
   *
   */
  public int getCode() {
    return this.code;
  }

  /**
   *
   * Returns the database type.
   *
   * @param code the number corresponding to a database type
   *
   * @return the corresponding {@link SQLDatabaseType} representation
   *
   */
  public static SQLDatabaseType getSQLDatabaseType(int code) {
    switch (code) {
    case 1:
      return SQLDatabaseType.MySQL;
    }
    return null;
  }

  /**
   *
   * Returns the driver name of the corresponding database. The library of this
   * driver has to be in the "lib" folder.
   *
   * @return a driver name
   *
   */
  public String getDriverName() {
    return driver;
  }

  /**
   *
   * Constructs a URL prefix that can be used for addressing a database.
   *
   * @param host     a database host address
   * @param database the database name
   * @param port     the port the database is running at
   *
   * @return a String representing the URL prefix
   *
   */
  public String getURLPrefix(String host, String database, int port) {
    return "jdbc:" + jdbc + "://" + host + ":" + port + "/" + database;
  }

}

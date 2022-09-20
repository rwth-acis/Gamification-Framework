package i5.las2peer.services.gamification.commons.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * This class manages database credentials and provides connection from a connection pooling system.<br>
 * <br>
 * This class is implemented as a 'semi'-singleton. The {@link #getInstance(DatabaseConfig)} method returns the same
 * instance if the given configuration is the same as the one of the cached instance (usually this should be the case,
 * since the configuration is only changed before application startup in the '*.properties' files)
 */
public class DatabaseManager {

	private static final Logger logger = Logger.getLogger(DatabaseManager.class.getName());

	private static DatabaseManager instance;

	/** Configuration used to create the data source. */
	private final DatabaseConfig config;

	/** The data source from which we can get (pooled) DB connections. */
	private final BasicDataSource dataSource;

	private DatabaseManager(DatabaseConfig config) {
		this.config = config;
		// prepare and configure data source
		this.dataSource = createDataSource(config);
	}

	public static DatabaseManager getInstance(DatabaseConfig config) {
		if (instance == null) {
			logger.info("No instance. Creating new one...");
			instance = new DatabaseManager(config);
		} else {
			if (!instance.supportsConfig(config)) {
				logger.info("Instance already existed, but new configuration requested. Creating new instance...");
				instance = new DatabaseManager(config);
			}
		}
		return instance;
	}

	/**
	 * Returns a {@link DatabaseManager} instance that matches the given configuration.<br>
	 * <br>
	 * This method is an alternative to {@link #createDataSource(DatabaseConfig)} that is primarily meant to
	 * replace constructor calls in existing code.
	 *
	 * @param jdbcDriverClassName
	 * @param jdbcLogin
	 * @param jdbcPass
	 * @param jdbcUrl
	 * @param jdbcSchema
	 * @return
	 */
	public static DatabaseManager getInstance(String jdbcDriverClassName, String jdbcLogin, String jdbcPass, String jdbcUrl,
											  String jdbcSchema) {
		return getInstance(new DatabaseConfig(jdbcDriverClassName, jdbcLogin, jdbcPass, jdbcUrl, jdbcSchema));
	}

	/**
	 * Returns whether the the {@link DatabaseManager} provides a JDBC connections that
	 * match the requested configuration.
	 *
	 * @param requestedConfig the database configuration for which connections will be requested
	 * @return
	 */
	public boolean supportsConfig(DatabaseConfig requestedConfig) {
		return config.equals(requestedConfig);
	}

	private BasicDataSource createDataSource(DatabaseConfig config) {
		logger.info("Creating new dataSource");
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDefaultAutoCommit(true);
		dataSource.setDriverClassName(config.getJdbcDriverClassName());
		dataSource.setUsername(config.getJdbcLogin());
		dataSource.setPassword(config.getJdbcPass());
		dataSource.setUrl(config.getJdbcUrl() + config.getJdbcSchema());
		dataSource.setValidationQuery("SELECT 1");
		dataSource.setDefaultQueryTimeout(1000);
		dataSource.setMaxConnLifetimeMillis(100000);
		return dataSource;
	}

	public Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}

	public static class DatabaseConfig {
		private final String jdbcDriverClassName;
		private final String jdbcLogin;
		private final String jdbcPass;
		private final String jdbcUrl;
		private final String jdbcSchema;

		public DatabaseConfig(String jdbcDriverClassName, String jdbcLogin, String jdbcPass, String jdbcUrl, String jdbcSchema) {
			this.jdbcDriverClassName = jdbcDriverClassName;
			this.jdbcLogin = jdbcLogin;
			this.jdbcPass = jdbcPass;
			this.jdbcUrl = jdbcUrl;
			this.jdbcSchema = jdbcSchema;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;
			DatabaseConfig that = (DatabaseConfig) o;

			return new EqualsBuilder()
					.append(jdbcDriverClassName, that.jdbcDriverClassName)
					.append(jdbcLogin, that.jdbcLogin)
					.append(jdbcPass, that.jdbcPass)
					.append(jdbcUrl, that.jdbcUrl)
					.append(jdbcSchema, that.jdbcSchema)
					.isEquals();
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder(17, 37)
					.append(jdbcDriverClassName)
					.append(jdbcLogin)
					.append(jdbcPass)
					.append(jdbcUrl)
					.append(jdbcSchema)
					.toHashCode();
		}

		public String getJdbcDriverClassName() {
			return jdbcDriverClassName;
		}

		public String getJdbcLogin() {
			return jdbcLogin;
		}

		public String getJdbcPass() {
			return jdbcPass;
		}

		public String getJdbcUrl() {
			return jdbcUrl;
		}

		public String getJdbcSchema() {
			return jdbcSchema;
		}
	}
}

package de.hpi.bpt.chimera.database;

import de.hpi.bpt.chimera.util.PropertyLoader;
import de.hpi.bpt.chimera.util.ScriptRunner;
import org.apache.log4j.Logger;

import java.io.*;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


/**
 * This class knows the database credentials and provides a method to connect to the database. It
 * initializes the JDBC Driver and checks whether the schema version used in the database is smaller
 * then the version used by the engine. In that case the database is updated.
 */
public final class ConnectionWrapper {
	private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
	private static final String SCHEMA_DEFINITION_PATH = PropertyLoader.getProperty("database.schema.file");
	private static final int PATH_LENGTH = SCHEMA_DEFINITION_PATH.split("/").length;
	private static final String SCHEMA_DEFINITION_FILE = SCHEMA_DEFINITION_PATH.split("/")[PATH_LENGTH - 1];
	private static ConnectionWrapper instance = null;
	private static Logger log = Logger.getLogger(ConnectionWrapper.class);
	/**
	 * Stores the current schema version used in the Chimera engine. Is set to '-1', if reading the
	 * version file fails.
	 */
	private final int schemaVersion = getSchemaVersion();
	private String username;
	private String password;
	private String url;
	private String testUrl;
	private boolean testMode;

	/**
	 * Builds a connection for the database with the credentials taken from the
	 * config.properties file. It compares the schema version to the version used in the database and
	 * "updates" the database by dropping the schema and executing a SQL script to recreate it.
	 *
	 * @return the instance of a new ConnectionWrapper.
	 */
	public static synchronized ConnectionWrapper getInstance() {
		if (instance == null) {
			instance = new ConnectionWrapper();
			instance.initializeDatabaseConfiguration();
			if (instance.schemaVersionOutdated()) {
				instance.updateSchema();
			}
		}
		return instance;
	}

	/**
	 * Connects to either the production database (property {@literal mysql.schema})
	 * or test database (property {@literal mysql.test.schema}) depending on the value
	 * of the field {@value testMode}.
	 * It returns the {@link java.sql.Connection} object.
	 *
	 * @return the open connection.
	 */
	public java.sql.Connection connect() {
		java.sql.Connection conn = null;
		try {
			// Register JDBC driver
			Class.forName(JDBC_DRIVER);
			// Open a connection to productive or test database depending on parameter
			String urlToUse = testMode ? testUrl : url;
			conn = DriverManager.getConnection(urlToUse, username, password);
		} catch (ClassNotFoundException | SQLException e) {
			// Handle errors for Class.forName
			log.error("MySQL Connection Error:", e);
		}
		return conn;
	}

	/**
	 * Sets the mode of the ConnectionWrapper, which can be either true or false.
	 * If it is true, the connection is established with the test database.
	 * @param useTestDatabase
	 */
	public void setTestMode(boolean useTestDatabase) {
		testMode = useTestDatabase;
	}

	/**
	 * Checks whether a schema update is necessary.
	 *
	 * @return {@literal true} if the schema version in the database it outdated or not
	 * found in the database.
	 */
	private Boolean schemaVersionOutdated() {
		int dbVersion;
		try {
			dbVersion = getDatabaseVersion();
		} catch (SQLException e) {
			log.error("Could not read schema version used in database. Will update the schema.", e);
			return true;
		}
		log.info(String.format("Version in DB: %d,%nVersion of schema: %d", dbVersion, schemaVersion));
		return schemaVersion > dbVersion;
	}

	/**
	 * Reads the schema version used by the Chimera engine from file.
	 *
	 * @return the schema version.
	 */
	private int getSchemaVersion() {
		try {
			InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("schemaversion");
			String line = (new BufferedReader(new InputStreamReader(is))).readLine();
			return Integer.parseInt(line);
		} catch (IOException e) {
			log.error("Could no read schema version used by this build of the engine " + "(should be stored in a file called 'schemaversion')." + "%n Will NOT update schema.", e);
			return -1;
		}
	}

	/**
	 * Reads the schema version used in the database from table 'version'.
	 *
	 * @return version number stored in the database.
	 * @throws SQLException if version table does not exist or is empty.
	 */
	private int getDatabaseVersion() throws SQLException {
		try (java.sql.Connection conn = this.connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery("SHOW TABLES LIKE 'version'")) {
			// if version tables not exists => update schema
			if (!rs.next()) {
				throw new SQLException("Table 'version' does not exist.");
			}
			ResultSet versionNumberRs = stmt.executeQuery("SELECT * FROM version ");
			// if empty ResultSet => update schema
			if (!versionNumberRs.next()) {
				throw new SQLException("Table 'version' is empty.");
			}
			int versionNumber = versionNumberRs.getInt(1);
			versionNumberRs.close();
			return versionNumber;
		} catch (SQLException e) {
			log.warn(e.getMessage(), e);
			throw new SQLException("Error occurred while loading 'version' table");
		}
	}

	/**
	 * Updates the schema. It drops the existing schema, then creates a new schema with the
	 * same name and reads the table structure from the sql script.
	 * TODO: Fix possible SQL injection
	 */
	private void updateSchema() {
		try (java.sql.Connection conn = this.connect(); Statement stmt = conn.createStatement()) {
			InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(SCHEMA_DEFINITION_FILE);
			if (is == null) {
				throw new FileNotFoundException(String.format("The schema definition file %s could not be found.", SCHEMA_DEFINITION_FILE));
			}
			String schemaName = PropertyLoader.getProperty("mysql.schema");
			log.info("Trying to update the schema <" + schemaName + "> using sql file " + SCHEMA_DEFINITION_FILE);
			stmt.execute("DROP SCHEMA " + schemaName);
			stmt.execute("CREATE SCHEMA " + schemaName);
			stmt.execute("USE  " + schemaName);
			// now run the script to create the schema definition
			ScriptRunner runner = new ScriptRunner(conn, false, false);
			runner.runScript(new InputStreamReader(is, "UTF-8"));
			//
			stmt.execute("INSERT INTO version VALUES (" + schemaVersion + ")");
		} catch (SQLException e) {
			log.error("Could not update the schema.", e);
		} catch (FileNotFoundException e) {
			log.error("Did not find schema definition file. Failed to update schema.", e);
		} catch (IOException e) {
			log.error("Could not read schema definition file. Failed to update schema.", e);
		}
	}

	/**
	 * Reads database credentials from the property file and stores them in fields.
	 */
	private void initializeDatabaseConfiguration() {
		username = PropertyLoader.getProperty("mysql.username");
		password = PropertyLoader.getProperty("mysql.password");
		url = PropertyLoader.getProperty("mysql.url");
		testUrl = PropertyLoader.getProperty("mysql.test.url");
	}
}
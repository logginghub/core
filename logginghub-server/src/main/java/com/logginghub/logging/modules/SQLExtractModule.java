package com.logginghub.logging.modules;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLNonTransientException;
import java.sql.SQLTransientException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.logginghub.logging.DefaultLogEvent;
import com.logginghub.logging.LogEvent;
import com.logginghub.logging.modules.configuration.DatabaseConfiguration;
import com.logginghub.logging.modules.configuration.SQLExtractConfiguration;
import com.logginghub.logging.modules.configuration.SQLExtractQueryConfiguration;
import com.logginghub.logging.utils.ValueStripper2;
import com.logginghub.utils.Destination;
import com.logginghub.utils.FileUtils;
import com.logginghub.utils.Is;
import com.logginghub.utils.Metadata;
import com.logginghub.utils.StacktraceUtils;
import com.logginghub.utils.Stopwatch;
import com.logginghub.utils.StringUtils;
import com.logginghub.utils.TimeUtils;
import com.logginghub.utils.WorkerThread;
import com.logginghub.utils.logging.Logger;
import com.logginghub.utils.module.Module;
import com.logginghub.utils.module.ServiceDiscovery;

public class SQLExtractModule implements Module<SQLExtractConfiguration> {

    private Destination<LogEvent> eventDestination;
    private SQLExtractConfiguration configuration;

    private static final Logger logger = Logger.getLoggerFor(SQLExtractModule.class);

    private Map<String, Connection> connectionsByName = new HashMap<String, Connection>();
    private Map<String, DatabaseConfiguration> databaseConfigurationsByName = new HashMap<String, DatabaseConfiguration>();

    private int threadID = 0;

    @Override public void start() {

        List<DatabaseConfiguration> databases = configuration.getDatabases();
        for (DatabaseConfiguration databaseConfiguration : databases) {

            String name = databaseConfiguration.getName();
            Is.notNullOrEmpty(name, "You must provide a unique 'name' attribute for each <database > configuration");
            Is.falseStatement(connectionsByName.containsKey(name), "You must provide a unique 'name' attribute for each <database > configuration");

            Is.notNullOrEmpty(databaseConfiguration.getUrl(), "You must provide a 'url' attribute for database '{}'", name);

            databaseConfigurationsByName.put(name, databaseConfiguration);

            try {
                Connection connection = createConnection(databaseConfiguration);
                connectionsByName.put(name, connection);
            }
            catch (SQLException e) {
                logger.warn(e,
                            "Failed to establish database connection to '{}' for user '{}' : {}",
                            databaseConfiguration.getUrl(),
                            databaseConfiguration.getUser(),
                            e.getMessage());
            }
        }

        List<SQLExtractQueryConfiguration> queries = configuration.getQueries();
        for (SQLExtractQueryConfiguration sqlExtractQueryConfiguration : queries) {

            String database = sqlExtractQueryConfiguration.getDatabase();
            Connection connection = connectionsByName.get(database);
            if (connection == null) {
                logger.warn("There is a problem with query '{}' - you haven't configured a database called '{}' - please check your configuration",
                            sqlExtractQueryConfiguration.getSql(),
                            sqlExtractQueryConfiguration.getDatabase());
            }
            else {
                startQueryThread(sqlExtractQueryConfiguration);
            }

        }
    }

    private Connection getConnection(String database) throws SQLException {

        // jshaw - potentially a hack, we use the db configurations as locks to make sure threads
        // after other databases dont block on one that is being dodgy right now
        DatabaseConfiguration databaseConfiguration = databaseConfigurationsByName.get(database);

        Connection connection = null;
        synchronized (databaseConfiguration) {
            connection = connectionsByName.get(database);
            if (connection == null || connection.isClosed()) {
                logger.fine("Connection to database '{}' is closed, creating a new one", database);

                try {
                    connection = createConnection(databaseConfiguration);
                    connectionsByName.put(database, connection);
                }
                catch (SQLException e) {
                    logger.warn("Failed to establish a connection to database '{}' : {}", database, StacktraceUtils.combineMessages(e));
                    throw e;
                }
            }
        }

        return connection;

    }

    private Connection clearConnection(String database) {

        // jshaw - potentially a hack, we use the db configurations as locks to make sure threads
        // after other databases dont block on one that is being dodgy right now
        DatabaseConfiguration databaseConfiguration = databaseConfigurationsByName.get(database);

        Connection connection = null;
        synchronized (databaseConfiguration) {
            Connection removed = connectionsByName.remove(database);
            if (removed != null) {
                FileUtils.closeQuietly(removed);
            }
        }

        return connection;

    }

    private Connection createConnection(DatabaseConfiguration databaseConfiguration) throws SQLException {
        Properties properties = new Properties();

        properties.setProperty("user", databaseConfiguration.getUser());
        properties.setProperty("password", databaseConfiguration.getPassword());

        if (StringUtils.isNotNullOrEmpty(databaseConfiguration.getProperties())) {
            Metadata metadata = new Metadata();
            metadata.parse(databaseConfiguration.getProperties());
            properties.putAll(metadata);
        }

        Connection connection = DriverManager.getConnection(databaseConfiguration.getUrl(), properties);
        return connection;
    }

    private void startQueryThread(final SQLExtractQueryConfiguration query) {

        long initialDelay = TimeUtils.parseInterval(query.getInitialDelay());
        long repeatDelay = TimeUtils.parseInterval(query.getRepeatDelay());

        final ValueStripper2 valueStripper = new ValueStripper2(query.getPattern());

        WorkerThread.executeOngoing("LoggingHub-SQLExtractModule-" + (threadID++), initialDelay, repeatDelay, new Runnable() {
            @Override public void run() {

                int retries = 0;
                int retriesLimit = 1;
                boolean done = false;

                while (!done && retries <= retriesLimit) {

                    if (retries > 0) {
                        logger.fine("Retrying... {} times out of {}", retries, retriesLimit);
                    }

                    Statement statement = null;

                    try {
                        logger.finer("Getting connection to database '{}'", query.getDatabase());
                        Connection connection = getConnection(query.getDatabase());
                        statement = connection.createStatement();

                        logger.finer("Executing query '{}' against database '{}'", query.getSql(), query.getDatabase());
                        Stopwatch sw = Stopwatch.start("Query");
                        ResultSet rs = statement.executeQuery(query.getSql());
                        ResultSetMetaData md = rs.getMetaData();
                        int columns = md.getColumnCount();
                        int rows = 0;

                        while (rs.next()) {
                            rows++;
                            Map<String, String> row = new HashMap<String, String>(columns);
                            
                            for (int i = 1; i <= columns; ++i) {
                                Object object = rs.getObject(i);
                                if (object != null) {
                                    row.put(md.getColumnName(i).toLowerCase(), object.toString());
                                }
                            }

                            String message = valueStripper.depatternise(row);

                            DefaultLogEvent event = query.getTemplate().createEvent();
                            event.setMessage(message);

                            eventDestination.send(event);
                        }

                        sw.stop();
                        logger.fine("Successfully executed query '{}' against database '{}' in {} ms - generated {} results",
                                    query.getSql(),
                                    query.getDatabase(),
                                    sw.getFormattedDurationMillis(),
                                    rows);

                        done = true;
                    }
                    catch (SQLTransientException e) {
                        logger.warn("Failed to execute query against database [transient issue] '{}' : {}",
                                    query.getDatabase(),
                                    StacktraceUtils.combineMessages(e));
                        retries++;
                    }
                    catch (SQLNonTransientException e) {
                        logger.warn("Failed to execute query against database [non-transient issue] '{}' : {}",
                                    query.getDatabase(),
                                    StacktraceUtils.combineMessages(e));
                        clearConnection(query.getDatabase());
                        retries++;
                    }
                    catch (SQLException e) {
                        logger.warn("Failed to execute query against database [general issue] '{}' : {}",
                                    query.getDatabase(),
                                    StacktraceUtils.combineMessages(e));
                        clearConnection(query.getDatabase());
                        retries++;
                    }
                    finally {
                        FileUtils.closeQuietly(statement);
                    }
                }
            }
        });

    }

    @Override public void stop() {}

    @SuppressWarnings("unchecked") @Override public void configure(SQLExtractConfiguration configuration, ServiceDiscovery discovery) {

        this.configuration = configuration;
        eventDestination = discovery.findService(Destination.class, LogEvent.class, configuration.getLogEventDestinationRef());

    }

}

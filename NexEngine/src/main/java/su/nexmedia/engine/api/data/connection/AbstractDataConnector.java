package su.nexmedia.engine.api.data.connection;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.NexPlugin;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public abstract class AbstractDataConnector {

    protected final NexPlugin<?> plugin;
    protected final String url;
    protected final HikariConfig config;
    protected final HikariDataSource dataSource;
    private static final String DATA_POOL_NAME = "NexEngineHikariPool";

    public AbstractDataConnector(@NotNull NexPlugin<?> plugin, @NotNull String url) {
        this(plugin, url, null, null, null, null, null, null, null);
    }

    public AbstractDataConnector(@NotNull NexPlugin<?> plugin, @NotNull String url,
                                 @Nullable String userName, @Nullable String password,
                                 @Nullable Integer size, @Nullable Integer idle, @Nullable Long lifetime,
                                 @Nullable Long keepalive, @Nullable Long timeout) {

        try { // Try to use the new driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e1) {
            try { // Otherwise fallback to the old one or just ignore if it still can't be found
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException ignored) {
            }
        }

        this.plugin = plugin;
        this.url = url;

        // Create the Connector config
        this.config = new HikariConfig();

        // Create jdbc driver connection url
        this.config.setJdbcUrl(url);

        // Authenticate
        if (userName != null)
            this.config.setUsername(userName);
        if (password != null)
            this.config.setPassword(password);


        if (this instanceof ConnectorMySQL) {
            // Set connection pool options
            this.config.setMaximumPoolSize(size);
            this.config.setMinimumIdle(idle);
            this.config.setMaxLifetime(lifetime);
            this.config.setKeepaliveTime(keepalive);
            this.config.setConnectionTimeout(timeout);
            this.config.setPoolName(DATA_POOL_NAME);

            // Set additional connection pool properties
            this.config.setDataSourceProperties(new Properties() {{
                put("cachePrepStmts", "true");
                put("prepStmtCacheSize", "250");
                put("prepStmtCacheSqlLimit", "2048");
                put("useServerPrepStmts", "true");
                put("useLocalSessionState", "true");
                put("useLocalTransactionState", "true");
                put("rewriteBatchedStatements", "true");
                put("cacheResultSetMetadata", "true");
                put("cacheServerConfiguration", "true");
                put("elideSetAutoCommits", "true");
                put("maintainTimeStats", "false");
            }});
        } else {
            // SQLite doesn't need more than 1 active connection.
            this.config.setMaximumPoolSize(1);
        }

        this.dataSource = new HikariDataSource(config);
    }


    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            this.dataSource.close();
        }
    }

    @NotNull
    public final Connection getConnection() throws SQLException {
        return this.dataSource.getConnection();
    }
}
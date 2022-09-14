package su.nexmedia.engine.api.data.connection;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.NexPlugin;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class AbstractDataConnector {

    protected final NexPlugin<?> plugin;
    protected final String       url;
    protected final HikariConfig config;
    protected final HikariDataSource dataSource;

    public AbstractDataConnector(@NotNull NexPlugin<?> plugin, @NotNull String url) {
        this(plugin, url, null, null);
    }

    public AbstractDataConnector(@NotNull NexPlugin<?> plugin, @NotNull String url,
                                 @Nullable String userName, @Nullable String password) {
        this.plugin = plugin;
        this.url = url;

        this.config = new HikariConfig();
        this.config.setJdbcUrl(url);
        if (userName != null) this.config.setUsername(userName);
        if (password != null) this.config.setPassword(password);
        this.config.addDataSourceProperty("cachePrepStmts", "true");
        this.config.addDataSourceProperty("prepStmtCacheSize", "250");
        this.config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        this.dataSource = new HikariDataSource(this.config);
    }

    public void close() {
        //this.dataSource.close();
    }

    @NotNull
    public final Connection getConnection() throws SQLException {
        return this.dataSource.getConnection();
    }
}

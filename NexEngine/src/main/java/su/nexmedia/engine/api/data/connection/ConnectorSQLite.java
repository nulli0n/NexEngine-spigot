package su.nexmedia.engine.api.data.connection;

import org.jetbrains.annotations.NotNull;
import org.sqlite.JDBC;
import su.nexmedia.engine.NexPlugin;

import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectorSQLite extends AbstractDataConnector {

    public ConnectorSQLite(@NotNull NexPlugin<?> plugin) throws SQLException {
        this(plugin, plugin.getDataFolder().getAbsolutePath() + "/data.db");
    }

    public ConnectorSQLite(@NotNull NexPlugin<?> plugin, @NotNull String filePath) throws SQLException {
        super(plugin, "jdbc:sqlite:" + filePath);
        DriverManager.registerDriver(new JDBC());
    }
}

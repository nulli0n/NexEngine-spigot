package su.nexmedia.engine.api.data.connection;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexPlugin;

import java.sql.SQLException;

public class ConnectorMySQL extends AbstractDataConnector {

    public ConnectorMySQL(@NotNull NexPlugin<?> plugin) throws SQLException {
        this(plugin,
            plugin.getConfigManager().dataMysqlHost, plugin.getConfigManager().dataMysqlBase,
            plugin.getConfigManager().dataMysqlUser, plugin.getConfigManager().dataMysqlPassword
        );
    }

    public ConnectorMySQL(@NotNull NexPlugin<?> plugin,
                          @NotNull String host, @NotNull String base,
                          @NotNull String userName, @NotNull String password) throws SQLException {
        super(plugin, "jdbc:mysql://" + host + "/" + base + "?allowPublicKeyRetrieval=true&useSSL=false", userName, password);
    }
}

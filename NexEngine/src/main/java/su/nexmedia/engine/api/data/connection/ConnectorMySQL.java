package su.nexmedia.engine.api.data.connection;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.api.data.config.DataConfig;

public class ConnectorMySQL extends AbstractDataConnector {

    public ConnectorMySQL(@NotNull NexPlugin<?> plugin, @NotNull DataConfig config) {
        super(plugin, "jdbc:mysql://" + config.mysqlHost + "/" + config.mysqlBase + config.mysqlParameters,
                config.mysqlUser, config.mysqlPassword, config.mysqlSize, config.mysqlIdle,
                config.mysqlLifetime, config.mysqlKeepalive, config.mysqlTimeout);
    }
}
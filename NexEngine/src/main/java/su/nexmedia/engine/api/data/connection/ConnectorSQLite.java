package su.nexmedia.engine.api.data.connection;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.api.data.config.DataConfig;

public class ConnectorSQLite extends AbstractDataConnector {

    public ConnectorSQLite(@NotNull NexPlugin<?> plugin, @NotNull DataConfig config) {
        this(plugin, plugin.getDataFolder().getAbsolutePath() + "/" + config.sqliteFilename);
    }

    public ConnectorSQLite(@NotNull NexPlugin<?> plugin, @NotNull String filePath) {
        super(plugin, "jdbc:sqlite:" + filePath);
    }
}

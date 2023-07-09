package su.nexmedia.engine.utils;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexEngine;

public class EngineUtils {

    public static final NexEngine ENGINE = NexEngine.getPlugin(NexEngine.class);

    public static final String VAULT           = "Vault";
    public static final String PLACEHOLDER_API = "PlaceholderAPI";
    public static final String FLOODGATE       = "floodgate";

    public static boolean hasPlugin(@NotNull String pluginName) {
        Plugin plugin = ENGINE.getPluginManager().getPlugin(pluginName);
        return plugin != null;
    }

    public static boolean hasPlaceholderAPI() {
        return hasPlugin(PLACEHOLDER_API);
    }

    public static boolean hasVault() {
        return hasPlugin(VAULT);
    }

    public static boolean hasFloodgate() {
        return hasPlugin(FLOODGATE);
    }
}

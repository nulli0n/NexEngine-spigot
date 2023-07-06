package su.nexmedia.engine.hooks;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.EngineUtils;

@Deprecated
public class Hooks {

    @Deprecated
    public static boolean hasPlugin(@NotNull String pluginName) {
        return EngineUtils.hasPlugin(pluginName);
    }

    public static boolean hasPlaceholderAPI() {
        return EngineUtils.hasPlaceholderAPI();
    }

    public static boolean hasVault() {
        return EngineUtils.hasVault();
    }

    public static boolean hasFloodgate() {
        return EngineUtils.hasFloodgate();
    }
}

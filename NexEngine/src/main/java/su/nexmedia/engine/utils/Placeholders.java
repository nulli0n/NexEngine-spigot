package su.nexmedia.engine.utils;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexPlugin;

import java.util.function.UnaryOperator;

public class Placeholders {

    public static final String DEFAULT = "default";
    public static final String NONE = "none";
    public static final String MASK_ANY = "*";

    public static class Plugin {

        public static final String NAME           = "%plugin_name%";
        public static final String NAME_LOCALIZED = "%plugin_name_localized%";

        @NotNull
        public static UnaryOperator<String> replacer(@NotNull NexPlugin<?> plugin) {
            return str -> str
                .replace(NAME, plugin.getName())
                .replace(NAME_LOCALIZED, plugin.getConfigManager().pluginName)
                ;
        }
    }

    public static class Player {

        public static final String NAME = "%player_name%";
        public static final String DISPLAY_NAME = "%player_display_name%";

        @NotNull
        public static UnaryOperator<String> replacer(@NotNull org.bukkit.entity.Player player) {
            return str -> str
                .replace(NAME, player.getName())
                .replace(DISPLAY_NAME, player.getDisplayName())
                ;
        }
    }
}

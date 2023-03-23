package su.nexmedia.engine.utils;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexPlugin;

import java.util.function.UnaryOperator;

public class Placeholders {

    public static final String DEFAULT = "default";
    public static final String NONE = "none";
    public static final String WILDCARD = "*";

    public static final Placeholder<org.bukkit.Location> LOCATION = new Location();
    public static final Placeholder<NexPlugin<?>> PLUGIN = (plugin) -> (str -> str
        .replace(Plugin.NAME, plugin.getName())
        .replace(Plugin.NAME_LOCALIZED, plugin.getConfigManager().pluginName));

    public interface Placeholder<U> {
        UnaryOperator<String> replacer(@NotNull U src);
    }

    public static class Plugin {

        public static final String NAME           = "%plugin_name%";
        public static final String NAME_LOCALIZED = "%plugin_name_localized%";

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

        @NotNull
        public static UnaryOperator<String> replacer(@NotNull CommandSender sender) {
            if (sender instanceof org.bukkit.entity.Player player) return replacer(player);
            return str -> str
                .replace(NAME, sender.getName())
                .replace(DISPLAY_NAME, sender.getName())
                ;
        }
    }

    public static class Location implements Placeholder<org.bukkit.Location> {

        public static final String X = "%location_x%";
        public static final String Y = "%location_y%";
        public static final String Z = "%location_z%";
        public static final String WORLD = "%location_world%";

        @Override
        public UnaryOperator<String> replacer(@NotNull org.bukkit.Location src) {
            return str -> str
                .replace(X, NumberUtil.format(src.getX()))
                .replace(Y, NumberUtil.format(src.getY()))
                .replace(Z, NumberUtil.format(src.getZ()))
                .replace(WORLD, LocationUtil.getWorldName(src))
                ;
        }
    }
}

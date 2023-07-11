package su.nexmedia.engine.utils;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.api.placeholder.PlaceholderMap;

import java.util.function.UnaryOperator;

public class Placeholders {

    public static final String DEFAULT  = "default";
    public static final String NONE     = "none";
    public static final String WILDCARD = "*";

    public static final String PLUGIN_NAME           = "%plugin_name%";
    public static final String PLUGIN_NAME_LOCALIZED = "%plugin_name_localized%";

    public static final String PLAYER_NAME         = "%player_name%";
    public static final String PLAYER_DISPLAY_NAME = "%player_display_name%";

    public static final String LOCATION_X     = "%location_x%";
    public static final String LOCATION_Y     = "%location_y%";
    public static final String LOCATION_Z     = "%location_z%";
    public static final String LOCATION_WORLD = "%location_world%";

    public static final String COMMAND_USAGE       = "%command_usage%";
    public static final String COMMAND_DESCRIPTION = "%command_description%";
    public static final String COMMAND_LABEL       = "%command_label%";

    @NotNull
    public static UnaryOperator<String> forLocation(@NotNull Location location) {
        return new PlaceholderMap()
            .add(LOCATION_X, () -> NumberUtil.format(location.getX()))
            .add(LOCATION_Y, () -> NumberUtil.format(location.getY()))
            .add(LOCATION_Z, () -> NumberUtil.format(location.getZ()))
            .add(LOCATION_WORLD, () -> LocationUtil.getWorldName(location))
            .replacer();
    }

    @NotNull
    public static UnaryOperator<String> forPlugin(@NotNull NexPlugin<?> plugin) {
        return new PlaceholderMap()
            .add(PLUGIN_NAME, plugin::getName)
            .add(PLUGIN_NAME_LOCALIZED, () -> plugin.getConfigManager().pluginName)
            .replacer();
    }

    @NotNull
    public static UnaryOperator<String> forPlayer(@NotNull Player player) {
        return new PlaceholderMap()
            .add(PLAYER_NAME, player::getName)
            .add(PLAYER_DISPLAY_NAME, player::getDisplayName)
            .replacer();
    }

    @NotNull
    public static UnaryOperator<String> forSender(@NotNull CommandSender sender) {
        if (sender instanceof Player player) return forPlayer(player);

        return new PlaceholderMap()
            .add(PLAYER_NAME, sender::getName)
            .add(PLAYER_DISPLAY_NAME, sender::getName)
            .replacer();
    }
}

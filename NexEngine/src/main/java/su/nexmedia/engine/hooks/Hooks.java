package su.nexmedia.engine.hooks;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexEngine;
import su.nexmedia.engine.hooks.external.MythicMobsHook;
import su.nexmedia.engine.utils.EntityUtil;
import su.nexmedia.engine.utils.PlayerUtil;

import java.util.Map;
import java.util.Set;

public class Hooks {

    public static final String VAULT           = "Vault";
    @Deprecated public static final String CITIZENS        = "Citizens";
    public static final String PLACEHOLDER_API = "PlaceholderAPI";
    @Deprecated public static final String MYTHIC_MOBS     = "MythicMobs";
    @Deprecated public static final String WORLD_GUARD     = "WorldGuard";
    public static final String FLOODGATE = "floodgate";

    private static final NexEngine ENGINE = NexEngine.get();

    @NotNull
    @Deprecated
    public static String getPermissionGroup(@NotNull Player player) {
        return PlayerUtil.getPermissionGroup(player);
    }

    @NotNull
    @Deprecated
    public static Set<String> getPermissionGroups(@NotNull Player player) {
        return PlayerUtil.getPermissionGroups(player);
    }

    @Deprecated
    public static long getGroupValueLong(@NotNull Player player, @NotNull Map<String, Long> rankMap, boolean isNegaBetter) {
        return PlayerUtil.getGroupValueLong(player, rankMap, isNegaBetter);
    }

    @Deprecated
    public static int getGroupValueInt(@NotNull Player player, @NotNull Map<String, Integer> map, boolean isNegaBetter) {
        return PlayerUtil.getGroupValueInt(player, map, isNegaBetter);
    }

    @Deprecated
    public static double getGroupValueDouble(@NotNull Player player, @NotNull Map<String, Double> map, boolean isNegaBetter) {
        return PlayerUtil.getGroupValueDouble(player, map, isNegaBetter);
    }

    @NotNull
    @Deprecated
    public static String getPrefix(@NotNull Player player) {
        return PlayerUtil.getPrefix(player);
    }

    @NotNull
    @Deprecated
    public static String getSuffix(@NotNull Player player) {
        return PlayerUtil.getSuffix(player);
    }

    @Deprecated
    public static boolean isCitizensNPC(@NotNull Entity entity) {
        return EntityUtil.isNPC(entity);
        //return hasPlugin(CITIZENS) && CitizensAPI.getNPCRegistry().isNPC(entity);
    }

    @Deprecated
    public static boolean isMythicMob(@NotNull Entity entity) {
        return hasMythicMobs() && MythicMobsHook.isMythicMob(entity);
    }

    public static boolean hasPlugin(@NotNull String pluginName) {
        Plugin plugin = ENGINE.getPluginManager().getPlugin(pluginName);
        return plugin != null;// && p.isEnabled();
    }

    public static boolean hasPlaceholderAPI() {
        return hasPlugin(PLACEHOLDER_API);
    }

    public static boolean hasVault() {
        return hasPlugin(VAULT);
    }

    @Deprecated
    public static boolean hasCitizens() {
        return hasPlugin(CITIZENS);
    }

    @Deprecated
    public static boolean hasMythicMobs() {
        return hasPlugin(MYTHIC_MOBS);
    }

    @Deprecated
    public static boolean hasWorldGuard() {
        return hasPlugin(WORLD_GUARD);
    }

    public static boolean hasFloodgate() {
        return hasPlugin(FLOODGATE);
    }
}

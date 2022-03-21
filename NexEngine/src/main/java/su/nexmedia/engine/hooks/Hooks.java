package su.nexmedia.engine.hooks;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexEngine;
import su.nexmedia.engine.hooks.external.MythicMobsHook;
import su.nexmedia.engine.hooks.external.VaultHook;
import su.nexmedia.engine.hooks.external.WorldGuardHook;
import su.nexmedia.engine.utils.Constants;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class Hooks {

    public static final String VAULT           = "Vault";
    public static final String CITIZENS        = "Citizens";
    public static final String PLACEHOLDER_API = "PlaceholderAPI";
    public static final String MYTHIC_MOBS     = "MythicMobs";
    public static final String WORLD_GUARD     = "WorldGuard";

    private static final NexEngine ENGINE = NexEngine.get();

    @NotNull
    @Deprecated
    public static String getPermissionGroup(@NotNull Player player) {
        VaultHook vault = ENGINE.getVault();
        return vault != null ? vault.getPlayerGroup(player).toLowerCase() : "";
    }

    @NotNull
    @Deprecated
    public static Set<String> getPermissionGroups(@NotNull Player player) {
        VaultHook vault = ENGINE.getVault();
        return vault != null ? vault.getPlayerGroups(player) : Collections.emptySet();
    }

    public static long getGroupValueLong(@NotNull Player player, @NotNull Map<String, Long> rankMap, boolean isNegaBetter) {
        Map<String, Double> map2 = rankMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, v -> (double) v.getValue()));
        return (long) getGroupValueDouble(player, map2, isNegaBetter);
    }

    public static int getGroupValueInt(@NotNull Player player, @NotNull Map<String, Integer> map, boolean isNegaBetter) {
        Map<String, Double> map2 = map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, v -> (double) v.getValue()));
        return (int) getGroupValueDouble(player, map2, isNegaBetter);
    }

    public static double getGroupValueDouble(@NotNull Player player, @NotNull Map<String, Double> map, boolean isNegaBetter) {
        Set<String> groups = VaultHook.getPermissionGroups(player);
        // System.out.println("[0] groups of '" + player.getName() + "': " + groups);
        // System.out.println("[1] map to compare: " + map);

        Optional<Map.Entry<String, Double>> opt = map.entrySet().stream().filter(entry -> entry.getKey().equalsIgnoreCase(Constants.DEFAULT) || groups.contains(entry.getKey())).min((entry1, entry2) -> {
            double val1 = entry1.getValue();
            double val2 = entry2.getValue();
            if (isNegaBetter && val2 < 0) return 1;
            if (isNegaBetter && val1 < 0) return -1;
            return (int) (val2 - val1);
        });

        // System.out.println("[2] max value for '" + player.getName() + "': " +
        // (opt.isPresent() ? opt.get() : "-1x"));

        return opt.isPresent() ? opt.get().getValue() : -1D;
    }

    @NotNull
    @Deprecated
    public static String getPrefix(@NotNull Player player) {
        VaultHook vault = ENGINE.getVault();
        return vault != null ? vault.getPrefix(player) : "";
    }

    @NotNull
    @Deprecated
    public static String getSuffix(@NotNull Player player) {
        VaultHook vault = ENGINE.getVault();
        return vault != null ? vault.getSuffix(player) : "";
    }

    public static boolean isCitizensNPC(@NotNull Entity entity) {
        return hasPlugin(CITIZENS) && CitizensAPI.getNPCRegistry().isNPC(entity);
    }

    @Deprecated
    public static boolean isMythicMob(@NotNull Entity entity) {
        MythicMobsHook mobsHK = ENGINE.getMythicMobs();
        return mobsHK != null && mobsHK.isMythicMob(entity);
    }

    public static boolean hasPlugin(@NotNull String pluginName) {
        Plugin plugin = ENGINE.getPluginManager().getPlugin(pluginName);
        return plugin != null;// && p.isEnabled();
    }

    public static boolean hasPlaceholderAPI() {
        return hasPlugin(PLACEHOLDER_API);
    }

    public static boolean hasCitizens() {
        return hasPlugin(CITIZENS);
    }

    public static boolean hasMythicMobs() {
        return hasPlugin(MYTHIC_MOBS);
    }

    public static boolean hasWorldGuard() {
        return hasPlugin(WORLD_GUARD);
    }

    @Deprecated
    public static boolean canFights(@NotNull Entity attacker, @NotNull Entity victim) {
        if (attacker.equals(victim)) return false;
        if (victim.isInvulnerable() || !(victim instanceof LivingEntity)) return false;

        if (isCitizensNPC(victim)) {
            if (!hasPlugin("Sentinel")) {
                return false;
            }

            NPC npc = CitizensAPI.getNPCRegistry().getNPC(victim);
            /*if (!npc.hasTrait(SentinelTrait.class)) {
                return false;
            }*/
        }

        WorldGuardHook worldGuard = ENGINE.getWorldGuard();
        if (worldGuard != null) {
            if (!worldGuard.canFights(attacker, victim)) {
                return false;
            }
        }

        return true;
    }
}

package su.nexmedia.engine.hooks.external;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

public class WorldGuardHook /*extends AbstractHook<NexEngine>*/ {

    static WorldGuard worldGuard = WorldGuard.getInstance();

    /*public WorldGuardHook(@NotNull NexEngine plugin, @NotNull String pluginName) {
        super(plugin, pluginName);
    }

    @Override
    public boolean setup() {
        //worldGuard = WorldGuard.getInstance();
        return true;
    }

    @Override
    public void shutdown() {
        worldGuard = null;
    }

    public static boolean isEnabled() {
        return worldGuard != null;
    }*/

    public static boolean canFights(@NotNull Entity damager, @NotNull Entity victim) {
        return WorldGuardPlugin.inst().createProtectionQuery().testEntityDamage(damager, victim);
    }

    public static boolean isInRegion(@NotNull Entity entity, @NotNull String region) {
        return getRegion(entity).equalsIgnoreCase(region);
    }

    @NotNull
    public static String getRegion(@NotNull Entity entity) {
        return getRegion(entity.getLocation());
    }

    @NotNull
    public static String getRegion(@NotNull Location loc) {
        ProtectedRegion region = getProtectedRegion(loc);
        return region == null ? "" : region.getId();
    }

    @Nullable
    public static ProtectedRegion getProtectedRegion(@NotNull Entity entity) {
        return getProtectedRegion(entity.getLocation());
    }

    @Nullable
    public static ProtectedRegion getProtectedRegion(@NotNull Location location) {
        World world = location.getWorld();
        if (world == null) return null;

        com.sk89q.worldedit.world.World sworld = BukkitAdapter.adapt(world);
        BlockVector3 vector3 = BukkitAdapter.adapt(location).toVector().toBlockPoint();
        RegionManager regionManager = worldGuard.getPlatform().getRegionContainer().get(sworld);
        if (regionManager == null) return null;

        ApplicableRegionSet set = regionManager.getApplicableRegions(vector3);
        return set.getRegions().stream().max(Comparator.comparingInt(ProtectedRegion::getPriority)).orElse(null);
    }

    @NotNull
    public static Collection<ProtectedRegion> getProtectedRegions(@NotNull World w) {
        com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(w);
        RegionManager regionManager = worldGuard.getPlatform().getRegionContainer().get(world);

        return regionManager == null ? Collections.emptySet() : regionManager.getRegions().values();
    }
}

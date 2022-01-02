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
import su.nexmedia.engine.NexEngine;
import su.nexmedia.engine.api.hook.AbstractHook;

import java.util.Collection;
import java.util.Optional;

public class WorldGuardHook extends AbstractHook<NexEngine> {

    private WorldGuard worldGuard;

    public WorldGuardHook(@NotNull NexEngine plugin, @NotNull String pluginName) {
        super(plugin, pluginName);
    }

    @Override
    public boolean setup() {
        this.worldGuard = WorldGuard.getInstance();
        return true;
    }

    @Override
    public void shutdown() {

    }

    public boolean canFights(@NotNull Entity damager, @NotNull Entity victim) {
        return WorldGuardPlugin.inst().createProtectionQuery().testEntityDamage(damager, victim);
    }

    public boolean isInRegion(@NotNull Entity entity, @NotNull String region) {
        return this.getRegion(entity).equalsIgnoreCase(region);
    }

    @NotNull
    public String getRegion(@NotNull Entity entity) {
        return this.getRegion(entity.getLocation());
    }

    @NotNull
    public String getRegion(@NotNull Location loc) {
        ProtectedRegion region = this.getProtectedRegion(loc);
        return region == null ? "" : region.getId();
    }

    @Nullable
    public ProtectedRegion getProtectedRegion(@NotNull Entity entity) {
        return this.getProtectedRegion(entity.getLocation());
    }

    @Nullable
    public ProtectedRegion getProtectedRegion(@NotNull Location loc) {
        com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(loc.getWorld());
        BlockVector3 vector3 = BukkitAdapter.adapt(loc).toVector().toBlockPoint();
        RegionManager regionManager = worldGuard.getPlatform().getRegionContainer().get(world);

        ApplicableRegionSet set = regionManager.getApplicableRegions(vector3);
        Optional<ProtectedRegion> best = set.getRegions().stream().sorted((reg1, reg2) -> {
            return reg2.getPriority() - reg1.getPriority();
        }).findFirst();
        return best.isPresent() ? best.get() : null;
    }

    @NotNull
    public Collection<ProtectedRegion> getProtectedRegions(@NotNull World w) {
        com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(w);
        RegionManager regionManager = worldGuard.getPlatform().getRegionContainer().get(world);

        return regionManager.getRegions().values();
    }
}

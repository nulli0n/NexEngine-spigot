package su.nexmedia.engine.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class LocationUtil {

    @Nullable
    public static String serialize(@NotNull Location loc) {
        World world = loc.getWorld();
        if (world == null) return null;

        return loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getPitch() + "," + loc.getYaw() + "," + world.getName();
    }

    @NotNull
    public static List<String> serialize(@NotNull Collection<Location> list) {
        return new ArrayList<>(list.stream().map(LocationUtil::serialize).filter(Objects::nonNull).toList());
    }

    @Nullable
    public static Location deserialize(@NotNull String raw) {
        String[] split = raw.split(",");
        if (split.length != 6) return null;

        World world = Bukkit.getWorld(split[5]);
        if (world == null) {
            EngineUtils.ENGINE.error("Invalid/Unloaded world for: '" + raw + "' location!");
            return null;
        }

        double x = StringUtil.getDouble(split[0], 0, true);
        double y = StringUtil.getDouble(split[1], 0, true);
        double z = StringUtil.getDouble(split[2], 0, true);
        float pitch = (float) StringUtil.getDouble(split[3], 0, true);
        float yaw = (float) StringUtil.getDouble(split[4], 0, true);

        return new Location(world, x, y, z, yaw, pitch);
    }

    @NotNull
    public static List<Location> deserialize(@NotNull Collection<String> list) {
        return new ArrayList<>(list.stream().map(LocationUtil::deserialize).filter(Objects::nonNull).toList());
    }

    @NotNull
    public static String getWorldName(@NotNull Location location) {
        World world = location.getWorld();
        return world == null ? "null" : world.getName();
    }

    @NotNull
    public static Location getCenter(@NotNull Location location) {
        return getCenter(location, true);
    }

    @NotNull
    public static Location getCenter(@NotNull Location location, boolean doVertical) {
        Location centered = location.clone();
        location.setX(location.getBlockX() + 0.5);
        location.setY(location.getBlockY() + (doVertical ? 0.5 : 0));
        location.setZ(location.getBlockZ() + 0.5);
        return location;
    }

    @NotNull
    public static Vector getDirection(@NotNull Location from, @NotNull Location to) {
        Location origin = from.clone();
        origin.setDirection(to.toVector().subtract(origin.toVector()));
        return origin.getDirection();
    }
}

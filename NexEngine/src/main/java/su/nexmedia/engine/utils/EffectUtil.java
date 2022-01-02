package su.nexmedia.engine.utils;

import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.utils.random.Rnd;

public class EffectUtil {

    @Deprecated
    public static void playEffect(@NotNull Location location, @NotNull String particle,
                                  double xOffset, double yOffset, double zOffset, double speed, int amount) {
        String[] nameSplit = particle.split(":");
        String particleName = nameSplit[0];
        String particleData = nameSplit.length >= 2 ? nameSplit[1].toUpperCase() : "";
        playEffect(location, particleName, particleData, xOffset, yOffset, zOffset, speed, amount);
    }

    @Nullable
    private static Object getParticleData(@NotNull Particle particle, @NotNull String dataRaw) {
        if (particle == Particle.REDSTONE) {
            Color color = Color.WHITE;
            if (!dataRaw.isEmpty()) {
                String[] pColor = dataRaw.split(",");
                int r = StringUtil.getInteger(pColor[0], Rnd.get(255));
                int g = pColor.length >= 2 ? StringUtil.getInteger(pColor[1], Rnd.get(255)) : 0;
                int b = pColor.length >= 3 ? StringUtil.getInteger(pColor[2], Rnd.get(255)) : 0;
                color = Color.fromRGB(r, g, b);
            }
            return new Particle.DustOptions(color, 1.5f);
        }
        else if (particle == Particle.BLOCK_CRACK || particle == Particle.FALLING_DUST || particle == Particle.BLOCK_DUST) {
            Material material = !dataRaw.isEmpty() ? Material.getMaterial(dataRaw) : Material.STONE;
            return material != null ? material.createBlockData() : Material.STONE.createBlockData();
        }
        else if (particle == Particle.ITEM_CRACK) {
            Material material = !dataRaw.isEmpty() ? Material.getMaterial(dataRaw) : Material.STONE;
            return material != null ? new ItemStack(material) : new ItemStack(Material.STONE);
        }
        return null;
    }

    public static void playEffect(@NotNull Location location, @NotNull String nameRaw, @NotNull String dataRaw,
                                  double xOffset, double yOffset, double zOffset, double speed, int amount) {

        World world = location.getWorld();
        if (world == null) return;

        Particle particle = CollectionsUtil.getEnum(nameRaw, Particle.class);
        if (particle == null || particle.name().equalsIgnoreCase("VIBRATION")) return;

        Object data = getParticleData(particle, dataRaw);
        if (data == null) {
            world.spawnParticle(particle, location, amount, xOffset, yOffset, zOffset, speed);
        }
        else {
            world.spawnParticle(particle, location, amount, xOffset, yOffset, zOffset, speed, data);
        }
    }

    public static void playEffect(@NotNull Player player, @NotNull Location location, @NotNull String nameRaw, @NotNull String dataRaw,
                                  double xOffset, double yOffset, double zOffset, double speed, int amount) {

        Particle particle = CollectionsUtil.getEnum(nameRaw, Particle.class);
        if (particle == null || particle == Particle.VIBRATION) return;

        Object data = getParticleData(particle, dataRaw);
        if (data == null) {
            player.spawnParticle(particle, location, amount, xOffset, yOffset, zOffset, speed);
        }
        else {
            player.spawnParticle(particle, location, amount, xOffset, yOffset, zOffset, speed, data);
        }
    }

    @Deprecated
    public static void playEffect(@NotNull Player player, @NotNull Location loc, @NotNull String eff, double x, double y, double z, double speed, int amount) {

        String[] nameSplit = eff.split(":");
        String particleName = nameSplit[0];
        String particleData = nameSplit.length >= 2 ? nameSplit[1].toUpperCase() : null;

        Particle particle = CollectionsUtil.getEnum(particleName, Particle.class);
        if (particle == null)
            return;

        if (particle == Particle.REDSTONE || particle == Particle.FALLING_DUST) {
            Color color = Color.WHITE;
            if (particleData != null) {
                String[] pColor = particleData.split(",");
                int r = StringUtil.getInteger(pColor[0], Rnd.get(255));
                int g = pColor.length >= 2 ? StringUtil.getInteger(pColor[1], Rnd.get(255)) : 0;
                int b = pColor.length >= 3 ? StringUtil.getInteger(pColor[2], Rnd.get(255)) : 0;
                color = Color.fromRGB(r, g, b);
            }

            Object data = new Particle.DustOptions(color, 1.5f);
            player.spawnParticle(particle, loc, amount, x, y, z, speed, data);
            return;
        }

        if (particle == Particle.BLOCK_CRACK || particle == Particle.LEGACY_BLOCK_CRACK) {
            Material m = particleData != null ? Material.getMaterial(particleData) : Material.STONE;
            BlockData blockData = m != null ? m.createBlockData() : Material.STONE.createBlockData();
            player.spawnParticle(particle, loc, amount, x, y, z, speed, blockData);
            return;
        }

        if (particle == Particle.ITEM_CRACK) {
            Material m = particleData != null ? Material.getMaterial(particleData) : Material.STONE;
            ItemStack item = m != null ? new ItemStack(m) : new ItemStack(Material.STONE);
            player.spawnParticle(particle, loc, amount, x, y, z, speed, item);
            return;
        }

        player.spawnParticle(particle, loc, amount, x, y, z, speed);
    }

    @Deprecated
    public static void drawLine(Location from, Location to, String pe,
                                float offX, float offY, float offZ, float speed, int amount) {
        Location origin = from.clone();
        Vector target = new Location(to.getWorld(), to.getX(), to.getY(), to.getZ()).toVector();
        origin.setDirection(target.subtract(origin.toVector()));
        Vector increase = origin.getDirection();

        for (int counter = 0; counter < from.distance(to); counter++) {
            Location loc = origin.add(increase);
            EffectUtil.playEffect(loc, pe, offX, offY, offZ, speed, amount);
        }
    }

    public static void drawLine(@NotNull Location from, @NotNull Location to,
                                @NotNull String nameRaw, @NotNull String dataRaw,
                                float offX, float offY, float offZ, float speed, int amount) {
        Location origin = from.clone();
        Vector target = new Location(to.getWorld(), to.getX(), to.getY(), to.getZ()).toVector();
        origin.setDirection(target.subtract(origin.toVector()));
        Vector increase = origin.getDirection();

        for (int counter = 0; counter < from.distance(to); counter++) {
            Location location = origin.add(increase);
            EffectUtil.playEffect(location, nameRaw, dataRaw, offX, offY, offZ, speed, amount);
        }
    }
}

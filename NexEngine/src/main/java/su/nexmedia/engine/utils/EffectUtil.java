package su.nexmedia.engine.utils;

import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.Version;
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
        if (particle == Particle.REDSTONE || particle == Particle.SPELL_MOB || particle == Particle.SPELL_MOB_AMBIENT) {
            Color color = dataRaw.isEmpty() ? Color.WHITE : StringUtil.parseColor(dataRaw);
            return new Particle.DustOptions(color, 1.5f);
        }
        else if (Version.CURRENT.isHigher(Version.V1_16_R3) && particle == Particle.DUST_COLOR_TRANSITION) {
            String[] colors = dataRaw.split(":");
            Color colorStart = dataRaw.isEmpty() ? Color.WHITE : StringUtil.parseColor(colors[0]);
            Color colorEnd = dataRaw.isEmpty() || colors.length < 2 ? Color.WHITE : StringUtil.parseColor(colors[1]);
            return new Particle.DustTransition(colorStart, colorEnd, 1.5f);
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

        playEffect(world, location, particle, dataRaw, xOffset, yOffset, zOffset, speed, amount);
    }

    public static void playEffect(@NotNull Player player, @NotNull Location location, @NotNull String nameRaw, @NotNull String dataRaw,
                                  double xOffset, double yOffset, double zOffset, double speed, int amount) {

        Particle particle = CollectionsUtil.getEnum(nameRaw, Particle.class);
        if (particle == null || particle.name().equalsIgnoreCase("VIBRATION")) return;

        playEffect(player, location, particle, dataRaw, xOffset, yOffset, zOffset, speed, amount);
    }

    public static void playEffect(@NotNull Player player, @NotNull Location location, @NotNull Particle particle, @NotNull String dataRaw,
                                  double xOffset, double yOffset, double zOffset, double speed, int amount) {

        Object data = getParticleData(particle, dataRaw);
        if ((particle == Particle.SPELL_MOB || particle == Particle.SPELL_MOB_AMBIENT) && data instanceof Particle.DustOptions dustOptions) {
            Color color = dustOptions.getColor();
            data = null;

            amount = 0;
            speed = 1D;
            xOffset = color.getRed() / 255D;
            yOffset = color.getGreen() / 255D;
            zOffset = color.getBlue() / 255D;
        }
        else if (particle == Particle.NOTE) {
            amount = 0;
            speed = 1D;
            xOffset = StringUtil.getInteger(dataRaw, 0) / 24D;
            yOffset = 0D;
            zOffset = 0D;
        }
        player.spawnParticle(particle, location, amount, xOffset, yOffset, zOffset, speed, data);
    }

    @Deprecated
    public static void playEffect(@NotNull World world, @NotNull Location location, @NotNull Particle particle, @NotNull String dataRaw,
                                  double xOffset, double yOffset, double zOffset, double speed, int amount) {

        playEffect(location, particle, dataRaw, xOffset, yOffset, zOffset, speed, amount);
    }

    public static void playEffect(@NotNull Location location, @NotNull Particle particle, @NotNull String dataRaw,
                                  double xOffset, double yOffset, double zOffset, double speed, int amount) {

        World world = location.getWorld();
        if (world == null) return;

        Object data = getParticleData(particle, dataRaw);
        if ((particle == Particle.SPELL_MOB || particle == Particle.SPELL_MOB_AMBIENT) && data instanceof Particle.DustOptions dustOptions) {
            Color color = dustOptions.getColor();
            data = null;

            amount = 0;
            speed = 1D;
            xOffset = color.getRed() / 255D;
            yOffset = color.getGreen() / 255D;
            zOffset = color.getBlue() / 255D;
        }
        else if (particle == Particle.NOTE) {
            amount = 0;
            speed = 1D;
            xOffset = StringUtil.getInteger(dataRaw, 0) / 24D;
            yOffset = 0D;
            zOffset = 0D;
        }
        world.spawnParticle(particle, location, amount, xOffset, yOffset, zOffset, speed, data);
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
        Particle particle = CollectionsUtil.getEnum(nameRaw, Particle.class);
        if (particle == null || particle.name().equalsIgnoreCase("VIBRATION")) return;

        drawLine(from, to, particle, dataRaw, offX, offY, offZ, speed, amount);
    }

    public static void drawLine(@NotNull Location from, @NotNull Location to,
                                @NotNull Particle particle, @NotNull String dataRaw,
                                float offX, float offY, float offZ, float speed, int amount) {
        Location origin = from.clone();
        Vector target = new Location(to.getWorld(), to.getX(), to.getY(), to.getZ()).toVector();
        origin.setDirection(target.subtract(origin.toVector()));
        Vector increase = origin.getDirection();

        for (int counter = 0; counter < from.distance(to); counter++) {
            Location location = origin.add(increase);
            EffectUtil.playEffect(location, particle, dataRaw, offX, offY, offZ, speed, amount);
        }
    }
}

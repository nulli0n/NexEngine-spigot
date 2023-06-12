package su.nexmedia.engine.api.particle;

import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.utils.StringUtil;

public class SimpleParticle {

    private final Particle particle;
    private final Object data;

    public SimpleParticle(@NotNull Particle particle, @Nullable Object data) {
        this.particle = particle;
        this.data = data;
    }

    @NotNull
    public static SimpleParticle of(@NotNull Particle particle) {
        return SimpleParticle.of(particle, null);
    }

    @NotNull
    public static SimpleParticle of(@NotNull Particle particle, @Nullable Object data) {
        return new SimpleParticle(particle, data);
    }

    @NotNull
    public static SimpleParticle itemCrack(@NotNull Material material) {
        return new SimpleParticle(Particle.ITEM_CRACK, new ItemStack(material));
    }

    @NotNull
    public static SimpleParticle blockCrack(@NotNull Material material) {
        return new SimpleParticle(Particle.BLOCK_CRACK, material.createBlockData());
    }

    @NotNull
    public static SimpleParticle blockDust(@NotNull Material material) {
        return new SimpleParticle(Particle.BLOCK_DUST, material.createBlockData());
    }

    @NotNull
    public static SimpleParticle blockMarker(@NotNull Material material) {
        return new SimpleParticle(Particle.BLOCK_MARKER, material.createBlockData());
    }

    @NotNull
    public static SimpleParticle fallingDust(@NotNull Material material) {
        return new SimpleParticle(Particle.FALLING_DUST, material.createBlockData());
    }

    @NotNull
    public static SimpleParticle redstone(@NotNull Color color, float size) {
        return new SimpleParticle(Particle.REDSTONE, new Particle.DustOptions(color, size));
    }

    @NotNull
    public static SimpleParticle read(@NotNull JYML cfg, @NotNull String path) {
        String name = cfg.getString(path + ".Name", "");
        Particle particle = StringUtil.getEnum(name, Particle.class).orElse(Particle.REDSTONE);

        Class<?> dataType = particle.getDataType();
        Object data = null;
        if (dataType == BlockData.class) {
            Material material = Material.getMaterial(cfg.getString(path + ".Material", ""));
            data = material != null ? material.createBlockData() : Material.STONE.createBlockData();
        }
        else if (dataType == Particle.DustOptions.class) {
            Color color = StringUtil.parseColor(cfg.getString(path + ".Color", ""));
            double size = cfg.getDouble(path + ".Size", 1D);
            data = new Particle.DustOptions(color, (float) size);
        }
        else if (dataType == Particle.DustTransition.class) {
            Color colorStart = StringUtil.parseColor(cfg.getString(path + ".Color_From", ""));
            Color colorEnd = StringUtil.parseColor(cfg.getString(path + ".Color_To", ""));
            double size = cfg.getDouble(path + ".Size", 1D);
            data = new Particle.DustTransition(colorStart, colorEnd, 1.0f);
        }
        else if (dataType == ItemStack.class) {
            ItemStack item = cfg.getItem(path + ".Item");
            data = item.getType().isAir() ? new ItemStack(Material.STONE) : item;
        }
        else if (dataType != Void.class) return SimpleParticle.of(Particle.REDSTONE);

        return SimpleParticle.of(particle, data);
    }

    public static void write(@NotNull SimpleParticle particle, @NotNull JYML cfg, @NotNull String path) {
        cfg.set(path + ".Name", particle.getParticle().name());

        Object data = particle.getData();
        if (data instanceof BlockData blockData) {
            cfg.set(path + ".Material", blockData.getMaterial().name());
        }
        else if (data instanceof Particle.DustTransition dustTransition) {
            Color colorStart = dustTransition.getColor();
            Color colorEnd = dustTransition.getToColor();
            cfg.set(path + ".Color_From", colorStart.getRed() + "," + colorStart.getGreen() + "," + colorStart.getBlue());
            cfg.set(path + ".Color_To", colorEnd.getRed() + "," + colorEnd.getGreen() + "," + colorEnd.getBlue());
            cfg.set(path + ".Size", dustTransition.getSize());
        }
        else if (data instanceof Particle.DustOptions dustOptions) {
            Color color = dustOptions.getColor();
            cfg.set(path + ".Color", color.getRed() + "," + color.getGreen() + "," + color.getBlue());
            cfg.set(path + ".Size", dustOptions.getSize());
        }
        else if (data instanceof ItemStack item) {
            cfg.setItem(path + ".Item", item);
        }
    }

    @NotNull
    public Particle getParticle() {
        return particle;
    }

    @Nullable
    public Object getData() {
        return data;
    }

    @NotNull
    public SimpleParticle parseData(@NotNull String from) {
        String[] split = from.split(" ");
        Class<?> dataType = this.getParticle().getDataType();
        Object data = null;
        if (dataType == BlockData.class) {
            Material material = Material.getMaterial(from.toUpperCase());
            data = material != null ? material.createBlockData() : Material.STONE.createBlockData();
        }
        else if (dataType == Particle.DustOptions.class) {
            Color color = StringUtil.parseColor(split[0]);
            double size = split.length >= 2 ? StringUtil.getDouble(split[1], 1D) : 1D;
            data = new Particle.DustOptions(color, (float) size);
        }
        else if (dataType == Particle.DustTransition.class) {
            Color colorStart = StringUtil.parseColor(split[0]);
            Color colorEnd = split.length >= 2 ? StringUtil.parseColor(split[1]) : colorStart;
            double size = split.length >= 3 ? StringUtil.getDouble(split[2], 1D) : 1D;
            data = new Particle.DustTransition(colorStart, colorEnd, 1.0f);
        }
        else if (dataType == ItemStack.class) {
            Material material = Material.getMaterial(from.toUpperCase());
            if (material != null && !material.isAir()) data = new ItemStack(material);
            else data = new ItemStack(Material.STONE);
        }
        else if (dataType != Void.class) return SimpleParticle.of(Particle.REDSTONE);

        return SimpleParticle.of(this.getParticle(), data);
    }

    public void play(@NotNull Location location, double speed, int amount) {
        this.play(location, 0D, speed, amount);
    }

    public void play(@NotNull Location location, double offsetAll, double speed, int amount) {
        this.play(location, offsetAll, offsetAll, offsetAll, speed, amount);
    }

    public void play(@NotNull Location location, double xOffset, double yOffset, double zOffset, double speed, int amount) {
        this.play(null, location, xOffset, yOffset, zOffset, speed, amount);
    }

    public void play(@NotNull Player player, @NotNull Location location, double speed, int amount) {
        this.play(player, location, 0D, speed, amount);
    }

    public void play(@NotNull Player player, @NotNull Location location, double offsetAll, double speed, int amount) {
        this.play(player, location, offsetAll, offsetAll, offsetAll, speed, amount);
    }

    public void play(@Nullable Player player, @NotNull Location location, double xOffset, double yOffset, double zOffset, double speed, int amount) {
        if (player == null) {
            World world = location.getWorld();
            if (world == null) return;

            world.spawnParticle(this.getParticle(), location, amount, xOffset, yOffset, zOffset, speed, this.getData());
            //EffectUtil.playParticle(location, this.getParticle(), this.getData(), xOffset, yOffset, zOffset, speed, amount);
        }
        else {
            player.spawnParticle(this.getParticle(), location, amount, xOffset, yOffset, zOffset, speed, this.getData());
            //EffectUtil.playParticle(player, location, this.getParticle(), this.getData(), xOffset, yOffset, zOffset, speed, amount);
        }
    }
}

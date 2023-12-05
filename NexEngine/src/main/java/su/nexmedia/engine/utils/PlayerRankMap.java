package su.nexmedia.engine.utils;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.config.JOption;
import su.nexmedia.engine.api.config.JYML;

import java.util.*;

public class PlayerRankMap<T extends Number> {

    private final Mode mode;
    private final String permissionPrefix;
    private final Map<String, T> values;
    private final boolean negativeBetter;

    public enum Mode {
        RANK, PERMISSION
    }

    @Deprecated
    public PlayerRankMap(@NotNull Map<String, T> values) {
        this(Mode.RANK, "", values);
    }

    public PlayerRankMap(@NotNull Mode mode, @NotNull String permissionPrefix, @NotNull Map<String, T> values) {
        this.mode = mode;
        this.permissionPrefix = permissionPrefix;
        this.negativeBetter = true;
        this.values = new HashMap<>(values);
    }

    @NotNull
    public static PlayerRankMap<Integer> readInt(@NotNull JYML cfg, @NotNull String path) {
        return read(cfg, path, Integer.class);
    }

    @NotNull
    public static PlayerRankMap<Double> readDouble(@NotNull JYML cfg, @NotNull String path) {
        return read(cfg, path, Double.class);
    }

    @NotNull
    public static PlayerRankMap<Long> readLong(@NotNull JYML cfg, @NotNull String path) {
        return read(cfg, path, Long.class);
    }

    @NotNull
    public static <T extends Number> PlayerRankMap<T> read(@NotNull JYML cfg, @NotNull String path, @NotNull Class<T> clazz) {
        if (!cfg.contains(path + ".Mode")) {
            for (String rank : cfg.getSection(path)) {
                T number;
                if (clazz == Double.class) {
                    number = clazz.cast(cfg.getDouble(path + "." + rank));
                }
                else number = clazz.cast(cfg.getInt(path + "." + rank));

                cfg.set(path + ".Values." + rank, number);
            }
            cfg.remove(path);
        }

        Mode mode = JOption.create(path + ".Mode", Mode.class, Mode.RANK).read(cfg);
        String permissionPrefix = JOption.create(path + ".Permission_Prefix", "sample.prefix.").read(cfg);

        Map<String, T> values = new HashMap<>();
        for (String rank : cfg.getSection(path + ".Values")) {
            T number;
            if (clazz == Double.class) {
                number = clazz.cast(cfg.getDouble(path + ".Values." + rank));
            }
            else number = clazz.cast(cfg.getInt(path + ".Values." + rank));

            values.put(rank.toLowerCase(), number);
        }

        return new PlayerRankMap<>(mode, permissionPrefix, values);
    }

    public void write(@NotNull JYML cfg, @NotNull String path) {
        cfg.set(path + ".Mode", this.getMode().name());
        cfg.set(path + ".Permission_Prefix", this.getPermissionPrefix());
        this.values.forEach((rank, number) -> {
            cfg.set(path + ".Values." + rank, number);
        });
    }

    @NotNull
    public T getBestValue(@NotNull Player player, @NotNull T def) {
        if (this.getMode() == Mode.RANK) {
            Set<String> groups = PlayerUtil.getPermissionGroups(player);
            Optional<Map.Entry<String, T>> opt = this.values.entrySet().stream().filter(entry -> entry.getKey().equalsIgnoreCase(Placeholders.DEFAULT) || groups.contains(entry.getKey())).min((entry1, entry2) -> {
                T val1 = entry1.getValue();
                T val2 = entry2.getValue();
                if (this.isNegativeBetter() && val2.doubleValue() < 0) return 1;
                if (this.isNegativeBetter() && val1.doubleValue() < 0) return -1;

                return Double.compare(val2.doubleValue(), val1.doubleValue());
            });
            return opt.map(Map.Entry::getValue).orElse(def);
        }
        else {
            return this.values.entrySet().stream()
                .filter(entry -> player.hasPermission(this.getPermissionPrefix() + entry.getKey()))
                .max(Comparator.comparingDouble(e -> e.getValue().doubleValue())).map(Map.Entry::getValue).orElse(def);
        }
    }

    @NotNull
    public T getLowestValue(@NotNull Player player, @NotNull T def) {
        if (this.getMode() == Mode.RANK) {
            Set<String> groups = PlayerUtil.getPermissionGroups(player);
            Optional<Map.Entry<String, T>> opt = this.values.entrySet().stream().filter(entry -> entry.getKey().equalsIgnoreCase(Placeholders.DEFAULT) || groups.contains(entry.getKey())).min((entry1, entry2) -> {
                T val1 = entry1.getValue();
                T val2 = entry2.getValue();
                if (this.isNegativeBetter() && val2.doubleValue() < 0) return 1;
                if (this.isNegativeBetter() && val1.doubleValue() < 0) return -1;

                return Double.compare(val1.doubleValue(), val2.doubleValue());
            });
            return opt.map(Map.Entry::getValue).orElse(def);
        }
        else {
            return this.values.entrySet().stream()
                .filter(entry -> player.hasPermission(this.getPermissionPrefix() + entry.getKey()))
                .min(Comparator.comparingDouble(e -> e.getValue().doubleValue())).map(Map.Entry::getValue).orElse(def);
        }
    }

    @NotNull
    public Mode getMode() {
        return mode;
    }

    public boolean isNegativeBetter() {
        return negativeBetter;
    }

    @Deprecated
    public boolean isCheckAsPermission() {
        return this.getMode() == Mode.PERMISSION;
    }

    @Nullable
    public String getPermissionPrefix() {
        return permissionPrefix;
    }

    @NotNull
    @Deprecated
    public PlayerRankMap<T> setNegativeBetter(boolean negativeBetter) {
        //this.negativeBetter = negativeBetter;
        return this;
    }

    @NotNull
    @Deprecated
    public PlayerRankMap<T> setCheckAsPermission(@NotNull String permissionPrefix) {
        //this.checkAsPermission = true;
        //this.permissionPrefix = permissionPrefix;
        return this;
    }
}

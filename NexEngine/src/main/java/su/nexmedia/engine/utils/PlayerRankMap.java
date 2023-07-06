package su.nexmedia.engine.utils;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.config.JYML;

import java.util.*;

public class PlayerRankMap<T extends Number> {

    private final Map<String, T> values;

    private boolean negativeBetter;
    private boolean checkAsPermission;
    private String permissionPrefix;

    public PlayerRankMap(@NotNull Map<String, T> values) {
        this.values = new HashMap<>(values);
    }

    @NotNull
    public static <T extends Number> PlayerRankMap<T> read(@NotNull JYML cfg, @NotNull String path, @NotNull Class<T> clazz) {
        Map<String, T> values = new HashMap<>();
        for (String rank : cfg.getSection(path)) {
            T number = clazz.cast(cfg.getDouble(path + "." + rank));
            values.put(rank.toLowerCase(), number);
        }
        return new PlayerRankMap<>(values);
    }

    public void write(@NotNull JYML cfg, @NotNull String path) {
        this.values.forEach((rank, number) -> {
            cfg.set(path + "." + rank, number);
        });
    }

    @NotNull
    public T getBestValue(@NotNull Player player, @NotNull T def) {
        Set<String> groups = PlayerUtil.getPermissionGroups(player);
        // System.out.println("[0] groups of '" + player.getName() + "': " + groups);
        // System.out.println("[1] map to compare: " + map);

        Optional<Map.Entry<String, T>> opt = this.values.entrySet().stream().filter(entry -> entry.getKey().equalsIgnoreCase(Placeholders.DEFAULT) || groups.contains(entry.getKey())).min((entry1, entry2) -> {
            T val1 = entry1.getValue();
            T val2 = entry2.getValue();
            if (this.isNegativeBetter() && val2.doubleValue() < 0) return 1;
            if (this.isNegativeBetter() && val1.doubleValue() < 0) return -1;

            return Double.compare(val1.doubleValue(), val2.doubleValue());
        });

        if (opt.isEmpty() && this.isCheckAsPermission()) {
            return this.values.entrySet().stream()
                .filter(entry -> player.hasPermission(this.getPermissionPrefix() + entry.getKey()))
                .max(Comparator.comparingDouble(e -> e.getValue().doubleValue())).map(Map.Entry::getValue).orElse(def);
        }

        // System.out.println("[2] max value for '" + player.getName() + "': " +
        // (opt.isPresent() ? opt.get() : "-1x"));

        return opt.map(Map.Entry::getValue).orElse(def);
    }

    public boolean isNegativeBetter() {
        return negativeBetter;
    }

    public boolean isCheckAsPermission() {
        return checkAsPermission;
    }

    @Nullable
    public String getPermissionPrefix() {
        return permissionPrefix;
    }

    @NotNull
    public PlayerRankMap<T> setNegativeBetter(boolean negativeBetter) {
        this.negativeBetter = negativeBetter;
        return this;
    }

    @NotNull
    public PlayerRankMap<T> setCheckAsPermission(@NotNull String permissionPrefix) {
        this.checkAsPermission = true;
        this.permissionPrefix = permissionPrefix;
        return this;
    }
}

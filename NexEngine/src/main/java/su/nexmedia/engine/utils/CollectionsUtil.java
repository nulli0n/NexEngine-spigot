package su.nexmedia.engine.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.config.EngineConfig;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CollectionsUtil {

    @NotNull
    public static List<String> playerNames() {
        return playerNames(null);
    }

    @NotNull
    public static List<String> playerNames(@Nullable Player viewer) {
        List<String> names = new ArrayList<>();
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            if (viewer != null && !viewer.canSee(player)) continue;

            names.add(player.getName());
            if (EngineConfig.RESPECT_PLAYER_DISPLAYNAME.get()) {
                names.add(Colorizer.strip(player.getDisplayName()));
            }
        }
        return names;
    }

    @NotNull
    public static List<String> worldNames() {
        return Bukkit.getServer().getWorlds().stream().map(WorldInfo::getName).toList();
    }

    @NotNull
    public static <T> List<List<T>> split(@NotNull List<T> list, int targetSize) {
        List<List<T>> lists = new ArrayList<>();
        if (targetSize <= 0) return lists;

        for (int index = 0; index < list.size(); index += targetSize) {
            lists.add(list.subList(index, Math.min(index + targetSize, list.size())));
        }
        return lists;
    }

    @NotNull
    public static <K, V extends Comparable<? super V>> Map<K, V> sortAscent(@NotNull Map<K, V> map) {
        return sort(map, Map.Entry.comparingByValue());
    }

    @NotNull
    public static <K, V extends Comparable<? super V>> Map<K, V> sortDescent(@NotNull Map<K, V> map) {
        return sort(map, Collections.reverseOrder(Map.Entry.comparingByValue()));
    }

    @NotNull
    public static <K, V extends Comparable<? super V>> Map<K, V> sort(@NotNull Map<K, V> map, @NotNull Comparator<Map.Entry<K, V>> comparator) {
        return new LinkedList<>(map.entrySet()).stream().sorted(comparator)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (old, nev) -> nev, LinkedHashMap::new));
    }

    @NotNull
    public static List<String> getEnumsList(@NotNull Class<?> clazz) {
        return new ArrayList<>(Stream.of(clazz.getEnumConstants()).map(Object::toString).toList());
    }

    @NotNull
    public static <T extends Enum<T>> T next(@NotNull Enum<T> numeration) {
        return shifted(numeration, 1);
    }

    @NotNull
    public static <T extends Enum<T>> T next(@NotNull Enum<T> numeration, @NotNull Predicate<T> predicate) {
        return shifted(numeration, 1, predicate);
    }

    @NotNull
    public static <T extends Enum<T>> T previous(@NotNull Enum<T> numeration) {
        return shifted(numeration, -1);
    }

    @NotNull
    public static <T extends Enum<T>> T previous(@NotNull Enum<T> numeration, @NotNull Predicate<T> predicate) {
        return shifted(numeration, -1, predicate);
    }

    @NotNull
    public static <T extends Enum<T>> T shifted(@NotNull Enum<T> numeration, int shift) {
        return shifted(numeration, shift, null);
    }

    @NotNull
    private static <T extends Enum<T>> T shifted(@NotNull Enum<T> numeration, int shift, @Nullable Predicate<T> predicate) {
        T[] values = numeration.getDeclaringClass().getEnumConstants();
        return shifted(values, numeration/*.ordinal()*/, shift, predicate);
    }

    @NotNull
    private static <T extends Enum<T>> T shifted(T[] values, @NotNull Enum<T> origin, int shift, @Nullable Predicate<T> predicate) {
        if (predicate != null) {
            T source = origin.getDeclaringClass().cast(origin);
            List<T> filtered = new ArrayList<>(Arrays.asList(values));
            filtered.removeIf(num -> !predicate.test(num) && num != source);

            int currentIndex = filtered.indexOf(source);
            //List<T> filtered = Stream.of(values).filter(predicate).toList();
            if (currentIndex < 0 | filtered.isEmpty()) return source;//values[currentIndex];

            return shifted(filtered, currentIndex, shift);
        }
        return shifted(values, origin.ordinal(), shift);
    }

    @NotNull
    public static <T> T shifted(T[] values, int currentIndex, int shift) {
        int index = currentIndex + shift;
        return values[index >= values.length || index < 0 ? 0 : index];
    }

    @NotNull
    public static <T> T shifted(@NotNull List<T> values, int currentIndex, int shift) {
        int index = currentIndex + shift;
        return values.get(index >= values.size() || index < 0 ? 0 : index);
    }
}

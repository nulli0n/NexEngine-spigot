package su.nexmedia.engine.utils;

import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CollectionsUtil {

    public static final boolean[] BOOLEANS = new boolean[]{true, false};

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
    @Deprecated
    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(@NotNull Map<K, V> map) {
        return sortAscent(map);
    }

    @NotNull
    @Deprecated
    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValueUpDown(@NotNull Map<K, V> map) {
        return sortDescent(map);
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
    public static String getEnums(@NotNull Class<?> clazz) {
        return String.join(ChatColor.GRAY + ", " + ChatColor.WHITE, getEnumsList(clazz));
    }

    @NotNull
    public static List<String> getEnumsList(@NotNull Class<?> clazz) {
        return new ArrayList<>(Stream.of(clazz.getEnumConstants()).filter(Objects::nonNull).map(Object::toString).toList());
    }

    @NotNull
    public static <T extends Enum<T>> T switchEnum(@NotNull Enum<T> en) {
        @NotNull T[] values = en.getDeclaringClass().getEnumConstants();
        int next = en.ordinal() + 1;
        return values[next >= values.length ? 0 : next];
    }

    @Nullable
    public static <T extends Enum<T>> T getEnum(@NotNull String str, @NotNull Class<T> clazz) {
        try {
            return Enum.valueOf(clazz, str.toUpperCase());
        }
        catch (Exception ex) {
            return null;
        }
    }
}

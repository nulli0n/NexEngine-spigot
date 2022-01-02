package su.nexmedia.engine.utils;

import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CollectionsUtil {

    public static final boolean[] BOOLEANS = new boolean[]{true, false};

    @NotNull
    public static <T> List<List<T>> split(@NotNull List<T> list, int targetSize) {
        List<List<T>> lists = new ArrayList<>();
        if (targetSize <= 0) return lists;

        for (int i = 0; i < list.size(); i += targetSize) {
            lists.add(list.subList(i, Math.min(i + targetSize, list.size())));
        }
        return lists;
    }

    @SuppressWarnings("null")
    @NotNull
    @Deprecated
    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(@NotNull Map<K, V> map) {
        List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
        list.sort((Map.Entry.comparingByValue()));

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    @NotNull
    @Deprecated
    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValueUpDown(@NotNull Map<K, V> map) {
        List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
        list.sort(Collections.reverseOrder(Map.Entry.comparingByValue()));

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    @NotNull
    public static String getEnums(@NotNull Class<?> clazz) {
        StringBuilder str = new StringBuilder();
        for (String enumName : getEnumsList(clazz)) {
            if (enumName == null) continue;
            if (str.length() > 0) {
                str.append(ChatColor.GRAY);
                str.append(",");
            }
            str.append(ChatColor.WHITE);
            str.append(enumName);
        }
        return str.toString();
    }

    @NotNull
    public static List<String> getEnumsList(@NotNull Class<?> clazz) {
        List<String> list = new ArrayList<>();
        if (!clazz.isEnum()) return list;

        for (Object enumName : clazz.getEnumConstants()) {
            if (enumName == null) continue;
            list.add(enumName.toString());
        }
        return list;
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

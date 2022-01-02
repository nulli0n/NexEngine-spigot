package su.nexmedia.engine.utils.random;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Rnd {

    public static final MTRandom rnd = new MTRandom();

    public static float get() {
        return Rnd.rnd.nextFloat();
    }

    public static float get(boolean normalize) {
        float f = Rnd.get();
        if (normalize) f *= 100f;
        return f;
    }

    public static int get(int n) {
        return Rnd.nextInt(n);
    }

    public static int get(int min, int max) {
        return min + (int) Math.floor(Rnd.rnd.nextDouble() * (max - min + 1));
    }

    public static double getDouble(double max) {
        return getDouble(0, max);
    }

    public static double getDouble(double min, double max) {
        return min + (max - min) * rnd.nextDouble();
    }

    @Deprecated // The same as above
    public static double getDoubleNega(double min, double max) {
        double range = max - min;
        double scaled = rnd.nextDouble() * range;
        double shifted = scaled + min;
        return shifted;
    }

    @NotNull
    public static <E> E get(@NotNull E[] list) {
        return list[get(list.length)];
    }

    public static int get(int[] list) {
        return list[get(list.length)];
    }

    @Nullable
    public static <E> E get(@NotNull List<E> list) {
        return list.isEmpty() ? null : list.get(get(list.size()));
    }

    @Nullable
    public static <E> E get(@NotNull Set<E> list) {
        return get(new ArrayList<>(list));
    }

    @Nullable
    public static <T> T get(@NotNull Map<@NotNull T, Double> map) {
        List<T> list = get(map, 1);
        return list.isEmpty() ? null : list.get(0);
    }

    @NotNull
    public static <T> List<T> get(@NotNull Map<@NotNull T, Double> map, int amount) {
        map.values().removeIf(chance -> chance <= 0D);
        if (map.isEmpty()) return Collections.emptyList();

        List<T> list = new ArrayList<>();
        double total = map.values().stream().mapToDouble(d -> d).sum();

        for (int count = 0; count < amount; count++) {
            double index = Rnd.getDouble(0D, total);// Math.random() * total;
            double countWeight = 0D;

            for (Map.Entry<T, Double> en : map.entrySet()) {
                countWeight += en.getValue();
                if (countWeight >= index) {
                    list.add(en.getKey());
                    break;
                }
            }
        }
        return list;
    }

    public static boolean chance(int chance) {
        return chance >= 1 && (chance > 99 || nextInt(99) + 1 <= chance);
    }

    public static boolean chance(double chance) {
        return nextDouble() <= chance / 100.0;
    }

    public static int nextInt(int n) {
        return (int) Math.floor(Rnd.rnd.nextDouble() * n);
    }

    public static int nextInt() {
        return Rnd.rnd.nextInt();
    }

    public static double nextDouble() {
        return Rnd.rnd.nextDouble();
    }

    public static double nextGaussian() {
        return Rnd.rnd.nextGaussian();
    }

    public static boolean nextBoolean() {
        return Rnd.rnd.nextBoolean();
    }
}

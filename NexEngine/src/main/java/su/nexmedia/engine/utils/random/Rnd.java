package su.nexmedia.engine.utils.random;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.CollectionsUtil;
import su.nexmedia.engine.utils.Pair;

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

    @NotNull
    public static <E> E get(@NotNull E[] list) {
        return list[get(list.length)];
    }

    public static int get(int[] list) {
        return list[get(list.length)];
    }

    @NotNull
    public static <E> E get(@NotNull List<E> list) {
        if (list.isEmpty()) throw new NoSuchElementException("Empty list provided!");

        return list.get(get(list.size()));
    }

    @NotNull
    public static <E> E get(@NotNull Set<E> list) {
        return get(new ArrayList<>(list));
    }

    @NotNull
    public static <T> T getByWeight(@NotNull Map<T, Double> itemsMap) {
        List<Pair<T, Double>> items = CollectionsUtil.sortAscent(itemsMap).entrySet().stream()
            .filter(e -> e.getValue() > 0D)
            .map(e -> Pair.of(e.getKey(), e.getValue())).toList();
        double totalWeight = items.stream().mapToDouble(Pair::getSecond).sum();

        int index = 0;
        for (double chance = Rnd.nextDouble() * totalWeight; index < items.size() - 1; ++index) {
            chance -= items.get(index).getSecond();
            if (chance <= 0D) break;
        }
        return items.get(index).getFirst();
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

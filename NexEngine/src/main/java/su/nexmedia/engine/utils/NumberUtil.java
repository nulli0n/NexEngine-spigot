package su.nexmedia.engine.utils;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.lang.EngineLang;
import su.nexmedia.engine.lang.LangManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.TreeMap;
import java.util.function.Supplier;

public class NumberUtil {

    private static final DecimalFormat FORMAT_ROUND_HUMAN;
    private final static TreeMap<Integer, String> ROMAN_MAP = new TreeMap<>();
    private final static TreeMap<Integer, Supplier<String>> NUMERIC_MAP = new TreeMap<>();

    static {
        FORMAT_ROUND_HUMAN = new DecimalFormat("#,###.##", new DecimalFormatSymbols(Locale.ENGLISH));

        NUMERIC_MAP.put(0, () -> "");
        NUMERIC_MAP.put(1, () -> LangManager.getPlain(EngineLang.NUMBER_SHORT_THOUSAND));
        NUMERIC_MAP.put(2, () -> LangManager.getPlain(EngineLang.NUMBER_SHORT_MILLION));
        NUMERIC_MAP.put(3, () -> LangManager.getPlain(EngineLang.NUMBER_SHORT_BILLION));
        NUMERIC_MAP.put(4, () -> LangManager.getPlain(EngineLang.NUMBER_SHORT_TRILLION));
        NUMERIC_MAP.put(5, () -> LangManager.getPlain(EngineLang.NUMBER_SHORT_QUADRILLION));

        ROMAN_MAP.put(1000, "M");
        ROMAN_MAP.put(900, "CM");
        ROMAN_MAP.put(500, "D");
        ROMAN_MAP.put(400, "CD");
        ROMAN_MAP.put(100, "C");
        ROMAN_MAP.put(90, "XC");
        ROMAN_MAP.put(50, "L");
        ROMAN_MAP.put(40, "XL");
        ROMAN_MAP.put(10, "X");
        ROMAN_MAP.put(9, "IX");
        ROMAN_MAP.put(5, "V");
        ROMAN_MAP.put(4, "IV");
        ROMAN_MAP.put(1, "I");
    }

    @NotNull
    public static String format(double value) {
        return FORMAT_ROUND_HUMAN.format(value);
    }

    public static Pair<String, String> formatCompact(double value) {
        boolean negative = false;
        if (value < 0) {
            value = Math.abs(value);
            negative = true;
        }
        int index = 0;
        while ((value / 1000) >= 1 && index < NUMERIC_MAP.size() - 1) {
            value = value / 1000;
            index++;
        }

        return Pair.of(FORMAT_ROUND_HUMAN.format(negative ? -value : value), NUMERIC_MAP.get(NUMERIC_MAP.floorKey(index)).get());
    }

    public static double round(double value) {
        return new BigDecimal(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    @NotNull
    public static String toRoman(int number) {
        if (number <= 0) return String.valueOf(number);

        int key = ROMAN_MAP.floorKey(number);
        if (number == key) {
            return ROMAN_MAP.get(number);
        }
        return ROMAN_MAP.get(key) + toRoman(number - key);
    }

    public static int[] splitIntoParts(int whole, int parts) {
        int[] arr = new int[parts];
        int remain = whole;
        int partsLeft = parts;
        for (int i = 0; partsLeft > 0; i++) {
            int size = (remain + partsLeft - 1) / partsLeft; // rounded up, aka ceiling
            arr[i] = size;
            remain -= size;
            partsLeft--;
        }
        return arr;
    }
}

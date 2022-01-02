package su.nexmedia.engine.utils;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class NumberUtil {

    public static final  NumberFormat  FORMAT_GROUP;
    private static final DecimalFormat FORMAT_DECIMAL_ROUND;
    private static final NumberFormat  FORMAT_DECIMAL_ROUND_GROUP;

    static {
        FORMAT_DECIMAL_ROUND = new DecimalFormat("#.##"); // #.00 for 3.00 values

        FORMAT_DECIMAL_ROUND_GROUP = DecimalFormat.getInstance(Locale.ENGLISH);
        FORMAT_DECIMAL_ROUND_GROUP.setMinimumFractionDigits(0);
        FORMAT_DECIMAL_ROUND_GROUP.setMaximumFractionDigits(2);
        FORMAT_DECIMAL_ROUND_GROUP.setGroupingUsed(true);

        FORMAT_GROUP = NumberFormat.getInstance();
        FORMAT_GROUP.setGroupingUsed(true);
    }

    @NotNull
    public static String format(double d) {
        return FORMAT_DECIMAL_ROUND.format(d).replace(",", ".");
    }

    @NotNull
    public static String formatGroup(double value) {
        return FORMAT_DECIMAL_ROUND_GROUP.format(value);//.replace(",", ".");
    }

    public static double round(double d) {
        return Double.parseDouble(format(d));
    }

    @NotNull
    public static String toRoman(int input) {
        if (input < 1 || input > 3999) {
            return "N/A";
        }

        StringBuilder s = new StringBuilder();
        while (input >= 1000) {
            s.append("M");
            input -= 1000;
        }
        while (input >= 900) {
            s.append("CM");
            input -= 900;
        }
        while (input >= 500) {
            s.append("D");
            input -= 500;
        }
        while (input >= 400) {
            s.append("CD");
            input -= 400;
        }
        while (input >= 100) {
            s.append("C");
            input -= 100;
        }
        while (input >= 90) {
            s.append("XC");
            input -= 90;
        }
        while (input >= 50) {
            s.append("L");
            input -= 50;
        }
        while (input >= 40) {
            s.append("XL");
            input -= 40;
        }
        while (input >= 10) {
            s.append("X");
            input -= 10;
        }
        while (input >= 9) {
            s.append("IX");
            input -= 9;
        }
        while (input >= 5) {
            s.append("V");
            input -= 5;
        }
        while (input >= 4) {
            s.append("IV");
            input -= 4;
        }
        while (input >= 1) {
            s.append("I");
            input -= 1;
        }
        return s.toString();
    }

    public static int fromRoman(@NotNull String romanNumber) {
        int decimal = 0;
        int lastNumber = 0;
        String romanNumeral = romanNumber.toUpperCase();
        for (int x = romanNumeral.length() - 1; x >= 0; x--) {
            char convertToDecimal = romanNumeral.charAt(x);

            switch (convertToDecimal) {
                case 'M' -> {
                    decimal = processDecimal(1000, lastNumber, decimal);
                    lastNumber = 1000;
                }
                case 'D' -> {
                    decimal = processDecimal(500, lastNumber, decimal);
                    lastNumber = 500;
                }
                case 'C' -> {
                    decimal = processDecimal(100, lastNumber, decimal);
                    lastNumber = 100;
                }
                case 'L' -> {
                    decimal = processDecimal(50, lastNumber, decimal);
                    lastNumber = 50;
                }
                case 'X' -> {
                    decimal = processDecimal(10, lastNumber, decimal);
                    lastNumber = 10;
                }
                case 'V' -> {
                    decimal = processDecimal(5, lastNumber, decimal);
                    lastNumber = 5;
                }
                case 'I' -> {
                    decimal = processDecimal(1, lastNumber, decimal);
                    lastNumber = 1;
                }
            }
        }
        return decimal;
    }

    private static int processDecimal(int decimal, int lastNumber, int lastDecimal) {
        if (lastNumber > decimal) {
            return lastDecimal - decimal;
        }
        return lastDecimal + decimal;
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

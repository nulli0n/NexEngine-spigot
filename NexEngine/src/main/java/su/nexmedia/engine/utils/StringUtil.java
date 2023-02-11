package su.nexmedia.engine.utils;

import org.bukkit.Color;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.random.Rnd;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StringUtil {

    @NotNull
    public static String oneSpace(@NotNull String str) {
        return str.trim().replaceAll("\\s+", " ");
    }

    @NotNull
    public static String noSpace(@NotNull String str) {
        return str.trim().replaceAll("\\s+", "");
    }

    @NotNull
    @Deprecated
    public static String color(@NotNull String str) {
        return Colorizer.apply(str);
    }

    @NotNull
    @Deprecated
    public static String colorFix(@NotNull String str) {
        return Colorizer.fixLegacy(str);
    }

    @NotNull
    @Deprecated
    public static String colorGradient(@NotNull String string) {
        return Colorizer.gradient(string);
    }

    @NotNull
    @Deprecated
    public static String colorHex(@NotNull String str) {
        return Colorizer.hex(str);
    }

    @NotNull
    @Deprecated
    public static String colorHexRaw(@NotNull String str) {
        return Colorizer.plainHex(str);
    }

    @NotNull
    @Deprecated
    public static String colorRaw(@NotNull String str) {
        return Colorizer.plain(str);
    }

    @NotNull
    @Deprecated
    public static String colorOff(@NotNull String str) {
        return Colorizer.strip(str);
    }

    @NotNull
    @Deprecated
    public static List<String> color(@NotNull List<String> list) {
        return Colorizer.apply(list);
    }

    @NotNull
    @Deprecated
    public static Set<String> color(@NotNull Set<String> list) {
        return Colorizer.apply(list);
    }

    @NotNull
    public static List<String> replace(@NotNull List<String> orig, @NotNull String placeholder, boolean keep, String... replacer) {
        return StringUtil.replace(orig, placeholder, keep, Arrays.asList(replacer));
    }

    @NotNull
    public static List<String> replace(@NotNull List<String> orig, @NotNull String placeholder, boolean keep, List<String> replacer) {
        List<String> replaced = new ArrayList<>();
        for (String line : orig) {
            if (line.contains(placeholder)) {
                if (!keep) {
                    replaced.addAll(replacer);
                }
                else {
                    replacer.forEach(lineRep -> replaced.add(line.replace(placeholder, lineRep)));
                }
                continue;
            }
            replaced.add(line);
        }

        return replaced;
    }

    public static double getDouble(@NotNull String input, double def) {
        return getDouble(input, def, false);
    }

    public static double getDouble(@NotNull String input, double def, boolean allowNegative) {
        try {
            double amount = Double.parseDouble(input);
            return (amount < 0D && !allowNegative ? def : amount);
        }
        catch (NumberFormatException ex) {
            return def;
        }
    }

    public static int getInteger(@NotNull String input, int def) {
        return getInteger(input, def, false);
    }

    public static int getInteger(@NotNull String input, int def, boolean allowNegative) {
        return (int) getDouble(input, def, allowNegative);
    }

    public static int[] getIntArray(@NotNull String str) {
        String[] split = noSpace(str).split(",");
        int[] array = new int[split.length];
        for (int index = 0; index < split.length; index++) {
            try {
                array[index] = Integer.parseInt(split[index]);
            }
            catch (NumberFormatException e) {
                array[index] = 0;
            }
        }
        return array;
    }

    @NotNull
    public static <T extends Enum<T>> Optional<T> getEnum(@NotNull String str, @NotNull Class<T> clazz) {
        try {
            return Optional.of(Enum.valueOf(clazz, str.toUpperCase()));
        }
        catch (Exception ex) {
            return Optional.empty();
        }
    }

    @NotNull
    public static Color parseColor(@NotNull String colorRaw) {
        String[] rgb = colorRaw.split(",");
        int red = StringUtil.getInteger(rgb[0], 0);
        if (red < 0) red = Rnd.get(255);

        int green = rgb.length >= 2 ? StringUtil.getInteger(rgb[1], 0) : 0;
        if (green < 0) green = Rnd.get(255);

        int blue = rgb.length >= 3 ? StringUtil.getInteger(rgb[2], 0) : 0;
        if (blue < 0) blue = Rnd.get(255);

        return Color.fromRGB(red, green, blue);
    }

    @NotNull
    public static String capitalizeUnderscored(@NotNull String str) {
        return capitalizeFully(str.replace("_", " "));
    }

    @NotNull
    public static String capitalizeFully(@NotNull String str) {
        if (str.length() != 0) {
            str = str.toLowerCase();
            return capitalize(str);
        }
        return str;
    }

    @NotNull
    public static String capitalize(@NotNull String str) {
        if (str.length() != 0) {
            int strLen = str.length();
            StringBuilder buffer = new StringBuilder(strLen);
            boolean capitalizeNext = true;

            for (int i = 0; i < strLen; ++i) {
                char ch = str.charAt(i);
                if (Character.isWhitespace(ch)) {
                    buffer.append(ch);
                    capitalizeNext = true;
                }
                else if (capitalizeNext) {
                    buffer.append(Character.toTitleCase(ch));
                    capitalizeNext = false;
                }
                else {
                    buffer.append(ch);
                }
            }
            return buffer.toString();
        }
        return str;
    }

    @NotNull
    public static String capitalizeFirstLetter(@NotNull String original) {
        if (original.isEmpty()) return original;
        return original.substring(0, 1).toUpperCase() + original.substring(1);
    }

    /**
     * @param original List to remove empty lines from.
     * @return A list with no multiple empty lines in a row.
     */
    @NotNull
    public static List<String> stripEmpty(@NotNull List<String> original) {
        List<String> stripped = new ArrayList<>();
        for (int index = 0; index < original.size(); index++) {
            String line = original.get(index);
            if (line.isEmpty()) {
                String last = stripped.isEmpty() ? null : stripped.get(stripped.size() - 1);
                if (last == null || last.isEmpty() || index == (original.size() - 1)) continue;
            }
            stripped.add(line);
        }
        return stripped;
    }

    /**
     * Kinda half-smart completer like in IDEA by partial word matches.
     * @param originals A list of all completions.
     * @param token A string to find partial matches for.
     * @param steps Part's size.
     * @return A list of completions that has partial matches to the given string.
     */
    @NotNull
    public static List<String> getByPartialMatches(@NotNull List<String> originals, @NotNull String token, int steps) {
        token = token.toLowerCase();

        int[] parts = NumberUtil.splitIntoParts(token.length(), steps);
        int lastIndex = 0;
        StringBuilder builder = new StringBuilder();
        for (int partSize: parts) {
            String sub = token.substring(lastIndex, lastIndex + partSize);
            lastIndex += partSize;

            builder.append(Pattern.quote(sub)).append("(?:.*)");
        }

        Pattern pattern = Pattern.compile(builder.toString());
        List<String> list = new ArrayList<>(originals.stream().filter(orig -> pattern.matcher(orig.toLowerCase()).find()).toList());
        Collections.sort(list);
        return list;
    }

    @NotNull
    public static String extractCommandName(@NotNull String command) {
        String commandName = Colorizer.strip(command).split(" ")[0].substring(1);

        String[] pluginPrefix = commandName.split(":");
        if (pluginPrefix.length == 2) {
            commandName = pluginPrefix[1];
        }

        return commandName;
    }

    public static boolean isCustomBoolean(@NotNull String str) {
        String[] customs = new String[]{"0","1","on","off","true","false","yes","no"};
        return Stream.of(customs).collect(Collectors.toSet()).contains(str.toLowerCase());
    }

    public static boolean parseCustomBoolean(@NotNull String str) {
        if (str.equalsIgnoreCase("0") || str.equalsIgnoreCase("off") || str.equalsIgnoreCase("no")) {
            return false;
        }
        if (str.equalsIgnoreCase("1") || str.equalsIgnoreCase("on") || str.equalsIgnoreCase("yes")) {
            return true;
        }
        return Boolean.parseBoolean(str);
    }
}

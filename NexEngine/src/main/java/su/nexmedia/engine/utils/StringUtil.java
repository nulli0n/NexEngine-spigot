package su.nexmedia.engine.utils;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Color;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexEngine;
import su.nexmedia.engine.utils.random.Rnd;
import su.nexmedia.engine.utils.regex.RegexUtil;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StringUtil {

    public static final Pattern PATTERN_HEX      = Pattern.compile("#([A-Fa-f0-9]{6})");
    public static final Pattern PATTERN_GRADIENT = Pattern.compile("<gradient:" + PATTERN_HEX.pattern() + ">(.*?)</gradient:" + PATTERN_HEX.pattern() + ">");

    @NotNull
    public static String oneSpace(@NotNull String str) {
        return str.trim().replaceAll("\\s+", " ");
    }

    @NotNull
    public static String noSpace(@NotNull String str) {
        return str.trim().replaceAll("\\s+", "");
    }

    @NotNull
    public static String color(@NotNull String str) {
        return colorHex(colorGradient(ChatColor.translateAlternateColorCodes('&', colorFix(str))));
    }

    /**
     * Removes color duplications.
     * @param str String to fix.
     * @return A string with a proper color codes formatting.
     */
    @NotNull
    public static String colorFix(@NotNull String str) {
        return NexEngine.get().getNMS().fixColors(str);
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

    private static ChatColor[] createGradient(@NotNull java.awt.Color start, @NotNull java.awt.Color end, int length) {
        ChatColor[] colors = new ChatColor[length];
        for (int index = 0; index < length; index++) {
            double percent = (double) index / (double) length;

            int red = (int) (start.getRed() + percent * (end.getRed() - start.getRed()));
            int green = (int) (start.getGreen() + percent * (end.getGreen() - start.getGreen()));
            int blue = (int) (start.getBlue() + percent * (end.getBlue() - start.getBlue()));

            java.awt.Color color = new java.awt.Color(red, green, blue);
            colors[index] = ChatColor.of(color);
        }
        return colors;
    }

    @NotNull
    public static String colorGradient(@NotNull String string) {
        Matcher matcher = PATTERN_GRADIENT.matcher(string);
        while (RegexUtil.matcherFind(matcher)) {
            String start = matcher.group(1);
            String end = matcher.group(3);
            String content = matcher.group(2);

            java.awt.Color colorStart = new java.awt.Color(Integer.parseInt(start, 16));
            java.awt.Color colorEnd = new java.awt.Color(Integer.parseInt(end, 16));
            ChatColor[] colors = createGradient(colorStart, colorEnd, StringUtil.colorOff(content).length());

            StringBuilder gradiented = new StringBuilder();
            StringBuilder specialColors = new StringBuilder();
            char[] characters = content.toCharArray();
            int outIndex = 0;
            for (int index = 0; index < characters.length; index++) {
                if (characters[index] == ChatColor.COLOR_CHAR) {
                    if (index + 1 < characters.length) {
                        if (characters[index + 1] == 'r') {
                            specialColors.setLength(0);
                        }
                        else {
                            specialColors.append(characters[index]);
                            specialColors.append(characters[index + 1]);
                        }
                        index++;
                    }
                    else gradiented.append(colors[outIndex++]).append(specialColors).append(characters[index]);
                }
                else gradiented.append(colors[outIndex++]).append(specialColors).append(characters[index]);
            }

            string = string.replace(matcher.group(0), gradiented.toString());
        }
        return string;
    }

    @NotNull
    public static String colorHex(@NotNull String str) {
        Matcher matcher = PATTERN_HEX.matcher(str);
        StringBuilder buffer = new StringBuilder(str.length() + 4 * 8);
        while (matcher.find()) {
            String group = matcher.group(1);
            matcher.appendReplacement(buffer, ChatColor.COLOR_CHAR + "x" + ChatColor.COLOR_CHAR + group.charAt(0) + ChatColor.COLOR_CHAR + group.charAt(1) + ChatColor.COLOR_CHAR + group.charAt(2) + ChatColor.COLOR_CHAR + group.charAt(3) + ChatColor.COLOR_CHAR + group.charAt(4) + ChatColor.COLOR_CHAR + group.charAt(5));
        }
        return matcher.appendTail(buffer).toString();
    }

    @NotNull
    public static String colorHexRaw(@NotNull String str) {
        StringBuilder buffer = new StringBuilder(str);

        int index;
        while ((index = buffer.toString().indexOf(ChatColor.COLOR_CHAR + "x")) >= 0) {
            int count = 0;
            buffer.replace(index, index + 2, "#");

            for (int point = index + 1; count < 6; point += 1) {
                buffer.deleteCharAt(point);
                count++;
            }
        }

        return buffer.toString();
    }

    @NotNull
    public static String colorRaw(@NotNull String str) {
        return str.replace(ChatColor.COLOR_CHAR, '&');
    }

    @NotNull
    public static String colorOff(@NotNull String str) {
        String off = ChatColor.stripColor(str);
        return off == null ? "" : off;
    }

    @NotNull
    public static List<String> color(@NotNull List<String> list) {
        list.replaceAll(StringUtil::color);
        return list;
    }

    @NotNull
    public static Set<String> color(@NotNull Set<String> list) {
        return new HashSet<>(StringUtil.color(new ArrayList<>(list)));
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
        /*for (String src : originals) {
            if (src.toLowerCase().startsWith(token.toLowerCase())) {
                list.add(src);
            }
        }*/
        Collections.sort(list);
        return list;
    }

    @NotNull
    public static String extractCommandName(@NotNull String cmd) {
        String cmdFull = colorOff(cmd).split(" ")[0];
        String cmdName = cmdFull.replace("/", "").replace("\\/", "");
        String[] pluginPrefix = cmdName.split(":");
        if (pluginPrefix.length == 2) {
            cmdName = pluginPrefix[1];
        }

        return cmdName;
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

    @NotNull
    public static String c(@NotNull String s) {
        char[] ch = s.toCharArray();
        char[] out = new char[ch.length * 2];
        int i = 0;
        for (char c : ch) {
            int orig = Character.getNumericValue(c);
            int min;
            int max;

            char cas;
            if (Character.isUpperCase(c)) {
                min = Character.getNumericValue('A');
                max = Character.getNumericValue('Z');
                cas = 'q';
            }
            else {
                min = Character.getNumericValue('a');
                max = Character.getNumericValue('z');
                cas = 'p';
            }

            int pick = min + (max - orig);
            char get = Character.forDigit(pick, Character.MAX_RADIX);
            out[i] = get;
            out[++i] = cas;
            i++;
        }
        return String.valueOf(out);
    }

    @NotNull
    public static String d(@NotNull String s) {
        char[] ch = s.toCharArray();
        char[] dec = new char[ch.length / 2];
        for (int i = 0; i < ch.length; i = i + 2) {
            int j = i;
            char letter = ch[j];
            char cas = ch[++j];
            boolean upper = cas == 'q';

            int max;
            int min;
            if (upper) {
                min = Character.getNumericValue('A');
                max = Character.getNumericValue('Z');
            }
            else {
                min = Character.getNumericValue('a');
                max = Character.getNumericValue('z');
            }

            int orig = max - Character.getNumericValue(letter) + min;
            char get = Character.forDigit(orig, Character.MAX_RADIX);
            if (upper)
                get = Character.toUpperCase(get);

            dec[i / 2] = get;
        }
        return String.valueOf(dec);
    }
}

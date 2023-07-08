package su.nexmedia.engine.utils.regex;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtil {

    @Deprecated public static final Pattern PATTERN_EN = Pattern.compile("[a-zA-Z0-9_]*");
    @Deprecated public static final Pattern PATTERN_RU = Pattern.compile("[a-zA-Zа-яА-Я0-9_]*");
    @Deprecated public static final Pattern PATTERN_IP = Pattern.compile(
        "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)");

    @Deprecated
    public static boolean matchesEn(@NotNull String msg) {
        return matches(PATTERN_EN, msg);
    }

    @Deprecated
    public static boolean matchesEnRu(@NotNull String msg) {
        return matches(PATTERN_RU, msg);
    }

    @Deprecated
    public static boolean isIpAddress(@NotNull String str) {
        return matches(PATTERN_IP, str);
    }

    public static boolean matches(@NotNull Pattern pattern, @NotNull String text) {
        Matcher matcher = getMatcher(pattern, text);
        return matcher.matches();
    }

    @NotNull
    public static Matcher getMatcher(@NotNull String pattern, @NotNull String text) {
        return getMatcher(pattern, text, 200);
    }

    @NotNull
    public static Matcher getMatcher(@NotNull String patternText, @NotNull String text, long timeout) {
        Pattern pattern = Pattern.compile(patternText);
        return getMatcher(pattern, text, timeout);
    }

    @NotNull
    public static Matcher getMatcher(@NotNull Pattern pattern, @NotNull String msg) {
        return getMatcher(pattern, msg, 200);
    }

    public static boolean matcherFind(@NotNull Matcher matcher) {
        try {
            return matcher.find();
        }
        catch (MatcherTimeoutException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Matcher timeout error: '" + matcher.pattern().pattern() + "' (" + e.getTimeout() + "ms)");
            return false;
        }
    }

    @NotNull
    public static Matcher getMatcher(@NotNull Pattern pattern, @NotNull String text, long timeout) {
        if (timeout <= 0) {
            return pattern.matcher(text);
        }
        return pattern.matcher(new TimeoutCharSequence(text, timeout));
    }
}

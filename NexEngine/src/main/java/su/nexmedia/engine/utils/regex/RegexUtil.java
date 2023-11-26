package su.nexmedia.engine.utils.regex;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.EngineUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Deprecated
public class RegexUtil {

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
        catch (MatcherTimeoutException exception) {
            EngineUtils.ENGINE.warn("Matcher " + exception.getTimeout() + "ms timeout error for: '" + matcher.pattern().pattern() + "'");
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

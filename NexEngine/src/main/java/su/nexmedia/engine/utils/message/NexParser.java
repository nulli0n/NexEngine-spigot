package su.nexmedia.engine.utils.message;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.utils.Colorizer;
import su.nexmedia.engine.utils.regex.RegexUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NexParser {

    public static final String OPTION_PATTERN = ":(?:'|\")(.*?)(?:'|\")(?=>|\\s|$)";

    private static final Pattern PATTERN_OPTIONS = Pattern.compile("<\\?(.*?)\\?>(.*?)(?:<\\/>)+");
    private static final Map<ClickEvent.Action, Pattern> PATTERN_CLICKS = new HashMap<>();
    private static final Map<HoverEvent.Action, Pattern> PATTERN_HOVERS = new HashMap<>();
    private static final Pattern PATTERN_FONT = Pattern.compile("font" + OPTION_PATTERN);
    private static final Pattern PATTERN_INSERTION = Pattern.compile("insertion" + OPTION_PATTERN);

    public static final String TAG_NEWLINE = "<newline>";

    static {
        for (ClickEvent.Action action : ClickEvent.Action.values()) {
            PATTERN_CLICKS.put(action, Pattern.compile(action.name().toLowerCase() + OPTION_PATTERN));
        }
        for (HoverEvent.Action action : HoverEvent.Action.values()) {
            PATTERN_HOVERS.put(action, Pattern.compile(action.name().toLowerCase() + OPTION_PATTERN));
        }
    }

    public static boolean contains(@NotNull String message) {
        Matcher matcher = RegexUtil.getMatcher(PATTERN_OPTIONS, message);
        return matcher.find();
    }

    @NotNull
    public static String removeFrom(@NotNull String message) {
        return strip(message, false);
    }

    @NotNull
    public static String toPlainText(@NotNull String message) {
        return strip(message, true);
    }

    @NotNull
    private static String strip(@NotNull String message, boolean toPlain) {
        Matcher matcher = RegexUtil.getMatcher(PATTERN_OPTIONS, message);
        while (RegexUtil.matcherFind(matcher)) {
            String matchFull = matcher.group(0);
            if (toPlain) {
                String matchOptions = matcher.group(1).trim();
                String matchText = matcher.group(2);
                message = message.replace(matchFull, matchText);
            }
            else message = message.replace(matchFull, "");
        }
        return message;
    }

    public static String[] getPlainParts(@NotNull String message) {
        //message = StringUtil.color(message.replace("\n", " "));
        return PATTERN_OPTIONS.split(message);
    }

    @NotNull
    public static NexMessage toMessage(@NotNull String message) {
        message = Colorizer.apply(message);

        Matcher matcher = RegexUtil.getMatcher(PATTERN_OPTIONS, message);
        Map<String, String> parameters = new HashMap<>();
        while (RegexUtil.matcherFind(matcher)) {
            String matchFull = matcher.group(0);
            String matchOptions = matcher.group(1).trim();
            String matchText = matcher.group(2);

            message = message.replace(matchFull, matchText);
            parameters.put(matchText, matchOptions);
        }

        NexMessage nexMessage = new NexMessage(message);
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            String text = entry.getKey();
            String options = entry.getValue();

            NexComponent component = nexMessage.addComponent(text, text);
            for (Map.Entry<ClickEvent.Action, Pattern> entryParams : PATTERN_CLICKS.entrySet()) {
                String paramValue = getOption(entryParams.getValue(), options);
                if (paramValue == null) continue;

                component.addClickEvent(entryParams.getKey(), paramValue);
                break;
            }
            for (Map.Entry<HoverEvent.Action, Pattern> entryParams : PATTERN_HOVERS.entrySet()) {
                String paramValue = getOption(entryParams.getValue(), options);
                if (paramValue == null) continue;

                component.addHoverEvent(entryParams.getKey(), paramValue);
                break;
            }
            component.setFont(getOption(PATTERN_FONT, options));
            component.setInsertion(getOption(PATTERN_INSERTION, options));
        }

        return nexMessage;
    }

    @Nullable
    private static String getOption(@NotNull Pattern pattern, @NotNull String from) {
        Matcher matcher = RegexUtil.getMatcher(pattern, from);
        if (!RegexUtil.matcherFind(matcher)) return null;

        return matcher.group(1).stripLeading();
    }
}

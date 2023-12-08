package su.nexmedia.engine.config;

import su.nexmedia.engine.api.config.JOption;
import su.nexmedia.engine.utils.values.UniFormatter;

import java.math.RoundingMode;
import java.util.Arrays;
import java.util.stream.Collectors;

public class EngineConfig {

    public static final JOption<Integer> TAB_COMPLETER_REGEX_MAX_LENGTH = JOption.create("TabCompleter.Regex_Max_Length",
        32,
        "Sets maximal length for input text to use a regex based search (aka smart tab-completer).",
        "When player entered text with length that exceeds this value, basic text search will be used instead.");

    public static final JOption<Integer> TAB_COMPLETER_REGEX_TIMEOUT = JOption.create("TabCompleter.Regex_Timeout",
        25,
        "Amount of milliseconds for regex matcher timeout in tab completion.",
        "If tab-completion takes more than this amount to find matches from a list, it will be interrupted.");

    public static final JOption<Boolean> RESPECT_PLAYER_DISPLAYNAME = JOption.create("Engine.Respect_Player_DisplayName",
        false,
        "Sets whether or not 'Player#getDisplayName' can be used to find & get players in addition to regular 'Player#getName'.");

    public static final JOption<UniFormatter> NUMBER_FORMATTER = JOption.create("Engine.Number",
        UniFormatter.of("#,###.##", RoundingMode.HALF_EVEN),
        "Control over how numerical data is formatted and rounded.",
        "Allowed modes: " + Arrays.stream(RoundingMode.values()).map(RoundingMode::name).map(String::toLowerCase).collect(Collectors.joining(", ")),
        "A tutorial can be found here: https://docs.oracle.com/javase/tutorial/i18n/format/decimalFormat.html");
}

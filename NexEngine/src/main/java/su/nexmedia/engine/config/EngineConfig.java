package su.nexmedia.engine.config;

import su.nexmedia.engine.api.config.JOption;
import su.nexmedia.engine.utils.values.UniFormatter;

import java.math.RoundingMode;
import java.util.Arrays;
import java.util.stream.Collectors;

public class EngineConfig {

    public static final JOption<Boolean> USER_DEBUG_ENABLED = JOption.create("UserData.Debug",
        false,
        "Enables debug messages for user data management.",
        "[Default is false]");

    public static final JOption<Integer> USER_CACHE_LIFETIME = JOption.create("UserData.Cache.LifeTime",
        300,
        "Sets how long (in seconds) user data will be cached for offline users",
        "until removed and needs to be loaded from the database again.",
        "[Default is 300 (5 minutes)]");

    public static final JOption<Boolean> USER_CACHE_NAME_AND_UUID = JOption.create("UserData.Cache.Names_And_UUIDs",
        true,
        "Sets whether or not plugin will cache player names and UUIDs.",
        "This will improve database performance when checking if user exists, but will increase memory usage.",
        "[Default is true]");

    public static final JOption<Integer> TAB_COMPLETER_REGEX_MAX_LENGTH = JOption.create("TabCompleter.Regex_Max_Length",
        32,
        "Sets maximal length for input text to use a regex based search (aka smart tab-completer).",
        "When player entered text with length that exceeds this value, basic text search will be used instead.",
        "[Default is 32]");

    public static final JOption<Integer> TAB_COMPLETER_REGEX_TIMEOUT = JOption.create("TabCompleter.Regex_Timeout",
        25,
        "Amount of milliseconds for regex matcher timeout in tab completion.",
        "If tab-completion takes more than this amount to find matches from a list, it will be interrupted.",
        "[Default is 25ms]");

    public static final JOption<Boolean> RESPECT_PLAYER_DISPLAYNAME = JOption.create("Engine.Respect_Player_DisplayName",
        false,
        "Sets whether or not 'Player#getDisplayName' can be used to find & get players in addition to regular 'Player#getName'.",
        "This is useful if you want to use custom player nicknames in commands.",
        "(Works only for NexEngine based plugins.)",
        "[Default is false]");

    public static final JOption<UniFormatter> NUMBER_FORMATTER = JOption.create("Engine.Number",
        UniFormatter.of("#,###.##", RoundingMode.HALF_EVEN),
        "Control over how numerical data is formatted and rounded.",
        "Allowed modes: " + Arrays.stream(RoundingMode.values()).map(RoundingMode::name).map(String::toLowerCase).collect(Collectors.joining(", ")),
        "A tutorial can be found here: https://docs.oracle.com/javase/tutorial/i18n/format/decimalFormat.html");
}

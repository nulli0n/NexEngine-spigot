package su.nexmedia.engine.api.command;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.Colorizer;
import su.nexmedia.engine.utils.StringUtil;

import java.util.function.Function;
import java.util.regex.Pattern;

public class CommandFlag<T> {

    private final String name;
    private final Pattern pattern;
    private final Function<String, T> parser;

    public CommandFlag(@NotNull String name, @NotNull Function<String, T> parser) {
        this.name = name;
        this.pattern = Pattern.compile("-" + name + "(\\s|$)([^-]*)");
        this.parser = parser;
    }

    @NotNull
    public static CommandFlag<World> worldFlag(@NotNull String name) {
        return new CommandFlag<>(name, Bukkit::getWorld);
    }

    @NotNull
    public static CommandFlag<String> stringFlag(@NotNull String name) {
        return new CommandFlag<>(name, Function.identity());
    }

    @NotNull
    public static CommandFlag<String> textFlag(@NotNull String name) {
        return new CommandFlag<>(name, Colorizer::apply);
    }

    @NotNull
    public static CommandFlag<Integer> intFlag(@NotNull String name) {
        return new CommandFlag<>(name, str -> StringUtil.getInteger(str, 0, true));
    }

    @NotNull
    public static CommandFlag<Double> doubleFlag(@NotNull String name) {
        return new CommandFlag<>(name, str -> StringUtil.getDouble(str, 0, true));
    }

    @NotNull
    public static CommandFlag<Boolean> booleanFlag(@NotNull String name) {
        return new CommandFlag<>(name, str -> true);
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public String getNamePrefixed() {
        return "-" + name;
    }

    @NotNull
    public Pattern getPattern() {
        return pattern;
    }

    @NotNull
    public Function<String, T> getParser() {
        return parser;
    }
}

package su.nexmedia.engine.actions.parameter;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.actions.parameter.parser.IParameterValueParser;

import java.util.regex.Pattern;

public abstract class AbstractParameter<T> {

    protected final String  name;
    protected final String  flag;
    protected final Pattern pattern;

    public AbstractParameter(@NotNull String name, @NotNull String flag) {
        this.name = name.toLowerCase();
        this.flag = flag;
        this.pattern = Pattern.compile("(~)+(" + this.getFlag() + ")+?(:)+(.*?)(;)");
    }

    @NotNull
    public final String getName() {
        return this.name;
    }

    @NotNull
    public final Pattern getPattern() {
        return this.pattern;
    }

    @NotNull
    public final String getFlag() {
        return this.flag;
    }

    @NotNull
    public abstract IParameterValueParser<T> getParser();
}

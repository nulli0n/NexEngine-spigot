package su.nexmedia.engine.actions.parameter.defaults;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.actions.parameter.AbstractParameter;
import su.nexmedia.engine.actions.parameter.parser.IParameterValueParser;

public class ParameterDefaultString extends AbstractParameter<String> {

    public ParameterDefaultString(@NotNull String key, @NotNull String flag) {
        super(key, flag);
    }

    @Override
    public @NotNull IParameterValueParser<String> getParser() {
        return IParameterValueParser.STRING;
    }
}

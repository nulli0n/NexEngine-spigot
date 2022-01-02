package su.nexmedia.engine.actions.parameter.parser;

import org.jetbrains.annotations.NotNull;

public class ParameterParserBoolean implements IParameterValueParser<Boolean> {

    @Override
    @NotNull
    public Boolean parse(@NotNull String str) {
        return Boolean.parseBoolean(str);
    }
}

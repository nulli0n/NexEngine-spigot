package su.nexmedia.engine.actions.parameter.parser;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.StringUtil;

public class ParameterParserString implements IParameterValueParser<String> {

    @Override
    @NotNull
    public String parse(@NotNull String str) {
        return StringUtil.color(str);
    }
}

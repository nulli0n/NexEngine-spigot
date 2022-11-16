package su.nexmedia.engine.actions.parameter.parser;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.utils.StringUtil;

@Deprecated // TODO Move in Parameter class
public interface IParameterValueParser<T> {

    IParameterValueParser<String>  STRING  = StringUtil::color;
    IParameterValueParser<Boolean> BOOLEAN = Boolean::parseBoolean;
    ParameterParserNumber  NUMBER  = new ParameterParserNumber();

    @Nullable T parse(@NotNull String str);
}

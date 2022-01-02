package su.nexmedia.engine.actions.parameter.parser;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IParameterValueParser<T> {

    ParameterParserString  STRING  = new ParameterParserString();
    ParameterParserBoolean BOOLEAN = new ParameterParserBoolean();
    ParameterParserNumber  NUMBER  = new ParameterParserNumber();

    @Nullable T parse(@NotNull String str);
}

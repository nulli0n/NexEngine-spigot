package su.nexmedia.engine.actions.parameter.defaults;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.actions.parameter.AbstractParameter;
import su.nexmedia.engine.actions.parameter.parser.IParameterValueParser;
import su.nexmedia.engine.actions.parameter.value.ParameterValueNumber;

@Deprecated // TODO Move in Paramter class as static constructor.
public class ParameterDefaultNumber extends AbstractParameter<ParameterValueNumber> {

    public ParameterDefaultNumber(@NotNull String key, @NotNull String flag) {
        super(key, flag);
    }

    @Override
    @NotNull
    public IParameterValueParser<ParameterValueNumber> getParser() {
        return IParameterValueParser.NUMBER;
    }
}

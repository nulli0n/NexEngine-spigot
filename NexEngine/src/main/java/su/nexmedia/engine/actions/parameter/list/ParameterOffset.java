package su.nexmedia.engine.actions.parameter.list;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.actions.parameter.AbstractParameter;
import su.nexmedia.engine.actions.parameter.ParameterId;
import su.nexmedia.engine.actions.parameter.parser.IParameterValueParser;
import su.nexmedia.engine.utils.StringUtil;

public class ParameterOffset extends AbstractParameter<double[]> {

    private final IParameterValueParser<double[]> parser;

    public ParameterOffset() {
        super(ParameterId.OFFSET, "offset");

        this.parser = (str) -> {
            String[] split = str.replace(" ", "").split(",");
            double x = StringUtil.getDouble(split[0], 0, true);
            double y = split.length >= 2 ? StringUtil.getDouble(split[1], 0, true) : 0;
            double z = split.length >= 2 ? StringUtil.getDouble(split[2], 0, true) : 0;
            return new double[]{x, y, z};
        };
    }

    @Override
    @NotNull
    public IParameterValueParser<double[]> getParser() {
        return this.parser;
    }
}

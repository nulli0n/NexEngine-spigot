package su.nexmedia.engine.actions.parameter.list;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.actions.parameter.AbstractParameter;
import su.nexmedia.engine.actions.parameter.ParameterId;
import su.nexmedia.engine.actions.parameter.parser.IParameterValueParser;
import su.nexmedia.engine.utils.LocationUtil;

public class ParameterLocation extends AbstractParameter<Location> {

    public ParameterLocation() {
        super(ParameterId.LOCATION, "location");
    }

    @Override
    @NotNull
    public IParameterValueParser<Location> getParser() {
        return LocationUtil::deserialize;
    }
}

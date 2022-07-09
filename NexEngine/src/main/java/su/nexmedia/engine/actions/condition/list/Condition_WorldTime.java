package su.nexmedia.engine.actions.condition.list;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.actions.condition.AbstractConditionValidator;
import su.nexmedia.engine.actions.condition.ConditionId;
import su.nexmedia.engine.actions.parameter.ParameterId;
import su.nexmedia.engine.actions.parameter.ParameterResult;
import su.nexmedia.engine.actions.parameter.value.ParameterValueNumber;

public class Condition_WorldTime extends AbstractConditionValidator {

    public Condition_WorldTime() {
        super(ConditionId.WORLD_TIME);
        this.registerParameter(ParameterId.AMOUNT);
        this.registerParameter(ParameterId.NAME);
    }

    @Override
    protected boolean validate(@NotNull Player player, @NotNull ParameterResult result) {
        String worldName = (String) result.getValue(ParameterId.NAME);
        World world = worldName != null ? ENGINE.getServer().getWorld(worldName) : null;

        ParameterValueNumber amount = (ParameterValueNumber) result.getValue(ParameterId.AMOUNT);
        if (amount == null) return true;

        long timeRequired = (long) amount.getValue(0);
        ParameterValueNumber.Operator oper = amount.getOperator();

        long timeWorld = world == null ? player.getWorld().getTime() : world.getTime();
        return oper.compare(timeWorld, timeRequired);
    }
}

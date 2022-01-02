package su.nexmedia.engine.actions.condition.list;

import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.actions.condition.AbstractConditionValidator;
import su.nexmedia.engine.actions.condition.ConditionId;
import su.nexmedia.engine.actions.parameter.ParameterId;
import su.nexmedia.engine.actions.parameter.ParameterResult;
import su.nexmedia.engine.actions.parameter.value.ParameterValueNumber;

import java.util.Set;
import java.util.function.Predicate;

public class Condition_WorldTime extends AbstractConditionValidator {

    public Condition_WorldTime() {
        super(ConditionId.WORLD_TIME);
        this.registerParameter(ParameterId.AMOUNT);
        this.registerParameter(ParameterId.NAME);
    }

    @Override
    public boolean mustHaveTarget() {
        return false;
    }

    @Override
    @Nullable
    protected Predicate<Entity> validate(@NotNull Entity executor, @NotNull Set<Entity> targets,
                                         @NotNull ParameterResult result) {
        String worldName = (String) result.getValue(ParameterId.NAME);
        World world = worldName != null ? ENGINE.getServer().getWorld(worldName) : null;

        ParameterValueNumber amount = (ParameterValueNumber) result.getValue(ParameterId.AMOUNT);
        if (amount == null) return null;

        long timeRequired = (long) amount.getValue(0);
        ParameterValueNumber.Operator oper = amount.getOperator();

        return target -> {
            long timeWorld = world == null ? target.getWorld().getTime() : world.getTime();
            return oper.compare(timeWorld, timeRequired);
        };
    }
}

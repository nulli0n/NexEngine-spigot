package su.nexmedia.engine.actions.condition.list;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.actions.condition.AbstractConditionValidator;
import su.nexmedia.engine.actions.condition.ConditionId;
import su.nexmedia.engine.actions.parameter.ParameterId;
import su.nexmedia.engine.actions.parameter.ParameterResult;

import java.util.Set;
import java.util.function.Predicate;

public class Condition_Permission extends AbstractConditionValidator {

    public Condition_Permission() {
        super(ConditionId.PERMISSION);
        this.registerParameter(ParameterId.NAME);
    }

    @Override
    public boolean mustHaveTarget() {
        return true;
    }

    @Override
    @Nullable
    protected Predicate<Entity> validate(@NotNull Entity executor, @NotNull Set<Entity> targets,
                                         @NotNull ParameterResult result) {
        String node = (String) result.getValue(ParameterId.NAME);
        if (node == null) return null;

        boolean negative = node.startsWith("-");
        return target -> target.hasPermission(node) == !negative;
    }
}

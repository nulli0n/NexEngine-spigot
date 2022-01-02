package su.nexmedia.engine.actions.condition.list;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.actions.condition.AbstractConditionValidator;
import su.nexmedia.engine.actions.condition.ConditionId;
import su.nexmedia.engine.actions.parameter.ParameterId;
import su.nexmedia.engine.actions.parameter.ParameterResult;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Condition_EntityType extends AbstractConditionValidator {

    public Condition_EntityType() {
        super(ConditionId.ENTITY_TYPE);
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

        String type = (String) result.getValue(ParameterId.TYPE);
        if (type == null || type.isEmpty()) return null;

        Set<String> types = Arrays.stream(type.split(",")).map(String::toUpperCase).collect(Collectors.toSet());
        return target -> types.contains(target.getType().name());
    }
}

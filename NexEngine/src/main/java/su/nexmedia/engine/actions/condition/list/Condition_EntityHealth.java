package su.nexmedia.engine.actions.condition.list;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.actions.condition.AbstractConditionValidator;
import su.nexmedia.engine.actions.condition.ConditionId;
import su.nexmedia.engine.actions.parameter.ParameterId;
import su.nexmedia.engine.actions.parameter.ParameterResult;
import su.nexmedia.engine.actions.parameter.value.ParameterValueNumber;
import su.nexmedia.engine.utils.EntityUtil;

import java.util.Set;
import java.util.function.Predicate;

public class Condition_EntityHealth extends AbstractConditionValidator {

    public Condition_EntityHealth() {
        super(ConditionId.ENTITY_HEALTH);
        this.registerParameter(ParameterId.AMOUNT);
    }

    @Override
    public boolean mustHaveTarget() {
        return true;
    }

    @Override
    @Nullable
    protected Predicate<Entity> validate(@NotNull Entity executor, @NotNull Set<Entity> targets, @NotNull ParameterResult result) {
        ParameterValueNumber amount = (ParameterValueNumber) result.getValue(ParameterId.AMOUNT);
        if (amount == null) return entity -> true;

        double healthRequired = amount.getValue(0D);
        boolean isPercent = amount.isPercent();
        ParameterValueNumber.Operator operator = amount.getOperator();

        return target -> {
            if (!(target instanceof LivingEntity livingEntity)) return false;

            double healthTarget = livingEntity.getHealth();
            double healthTargetMax = EntityUtil.getAttribute(livingEntity, Attribute.GENERIC_MAX_HEALTH);

            if (isPercent) {
                healthTarget = healthTarget / healthTargetMax * 100D;
            }

            return operator.compare(healthTarget, healthRequired);
        };
    }
}

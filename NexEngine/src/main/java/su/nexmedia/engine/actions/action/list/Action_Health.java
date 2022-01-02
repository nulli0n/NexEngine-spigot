package su.nexmedia.engine.actions.action.list;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.actions.action.AbstractActionExecutor;
import su.nexmedia.engine.actions.action.ActionId;
import su.nexmedia.engine.actions.parameter.ParameterId;
import su.nexmedia.engine.actions.parameter.ParameterResult;
import su.nexmedia.engine.actions.parameter.value.ParameterValueNumber;
import su.nexmedia.engine.utils.EntityUtil;

import java.util.Set;

public class Action_Health extends AbstractActionExecutor {

    public Action_Health() {
        super(ActionId.HEALTH);
        this.registerParameter(ParameterId.AMOUNT);
    }

    @Override
    public boolean mustHaveTarget() {
        return true;
    }

    @Override
    protected void execute(@NotNull Entity executor, @NotNull Set<Entity> targets, @NotNull ParameterResult result) {
        ParameterValueNumber value = (ParameterValueNumber) result.getValue(ParameterId.AMOUNT);
        if (value == null) return;

        double hp = value.getValue(0);
        if (hp == 0) return;

        boolean percent = value.isPercent();
        targets.forEach(target -> {
            if (!(target instanceof LivingEntity livingEntity)) return;

            double hp2 = hp;
            double maxHp = EntityUtil.getAttribute(livingEntity, Attribute.GENERIC_MAX_HEALTH);
            if (percent) {
                hp2 = maxHp * (hp / 100D);
            }

            livingEntity.setHealth(Math.min(livingEntity.getHealth() + hp2, maxHp));
        });
    }
}

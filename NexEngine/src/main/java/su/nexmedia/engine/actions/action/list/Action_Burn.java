package su.nexmedia.engine.actions.action.list;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.actions.action.AbstractActionExecutor;
import su.nexmedia.engine.actions.action.ActionId;
import su.nexmedia.engine.actions.parameter.ParameterId;
import su.nexmedia.engine.actions.parameter.ParameterResult;
import su.nexmedia.engine.actions.parameter.value.ParameterValueNumber;

import java.util.Set;

public class Action_Burn extends AbstractActionExecutor {

    public Action_Burn() {
        super(ActionId.BURN);
        this.registerParameter(ParameterId.DURATION);
    }

    @Override
    public boolean mustHaveTarget() {
        return true;
    }

    @Override
    protected void execute(@NotNull Entity executor, @NotNull Set<Entity> targets, @NotNull ParameterResult result) {
        ParameterValueNumber number = (ParameterValueNumber) result.getValue(ParameterId.DURATION);
        if (number == null) return;

        int duration = (int) number.getValue(0);
        if (duration <= 0) return;

        for (Entity target : targets) {
            target.setFireTicks(duration);
        }
    }
}

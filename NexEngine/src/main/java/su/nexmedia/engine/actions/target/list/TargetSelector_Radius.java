package su.nexmedia.engine.actions.target.list;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.actions.parameter.ParameterId;
import su.nexmedia.engine.actions.parameter.ParameterResult;
import su.nexmedia.engine.actions.target.AbstractTargetSelector;
import su.nexmedia.engine.actions.target.TargetSelectorId;

import java.util.Set;

public class TargetSelector_Radius extends AbstractTargetSelector {

    public TargetSelector_Radius() {
        super(TargetSelectorId.RADIUS);
        this.registerParameter(ParameterId.ALLOW_SELF);
        this.registerParameter(ParameterId.ATTACKABLE);
        this.registerParameter(ParameterId.DISTANCE);
    }

    @Override
    protected void validateTarget(@NotNull Entity executor, @NotNull Set<Entity> targets, @NotNull ParameterResult result) {
        Double distance = (Double) result.getValue(ParameterId.DISTANCE);
        if (distance == null) return;

        targets.addAll(executor.getNearbyEntities(distance, distance, distance));
    }
}

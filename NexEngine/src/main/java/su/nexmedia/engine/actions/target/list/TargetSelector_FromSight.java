package su.nexmedia.engine.actions.target.list;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.actions.parameter.ParameterId;
import su.nexmedia.engine.actions.parameter.ParameterResult;
import su.nexmedia.engine.actions.target.AbstractTargetSelector;
import su.nexmedia.engine.actions.target.TargetSelectorId;

import java.util.Set;

public class TargetSelector_FromSight extends AbstractTargetSelector {

    public TargetSelector_FromSight() {
        super(TargetSelectorId.FROM_SIGHT);
        this.registerParameter(ParameterId.ALLOW_SELF);
        this.registerParameter(ParameterId.ATTACKABLE);
        this.registerParameter(ParameterId.DISTANCE);
    }

    @Override
    protected void validateTarget(@NotNull Entity executor, @NotNull Set<Entity> targets, @NotNull ParameterResult result) {
        Double distance = (Double) result.getValue(ParameterId.DISTANCE);
        if (distance == null) return;

        Location start = executor.getLocation();
        if (executor instanceof LivingEntity livingEntity) {
            start = livingEntity.getEyeLocation();
        }

        Vector increase = start.getDirection();
        for (int counter = 0; counter < distance; counter++) {
            Location point = start.add(increase);

            Material wall = point.getBlock().getType();
            if (!point.getBlock().isEmpty() && wall.isSolid()) {
                break;
            }

            targets.addAll(executor.getWorld().getNearbyEntities(point, 1.25, 1.25, 1.25));
        }
    }
}

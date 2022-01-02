package su.nexmedia.engine.actions.action.list;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.actions.action.AbstractActionExecutor;
import su.nexmedia.engine.actions.action.ActionId;
import su.nexmedia.engine.actions.parameter.ParameterId;
import su.nexmedia.engine.actions.parameter.ParameterResult;
import su.nexmedia.engine.utils.EffectUtil;

import java.util.Set;

public class Action_ParticleSimple extends AbstractActionExecutor {

    public Action_ParticleSimple() {
        super(ActionId.PARTICLE_SIMPLE);
        this.registerParameter(ParameterId.TARGET);
        this.registerParameter(ParameterId.AMOUNT);
        this.registerParameter(ParameterId.SPEED);
        this.registerParameter(ParameterId.OFFSET);
    }

    @Override
    public boolean mustHaveTarget() {
        return true;
    }

    @Override
    protected void execute(@NotNull Entity exe, @NotNull Set<Entity> targets, @NotNull ParameterResult result) {
        String name = (String) result.getValue(ParameterId.NAME);
        if (name == null) return;

        double[] offset = (double[]) result.getValue(ParameterId.OFFSET);
        if (offset == null) offset = new double[3];

        int amount = result.getValueOrDefault(ParameterId.AMOUNT, 30);
        float speed = result.getValueOrDefault(ParameterId.SPEED, 0.1F);

        for (Entity target : targets) {
            Location loc = target.getLocation();
            if (target instanceof LivingEntity livingEntity) {
                loc = livingEntity.getEyeLocation();
            }

            EffectUtil.playEffect(loc, name, "", (float) offset[0], (float) offset[1], (float) offset[2], speed, amount);
        }
    }
}

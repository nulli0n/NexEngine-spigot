package su.nexmedia.engine.actions.target;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.actions.parameter.AbstractParametized;
import su.nexmedia.engine.actions.parameter.ParameterId;
import su.nexmedia.engine.actions.parameter.ParameterResult;
import su.nexmedia.engine.hooks.Hooks;

import java.util.Set;

public abstract class AbstractTargetSelector extends AbstractParametized {

    public AbstractTargetSelector(@NotNull String key) {
        super(key);
        this.registerParameter(ParameterId.NAME);
    }

    public final void select(@NotNull Entity executor, @NotNull Set<Entity> targets, @NotNull String str) {
        ParameterResult result = this.getParameterResult(str);
        this.validateTarget(executor, targets, result);
        this.validateDefaults(executor, targets, result);
    }

    protected abstract void validateTarget(@NotNull Entity executor, @NotNull Set<Entity> targets, @NotNull ParameterResult result);

    private void validateDefaults(@NotNull Entity executor, @NotNull Set<Entity> targets, @NotNull ParameterResult result) {
        if (executor instanceof Projectile projectile && projectile.getShooter() instanceof Entity shooter) {
            executor = shooter;
        }

        final Entity executor2 = executor;

        Boolean allowSelf = (Boolean) result.getValue(ParameterId.ALLOW_SELF);
        if (allowSelf != null && !allowSelf) {
            targets.remove(executor2);
        }

        Boolean attackable = (Boolean) result.getValue(ParameterId.ATTACKABLE);
        if (attackable != null) {
            targets.removeIf(target -> {
                boolean canDamage = Hooks.canFights(executor2, target);
                return canDamage != attackable;
            });
        }
    }
}

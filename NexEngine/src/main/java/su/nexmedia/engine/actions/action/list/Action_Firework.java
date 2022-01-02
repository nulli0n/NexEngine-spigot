package su.nexmedia.engine.actions.action.list;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.actions.action.AbstractActionExecutor;
import su.nexmedia.engine.actions.action.ActionId;
import su.nexmedia.engine.actions.parameter.ParameterResult;
import su.nexmedia.engine.utils.EntityUtil;

import java.util.Set;

public class Action_Firework extends AbstractActionExecutor {

    public Action_Firework() {
        super(ActionId.FIREWORK);
    }

    @Override
    public boolean mustHaveTarget() {
        return false;
    }

    @Override
    protected void execute(@NotNull Entity executor, @NotNull Set<Entity> targets, @NotNull ParameterResult result) {
        for (Entity target : targets) {
            EntityUtil.spawnRandomFirework(target.getLocation());
        }
    }
}

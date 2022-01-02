package su.nexmedia.engine.actions.action.list;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.actions.action.AbstractActionExecutor;
import su.nexmedia.engine.actions.action.ActionId;
import su.nexmedia.engine.actions.parameter.ParameterResult;

import java.util.Set;

public class Action_Lightning extends AbstractActionExecutor {

    public Action_Lightning() {
        super(ActionId.LIGHTNING);
    }

    @Override
    public boolean mustHaveTarget() {
        return true;
    }

    @Override
    protected void execute(@NotNull Entity executor, @NotNull Set<Entity> targets, @NotNull ParameterResult result) {
        for (Entity target : targets) {
            target.getWorld().strikeLightning(target.getLocation());
        }
    }
}

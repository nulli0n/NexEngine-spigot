package su.nexmedia.engine.actions.target.list;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.actions.parameter.ParameterResult;
import su.nexmedia.engine.actions.target.AbstractTargetSelector;
import su.nexmedia.engine.actions.target.TargetSelectorId;

import java.util.Set;

public class TargetSelector_Executor extends AbstractTargetSelector {

    public TargetSelector_Executor() {
        super(TargetSelectorId.EXECUTOR);
    }

    @Override
    protected void validateTarget(@NotNull Entity executor, @NotNull Set<Entity> targets, @NotNull ParameterResult result) {
        targets.add(executor);
    }
}

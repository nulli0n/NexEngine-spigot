package su.nexmedia.engine.actions.action.list;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.actions.action.AbstractActionExecutor;
import su.nexmedia.engine.actions.action.ActionId;
import su.nexmedia.engine.actions.parameter.ParameterId;
import su.nexmedia.engine.actions.parameter.ParameterResult;

import java.util.Set;

public class Action_Teleport extends AbstractActionExecutor {

    public Action_Teleport() {
        super(ActionId.TELEPORT);
        this.registerParameter(ParameterId.LOCATION);
    }

    @Override
    public boolean mustHaveTarget() {
        return true;
    }

    @Override
    protected void execute(@NotNull Entity executor, @NotNull Set<Entity> targets, @NotNull ParameterResult result) {
        Location locExe = (Location) result.getValue(ParameterId.LOCATION);
        if (locExe == null) return;

        World world = locExe.getWorld();
        if (world == null) return;

        for (Entity target : targets) {
            target.teleport(locExe);
        }
    }
}

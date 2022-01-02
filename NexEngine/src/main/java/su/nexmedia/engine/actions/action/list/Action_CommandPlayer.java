package su.nexmedia.engine.actions.action.list;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.actions.action.AbstractActionExecutor;
import su.nexmedia.engine.actions.action.ActionId;
import su.nexmedia.engine.actions.parameter.ParameterId;
import su.nexmedia.engine.actions.parameter.ParameterResult;

import java.util.Set;

public class Action_CommandPlayer extends AbstractActionExecutor {

    public Action_CommandPlayer() {
        super(ActionId.COMMAND_PLAYER);
        this.registerParameter(ParameterId.MESSAGE);
    }

    @Override
    public boolean mustHaveTarget() {
        return false;
    }

    @Override
    protected void execute(@NotNull Entity executor, @NotNull Set<Entity> targets, @NotNull ParameterResult result) {
        if (!(executor instanceof Player player)) return;

        String text = (String) result.getValue(ParameterId.MESSAGE);
        if (text == null) return;

        text = text.replace(PLACEHOLDER_EXECUTOR_NAME, player.getName());

        if (!targets.isEmpty()) {
            for (Entity target : targets) {
                String text2 = text.replace(PLACEHOLDER_TARGET_NAME, target.getName());
                player.performCommand(text2);
            }
        }
        else {
            player.performCommand(text);
        }
    }
}

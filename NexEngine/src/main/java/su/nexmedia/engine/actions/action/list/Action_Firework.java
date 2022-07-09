package su.nexmedia.engine.actions.action.list;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.actions.action.AbstractActionExecutor;
import su.nexmedia.engine.actions.action.ActionId;
import su.nexmedia.engine.actions.parameter.ParameterResult;
import su.nexmedia.engine.utils.EntityUtil;

public class Action_Firework extends AbstractActionExecutor {

    public Action_Firework() {
        super(ActionId.FIREWORK);
    }

    @Override
    protected void execute(@NotNull Player player, @NotNull ParameterResult result) {
        EntityUtil.spawnRandomFirework(player.getLocation());
    }
}

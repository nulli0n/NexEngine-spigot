package su.nexmedia.engine.actions.action.list;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.actions.action.AbstractActionExecutor;
import su.nexmedia.engine.actions.action.ActionId;
import su.nexmedia.engine.actions.parameter.ParameterId;
import su.nexmedia.engine.actions.parameter.ParameterResult;

public class Action_Goto extends AbstractActionExecutor {

    public Action_Goto() {
        super(ActionId.GOTO);
        this.registerParameter(ParameterId.NAME);
    }

    @Override
    protected void execute(@NotNull Player player, @NotNull ParameterResult result) {

    }
}

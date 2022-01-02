package su.nexmedia.engine.actions.action.list;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.actions.action.AbstractActionExecutor;
import su.nexmedia.engine.actions.action.ActionId;
import su.nexmedia.engine.actions.parameter.ParameterId;
import su.nexmedia.engine.actions.parameter.ParameterResult;
import su.nexmedia.engine.utils.MessageUtil;

import java.util.Set;

public class Action_MessageChat extends AbstractActionExecutor {

    public Action_MessageChat() {
        super(ActionId.MESSAGE_CHAT);
        this.registerParameter(ParameterId.MESSAGE);
    }

    @Override
    public boolean mustHaveTarget() {
        return true;
    }

    @Override
    protected void execute(@NotNull Entity executor, @NotNull Set<Entity> targets, @NotNull ParameterResult result) {
        String text = (String) result.getValue(ParameterId.MESSAGE);
        if (text == null) return;

        text = text.replace(PLACEHOLDER_EXECUTOR_NAME, executor.getName());

        for (Entity target : targets) {
            MessageUtil.sendWithJSON(target, text.replace(PLACEHOLDER_TARGET_NAME, target.getName()));
        }
    }
}

package su.nexmedia.engine.actions.action.list;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.actions.action.AbstractActionExecutor;
import su.nexmedia.engine.actions.action.ActionId;
import su.nexmedia.engine.actions.parameter.ParameterId;
import su.nexmedia.engine.actions.parameter.ParameterResult;
import su.nexmedia.engine.hooks.Hooks;
import su.nexmedia.engine.utils.MessageUtil;

import java.util.Set;

public class Action_MessageActionBar extends AbstractActionExecutor {

    public Action_MessageActionBar() {
        super(ActionId.MESSAGE_ACTION_BAR);
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
            if (target instanceof Player player) {
                String text2 = text.replace(PLACEHOLDER_TARGET_NAME, target.getName());
                if (Hooks.hasPlaceholderAPI()) {
                    text2 = PlaceholderAPI.setPlaceholders(player, text2);
                }
                MessageUtil.sendActionBar(player, text2);
            }
        }
    }
}

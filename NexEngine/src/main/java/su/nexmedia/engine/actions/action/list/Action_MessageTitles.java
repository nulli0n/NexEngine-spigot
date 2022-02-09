package su.nexmedia.engine.actions.action.list;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.actions.action.AbstractActionExecutor;
import su.nexmedia.engine.actions.action.ActionId;
import su.nexmedia.engine.actions.parameter.ParameterId;
import su.nexmedia.engine.actions.parameter.ParameterResult;
import su.nexmedia.engine.actions.parameter.value.ParameterValueNumber;

import java.util.Set;

public class Action_MessageTitles extends AbstractActionExecutor {

    public Action_MessageTitles() {
        super(ActionId.MESSAGE_TITLES);
        this.registerParameter(ParameterId.TITLES_TITLE);
        this.registerParameter(ParameterId.TITLES_SUBTITLE);
        this.registerParameter(ParameterId.TITLES_FADE_IN);
        this.registerParameter(ParameterId.TITLES_FADE_OUT);
        this.registerParameter(ParameterId.TITLES_STAY);
    }

    @Override
    public boolean mustHaveTarget() {
        return true;
    }

    @Override
    protected void execute(@NotNull Entity executor, @NotNull Set<Entity> targets, @NotNull ParameterResult result) {
        String title = (String) result.getValue(ParameterId.TITLES_TITLE);
        String subtitle = (String) result.getValue(ParameterId.TITLES_SUBTITLE);
        if (title == null && subtitle == null) return;

        ParameterValueNumber numberIn = (ParameterValueNumber) result.getValue(ParameterId.TITLES_FADE_IN);
        ParameterValueNumber numberStay = (ParameterValueNumber) result.getValue(ParameterId.TITLES_STAY);
        ParameterValueNumber numberOut = (ParameterValueNumber) result.getValue(ParameterId.TITLES_FADE_OUT);
        if (numberIn == null) return;
        if (numberStay == null) return;
        if (numberOut == null) return;

        int fadeIn = (int) numberIn.getValue(0);
        int stay = (int) numberStay.getValue(0);
        int fadeOut = (int) numberOut.getValue(0);

        for (Entity target : targets) {
            if (!(target instanceof Player player)) continue;

            String title2 = title == null ? "" : title
                .replace(PLACEHOLDER_EXECUTOR_NAME, executor.getName())
                .replace(PLACEHOLDER_TARGET_NAME, target.getName());
            String subtitle2 = subtitle == null ? "" : subtitle
                .replace(PLACEHOLDER_EXECUTOR_NAME, executor.getName())
                .replace(PLACEHOLDER_TARGET_NAME, target.getName());

            player.sendTitle(title2, subtitle2, fadeIn, stay, fadeOut);
        }
    }
}

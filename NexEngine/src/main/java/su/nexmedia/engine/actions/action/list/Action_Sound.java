package su.nexmedia.engine.actions.action.list;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.actions.action.AbstractActionExecutor;
import su.nexmedia.engine.actions.action.ActionId;
import su.nexmedia.engine.actions.parameter.ParameterId;
import su.nexmedia.engine.actions.parameter.ParameterResult;
import su.nexmedia.engine.utils.CollectionsUtil;
import su.nexmedia.engine.utils.MessageUtil;

public class Action_Sound extends AbstractActionExecutor {

    public Action_Sound() {
        super(ActionId.SOUND);
        this.registerParameter(ParameterId.NAME);
    }

    @Override
    protected void execute(@NotNull Player player, @NotNull ParameterResult result) {
        String name = (String) result.getValue(ParameterId.NAME);
        if (name == null) return;

        Sound sound = CollectionsUtil.getEnum(name, Sound.class);
        if (sound == null) return;

        MessageUtil.sound(player, sound);
    }
}

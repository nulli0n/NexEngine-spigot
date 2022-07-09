package su.nexmedia.engine.actions.action.list;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.actions.action.AbstractActionExecutor;
import su.nexmedia.engine.actions.action.ActionId;
import su.nexmedia.engine.actions.parameter.ParameterId;
import su.nexmedia.engine.actions.parameter.ParameterResult;
import su.nexmedia.engine.actions.parameter.value.ParameterValueNumber;

public class Action_Saturation extends AbstractActionExecutor {

    public Action_Saturation() {
        super(ActionId.SATURATION);
        this.registerParameter(ParameterId.AMOUNT);
    }

    @Override
    protected void execute(@NotNull Player player, @NotNull ParameterResult result) {
        ParameterValueNumber value = (ParameterValueNumber) result.getValue(ParameterId.AMOUNT);
        if (value == null) return;

        double amount = value.getValue(0);
        if (amount == 0) return;

        boolean percent = value.isPercent();

        double amount2 = amount;
        double has = player.getSaturation();
        if (percent) {
            amount2 = has * (amount / 100D);
        }

        player.setSaturation((float) (has + amount2));
    }
}

package su.nexmedia.engine.actions.condition.list;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.actions.condition.AbstractConditionValidator;
import su.nexmedia.engine.actions.condition.ConditionId;
import su.nexmedia.engine.actions.parameter.ParameterId;
import su.nexmedia.engine.actions.parameter.ParameterResult;

public class Condition_Permission extends AbstractConditionValidator {

    public Condition_Permission() {
        super(ConditionId.PERMISSION);
        this.registerParameter(ParameterId.NAME);
    }

    @Override
    protected boolean validate(@NotNull Player player, @NotNull ParameterResult result) {
        String node = (String) result.getValue(ParameterId.NAME);
        if (node == null) return true;

        boolean negative = node.startsWith("-");
        return player.hasPermission(node) == !negative;
    }
}

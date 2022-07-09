package su.nexmedia.engine.actions.condition.list;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.actions.condition.AbstractConditionValidator;
import su.nexmedia.engine.actions.condition.ConditionId;
import su.nexmedia.engine.actions.parameter.ParameterId;
import su.nexmedia.engine.actions.parameter.ParameterResult;
import su.nexmedia.engine.actions.parameter.value.ParameterValueNumber;
import su.nexmedia.engine.hooks.external.VaultHook;

public class Condition_EconomyBalance extends AbstractConditionValidator {

    public Condition_EconomyBalance() {
        super(ConditionId.ECONOMY_BALANCE);
        this.registerParameter(ParameterId.AMOUNT);
    }

    @Override
    protected boolean validate(@NotNull Player player, @NotNull ParameterResult result) {
        if (!VaultHook.hasEconomy()) return true;

        ParameterValueNumber amount = (ParameterValueNumber) result.getValue(ParameterId.AMOUNT);
        if (amount == null) return true;

        double moneyRequired = amount.getValue(0D);
        if (moneyRequired <= 0D) return true;

        ParameterValueNumber.Operator operator = amount.getOperator();

        double moneyBalance = VaultHook.getBalance(player);
        return operator.compare(moneyBalance, moneyRequired);
    }
}

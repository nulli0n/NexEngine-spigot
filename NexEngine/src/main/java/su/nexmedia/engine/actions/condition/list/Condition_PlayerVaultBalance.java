package su.nexmedia.engine.actions.condition.list;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.actions.condition.AbstractConditionValidator;
import su.nexmedia.engine.actions.condition.ConditionId;
import su.nexmedia.engine.actions.parameter.ParameterId;
import su.nexmedia.engine.actions.parameter.ParameterResult;
import su.nexmedia.engine.actions.parameter.value.ParameterValueNumber;
import su.nexmedia.engine.hooks.external.VaultHook;

import java.util.Set;
import java.util.function.Predicate;

public class Condition_PlayerVaultBalance extends AbstractConditionValidator {

    public Condition_PlayerVaultBalance() {
        super(ConditionId.PLAYER_VAULT_BALANCE);
        this.registerParameter(ParameterId.AMOUNT);
    }

    @Override
    public boolean mustHaveTarget() {
        return true;
    }

    @Override
    @Nullable
    protected Predicate<Entity> validate(@NotNull Entity executor, @NotNull Set<Entity> targets,
                                         @NotNull ParameterResult result) {
        if (!VaultHook.hasEconomy()) return null;

        ParameterValueNumber amount = (ParameterValueNumber) result.getValue(ParameterId.AMOUNT);
        if (amount == null) return null;

        double moneyRequired = amount.getValue(0D);
        if (moneyRequired <= 0D) return null;

        ParameterValueNumber.Operator operator = amount.getOperator();

        return target -> {
            if (target instanceof Player player) {
                double moneyBalance = VaultHook.getBalance(player);
                return operator.compare(moneyBalance, moneyRequired);
            }
            return false;
        };
    }
}

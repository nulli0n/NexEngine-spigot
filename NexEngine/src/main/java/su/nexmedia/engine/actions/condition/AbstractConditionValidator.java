package su.nexmedia.engine.actions.condition;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.actions.parameter.AbstractParametized;
import su.nexmedia.engine.actions.parameter.ParameterId;
import su.nexmedia.engine.actions.parameter.ParameterResult;
import su.nexmedia.engine.utils.Constants;
import su.nexmedia.engine.actions.ActionManipulator;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public abstract class AbstractConditionValidator extends AbstractParametized {

    public AbstractConditionValidator(@NotNull String key) {
        super(key);
        this.registerParameter(ParameterId.TARGET);
        this.registerParameter(ParameterId.MESSAGE);
        this.registerParameter(ParameterId.FILTER);
    }

    public abstract boolean mustHaveTarget();

    @Nullable
    protected abstract Predicate<Entity> validate(@NotNull Entity executor, @NotNull Set<Entity> targets,
                                                  @NotNull ParameterResult result);

    public final boolean process(@NotNull Entity executor, @NotNull Map<String, Set<Entity>> targetMap, @NotNull String str) {
        return this.process(executor, targetMap, str, null);
    }

    public final boolean process(@NotNull Entity executor, @NotNull Map<String, Set<Entity>> targetMap,
                                 @NotNull String str, @Nullable ActionManipulator manipulator) {

        ParameterResult result = this.getParameterResult(str);

        // Check for empty map because it may contain default executor in some cases
        String[] targetSelectorNames = result.getValueOrDefault(ParameterId.TARGET, "").split(",");
        Set<Entity> targets = new HashSet<>(targetMap.getOrDefault(Constants.DEFAULT, Collections.emptySet()));
        for (String targetSelectorName : targetSelectorNames) {
            targets.addAll(targetMap.getOrDefault(targetSelectorName.toLowerCase(), Collections.emptySet()));
        }

        // Fine target entities.
        if (targets.isEmpty()) {
            // If there is no default target and the condition must have target, then action should be interrupted.
            if (this.mustHaveTarget()) {
                ENGINE.warn("Invalid or no Target(s) specified for condition: '" + str + "' !");
                return false;
            }
            // If target param is not must have, then we add executor to be able to test condition predicates.
            else {
                targets.add(executor);
            }
        }

        boolean isFilter = result.getValueOrDefault(ParameterId.FILTER, false);
        Predicate<Entity> predicate = this.validate(executor, targets, result);
        if (predicate == null) {
            ENGINE.error("Could not validate condition: '" + str + "'. Skipping...");
            return true;
        }

        if (isFilter) {
            targets.removeIf(target -> Predicate.not(predicate).test(target));
        }
        else {
            if (!targets.stream().allMatch(predicate)) {
                String message = (String) result.getValue(ParameterId.MESSAGE);
                if (message != null) executor.sendMessage(message);
                return false;
            }
        }

        // Remove filtered targets from the original target map
        // to avoid them being applied on actions.
        for (String targetId : targetSelectorNames) {
            targetMap.getOrDefault(targetId.toLowerCase(), new HashSet<>()).removeIf(target2 -> !targets.contains(target2));
        }
        return true;
    }
}

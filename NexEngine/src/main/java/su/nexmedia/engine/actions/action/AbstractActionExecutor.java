package su.nexmedia.engine.actions.action;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.actions.parameter.AbstractParametized;
import su.nexmedia.engine.actions.parameter.ParameterId;
import su.nexmedia.engine.actions.parameter.ParameterResult;
import su.nexmedia.engine.actions.parameter.value.ParameterValueNumber;
import su.nexmedia.engine.actions.ActionManipulator;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class AbstractActionExecutor extends AbstractParametized {

    protected final static String PLACEHOLDER_EXECUTOR_NAME = "%executor_name%";
    protected final static String PLACEHOLDER_TARGET_NAME   = "%target_name%";

    public AbstractActionExecutor(@NotNull String key) {
        super(key);
        this.registerParameter(ParameterId.TARGET);
        this.registerParameter(ParameterId.DELAY);
    }

    public abstract boolean mustHaveTarget();

    protected abstract void execute(@NotNull Entity executor, @NotNull Set<Entity> targets, @NotNull ParameterResult result);

    public final void process(@NotNull Entity executor, @NotNull Map<String, Set<Entity>> targetMap, @NotNull String str,
                              @NotNull ActionManipulator manipulator) {

        ParameterResult result = this.getParameterResult(str);
        ParameterValueNumber numberDelay = (ParameterValueNumber) result.getValue(ParameterId.DELAY);

        if (str.contains(FLAG_NO_DELAY)) {
            str = str.replace(FLAG_NO_DELAY, "");
        }
        else if (numberDelay != null) {
            int delay = (int) numberDelay.getValue(0);
            final String strDelay = str;

            if (delay > 0) {
                ENGINE.getServer().getScheduler().runTaskLater(ENGINE, () -> {
                    this.process(executor, targetMap, strDelay + FLAG_NO_DELAY, manipulator);
                }, delay);
                return;
            }
        }

        String[] targetSelectorNames = result.getValueOrDefault(ParameterId.TARGET, "").split(",");
        Set<Entity> targets = new HashSet<>();
        for (String targetSelectorName : targetSelectorNames) {
            targets.addAll(targetMap.getOrDefault(targetSelectorName.toLowerCase(), Collections.emptySet()));
        }

        if (this.mustHaveTarget() && targets.isEmpty()) {
            ENGINE.warn("Invalid or no Target(s) specified for action: '" + str + "' !");
            return;
        }

        if (this.getName().equalsIgnoreCase(ActionId.GOTO)) {
            String sectionId = (String) result.getValue(ParameterId.NAME);
            if (sectionId == null) return;

            manipulator.process(executor, sectionId);
            return;
        }

        this.execute(executor, targets, result);
    }
}

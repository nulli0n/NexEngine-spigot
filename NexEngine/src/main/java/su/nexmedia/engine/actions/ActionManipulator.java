package su.nexmedia.engine.actions;

import com.google.common.collect.Sets;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexEngine;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.actions.action.AbstractActionExecutor;
import su.nexmedia.engine.actions.condition.AbstractConditionValidator;
import su.nexmedia.engine.actions.parameter.ParameterId;
import su.nexmedia.engine.actions.parameter.ParameterResult;
import su.nexmedia.engine.actions.target.AbstractTargetSelector;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.hooks.Hooks;
import su.nexmedia.engine.utils.Constants;

import java.util.*;
import java.util.function.UnaryOperator;

public class ActionManipulator {

    private static final NexEngine ENGINE = NexEngine.get();

    private final Map<String, ActionSection> actions;

    public ActionManipulator() {
        this.actions = new LinkedHashMap<>();
    }

    public ActionManipulator(@NotNull ActionManipulator copy) {
        this();
        for (Map.Entry<String, ActionSection> en : copy.getActions().entrySet()) {
            this.actions.put(en.getKey(), new ActionSection(en.getValue()));
        }
    }

    public ActionManipulator(@NotNull JYML cfg, @NotNull String path) {
        this();
        for (String id : cfg.getSection(path)) {
            String path2 = path + "." + id + ".";

            List<String> targetSelectors = cfg.getStringList(path2 + "Target_Selectors");
            List<String> conditionList = cfg.getStringList(path2 + "Conditions.List");
            String conditionActionOnFail = cfg.getString(path2 + "Conditions.Fail_Actions", "");
            List<String> actionExecutors = cfg.getStringList(path2 + "Action_Executors");

            ActionSection engine = new ActionSection(targetSelectors, conditionList, conditionActionOnFail, actionExecutors);
            this.actions.put(id.toLowerCase(), engine);
        }
    }

    public static boolean processConditions(@NotNull NexPlugin<?> plugin, @NotNull Entity exec, @NotNull List<String> condis) {
        return processConditions(plugin, exec, condis, Collections.emptyMap());
    }

    public static boolean processConditions(@NotNull NexPlugin<?> plugin, @NotNull Entity exec, @NotNull List<String> condis, @NotNull Map<String, Set<Entity>> targetMap2) {

        Map<String, Set<@NotNull Entity>> targetMap = new HashMap<>();
        targetMap.put(Constants.DEFAULT, Sets.newHashSet(exec));
        targetMap2.forEach((fromKey, fromVal) -> {
            targetMap.merge(fromKey, fromVal, (old, now) -> {
                Set<@NotNull Entity> set = new HashSet<>(old);
                set.addAll(now);
                return set;
            });
        });

        Player p = null;
        if (Hooks.hasPlaceholderAPI() && exec instanceof Player) {
            p = (Player) exec;
        }

        // Check conditions
        for (String condition : condis) {
            if (p != null) {
                condition = PlaceholderAPI.setPlaceholders(p, condition);
            }

            String key = condition.split(" ")[0].replace("[", "").replace("]", "");
            AbstractConditionValidator validator = plugin.getActionsManager().getConditionValidator(key);
            if (validator == null) {
                plugin.error("Invalid condition validator '" + key + "' in '" + condition + "' !");
                continue;
            }

            if (!validator.process(exec, targetMap, condition)) {
                return false;
            }
        }
        return true;
    }

    @NotNull
    public ActionManipulator replace(@NotNull UnaryOperator<String> func) {
        ActionManipulator manipulatorCopy = new ActionManipulator(this);

        for (ActionSection copyEngine : manipulatorCopy.getActions().values()) {
            copyEngine.getActionExecutors().replaceAll(func);
            copyEngine.getConditions().replaceAll(func);
            copyEngine.getTargetSelectors().replaceAll(func);
        }

        return manipulatorCopy;
    }

    @NotNull
    public Map<String, ActionSection> getActions() {
        return this.actions;
    }

    public void process(@NotNull Entity exec) {
        this.process(exec, Collections.emptyMap());
    }

    public void process(@NotNull Entity exec, @NotNull Map<String, Set<Entity>> targetMap2) {
        if (this.actions.isEmpty()) return;
        String id = new ArrayList<>(this.actions.keySet()).get(0);
        this.process(exec, id, targetMap2);
    }

    public void process(@NotNull Entity exec, @NotNull String id) {
        this.process(exec, id, Collections.emptyMap());
    }

    public void process(@NotNull Entity exec, @NotNull String id, @NotNull Map<String, Set<Entity>> targetMap2) {
        ActionSection ae = this.actions.get(id.toLowerCase());
        if (ae == null)
            return;

        Map<String, Set<Entity>> targetMap = new HashMap<>();
        targetMap2.forEach((fromKey, fromVal) -> {
            targetMap.merge(fromKey, fromVal, (old, now) -> {
                Set<Entity> set = new HashSet<>(old);
                set.addAll(now);
                return set;
            });
        });

        Player p = null;
        if (Hooks.hasPlugin(Hooks.PLACEHOLDER_API) && exec instanceof Player) {
            p = (Player) exec;
        }

        // Precache target selectors for actions
        for (String selector : ae.getTargetSelectors()) {
            if (p != null)
                selector = PlaceholderAPI.setPlaceholders(p, selector);

            String selectorKey = selector.split(" ")[0].replace("[", "").replace("]", "");
            AbstractTargetSelector targetSelector = ENGINE.getActionsManager().getTargetSelector(selectorKey);
            if (targetSelector == null) {
                ENGINE.error("Invalid target selector '" + selectorKey + "' in '" + selector + "' !");
                continue;
            }
            ParameterResult result = targetSelector.getParameterResult(selector);
            String targetId = result.getValueOrDefault(ParameterId.NAME, Constants.DEFAULT);
            Set<Entity> targets = new HashSet<>();

            targetSelector.select(exec, targets, selector);

            // We use merge instead of single set to prevent targets from
            // different selectors being removed due to different params.
            targetMap.merge(targetId, targets, (old, now) -> {
                Set<Entity> set = new HashSet<>(old);
                set.addAll(now);
                return set;
            });
        }

        // Check conditions
        for (String condition : ae.getConditions()) {
            if (p != null) {
                condition = PlaceholderAPI.setPlaceholders(p, condition);
            }

            String key = condition.split(" ")[0].replace("[", "").replace("]", "");
            AbstractConditionValidator validator = ENGINE.getActionsManager().getConditionValidator(key);
            if (validator == null) {
                ENGINE.error("Invalid condition validator '" + key + "' in '" + condition + "' !");
                continue;
            }

            if (!validator.process(exec, targetMap, condition, this)) {
                this.process(exec, ae.getConditionFailActions());
                return;
            }
        }

        // Run actions
        for (String action : ae.getActionExecutors()) {
            if (p != null) {
                action = PlaceholderAPI.setPlaceholders(p, action);
            }

            String key = action.split(" ")[0].replace("[", "").replace("]", "");
            AbstractActionExecutor executor = ENGINE.getActionsManager().getActionExecutor(key);
            if (executor == null) {
                ENGINE.error("Invalid action executor '" + key + "' in '" + action + "' !");
                continue;
            }

            executor.process(exec, targetMap, action, this);
        }
    }
}

package su.nexmedia.engine.actions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.NexEngine;
import su.nexmedia.engine.actions.action.AbstractActionExecutor;
import su.nexmedia.engine.actions.action.list.*;
import su.nexmedia.engine.actions.condition.AbstractConditionValidator;
import su.nexmedia.engine.actions.condition.list.Condition_EconomyBalance;
import su.nexmedia.engine.actions.condition.list.Condition_Health;
import su.nexmedia.engine.actions.condition.list.Condition_Permission;
import su.nexmedia.engine.actions.condition.list.Condition_WorldTime;
import su.nexmedia.engine.actions.parameter.AbstractParameter;
import su.nexmedia.engine.actions.parameter.ParameterId;
import su.nexmedia.engine.actions.parameter.defaults.ParameterDefaultNumber;
import su.nexmedia.engine.actions.parameter.defaults.ParameterDefaultString;
import su.nexmedia.engine.api.manager.AbstractManager;

import java.util.HashMap;
import java.util.Map;

public class ActionsManager extends AbstractManager<NexEngine> {

    private Map<String, AbstractParameter<?>>       parameterMap;
    private Map<String, AbstractConditionValidator> conditionValidatorMap;
    private Map<String, AbstractActionExecutor>     actionExecutorMap;

    public ActionsManager(@NotNull NexEngine plugin) {
        super(plugin);
    }

    @Override
    protected void onLoad() {
        this.parameterMap = new HashMap<>();
        this.conditionValidatorMap = new HashMap<>();
        this.actionExecutorMap = new HashMap<>();

        this.addDefaults();
    }

    @Override
    protected void onShutdown() {
        this.parameterMap.clear();
        this.conditionValidatorMap.clear();
        this.actionExecutorMap.clear();
    }

    private void addDefaults() {
        this.registerParameter(new ParameterDefaultNumber(ParameterId.AMOUNT, "amount"));
        this.registerParameter(new ParameterDefaultNumber(ParameterId.DELAY, "delay"));
        this.registerParameter(new ParameterDefaultNumber(ParameterId.DISTANCE, "distance"));
        this.registerParameter(new ParameterDefaultNumber(ParameterId.DURATION, "duration"));
        this.registerParameter(new ParameterDefaultString(ParameterId.MESSAGE, "message"));
        this.registerParameter(new ParameterDefaultString(ParameterId.NAME, "name"));
        this.registerParameter(new ParameterDefaultString(ParameterId.TYPE, "type"));
        this.registerParameter(new ParameterDefaultNumber(ParameterId.SPEED, "speed"));
        this.registerParameter(new ParameterDefaultString(ParameterId.TITLES_TITLE, "title"));
        this.registerParameter(new ParameterDefaultString(ParameterId.TITLES_SUBTITLE, "subtitle"));
        this.registerParameter(new ParameterDefaultNumber(ParameterId.TITLES_FADE_IN, "fadeIn"));
        this.registerParameter(new ParameterDefaultNumber(ParameterId.TITLES_STAY, "stay"));
        this.registerParameter(new ParameterDefaultNumber(ParameterId.TITLES_FADE_OUT, "fadeOut"));

        this.registerConditionValidator(new Condition_Health());
        this.registerConditionValidator(new Condition_Permission());
        this.registerConditionValidator(new Condition_EconomyBalance());
        this.registerConditionValidator(new Condition_WorldTime());

        this.registerActionExecutor(new Action_Burn());
        this.registerActionExecutor(new Action_CommandConsole());
        this.registerActionExecutor(new Action_CommandPlayer());
        this.registerActionExecutor(new Action_Firework());
        this.registerActionExecutor(new Action_Goto());
        this.registerActionExecutor(new Action_Health());
        this.registerActionExecutor(new Action_Hunger());
        this.registerActionExecutor(new Action_MessageActionBar());
        this.registerActionExecutor(new Action_MessageChat());
        this.registerActionExecutor(new Action_MessageTitles());
        this.registerActionExecutor(new Action_Lightning());
        this.registerActionExecutor(new Action_Potion());
        this.registerActionExecutor(new Action_Saturation());
        this.registerActionExecutor(new Action_Sound());
    }

    public void registerParameter(@NotNull AbstractParameter<?> parameter) {
        if (this.parameterMap.put(parameter.getName(), parameter) != null) {
            plugin.info("[Actions Engine] Replaced registered param '" + parameter.getName() + "' with a new one.");
        }
    }

    public void registerConditionValidator(@NotNull AbstractConditionValidator conditionValidator) {
        if (this.conditionValidatorMap.put(conditionValidator.getName(), conditionValidator) != null) {
            plugin.info("[Actions Engine] Replaced registered condition validator '" + conditionValidator.getName() + "' with a new one.");
        }
    }

    public void registerActionExecutor(@NotNull AbstractActionExecutor actionExecutor) {
        if (this.actionExecutorMap.put(actionExecutor.getName(), actionExecutor) != null) {
            plugin.info("[Actions Engine] Replaced registered action executoe: '" + actionExecutor.getName() + "' with a new one.");
        }
    }

    @Nullable
    public AbstractParameter<?> getParameter(@NotNull String id) {
        return this.parameterMap.get(id.toLowerCase());
    }

    @Nullable
    public AbstractConditionValidator getConditionValidator(@NotNull String id) {
        return this.conditionValidatorMap.get(id.toLowerCase());
    }

    @Nullable
    public AbstractActionExecutor getActionExecutor(@NotNull String id) {
        return this.actionExecutorMap.get(id.toLowerCase());
    }

    /*
    @Nullable
    public ParameterDefaultNumber getParameterNumber(@NotNull String id) {
        return (ParameterDefaultNumber) this.getParameter(id);
    }

    @Nullable
    public ParameterDefaultBoolean getParameterBoolean(@NotNull String id) {
        return (ParameterDefaultBoolean) this.getParameter(id);
    }

    @Nullable
    public ParameterDefaultString getParameterString(@NotNull String id) {
        return (ParameterDefaultString) this.getParameter(id);
    }

     */
}

package su.nexmedia.engine.actions;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

public class ActionSection {

    private List<String> targetSelectors;
    private List<String> conditionList;
    private String       conditionActionOnFail;
    private List<String> actionExecutors;

    public ActionSection(@NotNull List<String> targetSelectors, @NotNull List<String> conditionList, @NotNull String conditionActionOnFail, @NotNull List<String> actionExecutors) {
        this.targetSelectors = targetSelectors;
        this.conditionList = conditionList;
        this.conditionActionOnFail = conditionActionOnFail;
        this.actionExecutors = actionExecutors;
    }

    public ActionSection(@NotNull ActionSection from) {
        this.targetSelectors = new ArrayList<>(from.getTargetSelectors());
        this.conditionList = new ArrayList<>(from.getConditions()); // New list
        this.actionExecutors = new ArrayList<>(from.getActionExecutors()); // New list
    }

    @NotNull
    public List<String> getTargetSelectors() {
        return this.targetSelectors;
    }

    @NotNull
    public List<String> getConditions() {
        return this.conditionList;
    }

    public void setConditions(@NotNull List<String> conditionsList) {
        this.conditionList = conditionsList;
    }

    @NotNull
    public String getConditionFailActions() {
        return this.conditionActionOnFail;
    }

    @NotNull
    public List<String> getActionExecutors() {
        return this.actionExecutors;
    }

    public void setActionExecutors(@NotNull List<String> actionExecutors) {
        this.actionExecutors = actionExecutors;
    }
}

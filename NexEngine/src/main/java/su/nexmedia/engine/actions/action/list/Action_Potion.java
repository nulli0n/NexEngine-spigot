package su.nexmedia.engine.actions.action.list;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.actions.action.AbstractActionExecutor;
import su.nexmedia.engine.actions.action.ActionId;
import su.nexmedia.engine.actions.parameter.ParameterId;
import su.nexmedia.engine.actions.parameter.ParameterResult;
import su.nexmedia.engine.actions.parameter.value.ParameterValueNumber;

import java.util.Set;

public class Action_Potion extends AbstractActionExecutor {

    public Action_Potion() {
        super(ActionId.POTION);
        this.registerParameter(ParameterId.NAME);
        this.registerParameter(ParameterId.DURATION);
        this.registerParameter(ParameterId.AMOUNT);
    }

    @Override
    public boolean mustHaveTarget() {
        return true;
    }

    @Override
    protected void execute(@NotNull Entity exe, @NotNull Set<Entity> targets, @NotNull ParameterResult result) {
        String name = (String) result.getValue(ParameterId.NAME);
        if (name == null) return;

        PotionEffectType effectType = PotionEffectType.getByName(name.toUpperCase());
        if (effectType == null) return;

        ParameterValueNumber numberDuration = (ParameterValueNumber) result.getValue(ParameterId.DURATION);
        if (numberDuration == null) return;

        int duration = (int) numberDuration.getValue(0);
        if (duration <= 0) return;

        ParameterValueNumber numberAmp = (ParameterValueNumber) result.getValue(ParameterId.AMOUNT);
        if (numberAmp == null) return;

        int amplifier = (int) Math.max(0, numberAmp.getValue(0) - 1);
        PotionEffect effect = new PotionEffect(effectType, duration, amplifier);

        for (Entity target : targets) {
            if (!(target instanceof LivingEntity livingEntity)) continue;
            livingEntity.addPotionEffect(effect);
        }
    }
}

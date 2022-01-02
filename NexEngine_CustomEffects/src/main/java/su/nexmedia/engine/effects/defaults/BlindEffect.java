package su.nexmedia.engine.effects.defaults;

import org.bukkit.Particle;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.effects.api.AbstractExpirableEffect;
import su.nexmedia.engine.effects.api.EffectType;
import su.nexmedia.engine.effects.config.Config;
import su.nexmedia.engine.utils.EffectUtil;

import java.util.List;

public class BlindEffect extends AbstractExpirableEffect {

    protected BlindEffect(@NotNull Builder builder) {
        super(builder);
    }

    @Override
    protected boolean onTrigger(boolean force) {
        this.applyPotionEffects();
        EffectUtil.playEffect(this.target.getEyeLocation(), Particle.SMOKE_LARGE.name(), 0.2, 0.2, 0.2, 0.08, 5);

        return false;
    }

    @Override
    protected void onClear() {
        this.removePotionEffects();
    }

    @Override
    @NotNull
    public String getType() {
        return EffectType.BLINDNESS;
    }

    @Override
    @NotNull
    public String getName() {
        return Config.ENTITY_EFFECTS_BLIND_NAME;
    }

    @Override
    @NotNull
    public List<String> getDescription() {
        return Config.ENTITY_EFFECTS_BLIND_DESCRIPTION;
    }

    @Override
    public boolean isDeathable() {
        return true;
    }

    @Override
    public boolean isSilent() {
        return false;
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public boolean isReplaceable() {
        return true;
    }

    @Override
    public boolean isPositive() {
        return false;
    }

    public static class Builder extends AbstractExpirableEffect.Builder<Builder> {

        public Builder(double lifeTime) {
            super(lifeTime);
            this.setApplyMessage(PLUGIN.lang().Effects_Blindness_Apply);
            this.addPotionEffects(new PotionEffect(PotionEffectType.BLINDNESS, Short.MAX_VALUE, 5));
        }

        @Override
        @NotNull
        public BlindEffect build() {
            return new BlindEffect(this.self());
        }

        @Override
        @NotNull
        protected Builder self() {
            return this;
        }
    }
}

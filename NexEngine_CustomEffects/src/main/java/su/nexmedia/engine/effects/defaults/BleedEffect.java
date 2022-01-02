package su.nexmedia.engine.effects.defaults;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.effects.api.AbstractPeriodicEffect;
import su.nexmedia.engine.effects.api.EffectType;
import su.nexmedia.engine.effects.config.Config;
import su.nexmedia.engine.utils.EffectUtil;
import su.nexmedia.engine.utils.NumberUtil;

import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public class BleedEffect extends AbstractPeriodicEffect {

    public static final String PLACEHOLDER_DAMAGE = "%effect_bleed_damage%";
    protected Function<LivingEntity, Double> damageFunction;
    protected String                         bloodType;
    protected String                         bloodData;

    private BleedEffect(@NotNull Builder builder) {
        super(builder);
        this.damageFunction = builder.damageFunction;
        this.bloodType = builder.bloodType;
        this.bloodData = builder.bloodData;
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders() {
        double damage = this.target == null ? 0 : this.damageFunction.apply(this.target);
        return str -> super.replacePlaceholders().apply(str)
            .replace(PLACEHOLDER_DAMAGE, NumberUtil.format(damage))
            ;
    }

    @Override
    public boolean onTrigger(boolean force) {
        double damage = this.damageFunction.apply(this.target);
        this.target.damage(damage);
        EffectUtil.playEffect(this.target.getEyeLocation(), this.bloodType, this.bloodData, 0.25, 0.25, 0.25, 0.1, 25);
        return true;
    }

    @Override
    public void onClear() {

    }

    @Override
    @NotNull
    public String getName() {
        return Config.ENTITY_EFFECTS_BLEED_NAME;
    }

    @Override
    @NotNull
    public List<String> getDescription() {
        return Config.ENTITY_EFFECTS_BLEED_DESCRIPTION;
    }

    @Override
    public boolean isDeathable() {
        return true;
    }

    @Override
    @NotNull
    public String getType() {
        return EffectType.BLEED;
    }
	
    /*@Override
    public boolean isIndicated() {
        return true;
    }*/

    @Override
    public boolean isPositive() {
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
    public boolean isSilent() {
        return false;
    }

    public static class Builder extends AbstractPeriodicEffect.Builder<Builder> {

        private Function<LivingEntity, Double> damageFunction;
        private String                         bloodType;
        private String                         bloodData;

        public Builder(double lifeTime, double interval, @NotNull Function<LivingEntity, Double> damageFunction) {
            super(lifeTime, interval);
            this.withFunction(damageFunction);
            this.withBloodType(Particle.BLOCK_CRACK.name());
            this.withBloodData(Material.REDSTONE_BLOCK.name());
        }

        public Builder withFunction(@NotNull Function<LivingEntity, Double> damageFunction) {
            this.damageFunction = damageFunction;
            return this.self();
        }

        public Builder withDamage(double damage) {
            return this.withFunction(entity -> damage);
        }

        @NotNull
        public Builder withBloodType(@NotNull String bloodType) {
            this.bloodType = bloodType;
            return this.self();
        }

        @NotNull
        public Builder withBloodData(@NotNull String bloodData) {
            this.bloodData = bloodData;
            return this.self();
        }

        @Override
        @NotNull
        public BleedEffect build() {
            return new BleedEffect(this);
        }

        @Override
        @NotNull
        protected Builder self() {
            return this;
        }
    }
}

package su.nexmedia.engine.effects.defaults;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.effects.api.AbstractExpirableEffect;
import su.nexmedia.engine.effects.api.EffectType;
import su.nexmedia.engine.effects.config.Config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Deprecated
public final class ResistEffect extends AbstractExpirableEffect {

    // TODO Make abstract or make it have different ID on building to prevent be overrided one resists by different ones

    private final Map<String, Double>  resistMap;
    private final Map<String, Integer> triggerCount;

    private ResistEffect(@NotNull Builder builder) {
        super(builder);
        this.resistMap = new HashMap<>(builder.resist);
        this.triggerCount = new HashMap<>();
    }

    @Override
    @NotNull
    public String getType() {
        return EffectType.RESIST;
    }

    @Override
    @NotNull
    public String getName() {
        return Config.ENTITY_EFFECTS_RESIST_NAME;
    }

    @Override
    @NotNull
    public List<String> getDescription() {
        return Config.ENTITY_EFFECTS_RESIST_DESCRIPTION;
    }

    @Override
    public boolean isPositive() {
        return true;
    }

    @Override
    public boolean isSilent() {
        return false;
    }

    @Override
    public boolean isReplaceable() {
        return true;
    }

    public double getResist(@NotNull String type, boolean safe) {
        double resist = this.resistMap.getOrDefault(type, 0D);

        // Check how many times we got the resist effect compare to effect charges
        // so we remove that stat if resist effect triggered more than charges amount aka expired.
        if (!safe && this.resistMap.containsKey(type) && this.isChargesLimited()) {
            int count = this.triggerCount.compute(type, (k, v) -> this.triggerCount.computeIfAbsent(type, v2 -> 0) + 1);
            if (count >= this.getCharges()) {
                this.triggerCount.remove(type);
                this.resistMap.remove(type);
            }
        }
        return resist;
    }

    @Override
    public boolean onTrigger(boolean force) {
        return this.resistMap.isEmpty();
    }

    @Override
    public void onClear() {

    }

    @Override
    public boolean isDeathable() {
        return true;
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    public static class Builder extends AbstractExpirableEffect.Builder<Builder> {

        private final Map<String, Double> resist;

        public Builder(double lifeTime) {
            super(lifeTime);
            this.resist = new HashMap<>();
        }

        @NotNull
        public Builder withResist(@NotNull String type, double resist) {
            this.resist.put(type, resist);
            return this.self();
        }

        @Override
        @NotNull
        public ResistEffect build() {
            return new ResistEffect(this);
        }

        @Override
        @NotNull
        protected Builder self() {
            return this;
        }
    }
}

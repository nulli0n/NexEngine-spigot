package su.nexmedia.engine.effects.defaults;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.effects.api.AbstractExpirableEffect;
import su.nexmedia.engine.effects.api.EffectType;
import su.nexmedia.engine.effects.config.Config;
import su.nexmedia.engine.utils.EffectUtil;
import su.nexmedia.engine.utils.random.Rnd;

import java.util.List;

public class DodgeEffect extends AbstractExpirableEffect {

    private boolean doStrafe;
    private String  particle;

    protected DodgeEffect(@NotNull Builder builder) {
        super(builder);
        this.doStrafe = builder.doStrafe;
        this.particle = builder.particle;
    }

    @Override
    protected boolean onTrigger(boolean force) {
        /*if (force) {
            this.addIndicator();
        }*/
        if (force && this.isDoStrafe()) {
            double mod = target.isOnGround() ? 1.5D : 0.5D;
            double movX = Rnd.getDouble(2);
            double movZ = Rnd.getDouble(2);

            if (this.particle != null) {
                EffectUtil.playEffect(target.getLocation().add(0, 1, 0), this.particle, 0.2, 0.2, 0.2, 0.1, 25);
            }
            target.setVelocity(target.getVelocity().setX(movX).setZ(movZ).multiply(mod));
        }
        return force;
    }

    @Override
    protected void onClear() {

    }

    public boolean isDoStrafe() {
        return this.doStrafe;
    }

    @Override
    @NotNull
    public String getType() {
        return EffectType.DODGE;
    }

    @Override
    @NotNull
    public String getName() {
        return Config.ENTITY_EFFECTS_DODGE_NAME;
    }

    @Override
    @NotNull
    public List<String> getDescription() {
        return Config.ENTITY_EFFECTS_DODGE_DESCRIPTION;
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
        return false;
    }

    @Override
    public boolean isPositive() {
        return true;
    }

    public static class Builder extends AbstractExpirableEffect.Builder<Builder> {

        private boolean doStrafe;
        private String  particle;

        public Builder(double lifeTime) {
            super(lifeTime);
            this.withStrafe(false);
            this.withParticle(null);
        }

        @NotNull
        public Builder withStrafe(boolean doStrafe) {
            this.doStrafe = doStrafe;
            return this.self();
        }

        @NotNull
        public Builder withParticle(@Nullable String particle) {
            this.particle = particle;
            return this.self();
        }

        @Override
        @NotNull
        public DodgeEffect build() {
            return new DodgeEffect(this.self());
        }

        @Override
        @NotNull
        protected Builder self() {
            return this;
        }
    }
}

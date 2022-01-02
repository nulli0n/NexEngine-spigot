package su.nexmedia.engine.effects.defaults;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.effects.api.AbstractPeriodicEffect;
import su.nexmedia.engine.effects.api.EffectType;
import su.nexmedia.engine.effects.config.Config;
import su.nexmedia.engine.utils.EffectUtil;

import java.util.ArrayList;
import java.util.List;

public class StunEffect extends AbstractPeriodicEffect {

    private final List<Vector> particleCircle;
    private       Location     location;
    private       int          particleCount;

    private StunEffect(@NotNull Builder builder) {
        super(builder);
        this.particleCircle = this.createCircle(0.3, 0.4);
        this.particleCount = 0;
    }

    @Override
    public boolean applyTo(@NotNull LivingEntity entity, boolean force) {
        if (super.applyTo(entity, force)) {
            this.location = entity.getLocation();
            this.location.setYaw(0f);
            entity.teleport(this.location);
            return true;
        }
        return false;
    }

    @Override
    public boolean onTrigger(boolean force) {
        this.applyPotionEffects();
        if (this.getTarget().isOnGround()) {
            this.getTarget().teleport(this.location);
        }

        if (!this.particleCircle.isEmpty()) {
            if (this.particleCount >= this.particleCircle.size()) {
                this.particleCount = 0;
            }
            Location eye = this.getTarget().getEyeLocation().clone();
            Vector vector = this.particleCircle.get(this.particleCount++);
            EffectUtil.playEffect(eye.add(vector), Particle.CRIT.name(), 0.1, 0.1, 0.1, 0.1, 5);
        }
        return true;
    }

    @Override
    public void onClear() {
        this.removePotionEffects();
        this.particleCircle.clear();
        this.particleCount = 0;
    }

    @NotNull
    @Deprecated // TODO Move to NexEgine
    private List<Vector> createCircle(double vertical, double radius) {
        double amount = radius * 64.0D / 2D;
        double d2 = 6.283185307179586D / amount;
        List<Vector> vectors = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            double d3 = i * d2;
            double cos = radius * Math.cos(d3);
            double sin = radius * Math.sin(d3);
            Vector vector = new Vector(cos, vertical, sin);
            vectors.add(vector);
        }
        return vectors;
    }

    @Override
    @NotNull
    public String getName() {
        return Config.ENTITY_EFFECTS_STUN_NAME;
    }

    @Override
    @NotNull
    public List<String> getDescription() {
        return Config.ENTITY_EFFECTS_STUN_DESCRIPTION;
    }

    @Override
    public boolean isDeathable() {
        return true;
    }

    @Override
    @NotNull
    public String getType() {
        return EffectType.STUN;
    }

    @Override
    public boolean isPositive() {
        return false;
    }

    @Override
    public boolean isSingle() {
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

    public static class Builder extends AbstractPeriodicEffect.Builder<Builder> {

        public Builder(double lifeTime) {
            super(lifeTime, 1D / 20D);
            this.setApplyMessage(PLUGIN.lang().Effects_Stun_Apply);

            this.addPotionEffects(new PotionEffect(PotionEffectType.SLOW, (int) (lifeTime * 20), 127));
            this.addPotionEffects(new PotionEffect(PotionEffectType.SLOW_DIGGING, (int) (lifeTime * 20), 127));
            this.addPotionEffects(new PotionEffect(PotionEffectType.BLINDNESS, (int) lifeTime * 20, 127));
        }

        @Override
        @NotNull
        public StunEffect build() {
            return new StunEffect(this);
        }

        @Override
        @NotNull
        protected Builder self() {
            return this;
        }
    }
}

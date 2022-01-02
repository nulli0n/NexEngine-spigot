package su.nexmedia.engine.effects.defaults;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.effects.api.AbstractPeriodicEffect;
import su.nexmedia.engine.effects.api.EffectType;
import su.nexmedia.engine.effects.config.Config;
import su.nexmedia.engine.utils.EffectUtil;

import java.util.ArrayList;
import java.util.List;

public class RootEffect extends AbstractPeriodicEffect {

    private Location locRoot;
    private String   particle;

    private RootEffect(@NotNull Builder builder) {
        super(builder);
        this.particle = builder.particle;
    }

    @Override
    public boolean applyTo(@NotNull LivingEntity entity, boolean force) {
        if (super.applyTo(entity, force)) {
            this.locRoot = entity.getLocation();
            return true;
        }
        return false;
    }

    @Override
    public boolean onTrigger(boolean force) {
        this.applyPotionEffects();
        if (this.getTarget().isOnGround()) {
            Location loc = this.getTarget().getLocation();
            if (loc.getX() != this.locRoot.getX() || loc.getY() != this.locRoot.getY() || loc.getZ() != this.locRoot.getZ()) {
                this.getTarget().teleport(this.locRoot);
            }

            if (this.particle != null) {
                for (Vector vector : this.createCircle(0.5D, 0.75D)) {
                    EffectUtil.playEffect(loc.add(vector), this.particle, 0.1, 0.1, 0.1, 0, 1);
                    loc.subtract(vector);
                }
            }
        }
        return true;
    }

    @Override
    public void onClear() {
        this.removePotionEffects();
    }

    @NotNull
    @Deprecated // TODO Move to NexEgine
    private List<Vector> createCircle(double vertical, double radius) {
        double amount = radius * 64.0D;
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
        return Config.ENTITY_EFFECTS_ROOT_NAME;
    }

    @Override
    @NotNull
    public List<String> getDescription() {
        return Config.ENTITY_EFFECTS_ROOT_DESCRIPTION;
    }

    @Override
    public boolean isDeathable() {
        return true;
    }

    @Override
    @NotNull
    public String getType() {
        return EffectType.ROOT;
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

        private String particle;

        public Builder(double lifeTime) {
            super(lifeTime, 1D / 20D);
            this.setApplyMessage(PLUGIN.lang().Effects_Root_Apply);
            this.withParticle(Particle.BLOCK_CRACK.name() + ":" + Material.VINE.name());
            this.addPotionEffects(new PotionEffect(PotionEffectType.SLOW, (int) (lifeTime * 20), 127));
        }

        @NotNull
        public Builder withParticle(@Nullable String particle) {
            this.particle = particle;
            return this.self();
        }

        @Override
        @NotNull
        public RootEffect build() {
            return new RootEffect(this);
        }

        @Override
        @NotNull
        protected Builder self() {
            return this;
        }
    }
}

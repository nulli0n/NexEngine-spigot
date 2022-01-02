package su.nexmedia.engine.effects.api;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.effects.NexCustomEffects;
import su.nexmedia.engine.api.config.LangMessage;
import su.nexmedia.engine.api.manager.ICleanable;
import su.nexmedia.engine.api.manager.IPlaceholder;
import su.nexmedia.engine.effects.config.Config;
import su.nexmedia.engine.effects.entity.EntityStats;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;

public abstract class AbstractEffect implements ICleanable, IPlaceholder {

    public static final String PLACEHOLDER_NAME        = "%effect_name%";
    public static final String PLACEHOLDER_DESCRIPTION = "%effect_description%";
    public static final String PLACEHOLDER_CHARGES     = "%effect_charges%";

    protected static final NexCustomEffects PLUGIN = NexCustomEffects.getPlugin(NexCustomEffects.class);

    protected LivingEntity                        caster;
    protected LivingEntity                        target;
    protected Map<PotionEffectType, PotionEffect> potions;
    protected int                                 charges;
    protected double                              threshold;

    protected LangMessage msgApply;
    protected LangMessage msgExpire;
    protected LangMessage msgReject;
    protected BossBar     displayBar;

    protected AbstractEffect(@NotNull Builder<?> builder) {
        this.caster = builder.caster;
        this.potions = new HashMap<>(builder.potions);
        this.charges = builder.charges;
        this.threshold = builder.threshold;

        this.msgApply = builder.msgApply;
        this.msgExpire = builder.msgExpire;
        this.msgReject = builder.msgReject;
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders() {
        return str -> str
            .replace(PLACEHOLDER_DESCRIPTION, String.join("\n", this.getDescription()))
            .replace(PLACEHOLDER_NAME, this.getName())
            .replace(PLACEHOLDER_CHARGES, String.valueOf(this.getCharges()))
            ;
    }

    public boolean trigger(boolean force) {
        if (this.onTrigger(force) || force) {
            this.takeCharge();
            return true;
        }
        return false;
    }

    @Override
    public void clear() {
        if (this.displayBar != null) {
            this.displayBar.removeAll();
        }
        this.onClear();
    }

    public boolean applyTo(@NotNull LivingEntity target, boolean force) {
        if (target.isDead() || !target.isValid()) return false;

        this.target = target;
        EntityStats targetStats = EntityStats.get(target);
        if (!force) {
            if (this.isSingle() && targetStats.hasEffect(this.getType()) && !this.isReplaceable()) {
                return false;
            }
        }

        double resist = targetStats.getEffectResist(this.getType(), false);
        if (resist > this.getThreshold()) {
            this.sendResistMessage();
            return false;
        }

        if (this.isSingle()) {
            targetStats.removeEffect(this.getType());
        }

        targetStats.addEffect(this);
        this.sendApplyMessage();
        return true;
    }

    public boolean cancel() {
        EntityStats stats = EntityStats.get(this.getTarget());
        return stats.removeEffect(this);
    }

    public void updateDisplayBar() {
        if (this.isSilent()) return;
        if (!(this instanceof AbstractExpirableEffect expirableEffect)) return;
        if (!(this.getTarget() instanceof Player)) return;
        if (expirableEffect.getLifeTime() == 0D) return;
        if (expirableEffect.isPermanent() && !this.isChargesLimited()) return;

        boolean isPositive = this.isPositive();

        if (this.displayBar == null) {
            BarColor color = isPositive ? Config.ENTITY_EFFECTS_BAR_POSITIVE_COLOR :
                Config.ENTITY_EFFECTS_BAR_NEGATIVE_COLOR;
            BarStyle style = isPositive ? Config.ENTITY_EFFECTS_BAR_POSITIVE_STYLE :
                Config.ENTITY_EFFECTS_BAR_NEGATIVE_STYLE;

            this.displayBar = PLUGIN.getServer().createBossBar("", color, style);
            this.displayBar.addPlayer((Player) this.getTarget());
        }

        String title = isPositive ? Config.ENTITY_EFFECTS_BAR_POSITIVE_TITLE :
            Config.ENTITY_EFFECTS_BAR_NEGATIVE_TITLE;

        String duration = this.isChargesLimited() ?
            expirableEffect.isPermanent() ? Config.ENTITY_EFFECTS_BAR_DURATION_CHARGES : Config.ENTITY_EFFECTS_BAR_DURATION_TIME_CHARGES
            : Config.ENTITY_EFFECTS_BAR_DURATION_TIME;

        title = title.replace("%duration%", duration);
        title = this.replacePlaceholders().apply(title);

        long now = System.currentTimeMillis();
        long end = expirableEffect.endTime;

        double diffSec = (double) (end - now) / 1000D;
        double proress = Math.min(1D, Math.max(0D, diffSec / expirableEffect.getLifeTime()));

        this.displayBar.setTitle(title);
        this.displayBar.setProgress(proress);
    }

    protected abstract boolean onTrigger(boolean force);

    protected abstract void onClear();

    @NotNull
    public abstract String getType();

    @NotNull
    public abstract String getName();

    @NotNull
    public abstract List<String> getDescription();

    @Nullable
    public LivingEntity getCaster() {
        return this.caster;
    }

    @NotNull
    public LivingEntity getTarget() {
        return this.target;
    }

    public final int getCharges() {
        return this.charges;
    }

    public double getThreshold() {
        return this.threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public final int addCharge() {
        return ++this.charges;
    }

    public final int takeCharge() {
        if (!this.isChargesLimited()) return this.charges;
        return --this.charges;
    }

    public boolean isType(@NotNull String type) {
        return type.equalsIgnoreCase(this.getType());
    }

    public final boolean isChargesLimited() {
        return this.getCharges() >= 0;
    }

    public boolean isExpired() {
        return this.getCharges() == 0;
    }

    public abstract boolean isDeathable();

    /**
     * Defines if effect should be 'silent', non-notifible by a player.
     * Hides all effect displays and notifications from showing up.
     *
     * @return TRUE if effect is silent.
     */
    public abstract boolean isSilent();

    public abstract boolean isSingle();

    public abstract boolean isReplaceable();

    public abstract boolean isPositive();

    public void sendExpireMessage() {
        if (this.target == null) return;
        if (this.msgExpire != null) {
            this.msgExpire.replace(this.replacePlaceholders()).send(this.getTarget());
        }
    }

    public void sendApplyMessage() {
        if (this.target == null) return;
        if (this.msgApply != null) {
            this.msgApply.replace(this.replacePlaceholders()).send(this.getTarget());
        }
    }

    public void sendResistMessage() {
        if (this.caster == null) return;
        if (this.msgReject != null) {
            this.msgReject.replace(this.replacePlaceholders()).send(this.getCaster());
        }
    }

    protected final void applyPotionEffects() {
        for (PotionEffect potionEffect : this.potions.values()) {
            PotionEffect has = target.getPotionEffect(potionEffect.getType());
            if (has != null && has.getAmplifier() > potionEffect.getAmplifier()) continue;

            this.target.addPotionEffect(potionEffect);
        }
    }

    protected final void removePotionEffects() {
        for (PotionEffect potionEffect : this.potions.values()) {
            PotionEffect has = target.getPotionEffect(potionEffect.getType());
            if (has != null && has.getAmplifier() != potionEffect.getAmplifier()) continue;

            target.removePotionEffect(potionEffect.getType());
        }
    }

    public abstract static class Builder<B extends Builder<B>> {

        private LivingEntity                        caster;
        private Map<PotionEffectType, PotionEffect> potions;
        private int                                 charges;
        private double                              threshold;

        private LangMessage msgApply;
        private LangMessage msgExpire;
        private LangMessage msgReject;

        public Builder() {
            this.caster = null;
            this.potions = new HashMap<>();
            this.charges = -1;
            this.threshold = 0D;
        }

        @NotNull
        public B withCaster(@Nullable LivingEntity caster) {
            this.caster = caster;
            return this.self();
        }

        @NotNull
        public B withCharges(int charges) {
            this.charges = charges;
            return this.self();
        }

        @NotNull
        public B withThreshold(double threshold) {
            this.threshold = threshold;
            return this.self();
        }

        @NotNull
        public B setApplyMessage(@NotNull LangMessage message) {
            this.msgApply = message;
            return this.self();
        }

        @NotNull
        public B setExpireMessage(@NotNull LangMessage message) {
            this.msgExpire = message;
            return this.self();
        }

        @NotNull
        public B setRejectMessage(@NotNull LangMessage message) {
            this.msgReject = message;
            return this.self();
        }

        @NotNull
        public B addPotionEffects(@NotNull PotionEffect... potionEffects) {
            for (PotionEffect effect : potionEffects) {
                this.potions.putIfAbsent(effect.getType(), effect);
            }
            return this.self();
        }

        @NotNull
        public abstract AbstractEffect build();

        @NotNull
        protected abstract B self();
    }
}

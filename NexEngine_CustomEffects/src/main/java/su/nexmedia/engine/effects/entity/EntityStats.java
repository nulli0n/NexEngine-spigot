package su.nexmedia.engine.effects.entity;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.effects.NexCustomEffects;
import su.nexmedia.engine.effects.api.AbstractEffect;
import su.nexmedia.engine.effects.api.AbstractPeriodicEffect;
import su.nexmedia.engine.effects.defaults.ResistEffect;
import su.nexmedia.engine.hooks.Hooks;

import java.util.*;

public class EntityStats {

    private static final NexCustomEffects         PLUGIN = NexCustomEffects.getPlugin(NexCustomEffects.class);
    private static final Map<String, EntityStats> STATS;

    static {
        STATS = Collections.synchronizedMap(new HashMap<>());
    }

    private final boolean             isNPC;
    private final Set<AbstractEffect> effects;
    private       LivingEntity        entity;
    private       Player              player;

    EntityStats(@NotNull LivingEntity entity) {
        this.updateHolder(entity);
        this.isNPC = Hooks.isCitizensNPC(this.entity);
        this.effects = Collections.synchronizedSet(new HashSet<>());
    }

    @NotNull
    public static EntityStats get(@NotNull LivingEntity entity) {
        String uuid = entity.getUniqueId().toString();
        EntityStats eStats = STATS.computeIfAbsent(uuid, stats -> new EntityStats(entity));
        eStats.updateHolder(entity);
        return eStats;
    }

    @NotNull
    public synchronized static Collection<EntityStats> getAll() {
        STATS.values().removeIf(stats -> {
            if (stats.isPlayer()) return false;
            return !stats.entity.isValid() || stats.entity.isDead();
        });
        return STATS.values();
    }

    public static void purge(@NotNull LivingEntity entity) {
        String uuid = entity.getUniqueId().toString();
        STATS.remove(uuid);
    }

    public void purge() {
        purge(entity);
    }

    public void handleDeath() {
        if (this.isPlayer()) {
            for (AbstractEffect effect : new HashSet<>(this.effects)) {
                if (effect.isDeathable()) {
                    this.removeEffect(effect);
                }
            }
        }
        else {
            this.purge();
        }
    }

    private void updateHolder(@NotNull LivingEntity valid) {
        if (this.entity == null || !this.entity.equals(valid)) {
            this.entity = valid;
        }
        this.player = this.entity instanceof Player ? (Player) this.entity : null;
    }

    public final boolean isPlayer() {
        return this.player != null;
    }

    public final boolean isNPC() {
        return this.isNPC;
    }

    @Nullable
    public final Player getPlayer() {
        return this.player;
    }

    @NotNull
    public synchronized Set<AbstractEffect> getActiveEffects() {
        Set<AbstractEffect> set = new HashSet<>();

        for (AbstractEffect effect : new HashSet<>(this.effects)) {
            if (effect.isExpired()) {
                effect.sendExpireMessage();
                this.removeEffect(effect);
                continue;
            }

            PLUGIN.runTask(c -> effect.updateDisplayBar(), true);

            if (effect instanceof AbstractPeriodicEffect periodic) {
                if (!periodic.isReady()) {
                    continue;
                }
            }
            set.add(effect);
        }

        return set;
    }

    public double getEffectResist(@NotNull String type, boolean safe) {
        return this.getActiveEffects().stream().filter(effect -> effect instanceof ResistEffect)
            .mapToDouble(effect -> {
                // Effect charge is not taken, but it's counted for each effect type.
                double resist = ((ResistEffect) effect).getResist(type, safe);
                // Effect chrage is taken only when no resists are left, so effect can expire and purge.
                if (resist != 0D) effect.trigger(false);
                return resist;
            }).sum();
    }

    public void triggerEffects() {
        this.getActiveEffects().forEach(effect -> effect.trigger(false));
    }

    public boolean addEffect(@NotNull AbstractEffect effect) {
        this.effects.add(effect);
        return true;
    }

    public boolean removeEffect(@NotNull AbstractEffect effect) {
        if (this.effects.remove(effect)) {
            effect.clear();
            return true;
        }
        return false;
    }

    public boolean removeEffect(@NotNull String type) {
        AbstractEffect effect = this.getEffect(type);
        if (effect != null) return this.removeEffect(effect);
        return false;
    }

    public boolean hasEffect(@NotNull String type) {
        return this.effects.stream().anyMatch(effect -> effect.isType(type));
    }

    @Nullable
    public AbstractEffect getEffect(@NotNull String type) {
        Optional<AbstractEffect> opt = this.effects.stream().filter(effect -> effect.isType(type)).findFirst();
        return opt.orElse(null);
    }
}

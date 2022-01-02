package su.nexmedia.engine.effects.entity;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.effects.NexCustomEffects;
import su.nexmedia.engine.effects.api.AbstractEffect;
import su.nexmedia.engine.effects.api.EffectType;
import su.nexmedia.engine.api.manager.AbstractListener;
import su.nexmedia.engine.api.manager.AbstractManager;
import su.nexmedia.engine.effects.defaults.DodgeEffect;

public class EntityManager extends AbstractManager<NexCustomEffects> {

    private EntityStatsTask statsTask;

    public EntityManager(@NotNull NexCustomEffects plugin) {
        super(plugin);
    }

    @Override
    public void onLoad() {
        this.addListener(new Listener(this.plugin));
        (this.statsTask = new EntityStatsTask(plugin)).start();
    }

    @Override
    public void onShutdown() {
        if (this.statsTask != null) {
            this.statsTask.stop();
            this.statsTask = null;
        }
    }

    static class Listener extends AbstractListener<NexCustomEffects> {

        public Listener(@NotNull NexCustomEffects plugin) {
            super(plugin);
        }

        @EventHandler(priority = EventPriority.NORMAL)
        public void onStatsDeath(EntityDeathEvent e) {
            EntityStats.get(e.getEntity()).handleDeath();
        }

        @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
        public void onEffectDamage(EntityDamageByEntityEvent e) {
            Entity eDamager = e.getDamager();
            Entity eVictim = e.getEntity();

            LivingEntity victim = eVictim instanceof LivingEntity ? (LivingEntity) eVictim : null;
            if (eDamager instanceof Projectile projectile) {
                ProjectileSource source = projectile.getShooter();
                if (source instanceof Entity) eDamager = (Entity) source;
            }

            Label_Damager:
            if (eDamager instanceof LivingEntity damager) {
                EntityStats stats = EntityStats.get(damager);

                // Check for custom Stun effect
                AbstractEffect stunEffect = stats.getEffect(EffectType.STUN);
                if (stunEffect != null) {
                    e.setCancelled(true);
                    return;
                }

                // Check for custom Blindness effect
                AbstractEffect blindEffect = stats.getEffect(EffectType.BLINDNESS);
                if (blindEffect != null && victim != null) {
                    blindEffect.takeCharge(); // We use 'takeCharge' instead of 'trigger', because it's auto-triggered anyway.
                    new DodgeEffect.Builder(-1).withCharges(1).build().applyTo(victim, true);
                    break Label_Damager;
                }
            }

            if (victim != null) {
                EntityStats stats = EntityStats.get(victim);
                AbstractEffect dodgeEffect = stats.getEffect(EffectType.DODGE);
                if (dodgeEffect != null) {
                    dodgeEffect.trigger(true); // We use 'trigger' to force take charge and trigger effect actions.
                    e.setCancelled(true);
                }
            }
        }
    }
}

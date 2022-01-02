package su.nexmedia.engine.effects.defaults;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.effects.api.AbstractEffect;
import su.nexmedia.engine.effects.api.EffectType;
import su.nexmedia.engine.effects.config.Config;
import su.nexmedia.engine.utils.EffectUtil;
import su.nexmedia.engine.utils.MessageUtil;

import java.util.List;

public class DisarmEffect extends AbstractEffect {

    private EntityEquipment equipment;

    public DisarmEffect(@NotNull AbstractEffect.Builder<Builder> builder) {
        super(builder);
    }

    @Override
    public boolean applyTo(@NotNull LivingEntity target, boolean force) {
        this.equipment = target.getEquipment();
        if (this.equipment == null) return false;

        ItemStack hand = this.equipment.getItemInMainHand();
        if (hand.getType().isAir()) return false;

        return super.applyTo(target, force);
    }

    @Override
    protected boolean onTrigger(boolean force) {
        ItemStack hand = this.equipment.getItemInMainHand();
        if (hand.getType().isAir()) return false;

        this.equipment.setItemInMainHand(null);
        this.target.getWorld().dropItemNaturally(target.getLocation(), hand).setPickupDelay(50);

        EffectUtil.playEffect(target.getEyeLocation().add(0, -0.2, 0), Particle.ITEM_CRACK.name(), hand.getType().name(), 0.2, 0.25, 0.2, 0.05, 25);
        MessageUtil.sound(target.getEyeLocation(), Sound.ENTITY_ITEM_BREAK.name());

        return true;
    }

    @Override
    protected void onClear() {

    }

    @Override
    @NotNull
    public String getName() {
        return Config.ENTITY_EFFECTS_DISARM_NAME;
    }

    @Override
    @NotNull
    public List<String> getDescription() {
        return Config.ENTITY_EFFECTS_DISARM_DESCRIPTION;
    }

    @Override
    @NotNull
    public String getType() {
        return EffectType.DISARM;
    }

    @Override
    public boolean isDeathable() {
        return true;
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
        return false;
    }

    public static class Builder extends AbstractEffect.Builder<Builder> {

        @Override
        @NotNull
        public DisarmEffect build() {
            this.setApplyMessage(PLUGIN.lang().Effects_Disarm_Apply);
            return new DisarmEffect(this.self());
        }

        @Override
        @NotNull
        protected Builder self() {
            return this;
        }
    }
}

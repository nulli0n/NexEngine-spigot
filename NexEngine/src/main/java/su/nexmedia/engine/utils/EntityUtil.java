package su.nexmedia.engine.utils;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexEngine;
import su.nexmedia.engine.utils.random.Rnd;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class EntityUtil {

    public static double getAttribute(@NotNull LivingEntity entity, @NotNull Attribute attribute) {
        AttributeInstance instance = entity.getAttribute(attribute);
        return instance == null ? 0D : instance.getValue();
    }

    public static double getAttributeBase(@NotNull LivingEntity entity, @NotNull Attribute attribute) {
        AttributeInstance instance = entity.getAttribute(attribute);
        return instance == null ? 0D : instance.getBaseValue();
    }

    @NotNull
    public static Map<EquipmentSlot, ItemStack> getEquippedItems(@NotNull LivingEntity entity) {
        return getEquippedItems(entity, EquipmentSlot.values());
    }

    @NotNull
    public static Map<EquipmentSlot, ItemStack> getEquippedItems(@NotNull LivingEntity entity, @NotNull EquipmentSlot... slots) {
        EntityEquipment equipment = entity.getEquipment();
        if (equipment == null) return Collections.emptyMap();

        Map<EquipmentSlot, ItemStack> map = new HashMap<>();
        Stream.of(slots).forEach(slot -> {
            map.put(slot, equipment.getItem(slot));
        });
        return map;
    }

    @NotNull
    public static Map<EquipmentSlot, ItemStack> getEquippedHands(@NotNull LivingEntity entity) {
        return getEquippedItems(entity, EquipmentSlot.HAND, EquipmentSlot.OFF_HAND);
    }

    @NotNull
    public static Map<EquipmentSlot, ItemStack> getEquippedArmor(@NotNull LivingEntity entity) {
        return getEquippedItems(entity, EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD);
    }

    @NotNull
    public static String getName(@NotNull Entity entity) {
        if (entity instanceof Player) {
            return entity.getName();
        }
        if (entity instanceof LivingEntity) {
            String cName = entity.getCustomName();
            if (cName != null) {
                return cName;
            }
        }

        return NexEngine.get().getLangManager().getEnum(entity.getType());
    }

    @NotNull
    public static Firework spawnRandomFirework(@NotNull Location location) {
        World world = location.getWorld();
        if (world == null) throw new IllegalStateException("World is null!");

        Firework firework = (Firework) world.spawnEntity(location, EntityType.FIREWORK);
        FireworkMeta meta = firework.getFireworkMeta();
        FireworkEffect.Type type = Rnd.get(FireworkEffect.Type.values());
        Color color = Color.fromBGR(Rnd.nextInt(255), Rnd.nextInt(255), Rnd.nextInt(255));
        Color fade = Color.fromBGR(Rnd.nextInt(255), Rnd.nextInt(255), Rnd.nextInt(255));
        FireworkEffect effect = FireworkEffect.builder().flicker(Rnd.nextBoolean()).withColor(color)
            .withFade(fade).with(type).trail(Rnd.nextBoolean()).build();
        meta.addEffect(effect);

        int power = Rnd.get(5);
        meta.setPower(power);
        firework.setFireworkMeta(meta);

        return firework;
    }
}

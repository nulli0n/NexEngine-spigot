package su.nexmedia.engine.utils;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexEngine;
import su.nexmedia.engine.utils.random.Rnd;

public class EntityUtil {

    public static double getAttribute(@NotNull LivingEntity entity, @NotNull Attribute attribute) {
        AttributeInstance instance = entity.getAttribute(attribute);
        return instance == null ? 0D : instance.getValue();
    }

    public static double getAttributeBase(@NotNull LivingEntity entity, @NotNull Attribute attribute) {
        AttributeInstance instance = entity.getAttribute(attribute);
        return instance == null ? 0D : instance.getBaseValue();
    }

    public static ItemStack[] getArmor(@NotNull LivingEntity entity) {
        EntityEquipment equip = entity.getEquipment();
        if (equip == null) return new ItemStack[4];

        return equip.getArmorContents();
    }

    public static ItemStack[] getEquipment(@NotNull LivingEntity entity) {
        ItemStack[] items = new ItemStack[6];

        EntityEquipment equip = entity.getEquipment();
        if (equip == null) return items;

        int aCount = 0;
        for (ItemStack armor : equip.getArmorContents()) {
            items[aCount++] = armor;
        }

        items[4] = equip.getItemInMainHand();
        items[5] = equip.getItemInOffHand();

        return items;
    }

    @NotNull
    @Deprecated
    public static ItemStack getSkull(@NotNull LivingEntity victim) {
        ItemStack item;
        if (victim instanceof WitherSkeleton) {
            item = new ItemStack(Material.WITHER_SKELETON_SKULL);
        }
        else if (victim instanceof Zombie && !(victim instanceof ZombieVillager)) {
            item = new ItemStack(Material.ZOMBIE_HEAD);
        }
        else if (victim instanceof Skeleton) {
            item = new ItemStack(Material.SKELETON_SKULL);
        }
        else if (victim instanceof Creeper) {
            item = new ItemStack(Material.CREEPER_HEAD);
        }
        else if (victim instanceof EnderDragon) {
            item = new ItemStack(Material.DRAGON_HEAD);
        }
        else {
            item = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) item.getItemMeta();
            if (meta == null)
                return item;

            String owner = victim instanceof Player ? victim.getName() : getValidSkullName(victim);
            meta.setOwner(owner);
            item.setItemMeta(meta);
        }

        return item;
    }

    @NotNull
    @Deprecated
    public static String getValidSkullName(@NotNull Entity entity) {
        return getValidSkullName(entity.getType());
    }

    @NotNull
    @Deprecated
    public static String getValidSkullName(@NotNull EntityType type) {
        switch (type) {
            case MAGMA_CUBE: {
                return "MHF_LavaSlime";
            }
            case ELDER_GUARDIAN: {
                return "MHF_EGuardian";
            }
            case IRON_GOLEM: {
                return "MHF_Golem";
            }
            default: {
                String s = type.name().toLowerCase().replace("_", " ");
                return "MHF_" + StringUtil.capitalizeFully(s).replace(" ", "");
            }
        }
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

        return NexEngine.get().lang().getEnum(entity.getType());
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

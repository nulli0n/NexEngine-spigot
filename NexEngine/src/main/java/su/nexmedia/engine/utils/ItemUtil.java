package su.nexmedia.engine.utils;

import com.google.common.base.Splitter;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.NexEngine;
import su.nexmedia.engine.Version;
import su.nexmedia.engine.config.EngineConfig;
import su.nexmedia.engine.hooks.Hooks;
import su.nexmedia.engine.lang.LangManager;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public class ItemUtil {

    private static final NexEngine ENGINE = NexEngine.get();

    @NotNull
    public static String getItemName(@NotNull ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        return (meta == null || !meta.hasDisplayName()) ? LangManager.getMaterial(item.getType()) : meta.getDisplayName();
    }

    public static void mapMeta(@NotNull ItemStack item, @NotNull Consumer<ItemMeta> function) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        function.accept(meta);
        item.setItemMeta(meta);
    }

    @NotNull
    public static List<String> getLore(@NotNull ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        return (meta == null || meta.getLore() == null) ? new ArrayList<>() : meta.getLore();
    }

    @NotNull
    public static ItemStack createCustomHead(@NotNull String texture) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        setSkullTexture(item, texture);
        return item;
    }

    public static void setSkullTexture(@NotNull ItemStack item, @NotNull String value) {
        if (item.getType() != Material.PLAYER_HEAD) return;
        if (!(item.getItemMeta() instanceof SkullMeta meta)) return;

        GameProfile profile = new GameProfile(EngineConfig.getIdForSkullTexture(value), null);
        profile.getProperties().put("textures", new Property("textures", value));

        Method method = Reflex.getMethod(meta.getClass(), "setProfile", GameProfile.class);
        if (method != null) {
            Reflex.invokeMethod(method, meta, profile);
        }
        else {
            Reflex.setFieldValue(meta, "profile", profile);
        }

        item.setItemMeta(meta);
    }

    @Nullable
    public static String getSkullTexture(@NotNull ItemStack item) {
        if (item.getType() != Material.PLAYER_HEAD) return null;

        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta == null) return null;

        GameProfile profile = (GameProfile) Reflex.getFieldValue(meta, "profile");
        if (profile == null) return null;

        Collection<Property> properties = profile.getProperties().get("textures");
        Optional<Property> opt = properties.stream().filter(prop -> {
            return prop.getName().equalsIgnoreCase("textures") || prop.getSignature().equalsIgnoreCase("textures");
        }).findFirst();

        return opt.map(Property::getValue).orElse(null);
    }

    public static void setPlaceholderAPI(@NotNull Player player, @NotNull ItemStack item) {
        if (!Hooks.hasPlaceholderAPI()) return;
        replace(item, str -> Colorizer.apply(PlaceholderAPI.setPlaceholders(player, str)));
    }

    public static void replace(@NotNull ItemStack item, @NotNull UnaryOperator<String> replacer) {
        mapMeta(item, meta -> replace(meta, replacer));
    }

    public static void replace(@NotNull ItemMeta meta, @NotNull UnaryOperator<String> replacer) {
        if (meta.hasDisplayName()) {
            meta.setDisplayName(replacer.apply(meta.getDisplayName()));
        }

        List<String> loreHas = meta.getLore();
        //List<String> loreReplaced = new ArrayList<>();
        if (loreHas != null) {
            // Should perform much faster
            String single = replacer.apply(String.join("\n", loreHas));
            meta.setLore(StringUtil.stripEmpty(Splitter.on("\n").splitToList(single)));

            //loreHas.replaceAll(replacer);
            //loreHas.forEach(line -> loreReplaced.addAll(Arrays.asList(line.split("\\n"))));
            //meta.setLore(StringUtil.stripEmpty(loreReplaced));
        }
    }

    public static void replaceLore(@NotNull ItemStack item, @NotNull String placeholder, @NotNull List<String> replacer) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        List<String> loreHas = meta.getLore();
        if (loreHas == null) return;

        List<String> loreReplaced = new ArrayList<>();
        for (String lineHas : loreHas) {
            if (lineHas.contains(placeholder)) {
                replacer.forEach(lineRep -> {
                    loreReplaced.add(lineHas.replace(placeholder, lineRep));
                });
                continue;
            }
            loreReplaced.add(lineHas);
        }
        meta.setLore(StringUtil.stripEmpty(loreReplaced));
        item.setItemMeta(meta);
    }

    @Deprecated
    public static boolean isWeapon(@NotNull ItemStack item) {
        return isSword(item) || isAxe(item) || isTrident(item);
    }

    public static boolean isTool(@NotNull ItemStack item) {
        return isAxe(item) || isHoe(item) || isPickaxe(item) || isShovel(item);
    }

    public static boolean isArmor(@NotNull ItemStack item) {
        return isHelmet(item) || isChestplate(item) || isLeggings(item) || isBoots(item);
    }

    public static boolean isBow(@NotNull ItemStack item) {
        return item.getType() == Material.BOW || item.getType() == Material.CROSSBOW;
    }

    public static boolean isSword(@NotNull ItemStack item) {
        if (Version.isAtLeast(Version.V1_19_R3)) {
            return Tag.ITEMS_SWORDS.isTagged(item.getType());
        }

        Material material = item.getType();
        return material == Material.DIAMOND_SWORD || material == Material.GOLDEN_SWORD
            || material == Material.IRON_SWORD || material == Material.NETHERITE_SWORD
            || material == Material.STONE_SWORD || material == Material.WOODEN_SWORD;
    }

    public static boolean isAxe(@NotNull ItemStack item) {
        if (Version.isAtLeast(Version.V1_19_R3)) {
            return Tag.ITEMS_AXES.isTagged(item.getType());
        }

        Material material = item.getType();
        return material == Material.DIAMOND_AXE || material == Material.GOLDEN_AXE
            || material == Material.IRON_AXE || material == Material.NETHERITE_AXE
            || material == Material.STONE_AXE || material == Material.WOODEN_AXE;
    }

    public static boolean isTrident(@NotNull ItemStack item) {
        return item.getType() == Material.TRIDENT;
    }

    public static boolean isPickaxe(@NotNull ItemStack item) {
        if (Version.isAtLeast(Version.V1_19_R3)) {
            return Tag.ITEMS_PICKAXES.isTagged(item.getType());
        }

        Material material = item.getType();
        return material == Material.DIAMOND_PICKAXE || material == Material.GOLDEN_PICKAXE
            || material == Material.IRON_PICKAXE || material == Material.NETHERITE_PICKAXE
            || material == Material.STONE_PICKAXE || material == Material.WOODEN_PICKAXE;
    }

    public static boolean isShovel(@NotNull ItemStack item) {
        if (Version.isAtLeast(Version.V1_19_R3)) {
            return Tag.ITEMS_SHOVELS.isTagged(item.getType());
        }

        Material material = item.getType();
        return material == Material.DIAMOND_SHOVEL || material == Material.GOLDEN_SHOVEL
            || material == Material.IRON_SHOVEL || material == Material.NETHERITE_SHOVEL
            || material == Material.STONE_SHOVEL || material == Material.WOODEN_SHOVEL;
    }

    public static boolean isHoe(@NotNull ItemStack item) {
        if (Version.isAtLeast(Version.V1_19_R3)) {
            return Tag.ITEMS_HOES.isTagged(item.getType());
        }

        Material material = item.getType();
        return material == Material.DIAMOND_HOE || material == Material.GOLDEN_HOE
            || material == Material.IRON_HOE || material == Material.NETHERITE_HOE
            || material == Material.STONE_HOE || material == Material.WOODEN_HOE;
    }

    public static boolean isElytra(@NotNull ItemStack item) {
        return item.getType() == Material.ELYTRA;
    }

    public static boolean isFishingRod(@NotNull ItemStack item) {
        return item.getType() == Material.FISHING_ROD;
    }

    public static boolean isHelmet(@NotNull ItemStack item) {
        return getEquipmentSlot(item) == EquipmentSlot.HEAD;
    }

    public static boolean isChestplate(@NotNull ItemStack item) {
        return getEquipmentSlot(item) == EquipmentSlot.CHEST;
    }

    public static boolean isLeggings(@NotNull ItemStack item) {
        return getEquipmentSlot(item) == EquipmentSlot.LEGS;
    }

    public static boolean isBoots(@NotNull ItemStack item) {
        return getEquipmentSlot(item) == EquipmentSlot.FEET;
    }

    @NotNull
    public static EquipmentSlot getEquipmentSlot(@NotNull ItemStack item) {
        Material material = item.getType();
        return material.isItem() ? material.getEquipmentSlot() : EquipmentSlot.HAND;
    }

    @NotNull
    public static String getNBTTag(@NotNull ItemStack item) {
        return ENGINE.getNMS().getNBTTag(item);
    }

    @Nullable
    public static String toBase64(@NotNull ItemStack item) {
        return ENGINE.getNMS().toBase64(item);
    }

    @NotNull
    public static List<String> toBase64(@NotNull ItemStack[] item) {
        return toBase64(Arrays.asList(item));
    }

    @NotNull
    public static List<String> toBase64(@NotNull List<ItemStack> items) {
        return new ArrayList<>(items.stream().map(ItemUtil::toBase64).filter(Objects::nonNull).toList());
    }

    @Nullable
    public static ItemStack fromBase64(@NotNull String data) {
        return ENGINE.getNMS().fromBase64(data);
    }

    @NotNull
    public static ItemStack[] fromBase64(@NotNull List<String> list) {
        List<ItemStack> items = list.stream().map(ItemUtil::fromBase64).filter(Objects::nonNull).toList();
        return items.toArray(new ItemStack[list.size()]);
    }
}

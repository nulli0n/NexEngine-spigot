package su.nexmedia.engine.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.NexEngine;
import su.nexmedia.engine.config.ConfigManager;
import su.nexmedia.engine.hooks.Hooks;

import java.util.*;
import java.util.function.UnaryOperator;

public class ItemUtil {

    public static final String LORE_FIX_PREFIX = "fogus_loren-";
    public static final String NAME_FIX_PREFIX = "fogus_namel-";
    public static final  String                     TAG_SPLITTER = "__x__";
    private static final NexEngine ENGINE;
    private static final Map<String, NamespacedKey> LORE_KEYS_CACHE;
    private static final Map<String, NamespacedKey> NAME_KEYS_CACHE;

    static {
        ENGINE = NexEngine.get();
        LORE_KEYS_CACHE = new HashMap<>();
        NAME_KEYS_CACHE = new HashMap<>();
    }

    public static void clear() {
        LORE_KEYS_CACHE.clear();
        NAME_KEYS_CACHE.clear();
    }

    public static int addToLore(@NotNull List<String> lore, int pos, @NotNull String value) {
        if (pos >= lore.size() || pos < 0) {
            lore.add(value);
        }
        else {
            lore.add(pos, value);
        }
        return pos < 0 ? pos : pos + 1;
    }

    public static void addLore(@NotNull ItemStack item, @NotNull String id, @NotNull String text, int pos) {
        String[] lines = text.split(TAG_SPLITTER);
        addLore(item, id, Arrays.asList(lines), pos);
    }

    public static void addLore(@NotNull ItemStack item, @NotNull String id, @NotNull List<String> text, int pos) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        List<String> lore = meta.getLore();
        if (lore == null) lore = new ArrayList<>();

        text = StringUtil.color(text);
        StringBuilder loreTag = new StringBuilder();

        delLore(item, id);
        for (String line : text) {
            pos = addToLore(lore, pos, line);

            if (loreTag.length() > 0)
                loreTag.append(TAG_SPLITTER);
            loreTag.append(line);
        }

        meta.setLore(lore);
        item.setItemMeta(meta);

        addLoreTag(item, id, loreTag.toString());
    }

    public static void delLore(@NotNull ItemStack item, @NotNull String id) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        List<String> lore = meta.getLore();
        if (lore == null) return;

        int index = getLoreIndex(item, id, 0);
        if (index < 0) return;

        int lastIndex = getLoreIndex(item, id, 1);
        int diff = lastIndex - index;

        for (int i = 0; i < (diff + 1); i++) {
            lore.remove(index);
        }

        meta.setLore(lore);
        item.setItemMeta(meta);

        delLoreTag(item, id);
    }

    public static int getLoreIndex(@NotNull ItemStack item, @NotNull String id) {
        return getLoreIndex(item, id, 0);
    }

    public static int getLoreIndex(@NotNull ItemStack item, @NotNull String id, int type) {
        String storedText = PDCUtil.getStringData(item, ItemUtil.getLoreKey(id));
        if (storedText == null) return -1;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return -1;

        List<String> lore = meta.getLore();
        if (lore == null) return -1;

        String[] lines = storedText.split(TAG_SPLITTER);
        String lastText = null;
        int count = 0;

        if (type == 0) {
            for (String line : lines) {
                lastText = line;
                if (!StringUtil.colorOff(lastText).isEmpty()) {
                    break;
                }
                count--;
            }
        }
        else {
            for (int i = lines.length; i > 0; i--) {
                lastText = lines[i - 1];
                if (!StringUtil.colorOff(lastText).isEmpty()) {
                    break;
                }
                count++;
            }
        }

        if (lastText == null)
            return -1;

        int index = lore.indexOf(lastText) + count;

        // Clean up invalid lore tags.
        if (index < 0) {
            delLoreTag(item, id);
        }
        return index;
    }

    @NotNull
    private static NamespacedKey getLoreKey(@NotNull String id2) {
        String id = id2.toLowerCase();
        return LORE_KEYS_CACHE.computeIfAbsent(id, key -> new NamespacedKey(ENGINE, LORE_FIX_PREFIX + id));
    }

    @NotNull
    private static NamespacedKey getNameKey(@NotNull String id2) {
        String id = id2.toLowerCase();
        return NAME_KEYS_CACHE.computeIfAbsent(id, key -> new NamespacedKey(ENGINE, NAME_FIX_PREFIX + id));
    }

    public static void addLoreTag(@NotNull ItemStack item, @NotNull String id, @NotNull String text) {
        ItemUtil.addLoreTag(item, ItemUtil.getLoreKey(id), text);
    }

    public static void addLoreTag(@NotNull ItemStack item, @NotNull NamespacedKey key, @NotNull String text) {
        PDCUtil.setData(item, key, text);
    }

    public static void delLoreTag(@NotNull ItemStack item, @NotNull String id) {
        ItemUtil.delLoreTag(item, ItemUtil.getLoreKey(id));
    }

    public static void delLoreTag(@NotNull ItemStack item, @NotNull NamespacedKey key) {
        PDCUtil.removeData(item, key);
    }

    @Nullable
    public static String getLoreTag(@NotNull ItemStack item, @NotNull String id) {
        return ItemUtil.getLoreTag(item, ItemUtil.getLoreKey(id));
    }

    @Nullable
    public static String getLoreTag(@NotNull ItemStack item, @NotNull NamespacedKey key) {
        return PDCUtil.getStringData(item, key);
    }

    public static void addNameTag(@NotNull ItemStack item, @NotNull String id, @NotNull String text) {
        PDCUtil.setData(item, ItemUtil.getNameKey(id), text);
    }

    public static void delNameTag(@NotNull ItemStack item, @NotNull String id) {
        PDCUtil.removeData(item, ItemUtil.getNameKey(id));
    }

    @Nullable
    public static String getNameTag(@NotNull ItemStack item, @NotNull String id) {
        return PDCUtil.getStringData(item, ItemUtil.getNameKey(id));
    }

    @NotNull
    public static String getItemName(@NotNull ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null && meta.hasDisplayName()) {
            return meta.getDisplayName();
        }
        return ENGINE.lang().getEnum(item.getType());
    }

    @NotNull
    public static List<String> getLore(@NotNull ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null || meta.getLore() == null) return new ArrayList<>();
        return meta.getLore();
    }

    public static void addSkullTexture(@NotNull ItemStack item, @NotNull String value) {
        ItemUtil.addSkullTexture(item, value, "");
    }

    public static void addSkullTexture(@NotNull ItemStack item, @NotNull String value, @Nullable String id) {
        if (item.getType() != Material.PLAYER_HEAD) return;

        UUID uuid = ConfigManager.getTempUUID(id == null ? "" : id);
        if (uuid == null) uuid = UUID.randomUUID();

        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta == null) return;

        GameProfile profile = new GameProfile(uuid, null);
        profile.getProperties().put("textures", new Property("textures", value));
        Reflex.setFieldValue(meta, "profile", profile);

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

    public static void applyPlaceholderAPI(@NotNull Player player, @NotNull ItemStack item) {
        if (!Hooks.hasPlaceholderAPI())
            return;
        replace(item, str -> StringUtil.color(PlaceholderAPI.setPlaceholders(player, str)));
    }

    public static void replace(@NotNull ItemStack item, @NotNull UnaryOperator<String> cs) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        String name = cs.apply(meta.hasDisplayName() ? meta.getDisplayName() : "");
        meta.setDisplayName(name);

        List<String> lore = meta.getLore();
        List<String> lore2 = new ArrayList<>();
        if (lore != null) {
            lore.replaceAll(cs);
            lore.forEach(line -> lore2.addAll(Arrays.asList(line.split("\\n"))));
            meta.setLore(lore2);
        }
        item.setItemMeta(meta);
    }

    public static void replaceLore(@NotNull ItemStack item, @NotNull String placeholder, @NotNull List<String> replacer) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return;

        List<String> lore = meta.getLore();
        if (lore == null)
            return;

        List<String> lore2 = new ArrayList<>();
        for (String line : lore) {
            if (line.contains(placeholder)) {
                replacer.forEach(lineRep -> {
                    lore2.add(line.replace(placeholder, lineRep));
                });
                continue;
            }
            lore2.add(line);
        }
        meta.setLore(lore2);

        item.setItemMeta(meta);
    }

    public static boolean isWeapon(@NotNull ItemStack item) {
        return ENGINE.getNMS().isWeapon(item);
    }

    public static boolean isTool(@NotNull ItemStack item) {
        return ENGINE.getNMS().isTool(item);
    }

    public static boolean isArmor(@NotNull ItemStack item) {
        return ENGINE.getNMS().isArmor(item);
    }

    public static boolean isBow(@NotNull ItemStack item) {
        return item.getType() == Material.BOW || item.getType() == Material.CROSSBOW;
    }

    public static boolean isSword(@NotNull ItemStack item) {
        return ENGINE.getNMS().isSword(item);
    }

    public static boolean isAxe(@NotNull ItemStack item) {
        return ENGINE.getNMS().isAxe(item);
    }

    public static boolean isTrident(@NotNull ItemStack item) {
        return ENGINE.getNMS().isTrident(item);
    }

    public static boolean isPickaxe(@NotNull ItemStack item) {
        return ENGINE.getNMS().isPickaxe(item);
    }

    public static boolean isShovel(@NotNull ItemStack item) {
        return ENGINE.getNMS().isShovel(item);
    }

    public static boolean isHoe(@NotNull ItemStack item) {
        return ENGINE.getNMS().isHoe(item);
    }

    public static boolean isHelmet(@NotNull ItemStack item) {
        return ENGINE.getNMS().isHelmet(item);
    }

    public static boolean isChestplate(@NotNull ItemStack item) {
        return ENGINE.getNMS().isChestplate(item);
    }

    public static boolean isLeggings(@NotNull ItemStack item) {
        return ENGINE.getNMS().isLeggings(item);
    }

    public static boolean isBoots(@NotNull ItemStack item) {
        return ENGINE.getNMS().isBoots(item);
    }

    @NotNull
    public static EquipmentSlot[] getItemSlots(@NotNull ItemStack item) {
        if (isArmor(item)) {
            return new EquipmentSlot[]{getEquipmentSlotByItemType(item)};
        }
        return new EquipmentSlot[]{EquipmentSlot.HAND, EquipmentSlot.OFF_HAND};
    }

    @NotNull
    public static EquipmentSlot getEquipmentSlotByItemType(@NotNull ItemStack item) {
        String raw = item.getType().name();
        if (raw.contains("HELMET") || raw.contains("SKULL") || raw.contains("HEAD")) {
            return EquipmentSlot.HEAD;
        }
        if (raw.endsWith("CHESTPLATE") || raw.endsWith("ELYTRA")) {
            return EquipmentSlot.CHEST;
        }
        if (raw.endsWith("LEGGINGS")) {
            return EquipmentSlot.LEGS;
        }
        if (raw.endsWith("BOOTS")) {
            return EquipmentSlot.FEET;
        }
        if (item.getType() == Material.SHIELD) {
            return EquipmentSlot.OFF_HAND;
        }
        return EquipmentSlot.HAND;
    }

    @NotNull
    public static String toJson(@NotNull ItemStack item) {
        return ENGINE.getNMS().toJSON(item);
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
        return items.stream().map(ItemUtil::toBase64).filter(Objects::nonNull).toList();
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

package su.nexmedia.engine.api.config;

import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.NexEngine;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.Version;
import su.nexmedia.engine.api.menu.MenuItem;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.api.type.ClickType;
import su.nexmedia.engine.utils.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.IntStream;

public class JYML extends YamlConfiguration {

    private final File    file;
    private       boolean isChanged = false;

    public JYML(@NotNull String path, @NotNull String file) {
        this(new File(path, file));
    }

    public JYML(@NotNull File file) {
        if (Version.isAbove(Version.V1_17_R1)) {
            this.options().width(1000);
        }

        FileUtil.create(file);
        this.file = file;
        this.reload();
    }

    @NotNull
    public static JYML loadOrExtract(@NotNull NexPlugin<?> plugin, @NotNull String filePath) {
        if (!filePath.startsWith("/")) {
            filePath = "/" + filePath;
        }

        File file = new File(plugin.getDataFolder() + filePath);
        if (FileUtil.create(file)) {
            try {
                InputStream input = plugin.getClass().getResourceAsStream(filePath);
                if (input != null) FileUtil.copy(input, file);
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return new JYML(file);
    }

    @NotNull
    public static List<JYML> loadAll(@NotNull String path, boolean deep) {
        return FileUtil.getFiles(path, deep).stream().filter(file -> file.getName().endsWith(".yml")).map(JYML::new).toList();
    }

    public void initializeOptions(@NotNull Object from) {
        initializeOptions(from, this);
    }

    public static void initializeOptions(@NotNull Object from, @NotNull JYML cfg) {
        boolean isStatic = from instanceof Class;
        Class<?> clazz = isStatic ? (Class<?>) from : from.getClass();

        for (Field field : Reflex.getFields(clazz)) {
            if (!JOption.class.isAssignableFrom(field.getType())) continue;
            if (!field.trySetAccessible()) continue;

            try {
                JOption<?> option = (JOption<?>) field.get(from);
                option.read(cfg);
            }
            catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        cfg.saveChanges();
    }

    @NotNull
    public File getFile() {
        return this.file;
    }

    public void save() {
        try {
            this.save(this.file);
        }
        catch (IOException e) {
            NexEngine.get().error("Could not save config: " + file.getName());
            e.printStackTrace();
        }
    }

    public boolean saveChanges() {
        if (this.isChanged) {
            this.save();
            this.isChanged = false;
            return true;
        }
        return false;
    }

    public boolean reload() {
        try {
            this.load(this.file);
            this.isChanged = false;
            return true;
        }
        catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean addMissing(@NotNull String path, @Nullable Object val) {
        if (this.contains(path)) return false;
        this.set(path, val);
        return true;
    }

    @Override
    public void set(@NotNull String path, @Nullable Object value) {
        if (value instanceof JOption.Writer writer) {
            writer.write(this, path);
        }
        else {
            if (value instanceof String str) {
                value = StringUtil.colorRaw(str);
            }
            else if (value instanceof Collection<?> collection) {
                List<Object> list = new ArrayList<>(collection);
                list.replaceAll(obj -> obj instanceof String str ? StringUtil.colorRaw(str) : obj);
                value = list;
            }
            else if (value instanceof Location location) {
                value = LocationUtil.serialize(location);
            }
            else if (value instanceof Enum<?> en) {
                value = en.name();
            }
            super.set(path, value);
        }
        this.isChanged = true;
    }

    public void setComments(@NotNull String path, @Nullable String... comments) {
        this.setComments(path, Arrays.asList(comments));
    }

    public void setInlineComments(@NotNull String path, @Nullable String... comments) {
        this.setInlineComments(path, Arrays.asList(comments));
    }

    @Override
    public void setComments(@NotNull String path, @Nullable List<String> comments) {
        if (Version.isBehind(Version.V1_18_R2)) return;
        if (this.getComments(path).equals(comments)) return;

        super.setComments(path, comments);
        this.isChanged = true;
    }

    @Override
    public void setInlineComments(@NotNull String path, @Nullable List<String> comments) {
        if (Version.isBehind(Version.V1_18_R2)) return;
        super.setInlineComments(path, comments);
    }

    public boolean remove(@NotNull String path) {
        if (!this.contains(path)) return false;
        this.set(path, null);
        return true;
    }

    @NotNull
    public Set<String> getSection(@NotNull String path) {
        ConfigurationSection section = this.getConfigurationSection(path);
        return section == null ? Collections.emptySet() : section.getKeys(false);
    }

    @Override
    @Nullable
    public String getString(@NotNull String path) {
        String str = super.getString(path);
        return str == null || str.isEmpty() ? null : str;
    }

    @Override
    @NotNull
    public String getString(@NotNull String path, @Nullable String def) {
        String str = super.getString(path, def);
        return str == null ? "" : str;
    }

    @NotNull
    public Set<String> getStringSet(@NotNull String path) {
        return new HashSet<>(this.getStringList(path));
    }

    @Override
    @Nullable
    public Location getLocation(@NotNull String path) {
        String raw = this.getString(path);
        return raw == null ? null : LocationUtil.deserialize(raw);
    }

    public int[] getIntArray(@NotNull String path) {
        int[] slots = new int[0];

        String str = this.getString(path);
        return str == null ? slots : StringUtil.getIntArray(str);
    }

    public void setIntArray(@NotNull String path, int[] arr) {
        if (arr == null) {
            this.set(path, null);
            return;
        }
        this.set(path, String.join(",", IntStream.of(arr).boxed().map(String::valueOf).toList()));
    }

    @Nullable
    public <T extends Enum<T>> T getEnum(@NotNull String path, @NotNull Class<T> clazz) {
        return CollectionsUtil.getEnum(this.getString(path, ""), clazz);
    }

    @NotNull
    public <T extends Enum<T>> T getEnum(@NotNull String path, @NotNull Class<T> clazz, @NotNull T def) {
        @Nullable T val = this.getEnum(path, clazz);
        return val == null ? def : val;
    }

    @NotNull
    public <T extends Enum<T>> List<T> getEnumList(@NotNull String path, @NotNull Class<T> clazz) {
        return this.getStringSet(path).stream().map(str -> CollectionsUtil.getEnum(str, clazz))
            .filter(Objects::nonNull).toList();
    }

    /*@NotNull
    public Set<FireworkEffect> getFireworkEffects(@NotNull String path) {
        Set<FireworkEffect> effects = new HashSet<>();
        for (String sId : this.getSection(path)) {
            String path2 = path + "." + sId + ".";
            FireworkEffect.Type type = this.getEnum(path2 + "Type", FireworkEffect.Type.class);
            if (type == null) continue;

            boolean flicker = this.getBoolean(path2 + "Flicker");
            boolean trail = this.getBoolean(path2 + "Trail");

            Set<Color> colors = new HashSet<>();
            for (String colorRaw : this.getStringList(path2 + "Colors")) {
                colors.add(StringUtil.parseColor(colorRaw));
            }

            Set<Color> fadeColors = new HashSet<>();
            for (String colorRaw : this.getStringList(path2 + "Fade_Colors")) {
                fadeColors.add(StringUtil.parseColor(colorRaw));
            }

            FireworkEffect.Builder builder = FireworkEffect.builder()
                .with(type).flicker(flicker).trail(trail).withColor(colors).withFade(fadeColors);
            effects.add(builder.build());
        }

        return effects;
    }*/

    @NotNull
    public ItemStack getItem(@NotNull String path, @Nullable ItemStack def) {
        ItemStack item = this.getItem(path);
        return item.getType().isAir() && def != null ? def : item;
    }

    @NotNull
    public ItemStack getItem(@NotNull String path) {
        if (!path.isEmpty() && !path.endsWith(".")) path = path + ".";

        /*if (this.getBoolean(path + "Encoded.Use")) {
            ItemStack item = this.getItemEncoded(path + "Encoded.Value");
            return item == null ? new ItemStack(Material.AIR) : item;
        }*/

        Material material = Material.getMaterial(this.getString(path + "Material", "").toUpperCase());
        if (material == null || material == Material.AIR) return new ItemStack(Material.AIR);

        ItemStack item = new ItemStack(material);
        item.setAmount(this.getInt(path + "Amount", 1));

        String headTexture = this.getString(path + "Head_Texture", "");
        if (!headTexture.isEmpty()) {
            ItemUtil.setSkullTexture(item, headTexture);
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        int durability = this.getInt(path + "Durability");
        if (durability > 0 && meta instanceof Damageable damageable) {
            damageable.setDamage(durability);
        }

        String name = this.getString(path + "Name");
        meta.setDisplayName(name != null ? StringUtil.color(name) : null);
        meta.setLore(StringUtil.color(this.getStringList(path + "Lore")));

        for (String sKey : this.getSection(path + "Enchants")) {
            Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(sKey.toLowerCase()));
            if (enchantment == null) continue;

            int eLvl = this.getInt(path + "Enchants." + sKey);
            if (eLvl <= 0) continue;

            meta.addEnchant(enchantment, eLvl, true);
        }

        int model = this.getInt(path + "Custom_Model_Data");
        meta.setCustomModelData(model != 0 ? model : null);

        List<String> flags = this.getStringList(path + "Item_Flags");
        if (flags.contains(Placeholders.WILDCARD)) {
            meta.addItemFlags(ItemFlag.values());
        }
        else {
            flags.stream().map(str -> CollectionsUtil.getEnum(str, ItemFlag.class)).filter(Objects::nonNull).forEach(meta::addItemFlags);
        }

        String colorRaw = this.getString(path + "Color");
        if (colorRaw != null && !colorRaw.isEmpty()) {
            Color color = StringUtil.parseColor(colorRaw);
            if (meta instanceof LeatherArmorMeta armorMeta) {
                armorMeta.setColor(color);
            }
            else if (meta instanceof PotionMeta potionMeta) {
                potionMeta.setColor(color);
            }
        }

        meta.setUnbreakable(this.getBoolean(path + "Unbreakable"));
        item.setItemMeta(meta);

        return item;
    }

    @NotNull
    public MenuItem getMenuItem(@NotNull String path) {
        return this.getMenuItem(path, MenuItemType.class);
    }

    @NotNull
    public <T extends Enum<T>> MenuItem getMenuItem(@NotNull String path, @Nullable Class<T> clazzEnum) {
        if (!path.endsWith(".")) path = path + ".";

        String[] pathSplit = path.split("\\.");
        String id = pathSplit[pathSplit.length - 1];
        if (id == null || id.isEmpty()) id = UUID.randomUUID().toString();

        for (String displayId : this.getSection(path + "Display")) {
            String path2 = path + "Display." + displayId + ".";
            ItemStack dItem = this.getItem(path2 + "Item");
            this.setItem(path + "Item", dItem);
            break;
        }
        this.remove(path + "Display");


        Enum<?> type = clazzEnum == null ? MenuItemType.NONE : this.getEnum(path + "Type", clazzEnum, clazzEnum.getEnumConstants()[0]);
        if (type != MenuItemType.NONE) {
            this.addMissing(path + "Priority", 5);
        }

        ItemStack item = this.getItem(path + "Item");
        int[] slots = this.getIntArray(path + "Slots");
        int priority = this.getInt(path + "Priority");

        Map<ClickType, List<String>> clickCommands = new HashMap<>();
        for (String sType : this.getSection(path + "Click_Actions")) {
            ClickType clickType = CollectionsUtil.getEnum(sType, ClickType.class);
            if (clickType == null) continue;

            clickCommands.put(clickType, this.getStringList(path + "Click_Actions." + sType));
        }

        this.saveChanges();
        return new MenuItem(id, type, slots, priority, item, clickCommands);
    }

    public void setItem(@NotNull String path, @Nullable ItemStack item) {
        if (item == null) {
            this.set(path, null);
            return;
        }

        if (!path.endsWith(".")) path = path + ".";
        this.set(path.substring(0, path.length() - 1), null);

        Material material = item.getType();
        this.set(path + "Material", material.name());
        this.set(path + "Amount", item.getAmount() <= 1 ? null : item.getAmount());
        this.set(path + "Head_Texture", ItemUtil.getSkullTexture(item));

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        /*boolean hasNbt = !meta.getPersistentDataContainer().isEmpty();
        if (hasNbt) {
            this.set(path + "Encoded.Use", true);
            this.setItemEncoded(path + "Encoded.Value", item);
        }
        else {
            if (this.contains(path + "Encoded.Value")) {
                this.set(path + "Encoded.Use", false);
            }
            else {
                this.remove(path + "Encoded");
            }
        }*/

        if (meta instanceof Damageable damageable) {
            this.set(path + "Durability", damageable.getDamage() <= 0 ? null : damageable.getDamage());
        }

        this.set(path + "Name", meta.getDisplayName().isEmpty() ? null : meta.getDisplayName());
        this.set(path + "Lore", meta.getLore());

        for (Map.Entry<Enchantment, Integer> entry : meta.getEnchants().entrySet()) {
            this.set(path + "Enchants." + entry.getKey().getKey().getKey(), entry.getValue());
        }
        this.set(path + "Custom_Model_Data", meta.hasCustomModelData() ? meta.getCustomModelData() : null);

        Color color = null;
        String colorRaw = null;
        if (meta instanceof PotionMeta potionMeta) {
            color = potionMeta.getColor();
        }
        else if (meta instanceof LeatherArmorMeta armorMeta) {
            color = armorMeta.getColor();
        }
        if (color != null) {
            colorRaw = color.getRed() + "," + color.getGreen() + "," + color.getBlue() + ",";
        }
        this.set(path + "Color", colorRaw);

        List<String> itemFlags = new ArrayList<>(meta.getItemFlags().stream().map(ItemFlag::name).toList());
        this.set(path + "Item_Flags", itemFlags.isEmpty() ? null : itemFlags);
        this.set(path + "Unbreakable", meta.isUnbreakable() ? true : null);
    }

    @Nullable
    public ItemStack getItemEncoded(@NotNull String path) {
        String code = this.getString(path);
        if (code == null) return null;

        return ItemUtil.fromBase64(code);
    }

    public void setItemEncoded(@NotNull String path, @Nullable ItemStack item) {
        this.set(path, item == null ? null : ItemUtil.toBase64(item));
    }

    @NotNull
    public ItemStack[] getItemsEncoded(@NotNull String path) {
        return ItemUtil.fromBase64(this.getStringList(path));
    }

    public void setItemsEncoded(@NotNull String path, @NotNull List<ItemStack> item) {
        List<String> code = new ArrayList<>(ItemUtil.toBase64(item));
        this.set(path, code);
    }
}

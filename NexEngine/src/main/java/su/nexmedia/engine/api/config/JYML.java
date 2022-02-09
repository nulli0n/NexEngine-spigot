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
import su.nexmedia.engine.actions.ActionManipulator;
import su.nexmedia.engine.api.craft.CraftRecipe;
import su.nexmedia.engine.api.craft.FurnaceRecipe;
import su.nexmedia.engine.api.menu.MenuItem;
import su.nexmedia.engine.api.menu.MenuItemDisplay;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.api.type.ClickType;
import su.nexmedia.engine.utils.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class JYML extends YamlConfiguration {

    private final File    file;
    private       boolean isChanged = false;

    public JYML(@NotNull String path, @NotNull String file) {
        this(new File(path, file));
    }

    public JYML(@NotNull File file) {
        FileUtil.create(file);
        this.file = file;
        this.reload();
    }

    @NotNull
    public static JYML loadOrExtract(@NotNull NexPlugin<?> plugin, @NotNull String filePath) {
        if (!plugin.getDataFolder().exists()) {
            FileUtil.mkdir(plugin.getDataFolder());
        }
        if (!filePath.startsWith("/")) {
            filePath = "/" + filePath;
        }

        File file = new File(plugin.getDataFolder() + filePath);
        if (!file.exists()) {
            FileUtil.create(file);
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
        List<JYML> configs = new ArrayList<>();
        for (File file : FileUtil.getFiles(path, deep)) {
            configs.add(new JYML(file));
        }
        return configs;
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
    public void set(@NotNull String path, @Nullable Object o) {
        /*
         * if (o != null && o instanceof String) { String s = (String) o; s =
         * s.replace('§', '&'); o = s; }
         */
        if (o instanceof Set) {
            o = new ArrayList<>((Set<?>) o);
        }
        else if (o instanceof Location) {
            o = LocationUtil.serialize((Location) o);
        }
        super.set(path, o);
        this.isChanged = true;
    }

    public boolean remove(@NotNull String path) {
        if (!this.contains(path))
            return false;
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
        if (str == null) {
            return def != null ? def : "";
        }
        return str;
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

    @Deprecated
    public void setLocation(@NotNull String path, @Nullable Location loc) {
        this.set(path, loc == null ? null : LocationUtil.serialize(loc));
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
    }

    @NotNull
    @Deprecated
    public ItemStack getItem(@NotNull String path) {
        return this.getItem(path, false);
    }

    @NotNull
    @Deprecated
    public ItemStack getItem(@NotNull String path, boolean id) {
        if (!path.isEmpty() && !path.endsWith(".")) path = path + ".";

        Material material = Material.getMaterial(this.getString(path + "material", "").toUpperCase());
        if (material == null || material == Material.AIR) return this.getItemNew(path);

        ItemStack item = new ItemStack(material);
        item.setAmount(this.getInt(path + "amount", 1));

        String hash = this.getString(path + "skull-hash", this.getString(path + "head-texture"));
        if (!hash.isEmpty()) {
            if (id) {
                String idz = this.getFile().getName().replace(".yml", "");
                ItemUtil.addSkullTexture(item, hash, idz);
            }
            else {
                ItemUtil.addSkullTexture(item, hash);
            }
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        int durability = this.getInt(path + "durability");
        if (durability > 0 && meta instanceof Damageable damageable) {
            damageable.setDamage(durability);
        }

        String name = this.getString(path + "name");
        meta.setDisplayName(name != null ? StringUtil.color(name) : null);
        meta.setLore(StringUtil.color(this.getStringList(path + "lore")));

        if (this.contains(path + "enchants")) {
            for (String sKey : this.getSection(path + "enchants")) {
                Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(sKey.toLowerCase()));
                if (enchantment == null)
                    continue;

                int eLvl = this.getInt(path + "enchants." + sKey);
                if (eLvl <= 1)
                    continue;

                meta.addEnchant(enchantment, eLvl, true);
            }
        }
        else if (this.getBoolean(path + "enchanted")) {
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
        }

        int model = this.getInt(path + "model-data", this.getInt(path + "Custom_Model_Data"));
        meta.setCustomModelData(model != 0 ? model : null);

        List<String> flags = this.getStringList(path + "item-flags");
        if (flags.contains(Constants.MASK_ANY)) {
            meta.addItemFlags(ItemFlag.values());
        }
        else {
            for (String flag : flags) {
                ItemFlag itemFlag = CollectionsUtil.getEnum(flag, ItemFlag.class);
                if (itemFlag != null)
                    meta.addItemFlags(itemFlag);
            }
        }

        String colorRaw = this.getString(path + "color");
        if (colorRaw != null && !colorRaw.isEmpty()) {
            Color color = StringUtil.parseColor(colorRaw);
            if (meta instanceof LeatherArmorMeta armorMeta) {
                armorMeta.setColor(color);
            }
            else if (meta instanceof PotionMeta potionMeta) {
                potionMeta.setColor(color);
            }
        }

        meta.setUnbreakable(this.getBoolean(path + "unbreakable"));
        item.setItemMeta(meta);

        return item;
    }

    @NotNull
    public ItemStack getItemNew(@NotNull String path) {
        return this.getItemNew(path, null);
    }

    @NotNull
    public ItemStack getItemNew(@NotNull String path, @Nullable String id) {
        if (!path.isEmpty() && !path.endsWith(".")) path = path + ".";

        Material material = Material.getMaterial(this.getString(path + "Material", "").toUpperCase());
        if (material == null || material == Material.AIR) return new ItemStack(Material.AIR);

        ItemStack item = new ItemStack(material);
        item.setAmount(this.getInt(path + "Amount", 1));

        String headTexture = this.getString(path + "Head_Texture", "");
        if (!headTexture.isEmpty()) {
            ItemUtil.addSkullTexture(item, headTexture, id);
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
        if (flags.contains(Constants.MASK_ANY)) {
            meta.addItemFlags(ItemFlag.values());
        }
        else {
            for (String flag : flags) {
                ItemFlag itemFlag = CollectionsUtil.getEnum(flag, ItemFlag.class);
                if (itemFlag != null)
                    meta.addItemFlags(itemFlag);
            }
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

        int[] slots = this.getIntArray(path + "Slots");
        Enum<?> type = clazzEnum == null ? MenuItemType.NONE : this.getEnum(path + "Type", clazzEnum, clazzEnum.getEnumConstants()[0]);

        Map<String, MenuItemDisplay> displayMap = new HashMap<>();
        for (String displayId : this.getSection(path + "Display")) {
            String path2 = path + "Display." + displayId + ".";
            int dPriority = this.getInt(path2 + "Priority");
            ItemStack dItem = this.getItem(path2 + "Item");
            if (dItem.getType().isAir()) dItem = this.getItemNew(path2 + "Item");

            List<String> dConditions = this.getStringList(path2 + "Conditions");

            MenuItemDisplay display = new MenuItemDisplay(displayId, dPriority, dItem, dConditions);
            displayMap.put(display.getId(), display);
        }

        int animationInterval = this.getInt(path + "Animation.Interval");
        String[] animationFrames = this.getString(path + "Animation.Switch.Display_Names", "").split(",");
        boolean animationIgnoreUnvailableFrames = this.getBoolean(path + "Animation.Switch.Ignore_Unavailable_Displays");
        boolean animationRandomOrder = this.getBoolean(path + "Animation.Switch.Random_Order");

        Map<ClickType, ActionManipulator> customClicks = new HashMap<>();
        for (String sType : this.getSection(path + "Click_Actions")) {
            ClickType clickType = CollectionsUtil.getEnum(sType, ClickType.class);
            if (clickType == null) continue;

            ActionManipulator actions = new ActionManipulator(this, path + "Click_Actions." + sType);
            customClicks.put(clickType, actions);
        }

        return new MenuItem(
            id, type, slots, displayMap, customClicks,
            animationInterval, animationFrames, animationIgnoreUnvailableFrames, animationRandomOrder);
    }

    @Deprecated
    public void setItem(@NotNull String path, @Nullable ItemStack item) {
        if (item == null) {
            this.set(path, null);
            return;
        }

        if (!path.endsWith("."))
            path = path + ".";
        this.set(path.substring(0, path.length() - 1), null);

        Material material = item.getType();
        this.set(path + "material", material.name());
        this.set(path + "amount", item.getAmount());
        this.set(path + "head-texture", ItemUtil.getSkullTexture(item));

        ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return;

        if (meta instanceof Damageable) {
            int durability = ((Damageable) meta).getDamage();
            this.set(path + "durability", durability);
        }

        if (meta.hasDisplayName()) {
            this.set(path + "name", StringUtil.colorRaw(meta.getDisplayName()));
        }

        List<String> lore = meta.getLore();
        if (lore != null) {
            List<String> loreRaw = new ArrayList<>();
            lore.forEach(line -> loreRaw.add(StringUtil.colorRaw(line)));
            this.set(path + "lore", loreRaw);
        }

        this.set(path + "enchanted", meta.hasEnchants());
        this.set(path + "custom-model-data", meta.hasCustomModelData() ? meta.getCustomModelData() : null);

        Color color = null;
        String colorRaw = null;
        if (meta instanceof PotionMeta) {
            PotionMeta pm = (PotionMeta) meta;
            color = pm.getColor();
        }
        else if (meta instanceof LeatherArmorMeta) {
            LeatherArmorMeta lm = (LeatherArmorMeta) meta;
            color = lm.getColor();
        }
        if (color != null) {
            colorRaw = new StringBuilder().append(color.getRed()).append(",").append(color.getGreen()).append(",").append(color.getBlue()).append(",").toString();
        }
        this.set(path + "color", colorRaw);

        List<String> itemFlags = meta.getItemFlags().stream().map(ItemFlag::name).collect(Collectors.toList());
        this.set(path + "item-flags", itemFlags);
        this.set(path + "unbreakable", meta.isUnbreakable());
    }

    public void setItemNew(@NotNull String path, @Nullable ItemStack item) {
        if (item == null) {
            this.set(path, null);
            return;
        }

        if (!path.endsWith(".")) path = path + ".";
        this.set(path.substring(0, path.length() - 1), null);

        Material material = item.getType();
        this.set(path + "Material", material.name());
        this.set(path + "Amount", item.getAmount());
        this.set(path + "Head_Texture", ItemUtil.getSkullTexture(item));

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        if (meta instanceof Damageable damageable) {
            int durability = damageable.getDamage();
            this.set(path + "Durability", durability);
        }

        if (meta.hasDisplayName()) {
            this.set(path + "Name", StringUtil.colorRaw(meta.getDisplayName()));
        }

        List<String> lore = meta.getLore();
        if (lore != null) {
            List<String> loreRaw = new ArrayList<>();
            lore.forEach(line -> loreRaw.add(StringUtil.colorRaw(line)));
            this.set(path + "Lore", loreRaw);
        }

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
        this.set(path + "Item_Flags", itemFlags);
        this.set(path + "Unbreakable", meta.isUnbreakable());
    }

    @Nullable
    public ItemStack getItem64(@NotNull String path) {
        String code = this.getString(path);
        if (code == null) return null;

        return ItemUtil.fromBase64(code);
    }

    public void setItem64(@NotNull String path, @Nullable ItemStack item) {
        if (item == null) {
            this.set(path, null);
        }
        else {
            String code = ItemUtil.toBase64(item);
            this.set(path, code);
        }
    }

    @NotNull
    public ItemStack[] getItemList64(@NotNull String path) {
        List<String> list = this.getStringList(path);
        return ItemUtil.fromBase64(list);
    }

    public void setItemList64(@NotNull String path, @NotNull List<ItemStack> item) {
        List<String> code = new ArrayList<>(ItemUtil.toBase64(item));
        this.set(path, code);
    }

    @Nullable
    @Deprecated
    public CraftRecipe getCraftRecipe(@NotNull NexPlugin<?> plugin, @NotNull String id, @NotNull String path) {
        if (!path.endsWith(".")) path += ".";

        boolean shape = this.getBoolean(path + "Shaped");
        ItemStack result;
        if (this.isConfigurationSection(path + "Result")) {
            result = this.getItem(path + "Result");
        }
        else result = this.getItem64(path + "Result");
        if (result == null) return null;

        CraftRecipe recipe = new CraftRecipe(plugin, id, result, shape);
        int ingCount = 0;
        for (String ingId : this.getSection(path + "Ingredients")) {
            String path2 = path + "Ingredients." + ingId;

            ItemStack ingredient;
            if (this.isConfigurationSection(path2)) {
                ingredient = this.getItem(path2);
            }
            else ingredient = this.getItem64(path2);
            if (ingredient == null) continue;

            recipe.addIngredient(ingCount++, ingredient);
        }

        return recipe;
    }

    @Deprecated
    public void setRecipe(@NotNull String path, @Nullable CraftRecipe recipe) {
        if (!path.endsWith(".")) path += ".";
        if (recipe == null) {
            if (path.endsWith(".")) path = path.substring(0, path.length() - 1);
            this.set(path, null);
            return;
        }

        this.set(path + "Shaped", recipe.isShaped());
        this.setItem64(path + "Result", recipe.getResult());

        char[] ingName = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I'};
        ItemStack[] ingredients = recipe.getIngredients();
        for (int index = 0; index < ingredients.length; index++) {
            this.setItem64(path + "Ingredients." + ingName[index], ingredients[index]);
        }
    }

    @Nullable
    public FurnaceRecipe getFurnaceRecipe(@NotNull NexPlugin<?> plugin, @NotNull String id, @NotNull String path) {
        if (!path.endsWith(".")) path += ".";

        ItemStack input;
        if (this.isConfigurationSection(path + "Input")) {
            input = this.getItemNew(path + "Input");
        }
        else input = this.getItem64(path + "Input");

        ItemStack result;
        if (this.isConfigurationSection(path + "Result")) {
            result = this.getItemNew(path + "Result");
        }
        else result = this.getItem64(path + "Result");

        if (result == null || input == null) {
            return null;
        }

        float exp = (float) this.getDouble(path + "Exp");
        double time = this.getDouble(path + "Time");

        FurnaceRecipe recipe = new FurnaceRecipe(plugin, id, result, exp, time);
        recipe.addIngredient(input);

        return recipe;
    }

    public void setRecipe(@NotNull String path, @Nullable FurnaceRecipe recipe) {
        if (!path.endsWith(".")) path += ".";
        if (recipe == null) {
            if (path.endsWith(".")) path = path.substring(0, path.length() - 1);
            this.set(path, null);
            return;
        }

        this.setItem64(path + "Input", recipe.getInput());
        this.setItem64(path + "Result", recipe.getResult());
        this.set(path + "Exp", recipe.getExp());
        this.set(path + "Time", recipe.getTime() / 20D); // Turn to decimal seconds
    }
}

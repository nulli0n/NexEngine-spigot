package su.nexmedia.engine.lang;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.NexEngine;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.editor.EditorButtonType;
import su.nexmedia.engine.api.lang.LangKey;
import su.nexmedia.engine.api.lang.LangMessage;
import su.nexmedia.engine.api.manager.AbstractManager;
import su.nexmedia.engine.utils.Reflex;
import su.nexmedia.engine.utils.StringUtil;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LangManager<P extends NexPlugin<P>> extends AbstractManager<P> {

    protected JYML config;
    protected Map<String, LangMessage> messages;

    public LangManager(@NotNull P plugin) {
        super(plugin);
    }

    @Override
    protected void onLoad() {
        this.plugin.getConfigManager().extract("lang");
        this.messages = new HashMap<>();

        if (this.plugin.isEngine()) {
            this.setupEnum(EntityType.class);
            this.setupEnum(Material.class);
            this.setupEnum(GameMode.class);

            for (PotionEffectType type : PotionEffectType.values()) {
                this.getConfig().addMissing("PotionEffectType." + type.getName(), StringUtil.capitalizeFully(type.getName().replace("_", " ")));
            }

            for (Enchantment e : Enchantment.values()) {
                this.getConfig().addMissing("Enchantment." + e.getKey().getKey(), StringUtil.capitalizeFully(e.getKey().getKey().replace("_", " ")));
            }
            this.getConfig().saveChanges();
        }
        else {
            NexEngine.get().getLangManager().getMessages().forEach((key, message) -> {
                this.getMessages().put(key, new LangMessage(this.plugin, message.getRaw()));
            });
        }
    }

    @Override
    protected void onShutdown() {
        this.messages.clear();
    }

    @NotNull
    public JYML getConfig() {
        if (this.config == null) {
            this.config = JYML.loadOrExtract(plugin, "lang/messages_" + plugin.getConfig().getString("Plugin.Language", "en") + ".yml");
        }
        return config;
    }

    @NotNull
    public Map<String, LangMessage> getMessages() {
        return messages;
    }

    @NotNull
    public LangMessage getMessage(@NotNull LangKey key) {
        LangMessage message = this.getMessages().get(key.getPath());
        if (message == null) {
            message = this.loadMessage(key);
        }
        return message;
    }

    @Nullable
    public String getMessage(@NotNull String path) {
        String str = this.getConfig().getString(path);
        return str == null ? null : StringUtil.color(str);
    }

    @NotNull
    private LangMessage loadMessage(@NotNull LangKey key) {
        if (!this.getConfig().contains(key.getPath())) {
            String textDefault = key.getDefaultText();
            String[] textSplit = textDefault.split("\n");
            this.getConfig().set(key.getPath(), textSplit.length > 1 ? Arrays.asList(textSplit) : textDefault);
            this.getConfig().saveChanges();
        }

        List<String> list = this.getConfig().getStringList(key.getPath());
        String text = !list.isEmpty() ? String.join("\\n", list) : this.getConfig().getString(key.getPath(), "<Missing Message [" + key.getPath() + "]>");
        LangMessage message = new LangMessage(plugin, text);
        this.getMessages().put(key.getPath(), message);

        return message;
    }

    /**
     * Loads and sets missing messages from the specified class.
     * This method is used to generate the default language file or add new messages to it.
     * @param clazz A class to load LangKey messages from.
     */
    public void loadMissing(@NotNull Class<?> clazz) {
        for (Field field : Reflex.getFields(clazz)) {
            if (!LangKey.class.isAssignableFrom(field.getType())) {
                continue;
            }

            LangKey langKey;
            try {
                langKey = (LangKey) field.get(this);
            }
            catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
                continue;
            }

            // Do not load/set messages of super class(es) or if they are already present in the lang file.
            if (!field.getDeclaringClass().equals(clazz)) {
                continue;
            }
            if (!this.plugin.isEngine() && this.getConfig().contains(langKey.getPath())) {
                continue;
            }

            this.loadMessage(langKey);
        }
    }

    public void setupEnum(@NotNull Class<? extends Enum<?>> clazz) {
        if (!clazz.isEnum()) return;
        for (Object eName : clazz.getEnumConstants()) {
            if (eName == null) continue;

            String name = eName.toString();
            if (clazz == Material.class && name.startsWith("LEGACY")) continue;

            String path = clazz.getSimpleName() + "." + name;
            String val = StringUtil.capitalizeFully(name.replace("_", " "));
            this.getConfig().addMissing(path, val);
        }
    }

    @NotNull
    public String getEnum(@NotNull Enum<?> e) {
        String path = e.getDeclaringClass().getSimpleName() + "." + e.name();
        String locEnum = this.getMessage(path);
        if (locEnum == null && !this.plugin.isEngine()) {
            return NexPlugin.getEngine().getLangManager().getEnum(e);
        }
        return locEnum == null ? "null" : locEnum;
    }

    public void setupEditorEnum(@NotNull Class<? extends Enum<? extends EditorButtonType>> clazz) {
        if (!clazz.isEnum()) return;
        for (Object eName : clazz.getEnumConstants()) {
            if (!(eName instanceof EditorButtonType buttonType)) continue;
            if (buttonType.getMaterial().isAir()) continue;

            String nameRaw = buttonType.name();
            String path = "Editor." + clazz.getSimpleName() + "." + nameRaw + ".";

            this.getConfig().addMissing(path + "Name", buttonType.getName());
            this.getConfig().addMissing(path + "Lore", buttonType.getLore());

            buttonType.setName(this.getConfig().getString(path + "Name", nameRaw));
            buttonType.setLore(this.getConfig().getStringList(path + "Lore"));
        }
    }

    @NotNull
    public static String getPotionType(@NotNull PotionEffectType type) {
        String name = NexEngine.get().getLangManager().getMessage("PotionEffectType." + type.getName());
        return name == null ? type.getName() : name;
    }

    @NotNull
    public static String getEnchantment(@NotNull Enchantment enchantment) {
        String key = enchantment.getKey().getKey();
        LangManager<NexEngine> manager = NexEngine.get().getLangManager();

        manager.getConfig().addMissing("Enchantment." + key, StringUtil.capitalizeFully(key.replace("_", " ")));
        manager.getConfig().saveChanges();

        String name = manager.getMessage("Enchantment." + key);
        return name == null ? key : name;
    }

    @NotNull
    public static String getBoolean(boolean b) {
        return NexEngine.get().getLangManager().getMessage(b ? EngineLang.OTHER_YES : EngineLang.OTHER_NO).getLocalized();
    }
}

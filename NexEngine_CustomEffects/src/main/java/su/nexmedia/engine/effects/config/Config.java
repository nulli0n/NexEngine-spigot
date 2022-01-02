package su.nexmedia.engine.effects.config;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.effects.NexCustomEffects;
import su.nexmedia.engine.api.config.ConfigTemplate;
import su.nexmedia.engine.utils.StringUtil;

import java.util.List;

public class Config extends ConfigTemplate {

    public static String       ENTITY_EFFECTS_BLEED_NAME;
    public static List<String> ENTITY_EFFECTS_BLEED_DESCRIPTION;
    public static String       ENTITY_EFFECTS_BLIND_NAME;
    public static List<String> ENTITY_EFFECTS_BLIND_DESCRIPTION;
    public static String       ENTITY_EFFECTS_DISARM_NAME;
    public static List<String> ENTITY_EFFECTS_DISARM_DESCRIPTION;
    public static String       ENTITY_EFFECTS_DODGE_NAME;
    public static List<String> ENTITY_EFFECTS_DODGE_DESCRIPTION;
    public static String       ENTITY_EFFECTS_RESIST_NAME;
    public static List<String> ENTITY_EFFECTS_RESIST_DESCRIPTION;
    public static String       ENTITY_EFFECTS_ROOT_NAME;
    public static List<String> ENTITY_EFFECTS_ROOT_DESCRIPTION;
    public static String       ENTITY_EFFECTS_STUN_NAME;
    public static List<String> ENTITY_EFFECTS_STUN_DESCRIPTION;
    public static boolean  ENTITY_EFFECTS_BAR_ENABLED;
    public static String   ENTITY_EFFECTS_BAR_DURATION_TIME;
    public static String   ENTITY_EFFECTS_BAR_DURATION_TIME_CHARGES;
    public static String   ENTITY_EFFECTS_BAR_DURATION_CHARGES;
    public static BarColor ENTITY_EFFECTS_BAR_NEGATIVE_COLOR;
    public static BarStyle ENTITY_EFFECTS_BAR_NEGATIVE_STYLE;
    public static String   ENTITY_EFFECTS_BAR_NEGATIVE_TITLE;
    public static BarColor ENTITY_EFFECTS_BAR_POSITIVE_COLOR;
    public static BarStyle ENTITY_EFFECTS_BAR_POSITIVE_STYLE;
    public static String   ENTITY_EFFECTS_BAR_POSITIVE_TITLE;

    public Config(@NotNull NexCustomEffects plugin) {
        super(plugin);
    }

    @Override
    protected void load() {
        String path = "Entity.Effects.List.";
        ENTITY_EFFECTS_BLEED_NAME = StringUtil.color(cfg.getString(path + "Bleed.Name", "Bleed"));
        ENTITY_EFFECTS_BLEED_DESCRIPTION = StringUtil.color(cfg.getStringList(path + "Bleed.Description"));
        ENTITY_EFFECTS_BLIND_NAME = StringUtil.color(cfg.getString(path + "Blind.Name", "Blind"));
        ENTITY_EFFECTS_BLIND_DESCRIPTION = StringUtil.color(cfg.getStringList(path + "Blind.Description"));
        ENTITY_EFFECTS_DISARM_NAME = StringUtil.color(cfg.getString(path + "Disarm.Name", "Disarm"));
        ENTITY_EFFECTS_DISARM_DESCRIPTION = StringUtil.color(cfg.getStringList(path + "Disarm.Description"));
        ENTITY_EFFECTS_DODGE_NAME = StringUtil.color(cfg.getString(path + "Dodge.Name", "Dodge"));
        ENTITY_EFFECTS_DODGE_DESCRIPTION = StringUtil.color(cfg.getStringList(path + "Dodge.Description"));
        ENTITY_EFFECTS_RESIST_NAME = StringUtil.color(cfg.getString(path + "Resist.Name", "Resist"));
        ENTITY_EFFECTS_RESIST_DESCRIPTION = StringUtil.color(cfg.getStringList(path + "Resist.Description"));
        ENTITY_EFFECTS_ROOT_NAME = StringUtil.color(cfg.getString(path + "Root.Name", "Root"));
        ENTITY_EFFECTS_ROOT_DESCRIPTION = StringUtil.color(cfg.getStringList(path + "Root.Description"));
        ENTITY_EFFECTS_STUN_NAME = StringUtil.color(cfg.getString(path + "Stun.Name", "Stun"));
        ENTITY_EFFECTS_STUN_DESCRIPTION = StringUtil.color(cfg.getStringList(path + "Stun.Description"));

        path = "Entity.Effects.";
        ENTITY_EFFECTS_BAR_ENABLED = cfg.getBoolean(path + "Enabled");
        ENTITY_EFFECTS_BAR_DURATION_TIME = StringUtil.color(cfg.getString(path + "Duration_Format.Time", "%time%"));
        ENTITY_EFFECTS_BAR_DURATION_TIME_CHARGES = StringUtil.color(cfg.getString(path + "Duration_Format.Time_With_Charges", "%time% | x%charges%"));
        ENTITY_EFFECTS_BAR_DURATION_CHARGES = StringUtil.color(cfg.getString(path + "Duration_Format.Charges", "%charges%"));
        ENTITY_EFFECTS_BAR_NEGATIVE_COLOR = cfg.getEnum(path + "Negative.Color", BarColor.class, BarColor.RED);
        ENTITY_EFFECTS_BAR_NEGATIVE_STYLE = cfg.getEnum(path + "Negative.Style", BarStyle.class, BarStyle.SOLID);
        ENTITY_EFFECTS_BAR_NEGATIVE_TITLE = StringUtil.color(cfg.getString(path + "Negative.Title", "&c&l%effect% &8- &7%duration%"));
        ENTITY_EFFECTS_BAR_POSITIVE_COLOR = cfg.getEnum(path + "Positive.Color", BarColor.class, BarColor.BLUE);
        ENTITY_EFFECTS_BAR_POSITIVE_STYLE = cfg.getEnum(path + "Positive.Style", BarStyle.class, BarStyle.SOLID);
        ENTITY_EFFECTS_BAR_POSITIVE_TITLE = StringUtil.color(cfg.getString(path + "Positive.Title", "&9&l%effect% &8- &7%duration%"));
    }
}

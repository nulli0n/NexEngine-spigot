package su.nexmedia.engine.core.config;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.NexEngine;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.api.config.LangMessage;
import su.nexmedia.engine.api.config.LangTemplate;
import su.nexmedia.engine.utils.StringUtil;

public class CoreLang extends LangTemplate {

    public CoreLang(@NotNull NexPlugin<?> plugin) {
        super(plugin, plugin.getConfigManager().configLang, plugin.isEngine() ? null : NexEngine.get().lang());
    }
    public CoreLang(@NotNull NexPlugin<?> plugin, @Nullable LangTemplate parent) {
        super(plugin, plugin.getConfigManager().configLang, parent);
    }

    @Override
    public void setup() {
        super.setup();
        if (!this.plugin.isEngine()) return;

        this.setupEnum(EntityType.class);
        this.setupEnum(Material.class);
        this.setupEnum(GameMode.class);

        for (PotionEffectType type : PotionEffectType.values()) {
            this.config.addMissing("PotionEffectType." + type.getName(), StringUtil.capitalizeFully(type.getName().replace("_", " ")));
        }

        for (Enchantment e : Enchantment.values()) {
            this.config.addMissing("Enchantment." + e.getKey().getKey(), StringUtil.capitalizeFully(e.getKey().getKey().replace("_", " ")));
        }
    }

    public LangMessage Prefix = new LangMessage(this, "&7(&6%plugin%&7) &7");

    public LangMessage Core_Command_Usage       = new LangMessage(this, "&cUsage: &e/%command_label% &6%command_usage%");
    public LangMessage Core_Command_Help_List   = new LangMessage(this, """
        &6&m              &6&l[ &e&l%plugin% &7- &e&lCommands &6&l]&6&m              &7
        &7
        &7          &4&l<> &7- Required, &2&l[] &7- Optional.
        &7
        &6▪ &e/%command_label% &6%command_usage% &7- %command_description%
        &7
        """);
    public LangMessage Core_Command_Help_Desc   = new LangMessage(this, "Show help page.");
    public LangMessage Core_Command_Editor_Desc = new LangMessage(this, "Opens GUI Editor.");
    public LangMessage Core_Command_About_Desc  = new LangMessage(this, "Some info about the plugin.");
    public LangMessage Core_Command_Reload_Desc = new LangMessage(this, "Reload the plugin.");
    public LangMessage Core_Command_Reload_Done = new LangMessage(this, "Reloaded!");

    public LangMessage Core_Editor_Tips_Commands = new LangMessage(this, """
        {message: ~prefix: false;}&7
        &b&lCommand Tips:
        &7
        &2• &a'[CONSOLE] <command>' &2- Execute as Console.
        &2• (no prefix) &a'<command>' &2- Execute as a Player.
        &2• &a%player% &2- Player name placeholder.
        &7""");
    public LangMessage Core_Editor_Tips_Header = new LangMessage(this, """
        {message: ~prefix: false;}&7
        &e&lSUGGESTED (ALLOWED) VALUES:
        """);
    public LangMessage Core_Editor_Tips_Hint   = new LangMessage(this, "&b&nClick to select!");
    public LangMessage Core_Editor_Tips_Exit_Name                   = new LangMessage(this, "&b<Click this message to &dExit &bthe &dEdit Mode&b>");
    public LangMessage Core_Editor_Tips_Exit_Hint                   = new LangMessage(this, "&7Click to exit edit mode.");
    public LangMessage Core_Editor_Display_Edit_Format              = new LangMessage(this, "{message: ~type: TITLES; ~fadeIn: 10; ~stay: -1; ~fadeOut: 10;}" + "%title%" + "\n" + "&7%message%");
    public LangMessage Core_Editor_Display_Done_Title               = new LangMessage(this, "&a&lDone!");
    public LangMessage Core_Editor_Display_Edit_Title               = new LangMessage(this, "&a&lEditing...");
    public LangMessage Core_Editor_Display_Error_Title              = new LangMessage(this, "&c&lError!");
    public LangMessage Core_Editor_Display_Error_Number_Invalid     = new LangMessage(this, "&c&lInvalid number!");
    public LangMessage Core_Editor_Display_Error_Number_MustDecimal = new LangMessage(this, "&7Must be &cInteger &7or &cDecimal");
    public LangMessage Core_Editor_Display_Error_Number_MustInteger = new LangMessage(this, "&7Must be &cInteger");
    public LangMessage Core_Editor_Display_Error_Type_Title         = new LangMessage(this, "&c&lInvalid Type!");
    public LangMessage Core_Editor_Display_Error_Type_Values        = new LangMessage(this, "&7See allowed values in chat.");

    public LangMessage Time_Day  = new LangMessage(this, "%s%d.");
    public LangMessage Time_Hour = new LangMessage(this, "%s%h.");
    public LangMessage Time_Min  = new LangMessage(this, "%s%min.");
    public LangMessage Time_Sec  = new LangMessage(this, "%s%sec.");

    public LangMessage Other_Yes  = new LangMessage(this, "&aYes");
    public LangMessage Other_No   = new LangMessage(this, "&cNo");
    public LangMessage Other_Any  = new LangMessage(this, "Any");
    public LangMessage Other_None = new LangMessage(this, "None");
    public LangMessage Other_OneTimed = new LangMessage(this, "One-Timed");
    public LangMessage Other_Unlimited = new LangMessage(this, "Unlimited");
    public LangMessage Other_Infinity = new LangMessage(this, "∞");

    public LangMessage Error_Player_Invalid = new LangMessage(this, "&cPlayer not found.");
    public LangMessage Error_World_Invalid  = new LangMessage(this, "&cWorld not found.");
    public LangMessage Error_Number_Invalid  = new LangMessage(this, "&7%num% &cis not a valid number.");
    public LangMessage Error_Permission_Deny = new LangMessage(this, "&cYou don't have permissions to do that!");
    public LangMessage Error_Item_Invalid = new LangMessage(this, "&cYou must hold an item!");
    public LangMessage Error_Type_Invalid = new LangMessage(this, "Invalid type. Available: %types%");
    public LangMessage Error_Command_Self   = new LangMessage(this, "Can not be used on yourself.");
    public LangMessage Error_Command_Sender = new LangMessage(this, "This command is for players only.");
    public LangMessage Error_Internal       = new LangMessage(this, "&cInternal error!");

    @NotNull
    public String getPotionType(@NotNull PotionEffectType type) {
        if (!this.plugin.isEngine()) return NexEngine.get().lang().getPotionType(type);

        return this.config.getString("PotionEffectType." + type.getName(), type.getName());
    }

    @NotNull
    public String getEnchantment(@NotNull Enchantment enchantment) {
        if (!this.plugin.isEngine()) return NexEngine.get().lang().getEnchantment(enchantment);

        String key = enchantment.getKey().getKey();
        this.config.addMissing("Enchantment." + key, StringUtil.capitalizeFully(key.replace("_", " ")));
        this.config.saveChanges();

        return this.config.getString("Enchantment." + key, key);
    }

    @NotNull
    public String getBoolean(boolean b) {
        return (b ? this.Other_Yes : this.Other_No).getLocalized();
    }
}

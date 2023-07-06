package su.nexmedia.engine.lang;

import su.nexmedia.engine.api.lang.LangColors;
import su.nexmedia.engine.api.lang.LangKey;

public class EngineLang implements LangColors {

    public static final LangKey CORE_COMMAND_USAGE       = new LangKey("Core.Command.Usage", "&cUsage: &e/%command_label% &6%command_usage%");
    public static final LangKey CORE_COMMAND_HELP_LIST   = new LangKey("Core.Command.Help.List", """
        <! prefix:"false" !>
        &6&m              &6&l[ &e&l%plugin_name_localized% &7- &e&lCommands &6&l]&6&m              &7
        &7
        &7          &4&l<> &7- Required, &2&l[] &7- Optional.
        &7
        &6▪ &e/%command_label% &6%command_usage% &7- %command_description%
        &7
        """);
    public static final LangKey CORE_COMMAND_HELP_DESC   = new LangKey("Core.Command.Help.Desc", "Show help page.");
    public static final LangKey CORE_COMMAND_ABOUT_DESC  = new LangKey("Core.Command.About.Desc", "Some info about the plugin.");
    public static final LangKey CORE_COMMAND_RELOAD_DESC = new LangKey("Core.Command.Reload.Desc", "Reload the plugin.");
    public static final LangKey CORE_COMMAND_RELOAD_DONE = new LangKey("Core.Command.Reload.Done", "Reloaded!");

    public static final LangKey TIME_DAY  = new LangKey("Time.Day", "%s%d.");
    public static final LangKey TIME_HOUR = new LangKey("Time.Hour", "%s%h.");
    public static final LangKey TIME_MIN  = new LangKey("Time.Min", "%s%min.");
    public static final LangKey TIME_SEC  = new LangKey("Time.Sec", "%s%sec.");

    public static final LangKey OTHER_YES       = new LangKey("Other.Yes", "&aYes");
    public static final LangKey OTHER_NO        = new LangKey("Other.No", "&cNo");
    public static final LangKey OTHER_ANY       = new LangKey("Other.Any", "Any");
    public static final LangKey OTHER_NONE      = new LangKey("Other.None", "None");
    public static final LangKey OTHER_NEVER     = new LangKey("Other.Never", "Never");
    public static final LangKey OTHER_ONE_TIMED = new LangKey("Other.OneTimed", "One-Timed");
    public static final LangKey OTHER_UNLIMITED = new LangKey("Other.Unlimited", "Unlimited");
    public static final LangKey OTHER_INFINITY  = new LangKey("Other.Infinity", "∞");

    public static final LangKey ERROR_PLAYER_INVALID  = LangKey.of("Error.Player.Invalid", RED + "Player not found.");
    public static final LangKey ERROR_WORLD_INVALID   = LangKey.of("Error.World.Invalid", RED + "World not found.");
    public static final LangKey ERROR_NUMBER_INVALID  = LangKey.of("Error.Number.Invalid", "&7%num% &cis not a valid number.");
    public static final LangKey ERROR_PERMISSION_DENY = LangKey.of("Error.Permission.Deny", RED + "You don't have permissions to do that!");
    @Deprecated public static final LangKey ERROR_ITEM_INVALID    = LangKey.of("Error.Item.Invalid", RED + "You must hold an item!");
    public static final LangKey ERROR_COMMAND_SELF    = LangKey.of("Error.Command.Self", RED + "This command is not applicable to yourself.");
    public static final LangKey ERROR_COMMAND_SENDER  = LangKey.of("Error.Command.Sender", RED + "This command is for players only.");

    public static final LangKey EDITOR_TIP_EXIT             = LangKey.of("Editor.Tip.Exit", "<! prefix:\"false\" !> <? showText:\"&7Click me or type &f#exit\" run_command:\"/#exit\" ?>&bClick to exit the &dEdit Mode</>");
    public static final LangKey EDITOR_TITLE_DONE           = LangKey.of("Editor.Title.Done", "&a&lDone!");
    public static final LangKey EDITOR_TITLE_EDIT           = LangKey.of("Editor.Title.Edit", "&a&l< Edit Mode >");
    public static final LangKey EDITOR_TITLE_ERROR          = LangKey.of("Editor.Title.Error", "&c&lError!");
    public static final LangKey EDITOR_ERROR_NUMBER_GENERIC = LangKey.of("Editor.Error.Number.Generic", "&7Invalid number!");
    public static final LangKey EDITOR_ERROR_NUMBER_NOT_INT = LangKey.of("Editor.Error.Number.NotInt", "&7Number must be &cInteger&7!");
    public static final LangKey EDITOR_ERROR_ENUM           = LangKey.of("Editor.Error.Enum", "&7Invalid type! See in chat.");
}

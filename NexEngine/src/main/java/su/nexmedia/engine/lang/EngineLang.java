package su.nexmedia.engine.lang;

import su.nexmedia.engine.api.lang.LangColors;
import su.nexmedia.engine.api.lang.LangKey;
import su.nexmedia.engine.editor.EditorManager;
import su.nexmedia.engine.utils.Placeholders;

public class EngineLang implements LangColors {

    public static final LangKey COMMAND_USAGE     = LangKey.of("Command.Usage",
        "<! prefix:\"false\" !>" +
            "\n" + GRAY +
            "\n" + RED + "Error: " + GRAY + "Wrong arguments!" +
            "\n" + RED + "Usage: " + YELLOW + "/" + Placeholders.COMMAND_LABEL + " " + LIGHT_YELLOW + Placeholders.COMMAND_USAGE +
            "\n" + GRAY);
    public static final LangKey COMMAND_HELP_LIST       = LangKey.of("Command.Help.List",
        "<! prefix:\"false\" !>" +
            "\n" + GRAY +
            "\n" + YELLOW + "&l" + Placeholders.PLUGIN_NAME_LOCALIZED + GRAY + " - " + YELLOW + "&lCommands:" +
            "\n" + GRAY +
            "\n" + RED + "&l<> " + GRAY + "- Required, " + GREEN + "&l[] " + GRAY + "- Optional." +
            "\n" + GRAY +
            "\n" + LIGHT_YELLOW + "▪ " + YELLOW + "/" + Placeholders.COMMAND_LABEL + " " + LIGHT_YELLOW + Placeholders.COMMAND_USAGE + " " + GRAY + "- " + Placeholders.COMMAND_DESCRIPTION +
            "\n" + GRAY);
    public static final LangKey COMMAND_HELP_DESC   = LangKey.of("Command.Help.Desc", "Show help page.");
    public static final LangKey COMMAND_ABOUT_DESC  = LangKey.of("Command.About.Desc", "Some info about the plugin.");
    public static final LangKey COMMAND_RELOAD_DESC = LangKey.of("Command.Reload.Desc", "Reload the whole plugin.");
    public static final LangKey COMMAND_RELOAD_DONE = LangKey.of("Command.Reload.Done", "All data & configuration has been reloaded!");

    public static final LangKey TIME_DAY  = new LangKey("Time.Day", "%s%d.");
    public static final LangKey TIME_HOUR = new LangKey("Time.Hour", "%s%h.");
    public static final LangKey TIME_MIN  = new LangKey("Time.Min", "%s%min.");
    public static final LangKey TIME_SEC  = new LangKey("Time.Sec", "%s%sec.");

    public static final LangKey OTHER_YES       = LangKey.of("Other.Yes", GREEN + "Yes");
    public static final LangKey OTHER_NO        = LangKey.of("Other.No", RED + "No");
    public static final LangKey OTHER_ANY       = LangKey.of("Other.Any", "Any");
    public static final LangKey OTHER_NONE      = LangKey.of("Other.None", "None");
    public static final LangKey OTHER_NEVER     = LangKey.of("Other.Never", "Never");
    public static final LangKey OTHER_ONE_TIMED = LangKey.of("Other.OneTimed", "One-Timed");
    public static final LangKey OTHER_UNLIMITED = LangKey.of("Other.Unlimited", "Unlimited");
    public static final LangKey OTHER_INFINITY  = LangKey.of("Other.Infinity", "∞");

    public static final LangKey ERROR_PLAYER_INVALID  = LangKey.of("Error.Player.Invalid", RED + "Player not found.");
    public static final LangKey ERROR_WORLD_INVALID   = LangKey.of("Error.World.Invalid", RED + "World not found.");
    public static final LangKey ERROR_NUMBER_INVALID  = LangKey.of("Error.Number.Invalid", RED + "%num% is not a valid number.");
    public static final LangKey ERROR_PERMISSION_DENY = LangKey.of("Error.Permission.Deny", RED + "You don't have permissions to do that!");
    public static final LangKey ERROR_COMMAND_SELF    = LangKey.of("Error.Command.Self", RED + "This command is not applicable to yourself.");
    public static final LangKey ERROR_COMMAND_SENDER  = LangKey.of("Error.Command.Sender", RED + "This command is for players only.");

    public static final LangKey EDITOR_TIP_EXIT             = LangKey.of("Editor.Tip.Exit",
        "<! prefix:\"false\" !>" +
        "<? show_text:\"" + GRAY + "Click me or type " + RED + EditorManager.EXIT + "\" run_command:\"/" + EditorManager.EXIT + "\" ?>" + GRAY + "Click here to " + RED + "[Exit Edit Mode]</>");
    public static final LangKey EDITOR_TITLE_DONE           = LangKey.of("Editor.Title.Done", "&a&lDone!");
    public static final LangKey EDITOR_TITLE_EDIT           = LangKey.of("Editor.Title.Edit", "&a&l< Edit Mode >");
    public static final LangKey EDITOR_TITLE_ERROR          = LangKey.of("Editor.Title.Error", "&c&lError!");
    public static final LangKey EDITOR_ERROR_NUMBER_GENERIC = LangKey.of("Editor.Error.Number.Generic", "&7Invalid number!");
    public static final LangKey EDITOR_ERROR_NUMBER_NOT_INT = LangKey.of("Editor.Error.Number.NotInt", "&7Number must be &cInteger&7!");
    public static final LangKey EDITOR_ERROR_ENUM           = LangKey.of("Editor.Error.Enum", "&7Invalid type! See in chat.");
}

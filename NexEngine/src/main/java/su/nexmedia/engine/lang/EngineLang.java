package su.nexmedia.engine.lang;

import su.nexmedia.engine.api.lang.LangKey;
import su.nexmedia.engine.editor.EditorManager;
import su.nexmedia.engine.utils.Placeholders;

import static su.nexmedia.engine.utils.Colors.*;
import static su.nexmedia.engine.utils.Placeholders.*;

public class EngineLang {

    public static final LangKey COMMAND_USAGE = LangKey.of("Command.Usage",
        "<! prefix:\"false\" !>" +
            "\n" + GRAY +
            "\n" + RED + "Error: " + GRAY + "Wrong arguments!" +
            "\n" + RED + "Usage: " + YELLOW + "/" + COMMAND_LABEL + " " + ORANGE + Placeholders.COMMAND_USAGE +
            "\n" + GRAY);

    public static final LangKey COMMAND_HELP_LIST = LangKey.of("Command.Help.List",
        "<! prefix:\"false\" !>" +
            "\n" + GRAY +
            "\n" + "  " + YELLOW + BOLD + PLUGIN_NAME_LOCALIZED + GRAY + " - " + YELLOW + BOLD + "Commands:" +
            "\n" + GRAY +
            "\n" + "  " + RED + BOLD + "<> " + GRAY + "- Required, " + GREEN + BOLD + "[] " + GRAY + "- Optional." +
            "\n" + GRAY +
            "\n" + "  " + YELLOW + "/" + COMMAND_LABEL + " " + ORANGE + Placeholders.COMMAND_USAGE + " " + GRAY + "- " + COMMAND_DESCRIPTION +
            "\n" + GRAY);

    public static final LangKey COMMAND_HELP_DESC   = LangKey.of("Command.Help.Desc", "Show help page.");

    public static final LangKey COMMAND_CHECKPERM_DESC = LangKey.of("Command.CheckPerm.Desc", "Print player permission info.");
    public static final LangKey COMMAND_CHECKPERM_USAGE = LangKey.of("Command.CheckPerm.Usage", "<player>");

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
    public static final LangKey ERROR_COMMAND_SELF    = LangKey.of("Error.Command.Self", RED + "This command can not be used on yourself.");
    public static final LangKey ERROR_COMMAND_SENDER  = LangKey.of("Error.Command.Sender", RED + "This command is for players only.");

    public static final LangKey EDITOR_TIP_EXIT = LangKey.of("Editor.Tip.Exit",
        "<! prefix:\"false\" !>" +
        "<? show_text:\"" + GRAY + "Click me or type " + RED + EditorManager.EXIT + "\" run_command:\"/" + EditorManager.EXIT + "\" ?>" + GRAY + "Click here to " + RED + "[Exit Edit Mode]</>");
    public static final LangKey EDITOR_TITLE_DONE           = LangKey.of("Editor.Title.Done", GREEN + BOLD + "Done!");
    public static final LangKey EDITOR_TITLE_EDIT           = LangKey.of("Editor.Title.Edit", GREEN + BOLD + "< Edit Mode >");
    public static final LangKey EDITOR_TITLE_ERROR          = LangKey.of("Editor.Title.Error", RED + BOLD + "Error!");
    public static final LangKey EDITOR_ERROR_NUMBER_GENERIC = LangKey.of("Editor.Error.Number.Generic", GRAY + "Invalid number!");
    public static final LangKey EDITOR_ERROR_NUMBER_NOT_INT = LangKey.of("Editor.Error.Number.NotInt", GRAY + "Expecting " + RED + "whole" + GRAY + " number!");
    public static final LangKey EDITOR_ERROR_ENUM           = LangKey.of("Editor.Error.Enum", GRAY + "Invalid Input!");

    public static final LangKey NUMBER_SHORT_THOUSAND = LangKey.of("Number.Thousand", "k");
    public static final LangKey NUMBER_SHORT_MILLION = LangKey.of("Number.Million", "m");
    public static final LangKey NUMBER_SHORT_BILLION = LangKey.of("Number.Billion", "b");
    public static final LangKey NUMBER_SHORT_TRILLION = LangKey.of("Number.Trillion", "t");
    public static final LangKey NUMBER_SHORT_QUADRILLION = LangKey.of("Number.Quadrillion", "q");
}

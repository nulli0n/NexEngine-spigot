package su.nexmedia.engine.command.list;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.api.command.AbstractCommand;
import su.nexmedia.engine.api.command.CommandResult;
import su.nexmedia.engine.api.lang.LangColors;
import su.nexmedia.engine.lang.EngineLang;
import su.nexmedia.engine.utils.Colorizer;
import su.nexmedia.engine.utils.EngineUtils;

import java.util.Arrays;
import java.util.List;

public class AboutSubCommand<P extends NexPlugin<P>> extends AbstractCommand<P> {

    public AboutSubCommand(@NotNull P plugin) {
        super(plugin, new String[]{"about"});
        this.setDescription(plugin.getMessage(EngineLang.CORE_COMMAND_ABOUT_DESC));
    }

    @Override
    protected void onExecute(@NotNull CommandSender sender, @NotNull CommandResult result) {
        List<String> info = Colorizer.apply(Arrays.asList(
            LangColors.GRAY,
            LangColors.YELLOW + ChatColor.BOLD + plugin.getName() + LangColors.ORANGE + " v" + plugin.getDescription().getVersion(),
            LangColors.GRAY + plugin.getDescription().getDescription(),
            LangColors.GRAY,
            LangColors.GREEN + "▪ " + LangColors.GRAY + "API Version: " + LangColors.GREEN + plugin.getDescription().getAPIVersion(),
            LangColors.GREEN + "▪ " + LangColors.GRAY + "Made by " + LangColors.GREEN + plugin.getDescription().getAuthors().get(0),
            LangColors.GREEN + "▪ " + LangColors.GRAY + "Powered by " + LangColors.GREEN + EngineUtils.ENGINE.getName(),
            LangColors.GRAY,
            LangColors.CYAN + ChatColor.UNDERLINE + "Made in the Urals" + LangColors.CYAN + " © 2019-2023",
            LangColors.GRAY));

        info.forEach(sender::sendMessage);
    }
}

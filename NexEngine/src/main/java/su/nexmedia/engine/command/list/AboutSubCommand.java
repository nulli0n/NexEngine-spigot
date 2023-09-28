package su.nexmedia.engine.command.list;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.api.command.AbstractCommand;
import su.nexmedia.engine.api.command.CommandResult;
import su.nexmedia.engine.lang.EngineLang;
import su.nexmedia.engine.utils.Colorizer;
import su.nexmedia.engine.utils.Colors;
import su.nexmedia.engine.utils.EngineUtils;

import java.util.Arrays;
import java.util.List;

public class AboutSubCommand<P extends NexPlugin<P>> extends AbstractCommand<P> {

    public AboutSubCommand(@NotNull P plugin) {
        super(plugin, new String[]{"about"});
        this.setDescription(plugin.getMessage(EngineLang.COMMAND_ABOUT_DESC));
    }

    @Override
    protected void onExecute(@NotNull CommandSender sender, @NotNull CommandResult result) {
        List<String> info = Colorizer.apply(Arrays.asList(
            Colors.GRAY,
            Colors.YELLOW + ChatColor.BOLD + plugin.getName() + Colors.ORANGE + " v" + plugin.getDescription().getVersion(),
            Colors.GRAY + plugin.getDescription().getDescription(),
            Colors.GRAY,
            Colors.YELLOW + "▪ " + Colors.GRAY + "API Version: " + Colors.YELLOW + plugin.getDescription().getAPIVersion(),
            Colors.YELLOW + "▪ " + Colors.GRAY + "Made by " + Colors.YELLOW + plugin.getDescription().getAuthors().get(0),
            Colors.YELLOW + "▪ " + Colors.GRAY + "Powered by " + Colors.YELLOW + EngineUtils.ENGINE.getName(),
            Colors.GRAY,
            Colors.CYAN + ChatColor.UNDERLINE + "Made in the Urals" + Colors.CYAN + " © 2019-2023",
            Colors.GRAY));

        info.forEach(sender::sendMessage);
    }
}

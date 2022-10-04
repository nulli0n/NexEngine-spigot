package su.nexmedia.engine.command.list;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexEngine;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.api.command.AbstractCommand;
import su.nexmedia.engine.lang.EngineLang;
import su.nexmedia.engine.utils.StringUtil;

import java.util.Arrays;
import java.util.List;

public class AboutSubCommand<P extends NexPlugin<P>> extends AbstractCommand<P> {

    public AboutSubCommand(@NotNull P plugin) {
        super(plugin, new String[]{"about"});
    }

    @Override
    @NotNull
    public String getUsage() {
        return "";
    }

    @Override
    @NotNull
    public String getDescription() {
        return plugin.getMessage(EngineLang.CORE_COMMAND_ABOUT_DESC).getLocalized();
    }

    @Override
    public boolean isPlayerOnly() {
        return false;
    }

    @Override
    public void onExecute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        List<String> info = StringUtil.color(Arrays.asList(
            "&7",
            "&e" + plugin.getName() + " &6v" + plugin.getDescription().getVersion() + " &ecreated by &6" + plugin.getAuthor(),
            "&eType &6/" + plugin.getLabel() + " help&e to list plugin commands.",
            "&7",
            "&2Powered by &a&l" + NexEngine.get().getName() + "&2, © 2019-2022 &a" + NexPlugin.TM));

        info.forEach(sender::sendMessage);
    }
}

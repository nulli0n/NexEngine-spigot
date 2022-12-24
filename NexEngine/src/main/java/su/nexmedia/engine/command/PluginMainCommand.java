package su.nexmedia.engine.command;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.api.command.GeneralCommand;

import java.util.Map;

public class PluginMainCommand<P extends NexPlugin<P>> extends GeneralCommand<P> {

    public PluginMainCommand(@NotNull P plugin) {
        super(plugin, plugin.getLabels());
    }

    @Override
    @NotNull
    public String getUsage() {
        return "";
    }

    @Override
    @NotNull
    public String getDescription() {
        return "";
    }

    @Override
    public boolean isPlayerOnly() {
        return false;
    }

    @Override
    protected void onExecute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, @NotNull Map<String, String> flags) {

    }
}

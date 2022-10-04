package su.nexmedia.engine.command.list;

import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.api.command.AbstractCommand;
import su.nexmedia.engine.lang.EngineLang;

public class ReloadSubCommand<P extends NexPlugin<P>> extends AbstractCommand<P> {

    public ReloadSubCommand(@NotNull P plugin, @NotNull Permission permission) {
        this(plugin, permission.getName());
    }

    public ReloadSubCommand(@NotNull P plugin, @NotNull String permission) {
        super(plugin, new String[]{"reload"}, permission);
    }

    @Override
    @NotNull
    public String getUsage() {
        return "";
    }

    @Override
    @NotNull
    public String getDescription() {
        return plugin.getMessage(EngineLang.CORE_COMMAND_RELOAD_DESC).getLocalized();
    }

    @Override
    public boolean isPlayerOnly() {
        return false;
    }

    @Override
    public void onExecute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        plugin.reload();
        plugin.getMessage(EngineLang.CORE_COMMAND_RELOAD_DESC).send(sender);
    }
}

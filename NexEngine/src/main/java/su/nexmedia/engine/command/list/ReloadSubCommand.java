package su.nexmedia.engine.command.list;

import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.api.command.AbstractCommand;
import su.nexmedia.engine.api.command.CommandResult;
import su.nexmedia.engine.lang.EngineLang;

public class ReloadSubCommand<P extends NexPlugin<P>> extends AbstractCommand<P> {

    public ReloadSubCommand(@NotNull P plugin, @NotNull Permission permission) {
        this(plugin, permission.getName());
    }

    public ReloadSubCommand(@NotNull P plugin, @NotNull String permission) {
        super(plugin, new String[]{"reload"}, permission);
        this.setDescription(plugin.getMessage(EngineLang.CORE_COMMAND_RELOAD_DESC));
    }

    @Override
    protected void onExecute(@NotNull CommandSender sender, @NotNull CommandResult result) {
        plugin.reload();
        plugin.getMessage(EngineLang.CORE_COMMAND_RELOAD_DONE).send(sender);
    }
}

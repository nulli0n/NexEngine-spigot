package su.nexmedia.engine.command.list;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.api.command.AbstractCommand;

public class ReloadSubCommand<P extends NexPlugin<P>> extends AbstractCommand<P> {

    public ReloadSubCommand(@NotNull P plugin) {
        super(plugin, new String[]{"reload"}, plugin.getNameRaw() + ".reload");
    }

    @Override
    @NotNull
    public String getUsage() {
        return "";
    }

    @Override
    @NotNull
    public String getDescription() {
        return plugin.lang().Core_Command_Reload_Desc.getLocalized();
    }

    @Override
    public boolean isPlayerOnly() {
        return false;
    }

    @Override
    public void onExecute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        plugin.reload();
        plugin.lang().Core_Command_Reload_Done.send(sender);
    }
}

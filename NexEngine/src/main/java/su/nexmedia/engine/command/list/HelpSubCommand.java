package su.nexmedia.engine.command.list;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.api.command.AbstractCommand;

public class HelpSubCommand<P extends NexPlugin<P>> extends AbstractCommand<P> {

    public HelpSubCommand(@NotNull P plugin) {
        super(plugin, new String[]{"help"});
    }

    @Override
    @NotNull
    public String getUsage() {
        return "";
    }

    @Override
    @NotNull
    public String getDescription() {
        return plugin.lang().Core_Command_Help_Desc.getMsg();
    }

    @Override
    public boolean isPlayerOnly() {
        return false;
    }

    @Override
    protected void onExecute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (args.length > 0) return;

        for (String line : plugin.lang().Core_Command_Help_List.asList()) {
            if (line.contains(AbstractCommand.PLACEHOLDER_LABEL)) {
                for (AbstractCommand<P> cmd : this.parent.getChildrens()) {
                    if (!cmd.hasPermission(sender)) continue;

                    sender.sendMessage(cmd.replacePlaceholders().apply(line));
                }
                continue;
            }
            sender.sendMessage(line);
        }
    }
}

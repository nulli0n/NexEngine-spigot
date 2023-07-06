package su.nexmedia.engine.command.list;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.api.command.AbstractCommand;
import su.nexmedia.engine.api.command.CommandResult;
import su.nexmedia.engine.lang.EngineLang;

public class HelpSubCommand<P extends NexPlugin<P>> extends AbstractCommand<P> {

    public HelpSubCommand(@NotNull P plugin) {
        super(plugin, new String[]{"help"});
        this.setDescription(plugin.getMessage(EngineLang.CORE_COMMAND_HELP_DESC));
    }

    @Override
    protected void onExecute(@NotNull CommandSender sender, @NotNull CommandResult result) {
        if (!this.parent.hasPermission(sender)) {
            this.errorPermission(sender);
            return;
        }

        plugin.getMessage(EngineLang.CORE_COMMAND_HELP_LIST)
            .replace(str -> str.contains(AbstractCommand.PLACEHOLDER_LABEL), (line, list) -> {
                this.parent.getChildrens().forEach(children -> {
                    if (!children.hasPermission(sender)) return;

                    list.add(children.replacePlaceholders().apply(line));
                });
            }).send(sender);
    }
}

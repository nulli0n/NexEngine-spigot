package su.nexmedia.engine.command.list;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.api.command.AbstractCommand;
import su.nexmedia.engine.api.editor.EditorHolder;
import su.nexmedia.engine.lang.EngineLang;

public class EditorSubCommand<P extends NexPlugin<P>> extends AbstractCommand<P> {

    public EditorSubCommand(@NotNull P plugin) {
        super(plugin, new String[]{"editor"}, plugin.getNameRaw() + ".cmd.editor");
    }

    @Override
    @NotNull
    public String getUsage() {
        return "";
    }

    @Override
    @NotNull
    public String getDescription() {
        return plugin.getMessage(EngineLang.CORE_COMMAND_EDITOR_DESC).getLocalized();
    }

    @Override
    public boolean isPlayerOnly() {
        return true;
    }

    @Override
    public void onExecute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        if (this.plugin instanceof EditorHolder<?, ?> editorHolder) {
            editorHolder.getEditor().open(player, 1);
        }
    }
}

package su.nexmedia.engine.command.list;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.api.command.AbstractCommand;
import su.nexmedia.engine.api.editor.EditorHolder;
import su.nexmedia.engine.lang.EngineLang;

import java.util.Map;

public class EditorSubCommand<P extends NexPlugin<P>> extends AbstractCommand<P> {

    protected final EditorHolder<P, ?> editorHolder;

    public EditorSubCommand(@NotNull P plugin, @NotNull EditorHolder<P, ?> editorHolder, @NotNull Permission permission) {
        this(plugin, editorHolder, permission.getName());
    }

    public EditorSubCommand(@NotNull P plugin, @NotNull EditorHolder<P, ?> editorHolder, @NotNull String permission) {
        super(plugin, new String[]{"editor"}, permission);
        this.editorHolder = editorHolder;
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
    protected void onExecute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, @NotNull Map<String, String> flags) {
        Player player = (Player) sender;
        this.editorHolder.getEditor().open(player, 1);
    }
}

package su.nexmedia.engine.command;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.api.command.GeneralCommand;
import su.nexmedia.engine.api.editor.EditorHolder;
import su.nexmedia.engine.api.manager.AbstractManager;
import su.nexmedia.engine.command.list.AboutSubCommand;
import su.nexmedia.engine.command.list.EditorSubCommand;
import su.nexmedia.engine.command.list.HelpSubCommand;
import su.nexmedia.engine.command.list.ReloadSubCommand;

import java.util.HashSet;
import java.util.Set;

public class CommandManager<P extends NexPlugin<P>> extends AbstractManager<P> {

    private Set<GeneralCommand<P>> commands;
    private PluginMainCommand<P>   mainCommand;

    public CommandManager(@NotNull P plugin) {
        super(plugin);
    }

    @Override
    public void onLoad() {
        this.commands = new HashSet<>();

        // Create main plugin command and attach help sub-command as a default executor.
        this.mainCommand = new PluginMainCommand<>(this.plugin);
        this.mainCommand.addDefaultCommand(new HelpSubCommand<>(this.plugin));

        // Register child plugin sub-commands to the main plugin command.
        this.plugin.registerCommands(this.mainCommand);

        // Check for plugin settings to register default commands.
        if (this.plugin instanceof EditorHolder) {
            this.mainCommand.addChildren(new EditorSubCommand<>(this.plugin));
        }
        this.mainCommand.addChildren(new ReloadSubCommand<>(this.plugin));

        if (!this.plugin.isEngine()) {
            this.mainCommand.addChildren(new AboutSubCommand<>(this.plugin));
        }

        // Register main command as a bukkit command.
        this.registerCommand(this.mainCommand);
    }

    @Override
    public void onShutdown() {
        for (GeneralCommand<P> cmd : new HashSet<>(this.commands)) {
            this.unregisterCommand(cmd);
            cmd.getChildrens().clear();
        }
        this.commands.clear();
    }

    @NotNull
    public Set<GeneralCommand<P>> getCommands() {
        return this.commands;
    }

    @NotNull
    public PluginMainCommand<P> getMainCommand() {
        return this.mainCommand;
    }

    public void registerCommand(@NotNull GeneralCommand<P> command) {
        if (this.commands.add(command)) {
            CommandRegister.register(this.plugin, command);
        }
    }

    public void unregisterCommand(@NotNull GeneralCommand<P> command) {
        if (this.commands.remove(command)) {
            CommandRegister.unregister(command.getAliases()[0]);
        }
    }
}

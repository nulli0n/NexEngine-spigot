package su.nexmedia.engine.command;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.*;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.api.command.GeneralCommand;
import su.nexmedia.engine.utils.Reflex;

import java.lang.reflect.Method;
import java.util.*;

public class CommandRegister extends Command implements PluginIdentifiableCommand {

    protected final CommandExecutor executor;
    protected       Plugin          plugin;
    protected       TabCompleter    tab;

    public CommandRegister(@NotNull Plugin plugin, @NotNull CommandExecutor executor, @NotNull TabCompleter tab,
                           @NotNull String[] aliases, @NotNull String description, @NotNull String usage) {
        super(aliases[0], description, usage, Arrays.asList(aliases));
        this.executor = executor;
        this.plugin = plugin;
        this.tab = tab;
    }

    public static void register(@NotNull Plugin plugin, @NotNull GeneralCommand<?> command) {
        CommandRegister register = new CommandRegister(plugin, command, command, command.getAliases(), command.getDescription(), command.getUsage());
        register.setPermission(command.getPermission());

        CommandMap map = (CommandMap) Reflex.getFieldValue(plugin.getServer(), "commandMap");
        if (map == null) return;

        map.register(plugin.getDescription().getName(), register);
    }

    public static void syncCommands() {
        // Fix tab completer when registerd on runtime
        Server server = Bukkit.getServer();
        Method method = Reflex.getMethod(server.getClass(), "syncCommands");
        if (method == null) return;

        Reflex.invokeMethod(method, server);
    }

    @SuppressWarnings("unchecked")
    @Deprecated
    public static void unregister(@NotNull NexPlugin<?> plugin, @NotNull String[] aliases) {
        SimpleCommandMap map = (SimpleCommandMap) Reflex.getFieldValue(plugin.getPluginManager(), "commandMap");
        if (map == null) return;

        HashMap<String, Command> knownCommands = (HashMap<String, Command>) Reflex.getFieldValue(map, "knownCommands");
        if (knownCommands == null) return;

        for (String command : aliases) {
            for (String commandAlias : getAliases(command, true)) {
                Command cmd = map.getCommand(commandAlias);
                if (cmd == null) {
                    continue;
                }
                if (!cmd.unregister(map)) {
                    plugin.error("Unable to unregister command: " + commandAlias);
                }
                knownCommands.remove(commandAlias);
                // plugin.info("Command unregistered: '" + commandAlias + "'");
            }
        }
    }

    public static void unregister(@NotNull String name) {
        SimpleCommandMap map = (SimpleCommandMap) Reflex.getFieldValue(Bukkit.getServer(), "commandMap");
        if (map == null) return;

        Command match = map.getCommands().stream()
            .filter(command -> command.getLabel().equalsIgnoreCase(name) || command.getAliases().contains(name))
            .findFirst().orElse(null);
        if (match == null) return;

        Map<String, Command> knownCommands = (HashMap<String, Command>) Reflex.getFieldValue(map, "knownCommands");
        if (knownCommands == null) return;

        if (match.unregister(map)) {
            knownCommands.keySet().removeIf(key -> key.equalsIgnoreCase(match.getName()) || match.getAliases().contains(key));
        }
    }

    @NotNull
    @Deprecated
    public static Set<String> getAliases(@NotNull String name) {
        return getAliases(name, true);
    }

    @NotNull
    public static Set<String> getAliases(@NotNull String name, boolean inclusive) {
        SimpleCommandMap map = (SimpleCommandMap) Reflex.getFieldValue(Bukkit.getServer(), "commandMap");
        if (map == null) return Collections.emptySet();

        Command match = map.getCommands().stream()
            .filter(command -> command.getLabel().equalsIgnoreCase(name) || command.getAliases().contains(name))
            .findFirst().orElse(null);

        if (match == null) return Collections.emptySet();

        Set<String> aliases = new HashSet<>(match.getAliases());
        if (inclusive) aliases.add(match.getName());

        return aliases;
    }

    @Override
    @NotNull
    public Plugin getPlugin() {
        return this.plugin;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        return this.executor.onCommand(sender, this, label, args);
    }

    @Override
    @NotNull
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        if (this.tab != null) {
            List<String> list = this.tab.onTabComplete(sender, this, alias, args);
            if (list != null) {
                return list;
            }
        }
        return Collections.emptyList();
    }
}

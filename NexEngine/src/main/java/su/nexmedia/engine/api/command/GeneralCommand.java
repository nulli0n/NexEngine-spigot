package su.nexmedia.engine.api.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.utils.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class GeneralCommand<P extends NexPlugin<P>> extends AbstractCommand<P> implements CommandExecutor, TabExecutor {

    private Command fallback;
    private AbstractCommand<P> defaultCommand;

    public GeneralCommand(@NotNull P plugin, @NotNull List<String> aliases) {
        this(plugin, aliases.toArray(new String[0]));
    }

    public GeneralCommand(@NotNull P plugin, @NotNull String[] aliases) {
        this(plugin, aliases, (String) null);
    }

    public GeneralCommand(@NotNull P plugin, @NotNull List<String> aliases, @Nullable String permission) {
        this(plugin, aliases.toArray(new String[0]), permission);
    }

    public GeneralCommand(@NotNull P plugin, @NotNull List<String> aliases, @Nullable Permission permission) {
        this(plugin, aliases.toArray(new String[0]), permission);
    }

    public GeneralCommand(@NotNull P plugin, @NotNull String[] aliases, @Nullable Permission permission) {
        super(plugin, aliases, permission);
    }

    public GeneralCommand(@NotNull P plugin, @NotNull String[] aliases, @Nullable String permission) {
        super(plugin, aliases, permission);
    }

    public void addDefaultCommand(@NotNull AbstractCommand<P> command) {
        this.addChildren(command);
        this.defaultCommand = command;
    }

    public Command getFallback() {
        return fallback;
    }

    public void setFallback(@NotNull Command fallback) {
        this.fallback = fallback;
    }

    @NotNull
    public AbstractCommand<P> findChildren(@NotNull String[] args) {
        AbstractCommand<P> command = this;//.defaultCommand;
        int childCount = 0;
        while (args.length > childCount) {
            AbstractCommand<P> child = command.getChildren(args[childCount++]);
            if (child == null) break;

            command = child;
        }
        return command;
    }

    /*private int countChildren(@NotNull String[] args) {
        AbstractCommand<P> command = this;
        int childCount = 0;
        while (args.length > childCount) {
            AbstractCommand<P> child = command.getChildren(args[childCount]);
            if (child == null) break;

            command = child;
            childCount++;
        }
        return childCount;
    }*/

    @Override
    public final boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        AbstractCommand<P> command = this.findChildren(args);
        if (command instanceof GeneralCommand<P> generalCommand) {
            if (generalCommand.defaultCommand != null) {
                command = generalCommand.defaultCommand;
            }
        }
        command.execute(sender, label, args);
        return true;
    }

    @Override
    public final List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd,
                                            @NotNull String label, String[] args) {

        if (!(sender instanceof Player player) || args.length == 0) return Collections.emptyList();

        AbstractCommand<P> command = this.findChildren(args);
        if (!command.hasPermission(sender)) return Collections.emptyList();

        if (!command.getChildrens().isEmpty()) {
            List<String> list = new ArrayList<>();
            command.getChildrens().stream().filter(child -> child.hasPermission(sender))
                .forEach(child -> list.addAll(Arrays.asList(child.getAliases())));
            return StringUtil.getByPartialMatches(list, args[args.length - 1], 2);
        }

        List<String> list = command.getTab(player, command.equals(this) ? (args.length) : (args.length - 1), args);
        return StringUtil.getByPartialMatches(list, args[args.length - 1], 2);
    }
}

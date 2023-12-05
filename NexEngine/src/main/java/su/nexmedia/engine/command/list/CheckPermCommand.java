package su.nexmedia.engine.command.list;

import net.milkbowl.vault.permission.Permission;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexEngine;
import su.nexmedia.engine.api.command.AbstractCommand;
import su.nexmedia.engine.api.command.CommandResult;
import su.nexmedia.engine.config.EnginePerms;
import su.nexmedia.engine.integration.VaultHook;
import su.nexmedia.engine.utils.CollectionsUtil;
import su.nexmedia.engine.utils.Colorizer;
import su.nexmedia.engine.utils.PlayerUtil;

import java.util.List;

public class CheckPermCommand extends AbstractCommand<NexEngine> {

    public CheckPermCommand(@NotNull NexEngine plugin) {
        super(plugin, new String[]{"checkperm"}, EnginePerms.COMMAND_CHECK_PERM);
        this.setDescription("Check player permissions.");
        this.setUsage("<player>");
    }

    @Override
    @NotNull
    public List<String> getTab(@NotNull Player player, int arg, @NotNull String[] args) {
        if (arg == 1) {
            return CollectionsUtil.playerNames(player);
        }
        return super.getTab(player, arg, args);
    }

    @Override
    protected void onExecute(@NotNull CommandSender sender, @NotNull CommandResult result) {
        if (result.length() < 2) {
            this.printUsage(sender);
            return;
        }

        Player player = PlayerUtil.getPlayer(result.getArg(1));
        if (player == null) {
            this.errorPlayer(sender);
            return;
        }

        Permission permission = VaultHook.getPermissions();
        if (permission == null) {
            sender.sendMessage("No permissions hook.");
            return;
        }

        sender.sendMessage("Permissions report for " + player.getName() + ":");
        sender.sendMessage("Plugin: " + permission.getName());
        sender.sendMessage("Primary Group: " + Colorizer.plain(VaultHook.getPermissionGroup(player)));
        sender.sendMessage("All Groups: " + Colorizer.plain(String.join(", ", VaultHook.getPermissionGroups(player))));
        sender.sendMessage("Prefix: " + VaultHook.getPrefix(player));
        sender.sendMessage("Suffix: " + VaultHook.getSuffix(player));
    }
}

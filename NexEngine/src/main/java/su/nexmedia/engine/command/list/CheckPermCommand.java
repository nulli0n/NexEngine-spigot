package su.nexmedia.engine.command.list;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexEngine;
import su.nexmedia.engine.api.command.AbstractCommand;
import su.nexmedia.engine.api.command.CommandResult;
import su.nexmedia.engine.config.EnginePerms;
import su.nexmedia.engine.integration.VaultHook;
import su.nexmedia.engine.lang.EngineLang;
import su.nexmedia.engine.utils.CollectionsUtil;
import su.nexmedia.engine.utils.Colorizer;
import su.nexmedia.engine.utils.PlayerUtil;

import java.util.List;

import static su.nexmedia.engine.utils.Colors2.*;

public class CheckPermCommand extends AbstractCommand<NexEngine> {

    public CheckPermCommand(@NotNull NexEngine plugin) {
        super(plugin, new String[]{"checkperm"}, EnginePerms.COMMAND_CHECK_PERM);
        this.setDescription(plugin.getMessage(EngineLang.COMMAND_CHECKPERM_DESC));
        this.setUsage(plugin.getMessage(EngineLang.COMMAND_CHECKPERM_USAGE));
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

        sender.sendMessage(LIGHT_YELLOW + BOLD + "Permissions report for " + LIGHT_ORANGE + player.getName() + ":");
        sender.sendMessage(LIGHT_ORANGE + "▪ " + LIGHT_YELLOW + "Primary Group: " + LIGHT_ORANGE + Colorizer.plain(VaultHook.getPermissionGroup(player)));
        sender.sendMessage(LIGHT_ORANGE + "▪ " + LIGHT_YELLOW + "All Groups: " + LIGHT_ORANGE + Colorizer.plain(String.join(", ", VaultHook.getPermissionGroups(player))));
        sender.sendMessage(LIGHT_ORANGE + "▪ " + LIGHT_YELLOW + "Prefix: " + LIGHT_ORANGE + VaultHook.getPrefix(player));
        sender.sendMessage(LIGHT_ORANGE + "▪ " + LIGHT_YELLOW + "Suffix: " + LIGHT_ORANGE + VaultHook.getSuffix(player));
    }
}

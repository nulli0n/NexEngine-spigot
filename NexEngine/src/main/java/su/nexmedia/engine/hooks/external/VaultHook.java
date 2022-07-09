package su.nexmedia.engine.hooks.external;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.server.ServiceRegisterEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.NexEngine;
import su.nexmedia.engine.api.hook.AbstractHook;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VaultHook extends AbstractHook<NexEngine> {

    private static Economy    economy;
    private static Permission permission;
    private static Chat       chat;

    public VaultHook(@NotNull NexEngine plugin, @NotNull String pluginName) {
        super(plugin, pluginName);
    }

    @Override
    public boolean setup() {
        this.setPermission();
        this.setEconomy();
        this.setChat();
        this.registerListeners();

        return true;
    }

    @Override
    public void shutdown() {
        this.unregisterListeners();

        economy = null;
        permission = null;
        chat = null;
    }

    private void setPermission() {
        RegisteredServiceProvider<Permission> rsp = plugin.getServer().getServicesManager().getRegistration(Permission.class);
        if (rsp == null) return;

        permission = rsp.getProvider();
        this.plugin.info("Successfully hooked with " + permission.getName() + " permissions");
    }

    private void setEconomy() {
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return;

        economy = rsp.getProvider();
        this.plugin.info("Successfully hooked with " + economy.getName() + " economy");
    }

    private void setChat() {
        RegisteredServiceProvider<Chat> rsp = plugin.getServer().getServicesManager().getRegistration(Chat.class);
        if (rsp == null) return;

        chat = rsp.getProvider();
        this.plugin.info("Successfully hooked with " + chat.getName() + " chat");
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onServiceRegisterEvent(ServiceRegisterEvent e) {
        Object provider = e.getProvider().getProvider();

        if (provider instanceof Economy) {
            this.setEconomy();
        }
        else if (provider instanceof Permission) {
            this.setPermission();
        }
        else if (provider instanceof Chat) {
            this.setChat();
        }
    }

    public static boolean hasPermissions() {
        return getPermissions() != null;
    }

    @Nullable
    public static Permission getPermissions() {
        return permission;
    }

    public static boolean hasChat() {
        return getChat() != null;
    }

    @Nullable
    public static Chat getChat() {
        return chat;
    }

    public static boolean hasEconomy() {
        return getEconomy() != null;
    }

    @Nullable
    public static Economy getEconomy() {
        return economy;
    }

    @NotNull
    public static String getEconomyName() {
        return hasEconomy() ? economy.getName() : "null";
    }

    @NotNull
    public static String getPermissionGroup(@NotNull Player player) {
        if (!hasPermissions() || !permission.hasGroupSupport()) return "";

        String group = permission.getPrimaryGroup(player);
        return group == null ? "" : group.toLowerCase();
    }

    @NotNull
    public static Set<String> getPermissionGroups(@NotNull Player player) {
        if (!hasPermissions() || !permission.hasGroupSupport()) return Collections.emptySet();

        String[] groups = permission.getPlayerGroups(player);
        if (groups == null) groups = new String[] {getPermissionGroup(player)};

        return Stream.of(groups).map(String::toLowerCase).collect(Collectors.toSet());
    }

    @NotNull
    public static String getPrefix(@NotNull Player player) {
        return hasChat() ? chat.getPlayerPrefix(player) : "";
    }

    @NotNull
    public static String getSuffix(@NotNull Player player) {
        return hasChat() ? chat.getPlayerSuffix(player) : "";
    }

    public static double getBalance(@NotNull Player player) {
        return economy.getBalance(player);
    }

    public static double getBalance(@NotNull OfflinePlayer player) {
        return economy.getBalance(player);
    }

    public static boolean addMoney(@NotNull Player player, double amount) {
        return addMoney((OfflinePlayer) player, amount);
    }

    public static boolean addMoney(@NotNull OfflinePlayer player, double amount) {
        return economy.depositPlayer(player, amount).transactionSuccess();
    }

    public static boolean takeMoney(@NotNull Player player, double amount) {
        return takeMoney((OfflinePlayer) player, amount);
    }

    public static boolean takeMoney(@NotNull OfflinePlayer player, double amount) {
        return economy.withdrawPlayer(player, Math.abs(amount)).transactionSuccess();
    }
}

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

    private Economy    economy;
    private Permission permission;
    private Chat       chat;

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
    }

    private void setPermission() {
        RegisteredServiceProvider<Permission> pp = plugin.getServer().getServicesManager().getRegistration(Permission.class);
        if (pp == null)
            return;

        this.permission = pp.getProvider();
        this.plugin.info("Successfully hooked with " + permission.getName() + " permissions");
    }

    private void setEconomy() {
        RegisteredServiceProvider<Economy> pp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (pp == null)
            return;

        this.economy = pp.getProvider();
        this.plugin.info("Successfully hooked with " + economy.getName() + " economy");
    }

    private void setChat() {
        RegisteredServiceProvider<Chat> ch = plugin.getServer().getServicesManager().getRegistration(Chat.class);
        if (ch == null)
            return;

        this.chat = ch.getProvider();
        this.plugin.info("Successfully hooked with " + chat.getName() + " chat");
    }

    // Some plugins loads too late, so we need to listen to them to be able to hook
    // into.
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEconomyFix(ServiceRegisterEvent e) {
        Object provider = e.getProvider().getProvider();

        if (!this.hasEconomy() && provider instanceof Economy) {
            this.setEconomy();
            return;
        }

        if (!this.hasPermissions() && provider instanceof Permission) {
            this.setPermission();
            return;
        }

        if (!this.hasChat() && provider instanceof Chat) {
            this.setChat();
            return;
        }
    }

    public boolean hasPermissions() {
        return this.getPermissions() != null;
    }

    @Nullable
    public Permission getPermissions() {
        return this.permission;
    }

    public boolean hasChat() {
        return this.getChat() != null;
    }

    @Nullable
    public Chat getChat() {
        return this.chat;
    }

    public boolean hasEconomy() {
        return this.getEconomy() != null;
    }

    @Nullable
    public Economy getEconomy() {
        return this.economy;
    }

    @NotNull
    public String getEconomyName() {
        return this.hasEconomy() ? this.economy.getName() : "null";
    }

    @NotNull
    public String getPlayerGroup(@NotNull Player player) {
        if (!this.hasPermissions() || !this.permission.hasGroupSupport())
            return "";

        String group = this.permission.getPrimaryGroup(player);
        return group == null ? "" : group;
    }

    @NotNull
    public Set<String> getPlayerGroups(@NotNull Player player) {
        if (!this.hasPermissions() || !this.permission.hasGroupSupport())
            return Collections.emptySet();

        String[] groups = this.permission.getPlayerGroups(player);
        if (groups == null)
            return Collections.emptySet();

        Set<String> set = Stream.of(groups).map(String::toLowerCase).collect(Collectors.toSet());
        return set;
    }

    @NotNull
    public String getPrefix(@NotNull Player player) {
        return this.hasChat() ? this.chat.getPlayerPrefix(player) : "";
    }

    @NotNull
    public String getSuffix(@NotNull Player player) {
        return this.hasChat() ? this.chat.getPlayerSuffix(player) : "";
    }

    public double getBalance(@NotNull Player player) {
        return this.economy.getBalance(player);
    }

    public double getBalance(@NotNull OfflinePlayer player) {
        return this.economy.getBalance(player);
    }

    public void give(@NotNull Player player, double amount) {
        this.economy.depositPlayer(player, amount);
    }

    public void give(@NotNull OfflinePlayer player, double amount) {
        this.economy.depositPlayer(player, amount);
    }

    public void take(@NotNull Player player, double amount) {
        this.economy.withdrawPlayer(player, Math.min(Math.abs(amount), this.getBalance(player)));
    }

    public void take(@NotNull OfflinePlayer player, double amount) {
        this.economy.withdrawPlayer(player, Math.min(Math.abs(amount), this.getBalance(player)));
    }
}

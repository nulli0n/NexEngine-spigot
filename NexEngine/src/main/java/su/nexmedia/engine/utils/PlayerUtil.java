package su.nexmedia.engine.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatMessageType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.geysermc.floodgate.api.FloodgateApi;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.integration.VaultHook;
import su.nexmedia.engine.utils.message.NexParser;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class PlayerUtil {

    public static boolean isBedrockPlayer(@NotNull Player player) {
        return EngineUtils.hasFloodgate() && FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId());
    }

    @NotNull
    public static String getPermissionGroup(@NotNull Player player) {
        return EngineUtils.hasVault() ? VaultHook.getPermissionGroup(player).toLowerCase() : "";
    }

    @NotNull
    public static Set<String> getPermissionGroups(@NotNull Player player) {
        return EngineUtils.hasVault() ? VaultHook.getPermissionGroups(player) : Collections.emptySet();
    }

    @NotNull
    public static String getPrefix(@NotNull Player player) {
        return EngineUtils.hasVault() ? VaultHook.getPrefix(player) : "";
    }

    @NotNull
    public static String getSuffix(@NotNull Player player) {
        return EngineUtils.hasVault() ? VaultHook.getSuffix(player) : "";
    }

    public static void sendRichMessage(@NotNull CommandSender sender, @NotNull String message) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Colorizer.apply(NexParser.toPlainText(message)));
            return;
        }
        NexParser.toMessage(message).send(sender);
    }

    public static void sendActionBar(@NotNull Player player, @NotNull String msg) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, NexParser.toMessage(msg).build());
    }

    public static void sound(@NotNull Player player, @Nullable Sound sound) {
        if (sound == null) return;
        player.playSound(player.getLocation(), sound, 0.9f, 0.9f);
    }

    public static void dispatchCommands(@NotNull Player player, @NotNull String... commands) {
        for (String command : commands) {
            dispatchCommand(player, command);
        }
    }

    public static void dispatchCommand(@NotNull Player player, @NotNull String command) {
        //command = command.replace("[CONSOLE]", "");
        //command = command.trim().replace("%player%", player.getName());
        command = Placeholders.forPlayer(player).apply(command);
        if (EngineUtils.hasPlaceholderAPI()) {
            command = PlaceholderAPI.setPlaceholders(player, command);
        }
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    public static boolean hasEmptyInventory(@NotNull Player player) {
        return Stream.of(player.getInventory().getContents()).allMatch(item -> item == null || item.getType().isAir());
    }

    public static boolean hasEmptyContents(@NotNull Player player) {
        return Stream.of(player.getInventory().getStorageContents()).allMatch(item -> item == null || item.getType().isAir());
    }

    public static int countItemSpace(@NotNull Player player, @NotNull ItemStack item) {
        int stackSize = item.getType().getMaxStackSize();
        return Stream.of(player.getInventory().getStorageContents()).mapToInt(itemHas -> {
            if (itemHas == null || itemHas.getType().isAir()) {
                return stackSize;
            }
            if (itemHas.isSimilar(item)) {
                return (stackSize - itemHas.getAmount());
            }
            return 0;
        }).sum();
    }

    public static int countItem(@NotNull Player player, @NotNull Predicate<ItemStack> predicate) {
        return Stream.of(player.getInventory().getStorageContents())
            .filter(item -> item != null && !item.getType().isAir() && predicate.test(item))
            .mapToInt(ItemStack::getAmount).sum();
    }

    public static int countItem(@NotNull Player player, @NotNull ItemStack item) {
        return countItem(player, item::isSimilar);
    }

    public static int countItem(@NotNull Player player, @NotNull Material material) {
        return countItem(player, itemHas -> itemHas.getType() == material);
    }

    public static boolean takeItem(@NotNull Player player, @NotNull ItemStack item) {
        return takeItem(player, itemHas -> itemHas.isSimilar(item), countItem(player, item));
    }

    public static boolean takeItem(@NotNull Player player, @NotNull ItemStack item, int amount) {
        return takeItem(player, itemHas -> itemHas.isSimilar(item), amount);
    }

    public static boolean takeItem(@NotNull Player player, @NotNull Material material) {
        return takeItem(player, itemHas -> itemHas.getType() == material, countItem(player, material));
    }

    public static boolean takeItem(@NotNull Player player, @NotNull Material material, int amount) {
        return takeItem(player, itemHas -> itemHas.getType() == material, amount);
    }

    public static boolean takeItem(@NotNull Player player, @NotNull Predicate<ItemStack> predicate) {
        return takeItem(player, predicate, countItem(player, predicate));
    }

    public static boolean takeItem(@NotNull Player player, @NotNull Predicate<ItemStack> predicate, int amount) {
        if (countItem(player, predicate) < amount) return false;

        int takenAmount = 0;

        Inventory inventory = player.getInventory();
        for (ItemStack itemHas : inventory.getStorageContents()) {
            if (itemHas == null || !predicate.test(itemHas)) continue;

            int hasAmount = itemHas.getAmount();
            if (takenAmount + hasAmount > amount) {
                int diff = (takenAmount + hasAmount) - amount;
                itemHas.setAmount(diff);
                break;
            }

            itemHas.setAmount(0);
            if ((takenAmount += hasAmount) == amount) {
                break;
            }
        }
        return true;
    }

    public static void addItem(@NotNull Player player, @NotNull ItemStack... items) {
        Arrays.asList(items).forEach(item -> addItem(player, item, item.getAmount()));
    }

    public static void addItem(@NotNull Player player, @NotNull ItemStack item2, int amount) {
        if (amount <= 0 || item2.getType().isAir()) return;

        World world = player.getWorld();
        ItemStack item = new ItemStack(item2);

        int realAmount = Math.min(item.getMaxStackSize(), amount);
        item.setAmount(realAmount);
        player.getInventory().addItem(item).values().forEach(left -> {
            world.dropItem(player.getLocation(), left);
        });

        amount -= realAmount;
        if (amount > 0) addItem(player, item2, amount);
    }
}

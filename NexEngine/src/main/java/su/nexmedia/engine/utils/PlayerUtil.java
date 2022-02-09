package su.nexmedia.engine.utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class PlayerUtil {

    public static void dispatchCommand(@NotNull Player player, @NotNull String command) {
        CommandSender sender = player;
        if (command.startsWith("[CONSOLE]")) {
            command = command.replace("[CONSOLE]", "");
            sender = Bukkit.getConsoleSender();
        }
        command = command.trim().replace("%player%", player.getName());
        Bukkit.dispatchCommand(sender, command);
    }

    @NotNull
    public static List<String> getPlayerNames() {
        return Bukkit.getServer().getOnlinePlayers().stream().map(Player::getName).toList();
    }

    @NotNull
    @Deprecated
    public static String getIP(@NotNull Player player) {
        InetSocketAddress inet = player.getAddress();
        return inet == null ? "null" : getIP(inet.getAddress());
    }

    @NotNull
    @Deprecated
    public static String getIP(@NotNull InetAddress inet) {
        return inet.toString().replace("\\/", "").replace("/", "");
    }

    @Deprecated
    public static void setExp(@NotNull Player player, long amount) {
        amount += getTotalExperience(player);

        if (amount > 2147483647L) {
            amount = 2147483647L;
        }
        if (amount < 0L) {
            amount = 0L;
        }

        setTotalExperience(player, (int) amount);
    }

    @Deprecated
    public static void setTotalExperience(@NotNull Player player, int exp) {
        if (exp < 0) {
            throw new IllegalArgumentException("Experience is negative!");
        }
        player.setExp(0.0F);
        player.setLevel(0);
        player.setTotalExperience(0);

        int amount = exp;
        while (amount > 0) {
            int expToLevel = getExpAtLevel(player);
            amount -= expToLevel;
            if (amount >= 0) {
                player.giveExp(expToLevel);
            }
            else {
                amount += expToLevel;
                player.giveExp(amount);
                amount = 0;
            }
        }
    }

    @Deprecated
    private static int getExpAtLevel(@NotNull Player player) {
        return getExpAtLevel(player.getLevel());
    }

    @Deprecated
    public static int getExpAtLevel(int level) {
        if (level <= 15) {
            return 2 * level + 7;
        }
        if ((level >= 16) && (level <= 30)) {
            return 5 * level - 38;
        }
        return 9 * level - 158;
    }

    @Deprecated
    public static int getExpToLevel(int level) {
        int currentLevel = 0;
        int exp = 0;
        while (currentLevel < level) {
            exp += getExpAtLevel(currentLevel);
            currentLevel++;
        }
        if (exp < 0) {
            exp = Integer.MAX_VALUE;
        }
        return exp;
    }

    @Deprecated
    public static int getTotalExperience(@NotNull Player player) {
        int exp = Math.round(getExpAtLevel(player) * player.getExp());
        int currentLevel = player.getLevel();
        while (currentLevel > 0) {
            currentLevel--;
            exp += getExpAtLevel(currentLevel);
        }
        if (exp < 0) {
            exp = Integer.MAX_VALUE;
        }
        return exp;
    }

    @Deprecated
    public static int getExpUntilNextLevel(@NotNull Player player) {
        int exp = Math.round(getExpAtLevel(player) * player.getExp());
        int nextLevel = player.getLevel();
        return getExpAtLevel(nextLevel) - exp;
    }

    public static boolean hasEmptyInventory(@NotNull Player player) {
        return Stream.of(player.getInventory().getContents()).allMatch(item -> item == null || item.getType().isAir());
    }

    public static int countItemSpace(@NotNull Player player, @NotNull ItemStack item) {
        int space = 0;
        int stackSize = item.getType().getMaxStackSize();
        for (int slot = 0; slot < 36; slot++) {
            ItemStack itemHas = player.getInventory().getItem(slot);
            if (itemHas == null || itemHas.getType().isAir()) {
                space += stackSize;
                continue;
            }
            if (itemHas.isSimilar(item)) {
                space += (stackSize - itemHas.getAmount());
            }
        }
        return space;
    }

    public static int countItem(@NotNull Player player, @NotNull Predicate<ItemStack> predicate) {
        int userHas = 0;
        for (ItemStack itemHas : player.getInventory().getContents()) {
            if (itemHas != null && !itemHas.getType().isAir() && predicate.test(itemHas)) {
                userHas += itemHas.getAmount();
            }
        }
        return userHas;
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
        for (ItemStack itemHas : inventory.getContents()) {
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
        Inventory inventory = player.getInventory();
        World world = player.getWorld();

        for (ItemStack item2 : items) {
            ItemStack item = new ItemStack(item2);
            if (item.getType().isAir()) continue;

            int space = countItemSpace(player, item);
            if (space < item.getAmount()) {
                ItemStack drop = new ItemStack(item);
                drop.setAmount(item.getAmount() - space);
                item.setAmount(space);
                world.dropItem(player.getLocation(), drop);
            }
            if (item.getAmount() > 0) {
                inventory.addItem(item);
            }
        }
    }
}

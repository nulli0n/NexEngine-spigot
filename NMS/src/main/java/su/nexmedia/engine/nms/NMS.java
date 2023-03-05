package su.nexmedia.engine.nms;

import io.netty.channel.Channel;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface NMS {

    @Deprecated
    @NotNull String toJSON(@NotNull ItemStack item);

    @NotNull String getNBTTag(@NotNull ItemStack item);

    @Nullable String toBase64(@NotNull ItemStack item);

    @Nullable ItemStack fromBase64(@NotNull String data);

    /**
     * @param player
     * @param i 0 = main hand, 3 = off hand.
     */
    @Deprecated
    void sendAttackPacket(@NotNull Player player, int i);

    boolean breakBlock(@NotNull Player player, @NotNull Block block);

    /**
     * Defines how long it takes to mine that block.
     * @param block Block object
     * @return the block strength value.
     */
    float getBlockStrength(@NotNull Block block);

    /**
     * Defines how much the block is protected from explosions.
     * @param block Block object
     * @return the block durability value.
     */
    float getBlockDurability(@NotNull Block block);

    @NotNull List<ItemStack> getBlockDrops(@NotNull Block block, @NotNull Player player, @NotNull ItemStack item);

    @Deprecated
    @NotNull Channel getChannel(@NotNull Player player);

    @Deprecated
    void sendPacket(@NotNull Player player, @NotNull Object packet);

    @NotNull ItemStack damageItem(@NotNull ItemStack item, int amount, @Nullable Player player);

    @Deprecated
    @NotNull String fixColors(@NotNull String str);


    @Deprecated
    double getDefaultDamage(@NotNull ItemStack itemStack);

    @Deprecated
    double getDefaultSpeed(@NotNull ItemStack itemStack);

    @Deprecated
    double getDefaultArmor(@NotNull ItemStack itemStack);

    @Deprecated
    double getDefaultToughness(@NotNull ItemStack itemStack);


    boolean isTool(@NotNull ItemStack itemStack);

    boolean isArmor(@NotNull ItemStack itemStack);

    boolean isWearable(@NotNull ItemStack itemStack);


    boolean isSword(@NotNull ItemStack itemStack);

    boolean isAxe(@NotNull ItemStack itemStack);

    boolean isPickaxe(@NotNull ItemStack itemStack);

    boolean isShovel(@NotNull ItemStack itemStack);

    boolean isHoe(@NotNull ItemStack itemStack);


    boolean isHelmet(@NotNull ItemStack itemStack);

    boolean isChestplate(@NotNull ItemStack itemStack);

    boolean isLeggings(@NotNull ItemStack itemStack);

    boolean isBoots(@NotNull ItemStack itemStack);
}

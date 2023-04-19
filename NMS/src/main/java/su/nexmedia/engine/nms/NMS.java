package su.nexmedia.engine.nms;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface NMS {

    @NotNull String getNBTTag(@NotNull ItemStack item);

    @Nullable String toBase64(@NotNull ItemStack item);

    @Nullable ItemStack fromBase64(@NotNull String data);

    @NotNull ItemStack damageItem(@NotNull ItemStack item, int amount, @Nullable Player player);

    void updateMenuTitle(@NotNull Player player, @NotNull String title);
}

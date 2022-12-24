package su.nexmedia.engine.api.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface MenuClick {

    void click(@NotNull Player player, @Nullable Enum<?> type, @NotNull InventoryClickEvent e);
}

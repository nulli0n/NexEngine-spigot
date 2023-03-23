package su.nexmedia.engine.api.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.api.manager.AbstractListener;

import java.util.Map;
import java.util.WeakHashMap;

@Deprecated
public class MenuListener<P extends NexPlugin<P>> extends AbstractListener<P> {

    private static final Map<Player, Long> FAST_CLICK = new WeakHashMap<>();

    private final AbstractMenu<P> menu;

    public MenuListener(@NotNull AbstractMenu<P> menu) {
        super(menu.plugin);
        this.menu = menu;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEventClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        AbstractMenu<?> menu = AbstractMenu.getMenu(player);
        if (menu == null || !menu.getId().equals(this.menu.getId())) return;

        // Fix visual glitch when item goes in player's offhand.
        if (e.getClick() == ClickType.SWAP_OFFHAND) {
            this.plugin.runTask(task -> player.updateInventory());
        }

        long lastClick = FAST_CLICK.getOrDefault(player, 0L);
        if (System.currentTimeMillis() - lastClick < 150) {
            e.setCancelled(true);
            return;
        }

        Inventory inventory = e.getInventory();
        ItemStack item = e.getCurrentItem();

        int slot = e.getRawSlot();
        boolean isPlayerSlot = slot >= inventory.getSize();
        boolean isEmptyItem = item == null || item.getType().isAir();

        AbstractMenu.SlotType slotType = isPlayerSlot ? (isEmptyItem ? AbstractMenu.SlotType.EMPTY_PLAYER : AbstractMenu.SlotType.PLAYER) : (isEmptyItem ? AbstractMenu.SlotType.EMPTY_MENU : AbstractMenu.SlotType.MENU);
        if (this.menu.cancelClick(e, slotType)) {
            e.setCancelled(true);
        }

        this.menu.onClick(player, item, slot, e);
        FAST_CLICK.put(player, System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEventDrag(InventoryDragEvent e) {
        Player player = (Player) e.getWhoClicked();
        AbstractMenu<?> menu = AbstractMenu.getMenu(player);
        if (menu == null || !menu.getId().equals(this.menu.getId())) return;

        if (this.menu.cancelClick(e)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEventClose(InventoryCloseEvent e) {
        Player player = (Player) e.getPlayer();
        AbstractMenu<?> menu = AbstractMenu.getMenu(player);
        if (menu == null || !menu.getId().equals(this.menu.getId())) return;

        this.menu.onClose(player, e);
        FAST_CLICK.remove(player);
    }
}

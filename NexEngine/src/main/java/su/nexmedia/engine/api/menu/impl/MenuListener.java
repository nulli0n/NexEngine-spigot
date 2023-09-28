package su.nexmedia.engine.api.menu.impl;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexEngine;
import su.nexmedia.engine.api.manager.AbstractListener;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public class MenuListener extends AbstractListener<NexEngine> {

    private static final Map<UUID, Long> FAST_CLICK = new WeakHashMap<>();

    public MenuListener(@NotNull NexEngine engine) {
        super(engine);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        Menu.PLAYER_MENUS.remove(event.getPlayer().getUniqueId());
        FAST_CLICK.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onMenuItemClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();

        Menu<?> menu = Menu.getMenu(player);
        if (menu == null) return;

        MenuViewer viewer = menu.getViewer(player);
        if (viewer == null) return;

        // Fix visual glitch when item goes in player's offhand.
        /*if (event.getClick() == ClickType.SWAP_OFFHAND || event.isShiftClick()) {
            this.plugin.runTask(task -> player.updateInventory());
        }*/

        // Prevent clicks spam in our GUIs.
        long lastClick = FAST_CLICK.getOrDefault(player.getUniqueId(), 0L);
        if (System.currentTimeMillis() - lastClick < 150) {
            event.setCancelled(true);
            return;
        }

        Inventory inventory = event.getInventory();
        ItemStack item = event.getCurrentItem();

        int slot = event.getRawSlot();
        boolean isPlayerSlot = slot >= inventory.getSize();
        boolean isEmptyItem = item == null || item.getType().isAir();

        Menu.SlotType slotType;
        if (isPlayerSlot) {
            slotType = isEmptyItem ? Menu.SlotType.PLAYER_EMPTY : Menu.SlotType.PLAYER;
        }
        else slotType = isEmptyItem ? Menu.SlotType.MENU_EMPTY : Menu.SlotType.MENU;

        menu.onClick(viewer, item, slotType, slot, event);
        FAST_CLICK.put(player.getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onMenuItemDrag(InventoryDragEvent event) {
        Player player = (Player) event.getWhoClicked();

        Menu<?> menu = Menu.getMenu(player);
        if (menu == null) return;

        MenuViewer viewer = menu.getViewer(player);
        if (viewer == null) return;

        menu.onDrag(viewer, event);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onMenuClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();

        Menu<?> menu = Menu.getMenu(player);
        if (menu == null) return;

        MenuViewer viewer = menu.getViewer(player);
        if (viewer == null) return;

        menu.onClose(viewer, event);
        FAST_CLICK.remove(player.getUniqueId());
    }
}

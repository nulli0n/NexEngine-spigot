package su.nexmedia.engine.api.menu;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.NexEngine;
import su.nexmedia.engine.actions.ActionManipulator;
import su.nexmedia.engine.api.manager.ICleanable;
import su.nexmedia.engine.api.type.ClickType;
import su.nexmedia.engine.utils.ItemUtil;

import java.util.*;

public interface IMenu extends ICleanable {

    Map<Player, IMenu> PLAYER_MENUS = new HashMap<>();

    @Nullable
    static IMenu getMenu(@NotNull Player player) {
        return PLAYER_MENUS.get(player);
    }

    @NotNull UUID getId();

    @NotNull String getTitle();

    void setTitle(@NotNull String title);

    int getSize();

    void setSize(int size);

    long getAnimationInterval();

    void setAnimationInterval(long interval);

    default boolean isAnimationEnabled() {
        return this.getAnimationInterval() > 0;
    }

    @NotNull Map<String, IMenuItem> getItemsMap();

    @NotNull Map<Player, List<IMenuItem>> getUserItemsMap();

    @NotNull
    default List<IMenuItem> getUserItems(@NotNull Player player) {
        return this.getUserItemsMap().computeIfAbsent(player, p -> new ArrayList<>());
    }

    @Nullable
    default IMenuItem getItem(@NotNull String id) {
        return this.getItemsMap().get(id.toLowerCase());
    }

    @Nullable
    default IMenuItem getItem(int slot) {
        return this.getItemsMap().values().stream()
            .filter(item -> ArrayUtils.contains(item.getSlots(), slot))
            .max(Comparator.comparingInt(m -> m.getType() == null || m.getType() == MenuItemType.NONE ? -1 : m.getType().ordinal()))
            /*.findFirst()*/.orElse(null);
    }

    @Nullable
    default IMenuItem getItem(@NotNull Player player, int slot) {
        return this.getUserItems(player).stream()
            .filter(item -> ArrayUtils.contains(item.getSlots(), slot))
            .max(Comparator.comparingInt(m -> m.getType() == null || m.getType() == MenuItemType.NONE ? -1 : m.getType().ordinal()))
            /*.findFirst()*/.orElse(this.getItem(slot));
    }

    void addItem(@NotNull ItemStack item, int... slots);

    void addItem(@NotNull Player player, @NotNull ItemStack item, int... slots);

    default void addItem(@NotNull IMenuItem menuItem) {
        this.getItemsMap().put(menuItem.getId(), menuItem);
    }

    default void addItem(@NotNull Player player, @NotNull IMenuItem menuItem) {
        this.getUserItems(player).add(menuItem);
    }

    @NotNull Map<Player, int[]> getUserPageMap();

    default int getPage(@NotNull Player player) {
        return this.getUserPageMap().getOrDefault(player, new int[]{-1, -1})[0];
    }

    default int getPageMax(@NotNull Player player) {
        return this.getUserPageMap().getOrDefault(player, new int[]{-1, -1})[1];
    }

    default void setPage(@NotNull Player player, int pageCurrent, int pageMax) {
        pageCurrent = Math.max(1, pageCurrent);
        pageMax = Math.max(1, pageMax);
        this.getUserPageMap().put(player, new int[]{Math.min(pageCurrent, pageMax), pageMax});
    }

    @NotNull Set<Player> getViewers();

    boolean destroyWhenNoViewers();

    default boolean isViewer(@NotNull Player player) {
        return this.getViewers().contains(player);
    }

    @NotNull
    default Inventory getInventory() {
        return Bukkit.getServer().createInventory(null, this.getSize(), this.getTitle());
    }

    @Deprecated
    boolean cancelClick(@NotNull SlotType slotType, int slot);

    default boolean cancelClick(@NotNull InventoryClickEvent e, @NotNull SlotType slotType) {
        return false;
    }

    void onPrepare(@NotNull Player player, @NotNull Inventory inventory);

    void onReady(@NotNull Player player, @NotNull Inventory inventory);

    default void open(@NotNull Player player, int page) {
        if (player.isSleeping()) return;

        Inventory inventory;
        if (this.isViewer(player)) {
            this.getUserItemsMap().remove(player);
            inventory = player.getOpenInventory().getTopInventory();
            inventory.clear();
        }
        else {
            inventory = this.getInventory();
        }

        this.setPage(player, page, page);
        this.onPrepare(player, inventory);
        this.setItems(player, inventory);
        this.onReady(player, inventory);
        if (this.getViewers().add(player)) {
            player.openInventory(inventory);
        }
        PLAYER_MENUS.put(player, this);
    }

    default void update() {
        this.getViewers().forEach(player -> this.open(player, this.getPage(player)));
    }

    default void setItems(@NotNull Player player, @NotNull Inventory inventory) {
        // Auto paginator
        int page = this.getPage(player);
        int pages = this.getPageMax(player);

        List<IMenuItem> items = new ArrayList<>();
        items.addAll(this.getItemsMap().values());
        items = new ArrayList<>(items.stream().sorted((i1, i2) -> {
            Enum<?> t1 = i1.getType();
            Enum<?> t2 = i2.getType();
            int o1 = t1 == null ? -1 : t1.ordinal();
            int o2 = t2 == null ? -1 : t2.ordinal();
            return o1 - o2;
        }).toList());
        items.addAll(this.getUserItems(player));


        for (IMenuItem menuItem : items) {

            if (menuItem.getType() == MenuItemType.PAGE_NEXT) {
                if (/*page < 0 || pages < 0 || */page >= pages) {
                    continue;
                }
            }
            if (menuItem.getType() == MenuItemType.PAGE_PREVIOUS) {
                if (page <= 1) {
                    continue;
                }
            }

            MenuItemDisplay display = this.onItemDisplayPrepare(player, menuItem);
            if (display == null) {
                NexEngine.get().error("Could not find display for menu item: '" + menuItem.getId() + "'.");
                continue;
            }

            ItemStack item = display.getItem();
            this.onItemPrepare(player, menuItem, item);

            for (int slot : menuItem.getSlots()) {
                if (slot >= inventory.getSize()) continue;
                inventory.setItem(slot, item);
            }
        }
    }

    @Nullable
    default MenuItemDisplay onItemDisplayPrepare(@NotNull Player player, @NotNull IMenuItem menuItem) {
        if (this.isAnimationEnabled() && menuItem.isAnimationEnabled()) {
            String displayId = menuItem.getAnimationFrames()[menuItem.getAnimationFrameCurrent()];
            MenuItemDisplay display = menuItem.getDisplay(displayId);
            if (display != null && (display.isAvailable(player) || !menuItem.isAnimationIgnoreUnvailableFrames())) {
                return display;
            }
        }
        return menuItem.getDisplay(player);
    }

    default void onItemPrepare(@NotNull Player player, @NotNull IMenuItem menuItem, @NotNull ItemStack item) {
        ItemUtil.applyPlaceholderAPI(player, item);
    }

    default void onClick(@NotNull Player player, @Nullable ItemStack item, int slot, @NotNull InventoryClickEvent e) {
        if (item == null || item.getType().isAir()) return;

        IMenuItem menuItem = this.getItem(player, slot);
        if (menuItem == null) return;

        IMenuClick click = menuItem.getClick();
        if (click != null) click.click(player, menuItem.getType(), e);

        // Execute custom user actions when click button.
        ClickType clickType = ClickType.from(e);
        ActionManipulator actions = menuItem.getClickCustomAction(clickType);
        if (actions != null) {
            actions.process(player);
        }
    }

    default void onClose(@NotNull Player player, @NotNull InventoryCloseEvent e) {
        this.getUserPageMap().remove(player);
        this.getUserItemsMap().remove(player);
        this.getViewers().remove(player);

        PLAYER_MENUS.remove(player);

        if (this.getViewers().isEmpty() && this.destroyWhenNoViewers()) {
            this.clear();
        }
    }

    enum SlotType {
        EMPTY_PLAYER, EMPTY_MENU, PLAYER, MENU
    }
}

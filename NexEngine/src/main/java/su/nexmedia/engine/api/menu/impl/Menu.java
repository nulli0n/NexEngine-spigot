package su.nexmedia.engine.api.menu.impl;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.Version;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.api.menu.click.ItemClick;
import su.nexmedia.engine.api.menu.item.ItemOptions;
import su.nexmedia.engine.api.menu.item.MenuItem;

import java.util.*;
import java.util.stream.Collectors;

public abstract class Menu<P extends NexPlugin<P>> {

    static final Map<UUID, Menu<?>> PLAYER_MENUS = new HashMap<>();

    protected final P                     plugin;
    protected final UUID                  id;
    protected final MenuOptions           options;
    protected final Map<UUID, MenuViewer> viewers;
    protected final Set<MenuItem>         items;

    public Menu(@NotNull P plugin, @NotNull String title, @NotNull InventoryType type) {
        this(plugin, new MenuOptions(title, 27, type));
    }

    public Menu(@NotNull P plugin, @NotNull String title, int size) {
        this(plugin, new MenuOptions(title, size, InventoryType.CHEST));
    }

    public Menu(@NotNull P plugin, @NotNull MenuOptions options) {
        this.plugin = plugin;
        this.id = UUID.randomUUID();
        this.options = new MenuOptions(options);
        this.viewers = new HashMap<>();
        this.items = new HashSet<>();
    }

    public void clear() {
        this.getItems().clear();
        new HashSet<>(this.getViewers()).forEach(viewer -> viewer.getPlayer().closeInventory());
        this.getViewers().clear();
    }

    public enum SlotType {
        PLAYER_EMPTY, MENU_EMPTY, PLAYER, MENU
    }

    @Nullable
    public static Menu<?> getMenu(@NotNull Player player) {
        return PLAYER_MENUS.get(player.getUniqueId());
    }

    @NotNull
    public P plugin() {
        return this.plugin;
    }

    public void update() {
        this.getViewers().forEach(viewer -> this.open(viewer.getPlayer(), viewer.getPage()));
    }

    public void openNextTick(@NotNull MenuViewer viewer, int page) {
        this.plugin.runTask(task -> this.open(viewer, page));
    }

    public void openNextTick(@NotNull Player player, int page) {
        this.plugin.runTask(task -> this.open(player, page));
    }

    public boolean open(@NotNull MenuViewer viewer, int page) {
        return this.open(viewer.getPlayer(), page);
    }

    public boolean open(@NotNull Player player, int page) {
        if (!this.canOpen(player, page)) {
            this.plugin.runTask(task -> player.closeInventory());
            return false;
        }

        MenuViewer previous = this.getViewer(player);
        MenuViewer viewer = previous == null ? new MenuViewer(player) : previous;
        MenuOptions options = new MenuOptions(this.getOptions());
        boolean isFresh = previous == null;

        this.getItems().removeIf(menuItem -> menuItem.getOptions().canBeDestroyed(viewer));
        viewer.setPage(page);
        this.onPrepare(viewer, options);

        Inventory inventory;
        if (isFresh) {
            inventory = options.createInventory();
        }
        else {
            inventory = player.getOpenInventory().getTopInventory();
            inventory.clear();
        }

        this.getItems(viewer).forEach(menuItem -> {
            ItemStack item = menuItem.getItem();
            menuItem.getOptions().modifyDisplay(viewer, item);

            for (int slot : menuItem.getSlots()) {
                if (slot < 0 || slot >= inventory.getSize()) continue;
                inventory.setItem(slot, item);
            }
        });

        this.onReady(viewer, inventory);

        if (isFresh) {
            player.openInventory(inventory);
            this.getViewersMap().put(player.getUniqueId(), viewer);
        }
        else {
            if (Version.isAtLeast(Version.V1_19_R3)) {
                viewer.getPlayer().getOpenInventory().setTitle(options.getTitle());
            }
        }

        PLAYER_MENUS.put(player.getUniqueId(), this);
        return true;
    }

    public boolean canOpen(@NotNull Player player, int page) {
        return !player.isSleeping();
    }

    public void onPrepare(@NotNull MenuViewer viewer, @NotNull MenuOptions options) {

    }

    public void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {

    }

    public void onClick(@NotNull MenuViewer viewer,
                        @Nullable ItemStack item,
                        @NotNull SlotType slotType,
                        int slot,
                        @NotNull InventoryClickEvent event) {
        event.setCancelled(true);

        if (item == null || item.getType().isAir()) return;

        MenuItem menuItem = this.getItem(viewer, slot);
        if (menuItem == null) return;

        ItemClick click = menuItem.getClick();
        if (click != null) click.click(viewer, event);
    }

    public void onDrag(@NotNull MenuViewer viewer, @NotNull InventoryDragEvent event) {
        event.setCancelled(true);
    }

    public void onClose(@NotNull MenuViewer viewer, @NotNull InventoryCloseEvent event) {
        Player player = viewer.getPlayer();

        this.getViewersMap().remove(player.getUniqueId());
        this.getItems().removeIf(menuItem -> menuItem.getOptions().canBeDestroyed(viewer));
        PLAYER_MENUS.remove(player.getUniqueId());

        if (this.getViewers().isEmpty() && !this.isPersistent()) {
            this.clear();
        }
    }

    public boolean isPersistent() {
        return true;
    }

    @NotNull
    public Collection<MenuViewer> getViewers() {
        return this.getViewersMap().values();
    }

    @Nullable
    public MenuViewer getViewer(@NotNull Player player) {
        return this.getViewersMap().get(player.getUniqueId());
    }



    @NotNull
    public Set<MenuItem> getItems(@NotNull Enum<?> type) {
        return this.getItems().stream().filter(menuItem -> menuItem.getType() == type).collect(Collectors.toSet());
    }

    @NotNull
    public List<MenuItem> getItems(@NotNull MenuViewer viewer) {
        return this.getItems().stream()
            .filter(menuItem -> {
                if (menuItem.getType() == MenuItemType.PAGE_NEXT && viewer.getPage() >= viewer.getPages()) return false;
                if (menuItem.getType() == MenuItemType.PAGE_PREVIOUS && viewer.getPage() == 1) return false;
                return menuItem.getOptions().canSee(viewer);
            })
            .sorted(Comparator.comparingInt(MenuItem::getPriority)).toList();
    }

    @Nullable
    public MenuItem getItem(int slot) {
        return this.getItems().stream()
            .filter(item -> ArrayUtils.contains(item.getSlots(), slot))
            .max(Comparator.comparingInt(MenuItem::getPriority)).orElse(null);
    }

    @Nullable
    public MenuItem getItem(@NotNull MenuViewer viewer, int slot) {
        return this.getItems(viewer).stream()
            .filter(menuItem -> ArrayUtils.contains(menuItem.getSlots(), slot))
            .max(Comparator.comparingInt(MenuItem::getPriority)).orElse(null);
    }

    @NotNull
    public MenuItem addItem(@NotNull ItemStack item, int... slots) {
        return this.addItem(new MenuItem(item, slots));
    }

    @NotNull
    public MenuItem addWeakItem(@NotNull Player player, @NotNull ItemStack item, int... slots) {
        MenuItem menuItem = new MenuItem(item, slots);
        menuItem.setOptions(ItemOptions.personalWeak(player));
        return this.addItem(menuItem);
    }

    @NotNull
    public MenuItem addItem(@NotNull MenuItem menuItem) {
        this.getItems().add(menuItem);
        return menuItem;
    }

    @NotNull
    public UUID getId() {
        return id;
    }

    @NotNull
    public Map<UUID, MenuViewer> getViewersMap() {
        return viewers;
    }

    @NotNull
    public Set<MenuItem> getItems() {
        return items;
    }

    @NotNull
    public MenuOptions getOptions() {
        return options;
    }
}

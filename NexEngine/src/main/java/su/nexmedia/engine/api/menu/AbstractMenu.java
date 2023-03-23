package su.nexmedia.engine.api.menu;

import me.clip.placeholderapi.PlaceholderAPI;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.entity.HumanEntity;
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
import su.nexmedia.engine.api.config.JOption;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.manager.AbstractListener;
import su.nexmedia.engine.api.manager.ICleanable;
import su.nexmedia.engine.api.type.ClickType;
import su.nexmedia.engine.hooks.Hooks;
import su.nexmedia.engine.utils.Colorizer;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.Pair;
import su.nexmedia.engine.utils.PlayerUtil;

import java.util.*;

@Deprecated
public abstract class AbstractMenu<P extends NexPlugin<P>> extends AbstractListener<P> implements ICleanable {

    private static final Map<Player, AbstractMenu<?>> PLAYER_MENUS = new WeakHashMap<>();

    protected final UUID                                    id;
    protected final Map<String, MenuItem>                   items;
    protected final Map<Player, Pair<Integer, Integer>> viewersMap;

    protected String title;
    protected int    size;
    protected InventoryType inventoryType;
    protected JYML   cfg;

    private boolean useMiniMessage;

    private MenuListener<P> listener;

    public AbstractMenu(@NotNull P plugin, @NotNull JYML cfg, @NotNull String path) {
        this(plugin, cfg.getString(path + "Title", ""), cfg.getInt(path + "Size"));
        this.cfg = cfg;
        this.setInventoryType(JOption.create("Inventory_Type", InventoryType.class, InventoryType.CHEST,
            "Sets Inventory Type for the GUI.",
            "https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/event/inventory/InventoryType.html").read(cfg));
        this.useMiniMessage = JOption.create("Use_Mini_Message", false,
            "Sets whether to use Paper's MiniMessage API for the GUI Title.").read(cfg);
    }

    public AbstractMenu(@NotNull P plugin, @NotNull String title, int size) {
        super(plugin);
        this.id = UUID.randomUUID();
        this.setTitle(title);
        this.setSize(size);
        this.setInventoryType(InventoryType.CHEST);

        this.items = new LinkedHashMap<>();
        this.viewersMap = new WeakHashMap<>();

        this.listener = new MenuListener<>(this);
        this.listener.registerListeners();
        this.registerListeners();
    }

    @Override
    public void clear() {
        this.viewersMap.keySet().forEach(HumanEntity::closeInventory);
        this.viewersMap.clear();
        this.items.clear();
        this.unregisterListeners();
        this.listener.unregisterListeners();
        this.listener = null;
        this.cfg = null;
    }

    public enum SlotType {
        EMPTY_PLAYER, EMPTY_MENU, PLAYER, MENU
    }

    @Nullable
    public static AbstractMenu<?> getMenu(@NotNull Player player) {
        return PLAYER_MENUS.get(player);
    }

    protected void onItemClickDefault(@NotNull Player player, @NotNull MenuItemType itemType) {
        int pageMax = this.getPageMax(player);
        switch (itemType) {
            case CLOSE -> player.closeInventory();
            case PAGE_NEXT -> this.open(player, Math.min(pageMax, this.getPage(player) + 1));
            case PAGE_PREVIOUS -> this.open(player, Math.max(1, this.getPage(player) - 1));
            default -> {}
        }
    }

    public boolean onPrepare(@NotNull Player player, @NotNull Inventory inventory) {
        return true;
    }

    public boolean onReady(@NotNull Player player, @NotNull Inventory inventory) {
        return true;
    }

    public abstract boolean cancelClick(@NotNull InventoryClickEvent e, @NotNull SlotType slotType);

    public boolean cancelClick(@NotNull InventoryDragEvent e) {
        return true;
    }

    public boolean open(@NotNull Player player, int page) {
        if (player.isSleeping()) return false;

        Inventory inventory;
        boolean freshOpen = false;
        if (this.isViewer(player)) {
            this.resetItemsVisibility(player);
            inventory = player.getOpenInventory().getTopInventory();
            inventory.clear();
        }
        else {
            inventory = this.createInventory(player);
            freshOpen = true;
        }

        this.setPage(player, page, page);
        if (!this.onPrepare(player, inventory)) {
            this.getViewersMap().remove(player);
            return false;
        }
        this.setItems(player, inventory);
        if (!this.onReady(player, inventory)) {
            this.getViewersMap().remove(player);
            return false;
        }
        if (freshOpen) {
            player.openInventory(inventory);
        }
        PLAYER_MENUS.put(player, this);
        return true;
    }

    public void update() {
        this.getViewers().forEach(player -> this.open(player, this.getPage(player)));
    }

    public void setItems(@NotNull Player player, @NotNull Inventory inventory) {
        // Auto paginator
        int page = this.getPage(player);
        int pages = this.getPageMax(player);

        List<MenuItem> items = this.getItemsMap().values().stream()
            .filter(menuItem -> menuItem.isVisible(player))
            .sorted(Comparator.comparingInt(MenuItem::getPriority)).toList();

        for (MenuItem menuItem : items) {
            if (menuItem.getType() == MenuItemType.PAGE_NEXT) {
                if (page >= pages) {
                    continue;
                }
            }
            if (menuItem.getType() == MenuItemType.PAGE_PREVIOUS) {
                if (page <= 1) {
                    continue;
                }
            }

            ItemStack item = menuItem.getItem();
            this.onItemPrepare(player, menuItem, item);

            for (int slot : menuItem.getSlots()) {
                if (slot >= inventory.getSize()) continue;
                inventory.setItem(slot, item);
            }
        }
    }

    public void onItemPrepare(@NotNull Player player, @NotNull MenuItem menuItem, @NotNull ItemStack item) {
        ItemUtil.setPlaceholderAPI(player, item);
    }

    public void onClick(@NotNull Player player, @Nullable ItemStack item, int slot, @NotNull InventoryClickEvent e) {
        if (item == null || item.getType().isAir()) return;

        MenuItem menuItem = this.getItem(player, slot);
        if (menuItem == null) return;

        MenuClick click = menuItem.getClickHandler();
        if (click != null) click.click(player, menuItem.getType(), e);

        // Execute custom user actions when click button.
        ClickType clickType = ClickType.from(e);
        menuItem.getClickCommands(clickType).forEach(command -> PlayerUtil.dispatchCommand(player, command));
    }

    public void onClose(@NotNull Player player, @NotNull InventoryCloseEvent e) {
        this.getViewersMap().remove(player);
        this.resetItemsVisibility(player);

        PLAYER_MENUS.remove(player);

        if (this.getViewers().isEmpty() && this.destroyWhenNoViewers()) {
            this.clear();
        }
    }

    protected void resetItemsVisibility(@NotNull Player player) {
        this.getItemsMap().values().removeIf(menuItem -> {
            return menuItem instanceof WeakMenuItem weakMenuItem && weakMenuItem.getWeakPolicy().test(player);
        });
        this.getItemsMap().values().forEach(menuItem -> menuItem.resetVisibility(player));
    }

    public boolean isViewer(@NotNull Player player) {
        return this.getViewersMap().containsKey(player);
    }

    @NotNull
    public Inventory createInventory(@NotNull Player player) {
        String title = this.getTitle(player);
        // TODO
        /*if (NexPlugin.isPaper && this.useMiniMessage) {
            if (this.getInventoryType() == InventoryType.CHEST) {
                return this.plugin.getServer().createInventory(null, this.getSize(), MiniMessage.miniMessage().deserialize(title));
            }
            else {
                return this.plugin.getServer().createInventory(null, this.getInventoryType(), MiniMessage.miniMessage().deserialize(title));
            }
        }
        else {*/
            if (this.getInventoryType() == InventoryType.CHEST) {
                return this.plugin.getServer().createInventory(null, this.getSize(), title);
            }
            else {
                return this.plugin.getServer().createInventory(null, this.getInventoryType(), title);
            }
        //}
    }

    @Nullable
    public MenuItem getItem(@NotNull String id) {
        return this.getItemsMap().get(id.toLowerCase());
    }

    @Nullable
    public MenuItem getItem(int slot) {
        return this.getItemsMap().values().stream()
            .filter(item -> ArrayUtils.contains(item.getSlots(), slot))
            .max(Comparator.comparingInt(MenuItem::getPriority)).orElse(null);
    }

    @Nullable
    public MenuItem getItem(@NotNull Player player, int slot) {
        return this.getItemsMap().values().stream()
            .filter(menuItem -> ArrayUtils.contains(menuItem.getSlots(), slot) && menuItem.isVisible(player))
            .max(Comparator.comparingInt(MenuItem::getPriority)).orElse(this.getItem(slot));
    }

    public void addItem(@NotNull ItemStack item, int... slots) {
        this.addItem(new MenuItem(item, slots));
    }

    @Deprecated
    public void addItem(@NotNull Player player, @NotNull ItemStack item, int... slots) {
        //this.addItem(player, new MenuItem(item, slots));
        this.addWeakItem(player, item, slots);
    }

    public void addItem(@NotNull MenuItem menuItem) {
        this.getItemsMap().put(menuItem.getId(), menuItem);
    }

    @Deprecated
    public void addItem(@NotNull Player player, @NotNull MenuItem menuItem) {
        WeakMenuItem weakMenuItem = new WeakMenuItem(player, menuItem.getItem(), menuItem.getSlots());
        weakMenuItem.setClickHandler(menuItem.getClickHandler());
        weakMenuItem.clickCommands = menuItem.clickCommands;
        this.addItem(weakMenuItem);
    }

    public void addWeakItem(@NotNull Player player, @NotNull ItemStack item, int... slots) {
        this.addItem(new WeakMenuItem(player, item, slots));
    }

    public int getPage(@NotNull Player player) {
        return this.getViewersMap().getOrDefault(player, Pair.of(-1,-1)).getFirst();
    }

    public int getPageMax(@NotNull Player player) {
        return this.getViewersMap().getOrDefault(player, Pair.of(-1,-1)).getSecond();
    }

    public void setPage(@NotNull Player player, int pageCurrent, int pageMax) {
        pageCurrent = Math.max(1, pageCurrent);
        pageMax = Math.max(1, pageMax);
        this.getViewersMap().put(player, Pair.of(Math.min(pageCurrent, pageMax), pageMax));
    }

    @NotNull
    public UUID getId() {
        return id;
    }

    @NotNull
    public String getTitle() {
        return title;
    }

    @NotNull
    public String getTitle(@NotNull Player player) {
        String title = this.getTitle();
        if (Hooks.hasPlaceholderAPI()) {
            title = PlaceholderAPI.setPlaceholders(player, title);
        }
        return title;
    }

    public void setTitle(@NotNull String title) {
        this.title = Colorizer.apply(title);
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    @NotNull
    public InventoryType getInventoryType() {
        return inventoryType;
    }

    public void setInventoryType(@NotNull InventoryType inventoryType) {
        this.inventoryType = inventoryType;
    }

    @NotNull
    public Map<String, MenuItem> getItemsMap() {
        return items;
    }

    @NotNull
    public Set<Player> getViewers() {
        return this.getViewersMap().keySet();
    }

    @NotNull
    public Map<Player, Pair<Integer, Integer>> getViewersMap() {
        return viewersMap;
    }

    public boolean destroyWhenNoViewers() {
        return false;
    }
}

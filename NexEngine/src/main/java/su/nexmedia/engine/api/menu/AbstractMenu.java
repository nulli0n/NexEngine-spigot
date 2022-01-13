package su.nexmedia.engine.api.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.manager.AbstractListener;
import su.nexmedia.engine.utils.StringUtil;

import java.util.*;

public abstract class AbstractMenu<P extends NexPlugin<P>> extends AbstractListener<P> implements IMenu {

    protected final UUID   id;
    protected final Map<Player, int[]>           userPage;
    protected final Set<Player>       viewers;
    private final   Map<Player, Long> fastClick;
    protected       String title;
    protected       int    size;
    protected       JYML   cfg;
    protected       Map<String, IMenuItem>       items;
    protected       Map<Player, List<IMenuItem>> userItems;
    protected long                 animationInterval;
    private   MenuAnimationTask<P> animationTask;

    public AbstractMenu(@NotNull P plugin, @NotNull JYML cfg, @NotNull String path) {
        this(plugin, cfg.getString(path + "Title", ""), cfg.getInt(path + "Size"));
        this.cfg = cfg;

        this.setAnimationInterval(cfg.getLong(path + "Animation.Tick"));
        this.setupAnimationTask();
    }

    public AbstractMenu(@NotNull P plugin, @NotNull String title, int size) {
        super(plugin);
        this.id = UUID.randomUUID();
        this.setTitle(title);
        this.setSize(size);

        this.items = new LinkedHashMap<>();
        this.userItems = new WeakHashMap<>();
        this.userPage = new WeakHashMap<>();
        this.viewers = new HashSet<>();
        this.fastClick = new HashMap<>();

        this.registerListeners();
    }

    @Override
    public void clear() {
        if (this.animationTask != null) {
            this.animationTask.stop();
            this.animationTask = null;
        }
        this.unregisterListeners();
        this.viewers.forEach(Player::closeInventory);
        this.viewers.clear();
        this.items.clear();
        this.userItems.clear();
        this.userPage.clear();
        this.fastClick.clear();
        this.cfg = null;
    }

    private void setupAnimationTask() {
        if (!this.isAnimationEnabled()) return;

        this.animationTask = new MenuAnimationTask<>(plugin, this);
        this.animationTask.start();
    }

    @Override
    public long getAnimationInterval() {
        return animationInterval;
    }

    @Override
    public void setAnimationInterval(long animationInterval) {
        this.animationInterval = animationInterval;
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

    @Override
    public void addItem(@NotNull ItemStack item, int... slots) {
        this.addItem(new MenuItem(item, slots));
    }

    @Override
    public void addItem(@NotNull Player player, @NotNull ItemStack item, int... slots) {
        this.addItem(player, new MenuItem(item, slots));
    }

    @NotNull
    @Override
    public UUID getId() {
        return id;
    }

    @NotNull
    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(@NotNull String title) {
        this.title = StringUtil.color(title);
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public void setSize(int size) {
        this.size = size;
    }

    @NotNull
    @Override
    public Map<String, IMenuItem> getItemsMap() {
        return items;
    }

    @NotNull
    @Override
    public Map<Player, List<IMenuItem>> getUserItemsMap() {
        return userItems;
    }

    @NotNull
    @Override
    public Map<Player, int[]> getUserPageMap() {
        return userPage;
    }

    @NotNull
    @Override
    public Set<Player> getViewers() {
        return viewers;
    }

    @Override
    public boolean destroyWhenNoViewers() {
        return false;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEventClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        IMenu menu = IMenu.getMenu(player);
        if (menu == null || !menu.getId().equals(this.getId())) return;

        long lastClick = this.fastClick.getOrDefault(player, 0L);
        if (System.currentTimeMillis() - lastClick < 150) { // TODO Config option
            e.setCancelled(true);
            return;
        }

        Inventory inventory = e.getInventory();
        ItemStack item = e.getCurrentItem();

        int slot = e.getRawSlot();
        boolean isPlayerSlot = slot >= inventory.getSize();
        boolean isEmptyItem = item == null || item.getType().isAir();

        SlotType slotType = isPlayerSlot ? (isEmptyItem ? SlotType.EMPTY_PLAYER : SlotType.PLAYER) : (isEmptyItem ? SlotType.EMPTY_MENU : SlotType.MENU);

        if (this.cancelClick(slotType, slot)) {
            e.setCancelled(true);
        }
        if (this.cancelClick(e, slotType)) {
            e.setCancelled(true);
        }

        this.onClick(player, item, slot, e);
        this.fastClick.put(player, System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEventDrag(InventoryDragEvent e) {
        Player player = (Player) e.getWhoClicked();
        IMenu menu = IMenu.getMenu(player);
        if (menu == null || !menu.getId().equals(this.getId())) return;

        if (this.cancelClick(e)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEventClose(InventoryCloseEvent e) {
        Player player = (Player) e.getPlayer();
        IMenu menu = IMenu.getMenu(player);
        if (menu == null || !menu.getId().equals(this.getId())) return;

        this.onClose(player, e);
    }
}

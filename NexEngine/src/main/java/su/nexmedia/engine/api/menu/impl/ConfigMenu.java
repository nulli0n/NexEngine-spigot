package su.nexmedia.engine.api.menu.impl;

import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.api.config.JOption;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.api.menu.click.ClickHandler;
import su.nexmedia.engine.api.menu.click.ClickType;
import su.nexmedia.engine.api.menu.click.ItemClick;
import su.nexmedia.engine.api.menu.item.MenuItem;
import su.nexmedia.engine.utils.Colorizer;
import su.nexmedia.engine.utils.PlayerUtil;
import su.nexmedia.engine.utils.StringUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class ConfigMenu<P extends NexPlugin<P>> extends Menu<P> {

    protected final JYML                                           cfg;
    protected final Map<Class<? extends Enum<?>>, ClickHandler<?>> handlers;
    protected String itemSection = "Content";

    public ConfigMenu(@NotNull P plugin, @NotNull JYML cfg) {
        super(plugin, "", 27);
        this.cfg = cfg;
        this.handlers = new HashMap<>();
    }

    public void load() {
        if (!this.isCodeCreation() || this.cfg.getBoolean("set_up")) {
            this.loadConfig();
        }
        else {
            this.loadDefaults();
            this.cfg.set("set_up", true);
            this.cfg.remove(this.itemSection);
            this.write();
        }
    }

    public boolean isCodeCreation() {
        return false;
    }

    public void loadDefaults() {

    }

    public void loadConfig() {
        String title = JOption.create("Title", "", "Sets the GUI title.")
            .mapReader(Colorizer::apply).read(cfg);

        int size = JOption.create("Size", 27, "Sets the GUI size. Must be multiply of 9.").read(cfg);

        InventoryType type = JOption.create("Inventory_Type", InventoryType.class, InventoryType.CHEST,
            "Sets the GUI type.",
            "https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/event/inventory/InventoryType.html").read(cfg);

        int autoRefresh = JOption.create("Settings.Auto_Refresh", 0,
            "Sets the GUI auto-refresh interval (in seconds). Set this to 0 to disable.").read(cfg);

        this.getOptions().setTitle(title);
        this.getOptions().setSize(size);
        this.getOptions().setType(type);
        this.getOptions().setAutoRefresh(autoRefresh);

        this.cfg.getSection(this.itemSection).forEach(sId -> {
            MenuItem menuItem = this.readItem(this.itemSection + "." + sId);
            this.addItem(menuItem);
        });

        // TODO All under 'Settings' + comment list with registered handler types
    }

    protected void write() {
        this.cfg.set("Title", this.getOptions().getTitle());
        this.cfg.set("Size", this.getOptions().getSize());
        this.cfg.set("Type", this.getOptions().getType().name());
        this.cfg.set("Settings.Auto_Refresh", this.getOptions().getAutoRefresh());

        AtomicInteger count = new AtomicInteger();
        this.getItems().forEach(menuItem -> {
            this.writeItem(menuItem, this.itemSection + "." + menuItem.getType().name() + "_" + count.incrementAndGet());
        });
        this.cfg.save();
    }

    @Override
    public void clear() {
        super.clear();
        this.handlers.clear();
    }

    @NotNull
    public <E extends Enum<E>> ClickHandler<E> registerHandler(@NotNull Class<E> clazz) {
        ClickHandler<E> handler = new ClickHandler<>();
        this.handlers.put(clazz, handler);
        return handler;
    }

    @NotNull
    public Set<Class<? extends Enum<?>>> getHandlerTypes() {
        return this.handlers.keySet();
    }

    @NotNull
    protected MenuItem readItem(@NotNull String path) {
        Enum<?> type = MenuItemType.NONE;
        ItemClick clickOrigin = null;

        String typeRaw = cfg.getString(path + ".Type", "");
        Label_Search:
        for (Class<? extends Enum<?>> clazz : this.getHandlerTypes()) {
            for (Enum<?> eType : clazz.getEnumConstants()) {
                if (eType.name().equalsIgnoreCase(typeRaw)) {
                    type = eType;
                    ClickHandler<?> handler = this.handlers.get(clazz);
                    if (handler != null) {
                        clickOrigin = handler.getClick(type);
                    }
                    break Label_Search;
                }
            }
        }

        ItemStack item = cfg.getItem(path + ".Item");
        int[] slots = cfg.getIntArray(path + ".Slots");
        int priority = cfg.getInt(path + ".Priority");

        ItemClick clickCommands = null;
        Map<ClickType, List<String>> commandMap = new HashMap<>();

        for (String sType : cfg.getSection(path + ".Click_Actions")) {
            ClickType clickType = StringUtil.getEnum(sType, ClickType.class).orElse(null);
            if (clickType == null) continue;

            List<String> commands = cfg.getStringList(path + ".Click_Actions." + sType);
            commandMap.put(clickType, commands);
        }

        if (!commandMap.isEmpty()) {
            clickCommands = ((viewer, event) -> {
                List<String> commands = commandMap.get(ClickType.from(event));
                if (commands == null || commands.isEmpty()) return;

                commands.forEach(command -> PlayerUtil.dispatchCommand(viewer.getPlayer(), command));
            });
        }

        MenuItem menuItem = new MenuItem(type, item, priority, slots);

        ItemClick finalClickOrigin = clickOrigin;
        ItemClick finalClickCommands = clickCommands;
        menuItem.setClick(((viewer, event) -> {
            if (finalClickOrigin != null) finalClickOrigin.click(viewer, event);
            if (finalClickCommands != null) finalClickCommands.click(viewer, event);
        }));

        return menuItem;
    }

    protected void writeItem(@NotNull MenuItem menuItem, @NotNull String path) {
        this.cfg.set(path + ".Priority", menuItem.getPriority());
        this.cfg.setItem(path + ".Item", menuItem.getItem());
        this.cfg.setIntArray(path + ".Slots", menuItem.getSlots());
        this.cfg.set(path + ".Type", menuItem.getType().name());
    }
}

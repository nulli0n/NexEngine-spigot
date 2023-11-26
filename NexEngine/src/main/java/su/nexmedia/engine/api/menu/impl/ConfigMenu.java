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
import su.nexmedia.engine.utils.*;

import java.util.*;
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
        this.loadConfig();
    }

    public boolean isCodeCreation() {
        return false;
    }

    @Deprecated
    public void loadDefaults() {

    }

    @NotNull
    protected MenuOptions createDefaultOptions() {
        return new MenuOptions("", 27, InventoryType.CHEST);
    }

    @NotNull
    protected List<MenuItem> createDefaultItems() {
        return new ArrayList<>();
    }

    public void loadConfig() {
        String oldTitle = cfg.getString("Title");
        int oldSize = cfg.getInt("Size", 0);
        InventoryType oldType = cfg.getEnum("Inventory_Type", InventoryType.class);

        boolean isCodeCreation = this.isCodeCreation() && !this.cfg.contains(this.itemSection);
        MenuOptions options;

        if (isCodeCreation) {
            options = this.createDefaultOptions();
        }
        else {
            options = new MenuOptions(oldTitle != null ? oldTitle : "", oldSize != 0 ? oldSize : 27, oldType != null ? oldType : InventoryType.CHEST);
        }

        String title = JOption.create("Settings.Title", options.getTitle(),
            "Sets the GUI title."
        ).mapReader(Colorizer::apply).read(cfg);

        int size = JOption.create("Settings.Size", options.getSize(),
            "Sets the GUI size. Must be multiply of 9.",
            "Useful for '" + InventoryType.CHEST.name() + "' Inventory Type only."
        ).read(cfg);

        InventoryType type = JOption.create("Settings.Inventory_Type", InventoryType.class, options.getType(),
            "Sets the GUI type.",
            "https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/event/inventory/InventoryType.html"
        ).read(cfg);

        int autoRefresh = JOption.create("Settings.Auto_Refresh", options.getAutoRefresh(),
            "Sets the GUI auto-refresh interval (in seconds). Set this to 0 to disable."
        ).read(cfg);

        this.getOptions().setTitle(title);
        this.getOptions().setSize(size);
        this.getOptions().setType(type);
        this.getOptions().setAutoRefresh(autoRefresh);

        this.loadAdditional();

        this.cfg.remove("Title");
        this.cfg.remove("Size");
        this.cfg.remove("Inventory_Type");

        if (isCodeCreation) {
            AtomicInteger count = new AtomicInteger();
            this.createDefaultItems().forEach(menuItem -> {
                String name = StringUtil.lowerCaseUnderscore(ItemUtil.getItemName(menuItem.getItem()));

                this.writeItem(menuItem, this.itemSection + "." + name + "_" + count.incrementAndGet());
            });
        }

        this.cfg.getSection(this.itemSection).forEach(sId -> {
            MenuItem menuItem = this.readItem(this.itemSection + "." + sId);
            this.addItem(menuItem);
        });

        List<String> comments = new ArrayList<>();
        comments.add("=".repeat(20) + " GUI CONTENT " + "=".repeat(20));
        comments.add("You can freely edit items in this section as you wish (add, remove, modify items).");
        comments.add("Get some tips in documentation: " + Placeholders.WIKI_MENU_URL);
        comments.add("The following values are available for button types:");
        this.handlers.forEach((clazz, handler) -> {
            comments.addAll(handler.getClicks().keySet().stream().map(e -> "- " + e.name()).sorted(String::compareTo).toList());
        });
        comments.add("=".repeat(50));
        this.cfg.setComments(this.itemSection, comments);
        this.cfg.saveChanges();
    }

    protected void loadAdditional() {

    }

    /*@Deprecated
    protected void write() {
        this.cfg.set("Settings.Title", this.getOptions().getTitle());
        this.cfg.set("Settings.Size", this.getOptions().getSize());
        this.cfg.set("Settings.Type", this.getOptions().getType().name());
        this.cfg.set("Settings.Auto_Refresh", this.getOptions().getAutoRefresh());

        AtomicInteger count = new AtomicInteger();
        this.getItems().forEach(menuItem -> {
            String name = StringUtil.lowerCaseUnderscore(ItemUtil.getItemName(menuItem.getItem()));

            this.writeItem(menuItem, this.itemSection + "." + name + "_" + count.incrementAndGet());
        });
    }*/

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

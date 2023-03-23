package su.nexmedia.engine.api.menu.impl;

import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.Colorizer;

public class MenuOptions {

    private String title;
    private int size;
    private InventoryType type;

    public MenuOptions(@NotNull String title, int size, @NotNull InventoryType type) {
        this.setTitle(title);
        this.setSize(size);
        this.setType(type);
    }

    public MenuOptions(@NotNull MenuOptions options) {
        this(options.getTitle(), options.getSize(), options.getType());
    }

    @NotNull
    public Inventory createInventory() {
        String title = this.getTitle();
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
        if (this.getType() == InventoryType.CHEST) {
            return Bukkit.getServer().createInventory(null, this.getSize(), title);
        }
        else {
            return Bukkit.getServer().createInventory(null, this.getType(), title);
        }
        //}
    }

    @NotNull
    public String getTitle() {
        return title;
    }

    public void setTitle(@NotNull String title) {
        this.title = Colorizer.apply(title);
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        if (size <= 0 || size % 9 != 0) size = 27;
        this.size = size;
    }

    @NotNull
    public InventoryType getType() {
        return type;
    }

    public void setType(@NotNull InventoryType type) {
        this.type = type;
    }
}

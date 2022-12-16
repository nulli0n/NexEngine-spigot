package su.nexmedia.engine.api.menu;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.actions.ActionManipulator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Deprecated
public class MenuItemDisplay {

    protected final String       id;
    protected final int          priority;
    protected final ItemStack    item;
    protected final List<String> conditions;

    public MenuItemDisplay(@NotNull ItemStack item) {
        this(UUID.randomUUID().toString(), 0, item, new ArrayList<>());
    }

    public MenuItemDisplay(
        @NotNull String id, int priority, @NotNull ItemStack item, @NotNull List<String> conditions) {
        this.id = id;
        this.priority = priority;
        this.item = new ItemStack(item);
        this.conditions = new ArrayList<>(conditions);
    }

    @NotNull
    public String getId() {
        return id;
    }

    public int getPriority() {
        return priority;
    }

    @NotNull
    public ItemStack getItem() {
        return new ItemStack(this.item);
    }

    @NotNull
    public List<String> getConditions() {
        return new ArrayList<>(this.conditions);
    }

    public boolean isAvailable(@NotNull Player player) {
        return ActionManipulator.processConditions(player, this.getConditions());
    }
}

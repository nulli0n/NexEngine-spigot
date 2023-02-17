package su.nexmedia.engine.api.menu;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.type.ClickType;

import java.util.*;
import java.util.function.Predicate;

public class MenuItem {

    protected final String  id;
    protected final Enum<?> type;

    protected int       priority;
    protected ItemStack item;
    protected int[]     slots;
    protected MenuItemVisibility visibility;
    protected Map<UUID, MenuItemVisibility> personalVisibility;
    protected Predicate<Player> visibilityPolicy;

    protected MenuClick                    clickHandler;
    protected Map<ClickType, List<String>> clickCommands;

    public MenuItem(@NotNull ItemStack item) {
        this(item, new int[0]);
    }

    public MenuItem(@NotNull ItemStack item, int... slots) {
        this(item, null, slots);
    }

    public MenuItem(@NotNull ItemStack item, @Nullable Enum<?> type, int... slots) {
        this(UUID.randomUUID().toString(), item, type, slots);
    }

    public MenuItem(@NotNull String id, @NotNull ItemStack item, int... slots) {
        this(id, item, null, slots);
    }

    public MenuItem(@NotNull String id, @NotNull ItemStack item, @Nullable Enum<?> type, int... slots) {
        this(id, type, slots, 0, item, new HashMap<>());
    }

    public MenuItem(@NotNull MenuItem menuItem) {
        this(menuItem.getId(), menuItem.getType(), menuItem.getSlots(), menuItem.getPriority(), menuItem.getItem(), menuItem.getClickCommands());
    }

    public MenuItem(
        @NotNull String id, @Nullable Enum<?> type, int[] slots, int priority,
        @NotNull ItemStack item,
        @NotNull Map<ClickType, List<String>> clickCommands) {
        this.id = id.toLowerCase();
        this.type = type;
        this.setPriority(priority);
        this.setSlots(slots);
        this.setItem(item);
        this.personalVisibility = new HashMap<>();
        this.setVisibility(MenuItemVisibility.VISIBLE);
        this.setVisibilityPolicy(null);
        this.clickCommands = clickCommands;
    }

    @NotNull
    public String getId() {
        return id;
    }

    @Nullable
    public Enum<?> getType() {
        return type;
    }

    public int[] getSlots() {
        return slots;
    }

    public void setSlots(int... slots) {
        this.slots = slots;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    @NotNull
    public ItemStack getItem() {
        return new ItemStack(this.item);
    }

    public void setItem(@NotNull ItemStack item) {
        this.item = new ItemStack(item);
    }

    @NotNull
    public MenuItemVisibility getVisibility() {
        return visibility;
    }

    public void setVisibility(@NotNull MenuItemVisibility visibility) {
        this.visibility = visibility;
    }

    @NotNull
    public Map<UUID, MenuItemVisibility> getPersonalVisibility() {
        return personalVisibility;
    }

    @Nullable
    public Predicate<Player> getVisibilityPolicy() {
        return visibilityPolicy;
    }

    public void setVisibilityPolicy(@Nullable Predicate<Player> visibilityPolicy) {
        this.visibilityPolicy = visibilityPolicy;
    }

    @NotNull
    public MenuItemVisibility getPersonalVisibility(@NotNull Player player) {
        return this.getPersonalVisibility().getOrDefault(player.getUniqueId(), this.getVisibility());
    }

    public void setPersonalVisibility(@NotNull Player player, @NotNull MenuItemVisibility visibility) {
        this.getPersonalVisibility().put(player.getUniqueId(), visibility);
    }

    public void hideFrom(@NotNull Player player) {
        this.setPersonalVisibility(player, MenuItemVisibility.HIDDEN);
    }

    public void showFor(@NotNull Player player) {
        this.setPersonalVisibility(player, MenuItemVisibility.VISIBLE);
    }

    public void resetVisibility(@NotNull Player player) {
        this.getPersonalVisibility().remove(player.getUniqueId());
    }

    public boolean isVisible(@NotNull Player player) {
        MenuItemVisibility personal = this.getPersonalVisibility(player);
        MenuItemVisibility global = this.getVisibility();
        if (global == MenuItemVisibility.HIDDEN && personal == MenuItemVisibility.VISIBLE) {
            return true;
        }
        else if (global == MenuItemVisibility.VISIBLE && personal == MenuItemVisibility.HIDDEN) {
            return false;
        }

        Predicate<Player> policy = this.getVisibilityPolicy();
        return policy == null || policy.test(player);
    }

    @Nullable
    public MenuClick getClickHandler() {
        return clickHandler;
    }

    public void setClickHandler(@Nullable MenuClick clickHandler) {
        this.clickHandler = clickHandler;
    }

    @NotNull
    public Map<ClickType, List<String>> getClickCommands() {
        return clickCommands;
    }

    @NotNull
    public List<String> getClickCommands(@NotNull ClickType clickType) {
        return this.getClickCommands().getOrDefault(clickType, Collections.emptyList());
    }
}

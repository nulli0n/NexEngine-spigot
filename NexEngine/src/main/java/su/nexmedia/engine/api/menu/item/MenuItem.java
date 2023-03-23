package su.nexmedia.engine.api.menu.item;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.api.menu.click.ItemClick;

public class MenuItem {

    //private String id;
    protected Enum<?> type;
    protected ItemStack item;
    protected int priority;
    protected int[] slots;

    protected ItemOptions options;
    protected ItemClick   click;

    public MenuItem(@NotNull ItemStack item) {
        this(MenuItemType.NONE, item);
    }

    public MenuItem(@NotNull ItemStack item, int... slots) {
        this(MenuItemType.NONE, item, 0, slots);
    }

    public MenuItem(@NotNull ItemStack item, int priority, int... slots) {
        this(MenuItemType.NONE, item, priority, slots);
    }

    public MenuItem( @NotNull ItemStack item, int priority, @NotNull ItemOptions options, int... slots) {
        this(MenuItemType.NONE, item, priority, options, slots);
    }


    public MenuItem(@NotNull Enum<?> type, @NotNull ItemStack item) {
        this(type, item, 0);
    }

    public MenuItem(@NotNull Enum<?> type, @NotNull ItemStack item, int priority) {
        this(type, item, priority, new int[]{});
    }

    public MenuItem(@NotNull Enum<?> type, @NotNull ItemStack item, int priority, int... slots) {
        this(type, item, priority, new ItemOptions(), slots);
    }

    public MenuItem(@NotNull Enum<?> type, @NotNull ItemStack item, int priority, @NotNull ItemOptions options, int... slots) {
        this.setType(type);
        this.setItem(item);
        this.setPriority(priority);
        this.setSlots(slots);
        this.setOptions(options);
    }

    @NotNull
    public MenuItem copy() {
        return new MenuItem(this.getType(), this.getItem(), this.getPriority(), this.getOptions(), this.getSlots());
    }

    public void resetOptions() {
        this.setOptions(new ItemOptions());
    }

    @NotNull
    public Enum<?> getType() {
        return type;
    }

    public void setType(@Nullable Enum<?> type) {
        this.type = type == null ? MenuItemType.NONE : type;
    }

    @NotNull
    public ItemStack getItem() {
        return new ItemStack(this.item);
    }

    public void setItem(@NotNull ItemStack item) {
        this.item = new ItemStack(item);
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int[] getSlots() {
        return slots;
    }

    public void setSlots(int... slots) {
        this.slots = slots;
    }

    @NotNull
    public ItemOptions getOptions() {
        return options;
    }

    public void setOptions(@NotNull ItemOptions options) {
        this.options = options;
    }

    @Nullable
    public ItemClick getClick() {
        return click;
    }

    @NotNull
    public MenuItem setClick(@Nullable ItemClick click) {
        this.click = click;
        return this;
    }
}

package su.nexmedia.engine.api.menu;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.actions.ActionManipulator;
import su.nexmedia.engine.api.type.ClickType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class AbstractMenuItem implements IMenuItem {

    protected final String  id;
    protected final Enum<?> type;
    protected       int[]   slots;

    @Deprecated protected Map<String, MenuItemDisplay> displayMap;

    @Deprecated protected int      animationTickInterval;
    @Deprecated protected String[] animationFrames;
    @Deprecated protected boolean  animationIgnoreUnavailableFrames;
    @Deprecated protected boolean  animationRandomOrder;
    @Deprecated protected int      animationFrameCurrent;

    protected IMenuClick                        click;
    protected Map<ClickType, ActionManipulator> customClicks;

    public AbstractMenuItem(@NotNull ItemStack item) {
        this(item, new int[0]);
    }

    public AbstractMenuItem(@NotNull ItemStack item, int[] slots) {
        this(item, null, slots);
    }

    public AbstractMenuItem(@NotNull ItemStack item, @Nullable Enum<?> type, int[] slots) {
        this(UUID.randomUUID().toString(), item, type, slots);
    }

    public AbstractMenuItem(@NotNull String id, @NotNull ItemStack item, int[] slots) {
        this(id, item, null, slots);
    }

    public AbstractMenuItem(@NotNull String id, @NotNull ItemStack item, @Nullable Enum<?> type, int[] slots) {
        this(id, type, slots, new HashMap<>(), new HashMap<>());

        MenuItemDisplay itemDisplay = new MenuItemDisplay(item);
        this.getDisplayMap().put(itemDisplay.getId(), itemDisplay);
    }

    public AbstractMenuItem(
        @NotNull String id, @Nullable Enum<?> type, int[] slots,
        @NotNull Map<String, MenuItemDisplay> displayMap,
        @NotNull Map<ClickType, ActionManipulator> customClicks) {
        this.id = id.toLowerCase();
        this.type = type;
        this.setSlots(slots);

        this.displayMap = displayMap;
        this.customClicks = customClicks;
    }

    @Deprecated
    public AbstractMenuItem(
        @NotNull String id, @Nullable Enum<?> type, int[] slots,
        @NotNull Map<String, MenuItemDisplay> displayMap,
        @NotNull Map<ClickType, ActionManipulator> customClicks,

        int animationTickInterval, String[] animationFrames, boolean animationIgnoreUnavailableFrames,
        boolean animationRandomOrder) {
        this.id = id.toLowerCase();
        this.type = type;
        this.setSlots(slots);

        this.displayMap = displayMap;
        this.customClicks = customClicks;

        this.animationTickInterval = animationTickInterval;
        this.animationFrames = animationFrames;
        this.animationIgnoreUnavailableFrames = animationIgnoreUnavailableFrames;
        this.animationRandomOrder = animationRandomOrder;
        this.animationFrameCurrent = 0;
    }

    @NotNull
    @Override
    public String getId() {
        return id;
    }

    @Nullable
    @Override
    public Enum<?> getType() {
        return type;
    }

    @Override
    public int[] getSlots() {
        return slots;
    }

    @Override
    public void setSlots(int[] slots) {
        this.slots = slots;
    }

    @NotNull
    @Override
    @Deprecated
    public Map<String, MenuItemDisplay> getDisplayMap() {
        return displayMap;
    }

    @Override
    @Deprecated
    public @NotNull Map<ClickType, ActionManipulator> getClickCustomActions() {
        return customClicks;
    }

    @Override
    @Deprecated
    public int getAnimationTickInterval() {
        return animationTickInterval;
    }

    @NotNull
    @Override
    @Deprecated
    public String[] getAnimationFrames() {
        return animationFrames;
    }

    @Override
    @Deprecated
    public boolean isAnimationIgnoreUnvailableFrames() {
        return this.animationIgnoreUnavailableFrames;
    }

    @Override
    @Deprecated
    public boolean isAnimationRandomOrder() {
        return this.animationRandomOrder;
    }

    @Override
    @Deprecated
    public int getAnimationFrameCurrent() {
        return animationFrameCurrent;
    }

    @Deprecated
    public void setAnimationFrameCurrent(int frame) {
        if (frame >= this.getAnimationFrames().length) frame = 0;
        this.animationFrameCurrent = frame;
    }

    @Nullable
    @Override
    public IMenuClick getClick() {
        return click;
    }

    @Override
    public void setClick(@Nullable IMenuClick click) {
        this.click = click;
    }
}

package su.nexmedia.engine.api.menu;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.type.ClickType;
import su.nexmedia.engine.actions.ActionManipulator;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class AbstractMenuItem implements IMenuItem {

    protected final String  id;
    protected final Enum<?> type;
    protected       int[]   slots;

    protected Map<String, MenuItemDisplay> displayMap;

    protected int      animationTickInterval;
    protected String[] animationFrames;
    protected boolean  animationIgnoreUnavailableFrames;
    protected boolean  animationRandomOrder;
    protected int      animationFrameCurrent;

    protected IMenuClick                        click;
    protected Map<ClickType, ActionManipulator> customClicks;

    public AbstractMenuItem(@NotNull ItemStack item) {
        this(item, new int[0]);
    }

    public AbstractMenuItem(@NotNull ItemStack item, int[] slots) {
        this(UUID.randomUUID().toString(), null, slots,
            new HashMap<>(),
            new HashMap<>(),

            0, new String[0], true, false);

        MenuItemDisplay itemDisplay = new MenuItemDisplay(item);
        this.getDisplayMap().put(itemDisplay.getId(), itemDisplay);
    }

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
    public Map<String, MenuItemDisplay> getDisplayMap() {
        return displayMap;
    }

    @Override
    public @NotNull Map<ClickType, ActionManipulator> getClickCustomActions() {
        return customClicks;
    }

    @Override
    public int getAnimationTickInterval() {
        return animationTickInterval;
    }

    @NotNull
    @Override
    public String[] getAnimationFrames() {
        return animationFrames;
    }

    @Override
    public boolean isAnimationIgnoreUnvailableFrames() {
        return this.animationIgnoreUnavailableFrames;
    }

    @Override
    public boolean isAnimationRandomOrder() {
        return this.animationRandomOrder;
    }

    @Override
    public int getAnimationFrameCurrent() {
        return animationFrameCurrent;
    }

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

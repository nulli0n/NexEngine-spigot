package su.nexmedia.engine.api.menu;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.actions.ActionManipulator;
import su.nexmedia.engine.api.type.ClickType;
import su.nexmedia.engine.utils.Placeholders;

import java.util.Map;
import java.util.Optional;

public interface IMenuItem {

    @NotNull String getId();

    @Nullable Enum<?> getType();

    int[] getSlots();

    void setSlots(int... slots);

    @Nullable IMenuClick getClick();

    void setClick(@Nullable IMenuClick click);

    @Deprecated
    @NotNull Map<String, MenuItemDisplay> getDisplayMap();


    @Deprecated
    default boolean isAnimationEnabled() {
        return this.getAnimationTickInterval() > 0 && this.getAnimationFrames().length > 1;
    }

    @Deprecated
    int getAnimationTickInterval();

    @Deprecated
    @NotNull String[] getAnimationFrames();

    @Deprecated
    boolean isAnimationIgnoreUnvailableFrames();

    @Deprecated
    boolean isAnimationRandomOrder();

    @Deprecated
    int getAnimationFrameCurrent();

    @Deprecated
    void setAnimationFrameCurrent(int animationFrameCurrent);

    @Deprecated
    @NotNull Map<ClickType, ActionManipulator> getClickCustomActions();

    @Nullable
    @Deprecated
    default ActionManipulator getClickCustomAction(@NotNull ClickType clickType) {
        return this.getClickCustomActions().get(clickType);
    }

    @Nullable
    @Deprecated
    default MenuItemDisplay getDisplay(@NotNull String id) {
        return this.getDisplayMap().get(id.toLowerCase());
    }

    @Nullable
    @Deprecated
    default MenuItemDisplay getDisplay(@NotNull Player player) {
        Optional<MenuItemDisplay> opt = this.getDisplayMap().values().stream()
            .filter(d -> d.isAvailable(player))
            .min((dis1, dis2) -> dis2.getPriority() - dis1.getPriority());

        return opt.orElse(this.getDisplay(Placeholders.DEFAULT));
    }


}

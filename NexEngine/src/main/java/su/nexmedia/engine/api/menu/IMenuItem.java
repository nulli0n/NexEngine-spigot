package su.nexmedia.engine.api.menu;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.type.ClickType;
import su.nexmedia.engine.utils.Constants;
import su.nexmedia.engine.actions.ActionManipulator;

import java.util.Map;
import java.util.Optional;

public interface IMenuItem {

    @NotNull String getId();

    @Nullable Enum<?> getType();

    int[] getSlots();

    void setSlots(int... slots);

    @Nullable IMenuClick getClick();

    void setClick(@Nullable IMenuClick click);

    @NotNull Map<String, MenuItemDisplay> getDisplayMap();


    default boolean isAnimationEnabled() {
        return this.getAnimationTickInterval() > 0 && this.getAnimationFrames().length > 1;
    }

    int getAnimationTickInterval();

    @NotNull String[] getAnimationFrames();

    boolean isAnimationIgnoreUnvailableFrames();

    boolean isAnimationRandomOrder();

    int getAnimationFrameCurrent();

    void setAnimationFrameCurrent(int animationFrameCurrent);


    @NotNull Map<ClickType, ActionManipulator> getClickCustomActions();

    @Nullable
    default ActionManipulator getClickCustomAction(@NotNull ClickType clickType) {
        return this.getClickCustomActions().get(clickType);
    }

    @Nullable
    default MenuItemDisplay getDisplay(@NotNull String id) {
        return this.getDisplayMap().get(id.toLowerCase());
    }

    @Nullable
    default MenuItemDisplay getDisplay(@NotNull Player player) {
        Optional<MenuItemDisplay> opt = this.getDisplayMap().values().stream()
            .filter(d -> d.isAvailable(player))
            .min((dis1, dis2) -> dis2.getPriority() - dis1.getPriority());

        return opt.orElse(this.getDisplay(Constants.DEFAULT));
    }


}

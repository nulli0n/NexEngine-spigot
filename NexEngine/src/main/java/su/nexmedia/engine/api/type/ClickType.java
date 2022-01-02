package su.nexmedia.engine.api.type;

import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

public enum ClickType {

    LEFT, RIGHT, MIDDLE, SHIFT_LEFT, SHIFT_RIGHT,
    ;

    @NotNull
    public static ClickType from(@NotNull InventoryClickEvent e) {
        if (e.isShiftClick()) {
            if (e.isLeftClick()) {
                return SHIFT_LEFT;
            }
            return SHIFT_RIGHT;
        }
        if (e.getClick() == org.bukkit.event.inventory.ClickType.MIDDLE) {
            return MIDDLE;
        }
        if (e.isRightClick()) {
            return RIGHT;
        }
        return LEFT;
    }

    @NotNull
    public static ClickType from(@NotNull Action action, boolean shift) {
        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            return shift ? ClickType.SHIFT_RIGHT : RIGHT;
        }
        return shift ? ClickType.SHIFT_LEFT : LEFT;
    }
}

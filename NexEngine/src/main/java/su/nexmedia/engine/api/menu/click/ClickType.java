package su.nexmedia.engine.api.menu.click;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

public enum ClickType {

    LEFT, RIGHT, SHIFT_LEFT, SHIFT_RIGHT,
    DROP_KEY, SWAP_KEY,
    NUMBER_1,
    NUMBER_2,
    NUMBER_3,
    NUMBER_4,
    NUMBER_5,
    NUMBER_6,
    NUMBER_7,
    NUMBER_8,
    NUMBER_9,
    ;

    @NotNull
    public static ClickType from(@NotNull InventoryClickEvent e) {
        if (e.getClick() == org.bukkit.event.inventory.ClickType.DROP) return DROP_KEY;
        if (e.getClick() == org.bukkit.event.inventory.ClickType.SWAP_OFFHAND) return SWAP_KEY;
        if (e.getHotbarButton() >= 0) {
            return switch (e.getHotbarButton()) {
                case 0 -> NUMBER_1;
                case 1 -> NUMBER_2;
                case 2 -> NUMBER_3;
                case 3 -> NUMBER_4;
                case 4 -> NUMBER_5;
                case 5 -> NUMBER_6;
                case 6 -> NUMBER_7;
                case 7 -> NUMBER_8;
                case 8 -> NUMBER_9;
                default -> throw new IllegalStateException("Unexpected value: " + e.getHotbarButton());
            };
        }

        if (e.isShiftClick()) {
            return e.isLeftClick() ? SHIFT_LEFT : SHIFT_RIGHT;
        }
        return e.isLeftClick() ? LEFT : RIGHT;
    }
}

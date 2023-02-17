package su.nexmedia.engine.api.menu;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class WeakMenuItem extends MenuItem {

    protected Predicate<Player> weakPolicy;

    public WeakMenuItem(@NotNull Player holder, @NotNull ItemStack item, int... slots) {
        super(item, slots);
        this.setVisibility(MenuItemVisibility.HIDDEN);
        this.showFor(holder);
        this.setWeakPolicy(e -> e.getUniqueId().equals(holder.getUniqueId()));
    }

    @NotNull
    public Predicate<Player> getWeakPolicy() {
        return weakPolicy;
    }

    public void setWeakPolicy(@NotNull Predicate<Player> weakPolicy) {
        this.weakPolicy = weakPolicy;
    }
}

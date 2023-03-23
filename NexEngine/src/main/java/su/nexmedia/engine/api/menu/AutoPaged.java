package su.nexmedia.engine.api.menu;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.menu.click.ItemClick;
import su.nexmedia.engine.api.menu.impl.MenuViewer;
import su.nexmedia.engine.api.menu.item.ItemOptions;
import su.nexmedia.engine.api.menu.item.MenuItem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public interface AutoPaged<I> {

    int[] getObjectSlots();

    @NotNull List<I> getObjects(@NotNull Player player);

    @NotNull ItemStack getObjectStack(@NotNull Player player, @NotNull I object);

    @NotNull ItemClick getObjectClick(@NotNull I object);

    @NotNull Comparator<I> getObjectSorter();

    @NotNull
    default List<MenuItem> getItemsForPage(@NotNull MenuViewer viewer) {
        Player player = viewer.getPlayer();
        List<MenuItem> items = new ArrayList<>();
        List<I> origin = this.getObjects(player);

        int limit = this.getObjectSlots().length;
        int skip = (viewer.getPage() - 1) * limit;
        int pages = (int) Math.ceil((double) origin.size() / (double) limit);
        viewer.setPages(pages);

        List<I> list = new ArrayList<>(origin.stream().skip(skip).limit(limit).sorted(this.getObjectSorter()).toList());
        int count = 0;
        for (I object : list) {
            ItemStack item = this.getObjectStack(player, object);
            ItemOptions options = ItemOptions.personalWeak(player);
            MenuItem menuItem = new MenuItem(item, 100, options, this.getObjectSlots()[count++]);
            menuItem.setClick(this.getObjectClick(object));
            items.add(menuItem);
        }

        return items;
    }
}

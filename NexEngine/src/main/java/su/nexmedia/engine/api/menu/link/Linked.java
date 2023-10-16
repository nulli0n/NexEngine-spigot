package su.nexmedia.engine.api.menu.link;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface Linked<T> {

    @NotNull ViewLink<T> getLink();

    default boolean open(@NotNull Player player, @NotNull T obj, int page) {
        this.getLink().set(player, obj);

        if (!this.open(player, page)) {
            this.getLink().clear(player);
            return false;
        }

        return true;
    }

    boolean open(@NotNull Player player, int page);
}

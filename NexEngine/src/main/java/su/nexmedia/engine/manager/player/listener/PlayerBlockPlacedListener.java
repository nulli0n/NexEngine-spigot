package su.nexmedia.engine.manager.player.listener;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.manager.player.PlayerBlockTracker;

import java.util.Set;
import java.util.function.Predicate;

@Deprecated // TODO Rename and make static and refactor
public class PlayerBlockPlacedListener {

    public static final Set<Block>            USER_PLACED    = PlayerBlockTracker.BLOCK_LIST;
    public static final Set<Predicate<Block>> BLOCK_FILTERS = PlayerBlockTracker.BLOCK_FILTERS;

    public static boolean isUserPlaced(@NotNull Block block) {
        return PlayerBlockTracker.isTracked(block);
    }

    public static boolean isHighPlant(@NotNull Material material) {
        return PlayerBlockTracker.isHighPlant(material);
    }

    public static void addUserBlock(@NotNull Block block) {
        PlayerBlockTracker.addTracked(block);
    }
}

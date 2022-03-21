package su.nexmedia.engine.manager.player;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexEngine;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.manager.AbstractListener;
import su.nexmedia.engine.utils.LocationUtil;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class PlayerBlockTracker extends AbstractListener<NexEngine> {

    public static final  Set<Block>            BLOCK_LIST    = new HashSet<>();
    public static final  Set<Predicate<Block>> BLOCK_FILTERS = new HashSet<>();
    private static final Set<Material>         BLOCK_TALL    = new HashSet<>(Arrays.asList(
        Material.SUGAR_CANE, Material.BAMBOO));

    private static final String META_TRACK_FALLING_BLOCK = "engineBlockTracker_Falling";

    private static PlayerBlockTracker instance;
    private static JYML configBlocks;

    private PlayerBlockTracker(@NotNull NexEngine engine) {
        super(engine);
    }

    public static PlayerBlockTracker getInstance() {
        return instance;
    }

    public static void initialize() {
        if (instance == null) {
            configBlocks = new JYML(NexEngine.get().getDataFolder().getAbsolutePath(), "user_placed_blocks");
            instance = new PlayerBlockTracker(NexEngine.get());
            instance.plugin.info("Enabling player block place tracker...");
            instance.plugin.runTask(c -> {
                instance.plugin.info("Loading player placed blocks in async mode...");
                BLOCK_LIST.addAll(LocationUtil.deserialize(configBlocks.getStringList("Locations"))
                    .stream().map(Location::getBlock).toList());
                instance.registerListeners();
            }, true);
        }
    }

    public static void shutdown() {
        if (instance != null) {
            instance.plugin.info("Disabling player block place tracker...");
            instance.unregisterListeners();
            BLOCK_LIST.removeIf(Block::isEmpty);
            configBlocks.remove("locations");
            configBlocks.set("Locations", LocationUtil.serialize(BLOCK_LIST.stream().map(Block::getLocation).toList()));
            configBlocks.save();
            instance = null;
        }
    }

    public static boolean isTracked(@NotNull Block block) {
        return BLOCK_LIST.contains(block);
    }

    public static boolean isHighPlant(@NotNull Material material) {
        return BLOCK_TALL.contains(material);
    }

    public static void addTracked(@NotNull Block block) {
        if (BLOCK_FILTERS.stream().anyMatch(filter -> filter.test(block))) {
            addTrackedForce(block);
        }
    }

    public static void addTrackedForce(@NotNull Block block) {
        BLOCK_LIST.add(block);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onGlitchBlockPlace(BlockPlaceEvent e) {
        Block block = e.getBlock();
        Material type = block.getType();

        boolean isPlant = block.getBlockData() instanceof Ageable;
        boolean isCane = isHighPlant(type);

        if (!isPlant || isCane) {
            addTracked(block);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onGlitchBlockGrow(BlockGrowEvent e) {
        Block block = e.getBlock();
        Material type = block.getType();

        if (isHighPlant(type)) {
            Block down = block.getRelative(BlockFace.DOWN);
            if (down.getType() != type) {
                BLOCK_LIST.remove(block);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onGlitchBlockPistonExtend(BlockPistonExtendEvent e) {
        BlockFace direction = e.getDirection();
        this.onGlitchBlockPistonEvent(direction, e.getBlocks());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onGlitchBlockPistonRetract(BlockPistonRetractEvent e) {
        BlockFace direction = e.getDirection();
        this.onGlitchBlockPistonEvent(direction, e.getBlocks());
    }

    private void onGlitchBlockPistonEvent(@NotNull BlockFace direction, @NotNull List<Block> blocks) {
        Set<Block> userBlocks = blocks.stream().filter(PlayerBlockTracker::isTracked).collect(Collectors.toSet());
        BLOCK_LIST.removeAll(userBlocks);
        userBlocks.stream().map(b -> b.getRelative(direction)).toList().forEach(PlayerBlockTracker::addTrackedForce);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onGlitchBlockFallingSpawn(EntitySpawnEvent e) {
        Entity entity = e.getEntity();
        if (!(entity instanceof FallingBlock)) return;

        Block block = entity.getLocation().getBlock();
        if (!isTracked(block)) return;

        entity.setMetadata(META_TRACK_FALLING_BLOCK, new FixedMetadataValue(plugin, true));
        BLOCK_LIST.remove(block);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onGlitchBlockFallingLand(EntityChangeBlockEvent e) {
        Entity entity = e.getEntity();
        if (!entity.hasMetadata(META_TRACK_FALLING_BLOCK)) return;

        addTrackedForce(e.getBlock());
    }
}

package su.nexmedia.engine.manager.player.listener;

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

public class PlayerBlockPlacedListener extends AbstractListener<NexEngine> {

    public static final  Set<Block>    USER_PLACED               = new HashSet<>();
    public static final  Set<Predicate<Block>> BLOCK_FILTERS = new HashSet<>();
    private static final String        META_GLITCH_FALLING_BLOCK = "USER_PLACED_FALLING_BLOCK";
    private static final Set<Material> HIGH_PLANTS               = new HashSet<>(Arrays.asList(
        Material.SUGAR_CANE, Material.BAMBOO));

    private final JYML configBlocks;

    public PlayerBlockPlacedListener(@NotNull NexEngine engine) {
        super(engine);
        this.configBlocks = new JYML(engine.getDataFolder().getAbsolutePath(), "user_placed_blocks");

        plugin.runTask(c -> {
            plugin.info("Loading user placed blocks in async mode...");
            USER_PLACED.addAll(LocationUtil.deserialize(configBlocks.getStringList("locations"))
                .stream().map(Location::getBlock).toList());
        }, true);
    }

    public static boolean isUserPlaced(@NotNull Block block) {
        return USER_PLACED.contains(block);
    }

    public static boolean isHighPlant(@NotNull Material material) {
        return HIGH_PLANTS.contains(material);
    }

    public static void addUserBlock(@NotNull Block block) {
        if (!BLOCK_FILTERS.isEmpty() && BLOCK_FILTERS.stream().anyMatch(p -> p.test(block))) {
            USER_PLACED.add(block);
        }
    }

    @Override
    public void unregisterListeners() {
        USER_PLACED.removeIf(Block::isEmpty);
        this.configBlocks.set("locations", LocationUtil.serialize(USER_PLACED.stream().map(Block::getLocation).toList()));
        this.configBlocks.save();
        super.unregisterListeners();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onGlitchBlockPlace(BlockPlaceEvent e) {
        Block block = e.getBlock();
        Material type = block.getType();

        boolean isPlant = block.getBlockData() instanceof Ageable;
        boolean isCane = isHighPlant(type);

        if (!isPlant || isCane) {
            addUserBlock(block);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onGlitchBlockGrow(BlockGrowEvent e) {
        Block block = e.getBlock();
        Material type = block.getType();

        if (isHighPlant(type)) {
            Block down = block.getRelative(BlockFace.DOWN);
            if (down.getType() != type) {
                USER_PLACED.remove(block);
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
        Set<Block> userBlocks = blocks.stream().filter(PlayerBlockPlacedListener::isUserPlaced)
            .collect(Collectors.toSet());

        USER_PLACED.removeAll(userBlocks);
        userBlocks.stream().map(b -> b.getRelative(direction)).toList().forEach(PlayerBlockPlacedListener::addUserBlock);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onGlitchBlockFallingSpawn(EntitySpawnEvent e) {
        Entity entity = e.getEntity();
        if (!(entity instanceof FallingBlock)) return;

        Block block = entity.getLocation().getBlock();
        if (!isUserPlaced(block)) return;

        entity.setMetadata(META_GLITCH_FALLING_BLOCK, new FixedMetadataValue(plugin, true));
        USER_PLACED.remove(block);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onGlitchBlockFallingLand(EntityChangeBlockEvent e) {
        Entity entity = e.getEntity();
        if (!entity.hasMetadata(META_GLITCH_FALLING_BLOCK)) return;
        addUserBlock(e.getBlock());
    }
}

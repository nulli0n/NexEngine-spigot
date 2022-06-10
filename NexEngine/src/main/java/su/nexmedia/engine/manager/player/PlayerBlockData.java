package su.nexmedia.engine.manager.player;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexEngine;
import su.nexmedia.engine.api.data.AbstractDataHandler;
import su.nexmedia.engine.api.data.DataTypes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;

@Deprecated
public class PlayerBlockData extends AbstractDataHandler<NexEngine> {

    private final Function<ResultSet, Block> functionBlock;
    private final Function<ResultSet, String> functionWorld;

    private static PlayerBlockData instance;

    private static final String TABLE_NAME = "player_blocks";

    protected PlayerBlockData(@NotNull NexEngine plugin) throws SQLException {
        super(plugin, plugin.getDataFolder().getAbsolutePath(), "blocks.db");

        this.functionBlock = resultSet -> {
            try {
                String worldName = resultSet.getString("world");
                int x = resultSet.getInt("locX");
                int y = resultSet.getInt("locY");
                int z = resultSet.getInt("locZ");

                World world = plugin.getServer().getWorld(worldName);
                if (world == null) return null;

                return world.getBlockAt(x, y, z);
            }
            catch (SQLException e) {
                return null;
            }
        };

        this.functionWorld = resultSet -> {
            try {
                return resultSet.getString("world");
            }
            catch (SQLException e) {
                return null;
            }
        };
    }

    @NotNull
    public static PlayerBlockData getInstance() throws SQLException {
        if (instance == null) {
            instance = new PlayerBlockData(NexEngine.get());
        }
        return instance;
    }

    @Override
    protected void onLoad() {
        super.onLoad();

        if (!this.hasTable(TABLE_NAME)) {
            LinkedHashMap<String, String> map = new LinkedHashMap<>();
            map.put("locX", DataTypes.INTEGER.build(this.dataType));
            map.put("locY", DataTypes.INTEGER.build(this.dataType));
            map.put("locZ", DataTypes.INTEGER.build(this.dataType));
            map.put("world", DataTypes.STRING.build(this.dataType));
            this.createTable(TABLE_NAME, map);
        }

        List<String> worldsData = this.getDatas(TABLE_NAME, new HashMap<>(), this.functionWorld, -1);
        List<String> worldsHas = plugin.getServer().getWorlds().stream().map(WorldInfo::getName).toList();
        worldsData.removeAll(worldsHas);
        worldsData.forEach(worldInvalid -> {
            LinkedHashMap<String, String> map = new LinkedHashMap<>();
            map.put("world", worldInvalid);
            this.deleteData(TABLE_NAME, map);
        });
    }

    /*@Nullable
    public Block getBlock(@NotNull Location location) {
        long now = System.currentTimeMillis();
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("locX", String.valueOf((int) location.getX()));
        map.put("locY", String.valueOf((int) location.getY()));
        map.put("locZ", String.valueOf((int) location.getZ()));
        map.put("world", location.getWorld().getName());
        Block block = this.getData(TABLE_NAME, map, this.functionBlock);
        System.out.println("database lookup took: " + (System.currentTimeMillis() - now) + " ms");

        return block;
    }*/

    @NotNull
    public List<Block> getBlocks() {
        return this.getDatas(TABLE_NAME, new HashMap<>(), this.functionBlock, -1);
    }

    public void addBlock(@NotNull Block block) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("locX", String.valueOf(block.getX()));
        map.put("locY", String.valueOf(block.getY()));
        map.put("locZ", String.valueOf(block.getZ()));
        map.put("world", block.getWorld().getName());
        this.addData(TABLE_NAME, map);
    }

    public void removeBlock(@NotNull Block block) {
        this.removeBlock(block.getX(), block.getY(), block.getZ(), block.getWorld().getName());
    }

    public void removeBlock(int x, int y, int z, @NotNull String world) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put("locX", String.valueOf(x));
        map.put("locY", String.valueOf(y));
        map.put("locZ", String.valueOf(z));
        map.put("world", world);
        this.deleteData(TABLE_NAME, map);
    }
}

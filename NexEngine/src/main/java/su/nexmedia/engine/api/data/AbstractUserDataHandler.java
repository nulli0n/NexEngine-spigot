package su.nexmedia.engine.api.data;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.NexPlugin;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;

public abstract class AbstractUserDataHandler<P extends NexPlugin<P>, U extends AbstractUser<P>> extends AbstractDataHandler<P> {

    protected static final String COL_USER_UUID        = "uuid";
    protected static final String COL_USER_NAME         = "name";
    protected static final String COL_USER_DATE_CREATED = "dateCreated";
    protected static final String COL_USER_LAST_ONLINE  = "last_online";
    protected final String tableUsers;

    public AbstractUserDataHandler(@NotNull P plugin) throws SQLException {
        super(plugin);
        this.tableUsers = plugin.getNameRaw() + "_users";
    }

    protected AbstractUserDataHandler(@NotNull P plugin,
                                      @NotNull String host, @NotNull String base,
                                      @NotNull String login, @NotNull String password) throws SQLException {
        super(plugin, host, base, login, password);
        this.tableUsers = plugin.getNameRaw() + "_users";
    }

    protected AbstractUserDataHandler(@NotNull P plugin,
                                      @NotNull String filePath, @NotNull String fileName) throws SQLException {
        super(plugin, filePath);
        this.tableUsers = plugin.getNameRaw() + "_users";
    }

    @Override
    protected void onLoad() {
        super.onLoad();

        this.createUsersTable();
        this.plugin.runTask(c -> this.purge(), true);
    }

    @Override
    protected void onShutdown() {
        super.onShutdown();
    }

    private void createUsersTable() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put(COL_USER_UUID, DataTypes.CHAR.build(this.dataType, 36));
        map.put(COL_USER_NAME, DataTypes.STRING.build(this.dataType, 24));
        map.put(COL_USER_DATE_CREATED, DataTypes.LONG.build(this.dataType, 64));
        map.put(COL_USER_LAST_ONLINE, DataTypes.LONG.build(this.dataType, 64));
        this.getColumnsToCreate().forEach((col, type) -> {
            map.merge(col, type, (oldV, newV) -> newV);
        });
        this.createTable(this.tableUsers, map);

        this.addColumn(tableUsers, COL_USER_DATE_CREATED, DataTypes.LONG.build(this.dataType), String.valueOf(System.currentTimeMillis()));
        this.addColumn(tableUsers, COL_USER_LAST_ONLINE, DataTypes.LONG.build(this.dataType), String.valueOf(System.currentTimeMillis()));

        this.onTableCreate();
    }

    @NotNull
    protected abstract LinkedHashMap<String, String> getColumnsToCreate();

    @NotNull
    protected abstract LinkedHashMap<String, String> getColumnsToSave(@NotNull U user);

    @NotNull
    protected abstract Function<ResultSet, U> getFunctionToUser();

    protected void onTableCreate() {

    }

    public void purge() {
        if (!plugin.getConfigManager().dataPurgeEnabled) return;

        int count = 0;
        for (U user : this.getUsers()) {
            long lastOnline = user.getLastOnline();

            long diff = System.currentTimeMillis() - lastOnline;
            int days = (int) ((diff / (1000 * 60 * 60 * 24)) % 7);

            if (days >= plugin.getConfigManager().dataPurgeDays) {
                this.deleteUser(user.getUUID().toString());
                count++;
            }
        }
        plugin.info("[User Data] Purged " + count + " inactive users.");
    }

    @NotNull
    public List<@NotNull U> getUsers() {
        return this.getDatas(this.tableUsers, Collections.emptyMap(), this.getFunctionToUser(), -1);
    }

    @Nullable
    public U getUser(@NotNull Player player) {
        return this.getUser(player.getUniqueId());
    }

    @Nullable
    public U getUser(@NotNull UUID uuid) {
        return this.getUser(uuid.toString(), true);
    }

    @Nullable
    public final U getUser(@NotNull String nameOrId, boolean isId) {
        Map<String, String> whereMap = new HashMap<>();
        whereMap.put(isId ? COL_USER_UUID : COL_USER_NAME, nameOrId);
        return this.getData(this.tableUsers, whereMap, this.getFunctionToUser());
    }

    public boolean isUserExists(@NotNull String nameOrId, boolean isId) {
        Map<String, String> whereMap = new HashMap<>();
        whereMap.put(isId ? COL_USER_UUID : COL_USER_NAME, nameOrId);
        return this.hasData(this.tableUsers, whereMap);
    }

    public void saveUser(@NotNull U user) {
        LinkedHashMap<String, String> colMap = new LinkedHashMap<>();
        colMap.put(COL_USER_NAME, user.getOfflinePlayer().getName());
        colMap.put(COL_USER_DATE_CREATED, String.valueOf(user.getDateCreated()));
        colMap.put(COL_USER_LAST_ONLINE, String.valueOf(user.getLastOnline()));
        this.getColumnsToSave(user).forEach((col, val) -> {
            colMap.merge(col, val, (oldV, newV) -> newV);
        });

        Map<String, String> whereMap = new HashMap<>();
        whereMap.put(COL_USER_UUID, user.getUUID().toString());

        this.saveData(this.tableUsers, colMap, whereMap);
    }

    public void addUser(@NotNull U user) {
        if (this.isUserExists(user.getUUID().toString(), true)) return;

        LinkedHashMap<String, String> colMap = new LinkedHashMap<>();
        colMap.put(COL_USER_UUID, user.getUUID().toString());
        colMap.put(COL_USER_NAME, user.getName());
        colMap.put(COL_USER_DATE_CREATED, String.valueOf(user.getDateCreated()));
        colMap.put(COL_USER_LAST_ONLINE, String.valueOf(user.getLastOnline()));
        this.getColumnsToSave(user).forEach((col, val) -> {
            colMap.merge(col, val, (oldV, newV) -> newV);
        });
        this.addData(this.tableUsers, colMap);
    }

    public void deleteUser(@NotNull String uuid) {
        LinkedHashMap<String, String> whereMap = new LinkedHashMap<>();
        whereMap.put(COL_USER_UUID, uuid);

        DataQueries.executeDelete(this.getConnector(), this.tableUsers, whereMap);
    }
}

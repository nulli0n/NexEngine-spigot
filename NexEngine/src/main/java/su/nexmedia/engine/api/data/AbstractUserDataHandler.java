package su.nexmedia.engine.api.data;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.api.data.config.DataConfig;
import su.nexmedia.engine.utils.TimeUtil;

import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;

public abstract class AbstractUserDataHandler<P extends NexPlugin<P>, U extends AbstractUser<P>> extends AbstractDataHandler<P> {

    protected static final String COL_USER_UUID        = "uuid";
    protected static final String COL_USER_NAME         = "name";
    protected static final String COL_USER_DATE_CREATED = "dateCreated";
    protected static final String COL_USER_LAST_ONLINE  = "last_online";

    protected final UserDataHolder<P, U> dataHolder;
    protected final String tableUsers;

    protected AbstractUserDataHandler(@NotNull P plugin, @NotNull UserDataHolder<P, U> dataHolder) {
        this(plugin, dataHolder, new DataConfig(plugin.getConfig()));
    }

    protected AbstractUserDataHandler(@NotNull P plugin, @NotNull UserDataHolder<P, U> dataHolder, @NotNull DataConfig config) {
        super(plugin, config);
        this.dataHolder = dataHolder;
        this.tableUsers = this.getTablePrefix() + "_users";
    }

    @Override
    protected void onLoad() {
        super.onLoad();

        this.createUsersTable();
    }

    @Override
    protected void onShutdown() {
        super.onShutdown();
    }

    @Override
    public void onSave() {
        int off = 0;
        for (U userLoaded : this.dataHolder.getUserManager().getUsersLoaded()) {
            if (!userLoaded.isOnline()) {
                this.dataHolder.getUserManager().getUsersLoadedMap().remove(userLoaded.getId());
                off++;
            }
            this.saveUser(userLoaded);
        }

        int on = this.dataHolder.getUserManager().getUsersLoadedMap().size();
        this.plugin.info("Auto-save: Saved " + on + " online users | " + off + " offline users.");
    }

    @Override
    public void onPurge() {
        if (!this.hasTable(this.tableUsers)) return;

        LocalDateTime deadline = LocalDateTime.now().minusDays(this.getConfig().purgePeriod);
        long deadlineMs = TimeUtil.toEpochMillis(deadline);

        String sql = "DELETE FROM " + this.tableUsers + " WHERE " + COL_USER_LAST_ONLINE + " < " + deadlineMs;
        DataQueries.executeStatement(this.getConnector(), sql);
    }

    private void createUsersTable() {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put(COL_USER_UUID, DataTypes.CHAR.build(this.getDataType(), 36));
        map.put(COL_USER_NAME, DataTypes.STRING.build(this.getDataType(), 24));
        map.put(COL_USER_DATE_CREATED, DataTypes.LONG.build(this.getDataType(), 64));
        map.put(COL_USER_LAST_ONLINE, DataTypes.LONG.build(this.getDataType(), 64));
        this.getColumnsToCreate().forEach((col, type) -> {
            map.merge(col, type, (oldV, newV) -> newV);
        });
        this.createTable(this.tableUsers, map);

        this.addColumn(tableUsers, COL_USER_DATE_CREATED, DataTypes.LONG.build(this.getDataType()), String.valueOf(System.currentTimeMillis()));
        this.addColumn(tableUsers, COL_USER_LAST_ONLINE, DataTypes.LONG.build(this.getDataType()), String.valueOf(System.currentTimeMillis()));

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

    @NotNull
    public List<@NotNull U> getUsers() {
        return this.getDatas(this.tableUsers, Collections.emptyMap(), this.getFunctionToUser(), -1);
    }

    @Nullable
    public U getUser(@NotNull Player player) {
        return this.getUser(player.getUniqueId());
    }

    @Nullable
    public final U getUser(@NotNull String name) {
        Map<String, String> whereMap = new HashMap<>();
        whereMap.put(COL_USER_NAME, name);
        return this.getData(this.tableUsers, whereMap, this.getFunctionToUser());
    }

    @Nullable
    public final U getUser(@NotNull UUID uuid) {
        Map<String, String> whereMap = new HashMap<>();
        whereMap.put(COL_USER_UUID, uuid.toString());
        return this.getData(this.tableUsers, whereMap, this.getFunctionToUser());
    }

    public boolean isUserExists(@NotNull String name) {
        Map<String, String> whereMap = new HashMap<>();
        whereMap.put(COL_USER_NAME, name);
        return this.hasData(this.tableUsers, whereMap);
    }

    public boolean isUserExists(@NotNull UUID uuid) {
        Map<String, String> whereMap = new HashMap<>();
        whereMap.put(COL_USER_UUID, uuid.toString());
        return this.hasData(this.tableUsers, whereMap);
    }

    public void saveUser(@NotNull U user) {
        LinkedHashMap<String, String> colMap = new LinkedHashMap<>();
        colMap.put(COL_USER_NAME, user.getName());
        colMap.put(COL_USER_DATE_CREATED, String.valueOf(user.getDateCreated()));
        colMap.put(COL_USER_LAST_ONLINE, String.valueOf(user.getLastOnline()));
        this.getColumnsToSave(user).forEach((col, val) -> {
            colMap.merge(col, val, (oldV, newV) -> newV);
        });

        Map<String, String> whereMap = new HashMap<>();
        whereMap.put(COL_USER_UUID, user.getId().toString());

        this.saveData(this.tableUsers, colMap, whereMap);
    }

    public void addUser(@NotNull U user) {
        if (this.isUserExists(user.getId())) return;

        LinkedHashMap<String, String> colMap = new LinkedHashMap<>();
        colMap.put(COL_USER_UUID, user.getId().toString());
        colMap.put(COL_USER_NAME, user.getName());
        colMap.put(COL_USER_DATE_CREATED, String.valueOf(user.getDateCreated()));
        colMap.put(COL_USER_LAST_ONLINE, String.valueOf(user.getLastOnline()));
        this.getColumnsToSave(user).forEach((col, val) -> {
            colMap.merge(col, val, (oldV, newV) -> newV);
        });
        this.addData(this.tableUsers, colMap);
    }

    public void deleteUser(@NotNull UUID uuid) {
        Map<String, String> whereMap = new LinkedHashMap<>();
        whereMap.put(COL_USER_UUID, uuid.toString());

        DataQueries.executeDelete(this.getConnector(), this.tableUsers, whereMap);
    }
}

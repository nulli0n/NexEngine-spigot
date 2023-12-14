package su.nexmedia.engine.api.data;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.api.data.config.DataConfig;
import su.nexmedia.engine.api.data.sql.SQLColumn;
import su.nexmedia.engine.api.data.sql.SQLCondition;
import su.nexmedia.engine.api.data.sql.SQLQueries;
import su.nexmedia.engine.api.data.sql.SQLValue;
import su.nexmedia.engine.api.data.sql.column.ColumnType;
import su.nexmedia.engine.config.EngineConfig;
import su.nexmedia.engine.utils.TimeUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;

public abstract class AbstractUserDataHandler<P extends NexPlugin<P>, U extends AbstractUser<P>> extends AbstractDataHandler<P> {

    protected static final SQLColumn COLUMN_USER_ID = SQLColumn.of("uuid", ColumnType.STRING);
    protected static final SQLColumn COLUMN_USER_NAME         = SQLColumn.of("name", ColumnType.STRING);
    protected static final SQLColumn COLUMN_USER_DATE_CREATED = SQLColumn.of("dateCreated", ColumnType.LONG);
    protected static final SQLColumn COLUMN_USER_LAST_ONLINE  = SQLColumn.of("last_online", ColumnType.LONG);

    protected final UserDataHolder<P, U> dataHolder;
    protected final String tableUsers;

    protected final Set<UUID> existIDs;
    protected final Set<String> existNames;

    protected AbstractUserDataHandler(@NotNull P plugin, @NotNull UserDataHolder<P, U> dataHolder) {
        this(plugin, dataHolder, new DataConfig(plugin.getConfig()));
    }

    protected AbstractUserDataHandler(@NotNull P plugin, @NotNull UserDataHolder<P, U> dataHolder, @NotNull DataConfig config) {
        super(plugin, config);
        this.dataHolder = dataHolder;
        this.tableUsers = this.getTablePrefix() + "_users";
        this.existIDs = new HashSet<>();
        this.existNames = new HashSet<>();
    }

    @Override
    protected void onLoad() {
        super.onLoad();
        this.createUserTable();
        this.cacheNamesAndIds();
    }

    @Override
    protected void onShutdown() {
        super.onShutdown();
    }

    @Override
    public void onSave() {
        for (U user : this.dataHolder.getUserManager().getUsersLoaded()) {
            this.saveUser(user);
        }
    }

    @Override
    public void onPurge() {
        if (!SQLQueries.hasTable(this.getConnector(), this.tableUsers)) return;

        LocalDateTime deadline = LocalDateTime.now().minusDays(this.getConfig().purgePeriod);
        long deadlineMs = TimeUtil.toEpochMillis(deadline);

        this.delete(this.tableUsers, SQLCondition.smaller(COLUMN_USER_LAST_ONLINE.toValue(deadlineMs)));
    }

    protected void createUserTable() {
        List<SQLColumn> columns = new ArrayList<>();
        columns.add(COLUMN_USER_ID);
        columns.add(COLUMN_USER_NAME);
        columns.add(COLUMN_USER_DATE_CREATED);
        columns.add(COLUMN_USER_LAST_ONLINE);
        columns.addAll(this.getExtraColumns());

        this.createTable(this.tableUsers, columns);
    }

    public void cacheNamesAndIds() {
        if (!EngineConfig.USER_CACHE_NAME_AND_UUID.get()) return;

        Function<ResultSet, Void> function = resultSet -> {
            try {
                this.existIDs.add(UUID.fromString(resultSet.getString(COLUMN_USER_ID.getName())));
                this.existNames.add(resultSet.getString(COLUMN_USER_NAME.getName()).toLowerCase());
            }
            catch (SQLException exception) {
                exception.printStackTrace();
            }
            return null;
        };

        this.load(this.tableUsers, function, Arrays.asList(COLUMN_USER_ID, COLUMN_USER_NAME), Collections.emptyList(), -1);
    }

    @NotNull
    protected abstract List<SQLColumn> getExtraColumns();

    @NotNull
    protected List<SQLColumn> getReadColumns() {
        return Collections.emptyList();
    }

    @NotNull
    protected abstract List<SQLValue> getSaveColumns(@NotNull U user);

    @NotNull
    protected abstract Function<ResultSet, U> getFunctionToUser();

    @NotNull
    public List<U> getUsers() {
        return this.load(this.tableUsers, this.getFunctionToUser(), Collections.emptyList(), Collections.emptyList(), -1);
    }

    @Nullable
    public U getUser(@NotNull Player player) {
        return this.getUser(player.getUniqueId());
    }

    @Nullable
    public final U getUser(@NotNull String name) {
        return this.load(this.tableUsers, this.getFunctionToUser(), this.getReadColumns(),
            Collections.singletonList(SQLCondition.equal(COLUMN_USER_NAME.asLowerCase().toValue(name.toLowerCase())))
        ).orElse(null);
    }

    @Nullable
    public final U getUser(@NotNull UUID uuid) {
        return this.load(this.tableUsers, this.getFunctionToUser(), this.getReadColumns(),
            Collections.singletonList(SQLCondition.equal(COLUMN_USER_ID.toValue(uuid)))
        ).orElse(null);
    }

    public boolean isUserExists(@NotNull String name) {
        if (EngineConfig.USER_CACHE_NAME_AND_UUID.get()) {
            return this.existNames.contains(name.toLowerCase());
        }
        return this.contains(this.tableUsers, Collections.singletonList(COLUMN_USER_NAME), SQLCondition.equal(COLUMN_USER_NAME.asLowerCase().toValue(name.toLowerCase())));
    }

    public boolean isUserExists(@NotNull UUID uuid) {
        if (EngineConfig.USER_CACHE_NAME_AND_UUID.get()) {
            return this.existIDs.contains(uuid);
        }
        return this.contains(this.tableUsers, Collections.singletonList(COLUMN_USER_ID), SQLCondition.equal(COLUMN_USER_ID.toValue(uuid)));
    }

    public void saveUser(@NotNull U user) {
        List<SQLValue> values = new ArrayList<>();
        values.add(COLUMN_USER_NAME.toValue(user.getName()));
        values.add(COLUMN_USER_DATE_CREATED.toValue(user.getDateCreated()));
        values.add(COLUMN_USER_LAST_ONLINE.toValue(user.getLastOnline()));
        values.addAll(this.getSaveColumns(user));

        this.update(this.tableUsers, values, SQLCondition.equal(COLUMN_USER_ID.toValue(user.getId())));
    }

    public void addUser(@NotNull U user) {
        List<SQLValue> values = new ArrayList<>();
        values.add(COLUMN_USER_ID.toValue(user.getId()));
        values.add(COLUMN_USER_NAME.toValue(user.getName()));
        values.add(COLUMN_USER_DATE_CREATED.toValue(user.getDateCreated()));
        values.add(COLUMN_USER_LAST_ONLINE.toValue(user.getLastOnline()));
        values.addAll(this.getSaveColumns(user));

        this.insert(this.tableUsers, values);

        this.existIDs.add(user.getId());
        this.existNames.add(user.getName());
    }

    public void deleteUser(@NotNull UUID uuid) {
        this.delete(this.tableUsers, SQLCondition.equal(COLUMN_USER_ID.toValue(uuid)));

        this.existIDs.clear();
        this.existNames.clear();
        this.cacheNamesAndIds();
    }

    public void deleteUser(@NotNull U user) {
        this.delete(this.tableUsers, SQLCondition.equal(COLUMN_USER_ID.toValue(user.getId())));

        this.existIDs.remove(user.getId());
        this.existNames.remove(user.getName());
    }
}

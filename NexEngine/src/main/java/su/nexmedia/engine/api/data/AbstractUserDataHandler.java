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
import su.nexmedia.engine.utils.TimeUtil;

import java.sql.ResultSet;
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
        this.createUserTable();
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
        return this.contains(this.tableUsers, Collections.singletonList(COLUMN_USER_NAME), SQLCondition.equal(COLUMN_USER_NAME.toValue(name)));
    }

    public boolean isUserExists(@NotNull UUID uuid) {
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
        //if (this.isUserExists(user.getId())) return;

        List<SQLValue> values = new ArrayList<>();
        values.add(COLUMN_USER_ID.toValue(user.getId()));
        values.add(COLUMN_USER_NAME.toValue(user.getName()));
        values.add(COLUMN_USER_DATE_CREATED.toValue(user.getDateCreated()));
        values.add(COLUMN_USER_LAST_ONLINE.toValue(user.getLastOnline()));
        values.addAll(this.getSaveColumns(user));

        this.insert(this.tableUsers, values);
    }

    public void deleteUser(@NotNull UUID uuid) {
        this.delete(this.tableUsers, SQLCondition.equal(COLUMN_USER_ID.toValue(uuid)));
    }
}

package su.nexmedia.engine.api.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.api.data.config.DataConfig;
import su.nexmedia.engine.api.data.connection.AbstractDataConnector;
import su.nexmedia.engine.api.data.connection.ConnectorMySQL;
import su.nexmedia.engine.api.data.connection.ConnectorSQLite;
import su.nexmedia.engine.api.data.serialize.ItemStackSerializer;
import su.nexmedia.engine.api.data.sql.SQLColumn;
import su.nexmedia.engine.api.data.sql.SQLCondition;
import su.nexmedia.engine.api.data.sql.SQLQueries;
import su.nexmedia.engine.api.data.sql.SQLValue;
import su.nexmedia.engine.api.data.sql.executor.*;
import su.nexmedia.engine.api.data.task.DataSaveTask;
import su.nexmedia.engine.api.data.task.DataSynchronizationTask;
import su.nexmedia.engine.api.manager.AbstractManager;

import java.sql.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class AbstractDataHandler<P extends NexPlugin<P>> extends AbstractManager<P> {

    protected final DataConfig config;
    protected final AbstractDataConnector connector;
    protected Gson gson;

    private DataSynchronizationTask<P> synchronizationTask;
    private DataSaveTask<P> saveTask;

    protected AbstractDataHandler(@NotNull P plugin) {
        this(plugin, new DataConfig(plugin.getConfig()));
    }

    protected AbstractDataHandler(@NotNull P plugin, @NotNull DataConfig config) {
        super(plugin);

        this.config = config;
        if (this.getDataType() == StorageType.MYSQL) {
            this.connector = new ConnectorMySQL(plugin, config);
        }
        else {
            this.connector = new ConnectorSQLite(plugin, config);
        }
    }

    @Override
    protected void onLoad() {
        this.gson = this.registerAdapters(new GsonBuilder().setPrettyPrinting()).create();

        if (this.config != null) {
            if (this.getConfig().saveInterval > 0) {
                this.saveTask = new DataSaveTask<>(this);
                this.saveTask.start();
            }

            if (this.getConfig().syncInterval > 0) {
                if (this.getDataType() != StorageType.SQLITE) {
                    this.synchronizationTask = new DataSynchronizationTask<>(this);
                    this.synchronizationTask.start();
                    this.plugin.info("Enabled data synchronization with " + config.syncInterval + " seconds interval.");
                }
                else {
                    this.plugin.warn("Data synchronization is useless for local databases (SQLite). It will be disabled.");
                }
            }

            if (this.getConfig().purgeEnabled && this.getConfig().purgePeriod > 0) {
                this.onPurge();
            }
        }
    }

    @Override
    protected void onShutdown() {
        if (this.synchronizationTask != null) {
            this.synchronizationTask.stop();
            this.synchronizationTask = null;
        }
        if (this.saveTask != null) {
            this.saveTask.stop();
            this.saveTask = null;
        }
        this.onSynchronize();
        this.onSave();
        this.getConnector().close();
    }

    public abstract void onSynchronize();

    public abstract void onSave();

    public abstract void onPurge();

    @NotNull
    public DataConfig getConfig() {
        return this.config;
    }

    @NotNull
    public StorageType getDataType() {
        return this.getConfig().storageType;
    }

    @NotNull
    public String getTablePrefix() {
        if (this.getConfig().tablePrefix.isEmpty()) {
            return this.plugin.getName().replace(" ", "_").toLowerCase();
        }
        return this.getConfig().tablePrefix;
    }

    @NotNull
    public AbstractDataConnector getConnector() {
        return this.connector;
    }

    @NotNull
    protected GsonBuilder registerAdapters(@NotNull GsonBuilder builder) {
        // TODO Register for location?
        return builder.registerTypeAdapter(ItemStack.class, new ItemStackSerializer());
    }

    @NotNull
    protected final Connection getConnection() throws SQLException {
        return this.getConnector().getConnection();
    }

    public boolean createTable(@NotNull String table, @NotNull List<SQLColumn> columns) {
        return CreateTableExecutor.builder(table, this.getDataType()).columns(columns).execute(this.getConnector());
    }

    public boolean renameTable2(@NotNull String from, @NotNull String to) {
        return RenameTableExecutor.builder(from, this.getDataType()).renameTo(to).execute(this.getConnector());
    }

    public boolean addColumn(@NotNull String table, @NotNull SQLValue... columns) {
        return AlterTableExecutor.builder(table, this.getDataType()).addColumn(columns).execute(this.getConnector());
    }

    public boolean renameColumn(@NotNull String table, @NotNull SQLValue... columns) {
        return AlterTableExecutor.builder(table, this.getDataType()).renameColumn(columns).execute(this.getConnector());
    }

    public boolean dropColumn(@NotNull String table, @NotNull SQLColumn... columns) {
        return AlterTableExecutor.builder(table, this.getDataType()).dropColumn(columns).execute(this.getConnector());
    }

    public boolean hasColumn(@NotNull String table, @NotNull SQLColumn column) {
        return SQLQueries.hasColumn(this.getConnector(), table, column);
    }

    public boolean insert(@NotNull String table, @NotNull List<SQLValue> values) {
        return InsertQueryExecutor.builder(table).values(values).execute(this.getConnector());
    }

    public boolean update(@NotNull String table, @NotNull List<SQLValue> values, @NotNull SQLCondition... conditions) {
        return UpdateQueryExecutor.builder(table).values(values).where(conditions).execute(this.getConnector());
    }

    public boolean delete(@NotNull String table, @NotNull SQLCondition... conditions) {
        return DeleteQueryExecutor.builder(table).where(conditions).execute(this.getConnector());
    }

    public boolean contains(@NotNull String table, @NotNull SQLCondition... conditions) {
        return this.load(table, (resultSet -> true), Collections.emptyList(), Arrays.asList(conditions)).isPresent();
    }

    @NotNull
    public <T> Optional<T> load(@NotNull String table, @NotNull Function<ResultSet, T> function,
                                @NotNull List<SQLColumn> columns,
                                @NotNull List<SQLCondition> conditions) {
        List<T> list = this.load(table, function, columns, conditions, 1);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @NotNull
    public <T> List<T> load(@NotNull String table, @NotNull Function<ResultSet, T> dataFunction,
                            @NotNull List<SQLColumn> columns,
                            @NotNull List<SQLCondition> conditions,
                            int amount) {
        return SelectQueryExecutor.builder(table, dataFunction).columns(columns).where(conditions).execute(this.getConnector());
    }


    @Deprecated
    protected final void createTable(@NotNull String table, @NotNull LinkedHashMap<String, String> valMap) {
        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS " + table + "(");
        StringBuilder columns = new StringBuilder();

        LinkedHashMap<String, String> valuesMap = new LinkedHashMap<>();
        valuesMap.put("id", DataTypes.INTEGER.build(this.getDataType(), 11, true));
        valuesMap.putAll(valMap);

        // Adding all other columns with their types.
        String values = valuesMap.entrySet().stream()
            .map(entry -> "`" + entry.getKey() + "` " + entry.getValue())
            .collect(Collectors.joining(", "));
        columns.append(values);

        // Add columns to main sql builder and close the statement.
        sql.append(columns).append(");");

        DataQueries.executeStatement(this.getConnector(), sql.toString());
    }

    @Deprecated
    protected final void renameTable(@NotNull String from, @NotNull String to) {
        if (!this.hasTable(from)) return;

        StringBuilder sql = new StringBuilder();
        if (this.getDataType() == StorageType.MYSQL) {
            sql.append("RENAME TABLE ").append(from).append(" TO ").append(to).append(";");
        }
        else {
            sql.append("ALTER TABLE ").append(from).append(" RENAME TO ").append(to);
        }
        DataQueries.executeStatement(this.getConnector(), sql.toString());
    }

    @Deprecated
    protected final boolean hasTable(@NotNull String table) {
        return SQLQueries.hasTable(this.getConnector(), table);

        /*boolean has = false;
        try (Connection connection = this.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet tables = metaData.getTables(null, null, table, null);
            has = tables.next();
            tables.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return has;*/
    }

    @Deprecated
    protected final void addColumn(@NotNull String table, @NotNull String column, @NotNull String type) {
        this.addColumn(table, column, type, "");
    }

    @Deprecated
    protected final void addColumn(@NotNull String table, @NotNull String column,
                                   @NotNull String type, @NotNull String def) {
        if (this.hasColumn(table, column)) return;

        StringBuilder sql = new StringBuilder();
        sql.append("ALTER TABLE ").append(table).append(" ");
        sql.append("ADD ").append(column).append(" ").append(type);
        if (!def.isEmpty()) sql.append(" ").append("DEFAULT '").append(def).append("'");

        DataQueries.executeStatement(this.getConnector(), sql.toString());
    }

    @Deprecated
    protected final void removeColumn(@NotNull String table, @NotNull String column) {
        if (!this.hasColumn(table, column)) return;

        String sql = "ALTER TABLE " + table + " DROP COLUMN " + column;
        DataQueries.executeStatement(this.getConnector(), sql);
    }

    @Deprecated
    protected final void renameColumn(@NotNull String table, @NotNull String from, @NotNull String to) {
        if (!this.hasColumn(table, from)) return;

        String sql = "ALTER TABLE " + table + " RENAME COLUMN " + from + " TO " + to;
        DataQueries.executeStatement(this.getConnector(), sql);
    }

    @Deprecated
    public final boolean hasColumn(@NotNull String table, @NotNull String columnName) {

        String sql = "SELECT * FROM " + table;
        try (Connection connection = this.getConnection();
            Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(sql);
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columns = metaData.getColumnCount();
            for (int x = 1; x <= columns; x++) {
                if (columnName.equals(metaData.getColumnName(x))) {
                    return true;
                }
            }
            return false;
        }
        catch (SQLException e) {
            plugin.error("Could not check SQL column: '" + columnName + "' for '" + table + "'");
            e.printStackTrace();
            return false;
        }
    }

    @Deprecated
    protected final void addData(@NotNull String table, @NotNull LinkedHashMap<String, String> keys) {
        DataQueries.executeInsert(this.getConnector(), table, keys);
    }

    @Deprecated
    protected final void saveData(@NotNull String table,
                                  @NotNull LinkedHashMap<String, String> valuesMap,
                                  @NotNull Map<String, String> whereMap) {
        DataQueries.executeUpdate(this.getConnector(), table, valuesMap, whereMap);
    }

    @Deprecated
    public final boolean hasData(@NotNull String table, @NotNull Map<String, String> whereMap) {
        return getData(table, whereMap, (resultSet -> true)) != null;
    }

    @Deprecated
    protected final void deleteData(@NotNull String table, @NotNull Map<String, String> whereMap) {
        DataQueries.executeDelete(this.getConnector(), table, whereMap);
    }

    @Nullable
    @Deprecated
    protected final <T> T getData(@NotNull String table,
                                  @NotNull Map<String, String> whereMap,
                                  @NotNull Function<ResultSet, T> function) {
        return DataQueries.readData(this.getConnector(), table, whereMap, function);
    }

    @NotNull
    @Deprecated
    protected final <T> List<@NotNull T> getDatas(@NotNull String table,
                                                  @NotNull Map<String, String> whereMap,
                                                  @NotNull Function<ResultSet, T> dataFunction,
                                                  int amount) {
        return DataQueries.readData(this.getConnector(), table, whereMap, dataFunction, amount);
    }
}

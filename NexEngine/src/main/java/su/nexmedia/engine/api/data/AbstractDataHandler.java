package su.nexmedia.engine.api.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

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
        return builder.registerTypeAdapter(ItemStack.class, new ItemStackSerializer());
    }

    @NotNull
    protected final Connection getConnection() throws SQLException {
        return this.getConnector().getConnection();
    }

    public void createTable(@NotNull String table, @NotNull List<SQLColumn> columns) {
        CreateTableExecutor.builder(table, this.getDataType()).columns(columns).execute(this.getConnector());
    }

    public void renameTable(@NotNull String from, @NotNull String to) {
        RenameTableExecutor.builder(from, this.getDataType()).renameTo(to).execute(this.getConnector());
    }

    public void addColumn(@NotNull String table, @NotNull SQLValue... columns) {
        AlterTableExecutor.builder(table, this.getDataType()).addColumn(columns).execute(this.getConnector());
    }

    public void renameColumn(@NotNull String table, @NotNull SQLValue... columns) {
        AlterTableExecutor.builder(table, this.getDataType()).renameColumn(columns).execute(this.getConnector());
    }

    public void dropColumn(@NotNull String table, @NotNull SQLColumn... columns) {
        AlterTableExecutor.builder(table, this.getDataType()).dropColumn(columns).execute(this.getConnector());
    }

    public boolean hasColumn(@NotNull String table, @NotNull SQLColumn column) {
        return SQLQueries.hasColumn(this.getConnector(), table, column);
    }

    public void insert(@NotNull String table, @NotNull List<SQLValue> values) {
        InsertQueryExecutor.builder(table).values(values).execute(this.getConnector());
    }

    public void update(@NotNull String table, @NotNull List<SQLValue> values, @NotNull SQLCondition... conditions) {
        UpdateQueryExecutor.builder(table).values(values).where(conditions).execute(this.getConnector());
    }

    public void delete(@NotNull String table, @NotNull SQLCondition... conditions) {
        DeleteQueryExecutor.builder(table).where(conditions).execute(this.getConnector());
    }

    public boolean contains(@NotNull String table, @NotNull SQLCondition... conditions) {
        return this.load(table, (resultSet -> true), Collections.emptyList(), Arrays.asList(conditions)).isPresent();
    }

    public boolean contains(@NotNull String table, @NotNull List<SQLColumn> columns, @NotNull SQLCondition... conditions) {
        return this.load(table, (resultSet -> true), columns, Arrays.asList(conditions)).isPresent();
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
}

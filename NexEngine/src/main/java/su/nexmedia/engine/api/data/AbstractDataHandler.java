package su.nexmedia.engine.api.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.api.data.connection.AbstractDataConnector;
import su.nexmedia.engine.api.data.connection.ConnectorMySQL;
import su.nexmedia.engine.api.data.connection.ConnectorSQLite;
import su.nexmedia.engine.api.data.serialize.ItemStackSerializer;
import su.nexmedia.engine.api.manager.AbstractManager;
import su.nexmedia.engine.config.ConfigManager;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class AbstractDataHandler<P extends NexPlugin<P>> extends AbstractManager<P> {

    private static final ItemStackSerializer SERIALIZER_ITEM_STACK = new ItemStackSerializer();

    protected StorageType dataType;
    protected AbstractDataConnector connector;
    protected Gson gson;

    protected AbstractDataHandler(@NotNull P plugin) throws SQLException {
        super(plugin);
        ConfigManager<P> config = plugin.getConfigManager();

        this.dataType = plugin.getConfigManager().dataStorage;
        if (this.dataType == StorageType.MYSQL) {
            this.connector = new ConnectorMySQL(plugin, config.dataMysqlHost, config.dataMysqlBase, config.dataMysqlUser, config.dataMysqlPassword);
        }
        else {
            this.connector = new ConnectorSQLite(plugin);
        }
    }

    protected AbstractDataHandler(@NotNull P plugin,
                                  @NotNull String host, @NotNull String base,
                                  @NotNull String login, @NotNull String password) throws SQLException {
        super(plugin);
        this.dataType = StorageType.MYSQL;
        this.connector = new ConnectorMySQL(plugin, host, base, login, password);
    }

    protected AbstractDataHandler(@NotNull P plugin, @NotNull String filePath) throws SQLException {
        super(plugin);
        this.dataType = StorageType.SQLITE;
        this.connector = new ConnectorSQLite(plugin, filePath);
    }

    @Override
    protected void onLoad() {
        this.gson = this.registerAdapters(new GsonBuilder().setPrettyPrinting()).create();
    }

    @Override
    protected void onShutdown() {
        this.getConnector().close();
    }

    @NotNull
    public AbstractDataConnector getConnector() {
        return connector;
    }

    @NotNull
    protected GsonBuilder registerAdapters(@NotNull GsonBuilder builder) {
        // TODO Register for location?
        return builder.registerTypeAdapter(ItemStack.class, SERIALIZER_ITEM_STACK);
    }

    @NotNull
    protected final Connection getConnection() throws SQLException {
        return this.getConnector().getConnection();
    }

    protected final void createTable(@NotNull String table, @NotNull LinkedHashMap<String, String> valMap) {
        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS " + table + "(");
        StringBuilder columns = new StringBuilder();

        LinkedHashMap<String, String> valuesMap = new LinkedHashMap<>();
        valuesMap.put("id", DataTypes.INTEGER.build(this.dataType, 11, true));
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

    protected final void renameTable(@NotNull String from, @NotNull String to) {
        if (!this.hasTable(from)) return;

        StringBuilder sql = new StringBuilder();
        if (this.dataType == StorageType.MYSQL) {
            sql.append("RENAME TABLE ").append(from).append(" TO ").append(to).append(";");
        }
        else {
            sql.append("ALTER TABLE ").append(from).append(" RENAME TO ").append(to);
        }
        DataQueries.executeStatement(this.getConnector(), sql.toString());
    }

    protected final boolean hasTable(@NotNull String table) {
        boolean has = false;
        try (Connection connection = this.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet tables = metaData.getTables(null, null, table, null);
            has = tables.next();
            tables.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return has;
    }

    protected final void addColumn(@NotNull String table, @NotNull String column, @NotNull String type) {
        this.addColumn(table, column, type, "");
    }

    protected final void addColumn(@NotNull String table, @NotNull String column,
                                   @NotNull String type, @NotNull String def) {
        if (this.hasColumn(table, column)) return;

        StringBuilder sql = new StringBuilder();
        sql.append("ALTER TABLE ").append(table).append(" ");
        sql.append("ADD ").append(column).append(" ").append(type);
        if (!def.isEmpty()) sql.append(" ").append("DEFAULT '").append(def).append("'");

        DataQueries.executeStatement(this.getConnector(), sql.toString());
    }

    protected final void renameColumn(@NotNull String table, @NotNull String from, @NotNull String to) {
        if (!this.hasColumn(table, from)) return;

        String sql = "ALTER TABLE " + table + " RENAME COLUMN " + from + " TO " + to;
        DataQueries.executeStatement(this.getConnector(), sql);
    }

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

    protected final void addData(@NotNull String table, @NotNull LinkedHashMap<String, String> keys) {
        DataQueries.executeInsert(this.getConnector(), table, keys);
    }

    protected final void saveData(@NotNull String table,
                                  @NotNull LinkedHashMap<String, String> valuesMap,
                                  @NotNull Map<String, String> whereMap) {
        DataQueries.executeUpdate(this.getConnector(), table, valuesMap, whereMap);
    }

    public final boolean hasData(@NotNull String table, @NotNull Map<String, String> whereMap) {
        return getData(table, whereMap, (resultSet -> true)) != null;
    }

    protected final void deleteData(@NotNull String table, @NotNull Map<String, String> whereMap) {
        DataQueries.executeDelete(this.getConnector(), table, whereMap);
    }

    @Nullable
    protected final <T> T getData(@NotNull String table,
                                  @NotNull Map<String, String> whereMap,
                                  @NotNull Function<ResultSet, T> function) {
        return DataQueries.readData(this.getConnector(), table, whereMap, function);
    }

    @NotNull
    protected final <T> List<@NotNull T> getDatas(@NotNull String table,
                                                  @NotNull Map<String, String> whereMap,
                                                  @NotNull Function<ResultSet, T> dataFunction,
                                                  int amount) {
        return DataQueries.readData(this.getConnector(), table, whereMap, dataFunction, amount);
    }
}

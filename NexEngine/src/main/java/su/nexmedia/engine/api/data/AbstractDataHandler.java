package su.nexmedia.engine.api.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sqlite.JDBC;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.api.manager.AbstractManager;
import su.nexmedia.engine.api.data.serialize.ItemStackSerializer;

import java.sql.*;
import java.util.*;
import java.util.function.Function;

public abstract class AbstractDataHandler<P extends NexPlugin<P>> extends AbstractManager<P> {

    private static final ItemStackSerializer SERIALIZER_ITEM_STACK = new ItemStackSerializer();
    private final String url;
    protected StorageType dataType;
    protected Connection  connection;
    protected long        lastLive;
    protected Gson gson;
    private       String user;
    private       String password;

    protected AbstractDataHandler(@NotNull P plugin) throws SQLException {
        super(plugin);
        this.lastLive = System.currentTimeMillis();
        this.dataType = plugin.cfg().dataStorage;
        if (this.dataType == StorageType.MYSQL) {
            this.url = "jdbc:mysql://" + plugin.cfg().mysqlHost + "/" + plugin.cfg().mysqlBase + "?allowPublicKeyRetrieval=true&useSSL=false";
            this.user = plugin.cfg().mysqlLogin;
            this.password = plugin.cfg().mysqlPassword;
        }
        else {
            this.url = "jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + "/data.db";
            DriverManager.registerDriver(new JDBC());
        }
    }

    protected AbstractDataHandler(@NotNull P plugin,
                                  @NotNull String host, @NotNull String base,
                                  @NotNull String login, @NotNull String password) {
        super(plugin);
        this.lastLive = System.currentTimeMillis();
        this.dataType = StorageType.MYSQL;

        this.url = "jdbc:mysql://" + host + "/" + base + "?useSSL=false";
        this.user = login;
        this.password = password;
    }

    protected AbstractDataHandler(@NotNull P plugin, @NotNull String filePath, @NotNull String fileName) throws SQLException {
        super(plugin);
        this.lastLive = System.currentTimeMillis();
        this.dataType = StorageType.SQLITE;

        if (!filePath.endsWith("/")) filePath += "/";
        this.url = "jdbc:sqlite:" + filePath + fileName;
        DriverManager.registerDriver(new JDBC());
    }

    @Override
    protected void onLoad() {
        this.gson = this.registerAdapters(new GsonBuilder().setPrettyPrinting()).create();

        this.openConnection();
    }

    @Override
    protected void onShutdown() {
        this.close();
    }

    private void openConnection() {
        try {
            if (this.dataType == StorageType.MYSQL) {
                this.connection = DriverManager.getConnection(this.url, this.user, this.password);
            }
            else {
                this.connection = DriverManager.getConnection(this.url);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void close() {
        try {
            if (this.connection != null) {
                this.connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (this.connection != null) {
                    this.connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @NotNull
    protected GsonBuilder registerAdapters(@NotNull GsonBuilder builder) {
        // TODO Register for location?
        return builder.registerTypeAdapter(ItemStack.class, SERIALIZER_ITEM_STACK);
    }

    @NotNull
    protected final Connection getConnection() {
        try {
            if (this.connection == null || this.connection.isClosed()) {
                this.openConnection();
            }
            if (System.currentTimeMillis() - this.lastLive >= 10000L) {
                this.connection.prepareStatement("SELECT 1").executeQuery();
                this.lastLive = System.currentTimeMillis();
            }
        } catch (SQLException ex) {
            this.openConnection();
        }
        return this.connection;
    }

    protected final void createTable(@NotNull String table, @NotNull LinkedHashMap<String, String> valMap) {
        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS " + table + "(");

        StringBuilder columns = new StringBuilder();

        // Adding primary id-key column.
        if (this.dataType == StorageType.MYSQL) {
            columns.append("`id` int(11) NOT NULL AUTO_INCREMENT");
        }
        else if (this.dataType == StorageType.SQLITE) {
            columns.append("`id` INTEGER PRIMARY KEY AUTOINCREMENT");
        }

        // Adding all other columns with their types.
        valMap.forEach((col, type) -> {
            if (columns.length() > 0) columns.append(", ");
            columns.append("`").append(col).append("` ").append(type);
        });

        // For MySQL define 'id' column as primary key.
        if (this.dataType == StorageType.MYSQL) {
            columns.append(", PRIMARY KEY (`id`)");
        }

        // Add columns to main sql builder and close the statement.
        sql.append(columns).append(");");

        this.executeSQL(sql.toString());
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
        this.executeSQL(sql.toString());
    }

    protected final boolean hasTable(@NotNull String table) {
        boolean has = false;
        try {
            DatabaseMetaData metaData = this.getConnection().getMetaData();
            ResultSet tables = metaData.getTables(null, null, table, null);
            has = tables.next();
            tables.close();
        } catch (SQLException e) {
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

        this.executeSQL(sql.toString());
    }

    protected final void renameColumn(@NotNull String table, @NotNull String from, @NotNull String to) {
        if (!this.hasColumn(table, from)) return;

        StringBuilder sql = new StringBuilder();
        sql.append("ALTER TABLE ").append(table).append(" ");
        sql.append("RENAME COLUMN ").append(from).append(" TO ").append(to);

        this.executeSQL(sql.toString());
    }

    public final boolean hasColumn(@NotNull String table, @NotNull String columnName) {
        this.connection = this.getConnection();
        String sql = "SELECT * FROM " + table;
        try (Statement statement = this.connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(sql);
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columns = metaData.getColumnCount();
            for (int x = 1; x <= columns; x++) {
                if (columnName.equals(metaData.getColumnName(x))) {
                    return true;
                }
            }
            return false;
        } catch (SQLException e) {
            plugin.error("Could not check SQL column: '" + columnName + "' for '" + table + "'");
            e.printStackTrace();
            return false;
        }
    }

    protected final void addData(@NotNull String table, @NotNull LinkedHashMap<String, String> keys) {
        StringBuilder sql = new StringBuilder("INSERT INTO " + table + "(");

        // Prepare column names.
        StringBuilder columns = new StringBuilder();
        keys.keySet().forEach((key) -> {
            if (columns.length() > 0) {
                columns.append(", ");
            }
            columns.append("`").append(key).append("`");
        });
        sql.append(columns).append(") VALUES(");

        // Prepare column values.
        StringBuilder values = new StringBuilder();
        keys.values().forEach((value) -> {
            if (values.length() > 0) {
                values.append(", ");
            }
            values.append("'").append(value).append("'");
        });
        sql.append(values).append(")");

        this.executeSQL(sql.toString());
    }

    protected final void saveData(@NotNull String table,
                                  @NotNull LinkedHashMap<String, String> valMap,
                                  @NotNull Map<String, String> whereMap) {

        StringBuilder sql = new StringBuilder("UPDATE " + table + " SET ");

        StringBuilder values = new StringBuilder();
        valMap.forEach((key, value) -> {
            if (values.length() > 0) {
                values.append(", ");
            }
            values.append("`").append(key).append("` = '").append(value).append("'");
        });
        sql.append(values);
        sql.append(" WHERE ");

        StringBuilder wheres = new StringBuilder();
        whereMap.forEach((key, value) -> {
            if (wheres.length() > 0) {
                wheres.append(" AND ");
            }
            wheres.append("`").append(key).append("` = '").append(value).append("'");
        });
        sql.append(wheres);

        this.executeSQL(sql.toString());
    }

    public final boolean hasData(@NotNull String table, @NotNull Map<String, String> whereMap) {
        StringBuilder sql = new StringBuilder("SELECT 1 FROM ").append(table);

        // Prepare 'where' columns.
        StringBuilder wheres = new StringBuilder();
        whereMap.keySet().forEach((key) -> {
            if (wheres.length() > 0) {
                wheres.append(" AND ");
            }
            wheres.append("`").append(key).append("` = ?");
        });
        sql.append(" WHERE ");
        sql.append(wheres);

        // Execute query and set 'where' values in statement.
        try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            int count = 1;
            for (String wValue : whereMap.values()) {
                statement.setString(count++, wValue);
            }

            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    protected final void deleteData(@NotNull String table, @NotNull Map<String, String> whereMap) {
        StringBuilder sql = new StringBuilder("DELETE FROM " + table + " WHERE ");

        StringBuilder wheres = new StringBuilder();
        whereMap.forEach((key, value) -> {
            if (wheres.length() > 0) {
                wheres.append(" AND ");
            }
            wheres.append("`").append(key).append("` = '").append(value).append("'");
        });
        sql.append(wheres);

        this.executeSQL(sql.toString());
    }

    @Nullable
    protected final <T> T getData(@NotNull String table,
                                  @NotNull Map<String, String> whereMap,
                                  @NotNull Function<ResultSet, T> function) {
        List<T> data = this.getDatas(table, whereMap, function, 1);
        return data.isEmpty() ? null : data.get(0);
    }

    @NotNull
    protected final <T> List<@NotNull T> getDatas(@NotNull String table,
                                                  @NotNull Map<String, String> whereMap,
                                                  @NotNull Function<ResultSet, T> dataFunction,
                                                  int amount) {

        StringBuilder sql = new StringBuilder("SELECT * FROM " + table);
        List<T> list = new ArrayList<>();
        this.connection = this.getConnection();

        if (!whereMap.isEmpty()) {
            StringBuilder wheres = new StringBuilder();
            whereMap.keySet().forEach((key) -> {
                if (wheres.length() > 0) {
                    wheres.append(" AND ");
                }
                wheres.append("`").append(key).append("` = ?");
            });
            sql.append(" WHERE ");
            sql.append(wheres);

            try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
                int count = 1;
                for (String wValue : whereMap.values()) {
                    statement.setString(count++, wValue);
                }

                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next() && (amount < 0 || list.size() < amount)) {
                    list.add(dataFunction.apply(resultSet));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        else {
            try (Statement statement = this.connection.createStatement()) {
                ResultSet resultSet = statement.executeQuery(sql.toString());
                while (resultSet.next() && (amount < 0 || list.size() < amount)) {
                    list.add(dataFunction.apply(resultSet));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        list.removeIf(Objects::isNull);

        return list;
    }

    public final void executeSQL(@NotNull String sql) {
        this.connection = this.getConnection();
        try (PreparedStatement statement = this.connection.prepareStatement(sql)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.error("Could not execute SQL statement: [" + sql + "]");
            e.printStackTrace();
        }
    }
}

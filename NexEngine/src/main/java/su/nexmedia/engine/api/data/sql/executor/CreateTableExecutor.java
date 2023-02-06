package su.nexmedia.engine.api.data.sql.executor;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.data.StorageType;
import su.nexmedia.engine.api.data.connection.AbstractDataConnector;
import su.nexmedia.engine.api.data.sql.SQLColumn;
import su.nexmedia.engine.api.data.sql.SQLExecutor;
import su.nexmedia.engine.api.data.sql.SQLQueries;
import su.nexmedia.engine.api.data.sql.column.ColumnFormer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class CreateTableExecutor extends SQLExecutor<Boolean> {

    private final StorageType storageType;
    private final List<SQLColumn> columns;

    private CreateTableExecutor(@NotNull String table, @NotNull StorageType storageType) {
        super(table);
        this.storageType = storageType;
        this.columns = new ArrayList<>();
    }

    @NotNull
    public static CreateTableExecutor builder(@NotNull String table, @NotNull StorageType storageType) {
        return new CreateTableExecutor(table, storageType);
    }

    @NotNull
    public CreateTableExecutor columns(@NotNull SQLColumn... columns) {
        return this.columns(Arrays.asList(columns));
    }

    @NotNull
    public CreateTableExecutor columns(@NotNull List<SQLColumn> columns) {
        this.columns.clear();
        this.columns.addAll(columns);
        return this;
    }

    @Override
    @NotNull
    public Boolean execute(@NotNull AbstractDataConnector connector) {
        if (this.columns.isEmpty()) return false;

        String id = "`id` " + ColumnFormer.INTEGER.build(this.storageType, 11);
        if (this.storageType == StorageType.SQLITE) {
            id += " PRIMARY KEY AUTOINCREMENT";
        }
        else {
            id += " PRIMARY KEY AUTO_INCREMENT";
        }

        String columns = id + "," + this.columns.stream()
            .map(column -> column.getNameEscaped() + " " + column.formatType(this.storageType))
            .collect(Collectors.joining(","));

        String sql = "CREATE TABLE IF NOT EXISTS " + this.table + "(" + columns + ");";

        return SQLQueries.executeStatement(connector, sql);
    }
}

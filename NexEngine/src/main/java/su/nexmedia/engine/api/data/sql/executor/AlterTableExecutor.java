package su.nexmedia.engine.api.data.sql.executor;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.data.StorageType;
import su.nexmedia.engine.api.data.connection.AbstractDataConnector;
import su.nexmedia.engine.api.data.sql.SQLColumn;
import su.nexmedia.engine.api.data.sql.SQLExecutor;
import su.nexmedia.engine.api.data.sql.SQLQueries;
import su.nexmedia.engine.api.data.sql.SQLValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class AlterTableExecutor extends SQLExecutor<Boolean> {

    private final StorageType    storageType;
    private final List<SQLValue> columns;
    private Type type;

    private AlterTableExecutor(@NotNull String table, @NotNull StorageType storageType) {
        super(table);
        this.storageType = storageType;
        this.columns = new ArrayList<>();
    }

    private enum Type {
        ADD_COLUMN, RENAME_COLUMN, DROP_COLUMN
    }

    @NotNull
    public static AlterTableExecutor builder(@NotNull String table, @NotNull StorageType storageType) {
        return new AlterTableExecutor(table, storageType);
    }

    @NotNull
    public AlterTableExecutor addColumn(@NotNull SQLValue... columns) {
        return this.addColumn(Arrays.asList(columns));
    }

    @NotNull
    public AlterTableExecutor addColumn(@NotNull List<SQLValue> columns) {
        return this.columns(columns, Type.ADD_COLUMN);
    }

    @NotNull
    public AlterTableExecutor renameColumn(@NotNull SQLValue... columns) {
        return this.addColumn(Arrays.asList(columns));
    }

    @NotNull
    public AlterTableExecutor renameColumn(@NotNull List<SQLValue> columns) {
        return this.columns(columns, Type.RENAME_COLUMN);
    }

    @NotNull
    public AlterTableExecutor dropColumn(@NotNull SQLColumn... columns) {
        return this.dropColumn(Arrays.asList(columns));
    }

    @NotNull
    public AlterTableExecutor dropColumn(@NotNull List<SQLColumn> columns) {
        return this.columns(columns.stream().map(column -> column.toValue("dummy")).toList(), Type.DROP_COLUMN);
    }

    private AlterTableExecutor columns(@NotNull List<SQLValue> values, @NotNull Type type) {
        this.columns.clear();
        this.columns.addAll(values);
        this.type = type;
        return this;
    }

    @Override
    @NotNull
    public Boolean execute(@NotNull AbstractDataConnector connector) {
        if (this.columns.isEmpty()) return false;

        if (this.type == Type.ADD_COLUMN) {
            this.columns.forEach(value -> {
                if (SQLQueries.hasColumn(connector, this.table, value.getColumn())) return;

                String sql = "ALTER TABLE " + this.table + " ADD "
                    + value.getColumn().getName() + " " + value.getColumn().formatType(this.storageType)
                    + " DEFAULT '" + value.getValue() + "'";
                SQLQueries.executeStatement(connector, sql);
            });
        }
        else if (this.type == Type.RENAME_COLUMN) {
            this.columns.forEach(value -> {
                if (!SQLQueries.hasColumn(connector, this.table, value.getColumn())) return;

                String sql = "ALTER TABLE " + this.table + " RENAME COLUMN " + value.getColumn().getName() + " TO " + value.getValue();
                SQLQueries.executeStatement(connector, sql);
            });
        }
        else if (this.type == Type.DROP_COLUMN) {
            this.columns.forEach(value -> {
                if (!SQLQueries.hasColumn(connector, this.table, value.getColumn())) return;

                String sql = "ALTER TABLE " + this.table + " DROP COLUMN " + value.getColumn().getName();
                SQLQueries.executeStatement(connector, sql);
            });
        }

        return true;
    }
}

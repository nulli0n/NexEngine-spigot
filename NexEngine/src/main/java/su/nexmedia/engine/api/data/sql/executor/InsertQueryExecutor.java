package su.nexmedia.engine.api.data.sql.executor;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.data.connection.AbstractDataConnector;
import su.nexmedia.engine.api.data.sql.SQLColumn;
import su.nexmedia.engine.api.data.sql.SQLExecutor;
import su.nexmedia.engine.api.data.sql.SQLQueries;
import su.nexmedia.engine.api.data.sql.SQLValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class InsertQueryExecutor extends SQLExecutor<Void> {

    private final List<SQLValue> values;

    private InsertQueryExecutor(@NotNull String table) {
        super(table);
        this.values = new ArrayList<>();
    }

    @NotNull
    public static InsertQueryExecutor builder(@NotNull String table) {
        return new InsertQueryExecutor(table);
    }

    @NotNull
    public InsertQueryExecutor values(@NotNull SQLValue... values) {
        return this.values(Arrays.asList(values));
    }

    @NotNull
    public InsertQueryExecutor values(@NotNull List<SQLValue> values) {
        this.values.clear();
        this.values.addAll(values);
        return this;
    }

    @Override
    @NotNull
    public Void execute(@NotNull AbstractDataConnector connector) {
        if (this.values.isEmpty()) return null;

        String columns = this.values.stream().map(SQLValue::getColumn).map(SQLColumn::getNameEscaped).collect(Collectors.joining(","));
        String values = this.values.stream().map(value -> "?").collect(Collectors.joining(","));
        String sql = INSERT_INTO + " " + this.getTable() + "(" + columns + ") " + VALUES + "(" + values + ")";
        List<String> values2 = this.values.stream().map(SQLValue::getValue).toList();

        SQLQueries.executeStatement(connector, sql, values2);
        return null;
    }
}

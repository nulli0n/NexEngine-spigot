package su.nexmedia.engine.api.data.sql.executor;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.data.connection.AbstractDataConnector;
import su.nexmedia.engine.api.data.sql.SQLCondition;
import su.nexmedia.engine.api.data.sql.SQLExecutor;
import su.nexmedia.engine.api.data.sql.SQLQueries;
import su.nexmedia.engine.api.data.sql.SQLValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class UpdateQueryExecutor extends SQLExecutor<Void> {

    private final List<SQLValue>     values;
    private final List<SQLCondition> wheres;

    private UpdateQueryExecutor(@NotNull String table) {
        super(table);
        this.values = new ArrayList<>();
        this.wheres = new ArrayList<>();
    }

    @NotNull
    public static UpdateQueryExecutor builder(@NotNull String table) {
        return new UpdateQueryExecutor(table);
    }

    @NotNull
    public UpdateQueryExecutor values(@NotNull SQLValue... values) {
        return this.values(Arrays.asList(values));
    }

    @NotNull
    public UpdateQueryExecutor values(@NotNull List<SQLValue> values) {
        this.values.clear();
        this.values.addAll(values);
        return this;
    }

    @NotNull
    public UpdateQueryExecutor where(@NotNull SQLCondition... wheres) {
        return this.where(Arrays.asList(wheres));
    }

    @NotNull
    public UpdateQueryExecutor where(@NotNull List<SQLCondition> wheres) {
        this.wheres.clear();
        this.wheres.addAll(wheres);
        return this;
    }

    @Override
    @NotNull
    public Void execute(@NotNull AbstractDataConnector connector) {
        if (this.values.isEmpty()) return null;

        String values = this.values.stream().map(value -> value.getColumn().getNameEscaped() + " = ?")
            .collect(Collectors.joining(","));
        String wheres = this.wheres.stream().map(where -> where.getValue().getColumn().getNameEscaped() + " " + where.getType().getOperator() + " ?")
            .collect(Collectors.joining(" AND "));
        String sql = "UPDATE " + this.getTable() + " SET " + values + (wheres.isEmpty() ? "" : " WHERE " + wheres);

        List<String> values2 = this.values.stream().map(SQLValue::getValue).toList();
        List<String> whers2 = this.wheres.stream().map(SQLCondition::getValue).map(SQLValue::getValue).toList();

        SQLQueries.executeStatement(connector, sql, values2, whers2);
        return null;
    }

}

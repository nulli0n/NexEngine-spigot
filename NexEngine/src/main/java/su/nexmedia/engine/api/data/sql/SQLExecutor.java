package su.nexmedia.engine.api.data.sql;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.data.connection.AbstractDataConnector;

public abstract class SQLExecutor<T> {

    protected static final String SELECT      = "SELECT";
    protected static final String FROM        = "FROM";
    protected static final String UPDATE      = "UPDATE";
    protected static final String SET         = "SET";
    protected static final String WHERE       = "WHERE";
    protected static final String INSERT_INTO = "INSERT INTO";
    protected static final String VALUES      = "VALUES";

    protected final String table;

    protected SQLExecutor(@NotNull String table) {
        this.table = table;
    }

    @NotNull
    public String getTable() {
        return "`" + this.table + "`";
    }

    @NotNull
    public abstract T execute(@NotNull AbstractDataConnector connector);
}

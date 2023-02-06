package su.nexmedia.engine.api.data.sql;

import org.jetbrains.annotations.NotNull;

public class SQLCondition {

    private final SQLValue value;
    private final Type      type;

    public SQLCondition(@NotNull SQLValue value, @NotNull Type type) {
        this.value = value;
        this.type = type;
    }

    public static SQLCondition of(@NotNull SQLValue value, @NotNull Type type) {
        return new SQLCondition(value, type);
    }

    @NotNull
    public Type getType() {
        return type;
    }

    @NotNull
    public SQLValue getValue() {
        return value;
    }

    public enum Type {
        GREATER(">"),
        SMALLER("<"),
        EQUAL("=");

        private final String operator;

        Type(@NotNull String operator) {
            this.operator = operator;
        }

        @NotNull
        public String getOperator() {
            return operator;
        }
    }
}

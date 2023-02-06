package su.nexmedia.engine.api.data.sql.column;

import org.jetbrains.annotations.NotNull;

public enum ColumnType {

    INTEGER(ColumnFormer.INTEGER),
    DOUBLE(ColumnFormer.DOUBLE),
    LONG(ColumnFormer.LONG),
    BOOLEAN(ColumnFormer.BOOLEAN),
    STRING(ColumnFormer.STRING),
    ;

    private final ColumnFormer former;

    ColumnType(@NotNull ColumnFormer former) {
        this.former = former;
    }

    @NotNull
    public ColumnFormer getFormer() {
        return former;
    }
}

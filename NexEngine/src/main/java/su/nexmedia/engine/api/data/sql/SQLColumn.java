package su.nexmedia.engine.api.data.sql;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.data.StorageType;
import su.nexmedia.engine.api.data.sql.column.ColumnType;

public class SQLColumn {

    private final String     name;
    private final ColumnType type;
    private final int        length;

    public SQLColumn(@NotNull String name, @NotNull ColumnType type, int length) {
        this.name = name.replace(" ", "_");
        this.type = type;
        this.length = length;
    }

    @NotNull
    public static SQLColumn of(@NotNull String name, @NotNull ColumnType type) {
        return SQLColumn.of(name, type, -1);
    }

    @NotNull
    public static SQLColumn of(@NotNull String name, @NotNull ColumnType type, int length) {
        return new SQLColumn(name, type, length);
    }

    @NotNull
    public SQLColumn asLowerCase() {
        return of("LOWER(" + this.getName() + ")", this.getType(), this.getLength());
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public String getNameEscaped() {
        if (this.getName().startsWith("LOWER(")) return this.getName();

        return this.getName().equalsIgnoreCase("*") ? this.getName() : "`" + this.getName() + "`";
    }

    @NotNull
    public ColumnType getType() {
        return type;
    }

    public int getLength() {
        return length;
    }

    @NotNull
    public String formatType(@NotNull StorageType storageType) {
        return this.getType().getFormer().build(storageType, this.getLength());
    }

    @NotNull
    public SQLValue toValue(@NotNull Object value) {
        return SQLValue.of(this, String.valueOf(value));
    }
}

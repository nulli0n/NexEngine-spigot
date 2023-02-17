package su.nexmedia.engine.api.data.sql.column;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.data.StorageType;

public interface ColumnFormer {

    ColumnFormer STRING = (storageType, length) -> {
        if (length < 1 || storageType == StorageType.SQLITE) {
            return storageType == StorageType.SQLITE ? "TEXT NOT NULL" : "MEDIUMTEXT NOT NULL";
        }
        return "varchar(" + length + ") CHARACTER SET utf8 NOT NULL";
    };

    ColumnFormer INTEGER = (storageType, length) -> {
        if (length < 1 || storageType == StorageType.SQLITE) {
            return "INTEGER NOT NULL";
        }
        return "int(" + length + ") NOT NULL";
    };

    ColumnFormer DOUBLE = (storageType, length) -> {
        return storageType == StorageType.SQLITE ? "REAL NOT NULL" : "double NOT NULL";
    };

    ColumnFormer LONG = (storageType, length) -> {
        return storageType == StorageType.SQLITE ? "BIGINT NOT NULL" : "bigint(" + length + ") NOT NULL";
    };

    ColumnFormer BOOLEAN = (storageType, length) -> {
        return storageType == StorageType.SQLITE ? "INTEGER NOT NULL" : "tinyint(1) NOT NULL";
    };

    @NotNull String build(@NotNull StorageType storageType, int length);
}

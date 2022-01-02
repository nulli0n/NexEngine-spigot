package su.nexmedia.engine.api.data;

import org.jetbrains.annotations.NotNull;

public class DataTypes {

    public static final DataString  STRING  = new DataString();
    public static final DataChar    CHAR    = new DataChar();
    public static final DataInteger INTEGER = new DataInteger();
    public static final DataDouble  DOUBLE  = new DataDouble();
    public static final DataLong    LONG    = new DataLong();
    public static final DataBoolean BOOLEAN = new DataBoolean();

    public static class DataString {

        @NotNull
        public String build(@NotNull StorageType dataType) {
            return build(dataType, -1);
        }

        @NotNull
        public String build(@NotNull StorageType dataType, int length) {
            if (length < 1 || dataType == StorageType.SQLITE) {
                return "TEXT NOT NULL";
            }
            return "varchar(" + length + ") CHARACTER SET utf8 NOT NULL";
        }
    }

    public static class DataChar {

        @NotNull
        public String build(@NotNull StorageType dataType) {
            return build(dataType, -1);
        }

        @NotNull
        public String build(@NotNull StorageType dataType, int length) {
            if (length < 1 || dataType == StorageType.SQLITE) {
                return "TEXT NOT NULL";
            }
            return "char(" + length + ") CHARACTER SET utf8 NOT NULL";
        }
    }

    public static class DataInteger {

        @NotNull
        public String build(@NotNull StorageType dataType) {
            return build(dataType, -1);
        }

        @NotNull
        public String build(@NotNull StorageType dataType, int length) {
            if (length < 1 || dataType == StorageType.SQLITE) {
                return "INTEGER NOT NULL";
            }
            return "int(" + length + ") NOT NULL";
        }
    }

    public static class DataDouble {

        @NotNull
        public String build(@NotNull StorageType dataType) {
            return dataType == StorageType.SQLITE ? "REAL NOT NULL" : "double NOT NULL";
        }
    }

    public static class DataLong {

        @NotNull
        public String build(@NotNull StorageType dataType) {
            return this.build(dataType, 64);
        }

        @NotNull
        public String build(@NotNull StorageType dataType, int length) {
            return dataType == StorageType.SQLITE ? "BIGINT NOT NULL" : "bigint(" + length + ") NOT NULL";
        }
    }

    public static class DataBoolean {

        @NotNull
        public String build(@NotNull StorageType dataType) {
            if (dataType == StorageType.SQLITE) {
                return "INTEGER NOT NULL";
            }
            return "tinyint(1) NOT NULL";
        }
    }
}

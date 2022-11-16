package su.nexmedia.engine.api.data.config;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.data.StorageType;

import java.util.stream.Stream;

public class DataConfig {

    public int         saveInterval;
    public int         syncInterval;
    public StorageType storageType;
    public String      tablePrefix;
    public boolean     purgeEnabled;
    public int         purgePeriod;

    public String mysqlUser;
    public String mysqlPassword;
    public String mysqlHost;
    public String mysqlBase;

    public String sqliteFilename;

    public DataConfig(@NotNull JYML cfg) {
        cfg.addMissing("Database.Auto_Save_Interval", 20);
        cfg.addMissing("Database.Sync_Interval", -1);
        cfg.addMissing("Database.Type", StorageType.SQLITE.name());
        cfg.addMissing("Database.Table_Prefix", "");
        cfg.addMissing("Database.MySQL.Username", "root");
        cfg.addMissing("Database.MySQL.Password", "root");
        cfg.addMissing("Database.MySQL.Host", "localhost");
        cfg.addMissing("Database.MySQL.Database", "minecraft");
        cfg.addMissing("Database.SQLite.FileName", "data.db");
        cfg.addMissing("Database.Purge.Enabled", false);
        cfg.addMissing("Database.Purge.For_Period", 60);

        cfg.setComments("Database", "Plugin database settings.");
        cfg.setComments("Database.Auto_Save_Interval", "Defines how often (in minutes) user data of online players will be saved to the database.", "Set -1 to disable.");
        cfg.setComments("Database.Sync_Interval", "Defines how often (in seconds) user data of online players will be fetched and loaded from the database.");
        cfg.setComments("Database.Type", "Sets database type.", "Available values: " + String.join(",", Stream.of(StorageType.values()).map(Enum::name).toList()));
        cfg.setComments("Database.Table_Prefix", "Table prefix to use when plugin create tables. Do not leave this empty.");
        cfg.setComments("Database.MySQL", "MySQL Settings. Useful only when Database Type is " + StorageType.MYSQL.name());
        cfg.setComments("Database.MySQL.Username", "Database user name.");
        cfg.setComments("Database.MySQL.Password", "Database password.");
        cfg.setComments("Database.MySQL.Host", "Database host address. Like http://127.0.0.1:3306/");
        cfg.setComments("Database.MySQL.Database", "MySQL database name, where new tables will be created.");
        cfg.setComments("Database.SQLite", "SQLite Settings. Useful only when Database Type is " + StorageType.SQLITE.name());
        cfg.setComments("Database.SQLite.FileName", "File name for the database file.", "Actually it's a path to the file, so you can use directories here.");
        cfg.setComments("Database.Purge", "Purge will remove all records from the plugin tables that are 'old' enough.");
        cfg.setComments("Database.Purge.Enabled", "Enables/Disables purge feature.");
        cfg.setComments("Database.Purge.For_Period", "Sets maximal 'age' for database records before they will be purged.", "This option may have different behavior depends on the plugin.", "By default it's days for inactive plugin users.");
        cfg.saveChanges();

        String path = "Database.";
        this.storageType = cfg.getEnum(path + "Type", StorageType.class, StorageType.SQLITE);
        this.saveInterval = cfg.getInt(path + "Auto_Save_Interval", 20);
        this.syncInterval = cfg.getInt(path + "Sync_Interval");
        this.tablePrefix = cfg.getString(path + "Table_Prefix", "");

        if (this.storageType == StorageType.MYSQL) {
            this.mysqlUser = cfg.getString(path + "MySQL.Username");
            this.mysqlPassword = cfg.getString(path + "MySQL.Password");
            this.mysqlHost = cfg.getString(path + "MySQL.Host");
            this.mysqlBase = cfg.getString(path + "MySQL.Database");
        }
        else {
            this.sqliteFilename = cfg.getString(path + "SQLite.FileName", "data.db");
        }

        path = "Database.Purge.";
        this.purgeEnabled = cfg.getBoolean(path + "Enabled");
        this.purgePeriod = cfg.getInt(path + "For_Period", 60);
    }
}

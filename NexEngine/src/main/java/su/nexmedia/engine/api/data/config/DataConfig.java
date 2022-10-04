package su.nexmedia.engine.api.data.config;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.data.StorageType;

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

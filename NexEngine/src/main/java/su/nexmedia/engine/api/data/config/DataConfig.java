package su.nexmedia.engine.api.data.config;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JOption;
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
    public String mysqlParameters;

    public int mysqlSize;
    public int mysqlIdle;
    public long mysqlLifetime;
    public long mysqlKeepalive;
    public long mysqlTimeout;

    public String sqliteFilename;

    public DataConfig(@NotNull JYML cfg) {
        String path = "Database.";
        this.storageType = JOption.create(path + "Type", StorageType.class, StorageType.SQLITE,
            "Sets database type.",
            "Available values: " + String.join(",", Stream.of(StorageType.values()).map(Enum::name).toList()))
            .read(cfg);
        this.saveInterval = JOption.create(path + "Auto_Save_Interval", 20,
            "Defines how often (in minutes) user data of online players will be saved to the database.",
            "Set -1 to disable.")
            .read(cfg);
        this.syncInterval = JOption.create(path + "Sync_Interval", -1,
            "Defines how often (in seconds) plugin data will be fetched and loaded from the remote database.")
            .read(cfg);
        this.tablePrefix = JOption.create(path + "Table_Prefix", "",
            "Table prefix to use when plugin create tables. Do not leave this empty.")
            .read(cfg);

        if (this.storageType == StorageType.MYSQL) {
            this.mysqlUser = JOption.create(path + "MySQL.Username", "root",
                "Database user name.")
                .read(cfg);
            this.mysqlPassword = JOption.create(path + "MySQL.Password", "root",
                "Database user password.")
                .read(cfg);
            this.mysqlHost = JOption.create(path + "MySQL.Host", "localhost:3306",
                "Database host address. Like http://127.0.0.1:3306/")
                .read(cfg);
            this.mysqlBase = JOption.create(path + "MySQL.Database", "minecraft",
                "MySQL database name, where plugin tables will be created.")
                .read(cfg);
            this.mysqlParameters = JOption.create(path + "MySQL.Parameters", "?autoReconnect=true&useSSL=false&useUnicode=true&characterEncoding=UTF-8",
                            "The parameters for the connection.")
                    .read(cfg);
            path = "Database.connection_pool.";
            this.mysqlSize = JOption.create(path + "size", 10,
                            "Sets the maximum size of the MySQL connection pool.")
                    .read(cfg);
            this.mysqlIdle = JOption.create(path + "idle", 10,
                            "Sets the minimum number of idle connections that the pool will try to maintain.")
                    .read(cfg);
            this.mysqlLifetime = JOption.create(path + "lifetime", 1800000,
                            "This setting controls the maximum lifetime of a connection in the pool in milliseconds.")
                    .read(cfg);
            this.mysqlKeepalive = JOption.create(path + "keepalive", 30000,
                            "This setting controls how frequently the pool will 'ping' a connection in order to prevent it",
                            "from being timed out by the database or network infrastructure, measured in milliseconds.")
                    .read(cfg);
            this.mysqlTimeout = JOption.create(path + "timeout", 20000,
                            "This setting controls the maximum number of milliseconds that the plugin will wait for a",
                            "connection from the pool, before timing out.")
                    .read(cfg);
        }
        else {
            this.sqliteFilename = JOption.create(path + "SQLite.FileName", "data.db",
                "File name for the database file.",
                "Actually it's a path to the file, so you can use directories here.")
                .read(cfg);
        }

        path = "Database.Purge.";
        this.purgeEnabled = JOption.create(path + "Enabled", false,
            "Enables/Disables purge feature.",
            "Purge will remove all records from the plugin tables that are 'old' enough.")
            .read(cfg);
        this.purgePeriod = JOption.create(path + "For_Period", 60,
            "Sets maximal 'age' for the database records before they will be purged.",
            "This option may have different behavior depends on the plugin.",
            "By default it's days of inactivity for the plugin users.")
            .read(cfg);

        cfg.saveChanges();
    }
}

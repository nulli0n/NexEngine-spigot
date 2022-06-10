package su.nexmedia.engine.api.config;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.api.data.StorageType;
import su.nexmedia.engine.api.data.UserDataHolder;
import su.nexmedia.engine.api.module.AbstractModule;
import su.nexmedia.engine.utils.StringUtil;

public abstract class ConfigTemplate {

    public String   pluginName;
    public String[] commandAliases;
    public String   languageCode;

    public int         dataSaveInterval;
    public boolean     dataSaveInstant;
    public StorageType dataStorage;
    public boolean     dataPurgeEnabled;
    public int         dataPurgeDays;

    public String mysqlLogin;
    public String mysqlPassword;
    public String mysqlHost;
    public String mysqlBase;

    protected NexPlugin<?> plugin;
    protected JYML         cfg;

    public ConfigTemplate(@NotNull NexPlugin<?> plugin) {
        this.plugin = plugin;
    }

    public final void setup() {
        this.cfg = plugin.getConfigManager().configMain;
        this.cfg.remove("core");
        this.cfg.remove("data");

        this.cfg.addMissing("Plugin.Prefix", plugin.getName());
        this.cfg.addMissing("Plugin.Command_Aliases", plugin.getNameRaw());
        this.cfg.addMissing("Plugin.Language", "en");

        this.pluginName = StringUtil.color(cfg.getString("Plugin.Prefix", plugin.getName()));
        this.commandAliases = cfg.getString("Plugin.Command_Aliases", "").split(",");
        this.languageCode = cfg.getString("Plugin.Language", "en").toLowerCase();

        if (this.plugin instanceof UserDataHolder) {
            this.cfg.addMissing("Database.Auto_Save_Interval", 20);
            this.cfg.addMissing("Database.Instant_Save", false);
            this.cfg.addMissing("Database.Type", StorageType.SQLITE.name());
            this.cfg.addMissing("Database.MySQL.Username", "root");
            this.cfg.addMissing("Database.MySQL.Password", "root");
            this.cfg.addMissing("Database.MySQL.Host", "localhost");
            this.cfg.addMissing("Database.MySQL.Database", "minecraft");
            this.cfg.addMissing("Database.Purge.Enabled", false);
            this.cfg.addMissing("Database.Purge.For_Inactive_Days", 60);

            String path = "Database.";
            this.dataStorage = cfg.getEnum(path + "Type", StorageType.class, StorageType.SQLITE);
            this.dataSaveInterval = cfg.getInt(path + "Auto_Save_Interval", 20);
            this.dataSaveInstant = cfg.getBoolean(path + "Instant_Save");

            if (this.dataStorage == StorageType.MYSQL) {
                this.mysqlLogin = cfg.getString(path + "MySQL.Username");
                this.mysqlPassword = cfg.getString(path + "MySQL.Password");
                this.mysqlHost = cfg.getString(path + "MySQL.Host");
                this.mysqlBase = cfg.getString(path + "MySQL.Database");
            }

            path = "Database.Purge.";
            this.dataPurgeEnabled = cfg.getBoolean(path + "Enabled");
            this.dataPurgeDays = cfg.getInt(path + "For_Inactive_Days", 60);
        }

        this.load();
        this.cfg.saveChanges();
    }

    protected abstract void load();

    @NotNull
    public JYML getJYML() {
        return this.cfg;
    }

    @Deprecated
    public final void save() {
        this.cfg.saveChanges();
    }

    public final boolean isModuleEnabled(@NotNull AbstractModule<?> module) {
        return this.isModuleEnabled(module.getId());
    }

    public final boolean isModuleEnabled(@NotNull String module) {
        this.cfg.addMissing("Modules." + module + ".Enabled", true);
        this.cfg.saveChanges();
        return this.cfg.getBoolean("Modules." + module + ".Enabled");
    }

    public final void disableModule(@NotNull AbstractModule<?> module) {
        this.cfg.set("Modules." + module.getId() + ".Enabled", false);
        this.cfg.saveChanges();
    }

    @NotNull
    public final String getModuleName(@NotNull AbstractModule<?> module) {
        this.cfg.addMissing("Modules." + module.getId() + ".Name", StringUtil.capitalizeFully(module.getId().replace("_", " ")));
        this.cfg.saveChanges();
        return this.cfg.getString("Modules." + module.getId() + ".Name", module.getId());
    }
}

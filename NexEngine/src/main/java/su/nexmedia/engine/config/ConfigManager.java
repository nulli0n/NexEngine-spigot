package su.nexmedia.engine.config;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.data.StorageType;
import su.nexmedia.engine.api.data.UserDataHolder;
import su.nexmedia.engine.api.manager.AbstractManager;
import su.nexmedia.engine.api.module.AbstractModule;
import su.nexmedia.engine.utils.Placeholders;
import su.nexmedia.engine.utils.ResourceExtractor;
import su.nexmedia.engine.utils.StringUtil;

import java.io.File;
import java.io.IOException;

public class ConfigManager<P extends NexPlugin<P>> extends AbstractManager<P> {

    private JYML config;

    public String   pluginName;
    public String pluginPrefix;
    public String[] commandAliases;
    public String   languageCode;

    public int         dataSaveInterval;
    public boolean     dataSaveInstant;
    public int         dataSyncInterval;
    public StorageType dataStorage;
    public boolean     dataPurgeEnabled;
    public int         dataPurgeDays;

    public String dataMysqlUser;
    public String dataMysqlPassword;
    public String dataMysqlHost;
    public String dataMysqlBase;

    public ConfigManager(@NotNull P plugin) {
        super(plugin);
    }

    @Override
    protected void onLoad() {
        this.getConfig().remove("core");
        this.getConfig().remove("data");

        this.getConfig().addMissing("Plugin.Name", plugin.getName());
        this.getConfig().addMissing("Plugin.Prefix", "&e" + Placeholders.Plugin.NAME + " &8» &7");
        this.getConfig().addMissing("Plugin.Command_Aliases", plugin.getNameRaw());
        this.getConfig().addMissing("Plugin.Language", "en");

        this.pluginName = StringUtil.color(getConfig().getString("Plugin.Name", plugin.getName()));
        this.pluginPrefix = StringUtil.color(this.getConfig().getString("Plugin.Prefix", "&e" + Placeholders.Plugin.NAME + " &8» &7")
            .replace(Placeholders.Plugin.NAME, this.pluginName));
        this.commandAliases = getConfig().getString("Plugin.Command_Aliases", "").split(",");
        this.languageCode = getConfig().getString("Plugin.Language", "en").toLowerCase();

        if (this.plugin instanceof UserDataHolder) {
            this.getConfig().addMissing("Database.Auto_Save_Interval", 20);
            this.getConfig().addMissing("Database.Instant_Save", false);
            this.getConfig().addMissing("Database.Sync_Interval", 60);
            this.getConfig().addMissing("Database.Type", StorageType.SQLITE.name());
            this.getConfig().addMissing("Database.MySQL.Username", "root");
            this.getConfig().addMissing("Database.MySQL.Password", "root");
            this.getConfig().addMissing("Database.MySQL.Host", "localhost");
            this.getConfig().addMissing("Database.MySQL.Database", "minecraft");
            this.getConfig().addMissing("Database.Purge.Enabled", false);
            this.getConfig().addMissing("Database.Purge.For_Inactive_Days", 60);

            String path = "Database.";
            this.dataStorage = this.getConfig().getEnum(path + "Type", StorageType.class, StorageType.SQLITE);
            this.dataSaveInterval = this.getConfig().getInt(path + "Auto_Save_Interval", 20);
            this.dataSaveInstant = this.getConfig().getBoolean(path + "Instant_Save");
            this.dataSyncInterval = this.getConfig().getInt(path + "Sync_Interval");

            if (this.dataStorage == StorageType.MYSQL) {
                this.dataMysqlUser = this.getConfig().getString(path + "MySQL.Username");
                this.dataMysqlPassword = this.getConfig().getString(path + "MySQL.Password");
                this.dataMysqlHost = this.getConfig().getString(path + "MySQL.Host");
                this.dataMysqlBase = this.getConfig().getString(path + "MySQL.Database");
            }

            path = "Database.Purge.";
            this.dataPurgeEnabled = getConfig().getBoolean(path + "Enabled");
            this.dataPurgeDays = getConfig().getInt(path + "For_Inactive_Days", 60);
        }

        this.getConfig().saveChanges();
    }

    @Override
    protected void onShutdown() {

    }

    @NotNull
    public JYML getConfig() {
        if (this.config == null) {
            this.config = JYML.loadOrExtract(this.plugin, "config.yml");
        }
        return this.config;
    }

    public final boolean isModuleEnabled(@NotNull AbstractModule<?> module) {
        return this.isModuleEnabled(module.getId());
    }

    public final boolean isModuleEnabled(@NotNull String module) {
        this.getConfig().addMissing("Modules." + module + ".Enabled", true);
        this.getConfig().saveChanges();
        return this.getConfig().getBoolean("Modules." + module + ".Enabled");
    }

    public final void disableModule(@NotNull AbstractModule<?> module) {
        this.getConfig().set("Modules." + module.getId() + ".Enabled", false);
        this.getConfig().saveChanges();
    }

    @NotNull
    public final String getModuleName(@NotNull AbstractModule<?> module) {
        this.getConfig().addMissing("Modules." + module.getId() + ".Name", StringUtil.capitalizeFully(module.getId().replace("_", " ")));
        this.getConfig().saveChanges();
        return this.getConfig().getString("Modules." + module.getId() + ".Name", module.getId());
    }

    public void extract(@NotNull String folder) {
        if (!folder.startsWith("/")) {
            folder = "/" + folder;
        }
        if (!folder.endsWith("/")) {
            folder += "/";
        }
        this.extractFullPath(plugin.getDataFolder() + folder);
    }

    public void extractFullPath(@NotNull String path) {
        this.extractFullPath(path, "yml", false);
    }

    public void extractFullPath(@NotNull String path, boolean override) {
        this.extractFullPath(path, "yml", override);
    }

    public void extractFullPath(@NotNull String path, @NotNull String extension) {
        this.extractFullPath(path, extension, false);
    }

    public void extractFullPath(@NotNull String path, @NotNull String extension, boolean override) {
        File f = new File(path);
        String jarPath = path.replace(plugin.getDataFolder() + "", "");
        if (jarPath.startsWith("/")) {
            jarPath = jarPath.substring(1);
        }
        if (jarPath.endsWith("/")) {
            jarPath = jarPath.substring(0, jarPath.length() - 1);
        }

        if (!f.exists()) {
            ResourceExtractor extract = new ResourceExtractor(plugin, f, jarPath, ".*\\.(" + extension + ")$");

            try {
                extract.extract(override, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void extractResource(@NotNull String toPath, @NotNull String jarPath, @NotNull String regex, boolean override) {
        if (jarPath.startsWith("/")) jarPath = jarPath.substring(1);
        if (jarPath.endsWith("/")) jarPath = jarPath.substring(0, jarPath.length() - 1);

        ResourceExtractor extract = new ResourceExtractor(plugin, new File(toPath), jarPath, regex);

        try {
            extract.extract(override, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

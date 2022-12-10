package su.nexmedia.engine.config;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.api.config.JYML;
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

    public ConfigManager(@NotNull P plugin) {
        super(plugin);
    }

    @Override
    protected void onLoad() {
        this.getConfig().addMissing("Plugin.Name", plugin.getName());
        this.getConfig().addMissing("Plugin.Prefix", "&e" + Placeholders.Plugin.NAME + " &8» &7");
        this.getConfig().addMissing("Plugin.Command_Aliases", plugin.getName().toLowerCase());
        this.getConfig().addMissing("Plugin.Language", "en");

        this.getConfig().setComments("Plugin.Name", "Localized plugin name. It's used in messages and with internal placeholders.");
        this.getConfig().setComments("Plugin.Prefix", "Plugin prefix. Used in messages.", "You can use " + Placeholders.Plugin.NAME_LOCALIZED + " placeholder for a plugin name.");
        this.getConfig().setComments("Plugin.Command_Aliases", "Command names that will be registered as main plugin commands.", "Do not leave this empty. Split multiple names with a comma.");
        this.getConfig().setComments("Plugin.Language", "Sets the plugin language.", "It will use language config from the /lang/ folder for specified language code.", "By default it's 'en', so 'messages_en.yml' will be used.");

        this.pluginName = StringUtil.color(getConfig().getString("Plugin.Name", plugin.getName()));
        this.pluginPrefix = StringUtil.color(this.getConfig().getString("Plugin.Prefix", "&e" + Placeholders.Plugin.NAME + " &8» &7")
            .replace(Placeholders.Plugin.NAME, this.pluginName));
        this.commandAliases = getConfig().getString("Plugin.Command_Aliases", "").split(",");
        this.languageCode = getConfig().getString("Plugin.Language", "en").toLowerCase();

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

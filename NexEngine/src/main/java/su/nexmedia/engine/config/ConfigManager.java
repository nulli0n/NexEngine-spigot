package su.nexmedia.engine.config;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.api.config.JOption;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.manager.AbstractManager;
import su.nexmedia.engine.lang.LangManager;
import su.nexmedia.engine.utils.Placeholders;
import su.nexmedia.engine.utils.ResourceExtractor;

import java.io.File;
import java.io.IOException;

public class ConfigManager<P extends NexPlugin<P>> extends AbstractManager<P> {

    private JYML     config;
    protected String filePath;

    public String   pluginName;
    public String   pluginPrefix;
    public String[] commandAliases;
    public String   languageCode;

    public ConfigManager(@NotNull P plugin) {
        super(plugin);
        this.filePath = "config.yml";
    }

    @Override
    protected void onLoad() {
        this.config = JYML.loadOrExtract(this.plugin, this.filePath);

        this.pluginName = JOption.create("Plugin.Name", plugin.getName(),
            "Localized plugin name. It's used in messages and with internal placeholders.")
            .read(config);

        this.pluginPrefix = JOption.create("Plugin.Prefix", "&e" + Placeholders.PLUGIN_NAME + " &8» &7",
            "Plugin prefix. Used in messages.",
            "You can use " + Placeholders.PLUGIN_NAME_LOCALIZED + " placeholder for a plugin name.")
            .read(config).replace(Placeholders.PLUGIN_NAME, this.pluginName);

        this.commandAliases = JOption.create("Plugin.Command_Aliases", plugin.getName().toLowerCase(),
            "Command names that will be registered as main plugin commands.",
            "Do not leave this empty. Split multiple names with a comma.")
            .read(config).split(",");

        this.languageCode = JOption.create("Plugin.Language", "en",
            "Sets the plugin language.",
            "It will use language config from the '" + LangManager.DIR_LANG + "' sub-folder for specified language code.",
            "By default it's 'en', so 'messages_en.yml' will be used.")
            .read(config).toLowerCase();

        this.config.saveChanges();
    }

    @Override
    protected void onShutdown() {

    }

    @NotNull
    public JYML getConfig() {
        return this.config;
    }

    public void extractResources(@NotNull String folder) {
        this.extractResources(folder,plugin.getDataFolder() + folder, false);
    }

    public void extractResources(@NotNull String jarPath, @NotNull String toPath) {
        this.extractResources(jarPath, toPath, false);
    }

    public void extractResources(@NotNull String jarPath, @NotNull String toPath, boolean override) {
        File destination = new File(toPath);
        if (destination.exists() && !override) return;

        if (jarPath.startsWith("/")) {
            jarPath = jarPath.substring(1);
        }
        if (jarPath.endsWith("/")) {
            jarPath = jarPath.substring(0, jarPath.length() - 1);
        }

        ResourceExtractor extract = ResourceExtractor.create(plugin, jarPath, destination);
        try {
            extract.extract(override);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}

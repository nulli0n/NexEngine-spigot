package su.nexmedia.engine.api.manager;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.api.config.JYML;

import java.io.File;

public abstract class AbstractConfigHolder<P extends NexPlugin<P>> implements ConfigHolder {

    protected final P      plugin;
    protected final JYML   cfg;
    private final   String id;

    public AbstractConfigHolder(@NotNull P plugin, @NotNull String filePath) {
        this(plugin, new JYML(new File(filePath)));
    }

    public AbstractConfigHolder(@NotNull P plugin, @NotNull JYML cfg) {
        this(plugin, cfg, cfg.getFile().getName().replace(".yml", "").toLowerCase());
    }

    public AbstractConfigHolder(@NotNull P plugin, @NotNull String filePath, @NotNull String id) {
        this(plugin, new JYML(new File(filePath)), id);
    }

    public AbstractConfigHolder(@NotNull P plugin, @NotNull JYML cfg, @NotNull String id) {
        this.plugin = plugin;
        this.cfg = cfg;
        this.id = id.toLowerCase().replace(" ", "_");
    }

    public abstract boolean load();

    public boolean reload() {
        return this.getConfig().reload() && this.load();
    }

    @NotNull
    public P plugin() {
        return this.plugin;
    }

    @NotNull
    public final String getId() {
        return this.id;
    }

    @NotNull
    public final JYML getConfig() {
        return this.cfg;
    }
}

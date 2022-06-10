package su.nexmedia.engine.api.module;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.api.manager.AbstractManager;
import su.nexmedia.engine.api.manager.ILogger;
import su.nexmedia.engine.core.config.CoreConfig;

import java.util.UUID;

public abstract class AbstractModule<P extends NexPlugin<P>> extends AbstractManager<P> implements ILogger {

    private final String id;
    private final String name;

    private boolean isFailed;
    private boolean isLoaded;

    @Deprecated
    public AbstractModule(@NotNull P plugin) {
        this(plugin, UUID.randomUUID().toString());
    }

    public AbstractModule(@NotNull P plugin, @NotNull String id) {
        super(plugin);
        this.id = id.toLowerCase();
        this.name = plugin.cfg().getModuleName(this);
        this.isLoaded = false;
        this.isFailed = false;
    }

    @Override
    public void setup() {
        if (this.isLoaded()) return;

        super.setup();

        if (this.isFailed) {
            this.shutdown();
            return;
        }
        this.isLoaded = true;
    }

    @Override
    public void shutdown() {
        super.shutdown();
        this.isLoaded = false;
        this.isFailed = false;
    }

    protected final void interruptLoad(@NotNull String error) {
        if (this.isLoaded()) return;
        this.error(error);
        this.isFailed = true;
    }

    public final boolean isEnabled() {
        return this.plugin.cfg().isModuleEnabled(this);
    }

    public final boolean isLoaded() {
        return this.isLoaded;
    }

    @NotNull
    public String getId() {
        return this.id;
    }

    @NotNull
    public final String getName() {
        return this.name;
    }

    @NotNull
    @Deprecated
    public String getVersion() {
        return "1.0";
    }

    /**
     * @return Local sub-path to the module folder, like /modules/MODULE_ID/
     */
    @NotNull
    public String getPath() {
        return CoreConfig.MODULES_PATH_INTERNAL + this.getId() + "/";
    }

    @NotNull
    public final String getFullPath() {
        return plugin.getDataFolder() + this.getPath();
    }

    @NotNull
    private String buildLog(@NotNull String msg) {
        return "[" + this.getName() + "] " + msg;
    }

    @Override
    public final void info(@NotNull String msg) {
        this.plugin.info(this.buildLog(msg));
    }

    @Override
    public final void warn(@NotNull String msg) {
        this.plugin.warn(this.buildLog(msg));
    }

    @Override
    public final void error(@NotNull String msg) {
        this.plugin.error(this.buildLog(msg));
    }
}

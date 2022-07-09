package su.nexmedia.engine.api.module;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.api.manager.AbstractManager;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class AbstractModuleManager<P extends NexPlugin<P>> extends AbstractManager<P> {

    public static final String DIR_NAME = "/modules/";

    private Map<String, AbstractModule<P>> modules;

    public AbstractModuleManager(@NotNull P plugin) {
        super(plugin);
    }

    @Override
    public void onLoad() {
        this.modules = new LinkedHashMap<>();
    }

    @Override
    public void onShutdown() {
        for (AbstractModule<P> module : new HashMap<>(this.modules).values()) {
            this.unregister(module);
        }
        this.modules.clear();
    }

    public abstract void loadModules();

    /**
     * @param module Module instance.
     * @return An object instance of registered module. Returns NULL if module
     * hasn't been registered.
     */
    @Nullable
    public <T extends AbstractModule<P>> T register(@NotNull T module) {
        if (!module.isEnabled()) return null;

        String id = module.getId();
        if (this.modules.containsKey(id)) {
            this.plugin.error("Could not register " + id + " module! Module with such id is already registered!");
            return null;
        }

        long loadTook = System.currentTimeMillis();
        module.setup();
        loadTook = System.currentTimeMillis() - loadTook;

        if (!module.isLoaded()) {
            this.plugin.error("Module not loaded: " + module.getName());
            return null;
        }

        this.plugin.info("Loaded module: " + module.getName() + " in " + loadTook + " ms.");
        this.modules.put(id.toLowerCase(), module);
        return module;
    }

    public void unregister(@NotNull AbstractModule<?> module) {
        String id = module.getId();
        if (this.modules.remove(id) != null) {
            this.plugin.info("Unloaded module: " + module.getName());
        }
        module.shutdown();
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <T extends AbstractModule<P>> T getModule(@NotNull Class<T> clazz) {
        for (AbstractModule<?> module : this.modules.values()) {
            if (clazz.isAssignableFrom(module.getClass())) {
                return (T) module;
            }
        }
        return null;
    }

    @Nullable
    public AbstractModule<P> getModule(@NotNull String id) {
        return this.modules.get(id.toLowerCase());
    }

    @NotNull
    public Collection<AbstractModule<P>> getModules() {
        return this.modules.values();
    }
}

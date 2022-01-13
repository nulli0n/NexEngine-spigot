package su.nexmedia.engine.module;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.api.manager.AbstractManager;
import su.nexmedia.engine.api.module.AbstractExternalModule;
import su.nexmedia.engine.api.module.AbstractExternalModule.LoadPriority;
import su.nexmedia.engine.api.module.AbstractModule;
import su.nexmedia.engine.core.config.CoreConfig;
import su.nexmedia.engine.utils.FileUtil;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ModuleManager<P extends NexPlugin<P>> extends AbstractManager<P> {

    private Map<String, AbstractModule<P>>  modules;
    private List<AbstractExternalModule<P>> externalCache;

    public ModuleManager(@NotNull P plugin) {
        super(plugin);
    }

    @Override
    public void onLoad() {
        this.modules = new LinkedHashMap<>();
        this.externalCache = new ArrayList<>();

        // Prepare external module instances from .jar files.
        //this.plugin.getConfigManager().extractFullPath(plugin.getDataFolder() + CoreConfig.MODULES_PATH_INTERNAL, "jar");
        FileUtil.getFiles(plugin.getDataFolder() + CoreConfig.MODULES_PATH_INTERNAL, false).forEach(file -> {
            AbstractExternalModule<P> module = this.loadFromFile(file);
            if (module != null) {
                this.externalCache.add(module);
            }
        });
        this.plugin.info("Found " + this.externalCache.size() + " external module(s).");
    }

    @Override
    public void onShutdown() {
        for (AbstractModule<P> module : new HashMap<>(this.modules).values()) {
            this.unregister(module);
        }
        this.modules.clear();
    }

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
            this.plugin.error("Module not loaded: " + module.getName() + " v" + module.getVersion());
            return null;
        }

        this.plugin.info("Loaded module: " + module.getName() + " v" + module.getVersion() + " in " + loadTook + " ms.");
        this.modules.put(id.toLowerCase(), module);
        return module;
    }

    public void registerExternal(@NotNull LoadPriority priority) {
        this.externalCache.removeIf(module -> {
            if (module.getPriority() == priority) {
                this.register(module);
                return true;
            }
            return false;
        });
    }

    public void unregister(@NotNull AbstractModule<?> module) {
        String id = module.getId();
        if (this.modules.remove(id) != null) {
            this.plugin.info("Unloaded module: " + module.getName() + " v" + module.getVersion());
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

    @SuppressWarnings({"resource", "unchecked"})
    @Nullable
    public AbstractExternalModule<P> loadFromFile(@NotNull File jar) {
        if (!jar.getName().endsWith(".jar")) return null;

        try {
            JarFile jarFile = new JarFile(jar);
            Enumeration<JarEntry> jarEntry = jarFile.entries();
            URL[] urls = {new URL("jar:file:" + jar.getPath() + "!/")};
            ClassLoader loader = URLClassLoader.newInstance(urls, plugin.getClazzLoader());

            while (jarEntry.hasMoreElements()) {
                JarEntry entry = jarEntry.nextElement();
                if (entry.isDirectory() || !entry.getName().endsWith(".class")) continue;

                String className = entry.getName().substring(0, entry.getName().length() - 6).replace('/', '.');
                Class<?> clazz = Class.forName(className, false, loader); // second was 'true'
                if (AbstractExternalModule.class.isAssignableFrom(clazz)) {
                    Class<? extends AbstractExternalModule<P>> mainClass = (Class<? extends AbstractExternalModule<P>>) clazz.asSubclass(AbstractExternalModule.class);
                    Constructor<? extends AbstractExternalModule<P>> con = mainClass.getConstructor(plugin.getClass());
                    return con.newInstance(plugin);
                }
            }
            // jarFile.close();
        } catch (Exception e) {
            this.plugin.error("Could not load external module: " + jar.getName());
            e.printStackTrace();
        }
        return null;
    }
}

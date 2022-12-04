package su.nexmedia.engine.hooks;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.NexEngine;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.api.hook.AbstractHook;
import su.nexmedia.engine.api.manager.AbstractManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Deprecated
public class HookManager extends AbstractManager<NexEngine> {

    private Map<String, Set<AbstractHook<?>>> hooks;

    public HookManager(@NotNull NexEngine plugin) {
        super(plugin);
    }

    @Override
    protected void onLoad() {
        this.hooks = new HashMap<>();
    }

    @Override
    protected void onShutdown() {
        this.plugin.getChildrens().forEach(this::shutdown);
    }

    public void shutdown(@NotNull NexPlugin<?> holder) {
        this.getHooks(holder).forEach(AbstractHook::shutdown);
        this.hooks.remove(holder.getName());
    }

    @Nullable
    public <T extends AbstractHook<?>> T register(@NotNull NexPlugin<?> holder, @NotNull String pluginName, @NotNull Class<T> clazz) {
        if (!Hooks.hasPlugin(pluginName)) return null;

        T hook;
        try {
            hook = clazz.getConstructor(holder.getClass(), String.class).newInstance(holder, pluginName);
            return this.register(holder, hook);
        }
        catch (Exception /*| NoClassDefFoundError*/ e) {
            holder.error("Could not initialize hook for '" + clazz.getSimpleName() + "' !");
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    private <T extends AbstractHook<?>> T register(@NotNull NexPlugin<?> holder, @NotNull T hook) {
        if (!hook.setup()) {
            holder.error("Unable to hook with " + hook.getPluginName() + ".");
            return null;
        }

        // Register only successful hooks.
        this.getHooks(holder).add(hook);
        holder.info("Successfully hooked with " + hook.getPluginName() + "!");
        return hook;
    }

    @NotNull
    public Set<AbstractHook<?>> getHooks(@NotNull NexPlugin<?> holder) {
        return this.hooks.computeIfAbsent(holder.getName(), hooks -> new HashSet<>());
    }

    @Nullable
    public <T extends AbstractHook<?>> T getHook(@NotNull NexPlugin<?> holder, @NotNull Class<T> clazz) {
        for (AbstractHook<?> hook : this.getHooks(holder)) {
            if (clazz.isAssignableFrom(hook.getClass())) {
                return clazz.cast(hook);
            }
        }
        return holder.isEngine() ? null : this.getHook(this.plugin, clazz);
    }

    @Nullable
    public AbstractHook<?> getHook(@NotNull NexPlugin<?> holder, @NotNull String name) {
        for (AbstractHook<?> hook : this.getHooks(holder)) {
            if (hook.getPluginName().equalsIgnoreCase(name)) {
                return hook;
            }
        }
        return holder.isEngine() ? null : this.getHook(this.plugin, name);
    }

    public boolean isHooked(@NotNull NexPlugin<?> holder, @NotNull Class<? extends AbstractHook<?>> clazz) {
        return this.getHook(holder, clazz) != null;
    }

    public boolean isHooked(@NotNull NexPlugin<?> holder, @NotNull String name) {
        return this.getHook(holder, name) != null;
    }
}

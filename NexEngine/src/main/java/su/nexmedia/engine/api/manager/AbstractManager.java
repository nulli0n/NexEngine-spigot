package su.nexmedia.engine.api.manager;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexPlugin;

import java.util.HashSet;
import java.util.Set;

public abstract class AbstractManager<P extends NexPlugin<P>> {

    protected final P                  plugin;
    protected final Set<EventListener> listeners;

    public AbstractManager(@NotNull P plugin) {
        this.plugin = plugin;
        this.listeners = new HashSet<>();
    }

    public void setup() {
        this.onLoad();
    }

    public void shutdown() {
        this.listeners.forEach(EventListener::unregisterListeners);
        this.listeners.clear();
        this.onShutdown();
    }

    public void reload() {
        this.shutdown();
        this.setup();
    }

    protected abstract void onLoad();

    protected abstract void onShutdown();

    /**
     * Adds listener to this manager. Listener will be registered when it's added.
     *
     * @param listener Listener to add.
     */
    protected void addListener(@NotNull EventListener listener) {
        if (this.listeners.add(listener)) {
            listener.registerListeners();
        }
    }

    @NotNull
    public P plugin() {
        return this.plugin;
    }
}

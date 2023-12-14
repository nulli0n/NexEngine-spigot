package su.nexmedia.engine.api.manager;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.utils.values.UniTask;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractManager<P extends NexPlugin<P>> {

    protected P plugin;
    protected final Set<EventListener> listeners;
    protected final List<UniTask> tasks;

    public AbstractManager(@NotNull P plugin) {
        this.plugin = plugin;
        this.listeners = new HashSet<>();
        this.tasks = new ArrayList<>();
    }

    public void setup() {
        this.onLoad();
    }

    public void shutdown() {
        this.tasks.forEach(UniTask::stop);
        this.tasks.clear();
        this.listeners.forEach(EventListener::unregisterListeners);
        this.listeners.clear();
        this.onShutdown();
    }

    public void reload() {
        this.shutdown();
        this.setup();
    }

    @NotNull
    public P plugin() {
        return this.plugin;
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

    protected void addTask(@NotNull UniTask.Builder builder) {
        this.addTask(builder.build());
    }

    protected void addTask(@NotNull UniTask task) {
        this.tasks.add(task);
        task.start();
    }
}

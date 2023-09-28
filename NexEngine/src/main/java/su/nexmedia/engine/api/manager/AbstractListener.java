package su.nexmedia.engine.api.manager;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexPlugin;

public abstract class AbstractListener<P extends NexPlugin<P>> implements EventListener {

    @NotNull
    public final P plugin;

    public AbstractListener(@NotNull P plugin) {
        this.plugin = plugin;
    }

    @Override
    public void registerListeners() {
        this.plugin.getPluginManager().registerEvents(this, this.plugin);
    }
}

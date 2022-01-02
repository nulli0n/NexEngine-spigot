package su.nexmedia.engine.api.data.event;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.api.data.AbstractUser;

public abstract class EngineUserEvent<P extends NexPlugin<P>, U extends AbstractUser<P>> extends Event {

    private final NexPlugin<?> plugin;
    private final U            user;

    public EngineUserEvent(@NotNull P plugin, @NotNull U user) {
        this.plugin = plugin;
        this.user = user;
    }

    @NotNull
    public NexPlugin<?> getPlugin() {
        return this.plugin;
    }

    @NotNull
    public U getUser() {
        return this.user;
    }
}

package su.nexmedia.engine.api.data.event;

import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.api.data.AbstractUser;

public class EngineUserLoadEvent<P extends NexPlugin<P>, U extends AbstractUser<P>> extends EngineUserEvent<P, U> {

    private static final HandlerList handlerList = new HandlerList();

    public EngineUserLoadEvent(@NotNull P plugin, @NotNull U user) {
        super(plugin, user);
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }
}

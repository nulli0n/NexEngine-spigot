package su.nexmedia.engine.api.packet.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class EnginePlayerPacketEvent extends EnginePacketEvent {

    private static final HandlerList handlerList = new HandlerList();

    public EnginePlayerPacketEvent(@NotNull Player reciever, @NotNull Object packet) {
        super(reciever, packet);
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

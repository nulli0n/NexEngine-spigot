package su.nexmedia.engine.api.packet;

import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.api.packet.event.EnginePlayerPacketEvent;
import su.nexmedia.engine.api.packet.event.EngineServerPacketEvent;

public interface IPacketHandler {

    void managePlayerPacket(@NotNull EnginePlayerPacketEvent event);

    void manageServerPacket(@NotNull EngineServerPacketEvent event);
}

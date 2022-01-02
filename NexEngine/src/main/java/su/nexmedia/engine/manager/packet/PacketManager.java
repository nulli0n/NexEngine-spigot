package su.nexmedia.engine.manager.packet;

import io.netty.channel.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexEngine;
import su.nexmedia.engine.api.manager.AbstractListener;
import su.nexmedia.engine.api.manager.AbstractManager;
import su.nexmedia.engine.api.packet.IPacketHandler;
import su.nexmedia.engine.api.packet.event.EnginePlayerPacketEvent;
import su.nexmedia.engine.api.packet.event.EngineServerPacketEvent;

import java.util.HashSet;
import java.util.Set;

public class PacketManager extends AbstractManager<NexEngine> {

    protected static final Set<IPacketHandler> PACKET_HANDLERS = new HashSet<>();
    private static final   String              INJECTOR_ID     = "nex_handler";

    public PacketManager(@NotNull NexEngine plugin) {
        super(plugin);
    }

    @Override
    public final void onLoad() {
        this.addListener(new Listener(plugin));
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            this.injectPlayer(player);
        }
    }

    @Override
    public final void onShutdown() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            this.removePlayer(player);
        }
        PACKET_HANDLERS.clear();
    }

    public void registerHandler(@NotNull IPacketHandler ipr) {
        PACKET_HANDLERS.add(ipr);
    }

    public void unregisterHandler(@NotNull IPacketHandler ipr) {
        PACKET_HANDLERS.remove(ipr);
    }

    @NotNull
    public Set<IPacketHandler> getHandlers() {
        return PACKET_HANDLERS;
    }

    public Channel getChannel(@NotNull Player player) {
        return this.plugin.getNMS().getChannel(player);
    }

    public void sendPacket(@NotNull Player player, @NotNull Object packet) {
        this.plugin.getNMS().sendPacket(player, packet);
    }

    private final void removePlayer(@NotNull Player player) {
        Channel channel = this.getChannel(player);
        if (channel.pipeline().get(INJECTOR_ID) != null) {
            channel.pipeline().remove(INJECTOR_ID);
        }
        /*
         * channel.eventLoop().submit(() -> { channel.pipeline().remove(INJECTOR_ID);
         * return null; });
         */
    }

    private final void injectPlayer(@NotNull Player player) {
        ChannelPipeline pipe = this.getChannel(player).pipeline();
        if (pipe.get(INJECTOR_ID) != null) return;

        ChannelDuplexHandler cdx = new ChannelDuplexHandler() {

            // From Player to Server (In)
            @Override
            public void channelRead(ChannelHandlerContext cont, Object packet) throws Exception {
                EngineServerPacketEvent e = new EngineServerPacketEvent(player, packet);
                plugin.getPluginManager().callEvent(e);
                if (e.isCancelled())
                    return;

                // System.out.print("PACKET IN: " + packet.toString());
                super.channelRead(cont, e.getPacket());
            }

            // From Server to Player (Out)
            @Override
            public void write(ChannelHandlerContext cont, Object packet, ChannelPromise prom) throws Exception {
                EnginePlayerPacketEvent e = new EnginePlayerPacketEvent(player, packet);
                plugin.getPluginManager().callEvent(e);
                if (e.isCancelled())
                    return;

                super.write(cont, e.getPacket(), prom);
            }
        };

        if (pipe.get("packet_handler") == null) {
            this.plugin.warn("No packet handler found for " + player.getName());
            return;
        }
        try {
            pipe.addBefore("packet_handler", INJECTOR_ID, cdx);
        }
        catch (Exception ex) {
            this.plugin.error("Could not add packet listener for " + player.getName() + " !");
            //ex.printStackTrace();
        }
    }

    class Listener extends AbstractListener<NexEngine> {

        public Listener(@NotNull NexEngine plugin) {
            super(plugin);
        }

        @EventHandler(priority = EventPriority.NORMAL)
        public void onJoin(PlayerJoinEvent e) {
            injectPlayer(e.getPlayer());
        }

        @EventHandler(priority = EventPriority.NORMAL)
        public void onQuit(PlayerQuitEvent e) {
            removePlayer(e.getPlayer());
        }

        @EventHandler(priority = EventPriority.NORMAL)
        public void onPacketOut(EnginePlayerPacketEvent e) {
            for (IPacketHandler handler : getHandlers()) {
                handler.managePlayerPacket(e);
            }
        }

        @EventHandler(priority = EventPriority.NORMAL)
        public void onPacketIn(EngineServerPacketEvent e) {
            for (IPacketHandler handler : getHandlers()) {
                handler.manageServerPacket(e);
            }
        }
    }
}

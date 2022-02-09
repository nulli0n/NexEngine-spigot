package su.nexmedia.engine.manager.player;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexEngine;
import su.nexmedia.engine.api.manager.AbstractManager;

@Deprecated
public class PlayerManager extends AbstractManager<NexEngine> {

    public PlayerManager(@NotNull NexEngine plugin) {
        super(plugin);
    }

    @Override
    protected void onLoad() {

    }

    @Override
    protected void onShutdown() {
        PlayerBlockTracker.shutdown();
    }

    @Deprecated
    public void enableUserBlockListening() {
        PlayerBlockTracker.initialize();
    }
}

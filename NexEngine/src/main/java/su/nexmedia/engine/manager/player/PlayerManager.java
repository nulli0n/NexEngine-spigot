package su.nexmedia.engine.manager.player;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexEngine;
import su.nexmedia.engine.api.manager.AbstractManager;
import su.nexmedia.engine.manager.player.listener.PlayerBlockPlacedListener;

public class PlayerManager extends AbstractManager<NexEngine> {

    private boolean isUserBlockPlaceListening;

    public PlayerManager(@NotNull NexEngine plugin) {
        super(plugin);
    }

    @Override
    protected void onLoad() {

    }

    @Override
    protected void onShutdown() {

    }

    public void enableUserBlockListening() {
        if (this.isUserBlockPlaceListening()) return;

        this.isUserBlockPlaceListening = true;
        this.addListener(new PlayerBlockPlacedListener(this.plugin));
        this.plugin.info("Enabled listener for player placed blocks.");
    }

    public boolean isUserBlockPlaceListening() {
        return isUserBlockPlaceListening;
    }
}

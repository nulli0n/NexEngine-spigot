package su.nexmedia.engine.api.menu.impl;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexEngine;
import su.nexmedia.engine.api.server.AbstractTask;

public class MenuRefreshTask extends AbstractTask<NexEngine> {

    private long count;

    public MenuRefreshTask(@NotNull NexEngine plugin) {
        super(plugin, 1, false);
        this.count = 0L;
    }

    @Override
    public void action() {
        if (this.count >= Integer.MAX_VALUE) this.count = 0L;

        Menu.PLAYER_MENUS.values().forEach(menu -> {
            int ref = menu.getOptions().getAutoRefresh();
            if (ref > 0 && this.count % ref == 0) {
                menu.update();
            }
        });

        this.count++;
    }

    @Override
    public boolean stop() {
        this.count = 0L;
        return super.stop();
    }
}

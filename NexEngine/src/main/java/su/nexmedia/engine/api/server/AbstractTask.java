package su.nexmedia.engine.api.server;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexPlugin;

public abstract class AbstractTask<P extends NexPlugin<P>> {

    @NotNull protected final P plugin;

    protected int     taskId;
    protected long    interval;
    protected boolean async;

    public AbstractTask(@NotNull P plugin, int interval, boolean async) {
        this(plugin, interval * 20L, async);
    }

    public AbstractTask(@NotNull P plugin, long interval, boolean async) {
        this.plugin = plugin;
        this.interval = interval;
        this.async = async;
        this.taskId = -1;
    }

    public abstract void action();

    public final void restart() {
        this.stop();
        this.start();
    }

    public boolean start() {
        if (this.taskId >= 0) return false;
        if (this.interval <= 0L) return false;

        if (this.async) {
            this.taskId = plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, this::action, 1L, interval).getTaskId();
        }
        else {
            this.taskId = plugin.getServer().getScheduler().runTaskTimer(this.plugin, this::action, 1L, interval).getTaskId();
        }
        return true;
    }

    public boolean stop() {
        if (this.taskId < 0) return false;

        this.plugin.getServer().getScheduler().cancelTask(this.taskId);
        this.taskId = -1;
        return true;
    }
}

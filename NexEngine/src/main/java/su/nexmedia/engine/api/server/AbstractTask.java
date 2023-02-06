package su.nexmedia.engine.api.server;

import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexPlugin;

public abstract class AbstractTask<P extends NexPlugin<P>> {

    @NotNull protected final P plugin;

    protected BukkitTask task;
    protected long    interval;
    protected boolean async;

    public AbstractTask(@NotNull P plugin, int interval, boolean async) {
        this(plugin, interval * 20L, async);
    }

    public AbstractTask(@NotNull P plugin, long interval, boolean async) {
        this.plugin = plugin;
        this.interval = interval;
        this.async = async;
    }

    public abstract void action();

    public final void restart() {
        this.stop();
        this.start();
    }

    public boolean start() {
        if (this.task != null || this.interval <= 0L) return false;

        if (this.async) {
            this.plugin.runTaskTimerAsync(task -> {
                this.task = task;
                this.action();
            }, 0L, this.interval);
        }
        else {
            this.plugin.runTaskTimer(task -> {
                this.task = task;
                this.action();
            }, 0L, this.interval);
        }
        return true;
    }

    public boolean stop() {
        if (this.task == null || this.task.isCancelled()) return false;
        this.task.cancel();
        return true;
    }
}

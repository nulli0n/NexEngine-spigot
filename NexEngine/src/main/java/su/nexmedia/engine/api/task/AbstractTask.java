package su.nexmedia.engine.api.task;

import org.jetbrains.annotations.NotNull;

import su.nexmedia.engine.NexPlugin;

@Deprecated
public abstract class AbstractTask<P extends NexPlugin<P>> {

    @NotNull
    protected P       plugin;
    protected int     id;
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

    public void start() {
        if (this.interval <= 0) return;

        if (async) {
            this.async();
        }
        else {
            this.sync();
        }
    }

    private void sync() {
        this.id = plugin.getServer().getScheduler().runTaskTimer(plugin, this::action, 1L, interval).getTaskId();
    }

    private void async() {
        this.id = plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, this::action, 1L, interval).getTaskId();
    }

    public void stop() {
        if (this.interval <= 0) return;
        this.plugin.getServer().getScheduler().cancelTask(this.getId());
    }

    public int getId() {
        return id;
    }
}

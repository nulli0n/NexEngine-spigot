package su.nexmedia.engine.api.data.task;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.api.data.AbstractDataHandler;
import su.nexmedia.engine.api.task.AbstractTask;

public class DataSaveTask<P extends NexPlugin<P>> extends AbstractTask<P> {

    private final AbstractDataHandler<P> dataHandler;

    public DataSaveTask(@NotNull AbstractDataHandler<P> dataHandler) {
        super(dataHandler.plugin(), dataHandler.getConfig().saveInterval * 60, true);
        this.dataHandler = dataHandler;
    }

    @Override
    public void action() {
        this.dataHandler.onSave();
    }
}

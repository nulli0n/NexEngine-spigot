package su.nexmedia.engine.api.module;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexPlugin;

public abstract class AbstractExternalModule<P extends NexPlugin<P>> extends AbstractModule<P> {

    public AbstractExternalModule(@NotNull P plugin) {
        super(plugin);
    }

    @NotNull
    public abstract LoadPriority getPriority();

    /*@Override
    @NotNull
    public String getPath() {
        return CoreConfig.MODULES_PATH_EXTERNAL + this.getId() + "/";
    }*/

    public enum LoadPriority {
        HIGH, LOW
    }
}

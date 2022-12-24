package su.nexmedia.engine.api.module;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.api.command.GeneralCommand;

@Deprecated
public abstract class AbstractModuleCommand<P extends NexPlugin<P>, M extends AbstractModule<P>> extends GeneralCommand<P> {

    protected M module;

    public AbstractModuleCommand(@NotNull M module, @NotNull String[] labels) {
        this(module, labels, null);
    }

    public AbstractModuleCommand(@NotNull M module, @NotNull String[] labels, @Nullable String permission) {
        super(module.plugin(), labels, permission);
        this.module = module;
    }
}

package su.nexmedia.engine.api.module;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.api.command.AbstractCommand;

import java.util.Arrays;
import java.util.List;

@Deprecated
public abstract class AbstractModuleSubCommand<P extends NexPlugin<P>, M extends AbstractModule<P>> extends AbstractCommand<P> {

    protected M module;

    public AbstractModuleSubCommand(@NotNull M module, @NotNull String[] aliases) {
        this(module, Arrays.asList(aliases));
    }

    public AbstractModuleSubCommand(@NotNull M module, @NotNull List<String> aliases) {
        this(module, aliases, null);
    }

    public AbstractModuleSubCommand(@NotNull M module, @NotNull String[] aliases, @Nullable String permission) {
        this(module, Arrays.asList(aliases), permission);
    }

    public AbstractModuleSubCommand(@NotNull M module, @NotNull List<String> aliases, @Nullable String permission) {
        super(module.plugin(), aliases, permission);
        this.module = module;
    }

    @NotNull
    public M getModule() {
        return this.module;
    }
}

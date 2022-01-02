package su.nexmedia.engine.api.data;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexPlugin;

public interface UserDataHolder<P extends NexPlugin<P>, U extends AbstractUser<P>> {

    boolean setupDataHandlers();

    @NotNull AbstractUserDataHandler<P, U> getData();

    @NotNull AbstractUserManager<P, U> getUserManager();
}

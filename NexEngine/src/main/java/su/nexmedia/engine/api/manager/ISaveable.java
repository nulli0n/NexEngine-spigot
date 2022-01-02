package su.nexmedia.engine.api.manager;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;

@Deprecated
public interface ISaveable {

    void save(@NotNull JYML cfg);
}

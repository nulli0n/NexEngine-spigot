package su.nexmedia.engine.api.config;

import org.jetbrains.annotations.NotNull;

@Deprecated
public interface JWriter {

    void write(@NotNull JYML cfg, @NotNull String path);
}

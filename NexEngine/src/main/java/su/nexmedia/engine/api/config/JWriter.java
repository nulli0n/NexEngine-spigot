package su.nexmedia.engine.api.config;

import org.jetbrains.annotations.NotNull;

public interface JWriter {

    void write(@NotNull JYML cfg, @NotNull String path);
}

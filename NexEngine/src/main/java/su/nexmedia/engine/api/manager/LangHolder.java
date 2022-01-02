package su.nexmedia.engine.api.manager;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.LangTemplate;

public interface LangHolder {

    @NotNull LangTemplate getLang();
}

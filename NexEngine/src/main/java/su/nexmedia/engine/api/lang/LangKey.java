package su.nexmedia.engine.api.lang;

import org.jetbrains.annotations.NotNull;

public class LangKey {

    private final String path;
    private final String defaultText;

    public LangKey(@NotNull String path, @NotNull String defaultText) {
        this.path = path;
        this.defaultText = defaultText;
    }

    @NotNull
    public static LangKey of(@NotNull String path, @NotNull String defaultText) {
        return new LangKey(path, defaultText);
    }

    @NotNull
    public String getPath() {
        return path;
    }

    @NotNull
    public String getDefaultText() {
        return defaultText;
    }
}

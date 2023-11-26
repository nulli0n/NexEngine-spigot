package su.nexmedia.engine.api.config;

import org.jetbrains.annotations.NotNull;

public class ConfigKey<T> {

    private final String            path;
    private final JOption.Writer<T> writer;
    private final JOption.Reader<T> reader;

    public ConfigKey(@NotNull String path, JOption.Writer<T> writer, JOption.Reader<T> reader) {
        this.path = path;
        this.writer = writer;
        this.reader = reader;
    }

    public static ConfigKey<Integer> asInt(@NotNull String path) {
        return new ConfigKey<>(path, JYML::set, JOption.READER_INT);
    }

    public T read(@NotNull JYML cfg, @NotNull T defaultValue) {
        return this.getReader().read(cfg, this.getPath(), defaultValue);
    }

    public void write(@NotNull JYML cfg, @NotNull T obj) {
        this.getWriter().write(cfg, this.getPath(), obj);
    }

    @NotNull
    public String getPath() {
        return path;
    }

    @NotNull
    public JOption.Reader<T> getReader() {
        return reader;
    }

    @NotNull
    public JOption.Writer<T> getWriter() {
        return writer;
    }
}

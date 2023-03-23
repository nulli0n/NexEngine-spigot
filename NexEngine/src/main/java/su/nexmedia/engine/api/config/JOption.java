package su.nexmedia.engine.api.config;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.api.particle.SimpleParticle;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class JOption<T> {

    public static final Reader<Boolean>      READER_BOOLEAN     = JYML::getBoolean;
    public static final Reader<Integer>      READER_INT         = JYML::getInt;
    public static final Reader<Double>       READER_DOUBLE      = JYML::getDouble;
    public static final Reader<Long>         READER_LONG        = JYML::getLong;
    public static final Reader<String>       READER_STRING      = JYML::getString;
    public static final Reader<Set<String>>  READER_SET_STRING  = (cfg, path, def) -> cfg.getStringSet(path);
    public static final Reader<List<String>> READER_LIST_STRING = (cfg, path, def) -> cfg.getStringList(path);
    public static final Reader<ItemStack>    READER_ITEM        = JYML::getItem;

    protected final String    path;
    protected final T         defaultValue;
    protected final String[]  description;
    protected T         value;
    protected Writer<T> writer;
    protected Reader<T> reader;

    public JOption(@NotNull String path, @NotNull Reader<T> reader, @NotNull Supplier<T> defaultValue, @NotNull String... description) {
        this(path, reader, defaultValue.get(), description);
    }

    public JOption(@NotNull String path, @NotNull Reader<T> reader, @NotNull T defaultValue, @NotNull String... description) {
        this.path = path;
        this.description = description;
        this.reader = reader;
        this.defaultValue = defaultValue;
    }

    @NotNull
    public static JOption<Boolean> create(@NotNull String path, boolean defaultValue, @NotNull String... description) {
        return new JOption<>(path, READER_BOOLEAN, defaultValue, description);
    }

    @NotNull
    public static JOption<Integer> create(@NotNull String path, int defaultValue, @NotNull String... description) {
        return new JOption<>(path, READER_INT, defaultValue, description);
    }

    @NotNull
    public static JOption<Double> create(@NotNull String path, double defaultValue, @NotNull String... description) {
        return new JOption<>(path, READER_DOUBLE, defaultValue, description);
    }

    @NotNull
    public static JOption<Long> create(@NotNull String path, long defaultValue, @NotNull String... description) {
        return new JOption<>(path, READER_LONG, defaultValue, description);
    }

    @NotNull
    public static JOption<String> create(@NotNull String path, @NotNull String defaultValue, @NotNull String... description) {
        return new JOption<>(path, READER_STRING, defaultValue, description);
    }

    @NotNull
    public static JOption<List<String>> create(@NotNull String path, @NotNull List<String> defaultValue, @NotNull String... description) {
        return new JOption<>(path, READER_LIST_STRING, defaultValue, description);
    }

    @NotNull
    public static JOption<Set<String>> create(@NotNull String path, @NotNull Set<String> defaultValue, @NotNull String... description) {
        return new JOption<>(path, READER_SET_STRING, defaultValue, description);
    }

    @NotNull
    public static JOption<ItemStack> create(@NotNull String path, @NotNull ItemStack defaultValue, @NotNull String... description) {
        return new JOption<>(path, READER_ITEM, defaultValue, description);
    }

    @NotNull
    public static <E extends Enum<E>> JOption<E> create(@NotNull String path, @NotNull Class<E> clazz, @NotNull E defaultValue, @NotNull String... description) {
        return new JOption<>(path, ((cfg, path1, def) -> cfg.getEnum(path1, clazz, defaultValue)), defaultValue, description)
            .setWriter((cfg, path1, type) -> cfg.set(path1, type.name()));
    }

    @NotNull
    public static JOption<SimpleParticle> create(@NotNull String path, @NotNull SimpleParticle defaulValue, @NotNull String... description) {
        return new JOption<>(path, (cfg, path1, def) -> SimpleParticle.read(cfg, path1), defaulValue, description)
            .setWriter((cfg, path1, particle) -> SimpleParticle.write(particle, cfg, path1));
    }

    @NotNull
    public T read(@NotNull JYML cfg) {
        if (!cfg.contains(this.getPath())) {
            this.write(cfg);
        }
        cfg.setComments(this.getPath(), this.getDescription());
        return (this.value = this.reader.read(cfg, this.getPath(), this.getDefaultValue()));
    }

    public void write(@NotNull JYML cfg) {
        if (this.getWriter() != null) {
            this.getWriter().write(cfg, this.getPath(), this.get());
        }
        else {
            cfg.set(this.getPath(), this.get());
        }
    }

    public boolean remove(@NotNull JYML cfg) {
        return cfg.remove(this.getPath());
    }

    @NotNull
    public String getPath() {
        return path;
    }

    @NotNull
    public String[] getDescription() {
        return description;
    }

    @NotNull
    @Deprecated
    public JOption.Reader<T> getValueLoader() {
        return this.getReader();
    }

    @NotNull
    public Reader<T> getReader() {
        return reader;
    }

    @NotNull
    public JOption<T> mapReader(@NotNull UnaryOperator<T> operator) {
        if (this.reader == null) return this;

        Reader<T> readerHas = this.reader;
        this.reader = (cfg, path1, def) -> operator.apply(readerHas.read(cfg, path1, def));
        return this;
    }

    @NotNull
    public T getDefaultValue() {
        return defaultValue;
    }

    @NotNull
    public T get() {
        return this.value == null ? this.getDefaultValue() : this.value;
    }

    public void set(@NotNull T value) {
        this.value = value;
    }

    @Nullable
    public JOption.Writer<T> getWriter() {
        return writer;
    }

    @NotNull
    public JOption<T> setWriter(@Nullable JOption.Writer<T> writer) {
        this.writer = writer;
        return this;
    }

    public interface Reader<T> {

        @NotNull T read(@NotNull JYML cfg, @NotNull String path, @NotNull T def);
    }

    public interface Writer<T> {

        void write(@NotNull JYML cfg, @NotNull String path, @NotNull T obj);
    }
}

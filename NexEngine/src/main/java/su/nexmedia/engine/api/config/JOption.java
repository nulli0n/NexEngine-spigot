package su.nexmedia.engine.api.config;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.utils.StringUtil;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class JOption<T> {

    public static final Reader<Boolean>      READER_BOOLEAN     = JYML::getBoolean;
    public static final Reader<Integer>      READER_INT         = JYML::getInt;
    public static final Reader<Double>       READER_DOUBLE      = JYML::getDouble;
    public static final Reader<Long>         READER_LONG        = JYML::getLong;
    public static final Reader<String>       READER_STRING      = (cfg, path, def) -> {
        if (!path.startsWith("Database.")) {
            return StringUtil.color(cfg.getString(path, def));
        } else {
            return cfg.getString(path, def);
        }
    };
    public static final Reader<Set<String>>  READER_SET_STRING  = (cfg, path, def) -> StringUtil.color(cfg.getStringSet(path));
    public static final Reader<List<String>> READER_LIST_STRING = (cfg, path, def) -> StringUtil.color(cfg.getStringList(path));
    public static final Reader<ItemStack>    READER_ITEM        = JYML::getItem;

    protected final Reader<T> reader;
    protected final String    path;
    protected final T         defaultValue;
    protected final String[]  description;
    protected       T         value;
    protected       Writer    writer;

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
        return new JOption<>(path, ((cfg, path1, def) -> cfg.getEnum(path1, clazz, defaultValue)), defaultValue, description);
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
            this.getWriter().write(cfg, this.getPath());
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
    public JOption.Reader<T> getValueLoader() {
        return reader;
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
    public Writer getWriter() {
        return writer;
    }

    @NotNull
    public JOption<T> setWriter(@Nullable Writer writer) {
        this.writer = writer;
        return this;
    }

    public interface Reader<T> {

        @NotNull T read(@NotNull JYML cfg, @NotNull String path, @NotNull T def);
    }

    public interface Writer {

        void write(@NotNull JYML cfg, @NotNull String path);
    }
}

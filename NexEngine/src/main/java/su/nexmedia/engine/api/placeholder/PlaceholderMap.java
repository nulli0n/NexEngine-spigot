package su.nexmedia.engine.api.placeholder;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.Pair;
import su.nexmedia.engine.utils.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class PlaceholderMap {

    private final List<Pair<String, Supplier<String>>> keys;

    public PlaceholderMap() {
        this.keys = new ArrayList<>();
    }

    @NotNull
    public PlaceholderMap add(@NotNull String key, @NotNull Supplier<String> replacer) {
        this.keys.add(Pair.of(key, replacer));
        return this;
    }

    public void clear() {
        this.keys.clear();
    }

    @NotNull
    public UnaryOperator<String> replacer() {
        return str -> StringUtil.replaceEach(str, this.keys);
    }
}

package su.nexmedia.engine.api.manager;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.Pair;
import su.nexmedia.engine.utils.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

public class PlaceholderMap {

    private final List<Pair<String, String>> keys;

    public PlaceholderMap() {
        this.keys = new ArrayList<>();
    }

    @NotNull
    public PlaceholderMap add(@NotNull String key, @NotNull String replacer) {
        this.keys.add(Pair.of(key, replacer));
        return this;
    }

    @NotNull
    public UnaryOperator<String> replacer() {
        String[] placeholders = new String[this.keys.size()];
        String[] replaces = new String[this.keys.size()];

        for (int index = 0; index < this.keys.size(); index++) {
            Pair<String, String> pair = this.keys.get(index);
            placeholders[index] = pair.getFirst();
            replaces[index] = pair.getSecond();
        }

        return str -> StringUtil.replaceEach(str, placeholders, replaces);
    }
}

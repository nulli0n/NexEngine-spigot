package su.nexmedia.engine.api.placeholder;

import org.jetbrains.annotations.NotNull;

import java.util.function.UnaryOperator;

public interface Placeholder {

    @NotNull PlaceholderMap getPlaceholders();

    @NotNull
    default UnaryOperator<String> replacePlaceholders() {
        return this.getPlaceholders().replacer();
    }
}

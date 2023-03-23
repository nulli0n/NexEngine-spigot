package su.nexmedia.engine.api.editor;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.Colorizer;

import java.util.Arrays;
import java.util.List;

public class EditorLocale {

    private final String       key;
    private final String       name;
    private final List<String> lore;

    private String localizedName;
    private List<String> localizedLore;

    public EditorLocale(@NotNull String key, @NotNull String name, @NotNull List<String> lore) {
        this.key = key;
        this.name = name;
        this.lore = lore;
    }

    @NotNull
    public static EditorLocale of(@NotNull String key, @NotNull String name, @NotNull String... lore) {
        return new EditorLocale(key, name, Arrays.asList(lore));
    }

    @NotNull
    public String getKey() {
        return key;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public List<String> getLore() {
        return lore;
    }

    @NotNull
    public String getLocalizedName() {
        return localizedName;
    }

    public void setLocalizedName(@NotNull String localizedName) {
        this.localizedName = Colorizer.apply(localizedName);
    }

    @NotNull
    public List<String> getLocalizedLore() {
        return localizedLore;
    }

    public void setLocalizedLore(@NotNull List<String> localizedLore) {
        this.localizedLore = Colorizer.apply(localizedLore);
    }
}

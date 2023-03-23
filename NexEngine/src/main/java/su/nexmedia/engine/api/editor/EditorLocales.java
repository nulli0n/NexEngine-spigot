package su.nexmedia.engine.api.editor;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class EditorLocales {

    //protected static final String HEADER = "#fffd7f&l";
    protected static final String RED = "#ff6a6a";
    protected static final String GREEN = "#bbff6a";
    protected static final String BLUE = "#6adbff";
    protected static final String ORANGE = "#ffa76a";
    protected static final String YELLOW = "#ffed6a";
    protected static final String GRAY = "#bdc8c9";

    public static final EditorLocale CLOSE = EditorLocale.of("Editor.Generic.Close", "#FF5733(✕) &lExit");
    public static final EditorLocale RETURN = EditorLocale.of("Editor.Generic.Return", "#ffee9a(↓) &fReturn");
    public static final EditorLocale NEXT_PAGE = EditorLocale.of("Editor.Generic.NextPage", "#e3fbf9(→) &lNext Page");
    public static final EditorLocale PREVIOUS_PAGE = EditorLocale.of("Editor.Generic.PreviousPage", "#e3fbf9(←) &lPrevious Page");

    @NotNull
    protected static Builder builder(@NotNull String key) {
        return new Builder(key);
    }

    protected static final class Builder {

        private final String key;
        private       String       name;
        private final List<String> lore;

        public Builder(@NotNull String key) {
            this.key = key;
            this.name = "";
            this.lore = new ArrayList<>();
        }

        @NotNull
        public EditorLocale build() {
            return new EditorLocale(this.key, this.name, this.lore);
        }

        @NotNull
        public Builder name(@NotNull String name) {
            this.name = YELLOW + "&l" + name;
            return this;
        }

        @NotNull
        public Builder text(@NotNull String... text) {
            return this.addLore(GRAY, text);
        }

        @NotNull
        public Builder currentHeader() {
            return this.addLore(YELLOW + "&l", "Current:");
        }

        @NotNull
        public Builder current(@NotNull String type, @NotNull String value) {
            return this.addLore(YELLOW + "▪ " + GRAY, type + ": " + YELLOW + value);
        }

        @NotNull
        public Builder warningHeader() {
            return this.addLore(RED + "&l", "Warning:");
        }

        @NotNull
        public Builder warning(@NotNull String... info) {
            return this.addLore(RED + "▪ " + GRAY, info);
        }

        @NotNull
        public Builder noteHeader() {
            return this.addLore(ORANGE + "&l", "Notes:");
        }

        @NotNull
        public Builder notes(@NotNull String... info) {
            return this.addLore(ORANGE + "▪ " + GRAY, info);
        }

        @NotNull
        public Builder actionsHeader() {
            return this.addLore(GREEN + "&l", "Actions:");
        }

        @NotNull
        public Builder action(@NotNull String click, @NotNull String action) {
            return this.addLore(GREEN + "▪ " + GRAY, click + ": " + GREEN + action);
        }

        @NotNull
        public Builder breakLine() {
            return this.addLore("", "");
        }

        @NotNull
        private Builder addLore(@NotNull String prefix, @NotNull String... text) {
            for (String str : text) {
                this.lore.add(prefix + str);
            }
            return this;
        }
    }
}

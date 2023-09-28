package su.nexmedia.engine.api.editor;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static su.nexmedia.engine.utils.Colors.*;

public class EditorLocales {

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
            this.name = YELLOW + BOLD + name;
            return this;
        }

        @NotNull
        public Builder text(@NotNull String... text) {
            return this.addLore(GRAY, text);
        }

        @NotNull
        public Builder textRaw(@NotNull String... text) {
            return this.addLore("", text);
        }

        @NotNull
        public Builder currentHeader() {
            return this.addLore(YELLOW + BOLD, "Current:");
        }

        @NotNull
        public Builder current(@NotNull String type, @NotNull String value) {
            return this.addLore(YELLOW + "▪ " + GRAY, type + ": " + YELLOW + value);
        }

        @NotNull
        public Builder warningHeader() {
            return this.addLore(RED + BOLD, "Warning:");
        }

        @NotNull
        public Builder warning(@NotNull String... info) {
            return this.addLore(RED + "▪ " + GRAY, info);
        }

        @NotNull
        public Builder noteHeader() {
            return this.addLore(ORANGE + BOLD, "Notes:");
        }

        @NotNull
        public Builder notes(@NotNull String... info) {
            return this.addLore(ORANGE + "▪ " + GRAY, info);
        }

        @NotNull
        public Builder actionsHeader() {
            return this.addLore(GREEN + BOLD, "Actions:");
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

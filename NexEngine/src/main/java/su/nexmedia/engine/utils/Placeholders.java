package su.nexmedia.engine.utils;

import org.jetbrains.annotations.NotNull;

import java.util.function.UnaryOperator;

public class Placeholders {

    public static class Player {

        public static final String NAME = "%player_name%";
        public static final String DISPLAY_NAME = "%player_display_name%";

        @NotNull
        public static UnaryOperator<String> apply(@NotNull org.bukkit.entity.Player player) {
            return str -> str
                .replace(NAME, player.getName())
                .replace(DISPLAY_NAME, player.getDisplayName())
                ;
        }
    }
}

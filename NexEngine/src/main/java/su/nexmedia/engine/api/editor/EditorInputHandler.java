package su.nexmedia.engine.api.editor;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface EditorInputHandler<C extends Enum<C>, T> {

    boolean onType(
        @NotNull Player player, @NotNull T object,
        @NotNull C type, @NotNull String msg);
}

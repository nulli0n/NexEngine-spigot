package su.nexmedia.engine.api.editor;

import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.jetbrains.annotations.NotNull;

@Deprecated
public class EditorObject<T,E extends Enum<E>> {

    private final E      type;
    private final T                object;
    private final EditorInput<T,E> input;

    public EditorObject(@NotNull T object, @NotNull E type, @NotNull EditorInput<T,E> input) {
        this.type = type;
        this.object = object;
        this.input = input;
    }

    @NotNull
    public E getType() {
        return type;
    }

    @NotNull
    public T getObject() {
        return object;
    }

    @NotNull
    public EditorInput<T,E> getInput() {
        return input;
    }

    public boolean handle(@NotNull Player player, @NotNull AsyncPlayerChatEvent e) {
        return this.getInput().handle(player, this.getObject(), this.getType(), e);
    }
}

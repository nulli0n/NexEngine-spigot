package su.nexmedia.engine.api.editor;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.api.menu.IMenu;

public interface EditorHolder<P extends NexPlugin<P>, C extends Enum<C>> {

    @NotNull IMenu getEditor();

    @NotNull AbstractEditorHandler<P, C> getEditorHandlerNew();
}

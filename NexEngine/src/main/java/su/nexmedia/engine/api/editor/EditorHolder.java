package su.nexmedia.engine.api.editor;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.api.menu.IMenu;

public interface EditorHolder<P extends NexPlugin<P>, C extends Enum<C>> {

    @NotNull IMenu getEditor();

    @Deprecated
    @NotNull default AbstractEditorHandler<P, C> getEditorHandlerNew() {
        return this.getEditorHandler();
    }

    @NotNull AbstractEditorHandler<P, C> getEditorHandler();
}

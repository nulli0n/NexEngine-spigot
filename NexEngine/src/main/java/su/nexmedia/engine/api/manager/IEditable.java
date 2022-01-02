package su.nexmedia.engine.api.manager;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.menu.IMenu;

public interface IEditable {

    @NotNull IMenu getEditor();
}

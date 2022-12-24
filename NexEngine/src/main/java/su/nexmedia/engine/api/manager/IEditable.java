package su.nexmedia.engine.api.manager;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.menu.AbstractMenu;

public interface IEditable {

    @NotNull AbstractMenu<?> getEditor();
}

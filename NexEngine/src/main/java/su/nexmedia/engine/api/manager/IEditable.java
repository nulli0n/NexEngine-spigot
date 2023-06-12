package su.nexmedia.engine.api.manager;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.menu.AbstractMenu;

@Deprecated
public interface IEditable {

    @NotNull AbstractMenu<?> getEditor();
}

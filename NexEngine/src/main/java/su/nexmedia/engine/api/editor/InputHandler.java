package su.nexmedia.engine.api.editor;

import org.jetbrains.annotations.NotNull;

public interface InputHandler {

    boolean handle(@NotNull InputWrapper wrapper);
}

package su.nexmedia.engine.api.editor;

import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.utils.Colorizer;
import su.nexmedia.engine.utils.StringUtil;

public class InputWrapper {

    private final String text;
    private final String textRaw;
    private final String textColored;

    public InputWrapper(@NotNull AsyncPlayerChatEvent e) {
        this(e.getMessage());
    }

    public InputWrapper(@NotNull String text) {
        this.text = text;
        this.textRaw = Colorizer.restrip(text);
        this.textColored = Colorizer.apply(text);
    }

    public int asInt() {
        return this.asInt(0);
    }

    public int asInt(int def) {
        return StringUtil.getInteger(this.getTextRaw(), def);
    }

    public int asAnyInt(int def) {
        return StringUtil.getInteger(this.getTextRaw(), def, true);
    }

    public double asDouble() {
        return this.asDouble(0D);
    }

    public double asDouble(double def) {
        return StringUtil.getDouble(this.getTextRaw(), def);
    }

    public double asAnyDouble(double def) {
        return StringUtil.getDouble(this.getTextRaw(), def, true);
    }

    @Nullable
    public <E extends Enum<E>> E asEnum(@NotNull Class<E> clazz) {
        return StringUtil.getEnum(this.getTextRaw(), clazz).orElse(null);
    }

    @NotNull
    public <E extends Enum<E>> E asEnum(@NotNull Class<E> clazz, @NotNull E def) {
        return StringUtil.getEnum(this.getTextRaw(), clazz).orElse(def);
    }

    @NotNull
    @Deprecated
    public String getMessage() {
        return this.text;
    }

    @NotNull
    public String getText() {
        return text;
    }

    @NotNull
    public String getTextRaw() {
        return textRaw;
    }

    @NotNull
    public String getTextColored() {
        return textColored;
    }
}

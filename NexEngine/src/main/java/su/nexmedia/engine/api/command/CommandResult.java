package su.nexmedia.engine.api.command;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.utils.StringUtil;

import java.util.Map;

public class CommandResult {

    private final String label;
    private final Map<CommandFlag<?>, String> flags;

    private String[] args;

    public CommandResult(@NotNull String label, String[] args, @NotNull Map<CommandFlag<?>, String> flags) {
        this.label = label;
        this.flags = flags;
        this.setArgs(args);
    }

    public int length() {
        return this.args.length;
    }

    public void setArgs(@NotNull String[] args) {
        this.args = args;
    }

    @NotNull
    public String getArg(int index) {
        return this.getArgs()[index];
    }

    @NotNull
    public String getArg(int index, @NotNull String def) {
        if (index >= this.length()) return def;

        return this.getArgs()[index];
    }

    public int getInt(int index, int def) {
        return StringUtil.getInteger(this.getArg(index, ""), def, true);
    }

    public double getDouble(int index, double def) {
        return StringUtil.getDouble(this.getArg(index, ""), def, true);
    }

    public boolean hasFlag(@NotNull CommandFlag<?> flag) {
        return this.getFlags().containsKey(flag);
    }

    @Nullable
    public <T> T getFlag(@NotNull CommandFlag<T> flag) {
        String value = this.getFlags().get(flag);
        if (value == null) return null;

        return flag.getParser().apply(value);
    }

    @NotNull
    public <T> T getFlag(@NotNull CommandFlag<T> flag, @NotNull T def) {
        T value = this.getFlag(flag);
        return value == null ? def : value;
    }

    @NotNull
    public String getLabel() {
        return label;
    }

    public String[] getArgs() {
        return args;
    }

    @NotNull
    public Map<CommandFlag<?>, String> getFlags() {
        return flags;
    }
}

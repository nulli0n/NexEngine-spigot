package su.nexmedia.engine.api.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.api.manager.IPlaceholder;
import su.nexmedia.engine.lang.EngineLang;
import su.nexmedia.engine.utils.CollectionsUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nexmedia.engine.utils.regex.RegexUtil;

import java.util.*;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public abstract class AbstractCommand<P extends NexPlugin<P>> implements IPlaceholder {

    public static final String PLACEHOLDER_USAGE       = "%command_usage%";
    public static final String PLACEHOLDER_DESCRIPTION = "%command_description%";
    public static final String PLACEHOLDER_LABEL       = "%command_label%";

    private final Map<String, Pattern>            flagPatterns;
    protected     P                               plugin;
    protected     String[]                        aliases;
    protected     String                          permission;
    protected     Map<String, AbstractCommand<P>> childrens;
    protected     AbstractCommand<P>              parent;
    protected     Set<String>                     flags;

    public AbstractCommand(@NotNull P plugin, @NotNull List<String> aliases) {
        this(plugin, aliases.toArray(new String[0]));
    }

    public AbstractCommand(@NotNull P plugin, @NotNull String[] aliases) {
        this(plugin, aliases, (String) null);
    }

    public AbstractCommand(@NotNull P plugin, @NotNull List<String> aliases, @Nullable String permission) {
        this(plugin, aliases.toArray(new String[0]), permission);
    }

    public AbstractCommand(@NotNull P plugin, @NotNull List<String> aliases, @Nullable Permission permission) {
        this(plugin, aliases.toArray(new String[0]), permission);
    }

    public AbstractCommand(@NotNull P plugin, @NotNull String[] aliases, @Nullable Permission permission) {
        this(plugin, aliases, permission == null ? null : permission.getName());
    }

    public AbstractCommand(@NotNull P plugin, @NotNull String[] aliases, @Nullable String permission) {
        this.plugin = plugin;
        this.aliases = Stream.of(aliases).map(String::toLowerCase).toArray(String[]::new);
        this.permission = permission;
        this.childrens = new TreeMap<>();
        this.flags = new HashSet<>();
        this.flagPatterns = new HashMap<>();
    }

    @Override
    @NotNull
    public UnaryOperator<String> replacePlaceholders() {
        return str -> str
            .replace(PLACEHOLDER_DESCRIPTION, this.getDescription())
            .replace(PLACEHOLDER_USAGE, this.getUsage())
            .replace(PLACEHOLDER_LABEL, this.getLabelFull())
            ;
    }

    @Nullable
    public AbstractCommand<P> getParent() {
        return parent;
    }

    public void setParent(@Nullable AbstractCommand<P> parent) {
        this.parent = parent;
    }

    public final void addChildren(@NotNull AbstractCommand<P> children) {
        if (children.getParent() != null) return;

        Stream.of(children.getAliases()).forEach(alias -> {
            this.childrens.put(alias, children);
        });
        children.setParent(this);
    }

    @Nullable
    public final AbstractCommand<P> getChildren(@NotNull String alias) {
        return this.childrens.get(alias);
    }

    public final void removeChildren(@NotNull String alias) {
        this.childrens.keySet().removeIf(key -> key.equalsIgnoreCase(alias));
    }

    @NotNull
    public Collection<AbstractCommand<P>> getChildrens() {
        return new HashSet<>(this.childrens.values());
    }

    @NotNull
    public final String[] getAliases() {
        return this.aliases;
    }

    @Nullable
    public final String getPermission() {
        return this.permission;
    }

    @NotNull
    public Set<String> getFlags() {
        return new HashSet<>(this.flags);
    }

    public final void registerFlag(@NotNull String flag) {
        this.flags.add(flag);
        this.flagPatterns.put(flag, Pattern.compile("(\\~" + flag + ")+(\\{)+(.*?)(\\})"));
    }

    public final void unregisterFlag(@NotNull String flag) {
        this.flags.remove(flag);
        this.flagPatterns.remove(flag);
    }

    @NotNull
    public abstract String getUsage();

    @NotNull
    public abstract String getDescription();

    public abstract boolean isPlayerOnly();

    @NotNull
    public List<@NotNull String> getTab(@NotNull Player player, int arg, @NotNull String[] args) {
        return Collections.emptyList();
    }

    protected void onExecute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        this.onExecute(sender, label, args, new HashMap<>());
    }

    protected void onExecute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, @NotNull Map<String, String> flags) {

    }

    public final void execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        if (this.isPlayerOnly() && !(sender instanceof Player)) {
            this.errorSender(sender);
            return;
        }
        if (!this.hasPermission(sender)) {
            this.errorPermission(sender);
            return;
        }

        if (!this.getFlags().isEmpty()) {
            String argLine = String.join(" ", args);
            Map<String, String> flags = new HashMap<>();

            for (Map.Entry<String, Pattern> entry : this.flagPatterns.entrySet()) {
                String flag = entry.getKey();
                Pattern pattern = entry.getValue();

                Matcher matcher = RegexUtil.getMatcher(pattern, argLine);
                if (matcher != null && matcher.find()) {
                    flags.put(flag, StringUtil.color(matcher.group(3)));
                    argLine = StringUtil.oneSpace(argLine.replace(matcher.group(0), ""));
                }
            }

            args = argLine.split(" ");
            this.onExecute(sender, label, args, flags);
        }
        else {
            this.onExecute(sender, label, args);
        }
    }

    public final boolean hasPermission(@NotNull CommandSender sender) {
        return this.permission == null || sender.hasPermission(this.permission);
    }

    @NotNull
    public String getLabelFull() {
        StringBuilder labels = new StringBuilder();
        AbstractCommand<P> parent = this.getParent();
        while (parent != null) {
            //labels.append(" ");
            labels.insert(0, parent.getAliases()[0] + " ");
            parent = parent.getParent();
        }
        labels.append(this.getAliases()[0]);
        return labels.toString();
    }

    protected final void printUsage(@NotNull CommandSender sender) {
        plugin.getMessage(EngineLang.CORE_COMMAND_USAGE).replace(this.replacePlaceholders()).send(sender);
    }

    protected final void errorPermission(@NotNull CommandSender sender) {
        plugin.getMessage(EngineLang.ERROR_PERMISSION_DENY).send(sender);
    }

    protected final void errorItem(@NotNull CommandSender sender) {
        plugin.getMessage(EngineLang.ERROR_ITEM_INVALID).send(sender);
    }

    protected final void errorPlayer(@NotNull CommandSender sender) {
        plugin.getMessage(EngineLang.ERROR_PLAYER_INVALID).send(sender);
    }

    protected final void errorSender(@NotNull CommandSender sender) {
        plugin.getMessage(EngineLang.ERROR_COMMAND_SENDER).send(sender);
    }

    protected final void errorType(@NotNull CommandSender sender, @NotNull Class<?> clazz) {
        plugin.getMessage(EngineLang.ERROR_TYPE_INVALID).replace("%types%", CollectionsUtil.getEnums(clazz)).send(sender);
    }

    protected final void errorNumber(@NotNull CommandSender sender, @NotNull String from) {
        plugin.getMessage(EngineLang.ERROR_NUMBER_INVALID).replace("%num%", from).send(sender);
    }
}

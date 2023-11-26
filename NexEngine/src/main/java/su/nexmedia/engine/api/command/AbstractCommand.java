package su.nexmedia.engine.api.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.api.lang.LangMessage;
import su.nexmedia.engine.api.placeholder.Placeholder;
import su.nexmedia.engine.api.placeholder.PlaceholderMap;
import su.nexmedia.engine.lang.EngineLang;
import su.nexmedia.engine.utils.Placeholders;
import su.nexmedia.engine.utils.regex.TimedMatcher;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public abstract class AbstractCommand<P extends NexPlugin<P>> implements Placeholder {

    protected final P                               plugin;
    protected final Map<String, AbstractCommand<P>> childrens;
    protected final Set<CommandFlag<?>>             commandFlags;
    protected final PlaceholderMap placeholderMap;

    protected AbstractCommand<P> parent;
    protected String[]           aliases;
    protected String             permission;
    protected String             usage;
    protected String             description;
    protected boolean            playerOnly;

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
        this.commandFlags = new HashSet<>();
        this.placeholderMap = new PlaceholderMap()
            .add(Placeholders.COMMAND_DESCRIPTION, this::getDescription)
            .add(Placeholders.COMMAND_USAGE, this::getUsage)
            .add(Placeholders.COMMAND_LABEL, this::getLabelFull);

    }

    @Override
    @NotNull
    public PlaceholderMap getPlaceholders() {
        return this.placeholderMap;
    }

    protected abstract void onExecute(@NotNull CommandSender sender, @NotNull CommandResult result);

    @NotNull
    public List<String> getTab(@NotNull Player player, int arg, @NotNull String[] args) {
        return this.getFlags().stream().map(CommandFlag::getNamePrefixed).toList();
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

        CommandResult result = new CommandResult(label, args, new HashMap<>());
        String argLine = String.join(" ", args);
        for (CommandFlag<?> flag : this.getFlags()) {
            String name = flag.getName();
            Pattern pattern = flag.getPattern();

            TimedMatcher matcher = TimedMatcher.create(pattern, argLine, 100);
            matcher.setDebug(true);
            if (matcher.find()) {
                result.getFlags().put(flag, matcher.getMatcher().group(2).trim());
                argLine = argLine.replace(matcher.getMatcher().group(0), "");
            }
        }
        result.setArgs(argLine.isEmpty() ? new String[0] : argLine.trim().split(" "));

        this.onExecute(sender, result);
    }

    public final void addChildren(@NotNull AbstractCommand<P> children) {
        if (children.getParent() != null) return;

        Stream.of(children.getAliases()).forEach(alias -> {
            this.childrens.put(alias, children);
        });
        children.setParent(this);
    }

    public final void removeChildren(@NotNull String alias) {
        this.childrens.keySet().removeIf(key -> key.equalsIgnoreCase(alias));
    }

    public final void addFlag(@NotNull CommandFlag<?>... flags) {
        for (CommandFlag<?> flag : flags) this.addFlag(flag);
    }

    public final void addFlag(@NotNull CommandFlag<?> flag) {
        this.getFlags().add(flag);
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

    @Nullable
    public AbstractCommand<P> getParent() {
        return parent;
    }

    public void setParent(@Nullable AbstractCommand<P> parent) {
        this.parent = parent;
    }

    @Nullable
    public final AbstractCommand<P> getChildren(@NotNull String alias) {
        return this.childrens.get(alias);
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

    public void setPermission(@Nullable Permission permission) {
        this.setPermission(permission == null ? null : permission.getName());
    }

    public void setPermission(@Nullable String permission) {
        this.permission = permission;
    }

    @NotNull
    @Deprecated
    public Set<CommandFlag<?>> getCommandFlags() {
        return this.getFlags();
    }

    @NotNull
    public Set<CommandFlag<?>> getFlags() {
        return this.commandFlags;
    }

    @NotNull
    public String getUsage() {
        return this.usage == null ? "" : this.usage;
    }

    public void setUsage(@NotNull LangMessage message) {
        this.setUsage(message.getLocalized());
    }

    public void setUsage(@NotNull String usage) {
        this.usage = usage;
    }

    @NotNull
    public String getDescription() {
        return this.description == null ? "" : this.description;
    }

    public void setDescription(@NotNull LangMessage message) {
        this.setDescription(message.getLocalized());
    }

    public void setDescription(@NotNull String description) {
        this.description = description;
    }

    public boolean isPlayerOnly() {
        return this.playerOnly;
    }

    public void setPlayerOnly(boolean playerOnly) {
        this.playerOnly = playerOnly;
    }

    protected final void printUsage(@NotNull CommandSender sender) {
        plugin.getMessage(EngineLang.COMMAND_USAGE).replace(this.replacePlaceholders()).send(sender);
    }

    protected final void errorPermission(@NotNull CommandSender sender) {
        plugin.getMessage(EngineLang.ERROR_PERMISSION_DENY).send(sender);
    }

    protected final void errorPlayer(@NotNull CommandSender sender) {
        plugin.getMessage(EngineLang.ERROR_PLAYER_INVALID).send(sender);
    }

    protected final void errorSender(@NotNull CommandSender sender) {
        plugin.getMessage(EngineLang.ERROR_COMMAND_SENDER).send(sender);
    }

    protected final void errorNumber(@NotNull CommandSender sender, @NotNull String from) {
        plugin.getMessage(EngineLang.ERROR_NUMBER_INVALID).replace("%num%", from).send(sender);
    }
}

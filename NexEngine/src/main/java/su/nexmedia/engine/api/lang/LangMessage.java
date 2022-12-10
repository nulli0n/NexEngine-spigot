package su.nexmedia.engine.api.lang;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.utils.CollectionsUtil;
import su.nexmedia.engine.utils.MessageUtil;
import su.nexmedia.engine.utils.Placeholders;
import su.nexmedia.engine.utils.StringUtil;
import su.nexmedia.engine.utils.regex.RegexUtil;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class LangMessage {

    private static final Pattern              PATTERN_MESSAGE_FULL   = Pattern.compile("((\\{message:)+(.+?)\\}+(.*?))");
    private static final Map<String, Pattern> PATTERN_MESSAGE_PARAMS = new HashMap<>();

    static {
        for (String parameter : new String[]{"type", "prefix", "sound", "fadeIn", "stay", "fadeOut"}) {
            PATTERN_MESSAGE_PARAMS.put(parameter, Pattern.compile("~+(" + parameter + ")+?:+(.*?);"));
        }
    }

    private final NexPlugin<?> plugin;

    private String msgRaw;
    private String msgLocalized;
    private OutputType type = OutputType.CHAT;
    private boolean hasPrefix = true;
    private Sound sound;
    private int[] titleTimes = new int[3];

    public LangMessage(@NotNull NexPlugin<?> plugin, @NotNull String raw) {
        this.plugin = plugin;
        this.setRaw(raw);
    }

    LangMessage(@NotNull LangMessage from) {
        this.plugin = from.plugin;
        this.msgRaw = from.getRaw();
        this.msgLocalized = from.getLocalized();
        this.type = from.type;
        this.hasPrefix = from.hasPrefix;
        this.sound = from.sound;
        this.titleTimes = Arrays.copyOf(from.titleTimes, from.titleTimes.length);
    }

    void setArguments(@NotNull String msg) {
        Matcher matcher = RegexUtil.getMatcher(PATTERN_MESSAGE_FULL, msg);
        if (!RegexUtil.matcherFind(matcher)) return;

        // String with only args
        String msgRaw = matcher.group(0);
        String msgParams = matcher.group(3).trim();
        this.msgLocalized = msg.replace(msgRaw, "");

        for (Map.Entry<String, Pattern> entryParams : PATTERN_MESSAGE_PARAMS.entrySet()) {
            Matcher matcherParam = RegexUtil.getMatcher(entryParams.getValue(), msgParams);
            if (!RegexUtil.matcherFind(matcherParam)) continue;

            String paramName = entryParams.getKey();
            String paramValue = matcherParam.group(2).stripLeading();
            switch (paramName) {
                case "type" -> this.type = CollectionsUtil.getEnum(paramValue, OutputType.class);
                case "prefix" -> this.hasPrefix = Boolean.parseBoolean(paramValue);
                case "sound" -> this.sound = CollectionsUtil.getEnum(paramValue, Sound.class);
                case "fadeIn" -> this.titleTimes[0] = StringUtil.getInteger(paramValue, -1);
                case "stay" -> {
                    this.titleTimes[1] = StringUtil.getInteger(paramValue, -1);
                    if (this.titleTimes[1] < 0) this.titleTimes[1] = Short.MAX_VALUE;
                }
                case "fadeOut" -> this.titleTimes[2] = StringUtil.getInteger(paramValue, -1);
            }
        }
    }

    @NotNull
    public String getRaw() {
        return this.msgRaw;
    }

    public void setRaw(@NotNull String msgRaw) {
        this.msgRaw = msgRaw;
        this.setLocalized(this.replaceDefaults().apply(this.getRaw()));
        this.setArguments(this.getLocalized());
    }

    @NotNull
    public String getLocalized() {
        return this.msgLocalized;
    }

    private void setLocalized(@NotNull String msgLocalized) {
        this.msgLocalized = StringUtil.color(msgLocalized);
    }

    @SuppressWarnings("unchecked")
    @NotNull
    public LangMessage replace(@NotNull String var, @NotNull Object replacer) {
        if (this.isEmpty()) return this;
        if (replacer instanceof List) return this.replace(var, (List<Object>) replacer);
        return this.replace(str -> str.replace(var, String.valueOf(replacer)));
    }

    @NotNull
    public LangMessage replace(@NotNull String var, @NotNull List<Object> replacer) {
        if (this.isEmpty()) return this;
        return this.replace(str -> str.replace(var, String.join("\\n", replacer.stream().map(Object::toString).toList())));
    }

    @NotNull
    public LangMessage replace(@NotNull UnaryOperator<String> replacer) {
        if (this.isEmpty()) return this;

        LangMessage msgCopy = new LangMessage(this);
        msgCopy.setLocalized(replacer.apply(msgCopy.getLocalized()));
        return msgCopy;
    }

    public boolean isEmpty() {
        return (this.type == LangMessage.OutputType.NONE || this.getLocalized().isEmpty());
    }

    public void broadcast() {
        if (this.isEmpty()) return;

        Bukkit.getServer().getOnlinePlayers().forEach(this::send);
        this.send(Bukkit.getServer().getConsoleSender());
    }

    public void send(@NotNull CommandSender sender) {
        if (this.isEmpty()) return;

        if (this.sound != null && sender instanceof Player player) {
            MessageUtil.sound(player, this.sound);
        }

        if (this.type == LangMessage.OutputType.CHAT) {
            String prefix = hasPrefix ? plugin.getConfigManager().pluginPrefix : "";
            this.asList().forEach(line -> MessageUtil.sendWithJson(sender, prefix + line));
            return;
        }

        if (sender instanceof Player player) {
            if (this.type == LangMessage.OutputType.ACTION_BAR) {
                MessageUtil.sendActionBar(player, this.getLocalized());
            }
            else if (this.type == LangMessage.OutputType.TITLES) {
                List<String> list = this.asList();
                String title = list.size() > 0 ? list.get(0) : "";
                String subtitle = list.size() > 1 ? list.get(1) : "";
                player.sendTitle(title, subtitle, this.titleTimes[0], this.titleTimes[1], this.titleTimes[2]);
            }
        }
    }

    @NotNull
    public List<String> asList() {
        return this.isEmpty() ? Collections.emptyList() : Stream.of(this.getLocalized().split("\\\\n"))
            .filter(Predicate.not(String::isEmpty)).toList();
    }

    /**
     * Replaces a raw '\n' new line splitter with a system one.
     * @return A string with a system new line splitters.
     */
    @NotNull
    public String normalizeLines() {
        return String.join("\n", this.asList());
    }

    @NotNull
    private UnaryOperator<String> replaceDefaults() {
        return str -> {
            /*for (Map.Entry<String, String> entry : this.template.getCustomPlaceholders().entrySet()) {
                if (entry.getKey().isEmpty() || entry.getValue().isEmpty()) continue;
                str = str.replace(entry.getKey(), entry.getValue());
            }*/
            return Placeholders.Plugin.replacer(plugin).apply(str);
        };
    }

    public enum OutputType {
        CHAT, ACTION_BAR, TITLES, NONE,
    }
}
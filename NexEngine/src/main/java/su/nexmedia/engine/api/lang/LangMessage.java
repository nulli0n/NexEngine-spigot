package su.nexmedia.engine.api.lang;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.api.placeholder.PlaceholderMap;
import su.nexmedia.engine.utils.*;
import su.nexmedia.engine.utils.message.NexParser;
import su.nexmedia.engine.utils.regex.RegexUtil;
import su.nexmedia.engine.utils.values.UniSound;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class LangMessage {

    private static final Pattern PATTERN_OPTIONS = Pattern.compile("<\\!(.*?)\\!>");

    public enum OutputType {
        CHAT, ACTION_BAR, TITLES, NONE,
    }

    enum Option {
        PREFIX("prefix"),
        SOUND("sound"),
        TYPE("type"),
        PAPI("papi"),
        ;

        private final Pattern pattern;

        Option(@NotNull String name) {
            this.pattern = Pattern.compile(name + NexParser.OPTION_PATTERN);
        }

        @NotNull
        public Pattern getPattern() {
            return pattern;
        }
    }

    private final NexPlugin<?> plugin;
    @Deprecated private final PlaceholderMap placeholderMap;

    private String msgRaw;
    private String msgLocalized;
    private OutputType type = OutputType.CHAT;
    private boolean hasPrefix = true;
    private boolean papi = false;
    private Sound sound;
    private int[] titleTimes = new int[3];

    public LangMessage(@NotNull NexPlugin<?> plugin, @NotNull String raw) {
        this.plugin = plugin;
        this.placeholderMap = new PlaceholderMap();
        this.plugin.getLangManager().getPlaceholders().forEach((placeholder, value) -> {
            this.placeholderMap.add(placeholder, () -> value);
        });

        this.setRaw(raw);
    }

    LangMessage(@NotNull LangMessage from) {
        this.plugin = from.plugin;
        this.msgRaw = from.getRaw();
        this.msgLocalized = from.getLocalized();
        this.type = from.type;
        this.hasPrefix = from.hasPrefix;
        this.papi = from.papi;
        this.sound = from.sound;
        this.titleTimes = Arrays.copyOf(from.titleTimes, from.titleTimes.length);
        this.placeholderMap = new PlaceholderMap(from.placeholderMap);
    }

    void setOptions(@NotNull String msg) {
        Matcher matcher = RegexUtil.getMatcher(PATTERN_OPTIONS, msg);
        if (!RegexUtil.matcherFind(matcher)) return;

        // String with only args
        String matchFull = matcher.group(0);
        String matchOptions = matcher.group(1).trim();
        this.msgLocalized = msg.replace(matchFull, "");

        for (Option option : Option.values()) {
            Matcher matcherParam = RegexUtil.getMatcher(option.getPattern(), matchOptions);
            if (!RegexUtil.matcherFind(matcherParam)) continue;

            String optionValue = matcherParam.group(1).stripLeading();
            switch (option) {
                case TYPE -> {
                    String[] split = optionValue.split(":");
                    this.type = StringUtil.getEnum(split[0], OutputType.class).orElse(OutputType.CHAT);
                    if (this.type == OutputType.TITLES) {
                        this.titleTimes[0] = split.length >= 2 ? StringUtil.getInteger(split[1], -1) : -1;
                        this.titleTimes[1] = split.length >= 3 ? StringUtil.getInteger(split[2], -1, true) : -1;
                        this.titleTimes[2] = split.length >= 4 ? StringUtil.getInteger(split[3], -1) : -1;

                        if (this.titleTimes[1] < 0) this.titleTimes[1] = Short.MAX_VALUE;
                    }
                }
                case PREFIX -> this.hasPrefix = Boolean.parseBoolean(optionValue);
                case PAPI -> this.papi = Boolean.parseBoolean(optionValue) && EngineUtils.hasPlaceholderAPI();
                case SOUND -> this.sound = StringUtil.getEnum(optionValue, Sound.class).orElse(null);
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
        this.setOptions(this.getLocalized());
    }

    @NotNull
    public String getLocalized() {
        return this.msgLocalized;
    }

    @NotNull
    public String getLocalized(@NotNull CommandSender sender) {
        String localized = this.getLocalized();
        if (this.papi && sender instanceof Player player) {
            localized = PlaceholderAPI.setPlaceholders(player, localized);
        }
        return localized;
    }

    private void setLocalized(@NotNull String msgLocalized) {
        this.msgLocalized = Colorizer.apply(msgLocalized);
    }

    @NotNull
    public LangMessage replace(@NotNull String var, @NotNull Object replacer) {
        if (this.isEmpty()) return this;
        return this.replace(str -> str.replace(var, String.valueOf(replacer)));
    }

    @NotNull
    public LangMessage replace(@NotNull UnaryOperator<String> replacer) {
        if (this.isEmpty()) return this;

        LangMessage msgCopy = new LangMessage(this);
        msgCopy.setLocalized(replacer.apply(msgCopy.getLocalized()));
        return msgCopy;
    }

    @NotNull
    public LangMessage replace(@NotNull Predicate<String> predicate, @NotNull BiConsumer<String, List<String>> replacer) {
        if (this.isEmpty()) return this;

        LangMessage msgCopy = new LangMessage(this);
        List<String> replaced = new ArrayList<>();
        msgCopy.asList().forEach(line -> {
            if (predicate.test(line)) {
                replacer.accept(line, replaced);
                return;
            }
            replaced.add(line);
        });
        msgCopy.setLocalized(String.join("\\n", replaced));
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
            UniSound.of(this.sound).play(player);
        }

        if (this.type == LangMessage.OutputType.CHAT) {
            String prefix = hasPrefix ? plugin.getConfigManager().pluginPrefix : "";
            this.asList(sender).forEach(line -> {
                PlayerUtil.sendRichMessage(sender, prefix + line);
            });
            return;
        }

        if (sender instanceof Player player) {
            if (this.type == LangMessage.OutputType.ACTION_BAR) {
                PlayerUtil.sendActionBar(player, this.getLocalized(player));
            }
            else if (this.type == LangMessage.OutputType.TITLES) {
                List<String> list = this.asList(player);
                String title = list.size() > 0 ? NexParser.toPlainText(list.get(0)) : "";
                String subtitle = list.size() > 1 ? NexParser.toPlainText(list.get(1)) : "";
                player.sendTitle(title, subtitle, this.titleTimes[0], this.titleTimes[1], this.titleTimes[2]);
            }
        }
    }

    public void send(@NotNull String playerName) {
        Player pTarget = plugin.getServer().getPlayerExact(playerName);
        if (pTarget != null) {
            send(pTarget);
        } else {
            if (this.type == LangMessage.OutputType.CHAT) {
                if (Bukkit.spigot().getConfig().getBoolean("settings.bungeecord", false)) {
                    String prefix = hasPrefix ? plugin.getConfigManager().pluginPrefix : "";
                    this.asList().forEach(line -> {
                        PlayerUtil.sendBungeeCordMessage(playerName, prefix + line);
                    });
                }
            }
        }

    }

    @NotNull
    public List<String> asList() {
        return this.asList(this.getLocalized());
    }

    @NotNull
    public List<String> asList(@NotNull CommandSender sender) {
        return this.asList(this.getLocalized(sender));
    }

    @NotNull
    private List<String> asList(@NotNull String localized) {
        return this.isEmpty() ? Collections.emptyList() : Stream.of(localized.split("\\\\n"))
            .filter(Predicate.not(String::isEmpty)).toList();
    }

    /**
     * Replaces plain '\n' line breaker with a system one.
     * @return A string with a system lin breakers.
     */
    @NotNull
    public String normalizeLines() {
        return String.join("\n", this.asList());
    }

    @NotNull
    private UnaryOperator<String> replaceDefaults() {
        return str -> Placeholders.forPlugin(this.plugin).apply(this.placeholderMap.replacer().apply(str));
    }
}
package su.nexmedia.engine.api.config;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.CollectionsUtil;
import su.nexmedia.engine.utils.MessageUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nexmedia.engine.utils.regex.RegexUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LangMessage {

    private static final Pattern   PATTERN_MESSAGE   = Pattern.compile("(\\{message:)+(.)+?(\\})+(.*?)(\\})?");
    private static final String[]  MESSAGE_ARGUMENTS = new String[]{"type", "prefix", "fadeIn", "stay", "fadeOut"};
    private static final Pattern[] PATTERN_ARGUMENTS = new Pattern[MESSAGE_ARGUMENTS.length];

    static {
        for (int i = 0; i < MESSAGE_ARGUMENTS.length; i++) {
            PATTERN_ARGUMENTS[i] = Pattern.compile("(~)+(" + MESSAGE_ARGUMENTS[i] + ")+?(:)+(.*?)(;)");
        }
    }

    private final LangTemplate template;
    private final String       msgDefault;
    private       String       msgColor;
    private       String       path;

    private OutputType out         = OutputType.CHAT;
    private boolean    isPrefix    = true;
    private int[]      titlesTimes = new int[3];

    public LangMessage(@NotNull LangTemplate template, @NotNull String message) {
        this.template = template;
        this.msgDefault = message;
        this.setMsg(message);
    }

    LangMessage(@NotNull LangMessage from) {
        this.template = from.template;
        this.msgDefault = from.getDefaultMsg();
        this.msgColor = from.getMsg();
        this.path = from.getPath();
        this.out = from.out;
        this.isPrefix = from.isPrefix;
        this.titlesTimes = from.titlesTimes;
    }

    @NotNull
    public String getPath() {
        return this.path;
    }

    void setPath(@NotNull String path) {
        this.path = path.replace("_", ".");
    }

    boolean setArguments(@NotNull String msg) {
        Matcher mArgs = PATTERN_MESSAGE.matcher(msg);
        if (!mArgs.find()) return false;

        // String with only args
        String extract = mArgs.group(0);
        String arguments = extract.replace("{message:", "").replace("}", "").trim();
        this.msgColor = msg.replace(extract, "");

        for (int i = 0; i < MESSAGE_ARGUMENTS.length; i++) {
            // Search for flag of this parameter
            String argType = MESSAGE_ARGUMENTS[i];
            Pattern pArgVal = PATTERN_ARGUMENTS[i];
            Matcher mArgVal = RegexUtil.getMatcher(pArgVal, arguments);//pArgVal.matcher(arguments);
            if (mArgVal == null) continue;

            // Get the flag value
            if (mArgVal.find()) {
                // Extract only value from all flag string
                String argValue = mArgVal.group(4).trim();
                switch (argType) {
                    case "type" -> this.out = CollectionsUtil.getEnum(argValue, OutputType.class);
                    case "prefix" -> this.isPrefix = Boolean.parseBoolean(argValue);
                    case "fadeIn" -> this.titlesTimes[0] = StringUtil.getInteger(argValue, -1);
                    case "stay" -> {
                        this.titlesTimes[1] = StringUtil.getInteger(argValue, -1);
                        if (this.titlesTimes[1] < 0) this.titlesTimes[1] = 10000;
                    }
                    case "fadeOut" -> this.titlesTimes[2] = StringUtil.getInteger(argValue, -1);
                }
            }
        }
        return true;
    }

    @NotNull
    public String getDefaultMsg() {
        return this.msgDefault;
    }

    @NotNull
    public String getMsgReady() {
        return this.replaceDefaults().apply(this.msgColor);
    }

    @NotNull
    public String getMsg() {
        return this.msgColor;
    }

    public void setMsg(@NotNull String msg) {
        // When TRUE, then 'msgColor' is already set by this 'msg' value.
        if (!this.setArguments(msg)) {
            this.msgColor = msg;
        }

        // Do not replace colors for JSON message, otherwise it will be broken.
        if (!MessageUtil.isJSON(msg)) {
            this.msgColor = StringUtil.color(this.msgColor);
        }
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

        StringBuilder builder = new StringBuilder();
        replacer.forEach(rep -> {
            if (builder.length() > 0) {
                builder.append("\\n");
            }
            builder.append(rep.toString());
        });

        return this.replace(str -> str.replace(var, builder.toString()));
    }

    @NotNull
    public LangMessage replace(@NotNull UnaryOperator<String> replacer) {
        if (this.isEmpty()) return this;

        LangMessage msgCopy = new LangMessage(this);
        msgCopy.msgColor = StringUtil.color(replacer.apply(msgCopy.getMsg()));
        return msgCopy;
    }

    public boolean isEmpty() {
        return (this.out == LangMessage.OutputType.NONE || this.getMsg().isEmpty());
    }

    public void broadcast() {
        if (this.isEmpty()) return;

        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            this.send(player);
        }
        this.send(Bukkit.getServer().getConsoleSender());
    }

    public void send(@NotNull CommandSender sender) {
        if (this.isEmpty()) return;

        if (this.out == LangMessage.OutputType.CHAT) {
            String prefix = isPrefix ? template.getPrefix() : "";

            this.asList().forEach(line -> {
                MessageUtil.sendWithJSON(sender, prefix + line);
            });
        }
        else if (sender instanceof Player player) {
            if (this.out == LangMessage.OutputType.ACTION_BAR) {
                MessageUtil.sendActionBar(player, this.getMsgReady());
            }
            else if (this.out == LangMessage.OutputType.TITLES) {
                List<String> list = this.asList();
                if (list.isEmpty()) return;

                String title = list.get(0);
                String subtitle = list.size() > 1 ? list.get(1) : "";
                player.sendTitle(title, subtitle, this.titlesTimes[0], this.titlesTimes[1], this.titlesTimes[2]);
            }
        }
    }

    @NotNull
    public List<String> asList() {
        String msg = this.getMsgReady();
        if (msg.isEmpty()) return Collections.emptyList();

        List<String> list = new ArrayList<>();
        for (String line : msg.split("\\\\n")) {
            list.add(line.trim());
        }
        return list;
    }

    /**
     * Replaces a raw '\n' new line splitter with a system one.
     *
     * @return A string with a system new line splitters.
     */
    @NotNull
    public String normalizeLines() {
        StringBuilder text = new StringBuilder();
        for (String line : this.asList()) {
            if (text.length() > 0) {
                text.append("\n");
            }
            text.append(line);
        }
        return text.toString();
    }

    @NotNull
    private UnaryOperator<String> replaceDefaults() {
        return str -> {
            for (Map.Entry<String, String> entry : this.template.getCustomPlaceholders().entrySet()) {
                if (entry.getKey().isEmpty() || entry.getValue().isEmpty()) continue;
                str = str.replace(entry.getKey(), entry.getValue());
            }
            return str.replace("%plugin%", template.plugin.cfg().pluginName);
        };
    }

    public enum OutputType {
        CHAT, ACTION_BAR, TITLES, NONE,
    }
}
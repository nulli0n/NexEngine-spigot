package su.nexmedia.engine.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.utils.json.text.ClickText;
import su.nexmedia.engine.utils.json.text.ClickWord;
import su.nexmedia.engine.utils.regex.RegexUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Deprecated
public class MessageUtil {

    @Deprecated private static final Pattern  PATTERN_LEGACY_JSON_FULL = Pattern.compile("((\\{json:)+(.*?)(\\})+(.*?))(\\{end-json\\})");
    @Deprecated private static final Map<String, Pattern> PATTERN_JSON_PARAMS      = new HashMap<>();
    //private static final Pattern              PATTERN_JSON_FULL   = Pattern.compile("((\\{json:)+(.+?)\\}+(.*?))");
    //private static final Pattern              PATTERN_JSON_FULL   = Pattern.compile("((\\{json:){1}(.*?)\\}{1})");
    @Deprecated private static final Pattern PATTERN_JSON_FULL = Pattern.compile("(\\{json:(.*?)\\}+)");

    static {
        for (String parameter : new String[]{"text", "hint", "hover", "showText", "chat-type", "runCommand", "chat-suggest", "suggestCommand", "url", "openUrl", "showItem", "copyToClipboard"}) {
            PATTERN_JSON_PARAMS.put(parameter, Pattern.compile("~+(" + parameter + ")+?:+(.*?);"));
        }
    }

    @Deprecated
    public static void sendCustom(@NotNull CommandSender sender, @NotNull String message) {
        PlayerUtil.sendRichMessage(sender, message);
    }

    public static void sendActionBar(@NotNull Player player, @NotNull String msg) {
        PlayerUtil.sendActionBar(player, msg);
    }

    public static void sound(@NotNull Player player, @Nullable Sound sound) {
        PlayerUtil.sound(player, sound);
    }

    public static void sound(@NotNull Location location, @Nullable Sound sound) {
        World world = location.getWorld();
        if (world == null || sound == null) return;

        world.playSound(location, sound, 0.9f, 0.9f);
    }

    @NotNull
    @Deprecated
    public static String toNewFormat(@NotNull String message) {
        Matcher matcherOld = RegexUtil.getMatcher(PATTERN_LEGACY_JSON_FULL, message);
        int index = 0;
        while (RegexUtil.matcherFind(matcherOld)) {
            String jsonRaw = matcherOld.group(0); // Full json text, like '{json: <args>}Text{end-json}
            String jsonArgs = matcherOld.group(3).trim(); // Only json parameters, like '~hover: Text; ~openUrl: google.com;'
            String jsonText = matcherOld.group(5); // The text to apply JSON on.

            message = message.replace(jsonRaw, "{json: ~text:" + jsonText + "; " + jsonArgs + "}");
        }
        return message;
    }

    @Deprecated
    public static boolean isJSON(@NotNull String str) {
        Matcher matcher = RegexUtil.getMatcher(PATTERN_LEGACY_JSON_FULL, str);
        return matcher.find();
    }

    @Deprecated
    public static boolean hasJson(@NotNull String str) {
        Matcher matcher = RegexUtil.getMatcher(PATTERN_JSON_FULL, str);
        return matcher.find() || isJSON(str);
    }

    @NotNull
    @Deprecated
    public static String stripJsonOld(@NotNull String message) {
        /*Matcher matcher = RegexUtil.getMatcher(PATTERN_LEGACY_JSON_FULL, message);
        if (matcher == null) return message;

        while (matcher.find()) {
            String jsonRaw = matcher.group(0); // Full json text, like '{json: <args>}Text{end-json}
            String jsonText = matcher.group(5); // The text to apply JSON on.

            message = message.replace(jsonRaw, jsonText);
        }*/
        return stripJson(toNewFormat(message));
    }

    @NotNull
    @Deprecated
    public static String stripJson(@NotNull String message) {
        Matcher matcher = RegexUtil.getMatcher(PATTERN_JSON_FULL, message);
        while (RegexUtil.matcherFind(matcher)) {
            String jsonRaw = matcher.group(0); // Full json text
            message = message.replace(jsonRaw, "");
        }
        return message;
    }

    @NotNull
    @Deprecated
    public static String toSimpleText(@NotNull String message) {
        message = toNewFormat(message);

        Matcher matcher = RegexUtil.getMatcher(PATTERN_JSON_FULL, message);
        while (RegexUtil.matcherFind(matcher)) {
            String jsonRaw = matcher.group(0); // Full json text, like '{json: <args>}Text{end-json}
            String jsonArgs = matcher.group(2).trim(); // Only json parameters, like '~hover: Text; ~openUrl: google.com;'
            String text = getParamValue(jsonArgs, "text");
            message = message.replace(jsonRaw, text == null ? "" : text);
        }
        return message;
    }

    @Deprecated
    public static void sendWithJSON(@NotNull CommandSender sender, @NotNull String message) {
        sendWithJson(sender, message);
    }

    @Deprecated
    public static String[] extractNonJson(@NotNull String message) {
        message = Colorizer.apply(message.replace("\n", " "));
        message = toNewFormat(message);
        return PATTERN_JSON_FULL.split(message);
    }

    @Deprecated
    public static void sendWithJson(@NotNull CommandSender sender, @NotNull String message) {
        message = Colorizer.apply(message.replace("\n", " "));
        message = toNewFormat(message);
        if (!(sender instanceof Player player)) {
            sender.sendMessage(toSimpleText(message));
            return;
        }

        Matcher matcher = RegexUtil.getMatcher(PATTERN_JSON_FULL, message);
        Map<String, String> textParams = new HashMap<>();
        int index = 0;
        while (RegexUtil.matcherFind(matcher)) {
            String jsonRaw = matcher.group(0); // Full json text, like '{json: <args>}
            String jsonArgs = matcher.group(2).trim(); // Only json parameters, like '~hover: Text; ~openUrl: google.com;'
            //String jsonText = matcher.group(5); // The text to apply JSON on.

            String placeholder = "{%" + (index++) + "%}";
            message = message.replace(jsonRaw, placeholder);
            textParams.put(placeholder, jsonArgs);
        }

        ClickText clickText = new ClickText(message);
        for (Map.Entry<String, String> entry : textParams.entrySet()) {
            String placeholder = entry.getKey();
            String params = entry.getValue();

            String text = getParamValue(params, "text");
            if (text == null) text = "";

            ClickWord clickWord = clickText.addComponent(placeholder, text);
            for (Map.Entry<String, Pattern> entryParams : PATTERN_JSON_PARAMS.entrySet()) {
                String param = entryParams.getKey();
                String paramValue = getParamValue(params, param);
                if (paramValue == null) continue;

                switch (param) {
                    case "hint", "hover", "showText" -> clickWord.showText(paramValue.split("\\|"));
                    case "chat-type", "runCommand" -> clickWord.runCommand(paramValue);
                    case "chat-suggest", "suggestCommand" -> clickWord.suggestCommand(paramValue);
                    case "url", "openUrl" -> clickWord.openURL(Colorizer.strip(paramValue));
                    case "copyToClipboard" -> clickWord.copyToClipboard(paramValue);
                    case "showItem" -> {
                        ItemStack item = ItemUtil.fromBase64(paramValue);
                        clickWord.showItem(item == null ? new ItemStack(Material.AIR) : item);
                    }
                }
            }
        }

        clickText.send(sender);
    }

    @Nullable
    @Deprecated
    private static String getParamValue(@NotNull String from, @NotNull String param) {
        Pattern pattern = PATTERN_JSON_PARAMS.get(param);
        if (pattern == null) return null;

        Matcher matcher = RegexUtil.getMatcher(pattern, from);
        if (!RegexUtil.matcherFind(matcher)) return null;

        return matcher.group(2).stripLeading();
    }
}

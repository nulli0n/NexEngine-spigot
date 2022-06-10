package su.nexmedia.engine.utils;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.utils.json.text.ClickWord;
import su.nexmedia.engine.utils.regex.RegexUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageUtil {

    private static final Pattern              PATTERN_MESSAGE_JSON_FULL   = Pattern.compile("((\\{json:)+(.+?)(\\})+(.*?))(\\{end-json\\})");
    private static final Map<String, Pattern> PATTERN_MESSAGE_JSON_PARAMS = new HashMap<>();

    static {
        for (String parameter : new String[]{"hint", "hover", "showText", "chat-type", "runCommand", "chat-suggest", "suggestCommand", "url", "openUrl", "showItem", "copyToClipboard"}) {
            PATTERN_MESSAGE_JSON_PARAMS.put(parameter, Pattern.compile("~+(" + parameter + ")+?:+(.*?);"));
        }
    }

    public static void sendActionBar(@NotNull Player player, @NotNull String msg) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(msg));
    }

    @Deprecated
    public static void sendTitles(@NotNull Player player, @NotNull String title, @NotNull String subtitle, int fadeIn, int stay, int fadeOut) {
        player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
    }

    @Deprecated
    public static void sound(@NotNull Player player, @NotNull String sound) {
        sound(player, CollectionsUtil.getEnum(sound, Sound.class));
    }

    public static void sound(@NotNull Player player, @Nullable Sound sound) {
        if (sound == null) return;
        player.playSound(player.getLocation(), sound, 0.9f, 0.9f);
    }

    @Deprecated
    public static void sound(@NotNull Location location, @NotNull String sound) {
        sound(location, CollectionsUtil.getEnum(sound, Sound.class));
    }

    public static void sound(@NotNull Location location, @Nullable Sound sound) {
        World world = location.getWorld();
        if (world == null || sound == null) return;

        world.playSound(location, sound, 0.9f, 0.9f);
    }

    public static boolean isJSON(@NotNull String str) {
        Matcher matcher = RegexUtil.getMatcher(PATTERN_MESSAGE_JSON_FULL, str);
        return matcher != null;
    }

    @NotNull
    public static String stripJson(@NotNull String message) {
        Matcher matcher = RegexUtil.getMatcher(PATTERN_MESSAGE_JSON_FULL, message);
        if (matcher == null) return message;

        while (matcher.find()) {
            String jsonRaw = matcher.group(0); // Full json text, like '{json: <args>}Text{end-json}
            String jsonText = matcher.group(5); // The text to apply JSON on.

            message = message.replace(jsonRaw, jsonText);
        }
        return message;
    }

    public static void sendWithJSON(@NotNull CommandSender sender, @NotNull String message) {
        message = StringUtil.color(message.replace("\n", " "));

        Matcher matcher = RegexUtil.getMatcher(PATTERN_MESSAGE_JSON_FULL, message);
        if (matcher == null) {
            sender.sendMessage(message);
            return;
        }

        Map<String, String> textParams = new HashMap<>();
        while (matcher.find()) {
            String jsonRaw = matcher.group(0); // Full json text, like '{json: <args>}Text{end-json}
            String jsonArgs = matcher.group(3).trim(); // Only json parameters, like '~hover: Text; ~openUrl: google.com;'
            String jsonText = matcher.group(5); // The text to apply JSON on.

            message = message.replace(jsonRaw, jsonText);
            textParams.put(jsonText, jsonArgs);
        }

        su.nexmedia.engine.utils.json.text.ClickText clickText = new su.nexmedia.engine.utils.json.text.ClickText(message);

        for (Map.Entry<String, String> entry : textParams.entrySet()) {
            String text = entry.getKey();
            String params = entry.getValue();

            ClickWord clickWord = clickText.addComponent(text);
            for (Map.Entry<String, Pattern> entryParams : PATTERN_MESSAGE_JSON_PARAMS.entrySet()) {
                String param = entryParams.getKey();
                Matcher matcherParam = RegexUtil.getMatcher(entryParams.getValue(), params);
                if (matcherParam == null || !matcherParam.find()) {
                    continue;
                }

                String paramValue = matcherParam.group(2).stripLeading();
                switch (param) {
                    case "hint","hover","showText" -> clickWord.showText(paramValue.split("\\|"));
                    case "chat-type","runCommand" -> clickWord.runCommand(paramValue);
                    case "chat-suggest","suggestCommand" -> clickWord.suggestCommand(paramValue);
                    case "url","openUrl" -> clickWord.openURL(StringUtil.colorOff(paramValue));
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
}

package su.nexmedia.engine.utils;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexEngine;
import su.nexmedia.engine.utils.json.text.ClickWord;
import su.nexmedia.engine.utils.json.text.ClickText;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class EditorUtils {

    @Deprecated
    public static final int TUNE_DISABLED = 0;
    @Deprecated
    public static final int TUNE_ENABLED  = 1;
    @Deprecated
    public static final int TUNE_WARNING  = 2;

    private static final NexEngine ENGINE = NexEngine.get();

    public static void sendClickableTips(@NotNull Player player, @NotNull Collection<String> items2) {
        if (items2.size() >= 100) {
            List<List<String>> split = CollectionsUtil.split(new ArrayList<>(items2), 50);
            split.forEach(items3 -> sendClickableTips(player, items3));
            return;
        }

        List<String> items = items2.stream().sorted(String::compareTo).map(str -> StringUtil.color("&a" + str)).collect(Collectors.toList());
        ClickText text = new ClickText(String.join(" &8-- ", items));
        items.forEach(item -> {
            ClickWord word = text.addComponent(item);
            word.showText(ENGINE.lang().Core_Editor_Tips_Hint.getLocalized());
            word.runCommand(StringUtil.colorOff(item));
        });

        ENGINE.lang().Editor_Help_Values.send(player);
        text.send(player);
    }

    public static void sendSuggestTips(@NotNull Player player, @NotNull Collection<String> items2) {
        if (items2.size() >= 100) {
            List<List<String>> split = CollectionsUtil.split(new ArrayList<>(items2), 50);
            split.forEach(items3 -> sendSuggestTips(player, items3));
            return;
        }

        List<String> items = items2.stream().sorted(String::compareTo).map(str -> StringUtil.color("&a" + str)).collect(Collectors.toList());
        ClickText text = new ClickText(String.join(" &8-- ", items));
        items.forEach(item -> {
            ClickWord word = text.addComponent(item);
            word.showText(ENGINE.lang().Core_Editor_Tips_Hint.getLocalized());
            word.suggestCommand(StringUtil.colorOff(item));
        });

        ENGINE.lang().Editor_Help_Values.send(player);
        text.send(player);
    }

    public static void sendCommandTips(@NotNull Player player) {
        ENGINE.lang().Editor_Help_Commands.send(player);
    }

    public static void tip(@NotNull Player player, @NotNull String title, @NotNull String sub) {
        ENGINE.lang().Core_Editor_Display_Edit_Format.replace("%title%", title).replace("%message%", sub).send(player);
    }

    public static void tipCustom(@NotNull Player player, @NotNull String sub) {
        tip(player, ENGINE.lang().Core_Editor_Display_Edit_Title.getMsg(), sub);
    }

    public static void errorNumber(@NotNull Player player, boolean mustDecimal) {
        String title = ENGINE.lang().Core_Editor_Display_Error_Number_Invalid.getMsg();
        String sub = ENGINE.lang().Core_Editor_Display_Error_Number_MustInteger.getMsg();
        if (mustDecimal)
            sub = ENGINE.lang().Core_Editor_Display_Error_Number_MustDecimal.getMsg();

        tip(player, title, sub);
    }

    public static void errorCustom(@NotNull Player player, @NotNull String sub) {
        tip(player, ENGINE.lang().Core_Editor_Display_Error_Title.getMsg(), sub);
    }

    public static void errorEnum(@NotNull Player player, @NotNull Class<?> clazz) {
        String title = ENGINE.lang().Core_Editor_Display_Error_Type_Title.getMsg();
        String sub = ENGINE.lang().Core_Editor_Display_Error_Type_Values.getMsg();
        tip(player, title, sub);
        sendClickableTips(player, CollectionsUtil.getEnumsList(clazz));
    }

    @NotNull
    public static String fineId(@NotNull String id) {
        return StringUtil.colorOff(StringUtil.color(id)).toLowerCase().replace(" ", "_");
    }
}

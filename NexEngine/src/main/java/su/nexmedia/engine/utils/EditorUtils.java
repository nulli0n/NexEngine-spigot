package su.nexmedia.engine.utils;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexEngine;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class EditorUtils {

    public static final int TUNE_DISABLED = 0;
    public static final int TUNE_ENABLED  = 1;
    public static final int TUNE_WARNING  = 2;
    private static final NexEngine ENGINE = NexEngine.get();

    public static void sendClickableTips(@NotNull Player player, @NotNull Collection<String> items2) {
        if (items2.size() >= 100) {
            List<List<String>> split = CollectionsUtil.split(items2.stream().collect(Collectors.toList()), 50);
            split.stream().forEach(items3 -> sendClickableTips(player, items3));
            return;
        }

        List<String> items = items2.stream().sorted((s1, s2) -> s1.compareTo(s2)).collect(Collectors.toList());

        String full = items.stream().map(str -> "%" + str + "%").collect(Collectors.joining(" &7| "));

        ClickText text = new ClickText(full);
        items.forEach(pz -> {
            ClickText.ClickWord word = text.createPlaceholder("%" + pz + "%", "&a" + pz);
            word.hint(ENGINE.lang().Core_Editor_Tips_Hint.getMsg());
            word.execCmd(pz);
        });

        ENGINE.lang().Core_Editor_Tips_Header.send(player);
        text.send(player);
    }

    public static void sendSuggestTips(@NotNull Player player, @NotNull Collection<String> items2) {
        if (items2.size() >= 100) {
            List<List<String>> split = CollectionsUtil.split(items2.stream().collect(Collectors.toList()), 50);
            split.stream().forEach(items3 -> sendSuggestTips(player, items3));
            return;
        }

        List<String> items = items2.stream().sorted((s1, s2) -> s1.compareTo(s2)).collect(Collectors.toList());

        String full = items.stream().map(str -> "%" + str + "%").collect(Collectors.joining(" &7| "));

        ClickText text = new ClickText(full);
        items.forEach(pz -> {
            ClickText.ClickWord word = text.createPlaceholder("%" + pz + "%", "&a" + pz);
            word.hint(ENGINE.lang().Core_Editor_Tips_Hint.getMsg());
            word.suggCmd(pz);
        });

        ENGINE.lang().Core_Editor_Tips_Header.send(player);
        text.send(player);
    }

    public static void sendCommandTips(@NotNull Player player) {
        ENGINE.lang().Core_Editor_Tips_Commands.send(player);
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

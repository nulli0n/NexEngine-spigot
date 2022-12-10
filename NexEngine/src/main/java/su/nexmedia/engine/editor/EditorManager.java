package su.nexmedia.engine.editor;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.NexEngine;
import su.nexmedia.engine.Version;
import su.nexmedia.engine.api.editor.EditorInput;
import su.nexmedia.engine.api.editor.EditorObject;
import su.nexmedia.engine.api.manager.AbstractManager;
import su.nexmedia.engine.api.manager.IListener;
import su.nexmedia.engine.api.menu.IMenu;
import su.nexmedia.engine.lang.EngineLang;
import su.nexmedia.engine.utils.CollectionsUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nexmedia.engine.utils.json.text.ClickText;
import su.nexmedia.engine.utils.json.text.ClickWord;

import java.util.*;

public class EditorManager extends AbstractManager<NexEngine> implements IListener {

    private static final NexEngine ENGINE = NexEngine.get();
    private static final Map<Player, Map.Entry<IMenu, Integer>> EDITOR_CACHE_MENU  = new WeakHashMap<>();
    private static final Map<Player, EditorObject<?,?>>         EDITOR_CACHE_INPUT = new WeakHashMap<>();

    private static final String EXIT       = "#exit";
    private static final int    TITLE_STAY = Short.MAX_VALUE;

    @Deprecated private static final String TIP_TITLE = StringUtil.color("&a&lEditing");
    @Deprecated private static final String ERROR_TITLE = StringUtil.color("&c&lError!");
    @Deprecated public static final String ERROR_NUM_INVALID = StringUtil.color("&7Invalid Number!");
    @Deprecated public static final String ERROR_NUM_NOT_INT = StringUtil.color("&7Number must be &fInteger&7!");
    @Deprecated public static final String ERROR_ENUM = StringUtil.color("&7Invalid Type! See in chat.");

    public EditorManager(@NotNull NexEngine plugin) {
        super(plugin);
    }

    @Override
    protected void onLoad() {
        this.registerListeners();
    }

    @Override
    protected void onShutdown() {
        this.unregisterListeners();
    }

    @Override
    public final void registerListeners() {
        this.plugin.getPluginManager().registerEvents(this, this.plugin);
    }

    @Nullable
    public static EditorObject<?, ?> getEditorInput(@NotNull Player player) {
        return EDITOR_CACHE_INPUT.get(player);
    }

    public static boolean isEditing(@NotNull Player player) {
        return getEditorInput(player) != null;
    }

    public static <T,E extends Enum<E>> void startEdit(@NotNull Player player, @NotNull T object, @NotNull E type, @NotNull EditorInput<T,E> input) {
        EDITOR_CACHE_INPUT.put(player, new EditorObject<>(object, type, input));

        IMenu menu = IMenu.getMenu(player);
        if (menu != null) {
            EDITOR_CACHE_MENU.put(player, new AbstractMap.SimpleEntry<>(menu, menu.getPage(player)));
        }
        ENGINE.getMessage(EngineLang.EDITOR_TIP_EXIT).send(player);
    }

    public static void endEdit(@NotNull Player player) {
        endEdit(player, true);
    }

    public static void endEdit(@NotNull Player player, boolean msg) {
        EDITOR_CACHE_INPUT.remove(player);

        Map.Entry<IMenu, Integer> entry = EDITOR_CACHE_MENU.remove(player);
        if (entry != null) {
            entry.getKey().open(player, entry.getValue());
        }

        player.sendTitle(ENGINE.getMessage(EngineLang.EDITOR_TITLE_DONE).getLocalized(), "", 10, 40, 10);
    }

    public static void suggestValues(@NotNull Player player, @NotNull Collection<String> items2, boolean autoRun) {
        if (items2.size() >= 100) {
            List<List<String>> split = CollectionsUtil.split(new ArrayList<>(items2), 50);
            split.forEach(items3 -> suggestValues(player, items3, autoRun));
            return;
        }

        boolean fixCommand = Version.isAbove(Version.V1_18_R2);

        List<String> items = items2.stream().sorted(String::compareTo).map(str -> StringUtil.color("&e" + str)).toList();
        ClickText text = new ClickText(String.join(" &8-- ", items));
        items.forEach(item -> {
            ClickWord word = text.addComponent(StringUtil.colorOff(item), item);
            word.showText(StringUtil.color("&7Click me to select &f" + item));

            if (autoRun && fixCommand && !item.startsWith("/")) item = "/" + item;

            if (autoRun) word.runCommand(StringUtil.colorOff(item));
            else word.suggestCommand(StringUtil.colorOff(item));
        });

        player.sendMessage(StringUtil.color("&6&m---------&6&l[ &e&lSuggested&7/&e&lAvailable Values &6&l]&6&m---------"));
        text.send(player);
    }

    public static void sendCommandTips(@NotNull Player player) {
        String text = StringUtil.color("""
        &7
        &b&lCommand Syntax:
        &2• &a'[CONSOLE] <command>' &2- Execute as Console.
        &2• (no prefix) &a'<command>' &2- Execute as a Player.
        &7
        &b&lCommand Placeholders:
        &2• &a%player_name% &2- For player name.
        &7
        &b&lCommand Examples:
        &2▸ &a[CONSOLE] eco give %player_name% 250
        &2▸ &abroadcast Hello!
        &7""");
        player.sendMessage(text);
    }

    public static void tip(@NotNull Player player, @NotNull String text) {
        tip(player, ENGINE.getMessage(EngineLang.EDITOR_TITLE_EDIT).getLocalized(), text);
    }

    public static void tip(@NotNull Player player, @NotNull String title, @NotNull String text) {
        player.sendTitle(StringUtil.color(title), StringUtil.color(text), 20, TITLE_STAY, 40);
    }

    public static void error(@NotNull Player player, @NotNull String text) {
        error(player, ENGINE.getMessage(EngineLang.EDITOR_TITLE_ERROR).getLocalized(), text);
    }

    public static void error(@NotNull Player player, @NotNull String title, @NotNull String text) {
        tip(player, title, text);
    }

    @NotNull
    public static String fineId(@NotNull String id) {
        return StringUtil.colorOff(StringUtil.color(id)).toLowerCase().replace(" ", "_");
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();

        EditorObject<?, ?> editorInput = getEditorInput(player);
        if (editorInput == null) return;

        e.getRecipients().clear();
        e.setCancelled(true);

        String msg = StringUtil.color(e.getMessage());
        this.plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (msg.equalsIgnoreCase(EXIT) || editorInput.handle(player, e)) {
                endEdit(player);
            }
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChatCommand(PlayerCommandPreprocessEvent e) {
        Player player = e.getPlayer();

        EditorObject<?, ?> editorInput = getEditorInput(player);
        if (editorInput == null) return;

        e.setCancelled(true);

        String msg = StringUtil.color(e.getMessage().substring(1));
        AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(true, player, msg, new HashSet<>());

        this.plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (msg.equalsIgnoreCase(EXIT) || editorInput.handle(player, event)) {
                endEdit(player);
            }
        });
    }
}

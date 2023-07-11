package su.nexmedia.engine.editor;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.NexEngine;
import su.nexmedia.engine.Version;
import su.nexmedia.engine.api.editor.InputHandler;
import su.nexmedia.engine.api.manager.AbstractManager;
import su.nexmedia.engine.api.menu.impl.Menu;
import su.nexmedia.engine.api.menu.impl.MenuViewer;
import su.nexmedia.engine.lang.EngineLang;
import su.nexmedia.engine.utils.Colorizer;
import su.nexmedia.engine.utils.EngineUtils;
import su.nexmedia.engine.utils.Pair;
import su.nexmedia.engine.utils.message.NexComponent;
import su.nexmedia.engine.utils.message.NexMessage;

import java.util.*;
import java.util.stream.Collectors;

public class EditorManager extends AbstractManager<NexEngine> {

    private static final Map<Player, Pair<Menu<?>, Integer>> CACHE_MENU   = new WeakHashMap<>();
    private static final Map<Player, InputHandler>           CACHE_INPUT  = new WeakHashMap<>();
    private static final Map<Player, List<String>>           CACHE_VALUES = new WeakHashMap<>();

    public static final String EXIT       = "#exit";
    static final String VALUES     = "#values";
    static final int    TITLE_STAY = Short.MAX_VALUE;

    public EditorManager(@NotNull NexEngine plugin) {
        super(plugin);
    }

    @Override
    protected void onLoad() {
        this.addListener(new EditorListener(this.plugin));
    }

    @Override
    protected void onShutdown() {
        CACHE_MENU.clear();
        CACHE_INPUT.clear();
        CACHE_VALUES.clear();
    }

    @Nullable
    public static InputHandler getInputHandler(@NotNull Player player) {
        return CACHE_INPUT.get(player);
    }

    public static boolean isEditing(@NotNull Player player) {
        return getInputHandler(player) != null;
    }

    public static void startEdit(@NotNull Player player, @NotNull InputHandler handler) {
        CACHE_INPUT.put(player, handler);

        Menu<?> menu = Menu.getMenu(player);
        if (menu != null) {
            MenuViewer viewer = menu.getViewer(player);
            int page = viewer == null ? 1 : viewer.getPage();
            CACHE_MENU.put(player, Pair.of(menu, page));
        }
        EngineUtils.ENGINE.getMessage(EngineLang.EDITOR_TIP_EXIT).send(player);
    }

    public static void endEdit(@NotNull Player player) {
        endEdit(player, true);
    }

    public static void endEdit(@NotNull Player player, boolean msg) {
        if (CACHE_INPUT.remove(player) != null) {
            Pair<Menu<?>, Integer> entry = CACHE_MENU.remove(player);
            if (entry != null) {
                entry.getFirst().open(player, entry.getSecond());
            }
        }
        CACHE_VALUES.remove(player);

        player.sendTitle(EngineUtils.ENGINE.getMessage(EngineLang.EDITOR_TITLE_DONE).getLocalized(), "", 10, 40, 10);
    }

    public static void suggestValues(@NotNull Player player, @NotNull Collection<String> values, boolean autoRun) {
        List<String> items = values.stream().sorted(String::compareTo).collect(Collectors.toCollection(ArrayList::new));

        CACHE_VALUES.put(player, items);
        displayValues(player, autoRun, 1);
    }

    public static void displayValues(@NotNull Player player, boolean autoRun, int page) {
        List<String> values = CACHE_VALUES.get(player);
        if (values == null || values.isEmpty()) return;

        int perPage = 10;
        int pages = (int) Math.ceil((double) values.size() / (double) perPage);
        if (page < 1) page = 1;
        else if (page > pages) page = pages;
        int skip = (page - 1) * perPage;

        boolean isLastPage = page == pages;
        boolean isFirstPage = page == 1;
        boolean fixCommand = Version.isAbove(Version.V1_18_R2);
        String prefix = ChatColor.DARK_GRAY + "> ";
        String header = ChatColor.GOLD + "=".repeat(8) + "[ " + ChatColor.YELLOW + "Value Helper" + ChatColor.GOLD + " ]" + "=".repeat(8);
        String footer = ChatColor.GOLD + "=".repeat(9) + ChatColor.GRAY + " [<] " + ChatColor.YELLOW + page + ChatColor.GOLD + "/" + ChatColor.YELLOW + pages + ChatColor.GRAY + " [>] " + ChatColor.GOLD + "=".repeat(9);

        List<String> items = new ArrayList<>(values.stream().skip(skip).limit(perPage).toList());
        List<String> prefixed = items.stream().map(str -> prefix + str).toList();

        NexMessage message = new NexMessage(String.join("\n", prefixed) + "\n" + footer);
        items.forEach(item -> {
            NexComponent component = message.addComponent(Colorizer.strip(item), ChatColor.GREEN + item);
            component.showText(ChatColor.GRAY + "Click me to select " + ChatColor.AQUA + item);

            if (autoRun && fixCommand && !item.startsWith("/")) item = "/" + item;

            if (autoRun) component.runCommand(Colorizer.strip(item));
            else component.suggestCommand(Colorizer.strip(item));
        });
        if (!isFirstPage) message.addComponent("[<]", ChatColor.RED + "[<]").showText(ChatColor.GRAY + "Previous Page").runCommand("/" + VALUES + " " + (page - 1) + " " + autoRun);
        if (!isLastPage) message.addComponent("[>]", ChatColor.RED + "[>]").showText(ChatColor.GRAY + "Next Page").runCommand("/" + VALUES + " " + (page + 1) + " " + autoRun);

        player.sendMessage(header);
        message.send(player);
    }

    public static void prompt(@NotNull Player player, @NotNull String text) {
        tip(player, EngineUtils.ENGINE.getMessage(EngineLang.EDITOR_TITLE_EDIT).getLocalized(), text);
    }

    public static void error(@NotNull Player player, @NotNull String text) {
        error(player, EngineUtils.ENGINE.getMessage(EngineLang.EDITOR_TITLE_ERROR).getLocalized(), text);
    }

    public static void tip(@NotNull Player player, @NotNull String title, @NotNull String text) {
        player.sendTitle(Colorizer.apply(title), Colorizer.apply(text), 20, TITLE_STAY, 40);
    }

    public static void error(@NotNull Player player, @NotNull String title, @NotNull String text) {
        tip(player, title, text);
    }
}

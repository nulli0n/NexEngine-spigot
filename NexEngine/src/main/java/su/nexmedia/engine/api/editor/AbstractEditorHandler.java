package su.nexmedia.engine.api.editor;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.api.manager.AbstractManager;
import su.nexmedia.engine.api.manager.IListener;
import su.nexmedia.engine.api.menu.IMenu;
import su.nexmedia.engine.utils.Constants;
import su.nexmedia.engine.utils.StringUtil;
import su.nexmedia.engine.utils.json.text.ClickText;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

public abstract class AbstractEditorHandler<P extends NexPlugin<P>, C extends Enum<C>> extends AbstractManager<P> implements IListener {

    private static final Map<Player, Map.Entry<IMenu, Integer>> EDITOR_CACHE_MENU = new WeakHashMap<>();
    private final        Map<Player, Map.Entry<C, Object>>      EDITOR_CACHE      = new WeakHashMap<>();
    protected Map<Class<?>, EditorInputHandler<C, ?>> handlers;

    public AbstractEditorHandler(@NotNull P plugin) {
        super(plugin);
    }

    @Override
    protected void onLoad() {
        this.handlers = new HashMap<>();
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

    @SuppressWarnings("unchecked")
    @Nullable
    public <T> EditorInputHandler<C, T> getInputHandler(@NotNull Class<T> clazz) {
        for (Map.Entry<Class<?>, EditorInputHandler<C, ?>> entry : this.handlers.entrySet()) {
            if (entry.getKey().isAssignableFrom(clazz)) {
                return (EditorInputHandler<C, T>) entry.getValue();
            }
        }
        return null;
    }

    public <T> void addInputHandler(@NotNull Class<T> clazz, @NotNull EditorInputHandler<C, T> handler) {
        this.handlers.put(clazz, handler);
    }

    public <T> boolean removeInputHandler(@NotNull Class<T> clazz) {
        return this.handlers.remove(clazz) != null;
    }

    @Nullable
    public Map.Entry<C, Object> getEditor(@NotNull Player player) {
        return EDITOR_CACHE.getOrDefault(player, null);
    }

    public boolean isEdit(@NotNull Player player) {
        return this.getEditor(player) != null;
    }

    public void startEdit(@NotNull Player player, @Nullable Object object, C type) {
        EDITOR_CACHE.put(player, new AbstractMap.SimpleEntry<>(type, object));

        IMenu menu = IMenu.getMenu(player);
        if (menu != null) {
            EDITOR_CACHE_MENU.put(player, new AbstractMap.SimpleEntry<>(menu, menu.getPage(player)));
        }

        String s = plugin.lang().Core_Editor_Tips_Exit_Name.getLocalized();
        ClickText text = new ClickText(s);
        text.addComponent(s).runCommand("/" + Constants.EXIT).showText(plugin.lang().Core_Editor_Tips_Exit_Hint.getLocalized());
        text.send(player);
    }

    public void endEdit(@NotNull Player player) {
        this.endEdit(player, true);
    }

    public void endEdit(@NotNull Player player, boolean msg) {
        EDITOR_CACHE.remove(player);

        Map.Entry<IMenu, Integer> entry = EDITOR_CACHE_MENU.remove(player);
        if (entry != null) {
            plugin().getServer().getScheduler().runTask(plugin, c -> {
                entry.getKey().open(player, entry.getValue());
            });
        }

        player.sendTitle(plugin.lang().Core_Editor_Display_Done_Title.getLocalized(), "", 10, 40, 10);
    }

    @SuppressWarnings("unchecked")
    protected boolean onType(@NotNull Player player, @NotNull Object object, @NotNull C type, @NotNull String input) {
        EditorInputHandler<C, Object> inputHandler = this.getInputHandler((Class<Object>) object.getClass());
        return inputHandler == null || inputHandler.onType(player, object, type, input);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();

        Map.Entry<C, Object> editor = this.getEditor(player);
        if (editor == null) return;

        e.getRecipients().clear();
        e.setCancelled(true);

        String msg = StringUtil.color(e.getMessage());
        if (msg.equalsIgnoreCase(Constants.EXIT)) {
            this.endEdit(player);
            return;
        }

        // Sync Output Handler
        // to avoid exceptions when changing non-async objects
        this.plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (this.onType(player, editor.getValue(), editor.getKey(), msg)) {
                this.endEdit(player);
            }
        });
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onEditorCommand(PlayerCommandPreprocessEvent e) {
        Player player = e.getPlayer();
        String msg = StringUtil.color(e.getMessage().substring(1));

        Map.Entry<C, Object> editor = this.getEditor(player);
        if (editor == null) return;

        if (msg.equalsIgnoreCase(Constants.EXIT)) {
            e.setCancelled(true);
            this.endEdit(player);
            return;
        }

        // Sync Output Handler
        // to avoid exceptions when changing non-async objects
        this.plugin.getServer().getScheduler().runTask(plugin, () -> {
            if (this.onType(player, editor.getValue(), editor.getKey(), msg)) {
                this.endEdit(player);
            }
        });
    }
}

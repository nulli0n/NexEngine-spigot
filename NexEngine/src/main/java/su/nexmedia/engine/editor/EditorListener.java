package su.nexmedia.engine.editor;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexEngine;
import su.nexmedia.engine.api.editor.InputHandler;
import su.nexmedia.engine.api.editor.InputWrapper;
import su.nexmedia.engine.api.manager.AbstractListener;
import su.nexmedia.engine.utils.Colorizer;
import su.nexmedia.engine.utils.StringUtil;

import java.util.HashSet;

public class EditorListener extends AbstractListener<NexEngine> {

    public EditorListener(@NotNull NexEngine plugin) {
        super(plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        EditorManager.endEdit(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChatText(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        InputHandler handler = EditorManager.getInputHandler(player);
        if (handler == null) return;

        event.getRecipients().clear();
        event.setCancelled(true);

        InputWrapper wrapper = new InputWrapper(event);

        this.plugin.runTask(task -> {
            if (wrapper.getTextRaw().equalsIgnoreCase(EditorManager.EXIT) || handler.handle(wrapper)) {
                EditorManager.endEdit(player);
            }
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChatCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        InputHandler handler = EditorManager.getInputHandler(player);
        if (handler == null) return;

        event.setCancelled(true);

        String raw = event.getMessage();
        String text = Colorizer.apply(raw.substring(1));
        if (text.startsWith(EditorManager.VALUES)) {
            String[] split = text.split(" ");
            int page = split.length >= 2 ? StringUtil.getInteger(split[1], 0) : 0;
            boolean auto = split.length >= 3 && Boolean.parseBoolean(split[2]);
            EditorManager.displayValues(player, auto, page);
            return;
        }

        AsyncPlayerChatEvent chatEvent = new AsyncPlayerChatEvent(true, player, text, new HashSet<>());
        InputWrapper wrapper = new InputWrapper(chatEvent);

        this.plugin.runTask(task -> {
            if (wrapper.getTextRaw().equalsIgnoreCase(EditorManager.EXIT) || handler.handle(wrapper)) {
                EditorManager.endEdit(player);
            }
        });
    }
}

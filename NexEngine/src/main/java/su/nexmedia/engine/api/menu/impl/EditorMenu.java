package su.nexmedia.engine.api.menu.impl;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.api.editor.EditorHandler;
import su.nexmedia.engine.api.editor.EditorLocale;
import su.nexmedia.engine.api.editor.EditorLocales;
import su.nexmedia.engine.api.editor.InputHandler;
import su.nexmedia.engine.api.lang.LangKey;
import su.nexmedia.engine.api.lang.LangMessage;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.api.menu.click.ClickHandler;
import su.nexmedia.engine.api.menu.item.MenuItem;
import su.nexmedia.engine.editor.EditorManager;
import su.nexmedia.engine.utils.ItemUtil;

public class EditorMenu<P extends NexPlugin<P>, T> extends Menu<P> {

    protected final T object;

    public EditorMenu(@NotNull P plugin, @NotNull T object, @NotNull String title, int size) {
        super(plugin, title, size);
        this.object = object;
    }

    @NotNull
    protected MenuItem addNextPage(int... slots) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        ItemUtil.setSkullTexture(item, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjgyYWQxYjljYjRkZDIxMjU5YzBkNzVhYTMxNWZmMzg5YzNjZWY3NTJiZTM5NDkzMzgxNjRiYWM4NGE5NmUifX19");
        MenuItem menuItem = this.addItem(item, EditorLocales.NEXT_PAGE, slots);
        menuItem.setType(MenuItemType.PAGE_NEXT);
        menuItem.setClick(ClickHandler.forNextPage(this));
        return menuItem;
    }

    @NotNull
    protected MenuItem addPreviousPage(int... slots) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        ItemUtil.setSkullTexture(item, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzdhZWU5YTc1YmYwZGY3ODk3MTgzMDE1Y2NhMGIyYTdkNzU1YzYzMzg4ZmYwMTc1MmQ1ZjQ0MTlmYzY0NSJ9fX0=");
        MenuItem menuItem = this.addItem(item, EditorLocales.PREVIOUS_PAGE, slots);
        menuItem.setType(MenuItemType.PAGE_PREVIOUS);
        menuItem.setClick(ClickHandler.forPreviousPage(this));
        return menuItem;
    }

    @NotNull
    protected MenuItem addReturn(int... slots) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        ItemUtil.setSkullTexture(item, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTM4NTJiZjYxNmYzMWVkNjdjMzdkZTRiMGJhYTJjNWY4ZDhmY2E4MmU3MmRiY2FmY2JhNjY5NTZhODFjNCJ9fX0=");
        return this.addItem(item, EditorLocales.RETURN, slots);
    }

    @NotNull
    protected MenuItem addCreation(@NotNull EditorLocale locale, int... slots) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        ItemUtil.setSkullTexture(item, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWZmMzE0MzFkNjQ1ODdmZjZlZjk4YzA2NzU4MTA2ODFmOGMxM2JmOTZmNTFkOWNiMDdlZDc4NTJiMmZmZDEifX19");
        return this.addItem(item, locale, slots);
    }

    @NotNull
    protected MenuItem addExit(int... slots) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        ItemUtil.setSkullTexture(item, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmViNTg4YjIxYTZmOThhZDFmZjRlMDg1YzU1MmRjYjA1MGVmYzljYWI0MjdmNDYwNDhmMThmYzgwMzQ3NWY3In19fQ==");
        MenuItem menuItem = this.addItem(item, EditorLocales.CLOSE, slots);
        menuItem.setClick((viewer, event) -> viewer.getPlayer().closeInventory());
        return menuItem;
    }

    @NotNull
    public MenuItem addItem(@NotNull Material material, @NotNull EditorLocale locale, int... slots) {
        return this.addItem(new ItemStack(material), locale, slots);
    }

    @NotNull
    public MenuItem addItem(@NotNull ItemStack item, @NotNull EditorLocale locale, int... slots) {
        ItemUtil.mapMeta(item, meta -> {
            meta.setDisplayName(locale.getLocalizedName());
            meta.setLore(locale.getLocalizedLore());
            meta.addItemFlags(ItemFlag.values());
        });

        MenuItem menuItem = new MenuItem(item, 100, slots);
        this.addItem(menuItem);
        return menuItem;
    }

    @Deprecated
    protected void startEdit(@NotNull Player player, @NotNull LangMessage prompt, @NotNull EditorHandler handler) {
        EditorManager.prompt(player, prompt.getLocalized());
        EditorManager.startEdit(player, handler);
        this.plugin.runTask(task -> player.closeInventory());
    }

    protected void handleInput(@NotNull MenuViewer viewer, @NotNull LangKey prompt, @NotNull InputHandler handler) {
        this.handleInput(viewer.getPlayer(), prompt, handler);
    }

    protected void handleInput(@NotNull Player player, @NotNull LangKey prompt, @NotNull InputHandler handler) {
        this.handleInput(player, this.plugin.getMessage(prompt), handler);
    }

    protected void handleInput(@NotNull Player player, @NotNull LangMessage prompt, @NotNull InputHandler handler) {
        EditorManager.prompt(player, prompt.getLocalized());
        EditorManager.startEdit(player, handler);
        this.plugin.runTask(task -> player.closeInventory());
    }

    @NotNull
    public T getObject() {
        return object;
    }
}

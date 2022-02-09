package su.nexmedia.engine.utils.json.text;

import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Item;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.StringUtil;

import java.util.List;

public class ClickWord {

    private final String text;

    public  HoverEvent hoverEvent;
    public  ClickEvent clickEvent;

    public ClickWord(@NotNull String text) {
        this.text = StringUtil.color(text);
    }

    @NotNull
    public String getText() {
        return this.text;
    }

    @Nullable
    public HoverEvent getHoverEvent() {
        return this.hoverEvent;
    }

    @Nullable
    public ClickEvent getClickEvent() {
        return this.clickEvent;
    }

    @NotNull
    public ClickWord showText(@NotNull String text) {
        return this.showText(text.split("\n"));
    }

    @NotNull
    public ClickWord showText(@NotNull List<String> text) {
        return this.showText(text.toArray(new String[0]));
    }

    @NotNull
    public ClickWord showText(@NotNull String... text) {
        BaseComponent[] base = TextComponent.fromLegacyText(StringUtil.color(String.join("\n", text)));
        this.hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(base));
        return this;
    }

    @NotNull
    public ClickWord showItem(@NotNull ItemStack item) {
        Item item1 = new Item(item.getType().getKey().getKey(), item.getAmount(), ItemTag.ofNbt(ItemUtil.getNBTTag(item)));
        this.hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_ITEM, item1);
        return this;
    }

    @NotNull
    public ClickWord runCommand(@NotNull String command) {
        this.clickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND, command);
        return this;
    }

    @NotNull
    public ClickWord suggestCommand(@NotNull String command) {
        this.clickEvent = new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command);
        return this;
    }

    @NotNull
    public ClickWord openURL(@NotNull String url) {
        this.clickEvent = new ClickEvent(ClickEvent.Action.OPEN_URL, url);
        return this;
    }

    @NotNull
    public ClickWord copyToClipboard(@NotNull String text) {
        this.clickEvent = new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, text);
        return this;
    }

    @NotNull
    public TextComponent build() {
        TextComponent component = new TextComponent(TextComponent.fromLegacyText(this.getText()));
        if (this.hoverEvent != null) {
            component.setHoverEvent(this.getHoverEvent());
        }
        if (this.clickEvent != null) {
            component.setClickEvent(this.getClickEvent());
        }
        return component;
    }
}

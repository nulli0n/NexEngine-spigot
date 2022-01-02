package su.nexmedia.engine.utils;

import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @author Fogus® Multimedia
 */
public class ClickText {

    private String                 text;
    private ComponentBuilder       builder;
    private Map<String, ClickWord> replacers;

    public ClickText(@NotNull String text) {
        this.text = StringUtil.color(text);
        this.builder = new ComponentBuilder("");
        this.replacers = new HashMap<>();
    }

    @NotNull
    public ClickWord createFullPlaceholder() {
        ClickWord clickWord = new ClickWord(this.text);
        this.replacers.put(Constants.MASK_ANY, clickWord);
        return clickWord;
    }

    @NotNull
    public ClickWord createPlaceholder(@NotNull String placeholder, @NotNull String text) {
        ClickWord clickWord = new ClickWord(text);
        this.replacers.put(placeholder, clickWord);
        return clickWord;
    }

    @NotNull
    private BaseComponent[] build(@NotNull String line) {
        this.builder = new ComponentBuilder("");

        // Support for full line the single ClickText
        ClickWord full = this.replacers.get(Constants.MASK_ANY);
        if (full != null) {
            this.builder.append(full.build());// .event(full.getClickEvent()).event(full.getHoverEvent());
            return this.builder.create();
        }

        String[] words = line.split(" ");
        for (String word : words) {

            // Get text Json data if it present.
            Optional<String> optWord = this.replacers.keySet().stream().filter(holder -> word.contains(holder)).findFirst();
            ClickWord clickWord = optWord.isPresent() ? this.replacers.get(optWord.get()) : null;

            if (clickWord != null) {
                this.builder.append(clickWord.build());
            }
            // FIXME: Saves bold, underline, etc. formatting for the next words even with
            // &r.
            // Probably needs to be reworked for 1.16+
            else {
                this.builder.append(TextComponent.fromLegacyText(word), ComponentBuilder.FormatRetention.FORMATTING);
            }
            this.builder.append(TextComponent.fromLegacyText(" "), ComponentBuilder.FormatRetention.FORMATTING);
        }
        return this.builder.create();
    }

    public void send(@NotNull CommandSender sender) {
        if (sender instanceof Player) {
            for (String line : this.text.split("\n")) {
                ((Player) sender).spigot().sendMessage(this.build(line));
            }
        }
    }

    public void send(@NotNull Set<Player> list) {
        for (String line : this.text.split("\n")) {
            BaseComponent[] jsonText = this.build(line);
            for (Player player : list) {
                player.spigot().sendMessage(jsonText);
            }
        }
    }

    public static class ClickWord {

        public  HoverEvent hover;
        public  ClickEvent click;
        private String     text;

        public ClickWord(@NotNull String text) {
            this.text = StringUtil.color(text);
        }

        @NotNull
        public String getText() {
            return this.text;
        }

        @NotNull
        public HoverEvent getHoverEvent() {
            return this.hover;
        }

        @NotNull
        public ClickEvent getClickEvent() {
            return this.click;
        }

        @NotNull
        public ClickWord hint(@NotNull String text) {
            return this.hint(text.split("\n"));
        }

        @NotNull
        public ClickWord hint(@NotNull List<String> text) {
            return this.hint(text.toArray(new String[0]));
        }

        @NotNull
        public ClickWord hint(@NotNull String... text) {
            StringBuilder textBuilder = new StringBuilder();
            for (String line : text) {
                if (textBuilder.length() > 0) {
                    textBuilder.append("\n");
                }
                textBuilder.append(line);
            }

            BaseComponent[] base = TextComponent.fromLegacyText(StringUtil.color(textBuilder.toString()));
            this.hover = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(base));
            return this;
        }

        @SuppressWarnings("deprecation")
        @NotNull
        public ClickWord showItem(@NotNull ItemStack item) {
            // if (Version.CURRENT.isHigher(Version.V1_15_R1)) {
            // Item item2 = new Item("f_item", item.getAmount(),
            // ItemTag.ofNbt(FogusCore.get().getNMS().getNbtString(item)));
            // this.hover = new HoverEvent(HoverEvent.Action.SHOW_ITEM, item2);
            // }
            // else {
            this.hover = new HoverEvent(HoverEvent.Action.SHOW_ITEM, toBase(item));
            // }
            return this;
        }

        @Deprecated
        public void showEntity(@NotNull String json) {
            this.hover = new HoverEvent(HoverEvent.Action.SHOW_ENTITY, toBase(json));
        }

        @Deprecated
        public void achievement(@NotNull String id) {
            this.hover = new HoverEvent(HoverEvent.Action.SHOW_ACHIEVEMENT, toBase(StringUtil.colorOff(id)));
        }

        //

        @NotNull
        public ClickWord execCmd(@NotNull String cmd) {
            this.click = new ClickEvent(ClickEvent.Action.RUN_COMMAND, cmd);
            return this;
        }

        @NotNull
        public ClickWord suggCmd(@NotNull String cmd) {
            this.click = new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, cmd);
            return this;
        }

        @NotNull
        public ClickWord url(@NotNull String url) {
            this.click = new ClickEvent(ClickEvent.Action.OPEN_URL, url);
            return this;
        }

        @NotNull
        public ClickWord file(@NotNull String path) {
            this.click = new ClickEvent(ClickEvent.Action.OPEN_FILE, path);
            return this;
        }

        @Deprecated
        public void page(int page) {
            this.click = new ClickEvent(ClickEvent.Action.CHANGE_PAGE, String.valueOf(page));
        }

        //

        @NotNull
        public TextComponent build() {
            TextComponent component = new TextComponent(TextComponent.fromLegacyText(this.text));
            if (this.hover != null) {
                component.setHoverEvent(this.hover);
            }
            if (this.click != null) {
                component.setClickEvent(this.click);
            }
            return component;
        }

        @NotNull
        @Deprecated
        private BaseComponent[] toBase(@NotNull String text) {
            return new BaseComponent[]{new TextComponent(text)};
        }

        @NotNull
        private BaseComponent[] toBase(@NotNull ItemStack item) {
            String json = ItemUtil.toJson(item);
            return TextComponent.fromLegacyText(json);
        }
    }
}

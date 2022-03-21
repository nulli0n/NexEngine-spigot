package su.nexmedia.engine.utils.json.text;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.utils.StringUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author NightExpress
 */
public class ClickText {

    private String                       text;
    private final Map<String, ClickWord> components;

    public ClickText(@NotNull String text) {
        this.text = StringUtil.color(text);
        this.components = new HashMap<>();
    }

    @NotNull
    public ClickWord addComponent(@NotNull String text) {
        ClickWord clickWord = new ClickWord(text);
        String placeholder = "%" + this.components.size() + "%";

        this.components.put(placeholder, clickWord);
        this.text = this.text.replace(StringUtil.color(text), placeholder);
        return clickWord;
    }

    @NotNull
    private BaseComponent[] build(@NotNull String line) {
        ComponentBuilder builder = new ComponentBuilder();

        String[] words = line.split(" ");
        for (String word : words) {

            boolean isFirst = builder.getParts().isEmpty();
            ChatColor colorLast = builder.getCurrentComponent().getColor();

            //System.out.println("------------");
            //System.out.println("word: '" + word + "'");

            ClickWord clickWord = this.components.get(StringUtil.colorOff(word));
            this.append(builder, word, clickWord);

            ChatColor colorCurrent = builder.getCurrentComponent().getColor();

            // Save text formations (bold, italic, etc.) for the same colors and reset it when the different color starts.
            // (я не придумал ничего лучше)
            if (clickWord != null && builder.getCursor() != 0 || (!isFirst && !colorCurrent.getName().equalsIgnoreCase(colorLast.getName()))) {
                //System.out.println("do reset");
                TextComponent component = (TextComponent) builder.getCurrentComponent();
                //System.out.println("current comp: " + component.toString());

                // Remove the recently added component to reset the text formatting.
                // For components with a text or extra elements, or strings with only color, remove the latest one.
                if (StringUtil.colorOff(word).isEmpty() || (!component.getText().isEmpty() || component.getExtra() != null)) {
                    // Because a multicolor word is being spliited into multiple components by TextComponent.fromLegacyText
                    // we have to get that array here and remove all of them.
                    BaseComponent[] componentWord = TextComponent.fromLegacyText(word);
                    for (BaseComponent baseComponent : componentWord) {
                        builder.getParts().remove(builder.getParts().size() - 1);
                        builder.setCursor(builder.getCursor() - 1);
                    }
                }
                // If there is 'empty' components containing only colors, remove all of them until the first text is found.
                // This happens for strings with a color on the end, like: '&aText&2', where '&2' will be an empty component
                // not related to the '&aText'.
                else {
                    while (component.getText().isEmpty() && component.getExtra() == null) {
                        component = (TextComponent) builder.getCurrentComponent();
                        builder.getParts().remove(builder.getParts().size() - 1);
                        builder.setCursor(builder.getCursor() - 1);
                    }
                }
                this.cleanUp(builder);
                //builder.append(component);

                this.append(builder, word, clickWord);
            }

            this.append(builder, " ", null);
        }
        return builder.create();
    }

    private void append(@NotNull ComponentBuilder builder, @NotNull String word, @Nullable ClickWord clickWord) {
        if (clickWord != null) {
            builder.append(clickWord.build());
        }
        else {
            BaseComponent[] componentWord = TextComponent.fromLegacyText(word);
            ChatColor colorFirst = null;

            for (BaseComponent baseComponent : componentWord) {
                builder.append(baseComponent, ComponentBuilder.FormatRetention.FORMATTING);
                if (colorFirst != null && !colorFirst.getName().equalsIgnoreCase(baseComponent.getColor().getName())) {
                    this.cleanUp(builder);
                }
                colorFirst = baseComponent.getColor();
            }
            //builder.append(TextComponent.fromLegacyText(word), ComponentBuilder.FormatRetention.FORMATTING);
        }
    }

    private void cleanUp(@NotNull ComponentBuilder builder) {
        builder.bold(false).italic(false).underlined(false).strikethrough(false).obfuscated(false);
    }

    public void send(@NotNull CommandSender sender) {
        if (sender instanceof Player player) {
            for (String line : this.text.split("\n")) {
                player.spigot().sendMessage(this.build(line));
            }
        }
    }

    public void send(@NotNull CommandSender... senders) {
        Stream.of(senders).forEach(this::send);
    }

    public void send(@NotNull Collection<CommandSender> senders) {
        senders.forEach(this::send);
    }
}

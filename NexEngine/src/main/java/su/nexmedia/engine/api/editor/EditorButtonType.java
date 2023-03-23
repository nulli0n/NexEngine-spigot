package su.nexmedia.engine.api.editor;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.utils.Colorizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@Deprecated
public interface EditorButtonType {

    String PREFIX_INFO = "&6&l[?] Description:";
    String PREFIX_NOTE = "&e&l[!] Note:";
    String PREFIX_WARN = "&c&l[!] Warning:";
    String PREFIX_CLICK = "#55e136&l[>] Actions:";
    String PREFIX_CURRENT = "&b&l[?] Current:";
    String COLOR_GRAY = "&7";
    String COLOR_RED = "#C70039";
    String COLOR_GREEN = "#86de2a";
    String COLOR_YELLOW = "#FFC300";

    @NotNull String name();

    @NotNull Material getMaterial();

    void setName(@NotNull String name);

    @NotNull String getName();

    void setLore(@NotNull List<String> lore);

    @NotNull List<String> getLore();

    @NotNull
    static String current(@NotNull String text) {
        return formatted(text, PREFIX_CURRENT, "&a");
    }

    @NotNull
    static String info(@NotNull String text) {
        return formatted(split(text), PREFIX_INFO, COLOR_GRAY);
    }

    @NotNull
    static String warn(@NotNull String text) {
        return formatted(split(text), PREFIX_WARN, COLOR_RED);
    }

    @NotNull
    static String note(@NotNull String text) {
        return formatted(split(text), PREFIX_NOTE, COLOR_YELLOW);
    }

    @NotNull
    static String click(@NotNull String text) {
        return formatted(text, PREFIX_CLICK, COLOR_GREEN);
    }

    @NotNull
    static String formatted(@NotNull String text, @NotNull String prefix, @NotNull String color) {
        List<String> list = new ArrayList<>(Arrays.asList(text.split("\n")));
        list.replaceAll(line -> color + line);
        list.add(0, prefix);
        return String.join("\n", list);
    }

    @NotNull
    static List<String> fineLore(@NotNull String... lores) {
        List<String> lore = new ArrayList<>();
        Stream.of(lores).map(str -> str.split("\n")).forEach(arr -> {
            if (!lore.isEmpty()) lore.add(" ");
            lore.addAll(Arrays.asList(arr));
        });
        return lore;
    }

    @NotNull
    static String split(@NotNull String str) {
        return str.replaceAll("((?:[^\\s]*\\s){5}[^\\s]*)\\s", "$1\n");
    }

    @NotNull
    default ItemStack getItem() {
        ItemStack item = new ItemStack(this.getMaterial());
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.setDisplayName(Colorizer.apply("&e&l") + this.getName());
        meta.setLore(this.getLore());
        meta.addItemFlags(ItemFlag.values());
        item.setItemMeta(meta);

        return item;
    }
}

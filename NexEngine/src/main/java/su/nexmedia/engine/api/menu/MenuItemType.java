package su.nexmedia.engine.api.menu;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.editor.EditorButtonType;
import su.nexmedia.engine.utils.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum MenuItemType implements EditorButtonType {
    NONE,
    PAGE_NEXT(Material.ARROW, "&6&lNext Page"),
    PAGE_PREVIOUS(Material.ARROW, "&6&lPrevious Page"),
    CLOSE(Material.BARRIER, "&c&lClose"),
    RETURN(Material.BARRIER, "&c&lReturn"),
    CONFIRMATION_ACCEPT(Material.LIME_DYE, "&a&lAccept"),
    CONFIRMATION_DECLINE(Material.PINK_DYE, "&c&lDecline"),
    ;

    private final Material material;
    private       String   name;
    private List<String> lore;

    MenuItemType() {
        this(Material.AIR, "", "");
    }

    MenuItemType(@NotNull Material material, @NotNull String name, @NotNull String... lore) {
        this.material = material;
        this.setName(name);
        this.setLore(Arrays.asList(lore));
    }

    @NotNull
    @Override
    public Material getMaterial() {
        return material;
    }

    @Override
    @NotNull
    public String getName() {
        return name;
    }

    @Override
    public void setName(@NotNull String name) {
        this.name = StringUtil.color(name);
    }

    @Override
    @NotNull
    public List<String> getLore() {
        return lore;
    }

    @Override
    public void setLore(@NotNull List<String> lore) {
        this.lore = StringUtil.color(new ArrayList<>(lore));
    }
}

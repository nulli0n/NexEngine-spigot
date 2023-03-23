package su.nexmedia.engine.api.menu.impl;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MenuViewer {

    private final Player player;
    private       int    page;
    private int pages;

    public MenuViewer(@NotNull Player player) {
        this.player = player;
        this.setPage(1);
        this.setPages(1);
    }

    @NotNull
    public Player getPlayer() {
        return player;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = Math.max(1, Math.min(page, this.getPages()));
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = Math.max(1, pages);
    }
}

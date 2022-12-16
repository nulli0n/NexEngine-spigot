package su.nexmedia.engine.api.menu;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.api.task.AbstractTask;
import su.nexmedia.engine.utils.random.Rnd;

@Deprecated
public class MenuAnimationTask<P extends NexPlugin<P>> extends AbstractTask<P> {

    private final IMenu menu;
    private       long  tickCount;

    public MenuAnimationTask(@NotNull P plugin, @NotNull IMenu menu) {
        super(plugin, menu.getAnimationInterval(), false);
        this.menu = menu;
        this.tickCount = 0;
    }

    @Override
    public void action() {
        if (menu.getViewers().isEmpty()) return;

        this.menu.getItemsMap().values().forEach(menuItem -> {
            if (!menuItem.isAnimationEnabled()) return;
            if (menuItem.getAnimationFrames().length == 0) return;
            if (this.tickCount % menuItem.getAnimationTickInterval() != 0) return;

            int frame;
            if (menuItem.isAnimationRandomOrder()) {
                frame = Rnd.get(menuItem.getAnimationFrames().length);
            }
            else {
                frame = menuItem.getAnimationFrameCurrent();
            }
            if (frame >= menuItem.getAnimationFrames().length) frame = 0;

            menuItem.setAnimationFrameCurrent(frame);
        });

        this.menu.update();
        if (this.tickCount++ >= Short.MAX_VALUE) {
            this.tickCount = 0;
        }
    }
}

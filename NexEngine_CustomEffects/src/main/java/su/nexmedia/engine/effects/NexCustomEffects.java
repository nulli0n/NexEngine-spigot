package su.nexmedia.engine.effects;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.api.command.GeneralCommand;
import su.nexmedia.engine.effects.config.Config;
import su.nexmedia.engine.effects.config.Lang;
import su.nexmedia.engine.effects.entity.EntityManager;

public class NexCustomEffects extends NexPlugin<NexCustomEffects> {

    private Config cfg;
    private Lang lang;

    private EntityManager entityManager;

    @Override
    public void enable() {
        this.entityManager = new EntityManager(this);
        this.entityManager.setup();
    }

    @Override
    public void disable() {
        if (this.entityManager != null) {
            this.entityManager.shutdown();
            this.entityManager = null;
        }
    }

    @Override
    public void setConfig() {
        this.cfg = new Config(this);
        this.cfg.setup();

        this.lang = new Lang(this);
        this.lang.setup();
    }

    @Override
    public void registerHooks() {

    }

    @Override
    public void registerCommands(@NotNull GeneralCommand<NexCustomEffects> mainCommand) {

    }

    @Override
    @NotNull
    public Config cfg() {
        return this.cfg;
    }

    @Override
    @NotNull
    public Lang lang() {
        return this.lang;
    }

    @Override
    public boolean useNewConfigFields() {
        return true;
    }

    @NotNull
    public EntityManager getEntityManager() {
        return entityManager;
    }
}

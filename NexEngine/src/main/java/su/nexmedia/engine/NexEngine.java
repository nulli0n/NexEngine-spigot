package su.nexmedia.engine;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.command.GeneralCommand;
import su.nexmedia.engine.api.editor.EditorLocales;
import su.nexmedia.engine.api.menu.impl.MenuListener;
import su.nexmedia.engine.api.menu.impl.MenuRefreshTask;
import su.nexmedia.engine.command.list.ReloadSubCommand;
import su.nexmedia.engine.config.EngineConfig;
import su.nexmedia.engine.editor.EditorManager;
import su.nexmedia.engine.integration.VaultHook;
import su.nexmedia.engine.lang.EngineLang;
import su.nexmedia.engine.utils.EngineUtils;
import su.nexmedia.engine.utils.Placeholders;
import su.nexmedia.engine.utils.blocktracker.PlayerBlockTracker;

import java.util.HashSet;
import java.util.Set;

public class NexEngine extends NexPlugin<NexEngine> {

    private final Set<NexPlugin<?>> childrens = new HashSet<>();

    private EditorManager editorManager;
    private MenuListener menuListener;
    private MenuRefreshTask menuRefreshTask;

    @Override
    @NotNull
    protected NexEngine getSelf() {
        return this;
    }

    @Override
    public void enable() {
        this.menuListener = new MenuListener(this);
        this.menuListener.registerListeners();

        this.menuRefreshTask = new MenuRefreshTask(this);
        this.menuRefreshTask.start();

        this.editorManager = new EditorManager(this);
        this.editorManager.setup();

        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
    }

    @Override
    public void disable() {
        if (this.editorManager != null) this.editorManager.shutdown();
        if (this.menuListener != null) this.menuListener.unregisterListeners();
        if (this.menuRefreshTask != null) this.menuRefreshTask.stop();

        if (EngineUtils.hasVault()) VaultHook.shutdown();
        PlayerBlockTracker.shutdown();

        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
    }

    @Override
    public void registerHooks() {
        if (EngineUtils.hasVault()) {
            VaultHook.setup();
        }
    }

    @Override
    public void registerCommands(@NotNull GeneralCommand<NexEngine> mainCommand) {
        mainCommand.addChildren(new ReloadSubCommand<>(this, Placeholders.WILDCARD));
    }

    @Override
    public void registerPermissions() {

    }

    @Override
    public void loadConfig() {
        this.getConfig().initializeOptions(EngineConfig.class);
    }

    @Override
    public void loadLang() {
        this.getLangManager().loadMissing(EngineLang.class);
        this.getLangManager().loadEditor(EditorLocales.class);
        this.getLang().saveChanges();
    }

    void addChildren(@NotNull NexPlugin<?> child) {
        this.childrens.add(child);
        child.info("Powered by: " + this.getName());
    }

    @NotNull
    public Set<NexPlugin<?>> getChildrens() {
        return this.childrens;
    }
}

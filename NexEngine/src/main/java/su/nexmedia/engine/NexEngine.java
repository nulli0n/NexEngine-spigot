package su.nexmedia.engine;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.command.GeneralCommand;
import su.nexmedia.engine.api.editor.EditorLocales;
import su.nexmedia.engine.api.menu.impl.MenuListener;
import su.nexmedia.engine.editor.EditorManager;
import su.nexmedia.engine.integration.VaultHook;
import su.nexmedia.engine.lang.EngineLang;
import su.nexmedia.engine.nms.*;
import su.nexmedia.engine.utils.EngineUtils;

import java.util.HashSet;
import java.util.Set;

public class NexEngine extends NexPlugin<NexEngine> {

    private static NexEngine instance;

    private final Set<NexPlugin<?>> childrens = new HashSet<>();

    private NMS nms;
    private EditorManager editorManager;
    private MenuListener menuListener;

    public NexEngine() {
        instance = this;
    }

    @NotNull
    public static NexEngine get() {
        return instance;
    }

    @Override
    @NotNull
    protected NexEngine getSelf() {
        return this;
    }

    final boolean loadCore() {
        if (!this.setupNMS()) {
            this.error("Your server version is unsupported!");
            return false;
        }
        this.info("Loaded NMS version: " + Version.getCurrent().name());
        return true;
    }

    private boolean setupNMS() {
        this.nms = switch (Version.getCurrent()) {
            case V1_19_R1, V1_19_R2, UNKNOWN -> null;
            case V1_17_R1 -> new V1_17_R1();
            case V1_18_R2 -> new V1_18_R2();
            case V1_19_R3 -> new V1_19_R3();
            case V1_20_R1 -> new V1_20_R1();
        };
        return this.nms != null;
    }

    @Override
    public void enable() {
        this.menuListener = new MenuListener(this);
        this.menuListener.registerListeners();

        this.editorManager = new EditorManager(this);
        this.editorManager.setup();
    }

    @Override
    public void disable() {
        if (this.editorManager != null) {
            this.editorManager.shutdown();
            this.editorManager = null;
        }
        if (this.menuListener != null) {
            this.menuListener.unregisterListeners();
            this.menuListener = null;
        }

        if (EngineUtils.hasVault()) VaultHook.shutdown();
    }

    @Override
    public void registerHooks() {
        if (EngineUtils.hasVault()) {
            VaultHook.setup();
        }
    }

    @Override
    public void registerCommands(@NotNull GeneralCommand<NexEngine> mainCommand) {

    }

    @Override
    public void registerPermissions() {

    }

    @Override
    public void loadConfig() {

    }

    @Override
    public void loadLang() {
        this.getLangManager().loadMissing(EngineLang.class);
        this.getLangManager().loadEditor(EditorLocales.class);
        this.getLang().saveChanges();
    }

    void addChildren(@NotNull NexPlugin<?> child) {
        this.childrens.add(child);
    }

    @NotNull
    public Set<NexPlugin<?>> getChildrens() {
        return this.childrens;
    }

    @NotNull
    public NMS getNMS() {
        return nms;
    }
}

package su.nexmedia.engine;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.command.GeneralCommand;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.config.EngineConfig;
import su.nexmedia.engine.editor.EditorManager;
import su.nexmedia.engine.hooks.Hooks;
import su.nexmedia.engine.hooks.external.VaultHook;
import su.nexmedia.engine.hooks.external.citizens.CitizensHook;
import su.nexmedia.engine.lang.EngineLang;
import su.nexmedia.engine.nms.NMS;
import su.nexmedia.engine.utils.Reflex;

import java.util.HashSet;
import java.util.Set;

public class NexEngine extends NexPlugin<NexEngine> {

    private static NexEngine instance;
    private Set<NexPlugin<?>> childrens;

    NMS nms;
    private EditorManager editorManager;

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
        this.childrens = new HashSet<>();

        if (!this.setupNMS()) {
            this.error("Could not setup NMS version. Plugin will be disabled.");
            return false;
        }

        this.editorManager = new EditorManager(this);
        this.editorManager.setup();

        return true;
    }

    private boolean setupNMS() {
        Version current = Version.CURRENT;

        String pack = NMS.class.getPackage().getName();
        Class<?> clazz = Reflex.getClass(pack, current.name());
        if (clazz == null) return false;

        try {
            this.nms = (NMS) clazz.getConstructor().newInstance();
            this.info("Loaded NMS version: " + current.name());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return this.nms != null;
    }

    @Override
    public void enable() {

    }

    @Override
    public void disable() {
        if (this.editorManager != null) {
            this.editorManager.shutdown();
            this.editorManager = null;
        }

        if (Hooks.hasCitizens()) CitizensHook.shutdown();
        if (Hooks.hasVault()) VaultHook.shutdown();
    }

    @Override
    public void registerHooks() {
        if (Hooks.hasVault()) {
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
        EngineConfig.load(this);
    }

    @Override
    public void loadLang() {
        this.getLangManager().loadMissing(EngineLang.class);
        this.getLangManager().setupEditorEnum(MenuItemType.class);
        this.getLang().saveChanges();
    }

    void addChildren(@NotNull NexPlugin<?> child) {
        this.childrens.add(child);
    }

    @NotNull
    public Set<NexPlugin<?>> getChildrens() {
        return this.childrens;
    }
}

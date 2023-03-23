package su.nexmedia.engine;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.command.GeneralCommand;
import su.nexmedia.engine.api.editor.EditorLocales;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.api.menu.impl.MenuListener;
import su.nexmedia.engine.editor.EditorManager;
import su.nexmedia.engine.hooks.Hooks;
import su.nexmedia.engine.hooks.external.VaultHook;
import su.nexmedia.engine.hooks.external.citizens.CitizensHook;
import su.nexmedia.engine.lang.EngineLang;
import su.nexmedia.engine.nms.*;
import su.nexmedia.engine.utils.CollectionsUtil;

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
            this.error("Your server version is unsupported! Please, upgrade to the one of followings: " + CollectionsUtil.getEnumsList(Version.class));
            return false;
        }
        return true;
    }

    private boolean setupNMS() {
        try {
            this.nms = switch (Version.CURRENT) {
                case V1_17_R1 -> new V1_17_R1();
                case V1_18_R2 -> new V1_18_R2();
                case V1_19_R1 -> new V1_19_R1();
                case V1_19_R2 -> new V1_19_R2();
                case V1_19_R3 -> new V1_19_R3();
            };
            this.info("Loaded NMS version: " + Version.CURRENT.name());
            return true;
        }
        catch (Exception e) {
            return false;
        }
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

    }

    @Override
    public void loadLang() {
        this.getLangManager().loadMissing(EngineLang.class);
        this.getLangManager().loadEditor(EditorLocales.class);
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

    @NotNull
    public NMS getNMS() {
        return nms;
    }
}

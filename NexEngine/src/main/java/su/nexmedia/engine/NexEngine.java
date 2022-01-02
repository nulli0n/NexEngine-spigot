package su.nexmedia.engine;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.actions.ActionsManager;
import su.nexmedia.engine.api.command.GeneralCommand;
import su.nexmedia.engine.api.module.AbstractExternalModule;
import su.nexmedia.engine.core.config.CoreConfig;
import su.nexmedia.engine.core.config.CoreLang;
import su.nexmedia.engine.craft.CraftManager;
import su.nexmedia.engine.hooks.HookManager;
import su.nexmedia.engine.hooks.Hooks;
import su.nexmedia.engine.hooks.external.MythicMobsHook;
import su.nexmedia.engine.hooks.external.VaultHook;
import su.nexmedia.engine.hooks.external.WorldGuardHook;
import su.nexmedia.engine.hooks.external.citizens.CitizensHook;
import su.nexmedia.engine.manager.packet.PacketManager;
import su.nexmedia.engine.manager.player.PlayerManager;
import su.nexmedia.engine.nms.NMS;
import su.nexmedia.engine.utils.Reflex;

import java.util.HashSet;
import java.util.Set;

public class NexEngine extends NexPlugin<NexEngine> implements Listener {

    private static NexEngine         instance;
    private final  Set<NexPlugin<?>> plugins;

    private CoreConfig cfg;
    private CoreLang   lang;

    NMS            nms;

    PluginManager  pluginManager;
    PacketManager  packetManager;
    ActionsManager actionsManager;
    CraftManager   craftManager;
    private HookManager   hookManager;
    private PlayerManager playerManager;

    VaultHook      hookVault;
    CitizensHook   hookCitizens;
    WorldGuardHook hookWorldGuard;
    MythicMobsHook hookMythicMobs;

    public NexEngine() {
        instance = this;
        this.plugins = new HashSet<>();
    }

    @NotNull
    public static NexEngine get() {
        return instance;
    }

    final boolean loadCore() {
        this.pluginManager = this.getServer().getPluginManager();

        if (!this.setupNMS()) {
            this.error("Could not setup NMS version. Plugin will be disabled.");
            return false;
        }

        this.getPluginManager().registerEvents(this, this);

        this.hookManager = new HookManager(this);
        this.hookManager.setup();

        this.packetManager = new PacketManager(this);
        this.packetManager.setup();

        this.actionsManager = new ActionsManager(this);
        this.actionsManager.setup();

        this.actionsManager = new su.nexmedia.engine.actions.ActionsManager(this);
        this.actionsManager.setup();

        this.craftManager = new CraftManager(this);
        this.craftManager.setup();

        this.playerManager = new PlayerManager(this);
        this.playerManager.setup();

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
        this.getModuleManager().registerExternal(AbstractExternalModule.LoadPriority.HIGH);
        this.getModuleManager().registerExternal(AbstractExternalModule.LoadPriority.LOW);
    }

    @Override
    public void disable() {
        // Unregister Custom Actions Engine
        if (this.actionsManager != null) {
            this.actionsManager.shutdown();
            this.actionsManager = null;
        }
        if (this.playerManager != null) {
            this.playerManager.shutdown();
            this.playerManager = null;
        }
        if (this.packetManager != null) {
            this.packetManager.shutdown();
        }
        if (this.hookManager != null) {
            this.hookManager.shutdown();
        }
        if (this.craftManager != null) {
            this.craftManager.shutdown();
            this.craftManager = null;
        }
    }

    @Override
    public void registerHooks() {
        this.hookVault = this.registerHook(Hooks.VAULT, VaultHook.class);
    }

    @Override
    public void registerCommands(@NotNull GeneralCommand<NexEngine> mainCommand) {

    }

    @Override
    public void setConfig() {
        this.cfg = new CoreConfig(this);
        this.cfg.setup();

        this.lang = new CoreLang(this);
        this.lang.setup();
    }

    @Override
    @NotNull
    public CoreConfig cfg() {
        return this.cfg;
    }

    @Override
    @NotNull
    public CoreLang lang() {
        return this.lang;
    }

    @NotNull
    public HookManager getHookManager() {
        return this.hookManager;
    }

    @NotNull
    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    void hookChild(@NotNull NexPlugin<?> child) {
        this.plugins.add(child);
    }

    @NotNull
    public Set<NexPlugin<?>> getChildPlugins() {
        return this.plugins;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onHookLate(PluginEnableEvent e) {
        String name = e.getPlugin().getName();
        if (this.hookMythicMobs == null && name.equalsIgnoreCase(Hooks.MYTHIC_MOBS)) {
            this.hookMythicMobs = this.registerHook(Hooks.MYTHIC_MOBS, MythicMobsHook.class);
            return;
        }
        if (this.hookWorldGuard == null && name.equalsIgnoreCase(Hooks.WORLD_GUARD)) {
            this.hookWorldGuard = this.registerHook(Hooks.WORLD_GUARD, WorldGuardHook.class);
            return;
        }
        if (this.hookCitizens == null && name.equalsIgnoreCase(Hooks.CITIZENS)) {
            this.hookCitizens = this.registerHook(Hooks.CITIZENS, CitizensHook.class);
            return;
        }
    }
}

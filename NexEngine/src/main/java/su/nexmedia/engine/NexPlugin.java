package su.nexmedia.engine;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.actions.ActionsManager;
import su.nexmedia.engine.actions.parameter.AbstractParametized;
import su.nexmedia.engine.api.command.GeneralCommand;
import su.nexmedia.engine.api.config.ConfigTemplate;
import su.nexmedia.engine.api.data.UserDataHolder;
import su.nexmedia.engine.api.manager.ILogger;
import su.nexmedia.engine.api.menu.IMenu;
import su.nexmedia.engine.hooks.Hooks;
import su.nexmedia.engine.manager.packet.PacketManager;
import su.nexmedia.engine.command.CommandManager;
import su.nexmedia.engine.command.PluginMainCommand;
import su.nexmedia.engine.config.ConfigManager;
import su.nexmedia.engine.core.config.CoreLang;
import su.nexmedia.engine.craft.CraftManager;
import su.nexmedia.engine.hooks.HookManager;
import su.nexmedia.engine.api.hook.AbstractHook;
import su.nexmedia.engine.hooks.external.MythicMobsHook;
import su.nexmedia.engine.hooks.external.VaultHook;
import su.nexmedia.engine.hooks.external.WorldGuardHook;
import su.nexmedia.engine.hooks.external.citizens.CitizensHook;
import su.nexmedia.engine.module.ModuleManager;
import su.nexmedia.engine.nms.NMS;

import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;

public abstract class NexPlugin<P extends NexPlugin<P>> extends JavaPlugin implements ILogger {

    public static final String TM = "NEX-Media";

    protected ConfigManager<P>  configManager;
    protected CommandManager<P> commandManager;
    protected ModuleManager<P>  moduleManager;
    private Logger  logger;
    private boolean isEngine;

    @NotNull
    public static NexEngine getEngine() {
        return NexEngine.get();
    }

    public final boolean isEngine() {
        return this.isEngine;
    }

    @Deprecated
    public boolean useNewConfigFields() {
        return true;
    }

    @Override
    public final void onEnable() {
        long loadTook = System.currentTimeMillis();
        this.logger = this.getLogger();
        this.isEngine = this instanceof NexEngine;

        NexEngine engine = getEngine();
        if (this.isEngine()) {
            if (!engine.loadCore()) {
                this.getPluginManager().disablePlugin(this);
                return;
            }
        }
        else {
            engine.hookChild(this);
            this.info("Powered by: " + engine.getName());
        }
        this.loadManagers();
        this.info("Plugin loaded in " + (System.currentTimeMillis() - loadTook) + " ms!");
    }

    @Override
    public final void onDisable() {
        this.unloadManagers();
    }

    public abstract void enable();

    public abstract void disable();

    public final void reload() {
        if (this.isEngine()) {
            this.setConfig();
            return;
        }
        this.unloadManagers();
        this.loadManagers();
    }

    public abstract void setConfig();

    public abstract void registerHooks();

    public void registerCommands(@NotNull GeneralCommand<P> mainCommand) {

    }

    @NotNull
    public abstract ConfigTemplate cfg();

    @NotNull
    public abstract CoreLang lang();

    @Override
    public final void info(@NotNull String msg) {
        this.logger.info(msg);
    }

    @Override
    public final void warn(@NotNull String msg) {
        this.logger.warning(msg);
    }

    @Override
    public final void error(@NotNull String msg) {
        this.logger.severe(msg);
    }

    @Nullable
    public final <T extends AbstractHook<P>> T registerHook(@NotNull String pluginName, @NotNull Class<T> clazz) {
        return this.getHooks().register(this, pluginName, clazz);
    }

    private void unregisterListeners() {
        for (Player player : this.getServer().getOnlinePlayers()) {
            IMenu menu = IMenu.getMenu(player);
            if (menu != null) {
                player.closeInventory();
            }
        }
        HandlerList.unregisterAll(this);
    }

    @SuppressWarnings("unchecked")
    protected void loadManagers() {
        // Setup plugin Hooks.
        this.registerHooks();

        // Setup ConfigManager before any other managers.
        this.configManager = new ConfigManager<>((P) this);
        this.configManager.setup();
        if (this.cfg().commandAliases == null || this.cfg().commandAliases.length == 0) {
            this.error("Could not register plugin commands!");
            this.getPluginManager().disablePlugin(this);
            return;
        }

        // Connect to the database if present.
        UserDataHolder<?, ?> dataHolder = null;
        if (this instanceof UserDataHolder) {
            dataHolder = (UserDataHolder<?, ?>) this;
            if (!dataHolder.setupDataHandlers()) {
                this.error("Could not setup plugin Data Handler!");
                this.getPluginManager().disablePlugin(this);
                return;
            }
        }

        // Register plugin commands.
        this.commandManager = new CommandManager<>((P) this);
        this.commandManager.setup();

        // Register plugin modules.
        this.moduleManager = new ModuleManager<>((P) this);
        this.moduleManager.setup();

        // Custom plugin loaders.
        this.enable();

        // Load plugin users only when full plugin is loaded.
        if (dataHolder != null) {
            dataHolder.getUserManager().loadOnlineUsers();
        }

        AbstractParametized.clearCache();
    }

    private void unloadManagers() {
        this.getServer().getScheduler().cancelTasks(this); // First stop all plugin tasks

        if (this.moduleManager != null) {
            this.moduleManager.shutdown();
        }
        this.disable();
        if (this.commandManager != null) {
            this.commandManager.shutdown();
        }

        // Unregister all plugin traits and NPC listeners.
        if (Hooks.hasCitizens()) {
            CitizensHook.unregisterTraits(this);
            CitizensHook.unregisterListeners(this);
        }

        // Unregister all plugin hooks.
        //if (!this.isEngine()) {
            this.getHooks().shutdown(this);
        //}

        // Unregister ALL plugin listeners.
        this.unregisterListeners();

        // Save user data and disconnect from the database.
        if (this instanceof UserDataHolder<?, ?> dataHolder) {
            dataHolder.getUserManager().shutdown();
            dataHolder.getData().shutdown();
        }
    }

    @NotNull
    public final String getAuthor() {
        List<String> list = this.getDescription().getAuthors();
        return list.isEmpty() ? TM : list.get(0);
    }

    @NotNull
    public final String getNameRaw() {
        return this.getName().toLowerCase().replace(" ", "").replace("-", "");
    }

    @NotNull
    public final String getLabel() {
        return this.getLabels()[0];
    }

    @NotNull
    public final String[] getLabels() {
        return this.cfg().commandAliases;
    }

    @NotNull
    public final NMS getNMS() {
        return getEngine().nms;
    }

    public final PluginMainCommand<P> getMainCommand() {
        return this.getCommandManager().getMainCommand();
    }

    @NotNull
    public final ConfigManager<P> getConfigManager() {
        return this.configManager;
    }

    public final CommandManager<P> getCommandManager() {
        return this.commandManager;
    }

    @NotNull
    public final CraftManager getCraftManager() {
        return getEngine().craftManager;
    }

    @NotNull
    public final ModuleManager<P> getModuleManager() {
        return this.moduleManager;
    }

    @NotNull
    public final ActionsManager getActionsManager() {
        return getEngine().actionsManager;
    }

    @NotNull
    public final PacketManager getPacketManager() {
        return getEngine().packetManager;
    }

    @NotNull
    public final PluginManager getPluginManager() {
        return getEngine().pluginManager;
    }

    @NotNull
    public final HookManager getHooks() {
        return getEngine().getHookManager();
    }

    public final boolean isHooked(@NotNull Class<? extends AbstractHook<?>> clazz) {
        return this.getHooks().isHooked(this, clazz);
    }

    public final boolean isHooked(@NotNull String plugin) {
        return this.getHooks().isHooked(this, plugin);
    }

    @Nullable
    public final <T extends AbstractHook<?>> T getHook(@NotNull Class<T> clazz) {
        return this.getHooks().getHook(this, clazz);
    }

    @Nullable
    public final AbstractHook<? extends NexPlugin<?>> getHook(@NotNull String name) {
        return this.getHooks().getHook(this, name);
    }

    @Nullable
    @Deprecated
    public final VaultHook getVault() {
        return getEngine().hookVault;
    }

    @Nullable
    @Deprecated
    public final CitizensHook getCitizens() {
        return getEngine().hookCitizens;
    }

    @Nullable
    @Deprecated
    public final WorldGuardHook getWorldGuard() {
        return getEngine().hookWorldGuard;
    }

    @Nullable
    @Deprecated
    public final MythicMobsHook getMythicMobs() {
        return getEngine().hookMythicMobs;
    }

    public ClassLoader getClazzLoader() {
        return this.getClassLoader();
    }

    public final void runTask(@NotNull Consumer<BukkitTask> consume, boolean async) {
        if (async) {
            this.getServer().getScheduler().runTaskAsynchronously(this, consume);
        }
        else {
            this.getServer().getScheduler().runTask(this, consume);
        }
    }
}

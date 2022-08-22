package su.nexmedia.engine.api.data;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.api.manager.AbstractListener;
import su.nexmedia.engine.api.manager.AbstractManager;
import su.nexmedia.engine.api.task.AbstractTask;
import su.nexmedia.engine.hooks.Hooks;

import java.util.*;

public abstract class AbstractUserManager<P extends NexPlugin<P>, U extends AbstractUser<P>> extends AbstractManager<P> {

    private final UserDataHolder<P, U> dataHolder;
    private final Map<UUID, U> usersLoaded;
    private SaveTask saveTask;
    private SyncTask syncTask;

    public AbstractUserManager(@NotNull P plugin, @NotNull UserDataHolder<P, U> dataHolder) {
        super(plugin);
        this.dataHolder = dataHolder;
        this.usersLoaded = new HashMap<>();
    }

    @Override
    protected void onLoad() {
        this.addListener(new PlayerListener(this.plugin));

        this.saveTask = new SaveTask(this.plugin);
        this.saveTask.start();

        if (this.plugin.getConfigManager().dataSaveInstant && this.plugin.getConfigManager().dataSyncInterval > 0) {
            this.syncTask = new SyncTask(this.plugin);
            this.syncTask.start();
            this.plugin.info("Enabled data synchronization with " + plugin.getConfigManager().dataSyncInterval + " seconds interval.");
        }
        else {
            this.plugin.warn("Data synchronization is disabled because 'Instant_Save' option is not enabled.");
        }
    }

    @Override
    protected void onShutdown() {
        if (this.saveTask != null) {
            this.saveTask.stop();
            this.saveTask = null;
        }
        if (this.syncTask != null) {
            this.syncTask.stop();
            this.syncTask = null;
        }
        this.onSynchronize();
        this.autosave();
        this.getUsersLoadedMap().clear();
    }

    protected abstract void onSynchronize();

    @NotNull
    protected abstract U createData(@NotNull UUID uuid, @NotNull String name);

    public void loadOnlineUsers() {
        this.plugin.getServer().getOnlinePlayers().stream().map(Player::getUniqueId).forEach(this::getUserData);
    }

    public void autosave() {
        int off = 0;
        for (U userOn : new HashSet<>(this.getUsersLoaded())) {
            if (!userOn.isOnline()) {
                this.getUsersLoadedMap().remove(userOn.getUUID());
                off++;
            }
            this.save(userOn);
        }

        int on = this.getUsersLoadedMap().size();
        this.plugin.info("Auto-save: Saved " + on + " online users | " + off + " offline users.");
    }

    public void save(@NotNull U user) {
        this.dataHolder.getData().saveUser(user);
    }

    public void save(@NotNull U user, boolean async) {
        this.plugin.runTask(c -> this.dataHolder.getData().saveUser(user), async);
    }

    @NotNull
    @Deprecated
    public final U getOrLoadUser(@NotNull Player player) {
        return this.getUserData(player);
    }

    /**
     * Gets the preloaded user data for specified player.
     * Throwns an exception if user data is not loaded for the player, because it have to be loaded on player login.
     * @param player A player instance to get user data for.
     * @return User data for the specified player.
     */
    @NotNull
    public final U getUserData(@NotNull Player player) {
        if (Hooks.isCitizensNPC(player)) {
            throw new IllegalStateException("Could not load user data from an NPC!");
        }

        U user = this.getUserLoaded(player.getUniqueId());
        if (user == null) {
            throw new IllegalStateException("User data for '" + player.getName() + "' is not loaded or created!");
        }
        return user;
    }

    @Nullable
    @Deprecated
    public final U getOrLoadUser(@NotNull String name) {
        return this.getUserData(name);
    }

    /**
     * Attempts to load user data from online player with that Name (if there is any).
     * In case if no such player is online, attempts to load data from the database.
     * @param name A user name to load data for.
     * @return User data for the specified user name.
     */
    @Nullable
    public final U getUserData(@NotNull String name) {
        Player player = this.plugin.getServer().getPlayer(name);
        if (player != null) return this.getUserData(player);

        U user = this.getUsersLoaded().stream().filter(userOff -> userOff.getName().equalsIgnoreCase(name))
            .findFirst().orElse(null);
        if (user != null) return user;

        user = this.dataHolder.getData().getUser(name, false);
        if (user != null) {
            user.onLoad();
            this.cache(user);
        }

        return user;
    }

    @Nullable
    @Deprecated
    public final U getOrLoadUser(@NotNull UUID uuid) {
        return this.getUserData(uuid);
    }

    /**
     * Attempts to load user data from online player with that UUID (if there is any).
     * In case if no such player is online, attempts to load data from the database.
     * @param uuid A user unique id to load data for.
     * @return User data for the specified uuid.
     */
    @Nullable
    public final U getUserData(@NotNull UUID uuid) {
        U user = this.getUserLoaded(uuid);
        if (user != null) return user;

        user = this.dataHolder.getData().getUser(uuid);
        if (user != null) {
            user.onLoad();
            this.cache(user);
        }

        return user;
    }

    public final void unloadUser(@NotNull Player player) {
        this.unloadUser(player.getUniqueId());
    }

    public final void unloadUser(@NotNull UUID uuid) {
        U user = this.getUsersLoadedMap().remove(uuid);
        if (user == null) return;

        this.unloadUser(user);
    }

    public void unloadUser(@NotNull U user) {
        user.onUnload();
        this.save(user, true);
    }

    @NotNull
    public Map<UUID, @NotNull U> getUsersLoadedMap() {
        return this.usersLoaded;
    }

    @NotNull
    public Collection<@NotNull U> getUsersLoaded() {
        return this.getUsersLoadedMap().values();
    }

    @Nullable
    public U getUserLoaded(@NotNull UUID uuid) {
        return this.getUsersLoadedMap().get(uuid);
    }

    public boolean isUserLoaded(@NotNull Player player) {
        return this.getUsersLoadedMap().containsKey(player.getUniqueId());
    }

    public void cache(@NotNull U user) {
        this.getUsersLoadedMap().put(user.getUUID(), user);
    }

    class PlayerListener extends AbstractListener<P> {

        public PlayerListener(@NotNull P plugin) {
            super(plugin);
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onUserLogin(AsyncPlayerPreLoginEvent e) {
            if (e.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) return;

            UUID uuid = e.getUniqueId();
            U user;
            if (!dataHolder.getData().isUserExists(uuid.toString(), true)) {
                user = createData(uuid, e.getName());
                user.setRecentlyCreated(true);
                cache(user);
                dataHolder.getData().addUser(user);
                plugin.info("Created new user data for: '" + uuid + "'");
                return;
            }
            else {
                user = getUserData(uuid);
            }

            if (user == null) {
                e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "Unable to load your user data.");
            }
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onUserQuit(PlayerQuitEvent e) {
            unloadUser(e.getPlayer());
        }
    }

    class SaveTask extends AbstractTask<P> {

        SaveTask(@NotNull P plugin) {
            super(plugin, plugin.getConfigManager().dataSaveInterval * 60, true);
        }

        @Override
        public void action() {
            autosave();
        }
    }

    class SyncTask extends AbstractTask<P> {

        public SyncTask(@NotNull P plugin) {
            super(plugin, plugin.getConfigManager().dataSyncInterval, true);
        }

        @Override
        public void action() {
            onSynchronize();
        }
    }
}

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
import su.nexmedia.engine.config.EngineConfig;
import su.nexmedia.engine.utils.PlayerUtil;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public abstract class AbstractUserManager<P extends NexPlugin<P>, U extends AbstractUser<P>> extends AbstractManager<P> {

    private final UserDataHolder<P, U> dataHolder;
    private final Map<UUID, U> usersLoaded;

    public AbstractUserManager(@NotNull P plugin, @NotNull UserDataHolder<P, U> dataHolder) {
        super(plugin);
        this.dataHolder = dataHolder;
        this.usersLoaded = new ConcurrentHashMap<>();
    }

    @Override
    protected void onLoad() {
        this.addListener(new PlayerListener(this.plugin));
    }

    @Override
    protected void onShutdown() {
        this.getUsersLoaded().forEach(user -> this.dataHolder.getData().saveUser(user));
        this.getUsersLoadedMap().clear();
    }

    @NotNull
    protected abstract U createData(@NotNull UUID uuid, @NotNull String name);

    public void loadOnlineUsers() {
        this.plugin.getServer().getOnlinePlayers().stream().map(Player::getUniqueId).forEach(id -> {
            U user = this.getUserData(id);
            if (user != null) this.cachePermanent(user);
        });
    }

    @NotNull
    public final U getUserData(@NotNull Player player) {
        UUID uuid = player.getUniqueId();

        U user = this.getUserLoaded(uuid);
        if (user != null) return user;

        if (PlayerUtil.isReal(player)) {
            user = this.getUserData(uuid);
            if (user != null) {
                if (EngineConfig.USER_DEBUG_ENABLED.get()) {
                    new Throwable().printStackTrace();
                    this.plugin.warn("Main thread user data load for '" + uuid + "' aka '" + player.getName() + "'.");
                }
                return user;
            }
        }

        return this.createData(uuid, player.getName());
    }

    /**
     * Attempts to load user data from online or cached player with that name.
     * If no such player data found, attempts to load it from the database.
     * @param name A user name to load data for.
     * @return User data for the specified user name.
     */
    @Nullable
    public final U getUserData(@NotNull String name) {
        Player player = PlayerUtil.getPlayer(name);
        if (player != null) return this.getUserData(player);

        U user = this.getUserLoaded(name);
        if (user != null) return user;

        user = this.dataHolder.getData().getUser(name);
        if (user != null) {
            user.onLoad();
            //this.plugin.debug("Loaded by name from DB: " + user.getName());
            this.cacheTemporary(user);
        }

        return user;
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
            //this.plugin.debug("Loaded by UUID from DB: " + user.getName());
            this.cacheTemporary(user);
        }

        return user;
    }

    public final CompletableFuture<U> getUserDataAsync(@NotNull String name) {
        return CompletableFuture.supplyAsync(() -> this.getUserData(name));
    }

    public final CompletableFuture<U> getUserDataAsync(@NotNull UUID uuid) {
        return CompletableFuture.supplyAsync(() -> this.getUserData(uuid));
    }

    public void getUserDataAndPerform(@NotNull String name, Consumer<U> consumer) {
        U user = this.getUserLoaded(name);
        if (user != null) {
            consumer.accept(user);
        }
        else this.getUserDataAsync(name).thenAccept(user2 -> this.plugin.runTask(task -> consumer.accept(user2)));
    }

    public void getUserDataAndPerform(@NotNull UUID uuid, Consumer<U> consumer) {
        U user = this.getUserLoaded(uuid);
        if (user != null) {
            consumer.accept(user);
        }
        else this.getUserDataAsync(uuid).thenAccept(user2 -> this.plugin.runTask(task -> consumer.accept(user2)));
    }

    public void getUserDataAndPerformAsync(@NotNull String name, Consumer<U> consumer) {
        U user = this.getUserLoaded(name);
        if (user != null) {
            consumer.accept(user);
        }
        else this.getUserDataAsync(name).thenAccept(consumer);
    }

    public void getUserDataAndPerformAsync(@NotNull UUID uuid, Consumer<U> consumer) {
        U user = this.getUserLoaded(uuid);
        if (user != null) {
            consumer.accept(user);
        }
        else this.getUserDataAsync(uuid).thenAccept(consumer);
    }

    public final void unloadUser(@NotNull Player player) {
        this.unloadUser(player.getUniqueId());
    }

    public final void unloadUser(@NotNull UUID uuid) {
        U user = this.getUsersLoadedMap().get(uuid);
        if (user == null) return;

        this.unloadUser(user);
    }

    public void unloadUser(@NotNull U user) {
        Player player = user.getPlayer();
        if (player != null) {
            user.setName(player.getName());
            user.setLastOnline(System.currentTimeMillis());
        }
        this.saveUser(user);
        this.cacheTemporary(user);
    }

    public void saveUser(@NotNull U user) {
        this.plugin.runTaskAsync(task -> this.dataHolder.getData().saveUser(user));
    }

    @NotNull
    public Set<U> getAllUsers() {
        Map<UUID, U> users = new HashMap<>();
        this.getUsersLoaded().forEach(user -> users.put(user.getId(), user));
        this.dataHolder.getData().getUsers().forEach(user -> {
            if (!users.containsKey(user.getId())) {
                users.put(user.getId(), user);
            }
        });
        return new HashSet<>(users.values());
    }

    @NotNull
    public Map<UUID, U> getUsersLoadedMap() {
        this.usersLoaded.values().removeIf(user -> {
            if (user.isCacheExpired()) {
                user.onUnload();
                //this.plugin.debug("Cache expired: " + user.getName());
                return true;
            }
            return false;
        });

        return this.usersLoaded;
    }

    @NotNull
    public Collection<U> getUsersLoaded() {
        return new HashSet<>(this.getUsersLoadedMap().values());
    }

    @Nullable
    public U getUserLoaded(@NotNull UUID uuid) {
        return this.getUsersLoadedMap().get(uuid);
    }

    @Nullable
    public U getUserLoaded(@NotNull String name) {
        return this.getUsersLoaded().stream().filter(user -> user.getName().equalsIgnoreCase(name))
            .findFirst().orElse(null);
    }

    public boolean isUserLoaded(@NotNull Player player) {
        return this.getUsersLoadedMap().containsKey(player.getUniqueId());
    }

    public boolean isUserExists(@NotNull String name) {
        return this.dataHolder.getData().isUserExists(name);
    }

    public boolean isUserExists(@NotNull UUID uuid) {
        return this.dataHolder.getData().isUserExists(uuid);
    }

    public void cacheTemporary(@NotNull U user) {
        user.setCachedUntil(System.currentTimeMillis() + EngineConfig.USER_CACHE_LIFETIME.get() * 1000L);
        this.cache(user);
        //this.plugin.debug("Temp user cache: " + user.getName());
    }

    public void cachePermanent(@NotNull U user) {
        user.setCachedUntil(-1);
        this.cache(user);
        //this.plugin.debug("Permanent user cache: " + user.getName());
    }

    private void cache(@NotNull U user) {
        this.getUsersLoadedMap().putIfAbsent(user.getId(), user);
    }

    class PlayerListener extends AbstractListener<P> {

        public PlayerListener(@NotNull P plugin) {
            super(plugin);
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onUserLogin(AsyncPlayerPreLoginEvent event) {
            if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) return;

            UUID uuid = event.getUniqueId();
            U user;
            if (!isUserExists(uuid)) {
                user = createData(uuid, event.getName());
                user.setRecentlyCreated(true);
                dataHolder.getData().addUser(user);
                plugin.info("Created new user data for: '" + uuid + "'");
            }
            else user = getUserData(uuid);

            if (user != null) {
                cachePermanent(user);
            }
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onUserQuit(PlayerQuitEvent event) {
            unloadUser(event.getPlayer());
        }
    }
}

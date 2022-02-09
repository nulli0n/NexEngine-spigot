package su.nexmedia.engine.api.data;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.NexPlugin;
import su.nexmedia.engine.api.data.event.EngineUserCreatedEvent;
import su.nexmedia.engine.api.data.event.EngineUserLoadEvent;
import su.nexmedia.engine.api.data.event.EngineUserUnloadEvent;
import su.nexmedia.engine.api.manager.AbstractListener;
import su.nexmedia.engine.api.manager.AbstractManager;
import su.nexmedia.engine.hooks.Hooks;
import su.nexmedia.engine.api.task.AbstractTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractUserManager<P extends NexPlugin<P>, U extends AbstractUser<P>> extends AbstractManager<P> {

    private final UserDataHolder<P, U> dataHolder;

    private Map<String, U> activeUsers;
    private Set<UUID>      isPassJoin;
    private Set<UUID>      toCreate;

    private SaveTask saveTask;

    public AbstractUserManager(@NotNull P plugin, @NotNull UserDataHolder<P, U> dataHolder) {
        super(plugin);
        this.dataHolder = dataHolder;
    }

    @Override
    protected void onLoad() {
        this.activeUsers = new HashMap<>();
        this.isPassJoin = ConcurrentHashMap.newKeySet();
        this.toCreate = ConcurrentHashMap.newKeySet();

        this.addListener(new PlayerListener(this.plugin));

        this.saveTask = new SaveTask(this.plugin);
        this.saveTask.start();
    }

    @Override
    protected void onShutdown() {
        if (this.saveTask != null) {
            this.saveTask.stop();
            this.saveTask = null;
        }
        this.autosave();
        this.activeUsers.clear();
        this.isPassJoin.clear();
        this.toCreate.clear();
    }

    @NotNull
    protected abstract U createData(@NotNull Player player);

    public void loadOnlineUsers() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            this.getOrLoadUser(player);
        }
    }

    public void autosave() {
        int off = 0;
        for (U userOn : new HashSet<>(this.getActiveUsers())) {
            if (!userOn.isOnline()) {
                this.activeUsers.remove(userOn.getUUID().toString());
                off++;
            }
            this.save(userOn);
        }

        int on = this.activeUsers.size();
        plugin.info("Auto-save: Saved " + on + " online users | " + off + " offline users.");
    }

    public void save(@NotNull U user) {
        this.dataHolder.getData().saveUser(user);
    }

    public void save(@NotNull U user, boolean async) {
        this.plugin.runTask(c -> this.dataHolder.getData().saveUser(user), async);
    }

    @NotNull
    public final U getOrLoadUser(@NotNull Player player) {
        if (Hooks.isCitizensNPC(player)) {
            throw new IllegalStateException("Could not load user data from an NPC!");
        }

        String uuid = player.getUniqueId().toString();

        // Check if user is loaded.
        U user = this.activeUsers.get(uuid);
        if (user == null) user = this.dataHolder.getData().getUser(uuid, true);
        if (user != null) {
            this.lateJoin(user);
            return user;
        }

        U user2 = this.createData(player);

        EngineUserCreatedEvent<P, U> event = new EngineUserCreatedEvent<>(plugin, user2);
        this.plugin.getPluginManager().callEvent(event);

        this.plugin.info("Created new user data for: '" + uuid + "'");
        this.plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            this.dataHolder.getData().addUser(user2);
        });
        this.activeUsers.put(uuid, user2);
        this.toCreate.remove(user2.getUUID());
        return user2;
    }

    @Nullable
    public final U getOrLoadUser(@NotNull String nameOrUuid, boolean isUuid) {
        Player playerHolder;
        if (isUuid) playerHolder = plugin.getServer().getPlayer(UUID.fromString(nameOrUuid));
        else playerHolder = plugin.getServer().getPlayer(nameOrUuid);
        if (playerHolder != null) return this.getOrLoadUser(playerHolder);

        // Check by user name.
        for (U userOff : this.getActiveUsers()) {
            if (userOff.getName().equalsIgnoreCase(nameOrUuid)) {
                return userOff;
            }
        }

        // Check if user is loaded.
        U user = this.activeUsers.get(nameOrUuid);
        if (user == null) user = this.dataHolder.getData().getUser(nameOrUuid, isUuid);
        if (user != null) {
            this.lateJoin(user);
            return user;
        }

        return null;
    }

    private void lateJoin(@NotNull U user) {
        this.activeUsers.put(user.getUUID().toString(), user);

        // Игрок уже успел войти полностью на сервер (пройти JoinEvent)
        // поэтому кастомный ивент в JoinEvent вызван не будет, а значит вызываем его здесь.
        if (this.isPassJoin.remove(user.getUUID())) {
            user.setLastOnline(System.currentTimeMillis());

            EngineUserLoadEvent<P, U> event = new EngineUserLoadEvent<>(plugin, user);
            plugin.getPluginManager().callEvent(event);
        }
    }

    public final void unloadUser(@NotNull Player player) {
        U user = this.activeUsers.remove(player.getUniqueId().toString());
        if (user == null) return;

        user.setLastOnline(System.currentTimeMillis());
        this.unloadUser(user);
    }

    public final void unloadUser(@NotNull UUID uuid) {
        U user = this.activeUsers.remove(uuid.toString());
        if (user == null) return;

        this.unloadUser(user);
    }

    private void unloadUser(@NotNull U user) {
        EngineUserUnloadEvent<P, U> event = new EngineUserUnloadEvent<>(plugin, user);
        plugin.getPluginManager().callEvent(event);
        this.save(user, true);
    }

    @NotNull
    public Map<String, @NotNull U> getActiveUsersMap() {
        return this.activeUsers;
    }

    @NotNull
    public Collection<@NotNull U> getActiveUsers() {
        return this.activeUsers.values();
    }

    public boolean isLoaded(@NotNull Player player) {
        return this.activeUsers.containsKey(player.getUniqueId().toString());
    }

    class PlayerListener extends AbstractListener<P> {

        public PlayerListener(@NotNull P plugin) {
            super(plugin);
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onUserLogin(AsyncPlayerPreLoginEvent e) {
            if (e.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) return;

            // For new players, prepare the UserManager to create new data on PlayerJoinEvent.
            if (!dataHolder.getData().isUserExists(e.getUniqueId().toString(), true)) {
                toCreate.add(e.getUniqueId());
                return;
            }

            // For old players, load the user data from the database in async mode.
            getOrLoadUser(e.getUniqueId().toString(), true);
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onUserJoin(PlayerJoinEvent e) {
            Player player = e.getPlayer();

            // Добавляем игрока в джойн лист для дальнейших проверок.
            isPassJoin.add(player.getUniqueId());

            // Если игрок до сих пор не был загружен из БД, при этом запись о нем есть в базе,
            // мы выходим из метода, оставляя его в "джойн" листе, таким образом
            // при завершении загрузки из БД, в методе getOrLoadUser менеджер увидит
            // его и загрузит в память с вызовом кастомного ивента.
            if (!isLoaded(player) && !toCreate.contains(player.getUniqueId())) return;

            // Так как при загрузке данных в асихнронном режиме мы не можем получить объект игрока,
            // то мы получаем уже загруженные его данные здесь для вызова кастомного ивента.
            // Либо здесь же создаются новые данные если игрока не было в базе.
            U user = getOrLoadUser(player);
            //if (user == null) return;

            //user.setLastOnline(System.currentTimeMillis());

            // Call custom UserLoad event.
            EngineUserLoadEvent<P, U> event = new EngineUserLoadEvent<>(plugin, user);
            plugin.getPluginManager().callEvent(event);

            isPassJoin.remove(user.getUUID());
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onUserQuit(PlayerQuitEvent e) {
            unloadUser(e.getPlayer());
        }
    }

    class SaveTask extends AbstractTask<P> {

        SaveTask(@NotNull P plugin) {
            super(plugin, plugin.cfg().dataSaveInterval * 60, true);
        }

        @Override
        public void action() {
            autosave();
        }
    }
}

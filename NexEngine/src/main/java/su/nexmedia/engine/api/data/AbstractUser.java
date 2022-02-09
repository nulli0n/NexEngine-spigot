package su.nexmedia.engine.api.data;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nexmedia.engine.NexPlugin;

import java.util.UUID;

public abstract class AbstractUser<P extends NexPlugin<P>> {

    protected transient final P plugin;

    protected final UUID   uuid;
    protected       String name;
    protected       long   lastOnline;

    private boolean isRecent;

    // Create new user data
    public AbstractUser(@NotNull P plugin, @NotNull Player player) {
        this(plugin, player.getUniqueId(), player.getName(), System.currentTimeMillis());
        this.isRecent = true;
    }

    // Load existent user data
    public AbstractUser(@NotNull P plugin, @NotNull UUID uuid, @NotNull String name, long lastOnline) {
        this.plugin = plugin;
        this.uuid = uuid;
        this.isRecent = false;
        this.setName(name);
        this.setLastOnline(lastOnline);
    }

    public boolean isRecentlyCreated() {
        return isRecent;
    }

    @NotNull
    public final UUID getUUID() {
        return this.uuid;
    }

    @NotNull
    public final String getName() {
        return this.name;
    }

    /**
     * Update stored user names to their mojang names.
     *
     * @param name stored user name.
     */
    public void setName(@NotNull String name) {
        OfflinePlayer offlinePlayer = this.getOfflinePlayer();
        String nameHas = offlinePlayer.getName();
        if (nameHas != null) name = nameHas;

        this.name = name;
    }

    public final long getLastOnline() {
        return this.lastOnline;
    }

    public final void setLastOnline(long lastOnline) {
        this.lastOnline = lastOnline;
    }

    public final boolean isOnline() {
        return this.getPlayer() != null;
    }

    @NotNull
    public final OfflinePlayer getOfflinePlayer() {
        return this.plugin.getServer().getOfflinePlayer(this.getUUID());
    }

    @Nullable
    public final Player getPlayer() {
        return this.plugin.getServer().getPlayer(this.getUUID());
    }

    @Override
    public String toString() {
        return "AbstractUser [uuid=" + this.uuid + ", name=" + this.name + ", lastOnline=" + this.lastOnline + "]";
    }
}
